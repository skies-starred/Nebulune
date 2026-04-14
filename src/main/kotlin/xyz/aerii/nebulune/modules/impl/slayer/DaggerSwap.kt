package xyz.aerii.nebulune.modules.impl.slayer

import net.minecraft.network.chat.Component
import net.minecraft.world.item.Item
import net.minecraft.world.item.Items
import tech.thatgravyboat.skyblockapi.api.datatype.DataTypes
import tech.thatgravyboat.skyblockapi.api.datatype.getData
import xyz.aerii.athen.accessors.attachedNames
import xyz.aerii.athen.annotations.Load
import xyz.aerii.athen.annotations.OnlyIn
import xyz.aerii.athen.api.location.SkyBlockIsland
import xyz.aerii.athen.config.Category
import xyz.aerii.athen.events.TickEvent
import xyz.aerii.athen.modules.Module
import xyz.aerii.library.api.client
import xyz.aerii.library.utils.stripped
import xyz.aerii.nebulune.events.PlayerAttackEvent
import xyz.aerii.nebulune.mixin.accessors.InventoryAccessor
import xyz.aerii.nebulune.utils.rightClick

@Load
@OnlyIn(islands = [SkyBlockIsland.CRIMSON_ISLE])
object DaggerSwap : Module(
    "Dagger swap",
    "Automatically swaps to the correct dagger for blaze!",
    Category.SLAYER
) {
    private val delay by config.slider("Delay", 1, 0, 10, "ticks")
    private val delayVariance by config.slider("Delay variance", 2, 0, 10, "ticks")

    private var last: Attunements? = null
    private var swap: Attunements? = null
    private var wait: Int = -1

    init {
        on<PlayerAttackEvent> {
            for (c in entity.attachedNames) {
                val s = c.string
                if (":" !in s || "♨" !in s) continue

                fn(c)
                break
            }
        }

        on<TickEvent.Client.Start> {
            val swap = swap ?: return@on
            swap(swap)
        }
    }

    private fun fn(component: Component) {
        val n = Attunements.get(component.stripped())?.takeIf { it != last } ?: return

        last = n
        swap = n
        wait = delay + (0..delayVariance).random()
    }

    private fun swap(attr: Attunements) {
        if (wait-- > 0) return
        val inv = client.player?.inventory ?: return
        val acc = inv as InventoryAccessor

        val heldStack = inv.getItem(acc.selectedSlot)
        val heldId = heldStack?.getData(DataTypes.SKYBLOCK_ID)?.skyblockId
        if (heldId != null && heldId in attr.set) {
            if (heldStack.item != attr.item) rightClick()
            swap = null
            return
        }

        for (i in 0..8) {
            val stack = inv.getItem(i) ?: continue
            val id = stack.getData(DataTypes.SKYBLOCK_ID)?.skyblockId ?: continue
            if (id !in attr.set) continue
            if (acc.selectedSlot != i) acc.selectedSlot = i
            return
        }

        swap = null
    }

    private enum class Attunements(val str: String, val set: Set<String>, val item: Item) {
        Ashen("ASHEN ♨", setOf("HEARTFIRE_DAGGER", "BURSTFIRE_DAGGER", "FIREDUST_DAGGER"), Items.STONE_SWORD),
        Auric("AURIC ♨", setOf("HEARTFIRE_DAGGER", "BURSTFIRE_DAGGER", "FIREDUST_DAGGER"), Items.GOLDEN_SWORD),
        Crystal("CRYSTAL ♨", setOf("HEARTMAW_DAGGER", "BURSTMAW_DAGGER", "MAWDUST_DAGGER"), Items.DIAMOND_SWORD),
        Spirit("SPIRIT ♨", setOf("HEARTMAW_DAGGER", "BURSTMAW_DAGGER", "MAWDUST_DAGGER"), Items.IRON_SWORD);

        companion object {
            fun get(a: String) = entries.firstOrNull { it.str in a }
        }
    }
}
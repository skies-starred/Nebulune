package xyz.aerii.nebulune.modules.impl.slayer

import net.minecraft.network.chat.Component
import tech.thatgravyboat.skyblockapi.api.datatype.DataTypes
import tech.thatgravyboat.skyblockapi.api.datatype.getData
import xyz.aerii.athen.annotations.Load
import xyz.aerii.athen.annotations.OnlyIn
import xyz.aerii.athen.api.location.SkyBlockIsland
import xyz.aerii.athen.config.Category
import xyz.aerii.athen.ducks.entity.attachedNames
import xyz.aerii.athen.events.PlayerEvent
import xyz.aerii.athen.events.TickEvent
import xyz.aerii.athen.modules.Module
import xyz.aerii.library.api.client
import xyz.aerii.library.utils.stripped
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
        on<PlayerEvent.Attack.Entity> {
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
        val heldId = heldStack.getData(DataTypes.ID)
        if (heldId != null && heldId in attr.set) {
            if (heldId !in attr.set) rightClick()
            swap = null
            return
        }

        for (i in 0..8) {
            val stack = inv.getItem(i)
            val id = stack.getData(DataTypes.ID) ?: continue
            if (id !in attr.set) continue
            if (acc.selectedSlot != i) acc.selectedSlot = i
            return
        }

        swap = null
    }

    private enum class Attunements(val str: String, val set: Set<String>) {
        Ashen("ASHEN ♨", setOf("HEARTFIRE_DAGGER", "BURSTFIRE_DAGGER", "FIREDUST_DAGGER")),
        Auric("AURIC ♨", setOf("HEARTFIRE_DAGGER", "BURSTFIRE_DAGGER", "FIREDUST_DAGGER")),
        Crystal("CRYSTAL ♨", setOf("HEARTMAW_DAGGER", "BURSTMAW_DAGGER", "MAWDUST_DAGGER")),
        Spirit("SPIRIT ♨", setOf("HEARTMAW_DAGGER", "BURSTMAW_DAGGER", "MAWDUST_DAGGER"));

        companion object {
            fun get(a: String) = entries.firstOrNull { it.str in a }
        }
    }
}
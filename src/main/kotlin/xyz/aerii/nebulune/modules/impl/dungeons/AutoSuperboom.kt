@file:Suppress("ObjectPrivatePropertyName", "Unused")

package xyz.aerii.nebulune.modules.impl.dungeons

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.serialization.Codec
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.phys.BlockHitResult
import tech.thatgravyboat.skyblockapi.api.remote.api.SkyBlockId.Companion.getSkyBlockId
import xyz.aerii.athen.annotations.Load
import xyz.aerii.athen.annotations.OnlyIn
import xyz.aerii.athen.api.location.SkyBlockIsland
import xyz.aerii.athen.config.Category
import xyz.aerii.athen.events.CommandRegistration
import xyz.aerii.athen.events.InputEvent
import xyz.aerii.athen.events.TickEvent
import xyz.aerii.athen.handlers.Scribble
import xyz.aerii.athen.handlers.Typo.modMessage
import xyz.aerii.athen.modules.Module
import xyz.aerii.library.api.client
import xyz.aerii.library.api.lie
import xyz.aerii.library.handlers.parser.parse
import xyz.aerii.nebulune.Nebulune
import xyz.aerii.nebulune.mixin.accessors.InventoryAccessor
import xyz.aerii.nebulune.utils.leftClick

@Load
@OnlyIn(islands = [SkyBlockIsland.THE_CATACOMBS])
object AutoSuperboom : Module(
    "Auto superboom",
    "Automatically swaps to and uses the superboom if clicking on a breakable wall.",
    Category.DUNGEONS
) {
    private val _unused by config.textParagraph("Use the command <red>\"/nebulune superboom [add|remove]\"<r> while looking at a block to add/remove it to the breakable blocks list!")

    private val minDelay by config.slider("Minimum delay", 1, 1, 5, "ticks")
    private val maxDelay by config.slider("Maximum delay", 3, 1, 5, "ticks")

    private val swapBack by config.switch("Swap back")
    private val `swapBack$minDelay` by config.slider("Minimum delay", 1, 1, 5, "ticks").dependsOn { swapBack }
    private val `swapBack$maxDelay` by config.slider("Maximum delay", 3, 1, 5, "ticks").dependsOn { swapBack }
    private val `swapBack$type` by config.dropdown("Swap to", listOf("Original slot", "Custom slot")).dependsOn { swapBack }
    private val `swapBack$custom` by config.slider("Custom slot number", 1, 1, 9).dependsOn { swapBack && `swapBack$type` == 1 }

    private val scribble = Scribble("features/autoSuperboom")
    private val breakable = scribble.mutableSet("breakable", Codec.STRING, mutableSetOf("minecraft:cracked_stone_bricks", "minecraft:barrier"))

    private val set = setOf("SUPERBOOM_TNT", "INFINITE_SUPERBOOM_TNT")
    private var tick = -1
    private var og = -1
    private var ts = -1
    private var int = 0

    init {
        on<CommandRegistration> {
            event.register(Nebulune.modId) {
                then("superboom") {
                    then("add") {
                        callback {
                            val h = client.hitResult as? BlockHitResult ?: return@callback "Not looking at a block!".modMessage()
                            val b = client.level?.getBlockState(h.blockPos)?.block ?: return@callback

                            val id = BuiltInRegistries.BLOCK.getKey(b)

                            if (id.toString() in breakable.value) return@callback "Block already in breakable list!".modMessage()
                            breakable.update { add(id.toString()) }

                            "Added \"${id.path}\" to the breakable block list!".modMessage()
                        }

                        thenCallback("block", StringArgumentType.string()) {
                            val it = "minecraft:${StringArgumentType.getString(this, "block")}"
                            if (ResourceLocation.tryParse(it) == null) return@thenCallback "Invalid block id, or format! Try the command \"/nebulune superboom add\" while looking at the block.".modMessage()
                            if (it in breakable.value) return@thenCallback "Block already in breakable list!".modMessage()

                            breakable.update { add(it) }
                            "Added \"${it.substringAfter(":")}\" to the breakable block list!".modMessage()
                        }
                    }

                    then("remove") {
                        callback {
                            val h = client.hitResult as? BlockHitResult ?: return@callback "Not looking at a block!".modMessage()
                            val b = client.level?.getBlockState(h.blockPos)?.block ?: return@callback

                            val id = BuiltInRegistries.BLOCK.getKey(b)

                            if (id.toString() !in breakable.value) return@callback "Block not in breakable list!".modMessage()
                            breakable.update { remove(id.toString()) }

                            "Removed \"${id.path}\" from the breakable block list!".modMessage()
                        }

                        thenCallback("block", StringArgumentType.string()) {
                            val it = "minecraft:${StringArgumentType.getString(this, "block")}"
                            if (ResourceLocation.tryParse(it) == null) return@thenCallback "Invalid block id, or format! Try the command \"/nebulune superboom add\" while looking at the block.".modMessage()
                            if (it !in breakable.value) return@thenCallback "Block not in breakable list!".modMessage()

                            breakable.update { remove(it) }
                            "Removed \"${it.substringAfter(":")}\" from the breakable block list!".modMessage()
                        }
                    }

                    thenCallback("list") {
                        "Breakable block list:".modMessage()
                        for (a in breakable.value) {
                            val b = a.substringBefore(":")
                            val c = a.substringAfter(":")

                            " <dark_gray>- <gray>$b:<r>$c".parse().lie()
                        }
                    }
                }
            }
        }

        on<InputEvent.Mouse.Press> {
            if (client.screen != null) return@on
            val p = client.player ?: return@on
            val h = client.hitResult as? BlockHitResult ?: return@on

            val block = client.level?.getBlockState(h.blockPos) ?: return@on
            if (BuiltInRegistries.BLOCK.getKey(block.block).toString() !in breakable.value) return@on

            val s = (p.inventory as InventoryAccessor).selectedSlot
            val t = fn()?.takeIf { it != s } ?: return@on

            tick = (minDelay..maxDelay.coerceAtLeast(minDelay)).random()
            og = s
            ts = t
            int = 0

            cancel()
        }

        on<TickEvent.Client.Start> {
            val p = client.player ?: return@on
            if (tick == -1) return@on
            if (tick-- > 0) return@on

            val acc = p.inventory as InventoryAccessor
            when (int) {
                0 -> {
                    val slot = ts.takeIf { it != -1 } ?: return@on reset()
                    acc.selectedSlot = slot
                    int = 1
                    tick = 1
                }

                1 -> {
                    leftClick()

                    if (!swapBack) return@on reset()
                    int = 2
                    tick = (`swapBack$minDelay`..`swapBack$maxDelay`.coerceAtLeast(`swapBack$minDelay`)).random()
                }

                2 -> {
                    acc.selectedSlot = if (`swapBack$type` == 0) og else (`swapBack$custom`.coerceIn(1, 9) - 1)
                    reset()
                }
            }
        }
    }

    private fun fn(): Int? {
        val player = client.player ?: return null
        for (i in 0..8) if (player.inventory.getItem(i)?.getSkyBlockId()?.skyblockId in set) return i
        return null
    }

    private fun reset() {
        tick = -1
        og = -1
        int = 0
    }
}
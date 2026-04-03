package xyz.aerii.nebulune.modules.impl.dungeons

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket
import xyz.aerii.athen.annotations.Load
import xyz.aerii.athen.annotations.OnlyIn
import xyz.aerii.athen.api.location.SkyBlockIsland
import xyz.aerii.athen.config.Category
import xyz.aerii.athen.events.GuiEvent
import xyz.aerii.athen.events.PacketEvent
import xyz.aerii.athen.handlers.Chronos
import xyz.aerii.athen.handlers.Smoothie.client
import xyz.aerii.athen.handlers.Typo.stripped
import xyz.aerii.athen.modules.Module
import xyz.aerii.nebulune.utils.send

@Load
@OnlyIn(islands = [SkyBlockIsland.THE_CATACOMBS])
object ChestCloser : Module(
    "Chest closer",
    "Automatically closes chests.",
    Category.DUNGEONS
) {
    private val mode by config.dropdown("Close mode", listOf("Auto", "Click"))

    private val minDelay by config.slider("Minimum delay", 0, 0, 5, "ticks").dependsOn { mode == 0 }
    private val maxDelay by config.slider("Maximum delay", 1, 0, 5, "ticks").dependsOn { mode == 0 }

    private val mouse by config.switch("Mouse", true).dependsOn { mode == 1 }
    private val key by config.switch("Key", true).dependsOn { mode == 1 }

    private val set = setOf("Chest", "Large Chest")

    init {
        on<PacketEvent.Receive, ClientboundOpenScreenPacket> {
            if (title.stripped() !in set) return@on
            if (mode != 0) return@on

            it.cancel()

            val r = (minDelay..maxDelay.coerceAtLeast(minDelay)).random()
            if (r == 0) return@on ServerboundContainerClosePacket(containerId).send()

            Chronos.Tick after r then {
                client.player?.closeContainer()
            }
        }

        on<GuiEvent.Input.Mouse.Press> {
            if (mode != 1) return@on
            if (!mouse) return@on

            val screen = client.screen as? AbstractContainerScreen<*> ?: return@on
            if (screen.title.stripped() !in set) return@on

            cancel()
            client.player?.closeContainer()
        }

        on<GuiEvent.Input.Key.Press> {
            if (mode != 1) return@on
            if (!key) return@on
            if (client.options.keyInventory.matches(keyEvent)) return@on

            val screen = client.screen as? AbstractContainerScreen<*> ?: return@on
            if (screen.title.stripped() !in set) return@on

            cancel()
            client.player?.closeContainer()
        }
    }
}
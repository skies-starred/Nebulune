@file:Suppress("ConstPropertyName", "Unused")

package xyz.aerii.nebulune

import net.fabricmc.api.ClientModInitializer
import tech.thatgravyboat.skyblockapi.helpers.McClient
import xyz.aerii.athen.Athen
import xyz.aerii.athen.config.ui.ClickGUI
import xyz.aerii.athen.events.CommandRegistration
import xyz.aerii.athen.events.core.on
import xyz.aerii.athen.handlers.Chronos
import xyz.aerii.athen.handlers.Typo.modMessage
import xyz.aerii.library.handlers.time.server
import java.util.concurrent.atomic.AtomicInteger
import kotlin.time.Duration.Companion.milliseconds

object Nebulune : ClientModInitializer {
    const val modVersion: String = /*$ mod_version*/ "0.1.4"
    const val modId: String = /*$ mod_id*/ "nebulune"

    override fun onInitializeClient() {
        Athen.LOGGER.info("Nebulune loaded.")

        on<CommandRegistration> {
            event.register(modId) {
                thenCallback("config") {
                    McClient.setScreen(ClickGUI)
                    "Opening Config GUI...".modMessage()
                }

                callback {
                    McClient.setScreen(ClickGUI)
                    "Opening Config GUI...".modMessage()
                }
            }
        }
    }

    @JvmStatic
    fun afterTimed(ms: Int, action: () -> Unit) {
        val count = AtomicInteger(0)
        val check = { if (count.incrementAndGet() == 2) action() }

        Chronos.schedule(ms.milliseconds) { check() }
        Chronos.schedule((ms / 50).server) { check() }
    }
}
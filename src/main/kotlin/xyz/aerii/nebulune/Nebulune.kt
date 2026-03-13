@file:Suppress("ConstPropertyName", "Unused")

package xyz.aerii.nebulune

import net.fabricmc.api.ClientModInitializer
import xyz.aerii.athen.Athen
import xyz.aerii.athen.handlers.Chronos
import java.util.concurrent.atomic.AtomicInteger
import kotlin.time.Duration.Companion.milliseconds

object Nebulune : ClientModInitializer {
    const val modVersion: String = /*$ mod_version*/ "0.1.2"
    const val modId: String = /*$ mod_id*/ "nebulune"

    override fun onInitializeClient() {
        Athen.LOGGER.info("Nebulune loaded.")
    }

    @JvmStatic
    fun after(timeMillis: Long, block: () -> Unit) {
        Chronos.Time after timeMillis.milliseconds then(block)
    }

    @JvmStatic
    fun afterTimed(ms: Int, action: () -> Unit) {
        val count = AtomicInteger(0)
        val check = { if (count.incrementAndGet() == 2) action() }

        Chronos.Time after ms.milliseconds then check
        Chronos.Server after (ms / 50) then check
    }
}
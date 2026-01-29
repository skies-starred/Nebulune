@file:Suppress("ConstPropertyName", "Unused")

package xyz.aerii.nebulune

import net.fabricmc.api.ClientModInitializer
import xyz.aerii.athen.Athen
import xyz.aerii.athen.handlers.Chronos
import kotlin.time.Duration.Companion.milliseconds

object Nebulune : ClientModInitializer {
    const val modVersion: String = /*$ mod_version*/ "0.0.5"
    const val modId: String = /*$ mod_id*/ "nebulune"

    override fun onInitializeClient() {
        Athen.LOGGER.info("Nebulune loaded.")
    }

    @JvmStatic
    fun after(timeMillis: Long, block: () -> Unit) {
        Chronos.Time after timeMillis.milliseconds then(block)
    }
}
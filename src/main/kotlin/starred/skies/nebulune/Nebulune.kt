package starred.skies.nebulune

import net.fabricmc.api.ClientModInitializer
import starred.skies.athen.Athen
import starred.skies.athen.handlers.Chronos
import kotlin.time.Duration.Companion.milliseconds

object Nebulune : ClientModInitializer {
    override fun onInitializeClient() {
        Athen.LOGGER.info("Nebulune loaded.")
    }

    @JvmStatic
    fun after(timeMillis: Long, block: () -> Unit) {
        Chronos.Time after timeMillis.milliseconds then(block)
    }
}
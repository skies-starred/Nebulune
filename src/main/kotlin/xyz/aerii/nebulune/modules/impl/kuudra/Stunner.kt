@file:Suppress("ObjectPrivatePropertyName")

package xyz.aerii.nebulune.modules.impl.kuudra

import xyz.aerii.athen.annotations.Load
import xyz.aerii.athen.handlers.Chronos
import xyz.aerii.athen.handlers.Smoothie.client
import xyz.aerii.athen.modules.impl.kuudra.StunHelper

@Load
object Stunner {
    private val autoClose by StunHelper.config.switch("Auto close GUI")
    private val `autoClose$delay` by StunHelper.config.slider("Close delay", 1, 0, 5, "ticks").dependsOn { autoClose }

    @JvmStatic
    fun fn() {
        if (!autoClose) return
        val player = client.player ?: return
        val menu = player.containerMenu ?: return

        Chronos.Tick after `autoClose$delay` then {
            if (menu == player.containerMenu) player.closeContainer()
        }
    }
}
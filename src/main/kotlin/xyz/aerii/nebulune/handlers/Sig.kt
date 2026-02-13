package xyz.aerii.nebulune.handlers

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import xyz.aerii.athen.annotations.Priority
import xyz.aerii.nebulune.events.TickStartEvent

@Priority
object Sig {
    init {
        ClientTickEvents.START_CLIENT_TICK.register { _ ->
            TickStartEvent.post()
        }
    }
}
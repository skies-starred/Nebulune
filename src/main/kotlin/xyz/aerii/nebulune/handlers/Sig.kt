package xyz.aerii.nebulune.handlers

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import xyz.aerii.athen.annotations.Priority
import xyz.aerii.nebulune.events.TickStartEvent
import xyz.aerii.nebulune.events.ClientChunkEvent

@Priority
object Sig {
    init {
        ClientTickEvents.START_CLIENT_TICK.register { _ ->
            TickStartEvent.post()
        }

        ClientChunkEvents.CHUNK_LOAD.register(ClientChunkEvents.Load { world, chunk ->
            ClientChunkEvent.Load(world, chunk).post()
        })
    }
}
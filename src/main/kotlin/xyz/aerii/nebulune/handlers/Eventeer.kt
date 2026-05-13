package xyz.aerii.nebulune.handlers

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents
import xyz.aerii.athen.annotations.Priority
import xyz.aerii.nebulune.events.ClientChunkEvent

@Priority
object Eventeer {
    init {
        ClientChunkEvents.CHUNK_LOAD.register(ClientChunkEvents.Load { world, chunk ->
            ClientChunkEvent.Load(world, chunk).post()
        })
    }
}
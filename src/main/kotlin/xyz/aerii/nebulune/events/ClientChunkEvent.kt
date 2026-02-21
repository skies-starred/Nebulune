package xyz.aerii.nebulune.events

import net.minecraft.client.multiplayer.ClientLevel
import xyz.aerii.athen.events.core.Event
import net.minecraft.world.level.chunk.LevelChunk

sealed class ClientChunkEvent {
    data class Load(
        val world: ClientLevel,
        val chunk: LevelChunk
    ) : Event()
}
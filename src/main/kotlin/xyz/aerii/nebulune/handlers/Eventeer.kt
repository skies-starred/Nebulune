package xyz.aerii.nebulune.handlers

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.event.player.AttackBlockCallback
import net.fabricmc.fabric.api.event.player.AttackEntityCallback
import net.minecraft.world.InteractionResult
import xyz.aerii.athen.annotations.Priority
import xyz.aerii.nebulune.events.TickStartEvent
import xyz.aerii.nebulune.events.ClientChunkEvent
import xyz.aerii.nebulune.events.PlayerAttackEvent

@Priority
object Eventeer {
    init {
        ClientTickEvents.START_CLIENT_TICK.register { _ ->
            TickStartEvent.post()
        }

        ClientChunkEvents.CHUNK_LOAD.register(ClientChunkEvents.Load { world, chunk ->
            ClientChunkEvent.Load(world, chunk).post()
        })

        AttackEntityCallback.EVENT.register { _, _, _, entity, _ ->
            PlayerAttackEvent(entity).post()
            InteractionResult.PASS
        }

        AttackBlockCallback.EVENT.register { _, _, _, pos, _ ->
            InteractionResult.FAIL
        }
    }
}
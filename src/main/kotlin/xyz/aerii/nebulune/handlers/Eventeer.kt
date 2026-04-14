package xyz.aerii.nebulune.handlers

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents
import net.fabricmc.fabric.api.event.player.AttackBlockCallback
import net.fabricmc.fabric.api.event.player.AttackEntityCallback
import net.minecraft.world.InteractionResult
import xyz.aerii.athen.annotations.Priority
import xyz.aerii.nebulune.events.BlockHitEvent
import xyz.aerii.nebulune.events.ClientChunkEvent
import xyz.aerii.nebulune.events.PlayerAttackEvent

@Priority
object Eventeer {
    init {
        ClientChunkEvents.CHUNK_LOAD.register(ClientChunkEvents.Load { world, chunk ->
            ClientChunkEvent.Load(world, chunk).post()
        })

        AttackEntityCallback.EVENT.register { _, _, _, entity, _ ->
            PlayerAttackEvent(entity).post()
            InteractionResult.PASS
        }

        AttackBlockCallback.EVENT.register { _, _, _, pos, _ ->
            if (BlockHitEvent(pos).post()) InteractionResult.FAIL else InteractionResult.PASS
        }
    }
}
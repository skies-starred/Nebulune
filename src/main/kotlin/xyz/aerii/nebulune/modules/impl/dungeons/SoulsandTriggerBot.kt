package xyz.aerii.nebulune.modules.impl.dungeons

import net.minecraft.core.BlockPos
import net.minecraft.world.item.Items
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.phys.BlockHitResult
import xyz.aerii.athen.annotations.Load
import xyz.aerii.athen.annotations.OnlyIn
import xyz.aerii.athen.api.dungeon.DungeonAPI
import xyz.aerii.athen.api.location.SkyBlockIsland
import xyz.aerii.athen.config.Category
import xyz.aerii.athen.events.TickEvent
import xyz.aerii.athen.events.core.runWhen
import xyz.aerii.athen.modules.Module
import xyz.aerii.library.api.client
import xyz.aerii.library.handlers.Observable.Companion.and
import xyz.aerii.nebulune.utils.rightClick

@Load
@OnlyIn(islands = [SkyBlockIsland.THE_CATACOMBS])
object SoulsandTriggerBot : Module(
    "Soulsand triggerbot",
    "Triggerbot that automatically places soul sand or chests in P3",
    category = Category.DUNGEONS
) {
    private val set = setOf(Items.SOUL_SAND, Items.CHEST, Items.ENDER_CHEST)

    init {
        on<TickEvent.Client.Start> {
            val a = client.hitResult as? BlockHitResult ?: return@on
            val b = client.level ?: return@on
            val c = client.player?.mainHandItem?.item ?: return@on
            if (c !in set) return@on

            val d = a.blockPos
            if (d.y != 105) return@on

            val e = b.getBlockState(d).block
            if (e != Blocks.STONE_BRICKS) return@on

            val f = b.getBlockState(BlockPos(d.x, d.y + 1, d.z)).block
            if (f != Blocks.LAVA) return@on

            rightClick()
        }.runWhen(DungeonAPI.inBoss and DungeonAPI.floor.map { it?.floorNumber == 7 })
    }
}
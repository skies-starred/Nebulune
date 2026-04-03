package xyz.aerii.nebulune.events

import net.minecraft.core.BlockPos
import xyz.aerii.athen.events.core.CancellableEvent

data class BlockHitEvent(val pos: BlockPos) : CancellableEvent()
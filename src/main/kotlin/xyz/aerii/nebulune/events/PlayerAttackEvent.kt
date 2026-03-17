package xyz.aerii.nebulune.events

import net.minecraft.world.entity.Entity
import xyz.aerii.athen.events.core.Event

data class PlayerAttackEvent(val entity: Entity) : Event()
package xyz.aerii.nebulune.modules

import xyz.aerii.athen.annotations.Load
import xyz.aerii.athen.annotations.OnlyIn
import xyz.aerii.athen.config.Category
import xyz.aerii.athen.events.EntityEvent
import xyz.aerii.athen.handlers.Smoothie.client
import xyz.aerii.athen.handlers.Typo.stripped
import xyz.aerii.athen.modules.Module
import xyz.aerii.nebulune.events.TickStartEvent
import xyz.aerii.nebulune.utils.rightClick

@Load
@OnlyIn(skyblock = true)
object FishingHelper : Module(
    "Fishing helper",
    "Automatically pulls back your rod when you catch something!",
    Category.GENERAL
) {
    private val delay by config.slider("Delay", 1, 0, 5)
    private val delayVariance by config.slider("Delay variance", 0, 0, 3)

    private var ticks = -1

    init {
        on<EntityEvent.NameChange> {
            if (component.stripped() != "!!!") return@on
            if (infoLineEntity.distanceTo(client.player?.fishing) > 2f) return@on

            val t0 =
                if (delayVariance > 0) (0..delayVariance).random()
                else 0

            ticks = (delay + t0).coerceAtLeast(0)
        }

        on<TickStartEvent> {
            if (ticks-- != 0) return@on

            rightClick()
            ticks = -1
        }
    }
}
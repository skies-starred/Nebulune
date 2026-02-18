@file:Suppress("ObjectPrivatePropertyName")

package xyz.aerii.nebulune.modules

import net.minecraft.world.entity.Entity
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
    "Helper features for fishing.",
    Category.GENERAL
) {
    private val autoPull by config.switch("Auto pull", true)
    private val `delay$pull` by config.slider("Delay", 1, 0, 5, "ticks").dependsOn { autoPull }
    private val `variance$pull` by config.slider("Delay variance", 0, 0, 3, "ticks").dependsOn { autoPull }

    private val recast by config.switch("Auto recast")
    private val `delay$recast` by config.slider("Recast delay", 1, 0, 10, "ticks").dependsOn { recast }
    private val `variance$recast` by config.slider("Delay variance", 0, 0, 5, "ticks").dependsOn { recast }

    private var ticks = -1
    private var recasting = false

    init {
        on<EntityEvent.Update.Named> {
            val entity = client.player?.fishing as? Entity ?: return@on
            if (component.stripped() != "!!!") return@on
            if (infoLineEntity.distanceTo(entity) > 2f) return@on

            val t0 =
                if (`variance$pull` > 0) (0..`variance$pull`).random()
                else 0

            ticks = (`delay$pull` + t0).coerceAtLeast(0)
            recasting = false
        }

        on<TickStartEvent> {
            if (ticks < 0) return@on
            if (ticks-- > 0) return@on

            rightClick()

            if (!recast || recasting) {
                ticks = -1
                recasting = false
                return@on
            }

            val t0 =
                if (`variance$recast` > 0) (0..`variance$recast`).random()
                else 0

            ticks = 2 + `delay$recast` + t0
            recasting = true
        }
    }
}
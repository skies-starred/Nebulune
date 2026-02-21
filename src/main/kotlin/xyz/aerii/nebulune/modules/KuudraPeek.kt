package xyz.aerii.nebulune.modules

import xyz.aerii.athen.annotations.Load
import xyz.aerii.athen.annotations.OnlyIn
import xyz.aerii.athen.api.kuudra.KuudraAPI
import xyz.aerii.athen.api.kuudra.enums.KuudraPhase
import xyz.aerii.athen.api.kuudra.enums.KuudraTier
import xyz.aerii.athen.api.location.SkyBlockIsland
import xyz.aerii.athen.config.Category
import xyz.aerii.athen.events.TickEvent
import xyz.aerii.athen.handlers.Smoothie.alert
import xyz.aerii.athen.handlers.Smoothie.client
import xyz.aerii.athen.handlers.Texter.parse
import xyz.aerii.athen.handlers.Typo.modMessage
import xyz.aerii.athen.modules.Module

@Load
@OnlyIn(islands = [SkyBlockIsland.KUUDRA])
object KuudraPeek : Module(
    "Kuudra peek",
    "Tries to detect which direction Kuudra will peek from!",
    Category.KUUDRA
) {
    private val y by config.switch("Check Y level", true)
    private val first by config.switch("Only show first peek")

    private var side: Side = Side.NONE

    private enum class Side {
        FRONT,
        BACK,
        RIGHT,
        LEFT,
        NONE;

        val str: String =
            name.lowercase().replaceFirstChar { it.uppercase() }
    }

    init {
        on<TickEvent.Client> {
            if (!KuudraAPI.inRun) return@on
            if (KuudraAPI.tier != KuudraTier.INFERNAL) return@on
            if (KuudraAPI.phase != KuudraPhase.Kill) return@on
            if (ticks % 2 != 0) return@on

            val player = client.player ?: return@on
            val kuudra = KuudraAPI.kuudra ?: return@on

            if (first && kuudra.health !in 24500f..25000f) return@on
            if (y && player.blockPosition().y >= 25) return@on

            val x = kuudra.blockPosition().x
            val z = kuudra.blockPosition().z

            val ns = when {
                x < -128 -> Side.RIGHT
                x > -72 -> Side.LEFT
                z > -84 -> Side.FRONT
                z < -132 -> Side.BACK
                else -> Side.NONE
            }

            if (side == ns) return@on
            side = ns

            "Kuudra peeked from <red>${side.str}<r>!".parse().modMessage()
            "<red>${side.name}!".parse().alert()
        }
    }
}
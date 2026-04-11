@file:Suppress("Unused")

package xyz.aerii.nebulune.modules

import xyz.aerii.athen.annotations.Load
import xyz.aerii.athen.handlers.Chronos
import xyz.aerii.athen.modules.impl.ModSettings
import xyz.aerii.library.api.client
import xyz.aerii.library.api.lie
import xyz.aerii.library.handlers.parser.parse
import kotlin.time.Duration.Companion.minutes

@Load
object ImportantFeature {
    private val set = setOf("516m")

    val enabled by ModSettings.config.switch("Important feature", true)
    private val _enabled by ModSettings.config.textParagraph("Disabling the important feature may cause issues!")

    init {
        Chronos.repeat(20.minutes) {
            if (client.level == null) return@repeat
            if (!enabled) return@repeat
            if ((0..100).random() > 4) return@repeat
            val a = if (set.size == 1) set.first() else set.random()

            "<red>[<orange>ዞ<red>] $a <yellow>has joined the lobby.".parse().lie()
        }
    }
}
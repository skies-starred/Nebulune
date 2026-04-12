package xyz.aerii.nebulune.modules.impl.slayer

import xyz.aerii.athen.annotations.Load
import xyz.aerii.athen.modules.impl.slayer.SlayerHighlight

@Load
object BossESP {
    val depth by SlayerHighlight.config.switch("Depth check", true)
}
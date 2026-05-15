package xyz.aerii.nebulune.modules.impl.render

import xyz.aerii.athen.annotations.Load
import xyz.aerii.athen.modules.impl.render.highlight.MobHighlight

@Load
object MobHighlightESP {
    val depth by MobHighlight.config.switch("Depth check")
    val tracer by MobHighlight.config.switch("Tracers")
}
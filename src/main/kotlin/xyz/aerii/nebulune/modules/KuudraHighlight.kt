@file:Suppress("ObjectPrivatePropertyName")

package xyz.aerii.nebulune.modules

import xyz.aerii.athen.annotations.Load
import xyz.aerii.athen.api.kuudra.KuudraAPI
import xyz.aerii.athen.events.WorldRenderEvent
import xyz.aerii.athen.events.core.on
import xyz.aerii.athen.events.core.runWhen
import xyz.aerii.athen.handlers.React.Companion.and
import xyz.aerii.athen.modules.impl.kuudra.KuudraInfo
import xyz.aerii.athen.ui.themes.Catppuccin
import xyz.aerii.athen.utils.render.renderPos
import xyz.aerii.nebulune.utils.drawTracer
import java.awt.Color

@Load
object KuudraHighlight {
    private val tracer = KuudraInfo.config.switch("Tracer", false).custom("tracer")
    private val `tracer$color` by KuudraInfo.config.colorPicker("Tracer color", Color(Catppuccin.Mocha.Peach.argb, true)).dependsOn { tracer.value }
    private val `tracer$width` by KuudraInfo.config.slider("Tracer width", 2f, 1f, 10f)
    private val `tracer$depth` by KuudraInfo.config.switch("Tracer depth").dependsOn { tracer.value }

    init {
        on<WorldRenderEvent.Extract> {
            if (!KuudraAPI.inRun) return@on
            val k = KuudraAPI.kuudra ?: return@on
            drawTracer(k.renderPos, `tracer$color`, `tracer$width`, `tracer$depth`)
        }.runWhen(KuudraInfo.react and tracer.state)
    }
}
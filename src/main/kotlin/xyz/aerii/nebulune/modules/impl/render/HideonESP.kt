package xyz.aerii.nebulune.modules.impl.render

import net.minecraft.client.renderer.entity.state.ShulkerRenderState
import net.minecraft.world.item.DyeColor
import xyz.aerii.athen.annotations.Load
import xyz.aerii.athen.annotations.OnlyIn
import xyz.aerii.athen.api.location.SkyBlockIsland
import xyz.aerii.athen.config.Category
import xyz.aerii.athen.events.WorldRenderEvent
import xyz.aerii.athen.modules.Module
import xyz.aerii.athen.ui.themes.Catppuccin
import xyz.aerii.athen.utils.render.Render3D
import xyz.aerii.athen.utils.render.renderBoundingBox
import xyz.aerii.athen.utils.render.renderPos
import xyz.aerii.nebulune.utils.drawTracer
import java.awt.Color

@Load
@OnlyIn(islands = [SkyBlockIsland.GALATEA])
object HideonESP : Module(
    "Hideon ESP",
    "ESP for Hideons",
    Category.RENDER
) {
    private val color by config.colorPicker("Color", Color(Catppuccin.Mocha.Mauve.argb, true))
    private val lineWidth by config.slider("Line width", 2f, 1f, 10f)
    private val tracer by config.switch("Show tracer")

    init {
        on<WorldRenderEvent.Entity.Post> {
            val r = renderState as? ShulkerRenderState ?: return@on
            val e = entity ?: return@on
            if (r.color != DyeColor.GREEN) return@on

            Render3D.drawBox(e.renderBoundingBox, color, lineWidth, false)
            if (tracer) drawTracer(e.renderPos, color, lineWidth)
        }
    }
}
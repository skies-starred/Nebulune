package xyz.aerii.nebulune.utils

import net.minecraft.client.renderer.blockentity.state.BeaconRenderState
import net.minecraft.world.phys.Vec3
import xyz.aerii.athen.handlers.Smoothie.client
import xyz.aerii.athen.utils.render.Render3D
import java.awt.Color

fun drawTracer(to: Vec3, color: Color, lineWidth: Float = 3f, depthTest: Boolean = false) {
    val camera = client.gameRenderer.mainCamera
    val from = camera.position/*? >= 1.21.11 { *//*()*//*? }*/.add(Vec3.directionFromRotation(camera.xRot/*? >= 1.21.11 { *//*()*//*? }*/, camera.yRot/*? >= 1.21.11 { *//*()*//*? }*/))
    Render3D.drawLine(from, to, color, lineWidth, depthTest)
}

fun drawString(
    text: String,
    pos: Vec3,
    color: Int = -1,
    backgroundColor: Int = 0,
    scale: Float = 1f,
    depthTest: Boolean = true,
    shadow: Boolean = true,
    increase: Boolean = false
) {
    var toScale = scale
    if (increase) {
        val camPos = client.gameRenderer.mainCamera.position/*? >= 1.21.11 { *//*()*//*? }*/
        val dist = camPos.distanceTo(pos)

        toScale *= dist.toFloat() / 3f
    }
    Render3D.drawString(
        text,
        pos,
        color,
        backgroundColor,
        toScale,
        depthTest,
        shadow
    )
}
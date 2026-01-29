package xyz.aerii.nebulune.utils

import net.minecraft.world.phys.Vec3
import xyz.aerii.athen.handlers.Smoothie.client
import xyz.aerii.athen.utils.render.Render3D
import java.awt.Color

fun drawTracer(to: Vec3, color: Color, lineWidth: Float = 3f, depthTest: Boolean = false) {
    val camera = client.gameRenderer.mainCamera
    val from = camera.position.add(Vec3.directionFromRotation(camera.xRot, camera.yRot))
    Render3D.drawLine(from, to, color, lineWidth, depthTest)
}
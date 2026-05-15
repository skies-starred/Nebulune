package xyz.aerii.nebulune.utils

import net.minecraft.world.phys.Vec3
import xyz.aerii.athen.api.rendering.level.impl.extensions.impl.extractLine
import xyz.aerii.library.api.client

fun extractTracer(to: Vec3, color: Int, lineWidth: Float = 3f, depthTest: Boolean = false) {
    val camera = client.gameRenderer.mainCamera
    val from = camera.position/*? >= 1.21.11 { *//*()*//*? }*/.add(Vec3.directionFromRotation(camera.xRot/*? >= 1.21.11 { *//*()*//*? }*/, camera.yRot/*? >= 1.21.11 { *//*()*//*? }*/))
    extractLine(from.toVector3f(), to.toVector3f(), color, lineWidth, depthTest)
}
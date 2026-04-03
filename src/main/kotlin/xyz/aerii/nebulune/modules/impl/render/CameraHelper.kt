package xyz.aerii.nebulune.modules.impl.render

import xyz.aerii.athen.annotations.Load
import xyz.aerii.athen.config.Category
import xyz.aerii.athen.modules.Module

@Load
object CameraHelper : Module(
    "Camera helper",
    "Cheat additions to the vanilla camera.",
    Category.RENDER
) {
    private val _clip by config.switch("Camera clip")
    private val _dist by config.switch("Custom distance")

    @JvmStatic
    val distance by config.slider("Distance", 4f, 3f, 15f, "blocks", true).dependsOn { _dist }

    @JvmStatic
    val dist: Boolean
        get() = enabled && _dist

    @JvmStatic
    val clip: Boolean
        get() = enabled && _clip
}
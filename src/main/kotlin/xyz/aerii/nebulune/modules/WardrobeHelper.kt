package xyz.aerii.nebulune.modules

import xyz.aerii.athen.annotations.Load
import xyz.aerii.athen.modules.impl.general.WardrobeKeybinds

@Load
object WardrobeHelper {
    val autoClose by WardrobeKeybinds.config.switch("Auto close after use")
}
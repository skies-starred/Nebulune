package xyz.aerii.nebulune.utils

import net.minecraft.client.KeyMapping
import xyz.aerii.athen.handlers.Smoothie.client
import xyz.aerii.athen.mixin.accessors.KeyMappingAccessor

fun rightClick() {
    val options = client.options ?: return
    val key = (options.keyUse as KeyMappingAccessor).boundKey
    KeyMapping.set(key, true)
    KeyMapping.click(key)
    KeyMapping.set(key, false)
}
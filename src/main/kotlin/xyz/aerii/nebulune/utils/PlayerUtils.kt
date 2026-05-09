package xyz.aerii.nebulune.utils

import net.minecraft.client.KeyMapping
import xyz.aerii.athen.mixin.accessors.KeyMappingAccessor
import xyz.aerii.library.api.client

fun rightClick() {
    val options = client.options ?: return
    val key = (options.keyUse as KeyMappingAccessor).boundKey
    KeyMapping.set(key, true)
    KeyMapping.click(key)
    KeyMapping.set(key, false)
}

fun leftClick() {
    val options = client.options ?: return
    val key = (options.keyAttack as KeyMappingAccessor).boundKey
    KeyMapping.set(key, true)
    KeyMapping.click(key)
    KeyMapping.set(key, false)
}
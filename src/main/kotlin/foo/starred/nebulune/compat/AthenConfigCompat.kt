package foo.starred.nebulune.compat

import foo.starred.athen.config.dsl.impl.builders.option.ConfigOptionBuilder

fun <T> ConfigOptionBuilder<T>.dependsOn(@Suppress("UNUSED_PARAMETER") block: () -> Boolean) = this

fun <T> ConfigOptionBuilder<T>.childOf(@Suppress("UNUSED_PARAMETER") block: () -> Any) = this

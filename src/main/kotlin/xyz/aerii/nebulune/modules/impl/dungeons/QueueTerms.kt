@file:Suppress("ObjectPropertyName")

package xyz.aerii.nebulune.modules.impl.dungeons

import xyz.aerii.athen.annotations.Load
import xyz.aerii.athen.config.Category
import xyz.aerii.athen.modules.Module
import xyz.aerii.athen.modules.impl.dungeon.terminals.solver.base.Click

@Load
object QueueTerms : Module(
    "Queue terms",
    "Queues terminal clicks to automatically fire.",
    Category.DUNGEONS
) {
    val timeout by config.slider("Resync timeout", 800, 400, 1000, "ms")

    val clicks = mutableListOf<Click>()
    var yearning = false
}
@file:Suppress("ObjectPropertyName")

package xyz.aerii.nebulune.modules

import xyz.aerii.athen.annotations.Load
import xyz.aerii.athen.modules.impl.dungeon.terminals.solver.TerminalSolver
import xyz.aerii.athen.modules.impl.dungeon.terminals.solver.base.Click
import kotlin.random.Random

@Load
object TerminalHelper {
    val mode by TerminalSolver.config.dropdown("Mode", listOf("Normal", "Queue"))
    val timeout by TerminalSolver.config.slider("Resync timeout", 800, 400, 1000, "ms").dependsOn { mode == 1 }
    val hoverTerms by TerminalSolver.config.switch("Hover terms").dependsOn { mode == 1 }
    val `hover$delay` by TerminalSolver.config.slider("Delay", 50, 10, 400, "ms")

    val clicks = mutableListOf<Click>()
    var yearning = false

    fun delay(): Long {
        val a = `hover$delay`.toLong()
        return Random.nextLong((a * 0.6).toLong().coerceAtLeast(10), (a * 1.4).toLong() + 1)
    }
}
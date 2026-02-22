package xyz.aerii.nebulune.modules

import xyz.aerii.athen.annotations.Load
import xyz.aerii.athen.modules.impl.dungeon.terminals.solver.TerminalSolver
import xyz.aerii.athen.modules.impl.dungeon.terminals.solver.base.Click

@Load
object TerminalHelper {
    val mode by TerminalSolver.config.dropdown("Mode", listOf("Normal", "Queue"))
    val timeout by TerminalSolver.config.slider("Resync timeout", 800, 400, 1000, "ms").dependsOn { mode == 1 }
    val hoverTerms by TerminalSolver.config.switch("Hover terms").dependsOn { mode == 1 }

    val clicks = mutableListOf<Click>()
    var yearning = false
}
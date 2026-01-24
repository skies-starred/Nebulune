package xyz.aerii.nebulune.modules

import xyz.aerii.athen.annotations.Load
import xyz.aerii.athen.modules.impl.dungeon.terminals.solver.TerminalSolver as AthenTerminalSolver
import xyz.aerii.athen.modules.impl.dungeon.terminals.solver.base.Click

@Load
object TerminalSolver {
    val mode by AthenTerminalSolver.config.dropdown("Mode", listOf("Normal", "Queue"))
    val timeout by AthenTerminalSolver.config.slider("Resync timeout", 800, 400, 1000)

    val clicks = mutableListOf<Click>()
    var yearning = false
}
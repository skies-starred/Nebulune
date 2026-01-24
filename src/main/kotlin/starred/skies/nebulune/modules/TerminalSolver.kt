package starred.skies.nebulune.modules

import starred.skies.athen.annotations.Load
import starred.skies.athen.modules.impl.dungeon.terminals.solver.TerminalSolver as Athen
import starred.skies.athen.modules.impl.dungeon.terminals.solver.base.Click

@Load
object TerminalSolver {
    @JvmStatic
    val mode by Athen.config.dropdown("Mode", listOf("Normal", "Queue"))

    @JvmStatic
    val timeout by Athen.config.slider("Resync timeout", 800, 400, 1000)

    @JvmField
    val clicks = mutableListOf<Click>()

    @JvmField
    var yearning = false
}
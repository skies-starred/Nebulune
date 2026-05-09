package xyz.aerii.nebulune.modules.impl.dungeons

import net.minecraft.world.inventory.ClickType
import xyz.aerii.athen.annotations.Load
import xyz.aerii.athen.api.dungeon.terminals.TerminalAPI
import xyz.aerii.athen.api.dungeon.terminals.TerminalType
import xyz.aerii.athen.config.Category
import xyz.aerii.athen.events.DungeonEvent
import xyz.aerii.athen.events.TickEvent
import xyz.aerii.athen.events.core.runWhen
import xyz.aerii.athen.modules.Module
import xyz.aerii.athen.modules.impl.dungeon.terminals.simulator.TerminalSimulator
import xyz.aerii.athen.modules.impl.dungeon.terminals.simulator.base.ITerminalSim
import xyz.aerii.athen.modules.impl.dungeon.terminals.solver.TerminalSolver
import xyz.aerii.athen.modules.impl.dungeon.terminals.solver.base.Click
import xyz.aerii.athen.modules.impl.dungeon.terminals.solver.impl.*
import xyz.aerii.library.api.client
import xyz.aerii.nebulune.accessors.ITerminalAccessor
import xyz.aerii.nebulune.utils.guiClick
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.random.Random

@Load
object AutoTerms : Module(
    "Auto terms",
    "Automatically solves terminals!",
    Category.DUNGEONS
) {
    private val rng = java.util.Random()

    private val minDelay by config.slider("Min delay", 80, 0, 500, "ms")
    private val maxDelay by config.slider("Max delay", 160, 0, 500, "ms")
    private val order by config.dropdown("Order", listOf("First", "Random", "Closest", "Furthest"), 2)

    private val numbers by config.switch("Numbers", true)
    private val panes by config.switch("Panes", true)
    private val colors by config.switch("Colors", true)
    private val name by config.switch("Name", true)
    private val rubix by config.switch("Rubix", true)
    private val melody by config.switch("Melody", true)

    private val solvers = mapOf(
        TerminalType.NUMBERS to NumbersSolver,
        TerminalType.PANES to PanesSolver,
        TerminalType.NAME to NameSolver,
        TerminalType.COLORS to ColorsSolver,
        TerminalType.RUBIX to RubixSolver,
        TerminalType.MELODY to MelodySolver
    )

    private val list = mutableListOf<Click>()

    private var last0: Int? = null
    private var last1: Long = 0
    private var next: Long = 0
    private var id: Int = -1

    init {
        on<DungeonEvent.Terminal.Open> {
            reset()
        }

        on<TickEvent.Client.Start> {
            if (TerminalType.MELODY.active) return@on fn()

            if (list.isEmpty()) return@on
            if (TerminalAPI.lastId != id) return@on list.clear()
            if (System.currentTimeMillis() < next) return@on

            val type = TerminalAPI.currentTerminal ?: return@on
            val next = list.removeFirst()

            val list = (solvers[type] as? ITerminalAccessor)?.`nebulune$getList`() ?: return@on list.clear()
            if (list.none { it.slot == next.slot }) return@on

            click(next)
        }.runWhen(TerminalAPI.terminalOpen)
    }

    @JvmStatic
    fun onUpdate() {
        val type = TerminalAPI.currentTerminal ?: return
        if (!type.active || type == TerminalType.MELODY) return

        val clicks = (solvers[type] as? ITerminalAccessor)?.`nebulune$getList`()?.toList() ?: return
        if (clicks.isEmpty()) return

        val pick = pick(clicks, type) ?: return
        val final = if (type == TerminalType.RUBIX) Click(pick.slot, if (pick.button > 0) 0 else 1) else pick
        if (last0 == final.slot && type != TerminalType.RUBIX) return

        if (list.any { it.slot == final.slot }) return

        last0 = final.slot
        id = TerminalAPI.lastId

        val fcLeft = TerminalSolver.fcDelay - (System.currentTimeMillis() - TerminalAPI.openTime)
        val delay = maxOf(next(), if (fcLeft > 0) fcLeft else 0L)
        next = System.currentTimeMillis() + delay

        list.add(final)
    }

    private fun fn() {
        if (System.currentTimeMillis() - TerminalAPI.openTime < TerminalSolver.fcDelay) return

        val correct = TerminalAPI.`melody$correct` ?: return
        val button = TerminalAPI.`melody$button` ?: return
        if (TerminalAPI.`melody$current` != correct) return
        if (System.currentTimeMillis() - last1 < 250) return

        last1 = System.currentTimeMillis()
        click(Click(button * 9 + 16, 0))
    }

    private fun reset() {
        last0 = null
        last1 = 0
        next = 0
        id = -1
        list.clear()
    }

    private fun pick(clicks: List<Click>, type: TerminalType): Click? {
        if (type == TerminalType.NUMBERS) return clicks.firstOrNull()
        return when (order) {
            0 -> clicks.firstOrNull()
            1 -> clicks.randomOrNull()
            2 -> clicks.minByOrNull { dist(it.slot, last0 ?: it.slot) }
            3 -> clicks.maxByOrNull { dist(it.slot, last0 ?: it.slot) }
            else -> clicks.firstOrNull()
        }
    }

    private fun dist(a: Int, b: Int): Double {
        val s1 = client.player?.containerMenu?.getSlot(a) ?: return Double.MAX_VALUE
        val s2 = client.player?.containerMenu?.getSlot(b) ?: return Double.MAX_VALUE
        val d = sqrt((s1.x - s2.x).toDouble().pow(2) + (s1.y - s2.y).toDouble().pow(2))
        return d + rng.nextGaussian() * (d * 0.25)
    }

    private fun next(): Long {
        val lo = minDelay.toLong()
        val hi = maxDelay.toLong()
        return if (lo >= hi) lo else Random.nextLong(lo, hi + 1)
    }

    private fun click(c: Click) {
        last0 = c.slot

        if (TerminalSimulator.s.value) {
            val screen = client.screen as? ITerminalSim ?: return
            val slot0 = screen.menu?.slots?.getOrNull(c.slot) ?: return
            screen.slotClicked(slot0, c.slot, c.button, if (c.button == 0) ClickType.CLONE else ClickType.PICKUP)
            return
        }

        guiClick(TerminalAPI.lastId, c.slot, if (c.button == 0) 2 else c.button, if (c.button == 0) ClickType.CLONE else ClickType.PICKUP)
    }

    private val TerminalType.active: Boolean
        get() = enabled && when (this) {
            TerminalType.NUMBERS -> numbers
            TerminalType.PANES -> panes
            TerminalType.COLORS -> colors
            TerminalType.NAME -> AutoTerms.name
            TerminalType.RUBIX -> rubix
            TerminalType.MELODY -> melody
        }
}
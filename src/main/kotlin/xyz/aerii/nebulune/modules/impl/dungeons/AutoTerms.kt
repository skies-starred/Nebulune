package xyz.aerii.nebulune.modules.impl.dungeons

import net.minecraft.world.inventory.ClickType
import xyz.aerii.athen.annotations.Load
import xyz.aerii.athen.api.dungeon.terminals.TerminalAPI
import xyz.aerii.athen.api.dungeon.terminals.TerminalType
import xyz.aerii.athen.config.Category
import xyz.aerii.athen.events.DungeonEvent
import xyz.aerii.athen.events.TickEvent
import xyz.aerii.athen.events.core.runWhen
import xyz.aerii.athen.handlers.Smoothie.client
import xyz.aerii.athen.modules.Module
import xyz.aerii.athen.modules.impl.dungeon.terminals.simulator.TerminalSimulator
import xyz.aerii.athen.modules.impl.dungeon.terminals.simulator.base.ITerminalSim
import xyz.aerii.athen.modules.impl.dungeon.terminals.solver.TerminalSolver
import xyz.aerii.athen.modules.impl.dungeon.terminals.solver.base.Click
import xyz.aerii.athen.modules.impl.dungeon.terminals.solver.impl.*
import xyz.aerii.athen.utils.mainThread
import xyz.aerii.nebulune.Nebulune
import xyz.aerii.nebulune.accessors.ITerminalAccessor
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

    private var last: Int? = null
    private var lastMelody = 0L
    private var pending = false

    init {
        on<TickEvent.Server> {
            if (!TerminalType.MELODY.active) return@on
            if (System.currentTimeMillis() - TerminalAPI.openTime < TerminalSolver.fcDelay) return@on

            val correct = TerminalAPI.`melody$correct` ?: return@on
            val button = TerminalAPI.`melody$button` ?: return@on
            if (TerminalAPI.`melody$current` != correct) return@on

            val slot = button * 9 + 16
            if (System.currentTimeMillis() - lastMelody < 250) return@on

            lastMelody = System.currentTimeMillis()
            click(Click(slot, 0))
        }.runWhen(TerminalAPI.terminalOpen)

        on<DungeonEvent.Terminal.Open> {
            reset()
        }
    }

    @JvmStatic
    fun onUpdate() {
        if (pending) return

        val type = TerminalAPI.currentTerminal ?: return
        if (!type.active || type == TerminalType.MELODY) return

        val clicks = (solvers[type] as? ITerminalAccessor)?.`nebulune$getList`()?.toList() ?: return
        if (clicks.isEmpty()) return

        val pick = pick(clicks, type) ?: return
        val final = if (type == TerminalType.RUBIX) Click(pick.slot, if (pick.button > 0) 0 else 1) else pick
        if (last == final.slot && type != TerminalType.RUBIX) return

        pending = true
        last = final.slot

        val windowId = TerminalAPI.lastId
        val fcLeft = TerminalSolver.fcDelay - (System.currentTimeMillis() - TerminalAPI.openTime)
        val delay = maxOf(next(), if (fcLeft > 0) fcLeft else 0L).toInt()

        Nebulune.afterTimed(delay) {
            pending = false

            if (!TerminalAPI.terminalOpen.value || TerminalAPI.lastId != windowId) return@afterTimed

            val list = (solvers[type] as? ITerminalAccessor)?.`nebulune$getList`() ?: return@afterTimed
            if (list.none { it.slot == final.slot }) return@afterTimed

            click(final)
        }
    }

    private fun reset() {
        last = null
        lastMelody = 0
        pending = false
    }

    private fun pick(clicks: List<Click>, type: TerminalType): Click? {
        if (type == TerminalType.NUMBERS) return clicks.firstOrNull()
        return when (order) {
            0 -> clicks.firstOrNull()
            1 -> clicks.randomOrNull()
            2 -> clicks.minByOrNull { dist(it.slot, last ?: it.slot) }
            3 -> clicks.maxByOrNull { dist(it.slot, last ?: it.slot) }
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
        last = c.slot
        mainThread {
            if (TerminalSimulator.s.value) {
                val screen = client.screen as? ITerminalSim ?: return@mainThread
                val slot0 = screen.menu?.slots?.getOrNull(c.slot) ?: return@mainThread
                screen.slotClicked(slot0, c.slot, c.button, if (c.button == 0) ClickType.CLONE else ClickType.PICKUP)
                return@mainThread
            }

            client.gameMode?.handleInventoryMouseClick(
                TerminalAPI.lastId, c.slot,
                if (c.button == 0) 2 else c.button,
                if (c.button == 0) ClickType.CLONE else ClickType.PICKUP,
                client.player ?: return@mainThread
            )
        }
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
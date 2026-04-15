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
import xyz.aerii.library.api.mainThread
import xyz.aerii.nebulune.accessors.ITerminalAccessor
import kotlin.random.Random

@Load
object HoverTerms : Module(
    "Hover terms",
    "Clicks terminals based on where your cursor is hovering :eyes:",
    Category.DUNGEONS
) {
    private val minDelay by config.slider("Min delay", 50, 0, 400, "ms")
    private val maxDelay by config.slider("Max delay", 120, 0, 400, "ms")

    private val solvers = mapOf(
        TerminalType.NUMBERS to NumbersSolver,
        TerminalType.PANES to PanesSolver,
        TerminalType.NAME to NameSolver,
        TerminalType.COLORS to ColorsSolver,
        TerminalType.RUBIX to RubixSolver,
    )

    private var slot0 = -1
    private var time = 0L

    init {
        on<TickEvent.Client.End> {
            val type = TerminalAPI.currentTerminal ?: return@on
            if (type == TerminalType.MELODY) return@on
            if (System.currentTimeMillis() - TerminalAPI.openTime < TerminalSolver.fcDelay) return@on

            val uiScale = 3f * TerminalSolver.`ui$scale`
            val mx = client.mouseHandler.xpos().toFloat() / uiScale
            val my = client.mouseHandler.ypos().toFloat() / uiScale

            val sp = 16f + TerminalSolver.`ui$gap`
            val pad = TerminalSolver.`ui$padding`
            val slots = type.slots
            val gridW = 7 * sp + 2 * pad
            val gridH = (slots / 9 - 2) * sp + 2 * pad
            val headerH = if (TerminalSolver.`ui$hideHeader`) 0f else 20f
            val padding = if (TerminalSolver.`ui$hideHeader`) 0f else 6f

            val ox = client.window.width / uiScale / 2 - gridW / 2
            val oy = client.window.height / uiScale / 2 - (gridH + headerH + padding) / 2

            val x = ((mx - ox - pad) / sp).toInt() + 1
            val y = ((my - (oy + headerH + padding) - pad) / sp).toInt() + 1
            if (x !in 1..7 || y < 1) return@on

            val slot = x + y * 9
            if (slot >= slots) return@on

            val list = (solvers[type] as? ITerminalAccessor)?.`nebulune$getList`() ?: return@on
            val c = list.find { it.slot == slot } ?: return@on reset()

            if (slot != slot0) {
                slot0 = slot
                time = System.currentTimeMillis() + delay()
            }

            if (System.currentTimeMillis() < time) return@on

            slot0 = -1
            val final = if (type == TerminalType.RUBIX) Click(c.slot, if (c.button > 0) 0 else 1) else c
            val windowId = TerminalAPI.lastId

            mainThread {
                if (!TerminalAPI.terminalOpen.value || TerminalAPI.lastId != windowId) return@mainThread
                if (TerminalSimulator.s.value) {
                    val screen = client.screen as? ITerminalSim ?: return@mainThread
                    val slot0 = screen.menu?.slots?.getOrNull(final.slot) ?: return@mainThread
                    screen.slotClicked(slot0, final.slot, final.button, if (final.button == 0) ClickType.CLONE else ClickType.PICKUP)
                    return@mainThread
                }

                client.gameMode?.handleInventoryMouseClick(
                    TerminalAPI.lastId,
                    final.slot,
                    if (final.button == 0) 2 else final.button,
                    if (final.button == 0) ClickType.CLONE else ClickType.PICKUP,
                    client.player ?: return@mainThread
                )
            }
        }.runWhen(TerminalAPI.terminalOpen)

        on<DungeonEvent.Terminal.Open> {
            reset()
        }
    }

    fun reset() {
        slot0 = -1
        time = 0L
    }

    private fun delay(): Long {
        val lo = minDelay.toLong()
        val hi = maxDelay.toLong()
        return if (lo >= hi) lo else Random.nextLong(lo, hi + 1)
    }
}
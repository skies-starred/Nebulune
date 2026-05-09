@file:Suppress("ObjectPropertyName")

package xyz.aerii.nebulune.modules.impl.dungeons

import net.minecraft.world.inventory.ContainerInput
import xyz.aerii.athen.annotations.Load
import xyz.aerii.athen.api.dungeon.terminals.TerminalAPI
import xyz.aerii.athen.config.Category
import xyz.aerii.athen.events.TickEvent
import xyz.aerii.athen.events.core.runWhen
import xyz.aerii.athen.modules.Module
import xyz.aerii.athen.modules.impl.dungeon.terminals.simulator.TerminalSimulator
import xyz.aerii.athen.modules.impl.dungeon.terminals.simulator.base.ITerminalSim
import xyz.aerii.athen.modules.impl.dungeon.terminals.solver.TerminalSolver
import xyz.aerii.athen.modules.impl.dungeon.terminals.solver.base.Click
import xyz.aerii.athen.utils.guiClick
import xyz.aerii.library.api.client
import xyz.aerii.nebulune.Nebulune

@Load
object QueueTerms : Module(
    "Queue terms",
    "Queues terminal clicks to automatically fire.",
    Category.DUNGEONS
) {
    val timeout by config.slider("Resync timeout", 800, 400, 1000, "ms")

    val clicks = mutableListOf<Click>()
    val list = mutableListOf<Click>()
    var yearning = false

    init {
        on<TickEvent.Client.Start> {
            if (list.isNotEmpty()) click(list.removeFirst())
        }.runWhen(TerminalAPI.terminalOpen)

        TerminalAPI.terminalOpen.onChange {
            list.clear()
        }
    }

    private fun click(click: Click) {
        yearning = true

        if (TerminalSimulator.s.value) {
            val screen = client.screen as? ITerminalSim ?: return
            val slots = screen.menu.slots ?: return
            val slotIndex = click.slot
            if (slotIndex >= slots.size) return

            val slot = slots[slotIndex]
            screen.slotClicked(slot, slotIndex, click.button, if (click.button == 0) ContainerInput.CLONE else ContainerInput.PICKUP)

            if (TerminalSolver.`sound$enabled`) TerminalSolver.clickSound.play()
            return
        }

        if (TerminalSolver.`sound$enabled`) TerminalSolver.clickSound.play()
        guiClick(TerminalAPI.lastId, click.slot, if (click.button == 0) 2 else click.button, if (click.button == 0) ContainerInput.CLONE else ContainerInput.PICKUP)

        val id = TerminalAPI.lastId
        val timeout0 = timeout

        Nebulune.afterTimed(timeout0) {
            if (!TerminalAPI.terminalOpen.value) return@afterTimed
            if (id != TerminalAPI.lastId) return@afterTimed

            list.clear()
            clicks.clear()
            yearning = false
        }
    }
}
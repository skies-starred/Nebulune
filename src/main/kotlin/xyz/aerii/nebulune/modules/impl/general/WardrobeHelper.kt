@file:Suppress("Unused")

package xyz.aerii.nebulune.modules.impl.general

import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket
import net.minecraft.world.inventory.ClickType
import xyz.aerii.athen.annotations.Load
import xyz.aerii.athen.api.location.LocationAPI
import xyz.aerii.athen.events.GuiEvent
import xyz.aerii.athen.events.InputEvent
import xyz.aerii.athen.events.PacketEvent
import xyz.aerii.athen.events.core.on
import xyz.aerii.athen.events.core.runWhen
import xyz.aerii.athen.handlers.React.Companion.and
import xyz.aerii.athen.handlers.Smoothie
import xyz.aerii.athen.handlers.Typo.command
import xyz.aerii.athen.handlers.Typo.stripped
import xyz.aerii.athen.modules.impl.general.WardrobeKeybinds
import xyz.aerii.nebulune.events.TickStartEvent

@Load
object WardrobeHelper {
    val autoClose by WardrobeKeybinds.config.switch("Auto close after use")
    val autoEquip = WardrobeKeybinds.config.switch("Auto equip").custom("autoEquip")
    private val _unused by WardrobeKeybinds.config.textParagraph("Automatically equips the wardrobe slot without opening the gui. Use at your own risk.")

    private var slot0: WardrobeKeybinds.WardrobeSlot? = null
    private var swapping = false
    private var id = -1

    init {
        on<InputEvent.Keyboard.Press> {
            if (swapping) return@on
            val slot = WardrobeKeybinds.wardrobeSlots.find { it.value == keyEvent.key }
                ?.takeIf { it.slot?.item?.isEmpty == false } ?: return@on

            slot0 = slot
            swapping = true
            id = -1

            "wd".command()
            cancel()
        }.runWhen(autoEquip.state and LocationAPI.isOnSkyBlock)

        on<PacketEvent.Receive, ClientboundOpenScreenPacket> {
            if (!swapping) return@on
            if ("Wardrobe" !in title.stripped()) return@on
            val player = Smoothie.client.player ?: return@on

            Smoothie.mainThread {
                player.containerMenu = type.create(containerId, player.inventory)
            }

            id = containerId
            it.cancel()
        }.runWhen(autoEquip.state and LocationAPI.isOnSkyBlock)

        on<GuiEvent.Container.Close> {
            reset()
        }.runWhen(autoEquip.state and LocationAPI.isOnSkyBlock)

        on<TickStartEvent> {
            if (!swapping) return@on

            val player = Smoothie.client.player ?: return@on
            val menu = player.containerMenu ?: return@on
            val slot = slot0 ?: return@on

            if (menu.containerId != id) return@on

            val mcSlot = menu.slots.getOrNull(slot.idx) ?: return@on
            if (mcSlot.item.isEmpty) return@on

            if (!slot.equipped) {
                Smoothie.client.gameMode?.handleInventoryMouseClick(id, slot.idx, 0, ClickType.PICKUP, player)
            }

            player.closeContainer()
            reset()
        }.runWhen(autoEquip.state and LocationAPI.isOnSkyBlock)
    }

    private fun reset() {
        swapping = false
        slot0 = null
        id = -1
    }
}
@file:Suppress("Unused")

package xyz.aerii.nebulune.modules.impl.general

import com.mojang.brigadier.arguments.IntegerArgumentType
import net.minecraft.client.KeyMapping
import net.minecraft.network.protocol.game.ClientboundContainerClosePacket
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket
import net.minecraft.world.inventory.ClickType
import net.minecraft.world.item.Items
import xyz.aerii.athen.annotations.Load
import xyz.aerii.athen.events.CommandRegistration
import xyz.aerii.athen.events.GuiEvent
import xyz.aerii.athen.events.InputEvent
import xyz.aerii.athen.events.PacketEvent
import xyz.aerii.athen.events.TickEvent
import xyz.aerii.athen.events.core.on
import xyz.aerii.athen.events.core.runWhen
import xyz.aerii.athen.handlers.Chronos
import xyz.aerii.athen.handlers.Typo
import xyz.aerii.athen.handlers.Typo.modMessage
import xyz.aerii.athen.mixin.accessors.KeyMappingAccessor
import xyz.aerii.athen.modules.impl.general.WardrobeKeybinds
import xyz.aerii.athen.utils.render.Render2D.sizedText
import xyz.aerii.library.api.client
import xyz.aerii.library.api.command
import xyz.aerii.library.api.mainThread
import xyz.aerii.library.handlers.Observable.Companion.and
import xyz.aerii.library.handlers.time.client
import xyz.aerii.library.utils.stripped
import xyz.aerii.nebulune.Nebulune

@Load
object WardrobeHelper {
    val autoClose by WardrobeKeybinds.config.switch("Auto close after use")
    private val autoEquip = WardrobeKeybinds.config.switch("Auto equip").custom("autoEquip")
    private val _unused by WardrobeKeybinds.config.textParagraph("Automatically equips the wardrobe slot without opening the gui. Use at your own risk.")
    private val moveEquip by WardrobeKeybinds.config.switch("Equip while moving").dependsOn { autoEquip.value }
    private val _unused0 by WardrobeKeybinds.config.textParagraph("Equip while moving increases your chances of being banned by a lot.").dependsOn { autoEquip.value && moveEquip }
    private val resetOpen by WardrobeKeybinds.config.switch("Reset on GUI open", true).dependsOn { autoEquip.value }
    private val equipDelay by WardrobeKeybinds.config.slider("Click delay", 1, 0, 8, "ticks").dependsOn { autoEquip.value }
    private val closeDelay by WardrobeKeybinds.config.slider("Close delay", 1, 0, 8, "ticks").dependsOn { autoEquip.value }
    private val delayVariance by WardrobeKeybinds.config.slider("Max delay variety", 1, 0, 5, "ticks").dependsOn { autoEquip.value }

    private val hud = WardrobeKeybinds.config.hud("Display text") {
        if (it) return@hud sizedText("Equipping §7[§c2§7]")
        if (!swapping) return@hud null
        val slot = slot0 ?: return@hud null
        sizedText("Equipping §7[§c${(slot.idx - 36) + 1}§7]")
    }

    private val all: List<KeyMapping>
        get() = listOf(
            client.options.keyUp,
            client.options.keyDown,
            client.options.keyLeft,
            client.options.keyRight,
            client.options.keyJump,
            client.options.keyShift
        )

    private var slot0: WardrobeKeybinds.WardrobeSlot? = null
    private var swapping: Boolean = false
    private var inMenu: Boolean = false
    private var id: Int = -1
    private var wait: Int = 0
    private var start: Long = 0

    init {
        on<CommandRegistration> {
            event.register(Nebulune.modId) {
                then("wd") {
                    thenCallback("slot", IntegerArgumentType.integer(1, 9)) {
                        if (!WardrobeKeybinds.enabled) return@thenCallback "Enable wardrobe keybinds!".modMessage(Typo.PrefixType.ERROR)
                        if (!autoEquip.value) return@thenCallback "Enable auto equip in wardrobe keybinds!".modMessage(Typo.PrefixType.ERROR)

                        val int = IntegerArgumentType.getInteger(this, "slot")
                        val slot = WardrobeKeybinds.wardrobeSlots.find { it.idx == 35 + int } ?: return@thenCallback

                        slot0 = slot
                        swapping = true
                        id = -1
                        start = System.currentTimeMillis()

                        "wd".command()
                    }
                }
            }
        }

        on<InputEvent.Keyboard.Press> {
            if (client.screen != null) return@on

            val key = keyEvent.key

            if (!moveEquip && swapping) for (a in all) if ((a as KeyMappingAccessor).boundKey.value == key) return@on cancel()
            if (swapping) return@on

            val slot = WardrobeKeybinds.wardrobeSlots.find { it.value == key } ?: return@on

            slot0 = slot
            swapping = true
            id = -1
            start = System.currentTimeMillis()

            "wd".command()
            cancel()
        }.runWhen(WardrobeKeybinds.observable and autoEquip.state)

        on<PacketEvent.Receive, ClientboundOpenScreenPacket> {
            if (!swapping) return@on
            if ("Wardrobe" !in title.stripped()) return@on
            val player = client.player ?: return@on

            mainThread {
                if (!moveEquip) for (a in all) a.isDown = false
                player.containerMenu = type.create(containerId, player.inventory)
            }

            id = containerId
            wait = equipDelay + (0..delayVariance).random()
            inMenu = true
            it.cancel()
        }.runWhen(WardrobeKeybinds.observable and autoEquip.state)

        on<PacketEvent.Receive, ClientboundContainerClosePacket> {
            reset()
        }.runWhen(WardrobeKeybinds.observable and autoEquip.state)

        on<PacketEvent.Send, ServerboundContainerClosePacket> {
            reset()
        }.runWhen(WardrobeKeybinds.observable and autoEquip.state)

        on<GuiEvent.Open.Container> {
            if (resetOpen) reset()
        }.runWhen(WardrobeKeybinds.observable and autoEquip.state)

        on<TickEvent.Client.Start> {
            if (!swapping) return@on
            if (System.currentTimeMillis() - start > 2000) return@on reset()
            if (!inMenu) return@on
            if (wait-- > 0) return@on

            val player = client.player ?: return@on
            val menu = player.containerMenu ?: return@on
            val slot = slot0 ?: return@on

            if (menu.containerId != id) return@on

            val mcSlot = menu.slots.getOrNull(slot.idx)?.takeIf { it.item?.isEmpty == false } ?: return@on
            if (mcSlot.item.item == Items.GRAY_DYE) return@on

            if (!slot.equipped) client.gameMode?.handleInventoryMouseClick(id, slot.idx, 0, ClickType.PICKUP, player)

            close()
            reset()
        }.runWhen(WardrobeKeybinds.observable and autoEquip.state)
    }

    @JvmStatic
    fun close(i: Int? = null) {
        val player = client.player ?: return

        Chronos.schedule((i ?: (closeDelay + (0..delayVariance).random())).client) {
            player.closeContainer()
        }
    }

    private fun reset() {
        swapping = false
        inMenu = false
        slot0 = null
        id = -1
        wait = 0
        start = 0
    }
}
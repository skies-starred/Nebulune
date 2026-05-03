package xyz.aerii.nebulune.modules.impl.general

import net.minecraft.network.chat.ClickEvent
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import xyz.aerii.athen.annotations.Load
import xyz.aerii.athen.annotations.OnlyIn
import xyz.aerii.athen.config.Category
import xyz.aerii.athen.events.MessageEvent
import xyz.aerii.athen.handlers.Chronos
import xyz.aerii.athen.modules.Module
import xyz.aerii.library.api.command
import xyz.aerii.library.handlers.time.client
import xyz.aerii.library.handlers.time.start

@Load
@OnlyIn(skyblock = true)
object AutoConversation : Module(
    "Auto conversation",
    "Automatically has a conversation with NPCs!",
    Category.GENERAL
) {
    private val multi by config.switch("Multi-option dialogues", true)
    private val green by config.switch("Check green color", true)
    private val delay by config.slider("Click delay", 4, 0, 40, "ticks")

    init {
        on<MessageEvent.Chat.Receive> {
            if (!stripped.startsWith("[NPC] ") && !stripped.startsWith("Select an option: ")) return@on
            val a = mutableListOf<String>()
            val b = green

            val c = message.style
            val d = (c.clickEvent as? ClickEvent.RunCommand)?.command
            if (d != null && (!b || c.color?.value == TextColor.GREEN)) a.add(d)

            for (s in message.siblings) {
                val e = s.style.takeIf { !b || it.color?.value == TextColor.GREEN } ?: continue
                a.add((e.clickEvent as? ClickEvent.RunCommand)?.command ?: continue)
            }

            if (a.isEmpty()) return@on
            if (a.size > 1 && !multi) return@on
            if (delay == 0) return@on a.first().command()

            Chronos.schedule((delay + (0..3).random()).client.start) {
                a.first().command()
            }
        }
    }
}
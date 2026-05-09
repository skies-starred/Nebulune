
@file:Suppress("ConstPropertyName")

package xyz.aerii.nebulune.modules.impl.general.messageactions.actions.impl

import xyz.aerii.athen.annotations.Load
import xyz.aerii.athen.modules.impl.general.messageactions.actions.IMessageAction
import xyz.aerii.athen.modules.impl.general.messageactions.actions.MessageActionType
import xyz.aerii.library.api.message

@Load
class MessageAction(val message: String) : IMessageAction {
    private val empty = message.isEmpty()

    override val id: Int = int
    override val name: String = str
    override val serializable: String = message

    override fun run() {
        if (empty) return
        message.message()
    }

    companion object {
        const val int = 2
        const val str = "Message"

        init {
            IMessageAction.register(MessageActionType(int, str) { MessageAction(it) })
        }
    }
}
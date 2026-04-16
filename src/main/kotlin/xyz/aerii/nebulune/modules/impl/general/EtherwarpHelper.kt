package xyz.aerii.nebulune.modules.impl.general

import net.minecraft.client.KeyMapping
import net.minecraft.world.InteractionHand
import xyz.aerii.athen.annotations.Load
import xyz.aerii.athen.annotations.OnlyIn
import xyz.aerii.athen.config.Category
import xyz.aerii.athen.events.InputEvent
import xyz.aerii.athen.events.core.runWhen
import xyz.aerii.athen.handlers.Chronos
import xyz.aerii.athen.mixin.accessors.KeyMappingAccessor
import xyz.aerii.athen.modules.Module
import xyz.aerii.athen.utils.etherwarp
import xyz.aerii.library.api.client
import xyz.aerii.library.handlers.time.client
import xyz.aerii.library.handlers.time.start
import xyz.aerii.nebulune.utils.rightClick

@Load
@OnlyIn(skyblock = true)
object EtherwarpHelper : Module(
    "Etherwarp helper",
    "Helper features for Etherwarp.",
    Category.GENERAL
) {
    private val lcew = config.switch("Left click warp").custom("lcew")
    private val shift by config.switch("Shift automatically").dependsOn { lcew.value }

    private val ints = intArrayOf(2, 3, 4)

    init {
        on<InputEvent.Mouse.Press> {
            if (client.screen != null) return@on
            if (buttonInfo.button != 0) return@on

            val p = client.player ?: return@on
            if (!p.mainHandItem.etherwarp()) return@on

            val a = p.isCrouching
            if (!a && !shift) return@on

            if (!a) {
                KeyMapping.set((client.options.keyShift as KeyMappingAccessor).boundKey, true)
                Chronos.schedule(ints.random().client.start) {
                    action()

                    Chronos.schedule(1.client.start) { KeyMapping.set((client.options.keyShift as KeyMappingAccessor).boundKey, false) }
                }

                return@on cancel()
            }

            cancel()
            action()
        }.runWhen(lcew.state)
    }

    private fun action() {
        rightClick()
        with(client.player ?: return) {
            if (swinging && swingTime >= 0) return

            swingingArm = InteractionHand.MAIN_HAND
            swingTime = -1
            swinging = true
        }
    }
}
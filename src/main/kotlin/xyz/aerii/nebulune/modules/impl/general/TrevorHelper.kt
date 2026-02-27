@file:Suppress("ObjectPrivatePropertyName", "Unused")

package xyz.aerii.nebulune.modules.impl.general

import net.minecraft.network.chat.ClickEvent
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.animal.Chicken
import net.minecraft.world.entity.animal.Cow
import net.minecraft.world.entity.animal.Pig
import net.minecraft.world.entity.animal.Rabbit
import net.minecraft.world.entity.animal.horse.Horse
import net.minecraft.world.entity.animal.sheep.Sheep
import tech.thatgravyboat.skyblockapi.api.data.MayorCandidates
import tech.thatgravyboat.skyblockapi.utils.extentions.serverMaxHealth
import tech.thatgravyboat.skyblockapi.utils.regex.RegexUtils.findThenNull
import xyz.aerii.athen.annotations.Load
import xyz.aerii.athen.annotations.OnlyIn
import xyz.aerii.athen.api.location.SkyBlockIsland
import xyz.aerii.athen.config.Category
import xyz.aerii.athen.events.LocationEvent
import xyz.aerii.athen.events.MessageEvent
import xyz.aerii.athen.events.WorldRenderEvent
import xyz.aerii.athen.handlers.Chronos
import xyz.aerii.athen.handlers.Smoothie.alert
import xyz.aerii.athen.handlers.Typo.command
import xyz.aerii.athen.handlers.Typo.stripped
import xyz.aerii.athen.handlers.parse
import xyz.aerii.athen.modules.Module
import xyz.aerii.athen.ui.themes.Catppuccin
import xyz.aerii.athen.utils.render.Render2D.sizedText
import xyz.aerii.athen.utils.render.Render3D
import xyz.aerii.athen.utils.render.renderBoundingBox
import xyz.aerii.athen.utils.render.renderPos
import xyz.aerii.athen.utils.toDurationFromMillis
import xyz.aerii.nebulune.utils.drawTracer
import java.awt.Color
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@Load
@OnlyIn(islands = [SkyBlockIsland.THE_BARN])
object TrevorHelper : Module(
    "Trevor helper",
    "Helper features for Trevor the Trapper!",
    Category.GENERAL
) {
    private val mobEsp by config.switch("Animal ESP")
    private val `esp$tracer` by config.switch("Show tracer").dependsOn { mobEsp }

    private val autoCall by config.switch("Auto call")
    private val callDelay by config.slider("Call delay", 1, 0, 5, "ticks").dependsOn { autoCall }

    private val autoAccept by config.switch("Auto accept")
    private val acceptDelay by config.slider("Accept delay", 1, 0, 5, "ticks").dependsOn { autoAccept }

    private val endAlert by config.switch("Cooldown end alert")
    private val `alert$message` by config.textInput("Alert message", "<red>Cooldown ended!").dependsOn { endAlert }
    private val `alert$sound` by config.sound("Alert sound").dependsOn { endAlert }

    private val hud = config.hud("Cooldown timer") {
        if (it) return@hud sizedText("Cooldown: §c12.4s")
        if (cooldown <= 0) return@hud null
        val t = (cooldown - System.currentTimeMillis()).coerceAtLeast(0).toDurationFromMillis(secondsDecimals = 1)

        sizedText("Cooldown: §c$t")
    }

    private val colorExpandable by config.expandable("Colors")
    private val `color$trackable` by config.colorPicker("Trackable color", Color(Catppuccin.Mocha.Text.argb, true)).childOf { colorExpandable }
    private val `color$untrackable` by config.colorPicker("Untrackable color", Color(Catppuccin.Mocha.Green.argb, true)).childOf { colorExpandable }
    private val `color$undetected` by config.colorPicker("Undetected color", Color(Catppuccin.Mocha.Blue.argb, true)).childOf { colorExpandable }
    private val `color$endangered` by config.colorPicker("Endangered color", Color(Catppuccin.Mocha.Mauve.argb, true)).childOf { colorExpandable }
    private val `color$elusive` by config.colorPicker("Elusive color", Color(Catppuccin.Mocha.Yellow.argb, true)).childOf { colorExpandable }

    private val animals = setOf(Cow::class, Pig::class, Sheep::class, Chicken::class, Rabbit::class, Horse::class)
    private val startRegex = Regex("\\[NPC] Trevor: You can find your (?<type>\\w+) animal near the .*")

    private var cooldown: Long = 0
    private var rarity: Rarity? = null

    init {
        on<LocationEvent.ServerConnect> {
            reset()
        }

        on<WorldRenderEvent.Entity.Post> {
            if (!mobEsp) return@on

            val rarity = rarity ?: return@on
            val entity = entity as? LivingEntity ?: return@on
            if (entity::class !in animals) return@on

            val max = if (entity is Horse) entity.serverMaxHealth / 2f else entity.serverMaxHealth
            if (max != rarity.hp) return@on

            Render3D.drawBox(entity.renderBoundingBox, rarity.color, depthTest = false)
            if (`esp$tracer`) drawTracer(entity.renderPos, rarity.color)
        }

        on<MessageEvent.Chat.Receive> {
            startRegex.findThenNull(stripped, "type") { (t) ->
                rarity = Rarity.get(t) ?: return@findThenNull
                cooldown = System.currentTimeMillis() + 20_000L

                Chronos.Time after 20.seconds then {
                    if (endAlert) `alert$message`.parse().alert(soundType = `alert$sound`.sound)
                    cooldown = 0
                }
            } ?: return@on

            if (stripped == "Return to the Trapper soon to get a new animal to hunt!") {
                if (!autoCall) return@on reset()

                val ms = (cooldown - System.currentTimeMillis()).coerceAtLeast(0)
                val extra = (callDelay + (0..2).random()) * 50L

                Chronos.Time after (ms + extra).milliseconds then {
                    "/call trevor".command()
                }

                return@on reset()
            }

            if (!autoAccept) return@on
            if (message.siblings?.getOrNull(0)?.stripped() == "Accept the trapper's task to hunt the animal?") {
                Chronos.Tick after acceptDelay + (0..2).random() then {
                    (message.siblings[3]?.style?.clickEvent as? ClickEvent.RunCommand)?.command?.command()
                }

                return@on
            }
        }
    }

    private fun reset() {
        rarity = null
    }

    private enum class Rarity(val normal: Float, val derpy: Float) {
        Trackable(100f, 200f),
        Untrackable(500f, 1000f),
        Undetected(1000f, 2000f),
        Endangered(5000f, 10000f),
        Elusive(10000f, 20000f);

        val hp: Float
            get() = if (MayorCandidates.DERPY.isActive) derpy else normal

        val color: Color
            get() = when (this) {
                Trackable -> `color$trackable`
                Untrackable -> `color$untrackable`
                Undetected -> `color$undetected`
                Endangered -> `color$endangered`
                Elusive -> `color$elusive`
            }

        companion object {
            fun get(a: String): Rarity? = entries.find { it.name.uppercase() == a }
        }
    }
}
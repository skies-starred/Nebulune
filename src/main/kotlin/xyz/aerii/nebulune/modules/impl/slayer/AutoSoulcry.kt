@file:Suppress("Unused")

package xyz.aerii.nebulune.modules.impl.slayer

import net.minecraft.world.item.Items
import tech.thatgravyboat.skyblockapi.api.area.slayer.SlayerType
import tech.thatgravyboat.skyblockapi.api.datatype.DataTypes
import tech.thatgravyboat.skyblockapi.api.datatype.getData
import tech.thatgravyboat.skyblockapi.api.profile.StatsAPI
import xyz.aerii.athen.annotations.Load
import xyz.aerii.athen.annotations.OnlyIn
import xyz.aerii.athen.api.location.SkyBlockIsland
import xyz.aerii.athen.api.skyblock.SlayerAPI
import xyz.aerii.athen.config.Category
import xyz.aerii.athen.events.TickEvent
import xyz.aerii.athen.events.core.runWhen
import xyz.aerii.athen.modules.Module
import xyz.aerii.athen.utils.enchants
import xyz.aerii.library.api.client
import xyz.aerii.library.api.held
import xyz.aerii.nebulune.events.PlayerAttackEvent
import xyz.aerii.nebulune.utils.rightClick

@Load
@OnlyIn(islands = [SkyBlockIsland.THE_END])
object AutoSoulcry : Module(
    "Auto soulcry",
    "Automatically uses the soulcry ability of your katana!",
    Category.SLAYER
) {
    private val mana by config.switch("Check mana", true)
    private val minDelay by config.slider("Min delay", 1, 0, 5, "ticks")
    private val maxDelay by config.slider("Max delay", 3, 0, 5, "ticks")
    private val detectType = config.multiCheckbox("Detection type", listOf("Tick based", "Attack based")).custom("detectType")
    private val otherBosses by config.switch("Work on other's bosses")
    private val _unused0 by config.textParagraph("The option <red>\"Work on other's bosses\"<r> requires you to have <red>Attack based<r> selected in Detection type!")

    private val ids = setOf("VOIDEDGE_KATANA", "VORPAL_KATANA", "ATOMSPLIT_KATANA")
    private var tick = -1

    init {
        on<TickEvent.Client.Start> {
            val slayer = SlayerAPI.slayer?.type as? SlayerType ?: return@on
            if (slayer != SlayerType.VOIDGLOOM_SERAPH) return@on reset()
            if (client.screen != null) return@on reset()

            val item = held ?: return@on reset()
            if (item.item != Items.DIAMOND_SWORD) return@on reset()
            if (item.getData(DataTypes.SKYBLOCK_ID)?.skyblockId !in ids) return@on reset()

            val m = if ("ultimate_wise" in item.enchants()) 100 else 200
            if (mana && (StatsAPI.mana + StatsAPI.overflowMana) < m) return@on reset()

            if (tick == -1) return@on ::tick.set((minDelay..maxDelay.coerceAtLeast(minDelay)).random())
            if (tick-- > 0) return@on

            rightClick()
            reset()
        }.runWhen(detectType.state.map { 0 in it })

        on<PlayerAttackEvent> {
            val si = SlayerAPI.slayerBosses[entity] ?: return@on
            if (!si.isOwnedByPlayer && !otherBosses) return@on

            val item = held ?: return@on
            if (item.item != Items.DIAMOND_SWORD) return@on
            if (item.getData(DataTypes.SKYBLOCK_ID)?.skyblockId !in ids) return@on

            val m = if ("ultimate_wise" in item.enchants()) 100 else 200
            if (mana && (StatsAPI.mana + StatsAPI.overflowMana) < m) return@on

            rightClick()
        }.runWhen(detectType.state.map { 1 in it })
    }

    private fun reset() {
        tick = -1
    }
}
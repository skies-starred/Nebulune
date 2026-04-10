package xyz.aerii.nebulune.modules.impl.render

import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.decoration.ArmorStand
import net.minecraft.world.phys.AABB
import tech.thatgravyboat.skyblockapi.utils.extentions.getTexture
import xyz.aerii.athen.annotations.Load
import xyz.aerii.athen.annotations.OnlyIn
import xyz.aerii.athen.api.location.SkyBlockIsland
import xyz.aerii.athen.config.Category
import xyz.aerii.athen.events.LocationEvent
import xyz.aerii.athen.events.PacketEvent
import xyz.aerii.athen.events.WorldRenderEvent
import xyz.aerii.athen.handlers.Chronos
import xyz.aerii.athen.modules.Module
import xyz.aerii.athen.ui.themes.Catppuccin
import xyz.aerii.athen.utils.render.Render3D
import xyz.aerii.athen.utils.render.renderPos
import xyz.aerii.library.api.level
import xyz.aerii.library.handlers.time.client
import xyz.aerii.nebulune.utils.drawTracer
import java.awt.Color

@Load
@OnlyIn(islands = [SkyBlockIsland.HUB])
object RatESP : Module(
    "Rat ESP",
    "Shows an ESP for rats in Hub.",
    Category.RENDER
) {
    private const val RAT = "ewogICJ0aW1lc3RhbXAiIDogMTYxODQxOTcwMTc1MywKICAicHJvZmlsZUlkIiA6ICI3MzgyZGRmYmU0ODU0NTVjODI1ZjkwMGY4OGZkMzJmOCIsCiAgInByb2ZpbGVOYW1lIiA6ICJCdUlJZXQiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYThhYmI0NzFkYjBhYjc4NzAzMDExOTc5ZGM4YjQwNzk4YTk0MWYzYTRkZWMzZWM2MWNiZWVjMmFmOGNmZmU4IiwKICAgICAgIm1ldGFkYXRhIiA6IHsKICAgICAgICAibW9kZWwiIDogInNsaW0iCiAgICAgIH0KICAgIH0KICB9Cn0="

    private val tracer by config.switch("Tracer")
    private val thickness by config.slider("Thickness", 2, 1, 10)
    private val color by config.colorPicker("Color", Color(Catppuccin.Mocha.Peach.rgba))
    private val entities = mutableSetOf<Entity>()

    init {
        on<PacketEvent.Receive, ClientboundSetEntityDataPacket> {
            Chronos.schedule(2.client) {
                val entity = level?.getEntity(id) as? ArmorStand ?: return@schedule
                if (!entity.hasItemInSlot(EquipmentSlot.HEAD)) return@schedule
                if (entity in entities) return@schedule
                if (entity.getItemBySlot(EquipmentSlot.HEAD)?.getTexture() != RAT) return@schedule

                entities.add(entity)
            }
        }

        on<WorldRenderEvent.Extract> {
            val it = entities.iterator()
            while (it.hasNext()) {
                val e = it.next()
                if (!e.isAlive) {
                    it.remove()
                    continue
                }

                val p = e.renderPos.add(-0.5, 1.0, -0.5)
                Render3D.drawBox(AABB.unitCubeFromLowerCorner(p), color, thickness.toFloat(), false)
                if (tracer) drawTracer(p, color, thickness.toFloat(), false)
            }
        }

        on<LocationEvent.Server.Connect> {
            entities.clear()
        }
    }
}
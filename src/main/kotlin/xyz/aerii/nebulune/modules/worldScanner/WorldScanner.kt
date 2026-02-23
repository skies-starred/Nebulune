package xyz.aerii.nebulune.modules.worldScanner

import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.SlabBlock
import net.minecraft.world.level.block.state.properties.EnumProperty
import net.minecraft.world.level.block.state.properties.SlabType
import net.minecraft.world.level.chunk.LevelChunk
import net.minecraft.world.phys.AABB
import xyz.aerii.athen.annotations.Load
import xyz.aerii.athen.annotations.OnlyIn
import xyz.aerii.athen.api.location.SkyBlockIsland
import xyz.aerii.athen.config.Category
import xyz.aerii.athen.config.ExpandableHandle
import xyz.aerii.athen.events.LocationEvent
import xyz.aerii.athen.events.WorldRenderEvent
import xyz.aerii.athen.handlers.Notifier.notify
import xyz.aerii.athen.modules.Module
import xyz.aerii.athen.utils.render.Render3D
import xyz.aerii.nebulune.events.ClientChunkEvent
import xyz.aerii.nebulune.utils.drawString
import xyz.aerii.nebulune.utils.drawTracer
import java.awt.Color

@Load
@OnlyIn(islands = [SkyBlockIsland.CRYSTAL_HOLLOWS])
object WorldScanner: Module(
    "World Scanner",
    "Scan Crystal Hollow world for structures",
    Category.RENDER
) {
    data class StructureEspConfig(
        val expandable: ExpandableHandle,
        val enable: () -> Boolean,
        val highlightStyle: () -> Int,
        val color: () -> Color,
        val tracer: () -> Boolean,
        val displayName: () -> Boolean,
        val displayScale: () -> Float,
        val displayBackgroundOpacity: () -> Float,
        val showNotification: () -> Boolean
    )

    fun createStructureEspConfig(
        expandable: ExpandableHandle,
        defaultColor: Color,
    ): StructureEspConfig {
        val key = expandable.key

        val enable by config.switch("Enable", true)
            .unique(key + "Enable")
            .childOf { expandable }

        val highlightStyle by config.dropdown("Highlight Style", listOf("Outline", "Filled", "Both"), 2)
            .unique(key + "Highlight Style")
            .childOf { expandable }

        val color by config.colorPicker("ESP Color", defaultColor)
            .unique(key + "ESP Color")
            .childOf { expandable }

        val tracer by config.switch("Tracer", false)
            .unique(key + "Tracer")
            .childOf { expandable }

        val displayName by config.switch("Display Name", true)
            .unique(key + "Display Name")
            .childOf { expandable }

        val displayScale by config.slider("Name Scale", 1f, 0f, 1f, showDouble = true)
            .unique(key + "Name Scale")
            .dependsOn { displayName }
            .childOf { expandable }

        val displayBackgroundOpacity by config.slider("Display Background Opacity", 0.5f, 0f, 1f, showDouble = true)
            .unique(key + "Display Background Opacity")
            .dependsOn { displayName }
            .childOf { expandable }

        val notification by config.switch("Show Notification", true)
            .unique(key + "Show Notification")
            .childOf { expandable }

        return StructureEspConfig(
            expandable,
            { enable },
            { highlightStyle },
            { color },
            { tracer },
            { displayName },
            { displayScale },
            { displayBackgroundOpacity },
            { notification }
        )
    }

    private val grottoExpandable by config.expandable("Fairy Grotto")
    val grottoConfig = createStructureEspConfig(
        grottoExpandable,
        Color(255, 85, 255)
    )
    val grottoConfigShowNumberOfBlocks by config.switch("Show Number of Blocks", true).childOf { grottoExpandable }
    val grottoConfigShowNumberOfBlocksBackgroundOpacity by config.slider("Text Background Opacity", 0.5f, 0f, 1f, showDouble = true).dependsOn { grottoConfigShowNumberOfBlocks }.childOf { grottoExpandable }

    private val sapphireExpandable by config.expandable("Sapphire Crystal")
    val sapphireConfig = createStructureEspConfig(
        sapphireExpandable,
        Color(85, 255, 255)
    )

    private val amberExpandable by config.expandable("Amber Crystal")
    val amberConfig = createStructureEspConfig(
        amberExpandable,
        Color(255, 170, 0)
    )

    private val amethystExpandable by config.expandable("Amethyst Crystal")
    val amethystConfig = createStructureEspConfig(
        amethystExpandable,
        Color(170, 0, 170)
    )

    private val jadeExpandable by config.expandable("Jade Crystal")
    val jadeConfig = createStructureEspConfig(
        jadeExpandable,
        Color(0, 170, 0)
    )

    private val topazExpandable by config.expandable("Topaz Crystal")
    val topazConfig = createStructureEspConfig(
        topazExpandable,
        Color(255, 255, 85)
    )

    private val corleoneExpandable by config.expandable("Corleone")
    val corleoneConfig = createStructureEspConfig(
        corleoneExpandable,
        Color(85, 255, 85)
    )

    private val goldenDragonExpandable by config.expandable("Golden Dragon")
    val goldenDragonConfig = createStructureEspConfig(
        goldenDragonExpandable,
        Color(255, 255, 255)
    )

    private val keyGuardianExpandable by config.expandable("Key Guardian")
    val keyGuardianConfig = createStructureEspConfig(
        keyGuardianExpandable,
        Color(170, 0, 170)
    )

    private val odawaExpandable by config.expandable("Odawa")
    val odawaConfig = createStructureEspConfig(
        odawaExpandable,
        Color(170, 170, 170)
    )

    private val wormFishingExpandable by config.expandable("Worm Fishing")
    val wormFishingConfig = createStructureEspConfig(
        wormFishingExpandable,
        Color(255, 85, 85)
    )

    private val grottos = mutableListOf<Triple<Pair<Int, Int>, BlockPos, Int>>()
    private val grottoChunks = mutableListOf<Triple<Pair<Int, Int>, BlockPos, Int>>()
    private val structures = mutableListOf<Pair<Structure, Triple<Int, Int, Int>>>()
    private val scannedChunks = mutableListOf<Pair<Int, Int>>()
    private val grottoChunksMap =
        mutableMapOf<Pair<Int, Int>, Triple<Pair<Int, Int>, BlockPos, Int>>()

    init {
        on<ClientChunkEvent.Load> {
            scanChunk(chunk)
        }

        on<WorldRenderEvent.Extract> {
            if (grottoConfig.enable()) {
                for (grotto in grottos) {
                    val blockPos = grotto.second
                    val center = blockPos.center
                    val aabb = AABB(blockPos)
                    val color = grottoConfig.color()

                    when (grottoConfig.highlightStyle()) {
                        0 -> Render3D.drawBox(aabb, color, 2f, false)
                        1 -> Render3D.drawFilledBox(aabb, color, false)
                        2 -> Render3D.drawStyledBox(aabb, color, Render3D.BoxStyle.BOTH, 2f, false)
                    }
                    if (grottoConfig.tracer()) drawTracer(center, grottoConfig.color(), 2f, false)
                    if (grottoConfig.displayName()) drawString(
                        "Fairy Grotto",
                        center.add(0.0, 10.0, 0.0),
                        grottoConfig.color().rgb,
                        Color(0, 0, 0, (255 * grottoConfig.displayBackgroundOpacity()).toInt()).rgb,
                        grottoConfig.displayScale(),
                        false,
                        true,
                        true
                    )
                    if (grottoConfigShowNumberOfBlocks) drawString(
                        grotto.third.toString(),
                        center,
                        grottoConfig.color().rgb,
                        Color(0, 0, 0, (255 * grottoConfigShowNumberOfBlocksBackgroundOpacity).toInt()).rgb,
                        grottoConfig.displayScale(),
                        false,
                        true,
                        true
                    )
                }
            }
            for (structure in structures) {
                if (structure.first.config.enable()) {
                    val structureConfig = structure.first.config
                    val pos = structure.second
                    val blockPos = BlockPos(pos.first, pos.second, pos.third)
                    val aabb = AABB(blockPos)
                    val color = structureConfig.color()
                    when (structureConfig.highlightStyle()) {
                        0 -> Render3D.drawBox(aabb, color, 2f, false)
                        1 -> Render3D.drawFilledBox(aabb, color, false)
                        2 -> Render3D.drawStyledBox(aabb, color, Render3D.BoxStyle.BOTH, 2f, false)
                    }
                    if (structureConfig.tracer()) drawTracer(blockPos.center, structureConfig.color(), 2f, false)
                    if (structureConfig.displayName()) drawString(
                        structure.first.displayName,
                        blockPos.center,
                        structureConfig.color().rgb,
                        Color(0, 0, 0, (255 * structureConfig.displayBackgroundOpacity()).toInt()).rgb,
                        structureConfig.displayScale(),
                        false,
                        true,
                        true
                    )
                }
            }
        }

        on<LocationEvent.ServerConnect> {
            grottos.clear()
            grottoChunks.clear()
            structures.clear()
            scannedChunks.clear()
            grottoChunksMap.clear()
        }
    }

    private fun scanStructure(
        chunk: LevelChunk,
        structure: Structure,
        x: Int,
        y: Int,
        z: Int
    ): Boolean {

        if (structure == Structure.FAIRY_GROTTO) {
            return false
        }

        val worldX = chunk.pos.x * 16 + x
        val worldZ = chunk.pos.z * 16 + z
        val worldPos = BlockPos(worldX, y, worldZ)
        if (
            structure == Structure.WORM_FISHING &&
            CrystalHollowsQuarter.MAGMA_FIELDS.testPredicate(worldPos)
        ) {
            return false
        }

        if (!structure.quarter.testPredicate(worldPos)) {
            return false
        }

        val blockPos = BlockPos.MutableBlockPos()

        for (structureY in structure.blocks.indices) {

            blockPos.set(x, y + structureY, z)

            val (block, enumProperty, expectedValue) = structure.blocks[structureY]

            if (block == null) continue

            val worldState = chunk.getBlockState(blockPos)

            if (!worldState.`is`(block)) {
                return false
            }

            if (enumProperty != null && expectedValue != null) {
                if (
                    !worldState.hasProperty(enumProperty) ||
                    worldState.getValue(enumProperty) != expectedValue
                ) {
                    return false
                }
            }
        }

        return true
    }

    private fun getAllNearbyGrottoChunks(x: Int, z: Int)
            : MutableList<Triple<Pair<Int, Int>, BlockPos, Int>> {

        val result = mutableListOf<Triple<Pair<Int, Int>, BlockPos, Int>>()
        val visited = mutableSetOf<Pair<Int, Int>>()
        val queue = ArrayDeque<Pair<Int, Int>>()

        queue.add(x to z)

        while (queue.isNotEmpty()) {
            val (cx, cz) = queue.removeFirst()
            val key = cx to cz

            if (!visited.add(key)) continue

            val current = grottoChunksMap[key] ?: continue
            result.add(current)

            for (dx in -1..1) {
                for (dz in -1..1) {
                    if (dx == 0 && dz == 0) continue
                    queue.add(cx + dx to cz + dz)
                }
            }
        }

        return result
    }

    private fun scanChunk(chunk: LevelChunk) {
        if (Pair(chunk.pos.x, chunk.pos.z) in scannedChunks) return
        scannedChunks.add(Pair(chunk.pos.x, chunk.pos.z))

        val structuresToScan = mutableListOf<Structure>()

        for (structure in Structure.entries) {
            if (structure.config.enable()) {
                structuresToScan.add(structure)
            }
        }

        val worldPos = BlockPos.MutableBlockPos()
        val chunkPos = BlockPos.MutableBlockPos()
        val chunkJasperBlocks = mutableListOf<BlockPos>()
        val chunkX = chunk.pos.x
        val chunkZ = chunk.pos.z

        val foundStructure = structures.mapTo(mutableSetOf()) { it.first }

        for (x in 0..15) {
            for (z in 0..15) {
                for (y in 0..169) {

                    val worldX = chunkX * 16 + x
                    val worldZ = chunkZ * 16 + z
                    val worldY = y

                    worldPos.set(worldX, worldY, worldZ)

                    for (structureToScan in structuresToScan) {

                        if (structureToScan in foundStructure) continue

                        if (scanStructure(chunk, structureToScan, x, y, z)) {

                            if (structureToScan.config.showNotification())
                                (structureToScan.displayName + " Found")
                                    .notify(duration = 5000)

                            structures.add(
                                structureToScan to Triple(
                                    worldX + structureToScan.offsetX,
                                    worldY + structureToScan.offsetY,
                                    worldZ + structureToScan.offsetZ
                                )
                            )

                            foundStructure.add(structureToScan)
                        }
                    }

                    if (grottoConfig.enable()) {
                        chunkPos.set(x, y, z)

                        val state = chunk.getBlockState(chunkPos)

                        if (
                            state.`is`(Blocks.MAGENTA_STAINED_GLASS_PANE) ||
                            state.`is`(Blocks.MAGENTA_STAINED_GLASS)
                        ) {
                            if (!CrystalHollowsQuarter.NUCLEUS.testPredicate(worldPos)) {
                                chunkJasperBlocks.add(worldPos)
                            }
                        }
                    }
                }
            }
        }


        if (chunkJasperBlocks.isEmpty()) return

        val center = BlockPos(
            chunkJasperBlocks.sumOf { it.x } / chunkJasperBlocks.size,
            chunkJasperBlocks.sumOf { it.y } / chunkJasperBlocks.size,
            chunkJasperBlocks.sumOf { it.z } / chunkJasperBlocks.size
        )

        if (CrystalHollowsQuarter.NUCLEUS.testPredicate(center)) return

        grottoChunks.add(Triple(Pair(chunkX, chunkZ), center, chunkJasperBlocks.size))
        grottoChunksMap[Pair(chunkX, chunkZ)] = Triple(Pair(chunkX, chunkZ), center, chunkJasperBlocks.size)

        val cluster = getAllNearbyGrottoChunks(chunkX, chunkZ)
        if (cluster.isEmpty()) return

        val merged = BlockPos(
            cluster.sumOf { it.second.x } / cluster.size,
            cluster.sumOf { it.second.y } / cluster.size,
            cluster.sumOf { it.second.z } / cluster.size
        )

        val numGrottos = grottos.size

        grottos.removeIf { grotto ->
            cluster.any { it.first.first == grotto.first.first && it.first.second == grotto.first.second }
        }

        grottos.add(Triple(Pair(chunkX, chunkZ), merged, cluster.sumOf { it.third }))

        if (grottoConfig.showNotification() && numGrottos != grottos.size) (Structure.FAIRY_GROTTO.displayName + " Found").notify(duration = 5000)
    }
}
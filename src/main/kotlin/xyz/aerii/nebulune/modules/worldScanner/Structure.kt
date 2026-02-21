package xyz.aerii.nebulune.modules.worldScanner

import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.SlabBlock
import net.minecraft.world.level.block.state.properties.EnumProperty
import net.minecraft.world.level.block.state.properties.SlabType
import xyz.aerii.nebulune.modules.worldScanner.WorldScanner.StructureEspConfig
import xyz.aerii.nebulune.modules.worldScanner.WorldScanner.amberConfig
import xyz.aerii.nebulune.modules.worldScanner.WorldScanner.amethystConfig
import xyz.aerii.nebulune.modules.worldScanner.WorldScanner.corleoneConfig
import xyz.aerii.nebulune.modules.worldScanner.WorldScanner.goldenDragonConfig
import xyz.aerii.nebulune.modules.worldScanner.WorldScanner.grottoConfig
import xyz.aerii.nebulune.modules.worldScanner.WorldScanner.jadeConfig
import xyz.aerii.nebulune.modules.worldScanner.WorldScanner.keyGuardianConfig
import xyz.aerii.nebulune.modules.worldScanner.WorldScanner.odawaConfig
import xyz.aerii.nebulune.modules.worldScanner.WorldScanner.sapphireConfig
import xyz.aerii.nebulune.modules.worldScanner.WorldScanner.topazConfig
import xyz.aerii.nebulune.modules.worldScanner.WorldScanner.wormFishingConfig

enum class Structure(
    val blocks: MutableList<Triple<Block?, EnumProperty<*>?, Comparable<*>?>>,
    val structureType: StructureType,
    val quarter: CrystalHollowsQuarter,
    val displayName: String,
    val config: StructureEspConfig,
    val offsetX: Int,
    val offsetY: Int,
    val offsetZ: Int
) {
    KING(
        mutableListOf(
            Triple(Blocks.RED_WOOL, null, null),
            Triple(Blocks.DARK_OAK_STAIRS, null, null),
            Triple(Blocks.DARK_OAK_STAIRS, null, null),
            Triple(Blocks.DARK_OAK_STAIRS, null, null),
        ),
        StructureType.CH_CRYSTALS,
        CrystalHollowsQuarter.GOBLIN_HOLDOUT,
        "King",
        amberConfig,
        1,
        -1,
        2
    ),

    QUEEN(
        mutableListOf(
            Triple(Blocks.STONE, null, null),
            Triple(Blocks.ACACIA_WOOD, null, null),
            Triple(Blocks.ACACIA_WOOD, null, null),
            Triple(Blocks.ACACIA_WOOD, null, null),
            Triple(Blocks.ACACIA_WOOD, null, null),
            Triple(Blocks.CAULDRON, null, null),
        ),
        StructureType.CH_CRYSTALS,
        CrystalHollowsQuarter.ANY,
        "Queen",
        amberConfig,
        0,
        5,
        0
    ),

    DIVAN(
        mutableListOf(
            Triple(Blocks.QUARTZ_PILLAR, null, null),
            Triple(Blocks.QUARTZ_STAIRS, null, null),
            Triple(Blocks.STONE_BRICK_STAIRS, null, null),
            Triple(Blocks.CHISELED_STONE_BRICKS, null, null),
        ),
        StructureType.CH_CRYSTALS,
        CrystalHollowsQuarter.MITHRIL_DEPOSITS,
        "Divan",
        jadeConfig,
        0,
        5,
        0
    ),

    CITY(
        mutableListOf(
            Triple(Blocks.STONE_BRICKS, null, null),
            Triple(Blocks.COBBLESTONE, null, null),
            Triple(Blocks.COBBLESTONE, null, null),
            Triple(Blocks.COBBLESTONE, null, null),
            Triple(Blocks.COBBLESTONE, null, null),
            Triple(Blocks.COBBLESTONE_STAIRS, null, null),
            Triple(Blocks.POLISHED_ANDESITE, null, null),
            Triple(Blocks.POLISHED_ANDESITE, null, null),
            Triple(Blocks.DARK_OAK_STAIRS, null, null),
        ),
        StructureType.CH_CRYSTALS,
        CrystalHollowsQuarter.PRECURSOR_REMNANTS,
        "City",
        sapphireConfig,
        24,
        0,
        -17
    ),

    TEMPLE(
        mutableListOf(
            Triple(Blocks.BEDROCK, null, null),
            Triple(Blocks.CLAY, null, null),
            Triple(Blocks.CLAY, null, null),
            Triple(Blocks.LIME_TERRACOTTA, null, null),
            Triple(Blocks.GREEN_WOOL, null, null),
            Triple(Blocks.OAK_LEAVES, null, null),
            Triple(Blocks.OAK_LEAVES, null, null),
        ),
        StructureType.CH_CRYSTALS,
        CrystalHollowsQuarter.ANY,
        "Temple",
        amethystConfig,
        -45,
        47,
        -18
    ),

    BAL(
        mutableListOf(
            Triple(Blocks.LAVA, null, null),
            Triple(Blocks.BARRIER, null, null),
            Triple(Blocks.BARRIER, null, null),
            Triple(Blocks.BARRIER, null, null),
            Triple(Blocks.BARRIER, null, null),
            Triple(Blocks.BARRIER, null, null),
            Triple(Blocks.BARRIER, null, null),
            Triple(Blocks.BARRIER, null, null),
            Triple(Blocks.BARRIER, null, null),
            Triple(Blocks.BARRIER, null, null),
            Triple(Blocks.BARRIER, null, null),
        ),
        StructureType.CH_CRYSTALS,
        CrystalHollowsQuarter.MAGMA_FIELDS,
        "Bal",
        topazConfig,
        0,
        1,
        0
    ),

    CORLEONE_DOCK(
        mutableListOf(
            Triple(Blocks.STONE_BRICKS, null, null), // 0
            Triple(Blocks.STONE_BRICKS, null, null), // 1
            Triple(Blocks.STONE_BRICKS, null, null), // 2
            Triple(Blocks.STONE_BRICKS, null, null), // 3
            Triple(null, null, null), // 4
            Triple(null, null, null), // 5
            Triple(null, null, null), // 6
            Triple(null, null, null), // 7
            Triple(null, null, null), // 8
            Triple(null, null, null), // 9
            Triple(null, null, null), // 10
            Triple(null, null, null), // 11
            Triple(null, null, null), // 12
            Triple(null, null, null), // 13
            Triple(null, null, null), // 14
            Triple(null, null, null), // 15
            Triple(null, null, null), // 16
            Triple(null, null, null), // 17
            Triple(null, null, null), // 18
            Triple(null, null, null), // 19
            Triple(null, null, null), // 20
            Triple(null, null, null), // 21
            Triple(null, null, null), // 22
            Triple(null, null, null), // 23
            Triple(Blocks.STONE_BRICKS, null, null), // 24
            Triple(Blocks.STONE_BRICKS, null, null), // 25
            Triple(Blocks.FIRE, null, null), // 26
            Triple(Blocks.STONE_BRICKS, null, null), // 27
        ),
        StructureType.CORLEONE,
        CrystalHollowsQuarter.MITHRIL_DEPOSITS,
        "Corleone Dock",
        corleoneConfig,
        23,
        11,
        17
    ),

    CORLEONE_HOLE(
        mutableListOf(
            Triple(Blocks.STONE_SLAB, SlabBlock.TYPE, SlabType.BOTTOM), // 0
            Triple(null, null, null), // 1
            Triple(null, null, null), // 2
            Triple(null, null, null), // 3
            Triple(null, null, null), // 4
            Triple(null, null, null), // 5
            Triple(null, null, null), // 6
            Triple(null, null, null), // 7
            Triple(null, null, null), // 8
            Triple(null, null, null), // 9
            Triple(null, null, null), // 10
            Triple(null, null, null), // 11
            Triple(null, null, null), // 12
            Triple(null, null, null), // 13
            Triple(Blocks.STONE_SLAB, SlabBlock.TYPE, SlabType.TOP), // 14
            Triple(Blocks.SMOOTH_STONE_SLAB, null, null), // 15
            Triple(null, null, null), // 16
            Triple(Blocks.STONE_SLAB, SlabBlock.TYPE, SlabType.TOP), // 17
            Triple(Blocks.STONE_BRICKS, null, null), // 18
        ),
        StructureType.CORLEONE,
        CrystalHollowsQuarter.MITHRIL_DEPOSITS,
        "Corleone Hole",
        corleoneConfig,
        0,
        -3,
        34
    ),

    KEY_GUARDIAN_SPIRAL(
        mutableListOf(
            Triple(Blocks.JUNGLE_STAIRS, null, null), // 0
            Triple(Blocks.JUNGLE_PLANKS, null, null), // 1
            Triple(Blocks.GLOWSTONE, null, null), // 2
            Triple(Blocks.BROWN_CARPET, null, null), // 3
            Triple(null, null, null), // 4
            Triple(Blocks.JUNGLE_SLAB, null, null), // 5
            Triple(null, null, null), // 6
            Triple(Blocks.JUNGLE_STAIRS, null, null), // 7
            Triple(Blocks.STONE, null, null), // 8
            Triple(Blocks.STONE, null, null), // 9
            Triple(Blocks.STONE, null, null),  // 10
        ),
        StructureType.KEY_GUARDIAN,
        CrystalHollowsQuarter.JUNGLE,
        "Key Guardian Spiral",
        keyGuardianConfig,
        0,
        0,
        0
    ),

    KEY_GUARDIAN_TOWER(
        mutableListOf(
            Triple(Blocks.STONE, null, null),
            Triple(Blocks.POLISHED_GRANITE, null, null),
            Triple(Blocks.JUNGLE_SLAB, SlabBlock.TYPE, SlabType.TOP),
            Triple(null, null, null),
            Triple(Blocks.JUNGLE_SLAB, SlabBlock.TYPE, SlabType.TOP),
            Triple(null, null, null),
            Triple(Blocks.JUNGLE_SLAB, SlabBlock.TYPE, SlabType.TOP),
            Triple(null, null, null),
            Triple(Blocks.JUNGLE_SLAB, SlabBlock.TYPE, SlabType.TOP),
            Triple(Blocks.JUNGLE_SLAB, SlabBlock.TYPE, SlabType.TOP),
            Triple(Blocks.JUNGLE_PLANKS, null, null),

            ),
        StructureType.KEY_GUARDIAN,
        CrystalHollowsQuarter.JUNGLE,
        "Key Guardian Tower",
        keyGuardianConfig,
        0,
        0,
        0
    ),

    ODAWA(
        mutableListOf(
            Triple(Blocks.JUNGLE_LOG, null, null), // 0
            Triple(Blocks.SPRUCE_STAIRS, null, null), // 1
            Triple(Blocks.SPRUCE_STAIRS, null, null), // 2
            Triple(Blocks.JUNGLE_LOG, null, null), // 3
            Triple(Blocks.SPRUCE_STAIRS, null, null), // 4
            Triple(Blocks.SPRUCE_STAIRS, null, null), // 5
            Triple(Blocks.JUNGLE_LOG, null, null), // 6
            Triple(Blocks.JUNGLE_LOG, null, null), // 7
            Triple(Blocks.JUNGLE_LOG, null, null), // 8
            Triple(Blocks.HAY_BLOCK, null, null), // 9
            Triple(Blocks.YELLOW_TERRACOTTA, null, null), // 10
        ),
        StructureType.ODAWA,
        CrystalHollowsQuarter.JUNGLE,
        "Odawa",
        odawaConfig,
        0,
        0,
        0
    ),

    GOLDEN_DRAGON(
        mutableListOf(
            Triple(Blocks.STONE, null, null),
            Triple(Blocks.RED_TERRACOTTA, null, null),
            Triple(Blocks.RED_TERRACOTTA, null, null),
            Triple(Blocks.RED_TERRACOTTA, null, null),
            Triple(Blocks.PLAYER_HEAD, null, null),
            Triple(Blocks.RED_WOOL, null, null),
        ),
        StructureType.GOLDEN_DRAGON,
        CrystalHollowsQuarter.ANY,
        "Golden Dragon",
        goldenDragonConfig,
        0,
        -3,
        5
    ),

    FAIRY_GROTTO(
        mutableListOf(
            Triple(Blocks.MAGENTA_STAINED_GLASS, null, null),
            Triple(Blocks.MAGENTA_STAINED_GLASS_PANE, null, null),
        ),
        StructureType.FAIRY_GROTTO,
        CrystalHollowsQuarter.ANY,
        "Fairy Grotto",
        grottoConfig,
        0,
        0,
        0
    ),

    WORM_FISHING(
        mutableListOf(
            Triple(Blocks.LAVA, null, null),
            Triple(Blocks.AIR, null, null),
        ),
        StructureType.WORM_FISHING,
        CrystalHollowsQuarter.PRECURSOR_REMNANTS,
        "Worm Fishing",
        wormFishingConfig,
        0,
        0,
        0
    )
}
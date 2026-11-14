package mrtjp.core

import mrtjp.core.world.{TGenerationLogic, WorldLib}
import net.minecraft.init.Blocks
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraftforge.common.BiomeDictionary
import java.util.Random
import scala.jdk.CollectionConverters.*

object WorldChecks {
    def run(world:World, entity:net.minecraft.entity.Entity): Unit = {
        val multi = new mrtjp.core.block.MultiTileBlock(net.minecraft.block.material.Material.ROCK)
        for (meta <- 0 until 16) assert(multi.getMetaFromState(multi.getStateFromMeta(meta)) == meta)
        val tilePos = new BlockPos(30, 90, 30)
        val oldBlock = world.getBlockState(tilePos)
        val oldTile = world.getTileEntity(tilePos)
        try {
            world.setBlockState(tilePos, Blocks.AIR.getDefaultState, 2)
            assert(!multi.canPlaceTorchOnTop(multi.getDefaultState, world, tilePos))
            multi.onEntityWalk(world, tilePos, entity)
            class WalkingTile extends mrtjp.core.block.MTBlockTile {
                var walks = 0
                def getBlock = multi
                override def onEntityWalk(entity:net.minecraft.entity.Entity): Unit = { walks += 1 }
            }
            val tile = new WalkingTile
            world.setBlockState(tilePos, Blocks.CHEST.getDefaultState, 2)
            world.setTileEntity(tilePos, tile)
            assert(world.getTileEntity(tilePos) eq tile)
            val state = multi.getStateFromMeta(3)
            assert(multi.getActualState(state, world, tilePos) == state)
            assert(multi.canPlaceTorchOnTop(state, world, tilePos))
            multi.onEntityWalk(world, tilePos, entity)
            assert(tile.walks == 1)
        } finally {
            world.setBlockState(tilePos, oldBlock, 2)
            if (oldTile != null) world.setTileEntity(tilePos, oldTile)
        }

        val pos = new BlockPos(12, 90, 12)
        val logic = new TGenerationLogic {
            def generate_impl(w:World, x:Int, z:Int, random:Random) = false
        }
        logic.biomes = Set(BiomeDictionary.getTypes(world.getBiome(pos)).asScala.toSet)
        assert(!logic.postFiltCheck(world, pos, world.rand), "Matching biome blacklist must reject generation")
        logic.biomeBlacklist = false
        assert(logic.postFiltCheck(world, pos, world.rand), "Matching biome whitelist must allow generation")
        logic.biomes = Set.empty
        assert(!logic.postFiltCheck(world, pos, world.rand))
        logic.biomeBlacklist = true
        assert(logic.postFiltCheck(world, pos, world.rand))

        val flowerPos = new BlockPos(20, 81, 20)
        val ground = flowerPos.down()
        val shade = flowerPos.up(3)
        val oldGround = world.getBlockState(ground)
        val oldFlower = world.getBlockState(flowerPos)
        val oldShade = world.getBlockState(shade)
        val decorator = new mrtjp.core.world.WorldGenDecorator
        decorator.dx = 1; decorator.dy = 1; decorator.dz = 1
        decorator.cluster = Set(((Blocks.YELLOW_FLOWER, 0), 1))
        decorator.material = Set((Blocks.AIR, -1))
        decorator.soil = Set((Blocks.DIRT, -1))
        try {
            world.setBlockState(ground, Blocks.DIRT.getDefaultState, 2)
            world.setBlockState(flowerPos, Blocks.AIR.getDefaultState, 2)
            world.setBlockState(shade, Blocks.AIR.getDefaultState, 2)
            assert(decorator.generate(world, new Random(0), ground), "Default checkStay must permit a valid flower")
            assert(world.getBlockState(flowerPos).getBlock == Blocks.YELLOW_FLOWER)
            world.setBlockState(flowerPos, Blocks.AIR.getDefaultState, 2)
            decorator.underTree = true
            assert(!decorator.generate(world, new Random(0), ground), "underTree must reject open sky")
            world.setBlockState(shade, Blocks.LEAVES.getDefaultState, 2)
            assert(decorator.generate(world, new Random(0), ground), "underTree must allow a canopy")
            world.setBlockState(flowerPos, Blocks.AIR.getDefaultState, 2)
            world.setBlockState(ground, Blocks.STONE.getDefaultState, 2)
            decorator.underTree = false
            decorator.soil = Set((Blocks.STONE, -1))
            assert(!decorator.generate(world, new Random(0), ground), "checkStay must reject unsupported soil")
            decorator.checkStay = false
            assert(decorator.generate(world, new Random(0), ground), "Disabling checkStay must bypass placement rules")
        } finally {
            world.setBlockState(ground, oldGround, 2)
            world.setBlockState(flowerPos, oldFlower, 2)
            world.setBlockState(shade, oldShade, 2)
        }

        val canopy = pos.up(3)
        val highCanopy = new BlockPos(pos.getX, 255, pos.getZ)
        val previous = world.getBlockState(canopy)
        val previousHigh = world.getBlockState(highCanopy)
        try {
            world.setBlockState(canopy, Blocks.LEAVES.getDefaultState, 2)
            assert(WorldLib.isBlockUnderTree(world, pos), "Tree canopy directly above must be found")
            world.setBlockState(canopy, Blocks.AIR.getDefaultState, 2)
            world.setBlockState(highCanopy, Blocks.LEAVES.getDefaultState, 2)
            assert(WorldLib.isBlockUnderTree(world, pos), "Canopy at the build height limit must be found")
        } finally {
            world.setBlockState(canopy, previous, 2)
            world.setBlockState(highCanopy, previousHigh, 2)
        }
    }
}

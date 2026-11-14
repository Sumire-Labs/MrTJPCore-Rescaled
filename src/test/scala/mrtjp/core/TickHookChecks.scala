package mrtjp.core

import mrtjp.core.world.{BlockUpdateHandler, IBlockEventHandler}
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.math.{BlockPos, ChunkPos}
import net.minecraft.world.{World, WorldServer}
import net.minecraftforge.fml.common.gameevent.TickEvent.{Phase, WorldTickEvent}
import net.minecraftforge.fml.relauncher.Side

object TickHookChecks {
    def run(world:WorldServer, player:EntityPlayer): Unit = {
        val players = world.getMinecraftServer.getPlayerList
        val previousDistance = players.getViewDistance
        val previousPosition = (player.posX, player.posY, player.posZ)
        val added = !world.playerEntities.contains(player)
        var farCalls = 0
        var loadedCalls = 0
        BlockUpdateHandler.register(new IBlockEventHandler {
            def onBlockUpdate(w:World, pos:BlockPos, state:IBlockState): Unit = {
                if ((pos.getX >> 4) == 10000 && (pos.getZ >> 4) == 10000) farCalls += 1
                if ((pos.getX >> 4) == 0 && (pos.getZ >> 4) == 0) loadedCalls += 1
            }
        })
        try {
            players.setViewDistance(0)
            if (added) world.playerEntities.add(player)
            player.setPosition(160008, 80, 160008)
            assert(world.getChunkProvider.getLoadedChunk(10000, 10000) == null)
            assert(BlockUpdateHandler.getActiveChunkSet(world).contains(new ChunkPos(10000, 10000)))
            BlockUpdateHandler.onTick(new WorldTickEvent(Side.SERVER, Phase.END, world))
            assert(world.getChunkProvider.getLoadedChunk(10000, 10000) == null,
                "Block tick hooks must not load an unrequested chunk")
            assert(farCalls == 0)
            player.setPosition(8, 80, 8)
            val before = loadedCalls
            BlockUpdateHandler.onTick(new WorldTickEvent(Side.SERVER, Phase.END, world))
            assert(loadedCalls > before, "Loaded chunks must still receive block update callbacks")
        } finally {
            if (added) world.playerEntities.remove(player)
            player.setPosition(previousPosition._1, previousPosition._2, previousPosition._3)
            players.setViewDistance(previousDistance)
        }
    }
}

package mrtjp.core

import mrtjp.core.handler.MrTJPConfig
import mrtjp.core.world.{ISimpleStructureGen, SimpleGenHandler}
import net.minecraft.nbt.{NBTTagCompound, NBTTagList, NBTTagString}
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraft.world.chunk.Chunk
import net.minecraftforge.event.world.{ChunkDataEvent, WorldEvent}
import net.minecraftforge.common.util.Constants
import net.minecraftforge.fml.common.gameevent.TickEvent.{Phase, WorldTickEvent}
import net.minecraftforge.fml.relauncher.Side
import java.util.{Random, UUID}

object RetrogenChecks {
    def run(world:World): Unit = {
        val wasEnabled = MrTJPConfig.retro_gen
        val previousId = MrTJPConfig.retro_gen_id
        val previousTime = world.getWorldTime
        val id = "smoke_"+UUID.randomUUID().toString
        MrTJPConfig.retro_gen = false
        MrTJPConfig.retro_gen_id = id
        val database = "RetrogenData_"+id
        var calls = 0
        var lastRetro = false
        SimpleGenHandler.registerStructure(new ISimpleStructureGen {
            def genID = id
            def generate(w:World, x:Int, z:Int, random:Random, retro:Boolean):Boolean = {
                calls += 1; lastRetro = retro; true
            }
        })
        def load(chunk:Chunk, data:NBTTagCompound = new NBTTagCompound()): Unit =
            SimpleGenHandler.chunkLoadEvent(new ChunkDataEvent.Load(chunk, data))
        def save(chunk:Chunk, db:String = database):Set[String] = {
            val saved = new NBTTagCompound()
            SimpleGenHandler.chunkSaveEvent(new ChunkDataEvent.Save(chunk, saved))
            val entries = saved.getCompoundTag(db).getTagList("StructList", Constants.NBT.TAG_STRING)
            (0 until entries.tagCount()).map(entries.getStringTagAt).toSet
        }
        def tick(): Unit = SimpleGenHandler.tickEnd(new WorldTickEvent(Side.SERVER, Phase.END, world))
        def unload(): Unit = SimpleGenHandler.worldUnloadEvent(new WorldEvent.Unload(world))
        try {
            world.setWorldTime(10)
            val chunk = world.getChunk(0, 0)
            load(chunk)
            assert(!save(chunk).contains(id), "Saving an ungenerated chunk must not mark a new structure as generated")
            MrTJPConfig.retro_gen = true
            load(chunk); load(chunk)
            assert(!save(chunk).contains(id), "Pending retrogen must remain pending in saved data")
            MrTJPConfig.retro_gen = false
            tick(); assert(calls == 0)
            MrTJPConfig.retro_gen = true
            tick(); assert(calls == 1 && lastRetro)
            assert(save(chunk).contains(id))
            tick(); assert(calls == 1, "Repeated load notifications must not duplicate generation")

            MrTJPConfig.retro_gen_id = id+"_changed"
            assert(!save(chunk, "RetrogenData_"+MrTJPConfig.retro_gen_id).contains(id))
            MrTJPConfig.retro_gen_id = id
            val farPos = new BlockPos(160000, 0, 160000)
            assert(!world.isBlockLoaded(farPos))
            load(new Chunk(world, 10000, 10000))
            tick()
            assert(calls == 1 && !world.isBlockLoaded(farPos), "Retrogen must not load an unloaded chunk")

            load(world.getChunk(1, 0))
            unload(); tick()
            assert(calls == 1, "World unload must discard its pending generation")
            val normal = world.getChunk(2, 0)
            MrTJPConfig.retro_gen = false
            SimpleGenHandler.generate(new Random(0), 2, 0, world, null, null)
            assert(calls == 2 && !lastRetro && save(normal).contains(id))

            val malformed = new NBTTagCompound()
            malformed.setInteger(database, 5)
            load(world.getChunk(3, 0), malformed)
            val legacyData = new NBTTagCompound()
            val legacyTag = new NBTTagCompound()
            val legacyList = new NBTTagList()
            legacyList.appendTag(new NBTTagString("legacy"))
            legacyTag.setTag("StructList", legacyList)
            legacyData.setTag(database, legacyTag)
            val legacy = world.getChunk(4, 0)
            load(legacy, legacyData)
            assert(save(legacy) == Set("legacy"), "Saving with retrogen disabled must preserve existing records")

            val failingId = id+"_failure"
            var fail = true
            SimpleGenHandler.registerStructure(new ISimpleStructureGen {
                def genID = failingId
                def generate(w:World, x:Int, z:Int, random:Random, retro:Boolean):Boolean = {
                    if (fail) throw new IllegalStateException("smoke generation failure")
                    true
                }
            })
            val partial = world.getChunk(5, 0)
            try {
                SimpleGenHandler.generate(new Random(0), 5, 0, world, null, null)
                throw new AssertionError("Expected the failing generator to propagate its exception")
            } catch { case e:IllegalStateException => assert(e.getMessage == "smoke generation failure") }
            assert(save(partial).contains(id) && !save(partial).contains(failingId))
            val beforeRetry = calls
            fail = false
            SimpleGenHandler.generate(new Random(0), 5, 0, world, null, null)
            assert(calls == beforeRetry && save(partial).contains(failingId))
        } finally {
            unload()
            world.setWorldTime(previousTime)
            MrTJPConfig.retro_gen = wasEnabled
            MrTJPConfig.retro_gen_id = previousId
        }
    }
}

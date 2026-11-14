/*
 * Copyright (c) 2015.
 * Created by MrTJP.
 * All rights reserved.
 */
package mrtjp.core.world

import java.util.Random

import mrtjp.core.handler.MrTJPConfig
import mrtjp.core.handler.MrTJPCoreMod.log
import net.minecraft.nbt.{NBTTagCompound, NBTTagList, NBTTagString}
import net.minecraft.world.World
import net.minecraft.world.chunk.{Chunk, IChunkProvider}
import net.minecraft.world.gen.IChunkGenerator
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.common.util.Constants
import net.minecraftforge.event.world.{ChunkDataEvent, WorldEvent}
import net.minecraftforge.fml.common.IWorldGenerator
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.{Phase, WorldTickEvent}
import net.minecraftforge.fml.common.registry.GameRegistry
import net.minecraftforge.fml.relauncher.Side

import scala.collection.mutable.LinkedHashMap
import java.util.{Collections, WeakHashMap}

trait ISimpleStructureGen
:
    def genID:String

    def generate(w:World, chunkX:Int, chunkZ:Int, rand:Random, isRetro:Boolean):Boolean

object SimpleGenHandler extends IWorldGenerator
:
    private var structures = Seq[ISimpleStructureGen]()
    private val queueLock = new Object
    private var genQueue = Map[Int, LinkedHashMap[(Int, Int), ChunkCoord]]()
    private val completed = Collections.synchronizedMap(new WeakHashMap[Chunk, GenerationRecord]())

    private def retroGen = MrTJPConfig.retro_gen
    private def tagDB = "RetrogenData_"+MrTJPConfig.retro_gen_id

    def init(): Unit =
        GameRegistry.registerWorldGenerator(this, 0)
        MinecraftForge.EVENT_BUS.register(this)
        MinecraftForge.ORE_GEN_BUS.register(this)

    def registerStructure(struct:ISimpleStructureGen): Unit =
        if structures.exists(_.genID == struct.genID) then
            log.error("MrTJP Structure gen duplicate structure '%s'", struct.genID)
        else structures :+= struct

    private def readStructures(tag:NBTTagCompound):Set[String] =
        val list = tag.getTagList("StructList", Constants.NBT.TAG_STRING)
        (0 until list.tagCount()).map(list.getStringTagAt).toSet

    @SubscribeEvent
    def chunkSaveEvent(event:ChunkDataEvent.Save): Unit =
        val database = tagDB
        val tag = event.getData.getCompoundTag(database)
        val record = Option(completed.get(event.getChunk)).filter(_.database == database)
        val ids = record.map(_.structures).getOrElse(readStructures(tag))
        val list = new NBTTagList
        ids.toSeq.sorted.foreach(id => list.appendTag(new NBTTagString(id)))
        tag.setTag("StructList", list)
        tag.setLong("StructHash", ids.iterator.map(_.hashCode.toLong).sum)
        event.getData.setTag(database, tag)

    @SubscribeEvent
    def chunkLoadEvent(event:ChunkDataEvent.Load): Unit =
        val database = tagDB
        val chunk = event.getChunk
        val ids = readStructures(event.getData.getCompoundTag(database))
        completed.put(chunk, GenerationRecord(database, ids))
        if retroGen && structures.exists(s => !ids.contains(s.genID)) then
            val dim = event.getWorld.provider.getDimension
            queueLock.synchronized {
                val chunks = genQueue.getOrElse(dim, LinkedHashMap.empty[(Int, Int), ChunkCoord])
                chunks.update((chunk.x, chunk.z), ChunkCoord(chunk.x, chunk.z, database, ids))
                genQueue += dim -> chunks
            }

    override def generate(rand:Random, chunkX:Int, chunkZ:Int, world:World, g:IChunkGenerator, p:IChunkProvider): Unit =
        subGenerate(world, chunkX, chunkZ, rand, false, tagDB)

    private def subGenerate(w:World, chunkX:Int, chunkZ:Int, rand:Random, isRetro:Boolean, database:String, existing:Set[String] = Set.empty): Unit =
        val chunk = w.getChunk(chunkX, chunkZ)
        var ids = Option(completed.get(chunk)).filter(_.database == database).map(_.structures).getOrElse(Set.empty) ++ existing
        var generated = false
        for structure <- structures if !ids.contains(structure.genID) do
            generated |= structure.generate(w, chunkX, chunkZ, rand, isRetro)
            ids += structure.genID
            completed.put(chunk, GenerationRecord(database, ids))
        if isRetro && generated then chunk.setModified(true)

    @SubscribeEvent
    def tickEnd(event:WorldTickEvent): Unit =
        if event.side != Side.SERVER || event.phase != Phase.END || !retroGen then return
        val world = event.world
        if world.getWorldTime%10 != 0 then return
        val dim = world.provider.getDimension
        val next = queueLock.synchronized { genQueue.get(dim).flatMap(_.headOption).map(_._2) }
        next.foreach { chunk =>
            if chunk.database == tagDB && world.isBlockLoaded(new net.minecraft.util.math.BlockPos(chunk.chunkX*16, 0, chunk.chunkZ*16)) then
                val worldSeed = world.getSeed
                val rand = new Random(worldSeed)
                val xSeed = rand.nextLong>>2+1L
                val zSeed = rand.nextLong>>2+1L
                rand.setSeed(xSeed*chunk.chunkX+zSeed*chunk.chunkZ^worldSeed)
                log.warn("Starting Retrogeneration on chunk @[DIM "+dim+"]"+chunk.toString)
                subGenerate(world, chunk.chunkX, chunk.chunkZ, rand, true, chunk.database, chunk.structList)
            queueLock.synchronized {
                genQueue.get(dim).foreach { chunks =>
                    val key = (chunk.chunkX, chunk.chunkZ)
                    if chunks.get(key).exists(_ eq chunk) then chunks.remove(key)
                    if chunks.isEmpty then genQueue -= dim
                }
            }
        }

    @SubscribeEvent
    def worldUnloadEvent(event:WorldEvent.Unload): Unit =
        if !event.getWorld.isRemote then queueLock.synchronized {
            genQueue -= event.getWorld.provider.getDimension
        }

    private case class GenerationRecord(database:String, structures:Set[String])
    private case class ChunkCoord(chunkX:Int, chunkZ:Int, database:String, structList:Set[String])
    :
        override def toString = "["+chunkX+", "+chunkZ+"]"

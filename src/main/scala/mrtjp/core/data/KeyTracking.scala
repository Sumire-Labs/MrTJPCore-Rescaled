/*
 * Copyright (c) 2015.
 * Created by MrTJP.
 * All rights reserved.
 */
package mrtjp.core.data

import codechicken.lib.packet.PacketCustom
import mrtjp.core.handler.MrTJPCoreSPH
import net.minecraft.client.Minecraft
import net.minecraft.client.settings.KeyBinding
import net.minecraft.entity.player.EntityPlayer
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.client.registry.ClientRegistry
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent
import net.minecraftforge.fml.relauncher.{Side, SideOnly}

import java.util.UUID
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent
import scala.collection.mutable.{HashMap as MHashMap, Map as MMap}

object KeyTracking
:
    private var idPool = 0
    private val map = MHashMap[Int, MMap[UUID, Boolean]]()

    def updatePlayerKey(id:Int, player:EntityPlayer, state:Boolean): Unit = synchronized {
        if player != null then map.get(id).foreach { states =>
            if state then states(player.getUniqueID) = true else states.remove(player.getUniqueID)
        }
    }

    def registerTracker(tracker:TServerKeyTracker): Unit = synchronized {
        require(idPool < 256, "Key tracker IDs are encoded as one byte")
        tracker.id = idPool
        idPool += 1
        map.getOrElseUpdate(tracker.id,
            MHashMap[UUID, Boolean]().withDefaultValue(false))
    }

    MinecraftForge.EVENT_BUS.register(this)

    def isKeyDown(id:Int, player:EntityPlayer): Boolean = synchronized {
        player != null && map.get(id).exists(_.getOrElse(player.getUniqueID, false))
    }

    @SubscribeEvent
    def onLogout(event:PlayerLoggedOutEvent): Unit = synchronized {
        map.values.foreach(_.remove(event.player.getUniqueID))
    }

    def clearStates(): Unit = synchronized { map.values.foreach(_.clear()) }

trait TServerKeyTracker
:
    var id = -1

    def isKeyDown(p:EntityPlayer) = KeyTracking.isKeyDown(id, p)

    def register(): Unit =
        KeyTracking.registerTracker(this)

trait TClientKeyTracker
{
    private var wasDown = false

    def getTracker:TServerKeyTracker

    def getIsKeyDown:Boolean

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    def tick(event:ClientTickEvent): Unit =
        if event.phase != net.minecraftforge.fml.common.gameevent.TickEvent.Phase.END then return
        val mc = Minecraft.getMinecraft
        if mc.player == null || mc.getConnection == null then
            wasDown = false
            return
        val down = getIsKeyDown
        if down != wasDown then
            wasDown = down
            if Minecraft.getMinecraft.getConnection != null then
                KeyTracking.updatePlayerKey(getTracker.id, Minecraft.getMinecraft.player, down)
                val packet = new PacketCustom(MrTJPCoreSPH.channel, MrTJPCoreSPH.keyBindPacket)
                packet.writeByte(getTracker.id)
                packet.writeBoolean(down)
                packet.sendToServer()

    @SideOnly(Side.CLIENT)
    def register(): Unit =
        MinecraftForge.EVENT_BUS.register(this)
        this match
            case kb:KeyBinding => ClientRegistry.registerKeyBinding(kb)
            case _ =>
}

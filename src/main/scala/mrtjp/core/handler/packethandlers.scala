/*
 * Copyright (c) 2014.
 * Created by MrTJP.
 * All rights reserved.
 */
package mrtjp.core.handler

import codechicken.lib.packet.ICustomPacketHandler.{IClientPacketHandler, IServerPacketHandler}
import codechicken.lib.packet.{ICustomPacketTile, PacketCustom}
import mrtjp.core.block.MTBlockTile
import mrtjp.core.data.KeyTracking
import mrtjp.core.gui.GuiHandler
import mrtjp.core.world.Messenger
import net.minecraft.client.Minecraft
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.network.play.{INetHandlerPlayClient, INetHandlerPlayServer}
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class MrTJPCorePH
:
    val channel = MrTJPCoreMod

    val tilePacket = 1
    val messagePacket = 2
    val guiPacket = 3
    val keyBindPacket = 4

    def handleTilePacket(world:World, packet:PacketCustom, pos:BlockPos): Unit =
        if world == null || !world.isBlockLoaded(pos) then return
        world.getTileEntity(pos) match
            case cpt:ICustomPacketTile => cpt.readFromPacket(packet)
            case _ =>

    def sendTilePacket(world:World, pos:BlockPos, packet:PacketCustom): Unit =
    {

    }

object MrTJPCoreCPH extends MrTJPCorePH with IClientPacketHandler
:
    def handlePacket(packet:PacketCustom, mc:Minecraft, nethandler:INetHandlerPlayClient): Unit =
        val world = mc.world
        packet.getType match
            case this.tilePacket => handleTilePacket(world, packet, packet.readPos())
            case this.messagePacket => Messenger.addMessage(packet.readDouble, packet.readDouble, packet.readDouble, packet.readString)
            case this.guiPacket => GuiHandler.receiveGuiPacket(packet)
            case _ =>

object MrTJPCoreSPH extends MrTJPCorePH with IServerPacketHandler
{
    override def handlePacket(packet:PacketCustom, sender:EntityPlayerMP, nethandler:INetHandlerPlayServer): Unit =
        packet.getType match
            case this.tilePacket =>
                val pos = packet.readPos()
                val world = sender.getEntityWorld
                if world.isBlockLoaded(pos) && sender.getDistanceSq(pos) <= 64.0 then
                    world.getTileEntity(pos) match
                        case tile:MTBlockTile => tile.readFromClientPacket(packet, sender)
                        case _ =>
            case this.keyBindPacket =>
                KeyTracking.updatePlayerKey(packet.readUByte(), sender, packet.readBoolean())
            case _ =>
}

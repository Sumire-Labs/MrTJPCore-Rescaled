package mrtjp.core

import mrtjp.core.gui.{NodeContainer, NodeGui, Slot3, TNode}
import mrtjp.core.inventory.SimpleInventory
import mrtjp.core.vec.{Point, Rect, Size}
import net.minecraft.client.Minecraft
import net.minecraft.init.Items
import net.minecraft.inventory.{ClickType, Slot}
import net.minecraft.item.ItemStack

object GuiInputChecks {
    def run(mc:Minecraft): Unit = {
        val inventory = new SimpleInventory(1, "input", 64)
        inventory.setInventorySlotContents(0, new ItemStack(Items.APPLE, 10))
        val container = new NodeContainer
        container.addSlotToContainer(new Slot3(inventory, 0, 10, 10))
        class Screen extends NodeGui(container, 100, 80) {
            var containerClicks = 0
            override def handleMouseClick(slot:Slot, id:Int, button:Int, clickType:ClickType): Unit = { containerClicks += 1 }
        }
        class Overlay extends TNode {
            var consume = true
            var clicks = 0
            var releases = 0
            var drags = 0
            override def frame = Rect(Point.zeroPoint, Size(100, 80))
            override def mouseClicked_Impl(p:Point, button:Int, consumed:Boolean): Boolean = {
                clicks += 1; consume
            }
            override def mouseReleased_Impl(p:Point, button:Int, consumed:Boolean): Boolean = {
                releases += 1; consume
            }
            override def mouseDragged_Impl(p:Point, button:Int, time:Long, consumed:Boolean): Boolean = {
                drags += 1; consume
            }
        }
        val previousController = mc.playerController
        mc.playerController = new net.minecraft.client.multiplayer.PlayerControllerMP(mc, mc.getConnection)
        try {
            val screen = new Screen
            screen.setWorldAndResolution(mc, 800, 600)
            val overlay = new Overlay
            screen.addChild(overlay)
            val x = screen.position.x+11
            val y = screen.position.y+11
            screen.mouseClicked(x, y, 0)
            screen.mouseClickMove(x, y, 0, 10)
            screen.mouseReleased(x, y, 0)
            assert(overlay.clicks == 1 && overlay.releases == 1 && overlay.drags == 1)
            assert(screen.containerClicks == 0, "Consumed node input must not reach the container")
            overlay.consume = false
            screen.mouseClicked(x, y, 0)
            screen.mouseReleased(x, y, 0)
            assert(screen.containerClicks > 0, "Unconsumed input must still reach the container")
            assert(inventory.getStackInSlot(0).getCount == 10)
        } finally { mc.playerController = previousController }
    }
}

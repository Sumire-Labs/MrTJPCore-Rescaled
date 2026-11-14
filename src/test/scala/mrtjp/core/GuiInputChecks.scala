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
        class Box extends TNode {
            override def frame = Rect(position, Size(10, 20))
        }
        val root = new NodeGui(100, 80)
        root.position = Point(50, 60)
        val parent = new Box
        parent.position = Point(3, 4)
        val child = new Box
        child.position = Point(7, 8)
        root.addChild(parent)
        parent.addChild(child)
        assert(parent.calculateChildrenFrame == Rect(Point(10, 12), Size(10, 20)))
        assert(root.calculateChildrenFrame == Rect(Point(53, 64), Size(17, 28)))
        child.hidden = true
        assert(parent.calculateChildrenFrame == Rect(parent.position, Size.zeroSize))
        parent.hidden = true
        assert(root.calculateChildrenFrame == Rect(root.position, Size.zeroSize))

        class Tab extends mrtjp.core.gui.TabNode(20, 30, 80, 100) {
            override def drawBox(): Unit = {}
            override def drawIcon(): Unit = {}
        }
        val firstTab = new Tab
        val secondTab = new Tab
        assert(firstTab.currentH == 30)
        firstTab.drawBack_Impl(Point.zeroPoint, 0)
        assert(firstTab.size == Size(20, 30))
        val tabs = new mrtjp.core.gui.TabControlNode(100, 50)
        tabs.addChild(firstTab); tabs.addChild(secondTab)
        tabs.frameUpdate_Impl(Point.zeroPoint, 0)
        assert(secondTab.position.y == 30)
        assert(secondTab.mouseClicked_Impl(Point(1, 31), 0, false))
        assert(secondTab.active)
        assert(firstTab.mouseClicked_Impl(Point(1, 1), 0, false))
        assert(firstTab.active && !secondTab.active)
        assert(firstTab.mouseClicked_Impl(Point(1, 1), 0, false))
        assert(!firstTab.active)
        class IconTab extends mrtjp.core.gui.TabNode(20, 30, 80, 100) with mrtjp.core.gui.TIconTab {
            var iconAt = Point.zeroPoint
            override def drawTexturedModalRect(x:Int, y:Int, sprite:net.minecraft.client.renderer.texture.TextureAtlasSprite, w:Int, h:Int): Unit = {
                iconAt = Point(x, y)
            }
        }
        val iconTab = new IconTab
        iconTab.position = Point(7, 11)
        iconTab.drawIcon()
        assert(iconTab.iconAt == Point(10, 14))

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

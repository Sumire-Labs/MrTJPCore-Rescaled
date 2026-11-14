package mrtjp.core

import mrtjp.core.block.MTBlockTile
import mrtjp.core.inventory.SimpleInventory
import mrtjp.core.data.{KeyTracking, TServerKeyTracker}
import codechicken.lib.packet.PacketCustom
import codechicken.lib.data.MCDataInput
import net.minecraft.init.{Blocks, Items}
import net.minecraft.item.ItemStack
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.nbt.NBTTagCompound
import io.netty.buffer.Unpooled

object SmokeChecks {
    def run(player:EntityPlayer): Unit = {
        val configFile = new java.io.File("config/array-smoke.cfg")
        class ArrayConfig extends mrtjp.core.data.ModConfig("array_smoke") {
            protected def initValues(): Unit = {}
            def checkList[T](key:String, defaults:Array[T], expectedType:net.minecraftforge.common.config.Property.Type): Unit = {
                val actual = BaseCategory("lists").put(key, defaults)
                assert(actual.toSeq == defaults.toSeq)
                assert(config.getCategory("lists").get(key).getType == expectedType)
            }
            def check(): Unit = {
                import net.minecraftforge.common.config.Property.Type.*
                checkList("empty_boolean", Array.emptyBooleanArray, BOOLEAN)
                checkList("empty_integer", Array.emptyIntArray, INTEGER)
                checkList("empty_double", Array.emptyDoubleArray, DOUBLE)
                checkList("empty_string", Array.empty[String], STRING)
                checkList("boolean", Array(true, false), BOOLEAN)
                checkList("integer", Array(1, -2), INTEGER)
                checkList("double", Array(1.25, -2.5), DOUBLE)
                checkList("string", Array("one", "two"), STRING)
            }
        }
        val arrayConfig = new ArrayConfig
        arrayConfig.config = new net.minecraftforge.common.config.Configuration(configFile)
        arrayConfig.check()
        arrayConfig.config.save()
        arrayConfig.config = new net.minecraftforge.common.config.Configuration(configFile)
        arrayConfig.config.load()
        arrayConfig.check()
        val savedLists = arrayConfig.config.getCategory("lists")
        assert(savedLists.get("integer").getIntList.toSeq == Seq(1, -2))
        assert(savedLists.get("double").getDoubleList.toSeq == Seq(1.25, -2.5))

        val inventory = new SimpleInventory(2, "smoke", 64)
        assert(inventory.getStackInSlot(-1).isEmpty)
        assert(inventory.getStackInSlot(2).isEmpty)
        inventory.setInventorySlotContents(-1, new ItemStack(Items.APPLE))
        inventory.setInventorySlotContents(2, new ItemStack(Items.APPLE))
        assert(inventory.removeStackFromSlot(2).isEmpty)
        assert(inventory.decrStackSize(-1, 1).isEmpty)
        inventory.setInventorySlotContents(0, new ItemStack(Items.APPLE, 7))
        assert(inventory.decrStackSize(0, 0).isEmpty)
        val tag = new NBTTagCompound()
        inventory.saveInv(tag)
        val restored = new SimpleInventory(2, "smoke", 64)
        restored.loadInv(tag)
        assert(restored.getStackInSlot(0).getCount == 7)
        assert(restored.decrStackSize(0, 3).getCount == 3)
        assert(restored.getStackInSlot(0).getCount == 4)

        val taggedStack = new ItemStack(Items.APPLE)
        val sourceTag = new NBTTagCompound()
        sourceTag.setInteger("revision", 1)
        taggedStack.setTagCompound(sourceTag)
        val snapshot = mrtjp.core.item.ItemKey.get(taggedStack)
        val lookup = Map(snapshot -> "saved")
        sourceTag.setInteger("revision", 2)
        assert(snapshot.tag.getInteger("revision") == 1)
        assert(lookup.get(mrtjp.core.item.ItemKey.get(snapshot.makeStack(1))).contains("saved"))
        val keyCopy = snapshot.copy
        keyCopy.tag.setInteger("revision", 3)
        assert(snapshot.tag.getInteger("revision") == 1)
        val stackCopy = snapshot.makeStack(1)
        stackCopy.getTagCompound.setInteger("revision", 4)
        assert(snapshot.tag.getInteger("revision") == 1)
        assert(mrtjp.core.item.ItemKey.get(new ItemStack(Items.APPLE)).hashCode !=
            mrtjp.core.item.ItemKey.get(new ItemStack(Items.STICK)).hashCode)

        val originalWrappers = mrtjp.core.inventory.InvWrapper.wrappers
        val custom = new mrtjp.core.inventory.VanillaWrapper(inventory, true)
        try {
            mrtjp.core.inventory.InvWrapper.wrappers = Seq(new mrtjp.core.inventory.IInvWrapperRegister {
                def wrapperID = "smoke"
                def matches(inv:net.minecraft.inventory.IInventory) = inv eq inventory
                def create(inv:net.minecraft.inventory.IInventory) = custom
            })
            assert(mrtjp.core.inventory.InvWrapper.wrap(inventory) eq custom)
        } finally { mrtjp.core.inventory.InvWrapper.wrappers = originalWrappers }

        val limited = new SimpleInventory(2, "limited", 64)
        val wrapped = mrtjp.core.inventory.InvWrapper.wrapInternal(limited)
        val sword = mrtjp.core.item.ItemKey.get(new ItemStack(Items.IRON_SWORD))
        assert(wrapped.injectItem(sword, 3) == 2)
        assert(limited.getStackInSlot(0).getCount == 1 && limited.getStackInSlot(1).getCount == 1)
        limited.clear()
        val apple = mrtjp.core.item.ItemKey.get(new ItemStack(Items.APPLE))
        limited.setInventorySlotContents(0, new ItemStack(Items.APPLE, 70))
        assert(wrapped.injectItem(apple, 5) == 5)
        assert(limited.getStackInSlot(0).getCount == 70 && limited.getStackInSlot(1).getCount == 5)
        assert(wrapped.injectItem(apple, -1) == 0)
        assert(limited.getStackInSlot(1).getCount == 5)

        assert(wrapped.getSpaceForItem(apple) == 59) // Overfull slots contribute zero space.
        for (capability <- Seq(false, true); hideSlot <- Seq(false, true); hideType <- Seq(false, true) if !(hideSlot && hideType)) {
            val storage = new SimpleInventory(3, "hidden", 64)
            storage.setInventorySlotContents(0, new ItemStack(Items.STICK, 1))
            storage.setInventorySlotContents(1, new ItemStack(Items.APPLE, 1))
            storage.setInventorySlotContents(2, new ItemStack(Items.APPLE, 1))
            val view: mrtjp.core.inventory.InvWrapper = if (capability)
                new mrtjp.core.inventory.CapWrapper(new net.minecraftforge.items.wrapper.InvWrapper(storage))
            else mrtjp.core.inventory.InvWrapper.wrapInternal(storage)
            view.setHidePerSlot(hideSlot)
            view.setHidePerType(hideType)
            val expected = if (hideSlot) 0 else if (hideType) 1 else 2
            assert(view.getItemCount(apple) == expected)
            assert(view.hasItem(apple) == (expected > 0))
            assert(view.extractItem(apple, 10) == expected)
            assert(view.getItemCount(apple) == 0 && !view.hasItem(apple))
        }

        val container = new mrtjp.core.gui.NodeContainer
        container.putStackInSlot(0, ItemStack.EMPTY)
        container.addSlotToContainer(new mrtjp.core.gui.Slot3(limited, 0, 0, 0))
        container.putStackInSlot(-1, ItemStack.EMPTY)
        container.putStackInSlot(1, ItemStack.EMPTY)
        assert(limited.getStackInSlot(0).getCount == 70)
        container.putStackInSlot(0, new ItemStack(Items.APPLE, 2))
        assert(limited.getStackInSlot(0).getCount == 2)

        val tracker = new TServerKeyTracker {}
        tracker.register()
        KeyTracking.updatePlayerKey(255, player, true) // Unknown IDs are ignored.
        KeyTracking.updatePlayerKey(tracker.id, player, true)
        assert(tracker.isKeyDown(player))
        KeyTracking.updatePlayerKey(tracker.id, player, false)
        assert(!tracker.isKeyDown(player))
        KeyTracking.updatePlayerKey(tracker.id, player, true)
        KeyTracking.onLogout(new net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent(player))
        assert(!tracker.isKeyDown(player))
        KeyTracking.updatePlayerKey(tracker.id, player, true)
        KeyTracking.clearStates()
        assert(!tracker.isKeyDown(player))

        class TestTile extends MTBlockTile {
            var descriptions = 0
            var updates = 0
            var actions = 0
            override def getBlock = Blocks.STONE
            override def readDesc(in:MCDataInput): Unit = { descriptions += 1 }
            override def read(in:MCDataInput, key:Int): Unit = { updates += 1 }
            override def readFromClient(in:MCDataInput, key:Int, p:EntityPlayer): Unit = { actions += key }
        }
        val tile = new TestTile
        def packet(key:Int) = new PacketCustom(Unpooled.buffer().writeByte(1).writeByte(key))
        val description = packet(0)
        val action = packet(7)
        try {
            tile.readFromClientPacket(description, player)
            tile.readFromClientPacket(action, player)
            assert(tile.descriptions == 0 && tile.updates == 0 && tile.actions == 7)
        } finally {
            description.asByteBuf.release()
            action.asByteBuf.release()
        }
    }
}

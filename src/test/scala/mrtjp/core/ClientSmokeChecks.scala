package mrtjp.core
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiMainMenu
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.{ClientTickEvent, Phase}
import java.nio.file.{Files, Path}
import mrtjp.core.gui.SimpleTextboxNode

object ClientSmokeChecks {
    def start(): Unit = MinecraftForge.EVENT_BUS.register(this)
    @SubscribeEvent def tick(event:ClientTickEvent): Unit = {
        val mc = Minecraft.getMinecraft
        if (event.phase != Phase.END || !mc.currentScreen.isInstanceOf[GuiMainMenu]) return
        MinecraftForge.EVENT_BUS.unregister(this)
        try {
            val box = new SimpleTextboxNode(0, 0, 200, 20, "initial")
            assert(box.text == "initial")
            box.setFocused(true)
            assert(box.keyPressed_Impl('x', 45, false) && box.text == "initialx")
            box.keyPressed_Impl('\b', 14, false)
            assert(box.text == "initial")
            var returned = false
            box.textReturnDelegate = () => returned = true
            box.keyPressed_Impl('\r', 28, false)
            assert(returned && !box.focused)
            assert(!box.keyPressed_Impl('x', 45, false))
            Files.writeString(Path.of(System.getProperty("mrtjpcore.client.result")), "PASS\n")
            println("MRTJPCORE_CLIENT_PASS: main menu loaded, textbox initialization and keyboard input")
        } finally { mc.shutdown() }
    }
}

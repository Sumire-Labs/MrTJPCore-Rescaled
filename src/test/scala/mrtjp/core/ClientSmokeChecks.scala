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
            val serverTracker = new mrtjp.core.data.TServerKeyTracker {}
            serverTracker.register()
            val clientTracker = new mrtjp.core.data.TClientKeyTracker {
                def getTracker = serverTracker
                def getIsKeyDown = false
            }
            val connection = new net.minecraft.client.network.NetHandlerPlayClient(mc, null,
                new net.minecraft.network.NetworkManager(net.minecraft.network.EnumPacketDirection.CLIENTBOUND),
                new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(), "SmokePlayer"))
            val world = new net.minecraft.client.multiplayer.WorldClient(connection,
                new net.minecraft.world.WorldSettings(0L, net.minecraft.world.GameType.SURVIVAL, false, false, net.minecraft.world.WorldType.FLAT),
                0, net.minecraft.world.EnumDifficulty.NORMAL, mc.profiler)
            val player = new net.minecraft.client.entity.EntityPlayerSP(mc, world, connection,
                new net.minecraft.stats.StatisticsManager(), new net.minecraft.stats.RecipeBook())
            val previousPlayer = mc.player
            try {
                mc.player = player
                mrtjp.core.gui.ClipChecks.run(mc)
                GuiInputChecks.run(mc)
                ParticleChecks.run(world)
                val resized = new mrtjp.core.gui.NodeGui(100, 80)
                resized.size = mrtjp.core.vec.Size(200, 120)
                resized.setWorldAndResolution(mc, 800, 600)
                assert(resized.position == mrtjp.core.vec.Point(300, 240))
                resized.size = mrtjp.core.vec.Size(240, 160)
                resized.setWorldAndResolution(mc, 800, 600)
                assert(resized.position == mrtjp.core.vec.Point(280, 220))
                val legacy = new mrtjp.core.gui.NodeGui(100, 80)
                legacy.setWorldAndResolution(mc, 800, 600)
                assert(legacy.size == mrtjp.core.vec.Size(100, 80))
                assert(legacy.position == mrtjp.core.vec.Point(350, 260))

                clientTracker.tick(new ClientTickEvent(Phase.END))
                mrtjp.core.data.KeyTracking.updatePlayerKey(serverTracker.id, player, true)
                assert(serverTracker.isKeyDown(player))
                mc.player = null
                clientTracker.tick(new ClientTickEvent(Phase.END))
                assert(!serverTracker.isKeyDown(player), "Disconnect must clear the tracked player's key state")
                mc.player = player
                clientTracker.tick(new ClientTickEvent(Phase.END))
                assert(!serverTracker.isKeyDown(player), "Reconnect must not restore stale key state")
            } finally {
                mc.player = previousPlayer
                clientTracker.tick(new ClientTickEvent(Phase.END))
            }
            Files.writeString(Path.of(System.getProperty("mrtjpcore.client.result")), "PASS\n")
            println("MRTJPCORE_CLIENT_PASS: main menu loaded, textbox input and key disconnect cleanup")
        } finally { mc.shutdown() }
    }
}

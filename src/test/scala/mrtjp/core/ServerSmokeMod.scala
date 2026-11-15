package mrtjp.core

import net.minecraftforge.fml.common.{FMLCommonHandler, Mod}
import net.minecraftforge.fml.common.event.FMLServerStartedEvent
import net.minecraftforge.common.util.FakePlayerFactory
import com.mojang.authlib.GameProfile
import java.util.UUID
import java.nio.file.{Files, Path}

@Mod(modid = "mrtjpcore_smoke", name = "MrTJPCore regression checks", version = "1", dependencies = "required-after:mrtjpcore")
class ServerSmokeMod {
    @Mod.EventHandler def loaded(event:net.minecraftforge.fml.common.event.FMLLoadCompleteEvent): Unit = {
        if (System.getProperty("mrtjpcore.client.result") != null)
            Class.forName("mrtjp.core.ClientSmokeChecks").getMethod("start").invoke(null)
    }
    @Mod.EventHandler def started(event:FMLServerStartedEvent): Unit = {
        val result = System.getProperty("mrtjpcore.smoke.result")
        if (result == null) return
        val server = FMLCommonHandler.instance().getMinecraftServerInstance
        try {
            RuntimeChecks.verify()
            val player = FakePlayerFactory.get(server.getWorld(0), new GameProfile(UUID.randomUUID(), "SmokePlayer"))
            SmokeChecks.run(player)
            WorldChecks.run(server.getWorld(0), player)
            RetrogenChecks.run(server.getWorld(0))
            TickHookChecks.run(server.getWorld(0), player)
            Files.writeString(Path.of(result), "PASS\n")
            println("MRTJPCORE_SMOKE_PASS: inventory bounds/NBT, key state cleanup, C2S description rejection")
        } finally {
            server.initiateShutdown()
        }
    }
}

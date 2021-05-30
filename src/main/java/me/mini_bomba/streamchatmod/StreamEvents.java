package me.mini_bomba.streamchatmod;

import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class StreamEvents {

    private final StreamChatMod mod;
    private final Logger LOGGER = LogManager.getLogger();

    public StreamEvents(StreamChatMod mod) {
        this.mod = mod;
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event){
        if (mod.loginMessageTimer == 0) {
            mod.printTwitchStatus(true);
        }
        if (mod.loginMessageTimer >= 0) mod.loginMessageTimer--;
        if (mod.httpServer != null && mod.httpShutdownTimer > -1) {
            if (mod.httpShutdownTimer == 0) {
                mod.httpServer.stop(0);
                mod.httpServer = null;
                LOGGER.warn("HTTP server shut down due to inactivity.");
                StreamUtils.addMessage(EnumChatFormatting.RED+"Timeout waiting for Twitch token generation. Token can still be manually set using /twitch settoken <token>");
            }
            --mod.httpShutdownTimer;
        }
        if (mod.eventSoundTimer > -1) {
            int timer = mod.eventSoundTimer;
            if (timer == 0) {
                StreamUtils.playSound("note.harp", (float) mod.config.eventSoundVolume.getDouble(), 1.0f);
            } else if (timer == 5) {
                StreamUtils.playSound("note.harp", (float) mod.config.eventSoundVolume.getDouble(), 1.25f);
            } else if (timer == 10) {
                StreamUtils.playSound("note.harp", (float) mod.config.eventSoundVolume.getDouble(), 1.5f);
            }
            mod.eventSoundTimer++;
            if (mod.eventSoundTimer >= 11) mod.eventSoundTimer = -1;
        }
    }

    @SubscribeEvent
    public void onEnterWorld(FMLNetworkEvent.ClientConnectedToServerEvent event) {
        mod.loginMessageTimer = 60;
    }
}

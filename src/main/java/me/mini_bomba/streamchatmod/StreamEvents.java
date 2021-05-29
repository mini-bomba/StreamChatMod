package me.mini_bomba.streamchatmod;

import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
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
        if (mod.httpServer != null && mod.httpShutdownTimer > -1) {
            if (mod.httpShutdownTimer == 0) {
                mod.httpServer.stop(0);
                mod.httpServer = null;
                LOGGER.warn("HTTP server shut down due to inactivity.");
                StreamUtils.addMessage(ChatFormatting.RED+"Timeout waiting for Twitch token generation. Token can still be manually set using /twitch settoken <token>");
            }
            --mod.httpShutdownTimer;
        }
        if (mod.eventSoundTimer > -1) {
            int timer = mod.eventSoundTimer;
            if (timer == 0) {
                StreamUtils.playSound("note.harp", 0.25f, 1.0f);
            } else if (timer == 5) {
                StreamUtils.playSound("note.harp", 0.25f, 1.25f);
            } else if (timer == 10) {
                StreamUtils.playSound("note.harp", 0.25f, 1.5f);
            }
            mod.eventSoundTimer++;
            if (mod.eventSoundTimer >= 11) mod.eventSoundTimer = -1;
        }
    }

}

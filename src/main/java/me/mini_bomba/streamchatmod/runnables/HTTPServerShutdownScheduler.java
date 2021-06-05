package me.mini_bomba.streamchatmod.runnables;

import me.mini_bomba.streamchatmod.StreamChatMod;
import me.mini_bomba.streamchatmod.StreamUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.util.EnumChatFormatting;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class HTTPServerShutdownScheduler implements Runnable {

    private final StreamChatMod mod;
    private static final Logger LOGGER = LogManager.getLogger();

    public HTTPServerShutdownScheduler(StreamChatMod mod) {
        this.mod = mod;
    }

    @Override
    public void run() {
        try { Thread.sleep(120000); } catch (InterruptedException ignored) { }
        if (mod.httpServer == null) return;
        mod.httpServer.stop(0);
        mod.httpServer = null;
        LOGGER.warn("HTTP server shut down due to inactivity.");
        Minecraft.getMinecraft().addScheduledTask(() -> StreamUtils.addMessage(EnumChatFormatting.RED+"Timeout waiting for Twitch token generation. Token can still be manually set using /twitch settoken <token>"));
        mod.httpShutdownScheduler = null;
    }
}

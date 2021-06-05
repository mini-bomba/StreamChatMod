package me.mini_bomba.streamchatmod;

import me.mini_bomba.streamchatmod.tweaker.TransformerField;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.*;
import java.lang.reflect.Field;

public class StreamEvents {

    private final StreamChatMod mod;
    private final Logger LOGGER = LogManager.getLogger();
    private static final int PURPLE = new Color(170, 0, 170).getRGB();
    private static final int BACKGROUND = new Color(0, 0, 0, 127).getRGB();
    private Field GuiChat_inputField = null;

    public StreamEvents(StreamChatMod mod) {
        this.mod = mod;
        try {
            GuiChat_inputField = GuiChat.class.getDeclaredField(TransformerField.GuiChat_inputField.getReflectorName());
            GuiChat_inputField.setAccessible(true);
        } catch (Exception e) {
            LOGGER.error("Failed to reflect GuiChat.inputField");
            e.printStackTrace();
        }
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

    @SubscribeEvent
    public void onGuiChatRender(GuiScreenEvent.DrawScreenEvent.Post event) {
        if (!(event.gui instanceof GuiChat) || mod.twitch == null || !mod.config.twitchMessageRedirectEnabled.getBoolean()
                || mod.config.twitchSelectedChannel.getString().length() == 0) return;
        GuiChat gui = (GuiChat) event.gui;
        try {
            if (GuiChat_inputField != null && ((GuiTextField) GuiChat_inputField.get(gui)).getText().startsWith("/")) return;
        } catch (Exception ignored) {}
        GuiScreen.drawRect(1, gui.height - 15, gui.width - 1, gui.height - 14, PURPLE);
        GuiScreen.drawRect(1, gui.height - 2, gui.width - 1, gui.height - 1, PURPLE);
        GuiScreen.drawRect(1, gui.height - 15, 2, gui.height - 1, PURPLE);
        GuiScreen.drawRect(gui.width - 2, gui.height - 15, gui.width - 1, gui.height - 1, PURPLE);
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRendererObj;
        String warning = EnumChatFormatting.LIGHT_PURPLE + "Twitch chat mode enabled - Messages forwarded to " + EnumChatFormatting.AQUA + mod.config.twitchSelectedChannel.getString() + EnumChatFormatting.LIGHT_PURPLE + "'s chat " + EnumChatFormatting.GRAY + "(/twitch mode)";
        GuiScreen.drawRect(1, gui.height - 26, 3+fontRenderer.getStringWidth(warning), gui.height - 15, BACKGROUND);
        fontRenderer.drawStringWithShadow(warning, 2, gui.height - 25, PURPLE);

    }
}

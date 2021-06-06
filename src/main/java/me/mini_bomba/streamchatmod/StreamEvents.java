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
        String text = "";
        try {
            if (GuiChat_inputField != null) text = ((GuiTextField) GuiChat_inputField.get(gui)).getText();
        } catch (Exception ignored) {}
        if (!text.startsWith("/")) {
            drawChatOutline(gui, PURPLE);
            String warning = EnumChatFormatting.LIGHT_PURPLE + "Twitch chat mode enabled - Messages forwarded to " + EnumChatFormatting.AQUA + mod.config.twitchSelectedChannel.getString() + EnumChatFormatting.LIGHT_PURPLE + "'s chat " + EnumChatFormatting.GRAY + "(/twitch mode)";
            drawTextWithBackground(1, gui.height - 26, warning, BACKGROUND, PURPLE);
        } else if (text.startsWith("/tc") || text.startsWith("/twitchchat")) {
            drawChatOutline(gui, PURPLE);
            String warning = EnumChatFormatting.LIGHT_PURPLE + "Sending message to " + EnumChatFormatting.AQUA + mod.config.twitchSelectedChannel.getString() + EnumChatFormatting.LIGHT_PURPLE + "'s chat";
            drawTextWithBackground(1, gui.height - 26, warning, BACKGROUND, PURPLE);
        }
    }

    private void drawChatOutline(GuiChat gui, int color) {
        GuiScreen.drawRect(1, gui.height - 15, gui.width - 1, gui.height - 14, color);
        GuiScreen.drawRect(1, gui.height - 2, gui.width - 1, gui.height - 1, color);
        GuiScreen.drawRect(1, gui.height - 15, 2, gui.height - 1, color);
        GuiScreen.drawRect(gui.width - 2, gui.height - 15, gui.width - 1, gui.height - 1, color);
    }

    private void drawTextWithBackground(int x, int y, String text, int backgroundColor, int textColor) {
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRendererObj;
        GuiScreen.drawRect(x, y, x+fontRenderer.getStringWidth(text)+2, y+11, backgroundColor);
        fontRenderer.drawStringWithShadow(text, x+1, y+1, textColor);
    }
}

package me.mini_bomba.streamchatmod.asm.hooks;

import me.mini_bomba.streamchatmod.utils.StreamEmote;
import net.minecraft.client.Minecraft;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;

@SuppressWarnings("unused")
public class FontRendererHook {
    private static final Logger LOGGER = LogManager.getLogger();
    private static boolean allowAnimated = true;

    public static float renderEmote(char c1, char c2, float posX, float posY) {
        int emoteId = ((c1 - 0xDBC0) << 10) + c2 & 1023;
        StreamEmote emote = StreamEmote.getEmote(emoteId);
        if (emote == null) return 0.0F;
        Minecraft.getMinecraft().renderEngine.bindTexture(emote.getCurrentFrame(allowAnimated));
        float renderedWidth = emote.width / (emote.height / 9.0F);
        GL11.glBegin(GL11.GL_TRIANGLE_STRIP);
        GL11.glTexCoord2f(0, 0);
        GL11.glVertex3f(posX - 0.5F, posY - 0.5F, 0.0F);
        GL11.glTexCoord2f(0, 1);
        GL11.glVertex3f(posX - 0.5F, posY + 8.49F, 0.0F);
        GL11.glTexCoord2f(1, 0);
        GL11.glVertex3f(posX + renderedWidth - 0.5F, posY - 0.5F, 0.0F);
        GL11.glTexCoord2f(1, 1);
        GL11.glVertex3f(posX + renderedWidth - 0.5F, posY + 8.49F, 0.0F);
        GL11.glEnd();
        return renderedWidth;
    }

    public static int getEmoteWidth(char c1, char c2) {
        int emoteId = ((c1 - 0xDBC0) << 10) + c2 & 1023;
        StreamEmote emote = StreamEmote.getEmote(emoteId);
        if (emote == null) return 0;
        return (int) (emote.width / (emote.height / 9.0F));
    }

    public static void setAllowAnimated(boolean allow) {
        allowAnimated = allow;
    }

}

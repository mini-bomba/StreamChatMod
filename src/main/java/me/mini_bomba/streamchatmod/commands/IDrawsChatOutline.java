package me.mini_bomba.streamchatmod.commands;

import me.mini_bomba.streamchatmod.StreamEvents;
import net.minecraft.client.gui.GuiChat;

public interface IDrawsChatOutline {

    /**
     * Draws a chat outline.<br>
     * Called by {@link StreamEvents#onGuiChatRender} when chat text matches this subcommand
     *
     * @param gui current GuiChat
     * @param args array of arguments to the subcommand. Remember that the command will most likely not be completed!
     */
    void drawChatOutline(GuiChat gui, String[] args);

}

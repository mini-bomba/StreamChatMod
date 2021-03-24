package me.mini_bomba.streamchatmod;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;

public class StreamUtils {

    public static void addMessage(ICommandSender player, String message) {
        player.addChatMessage(new ChatComponentText(message));
    }

    public static void addMessage(EntityPlayerSP player, String message) {
        player.addChatMessage(new ChatComponentText(message));
    }

    public static void addMessages(ICommandSender player, String[] messages) {
        for (String msg : messages) addMessage(player, msg);
    }

    public static void addMessages(EntityPlayerSP player, String[] messages) {
        for (String msg : messages) addMessage(player, msg);
    }

}

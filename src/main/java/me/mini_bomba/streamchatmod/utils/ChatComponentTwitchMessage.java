package me.mini_bomba.streamchatmod.utils;

import net.minecraft.util.ChatComponentText;

public class ChatComponentTwitchMessage extends ChatComponentText {
    public final String messageId;
    public final String channelId;
    public final String userId;

    public ChatComponentTwitchMessage(String messageId, String channelId, String userId, String msg) {
        super(msg);
        this.messageId = messageId;
        this.channelId = channelId;
        this.userId = userId;
    }
}

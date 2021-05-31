package me.mini_bomba.streamchatmod.asm.hooks;

import me.mini_bomba.streamchatmod.StreamChatMod;

public class GuiScreenHook {

    private static StreamChatMod mod = null;

    public static void setMod(StreamChatMod newMod) {
        if (mod != null) throw new RuntimeException("Mod is already set!");
        mod = newMod;
    }

    public static boolean redirectMessage(String msg) {
        if (msg.startsWith("/")) return false;
        if (mod == null || mod.twitch == null || !mod.config.twitchEnabled.getBoolean() || mod.twitch.getChat() == null ||
            !mod.config.twitchMessageRedirectEnabled.getBoolean() || mod.config.twitchSelectedChannel.getString().length() == 0) return false;
        mod.twitch.getChat().sendMessage(mod.config.twitchSelectedChannel.getString(), msg);
        return true;
    }

}

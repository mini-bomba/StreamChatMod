package me.mini_bomba.streamchatmod.runnables;

import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import com.github.twitch4j.common.enums.CommandPermission;
import me.mini_bomba.streamchatmod.StreamChatMod;
import me.mini_bomba.streamchatmod.StreamUtils;
import net.minecraft.event.ClickEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;

import java.util.Set;

public class TwitchMessageHandler implements Runnable {
    private final ChannelMessageEvent event;
    private final StreamChatMod mod;
    private static final char formatChar = '\u00a7';
    private static final String validFormats = "0123456789abcdefklmnorABCDEFKLMNOR";

    public TwitchMessageHandler(StreamChatMod mod, ChannelMessageEvent event) {
        this.mod = mod;
        this.event = event;
    }

    @Override
    public void run() {
        boolean showChannel = mod.config.forceShowChannelName.getBoolean() ||(mod.twitch != null && mod.twitch.getChat().getChannels().size() > 1);
        Set<CommandPermission> perms = event.getPermissions();
        String prefix = perms.contains(CommandPermission.BROADCASTER) ? EnumChatFormatting.RED + " STREAMER " :
                      ( perms.contains(CommandPermission.TWITCHSTAFF) ? EnumChatFormatting.BLACK + " STAFF " :
                      ( perms.contains(CommandPermission.MODERATOR) ? EnumChatFormatting.GREEN + " MOD " :
                      ( perms.contains(CommandPermission.VIP) ? EnumChatFormatting.DARK_PURPLE + " VIP " :
                      ( perms.contains(CommandPermission.SUBSCRIBER) ? EnumChatFormatting.GOLD + " SUB " : " "))));
        String message = event.getMessage();
        message = message.replace(formatChar, '&');
        if (mod.config.allowFormatting.getBoolean()) {
            char[] msg = message.toCharArray();
            for (int i = 0; i < msg.length; i++) {
                if (msg[i] == '&') {
                    if (validFormats.contains(String.valueOf(msg[i+1])))
                        msg[i] = formatChar;
                }
            }
            message = String.valueOf(msg);
        }
        IChatComponent component = new ChatComponentText(EnumChatFormatting.DARK_PURPLE+"[TWITCH"+(showChannel ? "/"+event.getChannel().getName() : "")+"]"+ prefix + EnumChatFormatting.WHITE + event.getUser().getName() + EnumChatFormatting.GRAY+" >> "+message);
        ChatStyle style = new ChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/twitch delete " + event.getChannel().getName() + " " + event.getMessageEvent().getMessageId().orElse("")));
        component.setChatStyle(style);
        StreamUtils.addMessage(component);
        if (mod.config.playSoundOnMessage.getBoolean()) StreamUtils.playSound("note.pling", (float) mod.config.messageSoundVolume.getDouble(), 1.25f);
    }
}

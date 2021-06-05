package me.mini_bomba.streamchatmod.runnables;

import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import me.mini_bomba.streamchatmod.StreamChatMod;
import me.mini_bomba.streamchatmod.StreamUtils;
import net.minecraft.event.ClickEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;

public class TwitchMessageHandler implements Runnable {
    private final ChannelMessageEvent event;
    private final StreamChatMod mod;

    public TwitchMessageHandler(StreamChatMod mod, ChannelMessageEvent event) {
        this.mod = mod;
        this.event = event;
    }

    @Override
    public void run() {
        boolean showChannel = mod.config.forceShowChannelName.getBoolean() ||(mod.twitch != null && mod.twitch.getChat().getChannels().size() > 1);
        IChatComponent component = new ChatComponentText(EnumChatFormatting.DARK_PURPLE+"[TWITCH"+(showChannel ? "/"+event.getChannel().getName() : "")+"]"+EnumChatFormatting.WHITE+" <"+event.getUser().getName()+"> "+event.getMessage());
        ChatStyle style = new ChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/twitch delete " + event.getChannel().getName() + " " + event.getMessageEvent().getMessageId().orElse("")));
        component.setChatStyle(style);
        StreamUtils.addMessage(component);
        if (mod.config.playSoundOnMessage.getBoolean()) StreamUtils.playSound("note.pling", (float) mod.config.messageSoundVolume.getDouble(), 1.25f);
    }
}

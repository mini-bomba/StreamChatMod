package me.mini_bomba.streamchatmod.utils;

import com.github.twitch4j.helix.domain.User;
import me.mini_bomba.streamchatmod.StreamChatMod;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.*;
import org.jetbrains.annotations.Nullable;

public class ChatComponentStreamEmote extends ChatComponentStyle {
    public final StreamEmote emote;
    private final StreamChatMod mod;

    public ChatComponentStreamEmote(StreamChatMod mod, StreamEmote emote, boolean useDefaultStyle) {
        super();
        this.mod = mod;
        this.emote = emote;
        if (useDefaultStyle) setChatStyle(getDefaultStyle());
    }

    public ChatComponentStreamEmote(StreamChatMod mod, StreamEmote emote) {
        this(mod, emote, true);
    }

    @Override
    public String getUnformattedTextForChat() {
        return shouldRender() ? String.valueOf(emote.getCharacter()) : emote.name;
    }

    public String getEmoteName() {
        return emote.name;
    }

    public ChatStyle getDefaultStyle() {
        ChatStyle style = new ChatStyle();
        style.setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, getHoverComponent()));
        String emoteLink = getEmoteLink();
        if (emoteLink != null) style.setChatClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, emoteLink));
        return style;
    }

    public IChatComponent getHoverComponent() {
        String text = EnumChatFormatting.YELLOW + (emote.animated ? "Animated " : "") + emote.type.description + "\n" +
                EnumChatFormatting.GRAY + "Name: " + EnumChatFormatting.AQUA + emote.name;
        if (emote.type == StreamEmote.Type.TWITCH_CHANNEL && emote instanceof TwitchEmote) {
            User owner = mod.getTwitchUserById(((TwitchEmote) emote).emote.getOwnerId());
            text += "\n" + EnumChatFormatting.GRAY + "Channel: ";
            if (owner != null)
                text += EnumChatFormatting.AQUA + owner.getDisplayName();
            else
                text += EnumChatFormatting.ITALIC + "unknown";
        } else if (emote instanceof FFZStreamEmote && ((FFZStreamEmote) emote).emote.owner != null) {
            FFZEmote ffzEmote = ((FFZStreamEmote) emote).emote;
            text += "\n" + EnumChatFormatting.GRAY + "Channel: " + EnumChatFormatting.AQUA + (ffzEmote.owner.displayName == null ? ffzEmote.owner.name : ffzEmote.owner.displayName);
        } else if (emote instanceof BTTVStreamEmote) {
            BTTVApi.EmoteOwner user = ((BTTVStreamEmote) emote).emote.getUser();
            if (user != null)
                text += "\n" + EnumChatFormatting.GRAY + "Channel: " + EnumChatFormatting.AQUA + user.displayName;
        } else if (emote instanceof TwitchChannelBadge)
            text += "\n" + EnumChatFormatting.GRAY + "Channel: " + EnumChatFormatting.AQUA + ((TwitchChannelBadge) emote).channelName;
        return new ChatComponentText(text);
    }

    @Nullable
    public String getEmoteLink() {
        if (emote instanceof TwitchEmote)
            return ((TwitchEmote) emote).emote.getImages().getLargeImageUrl();
        if (emote instanceof BTTVStreamEmote)
            return ((BTTVStreamEmote) emote).emote.getLargeEmoteURL();
        if (emote instanceof FFZStreamEmote)
            return ((FFZStreamEmote) emote).emote.getLargeEmoteURL();
        if (emote instanceof TwitchBadge)
            return ((TwitchBadge) emote).badge.getLargeImageUrl();
        return null;
    }

    public boolean shouldRender() {
        return emote.type.isEnabled(mod.config);
    }

    @Override
    public ChatComponentStreamEmote createCopy() {
        ChatComponentStreamEmote component = new ChatComponentStreamEmote(mod, emote);
        component.setChatStyle(this.getChatStyle().createShallowCopy());

        for (IChatComponent ichatcomponent : this.getSiblings()) {
            component.appendSibling(ichatcomponent.createCopy());
        }

        return component;
    }
}

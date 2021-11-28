package me.mini_bomba.streamchatmod.runnables;

import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import com.github.twitch4j.common.enums.CommandPermission;
import com.github.twitch4j.helix.domain.Clip;
import com.github.twitch4j.helix.domain.Game;
import com.github.twitch4j.helix.domain.User;
import me.mini_bomba.streamchatmod.StreamChatMod;
import me.mini_bomba.streamchatmod.StreamUtils;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TwitchMessageHandler implements Runnable {
    private final ChannelMessageEvent event;
    private final StreamChatMod mod;
    private static final Logger LOGGER = LogManager.getLogger();
    private static final char formatChar = '\u00a7';
    private static final String validFormats = "0123456789abcdefklmnorABCDEFKLMNOR";
    public static final Pattern urlPattern = Pattern.compile("https?://[^.\\s/]+(?:\\.[^.\\s/]+)+\\S*");
    private static final String clipsDomain = "https://clips.twitch.tv/";

    public TwitchMessageHandler(StreamChatMod mod, ChannelMessageEvent event) {
        this.mod = mod;
        this.event = event;
    }

    private String processColorCodes(String message, boolean allowFormatting) {
        message = message.replace(formatChar, '&');
        if (allowFormatting) {
            char[] msg = message.toCharArray();
            for (int i = 0; i < msg.length; i++) {
                if (msg[i] == '&') {
                    if (validFormats.contains(String.valueOf(msg[i+1])))
                        msg[i] = formatChar;
                }
            }
            message = String.valueOf(msg);
        }
        return message;
    }

    @Override
    public void run() {
        boolean showChannel = mod.config.forceShowChannelName.getBoolean() ||(mod.twitch != null && mod.twitch.getChat().getChannels().size() > 1);
        Set<CommandPermission> perms = event.getPermissions();
        String prefix = perms.contains(CommandPermission.BROADCASTER) ? EnumChatFormatting.RED + " STREAMER " :
                      ( perms.contains(CommandPermission.TWITCHSTAFF) ? EnumChatFormatting.BLACK + " STAFF " :
                      ( perms.contains(CommandPermission.MODERATOR) ? EnumChatFormatting.GREEN + " MOD " :
                      ( perms.contains(CommandPermission.VIP) ? EnumChatFormatting.LIGHT_PURPLE + " VIP " :
                      ( perms.contains(CommandPermission.SUBSCRIBER) ? EnumChatFormatting.GOLD + " SUB " : " "))));
        boolean allowFormatting = mod.config.allowFormatting.getBoolean() && (prefix.length() > 1 || !mod.config.subOnlyFormatting.getBoolean());
        String message = event.getMessage();
        Matcher matcher = urlPattern.matcher(message);
        List<ClipComponentMapping> clips = new ArrayList<>();
        IChatComponent component = new ChatComponentText(EnumChatFormatting.DARK_PURPLE+"[TWITCH"+(showChannel ? "/"+event.getChannel().getName() : "")+"]"+ prefix + EnumChatFormatting.WHITE + event.getUser().getName() + EnumChatFormatting.GRAY+" >> ");
        int lastEnd = 0;
        while (matcher.find()) {
            if (matcher.start() > lastEnd)
                component.appendSibling(new ChatComponentText(processColorCodes(message.substring(lastEnd, matcher.start()), allowFormatting)));
            String url = matcher.group();
            // Check if URL is a clip
            ChatComponentText comp;
            if (url.startsWith(clipsDomain) && url.length() > clipsDomain.length()) {
                String clipId = url.substring(clipsDomain.length());
                if (mod.clipCache.contains(clipId))
                    comp = twitchClipComponent(null, mod.clipCache.get(clipId), url);
                else {
                    comp = new ChatComponentText("Clip: Loading clip...");
                    ChatStyle style = new ChatStyle()
                            .setColor(EnumChatFormatting.YELLOW)
                            .setItalic(true)
                            .setChatClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url));
                    comp.setChatStyle(style);
                    clips.add(new ClipComponentMapping(comp, clipId, url));
                }
            } else {
                comp = new ChatComponentText(url);
                ChatStyle style = new ChatStyle()
                        .setColor(EnumChatFormatting.BLUE)
                        .setUnderlined(true)
                        .setChatClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url));
                comp.setChatStyle(style);
            }
            component.appendSibling(comp);
            lastEnd = matcher.end();
        }
        if (message.length() > lastEnd)
            component.appendSibling(new ChatComponentText(processColorCodes(message.substring(lastEnd), allowFormatting)));
        ChatStyle style = new ChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/twitch delete " + event.getChannel().getName() + " " + event.getMessageEvent().getMessageId().orElse("")));
        component.setChatStyle(style);
        StreamUtils.addMessage(component);
        if (mod.config.playSoundOnMessage.getBoolean()) StreamUtils.playSound("note.pling", (float) mod.config.messageSoundVolume.getDouble(), 1.25f);
        if (!clips.isEmpty())
            new Thread(new ClipLookupTask(clips)).start();
    }

    private static class ClipComponentMapping {
        public final ChatComponentText component;
        public final String clipId;
        public final String clipUrl;

        public ClipComponentMapping(ChatComponentText component, String clipId, String clipUrl) {
            this.component = component;
            this.clipId = clipId;
            this.clipUrl = clipUrl;
        }

    }

    private class ClipLookupTask implements Runnable {

        private final List<ClipComponentMapping> clips;

        private ClipLookupTask(List<ClipComponentMapping> clips) {
            this.clips = clips;
        }

        @Override
        public void run() {
            for (ClipComponentMapping clip : clips) {
                twitchClipComponent(clip.component, mod.getTwitchClip(clip.clipId), clip.clipUrl);
                StreamUtils.queueRefreshChat();
            }
        }
    }

    private ChatComponentText twitchClipComponent(ChatComponentText component, Clip clip, String clipUrl) {
        if (clip == null) {
            if (component == null)
                component = new ChatComponentText("Clip: Unknown clip");
            else
                StreamUtils.editTextComponent(component, "Clip: Unknown clip");
            ChatStyle style = new ChatStyle()
                    .setColor(EnumChatFormatting.RED)
                    .setItalic(true)
                    .setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText(EnumChatFormatting.RED+"This clip failed to resolve.\n"+EnumChatFormatting.RED+"You may still click this to open the link.")))
                    .setChatClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, clipUrl));
            component.setChatStyle(style);
        } else {
            Game category = mod.getTwitchCategory(clip.getGameId());
            String componentText = "Clip: \""+clip.getTitle()+"\"";
            if (component == null)
                component = new ChatComponentText(componentText);
            else
                StreamUtils.editTextComponent(component, componentText);
            ChatStyle style = new ChatStyle()
                    .setColor(EnumChatFormatting.AQUA)
                    .setItalic(true)
                    .setUnderlined(true)
                    .setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText(EnumChatFormatting.GRAY+"Title: "+EnumChatFormatting.AQUA+clip.getTitle()+"\n"+EnumChatFormatting.GRAY+"Category: "+EnumChatFormatting.AQUA+(category == null ? "unknown" : category.getName())+"\n"+EnumChatFormatting.GRAY+"Clip author: "+EnumChatFormatting.AQUA+clip.getCreatorName()+"\n"+EnumChatFormatting.GRAY+"Streamer: "+EnumChatFormatting.AQUA+clip.getBroadcasterName()+"\n"+EnumChatFormatting.GRAY+"Length: "+EnumChatFormatting.AQUA+clip.getDuration()+" seconds\n\n"+EnumChatFormatting.YELLOW+"Click to view clip")))
                    .setChatClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, clip.getUrl()));
            component.setChatStyle(style);
        }
        return component;
    }
}

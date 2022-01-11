package me.mini_bomba.streamchatmod.runnables;

import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import com.github.twitch4j.common.enums.CommandPermission;
import com.github.twitch4j.helix.domain.Clip;
import com.github.twitch4j.helix.domain.Game;
import me.mini_bomba.streamchatmod.StreamChatMod;
import me.mini_bomba.streamchatmod.StreamUtils;
import me.mini_bomba.streamchatmod.utils.ChatComponentStreamEmote;
import me.mini_bomba.streamchatmod.utils.ChatComponentTwitchMessage;
import me.mini_bomba.streamchatmod.utils.ColorUtil;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

public class TwitchMessageHandler implements Runnable {
    private final ChannelMessageEvent event;
    private final StreamChatMod mod;
    private static final Logger LOGGER = LogManager.getLogger();
    private static final char formatChar = '\u00a7';
    private static final String validFormats = "0123456789abcdefklmnorABCDEFKLMNORzZ";
    public static final Pattern urlPattern = Pattern.compile("https?://[^.\\s/]+(?:\\.[^.\\s/]+)+\\S*");
    public static final Pattern whitespacePattern = Pattern.compile("\\s+");
    private static final Pattern formatCodePattern = Pattern.compile(formatChar + "[0-9a-fA-Fk-rK-RzZ]");
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
                    if (validFormats.contains(String.valueOf(msg[i + 1])))
                        msg[i] = formatChar;
                }
            }
            message = String.valueOf(msg);
        }
        return message;
    }

    public static List<IChatComponent> processEmotes(StreamChatMod mod, String message, String channelId) {
        List<IChatComponent> result = new LinkedList<>();
        List<String> nextComponent = new LinkedList<>();
        char color = 0;
        char format = 0;
        char nextColor = 0;
        char nextFormat = 0;
        Matcher whitespace = whitespacePattern.matcher(message);
        if (message.length() > 0 && whitespacePattern.matcher(message.substring(0, 1)).find() && whitespace.find())
            nextComponent.add(whitespace.group());
        for (String word : StringUtils.split(message, " \n\t")) {
            if (mod.emotes.isEmote(channelId, word)) {
                if (nextComponent.size() > 0)
                    result.add(new ChatComponentText((color != 0 ? "" + formatChar + color : "") + (format != 0 ? "" + formatChar + format : "") + String.join("", nextComponent)));
                nextComponent.clear();
                if (whitespace.find()) nextComponent.add(whitespace.group());
                color = nextColor;
                format = nextFormat;
                result.add(new ChatComponentStreamEmote(mod, mod.emotes.getEmote(channelId, word)));
            } else {
                Matcher formatMatcher = formatCodePattern.matcher(word);
                while (formatMatcher.find()) {
                    char code = formatMatcher.group().charAt(1);
                    if ("rR".indexOf(code) >= 0) {
                        nextColor = 0;
                        nextFormat = 0;
                    } else if ("klmnoKLMNO".indexOf(code) >= 0)
                        nextFormat = code;
                    else {
                        nextColor = code;
                        nextFormat = 0;
                    }
                }
                nextComponent.add(word);
                if (whitespace.find()) nextComponent.add(whitespace.group());
            }
        }
        if (nextComponent.size() > 0)
            result.add(new ChatComponentText((color != 0 ? "" + formatChar + color : "") + (format != 0 ? "" + formatChar + format : "") + String.join("", nextComponent)));
        return result;
    }

    private List<IChatComponent> processEmotes(String message) {
        return processEmotes(mod, message, event.getChannel().getId());
    }

    @Override
    public void run() {
        boolean showChannel = mod.config.forceShowChannelName.getBoolean() || (mod.twitch != null && mod.twitch.getChat().getChannels().size() > 1);
        Set<CommandPermission> perms = event.getPermissions();
        IChatComponent badges = new ChatComponentText("");
        if (mod.config.showTwitchGlobalBadges.getBoolean()) {
            boolean showChannelBadges = mod.config.showTwitchChannelBadges.getBoolean();
            event.getMessageEvent().getBadges().entrySet().stream()
                    .map(entry -> showChannelBadges ? mod.emotes.getBadge(event.getChannel().getId(), entry.getKey(), entry.getValue()) : mod.emotes.getGlobalBadge(entry.getKey(), entry.getValue()))
                    .filter(Objects::nonNull)
                    .forEach(badge -> badges.appendSibling(new ChatComponentStreamEmote(mod, badge)));
        } else {
            ArrayList<String> badgesTexts = new ArrayList<>();
            if (perms.contains(CommandPermission.BROADCASTER))
                badgesTexts.add(EnumChatFormatting.RED + "STREAMER");
            if (perms.contains(CommandPermission.TWITCHSTAFF))
                badgesTexts.add(EnumChatFormatting.BLACK + "STAFF");
            if (perms.contains(CommandPermission.MODERATOR) && !perms.contains(CommandPermission.BROADCASTER))
                badgesTexts.add(EnumChatFormatting.GREEN + "MOD");
            if (perms.contains(CommandPermission.VIP))
                badgesTexts.add(EnumChatFormatting.LIGHT_PURPLE + "VIP");
            if (perms.contains(CommandPermission.SUBSCRIBER))
                badgesTexts.add(EnumChatFormatting.GOLD + "SUB");
            if (badgesTexts.size() > 0)
                badges.appendSibling(new ChatComponentText(StringUtils.join(badgesTexts, " ")));
        }
        boolean allowFormatting = mod.config.allowFormatting.getBoolean() && (!mod.config.subOnlyFormatting.getBoolean() || perms.stream().anyMatch(p -> p == CommandPermission.SUBSCRIBER || p == CommandPermission.VIP || p == CommandPermission.MODERATOR || p == CommandPermission.TWITCHSTAFF || p == CommandPermission.BROADCASTER));
        String message = event.getMessage();

        Matcher matcher = urlPattern.matcher(message);
        List<ClipComponentMapping> clips = new ArrayList<>();
        IChatComponent component = new ChatComponentTwitchMessage(event.getMessageEvent().getMessageId().orElse(""), event.getChannel().getId(), event.getUser().getId(), (showChannel ? mod.config.getTwitchPrefixWithChannel(event.getChannel().getName()) : mod.config.getFullTwitchPrefix()) + " ");
        if (badges.getSiblings().size() > 0) component.appendSibling(badges);

        String username = event.getMessageEvent().getTagValue("display-name").orElse(event.getUser().getName());

        component.appendSibling(new ChatComponentText((badges.getSiblings().size() > 0 ? " " : "") + ColorUtil.getColorFromHex(event.getMessageEvent().getTagValue("color").orElse("#FFFFFF")) + username + " " + mod.config.getTwitchUserMessageSeparator() + " "));
        int lastEnd = 0;
        while (matcher.find()) {
            if (matcher.start() > lastEnd)
                processEmotes(processColorCodes(message.substring(lastEnd, matcher.start()), allowFormatting)).forEach(component::appendSibling);
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
            processEmotes(processColorCodes(message.substring(lastEnd), allowFormatting)).forEach(component::appendSibling);
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

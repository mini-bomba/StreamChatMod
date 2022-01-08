package me.mini_bomba.streamchatmod;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

import static me.mini_bomba.streamchatmod.StreamChatMod.PRERELEASE;

public class StreamConfig {
    private final Configuration config;
    // common
    public final Property updateCheckerEnabled;
    public final Property forceShowChannelName;
    public final Property allowFormatting;
    public final Property subOnlyFormatting;
    public final Property minecraftChatPrefix;
    public final Property allowMessageDeletion;
    // tokens
    protected final Property twitchToken;
    // twitch
    public final Property twitchEnabled;
    public final Property twitchChannels;
    public final Property twitchSelectedChannel;
    public final Property twitchMessageRedirectEnabled;
    public final Property twitchPrefix;
    public final Property twitchPrefixChannelSeparator;
    public final Property twitchPrefixLastChar;
    public final Property twitchUserMessageSeparator;
    // sounds
    public final Property playSoundOnMessage;
    public final Property playSoundOnFollow;
    public final Property messageSoundVolume;
    public final Property eventSoundVolume;
    // twitch events
    public final Property followEventEnabled;
    // emotes
    public final Property showTwitchGlobalEmotes;
    public final Property showTwitchChannelEmotes;
    public final Property showTwitchGlobalBadges;
    public final Property showBTTVGlobalEmotes;
    public final Property showBTTVChannelEmotes;
    public final Property showFFZGlobalEmotes;
    public final Property showFFZChannelEmotes;
    public final Property allowAnimatedEmotes;

    private static final Logger LOGGER = LogManager.getLogger();

    public StreamConfig(File configFile) {
        config = new Configuration(configFile);
        // common
        updateCheckerEnabled = config.get("common", "updateCheckerEnabled", PRERELEASE);
        forceShowChannelName = config.get("common", "forceShowChannelName", false);
        allowFormatting = config.get("common", "allowFormatting", false);
        subOnlyFormatting = config.get("common", "subOnlyFormatting", false);
        minecraftChatPrefix = config.get("common", "minecraftChatPrefix", "!!");
        allowMessageDeletion = config.get("common", "allowMessageDeletion", true);
        // tokens
        twitchToken = config.get("tokens", "twitch", "");
        // twitch
        twitchEnabled = config.get("twitch", "enabled", false);
        twitchChannels = config.get("twitch", "channels", new String[0]);
        twitchSelectedChannel = config.get("twitch", "selectedChannel", "");
        twitchMessageRedirectEnabled = config.get("twitch", "messageRedirectEnabled", false);
        twitchPrefix = config.get("twitch", "prefix", "&5[TWITCH");
        twitchPrefixChannelSeparator = config.get("twitch", "prefix_channel_separator", "/");
        twitchPrefixLastChar = config.get("twitch", "prefix_last_char", "]");
        twitchUserMessageSeparator = config.get("twitch", "user-message_separator", "&7>>");
        // sounds
        playSoundOnMessage = config.get("sounds", "onMessage", true);
        playSoundOnFollow = config.get("sounds", "onFollow", true);
        messageSoundVolume = config.get("sounds", "messageVolume", 1.0d);
        eventSoundVolume = config.get("sounds", "eventVolume", 1.0d);
        // twitch events
        followEventEnabled = config.get("twitchEvents", "followers", true);
        // emotes
        showTwitchGlobalEmotes = config.get("emotes", "twitch_globals", true);
        showTwitchChannelEmotes = config.get("emotes", "twitch_channel", true);
        showTwitchGlobalBadges = config.get("emotes", "twitch_global_badges", true);
        showBTTVGlobalEmotes = config.get("emotes", "bttv_globals", true);
        showBTTVChannelEmotes = config.get("emotes", "bttv_channel", true);
        showFFZGlobalEmotes = config.get("emotes", "ffz_globals", true);
        showFFZChannelEmotes = config.get("emotes", "ffz_channel", true);
        allowAnimatedEmotes = config.get("emotes", "animated", true);
        saveIfChanged();
    }

    public void saveIfChanged() {
        if (config.hasChanged()) config.save();
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isTwitchTokenSet() {
        return !twitchToken.getString().equals("");
    }

    public void setTwitchToken(String token) {
        twitchToken.set(token);
    }

    public boolean revokeTwitchToken() {
        String token = twitchToken.getString();
        if (token.length() == 0) return false;
        HttpClient client = HttpClients.createDefault();
        HttpPost request = new HttpPost("https://id.twitch.tv/oauth2/revoke?client_id=q7s0qfrigoczrj1a1cltcebjx95q8g&token=" + token);
        HttpResponse response;
        try {
            response = client.execute(request);
        } catch (Exception e) {
            LOGGER.error("Failed to send request to revoke twitch token!");
            e.printStackTrace();
            return false;
        }
        int code = response.getStatusLine().getStatusCode();
        return code == 200;
    }

    public String getFullTwitchPrefix() {
        return getTwitchPrefixWithoutLast() + getTwitchPrefixLastChar();
    }

    public String getTwitchPrefixWithChannel(String channel) {
        return getTwitchPrefixWithoutLast() + getTwitchPrefixChannelSeparator() + channel + getTwitchPrefixLastChar();
    }

    public String getTwitchPrefixLastChar() {
        return twitchPrefixLastChar.getString().replace("&", "\u00a7");
    }

    public String getTwitchPrefixWithoutLast() {
        return twitchPrefix.getString().replace("&", "\u00a7");
    }

    public String getTwitchPrefixChannelSeparator() {
        return twitchPrefixChannelSeparator.getString().replace("&", "\u00a7");
    }

    public String getTwitchUserMessageSeparator() {
        return twitchUserMessageSeparator.getString().replace("&", "\u00a7");
    }
}

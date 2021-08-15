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

public class StreamConfig {
    private final Configuration config;
    public final Property forceShowChannelName;
    public final Property allowFormatting;
    public final Property subOnlyFormatting;
    protected final Property twitchToken;
    public final Property twitchEnabled;
    public final Property twitchChannels;
    public final Property twitchSelectedChannel;
    public final Property twitchMessageRedirectEnabled;
    public final Property playSoundOnMessage;
    public final Property playSoundOnFollow;
    public final Property followEventEnabled;
    public final Property messageSoundVolume;
    public final Property eventSoundVolume;

    private static final Logger LOGGER = LogManager.getLogger();

    public StreamConfig(File configFile) {
        config = new Configuration(configFile);
        forceShowChannelName = config.get("common", "forceShowChannelName", false);
        allowFormatting = config.get("common", "allowFormatting", false);
        subOnlyFormatting = config.get("common", "subOnlyFormatting", false);
        twitchToken = config.get("tokens", "twitch", "");
        twitchEnabled = config.get("twitch", "enabled", false);
        twitchChannels = config.get("twitch", "channels", new String[0]);
        twitchSelectedChannel = config.get("twitch", "selectedChannel", "");
        twitchMessageRedirectEnabled = config.get("twitch", "messageRedirectEnabled", false);
        playSoundOnMessage = config.get("sounds", "onMessage", true);
        playSoundOnFollow = config.get("sounds", "onFollow", true);
        messageSoundVolume = config.get("sounds", "messageVolume", 1.0d);
        eventSoundVolume = config.get("sounds", "eventVolume", 1.0d);
        followEventEnabled = config.get("twitchEvents", "followers", true);
        saveIfChanged();
    }

    public void saveIfChanged() {
        if (config.hasChanged()) config.save();
    }

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

}

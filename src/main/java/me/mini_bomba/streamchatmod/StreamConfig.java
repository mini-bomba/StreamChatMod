package me.mini_bomba.streamchatmod;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

import java.io.File;

public class StreamConfig {
    private final Configuration config;
    public final Property forceShowChannelName;
    protected final Property twitchToken;
    public final Property twitchEnabled;
    public final Property twitchChannels;
    public final Property twitchSelectedChannel;
    public final Property playSoundOnMessage;
    public final Property playSoundOnFollow;
    public final Property followEventEnabled;

    public StreamConfig(File configFile) {
        config = new Configuration(configFile);
        forceShowChannelName = config.get("common", "forceShowChannelName", false);
        twitchToken = config.get("tokens", "twitch", "");
        twitchEnabled = config.get("twitch", "enabled", false);
        twitchChannels = config.get("twitch", "channels", new String[0]);
        twitchSelectedChannel = config.get("twitch", "selectedChannel", "");
        playSoundOnMessage = config.get("sounds", "onMessage", true);
        playSoundOnFollow = config.get("sounds", "onFollow", true);
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

}

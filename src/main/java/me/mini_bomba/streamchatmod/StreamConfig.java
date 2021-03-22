package me.mini_bomba.streamchatmod;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

import java.io.File;

public class StreamConfig {
    private final Configuration config;
    protected final Property showChannelName;
    protected final Property twitchToken;
    protected final Property twitchEnabled;
    protected final Property twitchChannels;
    protected final Property twitchDefaultChannel;

    public StreamConfig(File configFile) {
        config = new Configuration(configFile);
        showChannelName = config.get("common", "showChannelName", false);
        twitchToken = config.get("tokens", "twitch", "");
        twitchEnabled = config.get("twitch", "enabled", false);
        twitchChannels = config.get("twitch", "channels", new String[0]);
        twitchDefaultChannel = config.get("twitch", "default_channel", "");
        saveIfChanged();
    }

    public void saveIfChanged() {
        if (config.hasChanged()) config.save();
    }

}

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

    public StreamConfig(File configFile) {
        config = new Configuration(configFile);
        forceShowChannelName = config.get("common", "forceShowChannelName", false);
        twitchToken = config.get("tokens", "twitch", "");
        twitchEnabled = config.get("twitch", "enabled", false);
        twitchChannels = config.get("twitch", "channels", new String[0]);
        twitchSelectedChannel = config.get("twitch", "selectedChannel", "");
        saveIfChanged();
    }

    public void saveIfChanged() {
        if (config.hasChanged()) config.save();
    }

}

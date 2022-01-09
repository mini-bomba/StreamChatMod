package me.mini_bomba.streamchatmod.utils;

import com.github.twitch4j.helix.domain.ChatBadge;
import com.github.twitch4j.helix.domain.ChatBadgeSet;

import java.io.IOException;

public class TwitchChannelBadge extends TwitchBadge {
    public final String channelId;
    public final String channelName;

    public TwitchChannelBadge(ChatBadge badge, ChatBadgeSet set, String channelId, String channelName) throws IOException {
        super(badge, set, false);
        this.channelId = channelId;
        this.channelName = channelName;
    }
}

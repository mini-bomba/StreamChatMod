package me.mini_bomba.streamchatmod.utils;

import com.github.twitch4j.helix.domain.ChatBadge;
import com.github.twitch4j.helix.domain.ChatBadgeSet;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class TwitchGlobalBadge extends TwitchBadge {

    public TwitchGlobalBadge(ChatBadge badge, ChatBadgeSet set) throws IOException, ExecutionException {
        super(badge, set, true);
    }
}

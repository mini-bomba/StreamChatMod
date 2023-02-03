package me.mini_bomba.streamchatmod.utils;

import com.github.twitch4j.helix.domain.ChatBadge;
import com.github.twitch4j.helix.domain.ChatBadgeSet;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class TwitchBadge extends StreamEmote {
    private static final Pattern idPattern = Pattern.compile("https://static-cdn.jtvnw.net/badges/v1/([\\da-f-]+)/\\d");
    public final String id;
    public final ChatBadgeSet set;
    public final ChatBadge badge;

    protected TwitchBadge(ChatBadge badge, ChatBadgeSet set, boolean global) throws IOException, ExecutionException {
        super(global ? Type.TWITCH_GLOBAL_BADGE : Type.TWITCH_CHANNEL_BADGE, getBadgeId(badge), "streamchatmod/emotes/twitch_" + (global ? "global" : "channel") + "_badges/" + getBadgeId(badge) + "_3x.png", set.getSetId() + ":" + badge.getId(), false);
        this.badge = badge;
        this.set = set;
        this.id = getBadgeId(badge);
    }

    public static String getBadgeId(ChatBadge badge) {
        Matcher m = idPattern.matcher(badge.getSmallImageUrl());
        return m.find() ? m.group(1) : null;
    }
}

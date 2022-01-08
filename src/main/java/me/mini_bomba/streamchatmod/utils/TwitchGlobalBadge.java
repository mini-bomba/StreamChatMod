package me.mini_bomba.streamchatmod.utils;

import com.github.twitch4j.helix.domain.ChatBadge;
import com.github.twitch4j.helix.domain.ChatBadgeSet;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TwitchGlobalBadge extends StreamEmote {
    private static final Pattern idPattern = Pattern.compile("https://static-cdn.jtvnw.net/badges/v1/([\\da-f-]+)/\\d");
    public final String id;
    public final ChatBadgeSet set;
    public final ChatBadge badge;

    public TwitchGlobalBadge(ChatBadge badge, ChatBadgeSet set) throws IOException {
        super(Type.TWITCH_GLOBAL_BADGE, getBadgeId(badge), "streamchatmod/emotes/twitch_global_badges/" + getBadgeId(badge) + "_3x.png", set.getSetId() + ":" + badge.getId(), false);
        this.badge = badge;
        this.set = set;
        this.id = getBadgeId(badge);
    }

    public static String getBadgeId(ChatBadge badge) {
        Matcher m = idPattern.matcher(badge.getSmallImageUrl());
        return m.find() ? m.group(1) : null;
    }
}

package me.mini_bomba.streamchatmod.utils;

import com.github.twitch4j.helix.domain.Emote;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class TwitchEmote extends StreamEmote {
    public final Emote emote;

    public TwitchEmote(Emote emote) throws IOException, ExecutionException {
        super(convertType(emote.getParsedEmoteType()), emote.getId(), (convertType(emote.getParsedEmoteType()) == Type.TWITCH_GLOBAL ? "streamchatmod/emotes/twitch_global/" : "streamchatmod/emotes/twitch_channel/") + emote.getId() + "_3x" + (emote.getFormat().contains(Emote.Format.ANIMATED) ? ".gif" : ".png"), emote.getName(), false);
        this.emote = emote;
    }

    private static Type convertType(Emote.Type type) {
        switch (type) {
            case BITS_TIER:
            case FOLLOWER:
            case SUBSCRIPTIONS:
            case CHANNEL_POINTS:
                return Type.TWITCH_CHANNEL;
            default:
                return Type.TWITCH_GLOBAL;
        }
    }
}

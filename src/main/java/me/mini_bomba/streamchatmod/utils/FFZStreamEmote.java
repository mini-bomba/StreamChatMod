package me.mini_bomba.streamchatmod.utils;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class FFZStreamEmote extends StreamEmote {
    public final FFZEmote emote;

    public FFZStreamEmote(FFZEmote emote, boolean isGlobal) throws IOException, ExecutionException {
        super(isGlobal ? Type.FFZ_GLOBAL : Type.FFZ_CHANNEL, String.valueOf(emote.id), (isGlobal ? "streamchatmod/emotes/ffz_global/" : "streamchatmod/emotes/ffz_channel/") + emote.id + "_2x.png", emote.name, false);
        this.emote = emote;
    }
}

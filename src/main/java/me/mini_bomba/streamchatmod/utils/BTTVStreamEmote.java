package me.mini_bomba.streamchatmod.utils;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class BTTVStreamEmote extends StreamEmote {
    public final BTTVEmote emote;

    public BTTVStreamEmote(BTTVEmote emote, boolean isGlobal) throws IOException, ExecutionException {
        super(isGlobal ? Type.BTTV_GLOBAL : Type.BTTV_CHANNEL, emote.id, (isGlobal ? "streamchatmod/emotes/bttv_global/" : "streamchatmod/emotes/bttv_channel/") + emote.id + "_2x." + emote.imageType.name().toLowerCase(), emote.name, emote.imageType == BTTVEmote.ImageType.GIF);
        this.emote = emote;
    }
}

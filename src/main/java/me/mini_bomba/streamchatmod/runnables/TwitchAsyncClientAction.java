package me.mini_bomba.streamchatmod.runnables;

import lombok.SneakyThrows;
import me.mini_bomba.streamchatmod.StreamChatMod;

public class TwitchAsyncClientAction implements Runnable {

    private final StreamChatMod mod;
    private final Runnable action;

    public TwitchAsyncClientAction(StreamChatMod mod, Runnable action) {
        this.mod = mod;
        this.action = action;
    }

    @SneakyThrows
    @Override
    public void run() {
        action.run();
        mod.twitchAsyncAction = null;
    }
}

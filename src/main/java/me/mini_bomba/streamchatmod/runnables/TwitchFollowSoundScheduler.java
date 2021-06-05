package me.mini_bomba.streamchatmod.runnables;

import me.mini_bomba.streamchatmod.StreamChatMod;
import me.mini_bomba.streamchatmod.StreamUtils;
import net.minecraft.client.Minecraft;

public class TwitchFollowSoundScheduler implements Runnable {

    private final StreamChatMod mod;

    public TwitchFollowSoundScheduler(StreamChatMod mod) {
        this.mod = mod;
    }

    @Override
    public void run() {
        Minecraft mc = Minecraft.getMinecraft();
        mc.addScheduledTask(() -> StreamUtils.playSound("note.harp", (float) mod.config.eventSoundVolume.getDouble(), 1.0f));
        try { Thread.sleep(250); } catch (InterruptedException ignored) {}
        mc.addScheduledTask(() -> StreamUtils.playSound("note.harp", (float) mod.config.eventSoundVolume.getDouble(), 1.25f));
        try { Thread.sleep(250); } catch (InterruptedException ignored) {}
        mc.addScheduledTask(() -> StreamUtils.playSound("note.harp", (float) mod.config.eventSoundVolume.getDouble(), 1.5f));
    }
}

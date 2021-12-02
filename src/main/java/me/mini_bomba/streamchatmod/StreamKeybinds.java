package me.mini_bomba.streamchatmod;

import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Keyboard;

import java.util.ConcurrentModificationException;

public class StreamKeybinds {
    public static final KeyBinding createMarker = new KeyBinding("Create new marker", Keyboard.KEY_NONE, "StreamChatMod");
    public static final KeyBinding createClip = new KeyBinding("Create new clip", Keyboard.KEY_NONE, "StreamChatMod");
    public static final KeyBinding showStreamStats = new KeyBinding("Show stream statistics", Keyboard.KEY_NONE, "StreamChatMod");
    private final StreamChatMod mod;

    protected StreamKeybinds(StreamChatMod mod) {
        this.mod = mod;
    }

    protected final void registerKeybindings() {
        ClientRegistry.registerKeyBinding(createMarker);
        ClientRegistry.registerKeyBinding(createClip);
        ClientRegistry.registerKeyBinding(showStreamStats);
    }

    @SubscribeEvent
    public void onKeybind(InputEvent.KeyInputEvent event) {
        if (createMarker.isPressed()) onCreateMarker();
        if (createClip.isPressed()) onCreateClip();
        if (showStreamStats.isPressed()) onShowStreamStats();
    }

    private void onCreateMarker() {
        StreamUtils.addMessage(EnumChatFormatting.GRAY+"Creating a marker...");
        try {
            mod.asyncCreateMarker();
        } catch (ConcurrentModificationException e) {
            StreamUtils.addMessage(EnumChatFormatting.RED+"Creating a marker failed: Another async operation is already in progress");
        }
    }

    private void onCreateClip() {
        StreamUtils.addMessage(EnumChatFormatting.GRAY + "Creating a clip...");
        try {
            mod.asyncCreateClip(true);
        } catch (ConcurrentModificationException e) {
            StreamUtils.addMessage(EnumChatFormatting.RED + "Creating a clip failed: Another async operation is already in progress");
        }
    }

    private void onShowStreamStats() {
        try {
            mod.asyncShowTwitchStreamStats();
        } catch (ConcurrentModificationException e) {
            StreamUtils.addMessage(EnumChatFormatting.RED + "Viewing stream statistics failed: Another async operation is already in progress");
        }
    }
}

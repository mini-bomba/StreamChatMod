package me.mini_bomba.streamchatmod.asm.hooks;

import me.mini_bomba.streamchatmod.events.LocalMessageEvent;
import net.minecraftforge.common.MinecraftForge;

@SuppressWarnings("unused")
public class GuiScreenHook {

    public static String redirectMessage(String msg) {
        LocalMessageEvent event = new LocalMessageEvent(msg);
        if (!MinecraftForge.EVENT_BUS.post(event) && event.message != null) return event.message;
        else return "";
    }

}

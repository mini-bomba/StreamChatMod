package me.mini_bomba.streamchatmod.events;

import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * This event is fired whenever the local player is about to send a chat message.<br>
 * This is done after no client commands match.<br>
 * <b>Note that the message still may be a server command, it's best to assume any message starting with '/' is a server command.</b><br>
 * <br>
 * {@link #message} is set to the message content and can be edited to change the content of the message.
 * If the message is empty, the event is automatically cancelled.<br>
 * <br>
 * This event is Cancelable - if canceled, the message will not be sent.
*/
@Cancelable
public class LocalMessageEvent extends Event {
    public String message;

    public LocalMessageEvent(String message) {
        this.message = message;
    }
}

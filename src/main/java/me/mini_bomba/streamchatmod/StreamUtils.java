package me.mini_bomba.streamchatmod;

import com.mojang.realmsclient.gui.ChatFormatting;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class StreamUtils {

    public static void addMessage(ICommandSender player, String message) {
        player.addChatMessage(new ChatComponentText(message));
    }

    public static void addMessage(EntityPlayerSP player, String message) {
        player.addChatMessage(new ChatComponentText(message));
    }

    public static void addMessage(EntityPlayerSP player, IChatComponent component) {
        player.addChatMessage(component);
    }

    public static void addMessage(IChatComponent component) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc != null) {
            EntityPlayerSP player = mc.thePlayer;
            if (player != null) addMessage(player, component);
        }
    }

    public static void addMessage(String message) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc != null) {
            EntityPlayerSP player = mc.thePlayer;
            if (player != null) addMessage(player, message);
        }
    }

    public static void addMessages(ICommandSender player, String[] messages) {
        for (String msg : messages) addMessage(player, msg);
    }

    public static void addMessages(EntityPlayerSP player, String[] messages) {
        for (String msg : messages) addMessage(player, msg);
    }

    public static void addMessages(String[] message) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc != null) {
            EntityPlayerSP player = mc.thePlayer;
            if (player != null) addMessages(player, message);
        }
    }

    public static void playSound(String sound, float volume, float pitch) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc != null) {
            EntityPlayerSP player = mc.thePlayer;
            if (player != null) player.playSound(sound, volume, pitch);
        }
    }

    public static Boolean readStringAsBoolean(String str) {
        switch(str.toLowerCase()) {
            case "yes":
            case "true":
            case "1":
            case "y":
            case "enable":
            case "enabled":
                return true;
            case "false":
            case "no":
            case "0":
            case "n":
            case "disable":
            case "disabled":
                return false;
            default:
                return null;
        }
    }

    public static class TwitchOAuth2HandlerMain implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            InputStream file = getClass().getClassLoader().getResourceAsStream("token.html");
            byte[] readFile;
            int len;
            if (file == null) {
                readFile = "oof".getBytes();
                len = readFile.length;
            } else {
                readFile = new byte[file.available()];
                len = file.read(readFile);
                file.close();
            }
            exchange.sendResponseHeaders(200, len);
            OutputStream os = exchange.getResponseBody();
            os.write(readFile);
            exchange.close();
//            exchange.getHttpContext().getServer().stop(5);
        }
    }

    public static class TwitchOAuth2HandlerSecondary implements HttpHandler {
        private final StreamChatMod mod;

        public TwitchOAuth2HandlerSecondary(StreamChatMod mod) {
            this.mod = mod;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            mod.httpShutdownTimer = -1;
            String token = exchange.getRequestURI().getQuery();
            if (token != null) {
                mod.config.setTwitchToken(token);
                mod.config.saveIfChanged();
                mod.stopTwitch();
                mod.startTwitch();
                addMessage(ChatFormatting.GREEN+"The Twitch token has been successfully set!");
            }
            exchange.sendResponseHeaders(token == null ? 400 : 200, 0);
            exchange.close();
            if (token == null) return;
            exchange.getHttpContext().getServer().stop(5);
            mod.httpServer = null;
        }
    }

}

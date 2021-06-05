package me.mini_bomba.streamchatmod;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Properties;

public class StreamUtils {
    private static final Logger LOGGER = LogManager.getLogger();

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

    public static void queueAddMessage(String message) {
        Minecraft.getMinecraft().addScheduledTask(() -> addMessage(message));
    }

    public static void queueAddMessages(String[] messages) {
        Minecraft.getMinecraft().addScheduledTask(() -> addMessages(messages));
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
            case "on":
                return true;
            case "false":
            case "no":
            case "0":
            case "n":
            case "disable":
            case "disabled":
            case "off":
                return false;
            default:
                return null;
        }
    }

    @Nullable
    public static String getLatestVersion() {
        try {
            URL url = new URL("https://raw.githubusercontent.com/mini-bomba/StreamChatMod/master/gradle.properties");
            Properties properties = new Properties();
            properties.load(url.openStream());
            return properties.getProperty("version");
        } catch (Exception e) {
            LOGGER.warn("Could not check for updates!");
            e.printStackTrace();
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
        }
    }

    public static class TwitchOAuth2HandlerSecondary implements HttpHandler {
        private final StreamChatMod mod;

        public TwitchOAuth2HandlerSecondary(StreamChatMod mod) {
            this.mod = mod;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String token = exchange.getRequestURI().getQuery();
            if (token != null) {
                mod.config.setTwitchToken(token);
                mod.config.twitchEnabled.set(true);
                mod.config.saveIfChanged();
                if (mod.twitchAsyncAction == null) {
                    addMessage(EnumChatFormatting.GRAY + "Token set, restarting twitch chat...");
                    mod.asyncRestartTwitch();
                } else {
                    addMessage(EnumChatFormatting.RED + "There was an async action running, could not restart Twitch client. Run /twitch restart to finish setup.");
                }
            }
            exchange.sendResponseHeaders(token == null ? 400 : 200, 0);
            exchange.close();
            if (token == null) return;
            exchange.getHttpContext().getServer().stop(5);
            mod.httpServer = null;
            if (mod.httpShutdownScheduler != null) mod.httpShutdownScheduler.interrupt();
            mod.httpShutdownScheduler = null;
        }
    }

}

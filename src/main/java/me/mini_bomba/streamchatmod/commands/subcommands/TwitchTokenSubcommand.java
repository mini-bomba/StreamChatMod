package me.mini_bomba.streamchatmod.commands.subcommands;

import com.sun.net.httpserver.HttpServer;
import me.mini_bomba.streamchatmod.StreamChatMod;
import me.mini_bomba.streamchatmod.StreamUtils;
import me.mini_bomba.streamchatmod.runnables.HTTPServerShutdownScheduler;
import me.mini_bomba.streamchatmod.commands.ICommandNode;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.EnumChatFormatting;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TwitchTokenSubcommand extends TwitchSubcommand {

    public TwitchTokenSubcommand(StreamChatMod mod, ICommandNode<TwitchSubcommand> parentCommand) {
        super(mod, parentCommand);
    }

    @Override
    public @NotNull List<TwitchSubcommand> getSubcommands() {
        return Collections.emptyList();
    }

    @Override
    public @NotNull String getSubcommandName() {
        return "token";
    }

    @Override
    public @NotNull List<String> getSubcommandAliases() {
        return Arrays.asList("gentoken", "generatetoken", "gettoken");
    }

    @Override
    public @NotNull String getSubcommandUsage() {
        return "token";
    }

    @Override
    public @NotNull String getDescription() {
        return "Opens a page to generate the token for Twitch & automatically updates it";
    }

    @Override
    public TwitchSubcommandCategory getCategory() {
        return TwitchSubcommandCategory.SETUP;
    }

    @Override
    public void processSubcommand(ICommandSender sender, String[] args) throws CommandException {
        try {
            if (mod.httpServer == null) {
                mod.httpServer = HttpServer.create(new InetSocketAddress(39571), 0);
                mod.httpServer.createContext("/", new StreamUtils.TwitchOAuth2HandlerMain());
                mod.httpServer.createContext("/setToken", new StreamUtils.TwitchOAuth2HandlerSecondary(mod));
                mod.httpServer.setExecutor(null);
                mod.httpServer.start();
                mod.httpShutdownScheduler = new Thread(new HTTPServerShutdownScheduler(mod));
                mod.httpShutdownScheduler.start();
            }
        } catch (Exception e) {
            StreamUtils.addMessage(sender, EnumChatFormatting.RED+"Something went wrong while attempting to start an HTTP server for automatic token setting. Please manually set the token using "+EnumChatFormatting.GRAY+"/twitch settoken "+EnumChatFormatting.RED+"after generating.");
        }
        boolean opened = false;
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            try {
                Desktop.getDesktop().browse(new URI("https://id.twitch.tv/oauth2/authorize?response_type=token&client_id=q7s0qfrigoczrj1a1cltcebjx95q8g&redirect_uri=http://localhost:39571&scope=chat:read+chat:edit+channel:moderate+channel:manage:broadcast+user:edit:broadcast"));
                opened = true;
            } catch (Exception ignored) {}
        }
        if (!opened) StreamUtils.addMessages(sender, new String[]{
            EnumChatFormatting.GREEN+"Please open this link in your browser:",
            EnumChatFormatting.GRAY+"https://id.twitch.tv/oauth2/authorize?response_type=token&client_id=q7s0qfrigoczrj1a1cltcebjx95q8g&redirect_uri=http://localhost:39571&scope=chat:read+chat:edit+channel:moderate+channel:manage:broadcast+user:edit:broadcast"
        });
        else StreamUtils.addMessage(sender, EnumChatFormatting.GREEN+"Opening link in your browser...");
        StreamUtils.addMessage(sender, EnumChatFormatting.AQUA+"The token will be automatically saved if generated within 120 seconds.");
    }
}

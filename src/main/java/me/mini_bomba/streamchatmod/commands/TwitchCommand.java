package me.mini_bomba.streamchatmod.commands;

import com.github.twitch4j.chat.TwitchChat;
import net.minecraft.util.EnumChatFormatting;
import com.sun.net.httpserver.HttpServer;
import me.mini_bomba.streamchatmod.StreamChatMod;
import me.mini_bomba.streamchatmod.StreamUtils;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;

import java.awt.*;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;

public class TwitchCommand extends CommandBase {

    private final StreamChatMod mod;

    public TwitchCommand(StreamChatMod mod) {
        this.mod = mod;
    }

    @Override
    public String getCommandName() {
        return "twitch";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/twitch ...";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        if (args.length == 0) throw new CommandException("No subcommand given: use /twitch help to see available subcommands.");
        TwitchChat chat = mod.twitch != null ? mod.twitch.getChat() : null;
        switch (args[0].toLowerCase()) {
            case "help":
            case "?":
            case "h":
                StreamUtils.addMessages(sender, new String[]{
                        EnumChatFormatting.GREEN + "Usage of /twitch:",
                        EnumChatFormatting.GRAY + "/twitch help"+EnumChatFormatting.WHITE+" - "+EnumChatFormatting.AQUA+"Shows this message",
                        EnumChatFormatting.GRAY + "/twitch enable"+EnumChatFormatting.WHITE+" - "+EnumChatFormatting.AQUA+"Enables the Twitch chat",
                        EnumChatFormatting.GRAY + "/twitch disable"+EnumChatFormatting.WHITE+" - "+EnumChatFormatting.AQUA+"Disables the Twitch chat",
                        EnumChatFormatting.GRAY + "/twitch restart"+EnumChatFormatting.WHITE+" - "+EnumChatFormatting.AQUA+"Restarts the Twitch chat",
                        EnumChatFormatting.GRAY + "/twitch channels"+EnumChatFormatting.WHITE+" - "+EnumChatFormatting.AQUA+"Manages joined Twitch chats",
                        EnumChatFormatting.GRAY + "/twitch sounds"+EnumChatFormatting.WHITE+" - "+EnumChatFormatting.AQUA+"Manages enabled sounds",
                        EnumChatFormatting.GRAY + "/twitch token"+EnumChatFormatting.WHITE+" - "+EnumChatFormatting.AQUA+"Opens a page to generate the token for Twitch & automatically updates it",
                        EnumChatFormatting.GRAY + "/twitch settoken <token>"+EnumChatFormatting.WHITE+" - "+EnumChatFormatting.AQUA+"Manually set the token for Twitch if /twitch token fails to automatically set it."
                });
                break;
            case "enable":
            case "on":
                if (!mod.config.isTwitchTokenSet()) throw new CommandException("Twitch token is not configured! Use /twitch token to configure it.");
                if (mod.twitch != null || mod.config.twitchEnabled.getBoolean()) throw new CommandException("Twitch chat is already enabled!");
                mod.config.twitchEnabled.set(true);
                mod.config.saveIfChanged();
                mod.startTwitch();
                StreamUtils.addMessage(sender, EnumChatFormatting.GREEN+"Enabled the Twitch Chat!");
                break;
            case "disable":
            case "off":
                if (mod.twitch == null || !mod.config.twitchEnabled.getBoolean()) throw new CommandException("Twitch chat is already disabled!");
                mod.config.twitchEnabled.set(false);
                mod.config.saveIfChanged();
                mod.stopTwitch();
                StreamUtils.addMessage(sender, EnumChatFormatting.GREEN+"Disabled the Twitch Chat!");
                break;
            case "restart":
            case "reload":
            case "r":
                if (!mod.config.isTwitchTokenSet()) throw new CommandException("Twitch token is not configured! Use /twitch token to configure it.");
                if (!mod.config.twitchEnabled.getBoolean()) throw new CommandException("Twitch chat is not enabled!");
                mod.stopTwitch();
                mod.startTwitch();
                StreamUtils.addMessage(sender, EnumChatFormatting.GREEN+"Restarted the Twitch Chat!");
                break;
            case "sounds":
            case "sound":
            case "s":
                switch (args.length >= 2 ? args[1].toLowerCase() : "") {
                    case "msg":
                    case "message":
                        if (args.length == 2)
                            StreamUtils.addMessage(EnumChatFormatting.AQUA + "Sound effect on new stream chat message is: " + (mod.config.playSoundOnMessage.getBoolean() ? EnumChatFormatting.GREEN + "Enabled" : EnumChatFormatting.RED + "Disabled"));
                        else {
                            Boolean newState = StreamUtils.readStringAsBoolean(args[2]);
                            if (newState == null)
                                throw new CommandException("Invalid boolean value" + args[2]);
                            else {
                                mod.config.playSoundOnMessage.set(newState);
                                StreamUtils.addMessage(EnumChatFormatting.GREEN + "Sound effect on new stream chat message has been " + (newState ? "enabled" : "disabled") + "!");
                            }
                        }
                        break;
                    default:
                        StreamUtils.addMessages(sender, new String[]{
                                EnumChatFormatting.GREEN + "Usage of /twitch sounds:",
                                EnumChatFormatting.GRAY + "/twitch sounds message [enable/disable]"+EnumChatFormatting.WHITE+" - "+EnumChatFormatting.AQUA+"Enables/disables sound effect on new stream chat message."
                        });
                }
                break;
            case "channel":
            case "channels":
            case "chat":
            case "chats":
            case "c":
                String[] channelArray = mod.config.twitchChannels.getStringList();
                ArrayList<String> channelList = new ArrayList<>(Arrays.asList(channelArray));
                String channel = args.length >= 3 ? args[2].toLowerCase() : "";
                switch (args.length >= 2 ? args[1].toLowerCase() : "") {
                    case "join":
                    case "j":
                    case "+":
                    case "add":
                        if (chat == null) throw new CommandException("Please enable Twitch chat first!");
                        if (channel.equals("")) throw new CommandException("Missing parameter: channel to join");
                        if (channelList.contains(channel) && chat.isChannelJoined(channel)) throw new CommandException("Channel "+channel+" is already joined!");
                        chat.joinChannel(channel);
                        if (!chat.isChannelJoined(channel)) throw new CommandException("Something went wrong: Could not join the channel.");
                        channelList.add(channel);
                        mod.config.twitchChannels.set(channelList.toArray(new String[0]));
                        mod.config.saveIfChanged();
                        StreamUtils.addMessage(sender, EnumChatFormatting.GREEN+"Joined "+channel+"'s chat!");
                        break;
                    case "leave":
                    case "l":
                    case "remove":
                    case "delete":
                    case "del":
                    case "-":
                        if (chat == null) throw new CommandException("Please enable Twitch chat first!");
                        if (channel.equals("")) throw new CommandException("Missing parameter: channel to leave");
                        if (!channelList.contains(channel) && !chat.isChannelJoined(channel)) throw new CommandException("Channel "+channel+" is not joined!");
                        chat.leaveChannel(channel);
                        if (chat.isChannelJoined(channel)) throw new CommandException("Something went wrong: Could not leave the channel.");
                        channelList.remove(channel);
                        mod.config.twitchChannels.set(channelList.toArray(new String[0]));
                        mod.config.saveIfChanged();
                        StreamUtils.addMessage(sender, EnumChatFormatting.GREEN+"Left "+channel+"'s chat!");
                        break;
                    case "list":
                    case "show":
                    case "ls":
                        if (chat == null) throw new CommandException("Please enable Twitch chat first!");
                        String[] channels = chat.getChannels().toArray(new String[0]).clone();
                        StreamUtils.addMessage(sender, EnumChatFormatting.GREEN+"Currently joined stream chats ("+channels.length+"):");
                        for (int i = 0; i < channels.length; i++) {
                            channels[i] = EnumChatFormatting.AQUA+"• "+channels[i];
                        }
                        StreamUtils.addMessages(sender, channels);
                        break;
                    case "select":
                    case "s":
                        if (chat == null) throw new CommandException("Please enable Twitch chat first!");
                        mod.config.twitchSelectedChannel.set(channel);
                        mod.config.saveIfChanged();
                        if (channel.equals("")) StreamUtils.addMessage(sender, EnumChatFormatting.GREEN+"Unselected the stream chat channel!");
                        else StreamUtils.addMessage(sender, EnumChatFormatting.GREEN+"Selected "+channel+"'s stream chat!");
                        break;
                    default:
                        StreamUtils.addMessages(sender, new String[]{
                                EnumChatFormatting.GREEN + "Usage of /twitch channels:",
                                EnumChatFormatting.GRAY + "/twitch channels join <channel name>"+EnumChatFormatting.WHITE+" - "+EnumChatFormatting.AQUA+"Joins the specified Twitch channel.",
                                EnumChatFormatting.GRAY + "/twitch channels leave <channel name>"+EnumChatFormatting.WHITE+" - "+EnumChatFormatting.AQUA+"Leaves the specified Twitch channel.",
                                EnumChatFormatting.GRAY + "/twitch channels list"+EnumChatFormatting.WHITE+" - "+EnumChatFormatting.AQUA+"Lists the joined Twitch channels.",
                                EnumChatFormatting.GRAY + "/twitch channels select [channel name]"+EnumChatFormatting.WHITE+" - "+EnumChatFormatting.AQUA+"Selects the specified channel to send messages to.",
                        });
                }
                break;
            case "settoken":
                if (args.length < 2) throw new CommandException("Missing required parameter: token. You can generate it by running /twitch gentoken");
                mod.config.setTwitchToken(args[1]);
                mod.config.saveIfChanged();
                StreamUtils.addMessage(sender, EnumChatFormatting.GREEN+"Twitch token was successfully updated!");
                break;
            case "gentoken":
            case "generatetoken":
            case "gettoken":
            case "token":
                try {
                    if (mod.httpServer == null) {
                        mod.httpServer = HttpServer.create(new InetSocketAddress(39571), 0);
                        mod.httpServer.createContext("/", new StreamUtils.TwitchOAuth2HandlerMain());
                        mod.httpServer.createContext("/setToken", new StreamUtils.TwitchOAuth2HandlerSecondary(mod));
                        mod.httpServer.setExecutor(null);
                        mod.httpServer.start();
                        mod.httpShutdownTimer = 120*40;
                    }
                } catch (Exception e) {
                    StreamUtils.addMessage(sender, EnumChatFormatting.RED+"Something went wrong while attempting to start an HTTP server for automatic token setting. Please manually set the token using "+EnumChatFormatting.GRAY+"/twitch settoken "+EnumChatFormatting.RED+"after generating.");
                }
                boolean opened = false;
                if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                    try {
                        Desktop.getDesktop().browse(new URI("https://id.twitch.tv/oauth2/authorize?response_type=token&client_id=q7s0qfrigoczrj1a1cltcebjx95q8g&redirect_uri=http://localhost:39571&scope=chat:read+chat:edit"));
                        opened = true;
                    } catch (Exception ignored) {}
                }
                if (!opened) StreamUtils.addMessages(sender, new String[]{
                            EnumChatFormatting.GREEN+"Please open this link in your browser:",
                            EnumChatFormatting.GRAY+"https://id.twitch.tv/oauth2/authorize?response_type=token&client_id=q7s0qfrigoczrj1a1cltcebjx95q8g&redirect_uri=http://localhost:39571&scope=chat:read+chat:edit"
                });
                else StreamUtils.addMessage(sender, EnumChatFormatting.GREEN+"Opening link in your browser...");
                StreamUtils.addMessage(sender, EnumChatFormatting.AQUA+"The token will be automatically saved if generated within 120 seconds.");
                break;
            default:
                throw new CommandException("Unknown subcommand: use /twitch help to see available subcommands.");
        }
    }

}

package me.mini_bomba.streamchatmod.commands;

import com.github.twitch4j.chat.TwitchChat;
import com.sun.net.httpserver.HttpServer;
import me.mini_bomba.streamchatmod.StreamChatMod;
import me.mini_bomba.streamchatmod.StreamUtils;
import me.mini_bomba.streamchatmod.runnables.HTTPServerShutdownScheduler;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.EnumChatFormatting;

import java.awt.*;
import java.net.InetSocketAddress;
import java.net.URI;
import java.time.Duration;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
                        EnumChatFormatting.GRAY + "/twitch status"+EnumChatFormatting.WHITE+" - "+EnumChatFormatting.AQUA+"Shows the status of the mod",
                        EnumChatFormatting.GRAY + "/twitch enable"+EnumChatFormatting.WHITE+" - "+EnumChatFormatting.AQUA+"Enables the Twitch chat",
                        EnumChatFormatting.GRAY + "/twitch disable"+EnumChatFormatting.WHITE+" - "+EnumChatFormatting.AQUA+"Disables the Twitch chat",
                        EnumChatFormatting.GRAY + "/twitch restart"+EnumChatFormatting.WHITE+" - "+EnumChatFormatting.AQUA+"Restarts the Twitch chat",
                        EnumChatFormatting.GRAY + "/twitch channels"+EnumChatFormatting.WHITE+" - "+EnumChatFormatting.AQUA+"Manages joined Twitch chats",
                        EnumChatFormatting.GRAY + "/twitch sounds"+EnumChatFormatting.WHITE+" - "+EnumChatFormatting.AQUA+"Manages enabled sounds",
                        EnumChatFormatting.GRAY + "/twitch events"+EnumChatFormatting.WHITE+" - "+EnumChatFormatting.AQUA+"Manages enabled events",
                        EnumChatFormatting.GRAY + "/twitch formatting [enable/disable]"+EnumChatFormatting.WHITE+" - "+EnumChatFormatting.AQUA+"Allows viewers to use chat formatting codes (ex. &7) to send formatted messages",
                        EnumChatFormatting.GRAY + "/twitch mode [new mode]"+EnumChatFormatting.WHITE+" - "+EnumChatFormatting.AQUA+"Manages the destination of messages sent through Minecraft chat",
                        EnumChatFormatting.GRAY + "/twitch ban <user> [reason]"+EnumChatFormatting.WHITE+" - "+EnumChatFormatting.AQUA+"Bans the user in the currently selected channel",
                        EnumChatFormatting.GRAY + "/twitch unban <user>"+EnumChatFormatting.WHITE+" - "+EnumChatFormatting.AQUA+"Unbans the user in the currently selected channel",
                        EnumChatFormatting.GRAY + "/twitch timeout <user> <time> [reason]"+EnumChatFormatting.WHITE+" - "+EnumChatFormatting.AQUA+"Timeouts the user in the currently selected channel",
                        EnumChatFormatting.GRAY + "/twitch clearchat"+EnumChatFormatting.WHITE+" - "+EnumChatFormatting.AQUA+"Clears the currently selected channel's chat",
                        EnumChatFormatting.GRAY + "/twitch delete <channel> <message id>"+EnumChatFormatting.WHITE+" - "+EnumChatFormatting.AQUA+"Deletes the selected message. Click a twitch message to automatically generate this command",
                        EnumChatFormatting.GRAY + "/twitch token"+EnumChatFormatting.WHITE+" - "+EnumChatFormatting.AQUA+"Opens a page to generate the token for Twitch & automatically updates it",
                        EnumChatFormatting.GRAY + "/twitch settoken <token>"+EnumChatFormatting.WHITE+" - "+EnumChatFormatting.AQUA+"Manually set the token for Twitch if /twitch token fails to automatically set it.",
                        EnumChatFormatting.GRAY + "/twitch revoketoken"+EnumChatFormatting.WHITE+" - "+EnumChatFormatting.AQUA+"Revoke the currently set token & removes it from the config. You can run this if you leak your current token."
                });
                break;
            case "status":
                mod.printTwitchStatus();
                break;
            case "enable":
            case "on":
            case "start":
                if (!mod.config.isTwitchTokenSet()) throw new CommandException("Twitch token is not configured! Use /twitch token to configure it.");
                if (mod.twitch != null) throw new CommandException("Twitch chat is already enabled!");
                if (mod.twitchAsyncAction != null) throw new CommandException("An action for the Twitch Chat is currently pending, please wait.");
                mod.config.twitchEnabled.set(true);
                mod.config.saveIfChanged();
                mod.asyncStartTwitch();
                StreamUtils.addMessage(EnumChatFormatting.GRAY + "Starting Twitch Chat...");
                break;
            case "disable":
            case "off":
            case "stop":
                if (mod.twitch == null && !mod.config.twitchEnabled.getBoolean()) throw new CommandException("Twitch chat is already disabled!");
                if (mod.twitchAsyncAction != null) throw new CommandException("An action for the Twitch Chat is currently pending, please wait.");
                mod.config.twitchEnabled.set(false);
                mod.config.saveIfChanged();
                mod.asyncStopTwitch();
                StreamUtils.addMessage(EnumChatFormatting.GRAY + "Stopping Twitch Chat...");
                break;
            case "restart":
            case "reload":
            case "r":
                if (!mod.config.isTwitchTokenSet()) throw new CommandException("Twitch token is not configured! Use /twitch token to configure it.");
                if (!mod.config.twitchEnabled.getBoolean()) throw new CommandException("Twitch chat is not enabled!");
                if (mod.twitchAsyncAction != null) throw new CommandException("An action for the Twitch Chat is currently pending, please wait.");
                mod.asyncRestartTwitch();
                StreamUtils.addMessage(EnumChatFormatting.GRAY + "Restarting Twitch Chat...");
                break;
            case "mode":
            case "chatmode":
            case "redirect":
                if (mod.twitch == null || !mod.config.twitchEnabled.getBoolean()) throw new CommandException("Twitch chat is disabled!");
                if (args.length == 1) {
                    boolean redirectEnabled = mod.config.twitchMessageRedirectEnabled.getBoolean();
                    StreamUtils.addMessages(new String[] {
                            EnumChatFormatting.AQUA + "Current Minecraft chat mode: " + (redirectEnabled ? EnumChatFormatting.DARK_PURPLE + "Redirect to selected Twitch channel" : EnumChatFormatting.GREEN + "Send to Minecraft server"),
                            EnumChatFormatting.GRAY + "Use " + EnumChatFormatting.DARK_AQUA + "/twitch mode " + (redirectEnabled ? "minecraft" : "twitch") + EnumChatFormatting.GRAY + " to send new Minecraft messages to the " + (redirectEnabled ? "Minecraft server" : "currently selected Twitch channel")
                    });
                } else {
                    Boolean newState = StreamUtils.readStringAsBoolean(args[1]);
                    if (newState == null) {
                        switch (args[1].toLowerCase()) {
                            case "twitch":
                            case "t":
                                newState = true;
                                break;
                            case "minecraft":
                            case "mc":
                            case "m":
                                newState = false;
                                break;
                            default:
                                throw new CommandException("Invalid mode: " + args[1]);
                        }
                    }
                    mod.config.twitchMessageRedirectEnabled.set(newState);
                    mod.config.saveIfChanged();
                    StreamUtils.addMessage(EnumChatFormatting.AQUA + "Minecraft chat mode has been set to " + (newState ? EnumChatFormatting.DARK_PURPLE + "Redirect to selected Twitch channel" : EnumChatFormatting.GREEN + "Send to Minecraft server"));
                }
                break;
            case "formatting":
            case "allowformatting":
            case "chatformatting":
            case "format":
            case "allowformat":
            case "chatformat":
            case "cf":
                if (args.length == 1) {
                    boolean formattingAllowed = mod.config.allowFormatting.getBoolean();
                    boolean subOnlyFormatting = mod.config.subOnlyFormatting.getBoolean();
                    StreamUtils.addMessages(new String[]{
                            EnumChatFormatting.AQUA + "Chat formatting codes are currently " + (formattingAllowed ? (subOnlyFormatting ? EnumChatFormatting.GOLD + "for subscribers+ only" : EnumChatFormatting.GREEN + "enabled") : EnumChatFormatting.RED + "disabled"),
                            EnumChatFormatting.GRAY + "Use " + EnumChatFormatting.DARK_AQUA + "/twitch formatting " + (formattingAllowed ? "disable" : "enable") + EnumChatFormatting.GRAY + " to " + (formattingAllowed ? "disallow" : "allow") + " viewers to send formatted messages" + (formattingAllowed ? ", or use " + EnumChatFormatting.DARK_AQUA + "/twitch formatting "+ (subOnlyFormatting ? "enable" : "subonly") + EnumChatFormatting.GRAY + " to allow " + (subOnlyFormatting ? "everyone" : "only subs/vips/mods") + " to use formatting" : "") + "!"
                    });
                } else {
                    Boolean newState = StreamUtils.readStringAsBoolean(args[1]);
                    boolean newSubOnly;
                    if (newState == null) switch (args[1].toLowerCase()) {
                        case "subonly":
                        case "sub":
                        case "viponly":
                        case "vip":
                        case "modonly":
                        case "mod":
                            newSubOnly = true;
                            newState = true;
                            break;
                        case "everyone":
                        case "all":
                        case "anyone":
                            newSubOnly = false;
                            newState = true;
                            break;
                        default:
                            throw new CommandException("Invalid boolean value: " + args[1]);
                    } else newSubOnly = false;
                    mod.config.allowFormatting.set(newState);
                    mod.config.subOnlyFormatting.set(newSubOnly);
                    mod.config.saveIfChanged();
                    StreamUtils.addMessage(EnumChatFormatting.GREEN + (newSubOnly ? "Subscribers, VIPs & moderators" : "Viewers") + " are " + (newState ? "now" : "no longer") + " allowed to use formatting codes in their messages!");
                }
                break;
            case "sounds":
            case "sound":
            case "s":
                switch (args.length >= 2 ? args[1].toLowerCase() : "") {
                    case "msg":
                    case "message":
                        if (args.length == 2)
                            StreamUtils.addMessage(EnumChatFormatting.AQUA + "Sound effect on new twitch chat message is: " + (mod.config.playSoundOnMessage.getBoolean() ? EnumChatFormatting.GREEN + "Enabled" : EnumChatFormatting.RED + "Disabled"));
                        else {
                            Boolean newState = StreamUtils.readStringAsBoolean(args[2]);
                            if (newState == null)
                                throw new CommandException("Invalid boolean value" + args[2]);
                            else {
                                mod.config.playSoundOnMessage.set(newState);
                                mod.config.saveIfChanged();
                                StreamUtils.addMessage(EnumChatFormatting.GREEN + "Sound effect on new twitch chat message has been " + (newState ? "enabled" : "disabled") + "!");
                            }
                        }
                        break;
                    case "follow":
                    case "follower":
                    case "f":
                        if (args.length == 2)
                            StreamUtils.addMessage(EnumChatFormatting.AQUA + "Sound effect on new twitch follower is: " + (mod.config.playSoundOnFollow.getBoolean() ? EnumChatFormatting.GREEN + "Enabled" : EnumChatFormatting.RED + "Disabled"));
                        else {
                            Boolean newState = StreamUtils.readStringAsBoolean(args[2]);
                            if (newState == null)
                                throw new CommandException("Invalid boolean value" + args[2]);
                            else {
                                mod.config.playSoundOnFollow.set(newState);
                                mod.config.saveIfChanged();
                                StreamUtils.addMessage(EnumChatFormatting.GREEN + "Sound effect on new twitch follower has been " + (newState ? "enabled" : "disabled") + "!");
                            }
                        }
                        break;
                    case "messagevolume":
                    case "msgvolume":
                    case "mvolume":
                    case "messagev":
                    case "msgv":
                    case "mv":
                        if (args.length == 2)
                            StreamUtils.addMessage(EnumChatFormatting.AQUA + "Volume for message sound effects: " + EnumChatFormatting.GREEN + mod.config.messageSoundVolume.getDouble() * 100 + "%");
                        else {
                            double newState;
                            try {
                                newState = Double.parseDouble(args[2]);
                            } catch (NumberFormatException e) {
                                throw new CommandException("Invalid double value: " + args[2]);
                            }
                            if (newState > 1) newState /= 100;
                            mod.config.messageSoundVolume.set(newState);
                            mod.config.saveIfChanged();
                            StreamUtils.addMessage(EnumChatFormatting.GREEN + "Volume for message sound effects has been set to " + EnumChatFormatting.AQUA + EnumChatFormatting.BOLD + newState * 100 + "%");
                        }
                        break;
                    case "eventvolume":
                    case "eventsvolume":
                    case "evolume":
                    case "eventv":
                    case "eventsv":
                    case "ev":
                        if (args.length == 2)
                            StreamUtils.addMessage(EnumChatFormatting.AQUA + "Volume for event sound effects: " + EnumChatFormatting.GREEN + mod.config.eventSoundVolume.getDouble() * 100 + "%");
                        else {
                            double newState;
                            try {
                                newState = Double.parseDouble(args[2]);
                            } catch (NumberFormatException e) {
                                throw new CommandException("Invalid double value: " + args[2]);
                            }
                            if (newState > 1) newState /= 100;
                            mod.config.eventSoundVolume.set(newState);
                            mod.config.saveIfChanged();
                            StreamUtils.addMessage(EnumChatFormatting.GREEN + "Volume for event sound effects has been set to " + EnumChatFormatting.AQUA + EnumChatFormatting.BOLD + newState * 100 + "%");
                        }
                        break;
                    default:
                        StreamUtils.addMessages(sender, new String[]{
                                EnumChatFormatting.GREEN + "Usage of /twitch sounds:",
                                EnumChatFormatting.GRAY + "/twitch sounds message [enable/disable]"+EnumChatFormatting.WHITE+" - "+EnumChatFormatting.AQUA+"Enables/disables sound effect on new twitch chat message.",
                                EnumChatFormatting.GRAY + "/twitch sounds follow [enable/disable]"+EnumChatFormatting.WHITE+" - "+EnumChatFormatting.AQUA+"Enables/disables sound effect on new twitch follower.",
                                EnumChatFormatting.GRAY + "/twitch sounds messagevolume [new volume]"+EnumChatFormatting.WHITE+" - "+EnumChatFormatting.AQUA+"Controls the volume of sounds effects for new messages.",
                                EnumChatFormatting.GRAY + "/twitch sounds eventvolume [new volume]"+EnumChatFormatting.WHITE+" - "+EnumChatFormatting.AQUA+"Controls the volume of sounds effects for events like new followers."
                        });
                }
                break;
            case "event":
            case "events":
            case "e":
                switch (args.length >= 2 ? args[1].toLowerCase() : "") {
                    case "follow":
                    case "follower":
                    case "f":
                        if (args.length == 2)
                            StreamUtils.addMessage(EnumChatFormatting.AQUA + "Displaying of new channel followers is: " + (mod.config.followEventEnabled.getBoolean() ? EnumChatFormatting.GREEN + "Enabled" : EnumChatFormatting.RED + "Disabled"));
                        else {
                            Boolean newState = StreamUtils.readStringAsBoolean(args[2]);
                            if (newState == null)
                                throw new CommandException("Invalid boolean value" + args[2]);
                            else {
                                boolean oldState = mod.config.followEventEnabled.getBoolean();
                                mod.config.followEventEnabled.set(newState);
                                mod.config.saveIfChanged();
                                StreamUtils.addMessage(EnumChatFormatting.GREEN + "Displaying of new channel followers has been " + (newState ? "enabled" : "disabled") + "!");
                                List<String> channels = Arrays.asList(mod.config.twitchChannels.getStringList());
                                if (oldState != newState) {
                                    if (newState)
                                        mod.twitch.getClientHelper().enableFollowEventListener(channels);
                                    else
                                        mod.twitch.getClientHelper().disableFollowEventListener(channels);
                                }
                            }
                        }
                        break;
                    default:
                        StreamUtils.addMessages(sender, new String[]{
                                EnumChatFormatting.GREEN + "Usage of /twitch events:",
                                EnumChatFormatting.GRAY + "/twitch events follower [enable/disable]"+EnumChatFormatting.WHITE+" - "+EnumChatFormatting.AQUA+"Enables/disables displaying of new channel followers."
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
                        if (mod.twitchAsyncAction != null) throw new CommandException("An action for the Twitch Chat is currently pending, please wait.");
                        mod.asyncJoinTwitchChannel(channel);
                        StreamUtils.addMessage(EnumChatFormatting.GRAY + "Joining channel...");
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
                        if (mod.twitchAsyncAction != null) throw new CommandException("An action for the Twitch Chat is currently pending, please wait.");
                        mod.asyncLeaveTwitchChannel(channel);
                        StreamUtils.addMessage(EnumChatFormatting.GRAY + "Leaving channel...");
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
            case "ban":
                channel = mod.config.twitchSelectedChannel.getString();
                if (mod.twitch == null || !mod.config.twitchEnabled.getBoolean()) throw new CommandException("Twitch chat is disabled!");
                if (channel.length() == 0) throw new CommandException("No selected channel. Use /twitch channels select <channel> to select one.");
                if (args.length < 2) throw new CommandException("Missing required parameter: user to ban");
                mod.twitch.getChat().ban(channel, args[1], String.join(" ", Arrays.asList(args).subList(2, args.length)));
                StreamUtils.addMessage(EnumChatFormatting.GREEN + "Banned " + EnumChatFormatting.AQUA + EnumChatFormatting.BOLD + args[1] + EnumChatFormatting.GREEN + " from "  + EnumChatFormatting.AQUA + EnumChatFormatting.BOLD + channel + EnumChatFormatting.GREEN + "'s chat." + (args.length >= 3 ? " Reason: " + EnumChatFormatting.AQUA + EnumChatFormatting.BOLD + String.join(" ", Arrays.asList(args).subList(2, args.length)) : ""));
                break;
            case "unban":
                channel = mod.config.twitchSelectedChannel.getString();
                if (mod.twitch == null || !mod.config.twitchEnabled.getBoolean()) throw new CommandException("Twitch chat is disabled!");
                if (channel.length() == 0) throw new CommandException("No selected channel. Use /twitch channels select <channel> to select one.");
                if (args.length < 2) throw new CommandException("Missing required parameter: user to unban");
                mod.twitch.getChat().unban(channel, args[1]);
                StreamUtils.addMessage(EnumChatFormatting.GREEN + "Unbanned " + EnumChatFormatting.AQUA + EnumChatFormatting.BOLD + args[1] + EnumChatFormatting.GREEN + " from "  + EnumChatFormatting.AQUA + EnumChatFormatting.BOLD + channel + EnumChatFormatting.GREEN + "'s chat.");
                break;
            case "time":
            case "timeout":
                channel = mod.config.twitchSelectedChannel.getString();
                if (mod.twitch == null || !mod.config.twitchEnabled.getBoolean()) throw new CommandException("Twitch chat is disabled!");
                if (channel.length() == 0) throw new CommandException("No selected channel. Use /twitch channels select <channel> to select one.");
                if (args.length < 2) throw new CommandException("Missing required parameters: user to unban & time to timeout for");
                if (args.length < 3) throw new CommandException("Missing required parameter: time to timeout for");
                Duration dur;
                try {
                    dur = Duration.parse(args[2]);
                } catch (DateTimeParseException e) {
                    try {
                        dur = Duration.ofSeconds(Integer.parseInt(args[2]));
                    } catch (NumberFormatException ee) {
                        throw new CommandException("Could not parse " + args[2] + " to a Duration. Use a whole number of seconds or the ISO 8601 format.");
                    }
                }
                mod.twitch.getChat().timeout(channel, args[1], dur, String.join(" ", Arrays.asList(args).subList(3, args.length)));
                StreamUtils.addMessage(EnumChatFormatting.GREEN + "Timed out " + EnumChatFormatting.AQUA + EnumChatFormatting.BOLD + args[1] + EnumChatFormatting.GREEN + " from "  + EnumChatFormatting.AQUA + EnumChatFormatting.BOLD + channel + EnumChatFormatting.GREEN + "'s chat for " + dur.getSeconds() + " seconds." + (args.length >= 4 ? " Reason: " + EnumChatFormatting.AQUA + EnumChatFormatting.BOLD + String.join(" ", Arrays.asList(args).subList(3, args.length)) : ""));
                break;
            case "clearchat":
                channel = mod.config.twitchSelectedChannel.getString();
                if (mod.twitch == null || !mod.config.twitchEnabled.getBoolean()) throw new CommandException("Twitch chat is disabled!");
                if (channel.length() == 0) throw new CommandException("No selected channel. Use /twitch channels select <channel> to select one.");
                mod.twitch.getChat().clearChat(channel);
                StreamUtils.addMessage("" + EnumChatFormatting.AQUA + EnumChatFormatting.BOLD + channel + EnumChatFormatting.GREEN + "'s Twitch chat cleared. Use F3+D to clear your in-game chat.");
                break;
            case "delete":
                if (mod.twitch == null || !mod.config.twitchEnabled.getBoolean()) throw new CommandException("Twitch chat is disabled!");
                if (args.length < 2) throw new CommandException("Missing required parameters: channel & id of the message to delete");
                if (args.length < 3) throw new CommandException("Missing required parameter: id of the message to delete");
                mod.twitch.getChat().delete(args[1], args[2]);
                StreamUtils.addMessage(EnumChatFormatting.GREEN + "Message deleted.");
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
                        mod.httpShutdownScheduler = new Thread(new HTTPServerShutdownScheduler(mod));
                        mod.httpShutdownScheduler.start();
                    }
                } catch (Exception e) {
                    StreamUtils.addMessage(sender, EnumChatFormatting.RED+"Something went wrong while attempting to start an HTTP server for automatic token setting. Please manually set the token using "+EnumChatFormatting.GRAY+"/twitch settoken "+EnumChatFormatting.RED+"after generating.");
                }
                boolean opened = false;
                if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                    try {
                        Desktop.getDesktop().browse(new URI("https://id.twitch.tv/oauth2/authorize?response_type=token&client_id=q7s0qfrigoczrj1a1cltcebjx95q8g&redirect_uri=http://localhost:39571&scope=chat:read+chat:edit+channel:moderate"));
                        opened = true;
                    } catch (Exception ignored) {}
                }
                if (!opened) StreamUtils.addMessages(sender, new String[]{
                            EnumChatFormatting.GREEN+"Please open this link in your browser:",
                            EnumChatFormatting.GRAY+"https://id.twitch.tv/oauth2/authorize?response_type=token&client_id=q7s0qfrigoczrj1a1cltcebjx95q8g&redirect_uri=http://localhost:39571&scope=chat:read+chat:edit+channel:moderate"
                });
                else StreamUtils.addMessage(sender, EnumChatFormatting.GREEN+"Opening link in your browser...");
                StreamUtils.addMessage(sender, EnumChatFormatting.AQUA+"The token will be automatically saved if generated within 120 seconds.");
                break;
            case "revoketoken":
            case "removetoken":
            case "tokenleaked":
            case "deltoken":
            case "resettoken":
                StreamUtils.addMessage(EnumChatFormatting.GRAY + "Revoking your current token...");
                mod.asyncRevokeTwitchToken();
                break;
            default:
                throw new CommandException("Unknown subcommand: use /twitch help to see available subcommands.");
        }
    }

}

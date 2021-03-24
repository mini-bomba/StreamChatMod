package me.mini_bomba.streamchatmod.commands;

import com.github.twitch4j.chat.TwitchChat;
import com.mojang.realmsclient.gui.ChatFormatting;
import me.mini_bomba.streamchatmod.StreamChatMod;
import me.mini_bomba.streamchatmod.StreamUtils;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;

import java.awt.*;
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
                        ChatFormatting.GREEN + "Usage of /twitch:",
                        ChatFormatting.GRAY + "/twitch help"+ChatFormatting.WHITE+" - "+ChatFormatting.AQUA+"Shows this message",
                        ChatFormatting.GRAY + "/twitch enable"+ChatFormatting.WHITE+" - "+ChatFormatting.AQUA+"Enables the Twitch chat",
                        ChatFormatting.GRAY + "/twitch disable"+ChatFormatting.WHITE+" - "+ChatFormatting.AQUA+"Disables the Twitch chat",
                        ChatFormatting.GRAY + "/twitch channels"+ChatFormatting.WHITE+" - "+ChatFormatting.AQUA+"Manages joined Twitch chats",
                        ChatFormatting.GRAY + "/twitch token"+ChatFormatting.WHITE+" - "+ChatFormatting.AQUA+"Configures the token for Twitch",
                        ChatFormatting.GRAY + "/twitch gentoken"+ChatFormatting.WHITE+" - "+ChatFormatting.AQUA+"Opens a link to generate the Twitch token"
                });
                break;
            case "enable":
            case "on":
                if (mod.twitch != null) throw new CommandException("Twitch chat is already enabled!");
                if (!mod.config.isTwitchTokenSet()) throw new CommandException("Twitch token is not configured! Use /twitch token to configure it.");
                mod.config.twitchEnabled.set(true);
                mod.config.saveIfChanged();
                mod.startTwitch();
                StreamUtils.addMessage(sender, ChatFormatting.GREEN+"Enabled the Twitch Chat!");
                break;
            case "disable":
            case "off":
                if (mod.twitch == null) throw new CommandException("Twitch chat is already disabled!");
                mod.config.twitchEnabled.set(false);
                mod.config.saveIfChanged();
                mod.stopTwitch();
                StreamUtils.addMessage(sender, ChatFormatting.GREEN+"Disabled the Twitch Chat!");
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
                        StreamUtils.addMessage(sender, ChatFormatting.GREEN+"Joined "+channel+"'s chat!");
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
                        StreamUtils.addMessage(sender, ChatFormatting.GREEN+"Left "+channel+"'s chat!");
                        break;
                    case "list":
                    case "show":
                    case "ls":
                        if (chat == null) throw new CommandException("Please enable Twitch chat first!");
                        String[] channels = chat.getChannels().toArray(new String[0]).clone();
                        StreamUtils.addMessage(sender, ChatFormatting.GREEN+"Currently joined stream chats ("+channels.length+"):");
                        for (int i = 0; i < channels.length; i++) {
                            channels[i] = ChatFormatting.AQUA+"• "+channels[i];
                        }
                        StreamUtils.addMessages(sender, channels);
                        break;
                    case "select":
                    case "s":
                        if (chat == null) throw new CommandException("Please enable Twitch chat first!");
                        mod.config.twitchSelectedChannel.set(channel);
                        mod.config.saveIfChanged();
                        if (channel.equals("")) StreamUtils.addMessage(sender, ChatFormatting.GREEN+"Unselected the stream chat channel!");
                        else StreamUtils.addMessage(sender, ChatFormatting.GREEN+"Selected "+channel+"'s stream chat!");
                        break;
                    default:
                        StreamUtils.addMessages(sender, new String[]{
                                ChatFormatting.GREEN + "Usage of /twitch channels:",
                                ChatFormatting.GRAY + "/twitch channels join <channel name>"+ChatFormatting.WHITE+" - "+ChatFormatting.AQUA+"Joins the specified Twitch channel.",
                                ChatFormatting.GRAY + "/twitch channels leave <channel name>"+ChatFormatting.WHITE+" - "+ChatFormatting.AQUA+"Leaves the specified Twitch channel.",
                                ChatFormatting.GRAY + "/twitch channels list"+ChatFormatting.WHITE+" - "+ChatFormatting.AQUA+"Lists the joined Twitch channels.",
                                ChatFormatting.GRAY + "/twitch channels select [channel name]"+ChatFormatting.WHITE+" - "+ChatFormatting.AQUA+"Selects the specified channel to send messages to.",
                        });
                }
                break;
            case "token":
                if (args.length < 2) throw new CommandException("Missing required parameter: token. You can generate it by running /twitch gentoken");
                mod.config.setTwitchToken(args[1]);
                mod.config.saveIfChanged();
                StreamUtils.addMessage(sender, ChatFormatting.GREEN+"Twitch token was successfully updated!");
                break;
            case "gentoken":
            case "generatetoken":
            case "gettoken":
                boolean opened = false;
                if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                    try {
                        Desktop.getDesktop().browse(new URI("https://id.twitch.tv/oauth2/authorize?response_type=token&client_id=q7s0qfrigoczrj1a1cltcebjx95q8g&redirect_uri=http://localhost&scope=chat:read+chat:edit"));
                        opened = true;
                    } catch (Exception ignored) {}
                }
                if (!opened) StreamUtils.addMessages(sender, new String[]{
                            ChatFormatting.GREEN+"Please open this link in your browser:",
                            ChatFormatting.GRAY+"https://id.twitch.tv/oauth2/authorize?response_type=token&client_id=q7s0qfrigoczrj1a1cltcebjx95q8g&redirect_uri=http://localhost&scope=chat:read+chat:edit"
                });
                else StreamUtils.addMessage(sender, ChatFormatting.GREEN+"Opening link in your browser...");
                break;
            default:
                throw new CommandException("Unknown subcommand: use /twitch help to see available subcommands.");
        }
    }

}

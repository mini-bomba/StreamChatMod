package me.mini_bomba.streamchatmod.commands.subcommands;

import me.mini_bomba.streamchatmod.StreamChatMod;
import me.mini_bomba.streamchatmod.StreamUtils;
import me.mini_bomba.streamchatmod.commands.ICommandNode;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.EnumChatFormatting;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TwitchBanSubcommand extends TwitchSubcommand {

    public TwitchBanSubcommand(StreamChatMod mod, ICommandNode<TwitchSubcommand> parentCommand) {
        super(mod, parentCommand);
    }

    @Override
    public @NotNull List<TwitchSubcommand> getSubcommands() {
        return Collections.emptyList();
    }

    @Override
    public @NotNull String getSubcommandName() {
        return "ban";
    }

    @Override
    public @NotNull List<String> getSubcommandAliases() {
        return Collections.emptyList();
    }

    @Override
    public @NotNull String getSubcommandUsage() {
        return "ban <user> [reason]";
    }

    @Override
    public @NotNull String getDescription() {
        return "Bans the user in the currently selected channel";
    }

    @Override
    public TwitchSubcommandCategory getCategory() {
        return TwitchSubcommandCategory.MODERATION;
    }

    @Override
    public void processSubcommand(ICommandSender sender, String[] args) throws CommandException {
        String channel = mod.config.twitchSelectedChannel.getString();
        if (mod.twitch == null || !mod.config.twitchEnabled.getBoolean()) throw new CommandException("Twitch chat is disabled!");
        if (channel.length() == 0) throw new CommandException("No selected channel. Use /twitch channels select <channel> to select one.");
        if (args.length == 0) throw new CommandException("Missing required parameter: user to ban");
        mod.twitch.getChat().ban(channel, args[0], String.join(" ", Arrays.asList(args).subList(1, args.length)));
        StreamUtils.addMessage(EnumChatFormatting.GREEN + "Banned " + EnumChatFormatting.AQUA + EnumChatFormatting.BOLD + args[0] + EnumChatFormatting.GREEN + " from "  + EnumChatFormatting.AQUA + EnumChatFormatting.BOLD + channel + EnumChatFormatting.GREEN + "'s chat." + (args.length >= 2 ? " Reason: " + EnumChatFormatting.AQUA + EnumChatFormatting.BOLD + String.join(" ", Arrays.asList(args).subList(1, args.length)) : ""));
    }
}

package me.mini_bomba.streamchatmod.commands.subcommands;

import me.mini_bomba.streamchatmod.StreamChatMod;
import me.mini_bomba.streamchatmod.StreamUtils;
import me.mini_bomba.streamchatmod.commands.ICommandNode;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.EnumChatFormatting;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TwitchTimeoutSubcommand extends TwitchSubcommand {

    public TwitchTimeoutSubcommand(StreamChatMod mod, ICommandNode<TwitchSubcommand> parentCommand) {
        super(mod, parentCommand);
    }

    @Override
    public @NotNull List<TwitchSubcommand> getSubcommands() {
        return Collections.emptyList();
    }

    @Override
    public @NotNull String getSubcommandName() {
        return "timeout";
    }

    @Override
    public @NotNull List<String> getSubcommandAliases() {
        return Arrays.asList("time", "mute");
    }

    @Override
    public @NotNull String getSubcommandUsage() {
        return "timeout <user> <time> [reason]";
    }

    @Override
    public @NotNull String getDescription() {
        return "Timeouts the user in the currently selected channel";
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
        if (args.length == 0) throw new CommandException("Missing required parameters: user to unban & time to timeout for");
        if (args.length == 1) throw new CommandException("Missing required parameter: time to timeout for");
        Duration dur;
        try {
            dur = Duration.parse(args[1]);
        } catch (DateTimeParseException e) {
            try {
                dur = Duration.ofSeconds(Integer.parseInt(args[1]));
            } catch (NumberFormatException ee) {
                throw new CommandException("Could not parse " + args[1] + " to a Duration. Use a whole number of seconds or the ISO 8601 format.");
            }
        }
        mod.twitch.getChat().timeout(channel, args[0], dur, String.join(" ", Arrays.asList(args).subList(2, args.length)));
        StreamUtils.addMessage(EnumChatFormatting.GREEN + "Timed out " + EnumChatFormatting.AQUA + EnumChatFormatting.BOLD + args[0] + EnumChatFormatting.GREEN + " from "  + EnumChatFormatting.AQUA + EnumChatFormatting.BOLD + channel + EnumChatFormatting.GREEN + "'s chat for " + dur.getSeconds() + " seconds." + (args.length >= 3 ? " Reason: " + EnumChatFormatting.AQUA + EnumChatFormatting.BOLD + String.join(" ", Arrays.asList(args).subList(2, args.length)) : ""));
    }
}

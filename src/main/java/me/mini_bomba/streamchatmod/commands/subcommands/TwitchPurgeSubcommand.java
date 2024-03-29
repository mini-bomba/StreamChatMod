package me.mini_bomba.streamchatmod.commands.subcommands;

import me.mini_bomba.streamchatmod.StreamChatMod;
import me.mini_bomba.streamchatmod.StreamUtils;
import me.mini_bomba.streamchatmod.commands.ICommandNode;
import me.mini_bomba.streamchatmod.commands.IDrawsChatOutline;
import me.mini_bomba.streamchatmod.commands.IHasAutocomplete;
import me.mini_bomba.streamchatmod.commands.TwitchCommand;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.EnumChatFormatting;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TwitchPurgeSubcommand extends TwitchSubcommand implements IDrawsChatOutline, IHasAutocomplete {

    public TwitchPurgeSubcommand(StreamChatMod mod, ICommandNode<TwitchSubcommand> parentCommand) {
        super(mod, parentCommand);
    }

    @Override
    public @NotNull List<TwitchSubcommand> getSubcommands() {
        return Collections.emptyList();
    }

    @Override
    public @NotNull String getSubcommandName() {
        return "purge";
    }

    @Override
    public @NotNull List<String> getSubcommandAliases() {
        return Collections.emptyList();
    }

    @Override
    public @NotNull String getSubcommandUsage() {
        return "purge <user> [reason]";
    }

    @Override
    public @NotNull String getDescription() {
        return "Removes all user's messages from the currently selected channel";
    }

    @Override
    public TwitchSubcommandCategory getCategory() {
        return TwitchSubcommandCategory.MODERATION;
    }

    @Override
    public void processSubcommand(ICommandSender sender, String[] args) throws CommandException {
        String channel = mod.config.twitchSelectedChannel.getString();
        if (mod.twitch == null || !mod.config.twitchEnabled.getBoolean())
            throw new CommandException("Twitch chat is disabled!");
        if (channel.length() == 0)
            throw new CommandException("No selected channel. Use /twitch channels select <channel> to select one.");
        if (args.length == 0) throw new CommandException("Missing required parameter: user whose messages to remove");
        mod.twitch.getChat().timeout(channel, args[0], Duration.ofSeconds(1), String.join(" ", Arrays.asList(args).subList(1, args.length)));
        StreamUtils.addMessage(EnumChatFormatting.GREEN + "Removing " + EnumChatFormatting.BOLD + args[0] + EnumChatFormatting.GRAY + "'s messages from " + EnumChatFormatting.BOLD + channel + EnumChatFormatting.GRAY + "'s chat...");
    }

    @Override
    public void drawChatOutline(GuiChat gui, String[] args) {
        String channel = mod.config.twitchSelectedChannel.getString();
        if (mod.twitch == null || !mod.config.twitchEnabled.getBoolean())
            StreamUtils.drawChatWarning(gui, StreamUtils.RED, StreamUtils.BACKGROUND, EnumChatFormatting.RED + "Twitch chat is disabled!");
        else if (channel.length() == 0)
            StreamUtils.drawChatWarning(gui, StreamUtils.RED, StreamUtils.BACKGROUND, EnumChatFormatting.RED + "No Twitch channel selected!");
        else if (args.length == 0)
            StreamUtils.drawChatWarning(gui, StreamUtils.RED, StreamUtils.BACKGROUND, EnumChatFormatting.LIGHT_PURPLE + "Removing user messages from " + EnumChatFormatting.AQUA + channel + EnumChatFormatting.LIGHT_PURPLE + "'s chat " + EnumChatFormatting.RED + "(missing user whose messages to remove parameter)");
        else if (args.length == 1)
            StreamUtils.drawChatWarning(gui, StreamUtils.PURPLE, StreamUtils.BACKGROUND, EnumChatFormatting.LIGHT_PURPLE + "Removing " + EnumChatFormatting.AQUA + args[0] + EnumChatFormatting.LIGHT_PURPLE + "'s messages from " + EnumChatFormatting.AQUA + channel + EnumChatFormatting.LIGHT_PURPLE + "'s chat");
        else
            StreamUtils.drawChatWarning(gui, StreamUtils.PURPLE, StreamUtils.BACKGROUND, EnumChatFormatting.LIGHT_PURPLE + "Removing " + EnumChatFormatting.AQUA + args[0] + EnumChatFormatting.LIGHT_PURPLE + "'s messages from " + EnumChatFormatting.AQUA + channel + EnumChatFormatting.LIGHT_PURPLE + "'s chat with reason \"" + EnumChatFormatting.AQUA + String.join(" ", Arrays.asList(args).subList(1, args.length)) + EnumChatFormatting.LIGHT_PURPLE + "\"");
    }

    @Override
    public List<String> getAutocompletions(String[] args) {
        return TwitchCommand.moderationAutocompletions(mod, args);
    }
}

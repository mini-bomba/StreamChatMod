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

public class TwitchEnableSubcommand extends TwitchSubcommand {

    public TwitchEnableSubcommand(StreamChatMod mod, ICommandNode<TwitchSubcommand> parentCommand) {
        super(mod, parentCommand);
    }

    @Override
    public @NotNull List<TwitchSubcommand> getSubcommands() {
        return Collections.emptyList();
    }

    @Override
    public @NotNull String getSubcommandName() {
        return "enable";
    }

    @Override
    public @NotNull List<String> getSubcommandAliases() {
        return Arrays.asList("on", "start");
    }

    @Override
    public @NotNull String getSubcommandUsage() {
        return "enable";
    }

    @Override
    public @NotNull String getDescription() {
        return "Enables the Twitch chat";
    }

    @Override
    public TwitchSubcommandCategory getCategory() {
        return TwitchSubcommandCategory.SETUP;
    }

    @Override
    public void processSubcommand(ICommandSender sender, String[] args) throws CommandException {
        if (!mod.config.isTwitchTokenSet())
            throw new CommandException("Twitch token is not configured! Use /twitch token to configure it.");
        if (mod.twitch != null) throw new CommandException("Twitch chat is already enabled!");
        if (mod.isImportantActionScheduled())
            throw new CommandException("An important action for the Twitch Chat is currently pending, please wait.");
        mod.config.twitchEnabled.set(true);
        mod.config.saveIfChanged();
        mod.asyncStartTwitch();
        StreamUtils.addMessage(EnumChatFormatting.GRAY + "Starting Twitch Chat...");
    }
}

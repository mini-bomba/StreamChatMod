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

public class TwitchDisableSubcommand extends TwitchSubcommand {

    public TwitchDisableSubcommand(StreamChatMod mod, ICommandNode<TwitchSubcommand> parentCommand) {
        super(mod, parentCommand);
    }

    @Override
    public @NotNull List<TwitchSubcommand> getSubcommands() {
        return Collections.emptyList();
    }

    @Override
    public @NotNull String getSubcommandName() {
        return "disable";
    }

    @Override
    public @NotNull List<String> getSubcommandAliases() {
        return Arrays.asList("off", "stop");
    }

    @Override
    public @NotNull String getSubcommandUsage() {
        return "disable";
    }

    @Override
    public @NotNull String getDescription() {
        return "Disables the Twitch chat";
    }

    @Override
    public TwitchSubcommandCategory getCategory() {
        return TwitchSubcommandCategory.SETUP;
    }

    @Override
    public void processSubcommand(ICommandSender sender, String[] args) throws CommandException {
        if (mod.twitch == null && !mod.config.twitchEnabled.getBoolean()) throw new CommandException("Twitch chat is already disabled!");
        if (mod.twitchAsyncAction != null) throw new CommandException("An action for the Twitch Chat is currently pending, please wait.");
        mod.config.twitchEnabled.set(false);
        mod.config.saveIfChanged();
        mod.asyncStopTwitch();
        StreamUtils.addMessage(EnumChatFormatting.GRAY + "Stopping Twitch Chat...");
    }
}

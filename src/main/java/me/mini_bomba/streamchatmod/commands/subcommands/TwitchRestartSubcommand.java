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

public class TwitchRestartSubcommand extends TwitchSubcommand {

    public TwitchRestartSubcommand(StreamChatMod mod, ICommandNode<TwitchSubcommand> parentCommand) {
        super(mod, parentCommand);
    }

    @Override
    public @NotNull List<TwitchSubcommand> getSubcommands() {
        return Collections.emptyList();
    }

    @Override
    public @NotNull String getSubcommandName() {
        return "restart";
    }

    @Override
    public @NotNull List<String> getSubcommandAliases() {
        return Arrays.asList("reload", "r");
    }

    @Override
    public @NotNull String getSubcommandUsage() {
        return "restart";
    }

    @Override
    public @NotNull String getDescription() {
        return "Restarts the Twitch chat";
    }

    @Override
    public TwitchSubcommandCategory getCategory() {
        return TwitchSubcommandCategory.SETUP;
    }

    @Override
    public void processSubcommand(ICommandSender sender, String[] args) throws CommandException {
        if (!mod.config.isTwitchTokenSet()) throw new CommandException("Twitch token is not configured! Use /twitch token to configure it.");
        if (!mod.config.twitchEnabled.getBoolean()) throw new CommandException("Twitch chat is not enabled!");
        if (mod.twitchAsyncAction != null) throw new CommandException("An action for the Twitch Chat is currently pending, please wait.");
        mod.asyncRestartTwitch();
        StreamUtils.addMessage(EnumChatFormatting.GRAY + "Restarting Twitch Chat...");
    }
}

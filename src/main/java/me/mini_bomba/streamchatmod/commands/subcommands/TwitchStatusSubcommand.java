package me.mini_bomba.streamchatmod.commands.subcommands;

import me.mini_bomba.streamchatmod.StreamChatMod;
import me.mini_bomba.streamchatmod.commands.ICommandNode;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class TwitchStatusSubcommand extends TwitchSubcommand {

    public TwitchStatusSubcommand(StreamChatMod mod, ICommandNode<TwitchSubcommand> parentCommand) {
        super(mod, parentCommand);
    }

    @Override
    public @NotNull List<TwitchSubcommand> getSubcommands() {
        return Collections.emptyList();
    }

    @Override
    public @NotNull String getSubcommandName() {
        return "status";
    }

    @Override
    public @NotNull List<String> getSubcommandAliases() {
        return Collections.emptyList();
    }

    @Override
    public @NotNull String getSubcommandUsage() {
        return "status";
    }

    @Override
    public @NotNull String getDescription() {
        return "Shows the status of the mod";
    }

    @Override
    public TwitchSubcommandCategory getCategory() {
        return TwitchSubcommandCategory.GENERAL;
    }

    @Override
    public void processSubcommand(ICommandSender sender, String[] args) throws CommandException {
        mod.printTwitchStatus();
    }
}

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

public class TwitchRevokeTokenSubcommand extends TwitchSubcommand {

    public TwitchRevokeTokenSubcommand(StreamChatMod mod, ICommandNode<TwitchSubcommand> parentCommand) {
        super(mod, parentCommand);
    }

    @Override
    public @NotNull List<TwitchSubcommand> getSubcommands() {
        return Collections.emptyList();
    }

    @Override
    public @NotNull String getSubcommandName() {
        return "revoketoken";
    }

    @Override
    public @NotNull List<String> getSubcommandAliases() {
        return Arrays.asList("removetoken", "tokenleaked", "deltoken", "resettoken", "notlikethis");
    }

    @Override
    public @NotNull String getSubcommandUsage() {
        return "revoketoken";
    }

    @Override
    public @NotNull String getDescription() {
        return "Revoke the currently set token & removes it from the config. You can run this if you leak your current token.";
    }

    @Override
    public TwitchSubcommandCategory getCategory() {
        return TwitchSubcommandCategory.SETUP;
    }

    @Override
    public void processSubcommand(ICommandSender sender, String[] args) throws CommandException {
        StreamUtils.addMessage(EnumChatFormatting.GRAY + "Revoking your current token...");
        mod.asyncRevokeTwitchToken();
    }
}

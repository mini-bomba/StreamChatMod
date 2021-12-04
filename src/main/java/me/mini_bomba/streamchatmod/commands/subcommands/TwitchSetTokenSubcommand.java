package me.mini_bomba.streamchatmod.commands.subcommands;

import me.mini_bomba.streamchatmod.StreamChatMod;
import me.mini_bomba.streamchatmod.StreamUtils;
import me.mini_bomba.streamchatmod.commands.ICommandNode;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.EnumChatFormatting;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class TwitchSetTokenSubcommand extends TwitchSubcommand {

    public TwitchSetTokenSubcommand(StreamChatMod mod, ICommandNode<TwitchSubcommand> parentCommand) {
        super(mod, parentCommand);
    }

    @Override
    public @NotNull List<TwitchSubcommand> getSubcommands() {
        return Collections.emptyList();
    }

    @Override
    public @NotNull String getSubcommandName() {
        return "settoken";
    }

    @Override
    public @NotNull List<String> getSubcommandAliases() {
        return Collections.emptyList();
    }

    @Override
    public @NotNull String getSubcommandUsage() {
        return "settoken <token>";
    }

    @Override
    public @NotNull String getDescription() {
        return "Manually set the token for Twitch if /twitch token fails to automatically set it";
    }

    @Override
    public TwitchSubcommandCategory getCategory() {
        return TwitchSubcommandCategory.SETUP;
    }

    @Override
    public boolean hasParameters() {
        return true;
    }

    @Override
    public void processSubcommand(ICommandSender sender, String[] args) throws CommandException {
        if (args.length == 0)
            throw new CommandException("Missing required parameter: token. You can generate it by running /twitch token");
        mod.config.setTwitchToken(args[0]);
        mod.config.saveIfChanged();
        StreamUtils.addMessage(sender, EnumChatFormatting.GREEN + "Twitch token was successfully updated!");
        if (mod.config.twitchEnabled.getBoolean()) {
            StreamUtils.addMessage(sender, EnumChatFormatting.GRAY + "Restarting Twitch Chat...");
            mod.asyncRestartTwitch();
        }
    }
}

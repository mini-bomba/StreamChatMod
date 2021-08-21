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

public class TwitchSoundsFollowerSubcommand extends TwitchSubcommand {

    public TwitchSoundsFollowerSubcommand(StreamChatMod mod, ICommandNode<TwitchSubcommand> parentCommand) {
        super(mod, parentCommand);
    }

    @Override
    public @NotNull List<TwitchSubcommand> getSubcommands() {
        return Collections.emptyList();
    }

    @Override
    public @NotNull String getSubcommandName() {
        return "follower";
    }

    @Override
    public @NotNull List<String> getSubcommandAliases() {
        return Arrays.asList("follow", "f", "followers");
    }

    @Override
    public @NotNull String getSubcommandUsage() {
        return "follower [enable/disable]";
    }

    @Override
    public @NotNull String getDescription() {
        return "Enables/disables sound effect on new twitch chat message";
    }

    @Override
    public TwitchSubcommandCategory getCategory() {
        return null;
    }

    @Override
    public void processSubcommand(ICommandSender sender, String[] args) throws CommandException {
        if (args.length == 0)
            StreamUtils.addMessage(EnumChatFormatting.AQUA + "Sound effect on new twitch follower is: " + (mod.config.playSoundOnFollow.getBoolean() ? EnumChatFormatting.GREEN + "Enabled" : EnumChatFormatting.RED + "Disabled"));
        else {
            Boolean newState = StreamUtils.readStringAsBoolean(args[0]);
            if (newState == null)
                throw new CommandException("Invalid boolean value: " + args[0]);
            else {
                mod.config.playSoundOnFollow.set(newState);
                mod.config.saveIfChanged();
                StreamUtils.addMessage(EnumChatFormatting.GREEN + "Sound effect on new twitch follower has been " + (newState ? "enabled" : "disabled") + "!");
            }
        }
    }
}

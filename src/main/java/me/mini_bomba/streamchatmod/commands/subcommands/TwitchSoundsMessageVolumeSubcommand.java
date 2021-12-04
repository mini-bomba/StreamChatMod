package me.mini_bomba.streamchatmod.commands.subcommands;

import me.mini_bomba.streamchatmod.StreamChatMod;
import me.mini_bomba.streamchatmod.StreamUtils;
import me.mini_bomba.streamchatmod.commands.ICommandNode;
import me.mini_bomba.streamchatmod.commands.IHasAutocomplete;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.EnumChatFormatting;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TwitchSoundsMessageVolumeSubcommand extends TwitchSubcommand implements IHasAutocomplete {

    public TwitchSoundsMessageVolumeSubcommand(StreamChatMod mod, ICommandNode<TwitchSubcommand> parentCommand) {
        super(mod, parentCommand);
    }

    @Override
    public @NotNull List<TwitchSubcommand> getSubcommands() {
        return Collections.emptyList();
    }

    @Override
    public @NotNull String getSubcommandName() {
        return "messagevolume";
    }

    @Override
    public @NotNull List<String> getSubcommandAliases() {
        return Arrays.asList("msgvolume", "mvolume", "messagev", "msgv", "mv");
    }

    @Override
    public @NotNull String getSubcommandUsage() {
        return "messagevolume [new-volume]";
    }

    @Override
    public @NotNull String getDescription() {
        return "Controls the volume of sounds effects for new messages";
    }

    @Override
    public TwitchSubcommandCategory getCategory() {
        return null;
    }

    @Override
    public void processSubcommand(ICommandSender sender, String[] args) throws CommandException {
        if (args.length == 0)
            StreamUtils.addMessage(EnumChatFormatting.AQUA + "Volume for message sound effects: " + EnumChatFormatting.GREEN + mod.config.messageSoundVolume.getDouble() * 100 + "%");
        else {
            double newState;
            try {
                newState = Double.parseDouble(args[0]);
            } catch (NumberFormatException e) {
                throw new CommandException("Invalid double value: " + args[0]);
            }
            if (newState > 1) newState /= 100;
            mod.config.messageSoundVolume.set(newState);
            mod.config.saveIfChanged();
            StreamUtils.addMessage(EnumChatFormatting.GREEN + "Volume for message sound effects has been set to " + EnumChatFormatting.AQUA + EnumChatFormatting.BOLD + newState * 100 + "%");
        }
    }

    @Override
    public List<String> getAutocompletions(String[] args) {
        if (args.length > 1 || args[0].length() > 0) return null;
        return StreamUtils.singletonModifiableList(String.valueOf(mod.config.messageSoundVolume.getDouble() * 100));
    }
}

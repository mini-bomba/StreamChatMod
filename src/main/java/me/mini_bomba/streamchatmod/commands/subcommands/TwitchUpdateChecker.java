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
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TwitchUpdateChecker extends TwitchSubcommand implements IHasAutocomplete {

    public TwitchUpdateChecker(StreamChatMod mod, ICommandNode<TwitchSubcommand> parentCommand) {
        super(mod, parentCommand);
    }

    @Override
    public @NotNull List<TwitchSubcommand> getSubcommands() {
        return Collections.emptyList();
    }

    @Override
    public @NotNull String getSubcommandName() {
        return "updatechecker";
    }

    @Override
    public @NotNull List<String> getSubcommandAliases() {
        return Arrays.asList("updates", "updater", "update", "updateschecker", "uc");
    }

    @Override
    public @NotNull String getSubcommandUsage() {
        return "updatechecker [enable/disable]";
    }

    @Override
    public @NotNull String getDescription() {
        return "Enables/disables the runtime update checker";
    }

    @Override
    public TwitchSubcommandCategory getCategory() {
        return TwitchSubcommandCategory.CONFIG;
    }

    @Override
    public void processSubcommand(ICommandSender sender, String[] args) throws CommandException {
        if (args.length == 0)
            StreamUtils.addMessage(EnumChatFormatting.AQUA + "The runtime update checker is: " + (mod.config.updateCheckerEnabled.getBoolean() ? EnumChatFormatting.GREEN + "Enabled" : EnumChatFormatting.RED + "Disabled"));
        else {
            Boolean newState = StreamUtils.readStringAsBoolean(args[0]);
            if (newState == null)
                throw new CommandException("Invalid boolean value" + args[0]);
            mod.config.updateCheckerEnabled.set(newState);
            mod.config.saveIfChanged();
            StreamUtils.addMessage(EnumChatFormatting.GREEN + "The runtime update checker has been " + (newState ? "enabled" : "disabled") + "!");
            if (newState)
                mod.startUpdateChecker(true);
            else
                mod.stopUpdateChecker();
        }
    }

    @Override
    public List<String> getAutocompletions(String[] args) {
        if (args.length > 1) return null;
        return Stream.of("enable", "disable").filter(s -> s.startsWith(args[0])).collect(Collectors.toList());
    }
}

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

public class TwitchEventsFollowSubcommand extends TwitchSubcommand implements IHasAutocomplete {

    public TwitchEventsFollowSubcommand(StreamChatMod mod, ICommandNode<TwitchSubcommand> parentCommand) {
        super(mod, parentCommand);
    }

    @Override
    public @NotNull List<TwitchSubcommand> getSubcommands() {
        return Collections.emptyList();
    }

    @Override
    public @NotNull String getSubcommandName() {
        return "follow";
    }

    @Override
    public @NotNull List<String> getSubcommandAliases() {
        return Arrays.asList("follower", "followers", "f");
    }

    @Override
    public @NotNull String getSubcommandUsage() {
        return "follow [enable/disable]";
    }

    @Override
    public @NotNull String getDescription() {
        return "Enables/disables displaying of new channel followers";
    }

    @Override
    public TwitchSubcommandCategory getCategory() {
        return null;
    }

    @Override
    public void processSubcommand(ICommandSender sender, String[] args) throws CommandException {
        if (args.length == 0)
            StreamUtils.addMessage(EnumChatFormatting.AQUA + "Displaying of new channel followers is: " + (mod.config.followEventEnabled.getBoolean() ? EnumChatFormatting.GREEN + "Enabled" : EnumChatFormatting.RED + "Disabled"));
        else {
            if (mod.isImportantActionScheduled())
                throw new CommandException("An important action for the Twitch Chat is currently pending, please wait.");
            Boolean newState = StreamUtils.readStringAsBoolean(args[0]);
            if (newState == null)
                throw new CommandException("Invalid boolean value" + args[0]);
            boolean oldState = mod.config.followEventEnabled.getBoolean();
            mod.config.followEventEnabled.set(newState);
            mod.config.saveIfChanged();
            StreamUtils.addMessage(EnumChatFormatting.GREEN + "Displaying of new channel followers has been " + (newState ? "enabled" : "disabled") + "!");
            if (oldState != newState && mod.twitch != null) {
                StreamUtils.addMessage(EnumChatFormatting.GRAY + "Updating listeners...");
                mod.asyncUpdateFollowEvents();
            }
        }
    }

    @Override
    public List<String> getAutocompletions(String[] args) {
        if (args.length > 1) return null;
        return Stream.of("enable", "disable").filter(s -> s.startsWith(args[0])).collect(Collectors.toList());
    }
}

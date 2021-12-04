package me.mini_bomba.streamchatmod.commands.subcommands;

import me.mini_bomba.streamchatmod.StreamChatMod;
import me.mini_bomba.streamchatmod.StreamUtils;
import me.mini_bomba.streamchatmod.commands.ICommandNode;
import me.mini_bomba.streamchatmod.commands.IHasAutocomplete;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.event.ClickEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class TwitchEventsSubcommand extends TwitchSubcommand implements IHasAutocomplete {
    private final List<TwitchSubcommand> subcommands;
    private final Map<String, TwitchSubcommand> subcommandMap;
    private final Map<String, IHasAutocomplete> subcommandMapWithAutocomplete;
    private final List<String> subcommandNames;
    private final List<String> autocompletions;

    public TwitchEventsSubcommand(StreamChatMod mod, ICommandNode<TwitchSubcommand> parentCommand) {
        super(mod, parentCommand);
        subcommands = Collections.singletonList(new TwitchEventsFollowSubcommand(mod, this));
        // Create param -> subcommand map
        subcommandMap = createNameMap();
        subcommandMapWithAutocomplete = createAutcompletableMap(subcommandMap);
        subcommandNames = createNameList();
        autocompletions = createAutocompletionList();
    }

    @Override
    public @NotNull List<TwitchSubcommand> getSubcommands() {
        return subcommands;
    }

    @Override
    public @NotNull String getSubcommandName() {
        return "events";
    }

    @Override
    public @NotNull List<String> getSubcommandAliases() {
        return Arrays.asList("event", "e");
    }

    @Override
    public @NotNull String getSubcommandUsage() {
        return "events ...";
    }

    @Override
    public @NotNull String getDescription() {
        return "Manages enabled events";
    }

    @Override
    public TwitchSubcommandCategory getCategory() {
        return TwitchSubcommandCategory.CONFIG;
    }

    @Override
    public boolean hasParameters() {
        return true;
    }

    @Override
    public void processSubcommand(ICommandSender sender, String[] args) throws CommandException {
        String cmdName = args.length == 0 ? null : args[0].toLowerCase();
        if (args.length == 0 || !subcommandMap.containsKey(cmdName)) {
            List<IChatComponent> components = new ArrayList<>();
            components.add(new ChatComponentText(EnumChatFormatting.GREEN + "Subcommand list of /twitch events:"));
            components.addAll(getSubcommands().stream().map(cmd -> new ChatComponentText(EnumChatFormatting.GRAY + "/twitch events " + cmd.getSubcommandUsage() + EnumChatFormatting.WHITE + " - " + EnumChatFormatting.AQUA + cmd.getDescription())
                    .setChatStyle(new ChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/twitch events " + cmd.getSubcommandUsage())))).collect(Collectors.toList()));
            StreamUtils.addMessages(sender, components.toArray(new IChatComponent[0]));
        } else subcommandMap.get(cmdName).processSubcommand(sender, Arrays.copyOfRange(args, 1, args.length));
    }

    @Override
    public List<String> getAutocompletions(String[] args) {
        return getAutocompletions(args, subcommandMap, subcommandMapWithAutocomplete, subcommandNames, autocompletions);
    }
}

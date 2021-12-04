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

public class TwitchChannelSubcommand extends TwitchSubcommand implements IHasAutocomplete {
    private final List<TwitchSubcommand> subcommands;
    private final Map<String, TwitchSubcommand> subcommandMap;
    private final Map<String, IHasAutocomplete> subcommandMapWithAutocomplete;
    private final List<String> subcommandNames;
    private final List<String> autocompletions;

    public TwitchChannelSubcommand(StreamChatMod mod, ICommandNode<TwitchSubcommand> parentCommand) {
        super(mod, parentCommand);
        subcommands = Collections.unmodifiableList(Arrays.asList(
                new TwitchChannelJoinSubcommand(mod, this),
                new TwitchChannelLeaveSubcommand(mod, this),
                new TwitchChannelListSubcommand(mod, this),
                new TwitchChannelSelectSubcommand(mod, this)
        ));
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
        return "channel";
    }

    @Override
    public @NotNull List<String> getSubcommandAliases() {
        return Arrays.asList("channels", "chat", "chats", "c");
    }

    @Override
    public @NotNull String getSubcommandUsage() {
        return "channel ...";
    }

    @Override
    public @NotNull String getDescription() {
        return "Manages joined Twitch chats";
    }

    @Override
    public TwitchSubcommandCategory getCategory() {
        return TwitchSubcommandCategory.CONFIG;
    }

    @Override
    public void processSubcommand(ICommandSender sender, String[] args) throws CommandException {
        String cmdName = args.length == 0 ? null : args[0].toLowerCase();
        if (args.length == 0 || !subcommandMap.containsKey(cmdName)) {
            List<IChatComponent> components = new ArrayList<>();
            components.add(new ChatComponentText(EnumChatFormatting.GREEN + "Subcommand list of /twitch channels:"));
            components.addAll(getSubcommands().stream().map(cmd -> new ChatComponentText(EnumChatFormatting.GRAY + "/twitch channel " + cmd.getSubcommandUsage() + EnumChatFormatting.WHITE + " - " + EnumChatFormatting.AQUA + cmd.getDescription())
                    .setChatStyle(new ChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/twitch channel " + cmd.getSubcommandUsage())))).collect(Collectors.toList()));
            StreamUtils.addMessages(sender, components.toArray(new IChatComponent[0]));
        } else subcommandMap.get(cmdName).processSubcommand(sender, Arrays.copyOfRange(args, 1, args.length));
    }

    @Override
    public List<String> getAutocompletions(String[] args) {
        return getAutocompletions(args, subcommandMap, subcommandMapWithAutocomplete, subcommandNames, autocompletions);
    }
}

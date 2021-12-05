package me.mini_bomba.streamchatmod.commands;

import com.github.twitch4j.tmi.domain.Chatters;
import me.mini_bomba.streamchatmod.StreamChatMod;
import me.mini_bomba.streamchatmod.commands.subcommands.*;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TwitchCommand extends CommandBase implements ICommandNode<TwitchSubcommand> {
    private final StreamChatMod mod;
    public final List<TwitchSubcommand> subcommands;
    public final Map<String, TwitchSubcommand> subcommandMap;
    public final Map<String, IDrawsChatOutline> subcommandMapWithChatOutlines;
    public final Map<String, IHasAutocomplete> subcommandMapWithAutocomplete;
    public final List<String> subcommandNames;
    public final List<String> autocompletions;

    public TwitchCommand(StreamChatMod mod) {
        this.mod = mod;
        subcommands = Collections.unmodifiableList(Arrays.asList(
                new TwitchHelpSubcommand(mod, this),
                new TwitchStatusSubcommand(mod, this),
                new TwitchModeSubcommand(mod, this),
                new TwitchEnableSubcommand(mod, this),
                new TwitchDisableSubcommand(mod, this),
                new TwitchRestartSubcommand(mod, this),
                new TwitchTokenSubcommand(mod, this),
                new TwitchSetTokenSubcommand(mod, this),
                new TwitchRevokeTokenSubcommand(mod, this),
                new TwitchChannelSubcommand(mod, this),
                new TwitchSoundsSubcommand(mod, this),
                new TwitchEventsSubcommand(mod, this),
                new TwitchFormattingSubcommand(mod, this),
                new TwitchMcChatPrefixSubcommand(mod, this),
                new TwitchBanSubcommand(mod, this),
                new TwitchUnbanSubcommand(mod, this),
                new TwitchTimeoutSubcommand(mod, this),
                new TwitchClearChatSubcommand(mod, this),
                new TwitchDeleteMessageSubcommand(mod, this),
                new TwitchMarkerSubcommand(mod, this),
                new TwitchClipSubcommand(mod, this),
                new TwitchStatsSubcommand(mod, this)
        ));
        // Create param -> subcommand map
        subcommandMap = Subcommand.createNameMap(subcommands);
        Map<String, IDrawsChatOutline> tempMap1 = new HashMap<>();
        Map<String, IHasAutocomplete> tempMap2 = new HashMap<>();
        for (String key : subcommandMap.keySet()) {
            TwitchSubcommand subcommand = subcommandMap.get(key);
            if (subcommand instanceof IDrawsChatOutline)
                tempMap1.put(key, (IDrawsChatOutline) subcommand);
            if (subcommand instanceof IHasAutocomplete)
                tempMap2.put(key, (IHasAutocomplete) subcommand);
        }
        subcommandMapWithChatOutlines = Collections.unmodifiableMap(tempMap1);
        subcommandMapWithAutocomplete = Collections.unmodifiableMap(tempMap2);
        subcommandNames = Subcommand.createNameList(subcommands);
        autocompletions = Subcommand.createAutocompletionList(subcommands);
    }

    @Override
    public String getCommandName() {
        return "twitch";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/twitch ...";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public ICommandNode<TwitchSubcommand> getParentSubcommand() {
        return null;
    }

    @Override
    public @NotNull List<TwitchSubcommand> getSubcommands() {
        return subcommands;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        if (args.length == 0)
            subcommandMap.get("help").processSubcommand(sender, new String[0]);
        else {
            String cmdName = args[0].toLowerCase();
            if (!subcommandMap.containsKey(cmdName))
                throw new CommandException("Unknown subcommand: use /twitch help to see available subcommands.");
            subcommandMap.get(cmdName).processSubcommand(sender, Arrays.copyOfRange(args, 1, args.length));
        }
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
        return Subcommand.getAutocompletions(args, subcommandMap, subcommandMapWithAutocomplete, subcommandNames, autocompletions);
    }

    public static List<String> moderationAutocompletions(StreamChatMod mod, String[] args) {
        if (mod.twitch == null || !mod.config.twitchEnabled.getBoolean() || args.length > 1) return null;
        Chatters chatters = mod.getChatters(mod.config.twitchSelectedChannel.getString());
        if (chatters == null) return null;
        return Stream.concat(chatters.getViewers().stream(), chatters.getVips().stream()).filter(user -> user.startsWith(args[0])).collect(Collectors.toList());
    }
}

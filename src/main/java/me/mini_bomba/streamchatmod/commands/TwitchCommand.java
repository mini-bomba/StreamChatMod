package me.mini_bomba.streamchatmod.commands;

import me.mini_bomba.streamchatmod.StreamChatMod;
import me.mini_bomba.streamchatmod.commands.subcommands.*;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class TwitchCommand extends CommandBase implements ICommandNode<TwitchSubcommand> {
    private final StreamChatMod mod;
    public final List<TwitchSubcommand> subcommands;
    public final Map<String, TwitchSubcommand> subcommandMap;
    public final Map<String, TwitchSubcommandWithOutline> subcommandMapWithChatOutlines;

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
                new TwitchDeleteMessageSubcommand(mod, this)
                ));
        // Create param -> subcommand map
        subcommandMap = Subcommand.createNameMap(subcommands);
        Map<String, TwitchSubcommandWithOutline> tempMap = new HashMap<>();
        for (String key : subcommandMap.keySet())
            if (subcommandMap.get(key) instanceof TwitchSubcommandWithOutline)
                tempMap.put(key, (TwitchSubcommandWithOutline) subcommandMap.get(key));
        subcommandMapWithChatOutlines = Collections.unmodifiableMap(tempMap);
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
}

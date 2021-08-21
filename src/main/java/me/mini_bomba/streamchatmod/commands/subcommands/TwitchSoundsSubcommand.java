package me.mini_bomba.streamchatmod.commands.subcommands;

import me.mini_bomba.streamchatmod.StreamChatMod;
import me.mini_bomba.streamchatmod.StreamUtils;
import me.mini_bomba.streamchatmod.commands.ICommandNode;
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

public class TwitchSoundsSubcommand extends TwitchSubcommand {
    private final List<TwitchSubcommand> subcommands;
    private final Map<String, TwitchSubcommand> subcommandMap;

    public TwitchSoundsSubcommand(StreamChatMod mod, ICommandNode<TwitchSubcommand> parentCommand) {
        super(mod, parentCommand);
        subcommands = Collections.unmodifiableList(Arrays.asList(
            new TwitchSoundsMessageSubcommand(mod, this),
            new TwitchSoundsFollowerSubcommand(mod, this),
            new TwitchSoundsMessageVolumeSubcommand(mod, this),
            new TwitchSoundsEventVolumeSubcommand(mod, this)
        ));
        // Create param -> subcommand map
        subcommandMap = createNameMap();
    }

    @Override
    public @NotNull List<TwitchSubcommand> getSubcommands() {
        return subcommands;
    }

    @Override
    public @NotNull String getSubcommandName() {
        return "sounds";
    }

    @Override
    public @NotNull List<String> getSubcommandAliases() {
        return Arrays.asList("sound", "s");
    }

    @Override
    public @NotNull String getSubcommandUsage() {
        return "sounds ...";
    }

    @Override
    public @NotNull String getDescription() {
        return "Manages enabled sounds";
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
            components.add(new ChatComponentText(EnumChatFormatting.GREEN + "Subcommand list of /twitch sounds:"));
            components.addAll(getSubcommands().stream().map(cmd -> new ChatComponentText(EnumChatFormatting.GRAY+"/twitch sounds "+cmd.getSubcommandUsage()+EnumChatFormatting.WHITE+" - "+EnumChatFormatting.AQUA+cmd.getDescription())
                    .setChatStyle(new ChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/twitch sounds "+cmd.getSubcommandUsage())))).collect(Collectors.toList()));
            StreamUtils.addMessages(sender, components.toArray(new IChatComponent[0]));
        } else subcommandMap.get(cmdName).processSubcommand(sender, Arrays.copyOfRange(args, 1, args.length));
    }
}

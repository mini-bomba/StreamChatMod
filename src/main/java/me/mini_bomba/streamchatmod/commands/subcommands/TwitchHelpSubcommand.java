package me.mini_bomba.streamchatmod.commands.subcommands;

import me.mini_bomba.streamchatmod.StreamChatMod;
import me.mini_bomba.streamchatmod.StreamUtils;
import me.mini_bomba.streamchatmod.commands.ICommandNode;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TwitchHelpSubcommand extends TwitchSubcommand {

    public TwitchHelpSubcommand(StreamChatMod mod, ICommandNode<TwitchSubcommand> parentCommand) {
        super(mod, parentCommand);
    }

    @Override
    public @NotNull List<TwitchSubcommand> getSubcommands() {
        return Collections.emptyList();
    }

    @Override
    public @NotNull String getSubcommandName() {
        return "help";
    }

    @Override
    public @NotNull List<String> getSubcommandAliases() {
        return Arrays.asList("?", "h");
    }

    @Override
    public @NotNull String getSubcommandUsage() {
        return "help [category]";
    }

    @Override
    public @NotNull String getDescription() {
        return "Lists all subcommands of /twitch";
    }

    @Override
    public TwitchSubcommandCategory getCategory() {
        return TwitchSubcommandCategory.GENERAL;
    }

    @Override
    public void processSubcommand(ICommandSender sender, String[] args) throws CommandException {
        if (args.length == 0) {
            IChatComponent[] components = new IChatComponent[]{
                    new ChatComponentText(EnumChatFormatting.GREEN + "Subcommand category list of the /twitch command:"),
                    new ChatComponentText(EnumChatFormatting.GRAY + "General commands "+EnumChatFormatting.DARK_GRAY+"(/twitch help general)"),
                    new ChatComponentText(EnumChatFormatting.GRAY + "Setup commands "+EnumChatFormatting.DARK_GRAY+"(/twitch help setup)"),
                    new ChatComponentText(EnumChatFormatting.GRAY + "Configuration commands "+EnumChatFormatting.DARK_GRAY+"(/twitch help config)"),
                    new ChatComponentText(EnumChatFormatting.GRAY + "Moderation commands "+EnumChatFormatting.DARK_GRAY+"(/twitch help moderation)"),
            };
            String[] categories = new String[]{"general", "setup", "config", "moderation"};
            for (int i = 0; i < 4; i++) {
                ChatStyle style = new ChatStyle().setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText("Click to show subcommands in the "+categories[i]+" category")))
                        .setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/twitch help "+categories[i]));
                components[i+1].setChatStyle(style);
            }
            StreamUtils.addMessages(components);
            return;
        }
        TwitchSubcommandCategory category = TwitchSubcommandCategory.getCategoryByName(args[0]);
        if (category == null) throw new CommandException("Unknown category name: "+args[0]);
        Stream<TwitchSubcommand> filteredSubcommands = getParentSubcommand().getSubcommands().stream().filter(cmd -> cmd.getCategory() == category);
        IChatComponent titleComponent = new ChatComponentText(EnumChatFormatting.GREEN + StreamUtils.capitalize(category.getName()) + " subcommand list of the /twitch command:");
        List<IChatComponent> components = filteredSubcommands.map(cmd -> new ChatComponentText(EnumChatFormatting.GRAY+"/twitch "+cmd.getSubcommandUsage()+EnumChatFormatting.WHITE+" - "+EnumChatFormatting.AQUA+cmd.getDescription())
                    .setChatStyle(new ChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/twitch "+cmd.getSubcommandUsage())))).collect(Collectors.toList());
        components.add(0, titleComponent);
        StreamUtils.addMessages(components.toArray(new IChatComponent[0]));
    }
}

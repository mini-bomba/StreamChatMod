package me.mini_bomba.streamchatmod.commands.subcommands;

import me.mini_bomba.streamchatmod.StreamChatMod;
import me.mini_bomba.streamchatmod.StreamUtils;
import me.mini_bomba.streamchatmod.commands.ICommandNode;
import me.mini_bomba.streamchatmod.commands.IHasAutocomplete;
import me.mini_bomba.streamchatmod.utils.LazyUnmodifiableMap;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class TwitchHelpSubcommand extends TwitchSubcommand implements IHasAutocomplete {
    private final Map<TwitchSubcommandCategory, List<TwitchSubcommand>> subcommandsByCategory = LazyUnmodifiableMap.from(() -> getParentSubcommand().getSubcommands().stream().collect(Collectors.groupingBy(TwitchSubcommand::getCategory)));
    private final List<String> categoryNames = Collections.unmodifiableList(Arrays.stream(TwitchSubcommandCategory.values()).map(TwitchSubcommandCategory::getName).sorted().collect(Collectors.toList()));
    private final List<String> autocompletions;

    public TwitchHelpSubcommand(StreamChatMod mod, ICommandNode<TwitchSubcommand> parentCommand) {
        super(mod, parentCommand);
        List<String> tempList = new ArrayList<>(TwitchSubcommandCategory.categoryMap.size());
        tempList.addAll(categoryNames);
        tempList.addAll(Arrays.stream(TwitchSubcommandCategory.values()).flatMap(category -> category.getAliases().stream()).collect(Collectors.toList()));
        autocompletions = Collections.unmodifiableList(tempList);
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
                    new ChatComponentText(EnumChatFormatting.GRAY + "Streaming commands "+EnumChatFormatting.DARK_GRAY+"(/twitch help streaming)"),
            };
            String[] categories = new String[]{"general", "setup", "config", "moderation", "streaming"};
            for (int i = 0; i < categories.length; i++) {
                ChatStyle style = new ChatStyle().setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText("Click to show subcommands in the "+categories[i]+" category")))
                        .setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/twitch help "+categories[i]));
                components[i+1].setChatStyle(style);
            }
            StreamUtils.addMessages(components);
            return;
        }
        TwitchSubcommandCategory category = TwitchSubcommandCategory.getCategoryByName(args[0]);
        if (category == null) throw new CommandException("Unknown category name: " + args[0]);
        IChatComponent titleComponent = new ChatComponentText(EnumChatFormatting.GREEN + StreamUtils.capitalize(category.getName()) + " subcommand list of the /twitch command:");
        List<IChatComponent> components = subcommandsByCategory.get(category).stream().map(cmd -> new ChatComponentText(EnumChatFormatting.GRAY + "/twitch " + cmd.getSubcommandUsage() + EnumChatFormatting.WHITE + " - " + EnumChatFormatting.AQUA + cmd.getDescription())
                .setChatStyle(new ChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/twitch " + cmd.getSubcommandUsage())))).collect(Collectors.toList());
        components.add(0, titleComponent);
        StreamUtils.addMessages(components.toArray(new IChatComponent[0]));
    }

    @Override
    public List<String> getAutocompletions(String[] args) {
        if (args.length > 1) return null;
        if (args[0].length() == 0)
            return new ArrayList<>(categoryNames);
        List<String> result = autocompletions.stream().filter(name -> name.startsWith(args[0])).collect(Collectors.toList());
        if (result.stream().map(TwitchSubcommandCategory.categoryMap::get).distinct().count() == 1) {
            TwitchSubcommandCategory category = TwitchSubcommandCategory.categoryMap.get(result.get(0));
            result = StreamUtils.singletonModifiableList(category.getName());
        }
        return result;
    }
}

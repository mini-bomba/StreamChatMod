package me.mini_bomba.streamchatmod.commands.subcommands;

import me.mini_bomba.streamchatmod.StreamChatMod;
import me.mini_bomba.streamchatmod.StreamUtils;
import me.mini_bomba.streamchatmod.asm.hooks.FontRendererHook;
import me.mini_bomba.streamchatmod.commands.ICommandNode;
import me.mini_bomba.streamchatmod.commands.IHasAutocomplete;
import me.mini_bomba.streamchatmod.utils.StreamEmote;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.event.ClickEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TwitchEmotesSubcommand extends TwitchSubcommand implements IHasAutocomplete {

    public TwitchEmotesSubcommand(StreamChatMod mod, ICommandNode<TwitchSubcommand> parentCommand) {
        super(mod, parentCommand);
    }

    @Override
    public @NotNull List<TwitchSubcommand> getSubcommands() {
        return Collections.emptyList();
    }

    @Override
    public @NotNull String getSubcommandName() {
        return "emotes";
    }

    @Override
    public @NotNull List<String> getSubcommandAliases() {
        return Arrays.asList("emote", "allowedemotes", "enabledemotes", "renderedemotes");
    }

    @Override
    public @NotNull String getSubcommandUsage() {
        return "emotes <type> [enable/disable]";
    }

    @Override
    public @NotNull String getDescription() {
        return "Manages rendered types of emotes";
    }

    @Override
    public TwitchSubcommandCategory getCategory() {
        return TwitchSubcommandCategory.CONFIG;
    }

    @Override
    public void processSubcommand(ICommandSender sender, String[] args) throws CommandException {
        if (args.length == 0) {
            List<IChatComponent> components = new ArrayList<>();
            components.add(new ChatComponentText(EnumChatFormatting.GREEN + "Rendered emote types:"));
            components.addAll(Arrays.stream(StreamEmote.Type.values()).map(type -> new ChatComponentText(EnumChatFormatting.AQUA + type.description + ": " + (type.isEnabled(mod.config) ? EnumChatFormatting.GREEN + "Enabled" : EnumChatFormatting.RED + "Disabled"))
                    .setChatStyle(new ChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/twitch emotes " + type.name().toLowerCase())))).collect(Collectors.toList()));
            components.add(new ChatComponentText(EnumChatFormatting.AQUA + "Animated emotes: " + (mod.config.allowAnimatedEmotes.getBoolean() ? EnumChatFormatting.GREEN + "Enabled" : EnumChatFormatting.RED + "Disabled"))
                    .setChatStyle(new ChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/twitch emotes animated"))));
            components.add(new ChatComponentText(EnumChatFormatting.AQUA + "Render emotes everywhere: " + (mod.config.showEmotesEverywhere.getBoolean() ? EnumChatFormatting.GREEN + "Enabled" : EnumChatFormatting.RED + "Disabled"))
                    .setChatStyle(new ChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/twitch emotes renderEverywhere"))));
            components.add(new ChatComponentText(EnumChatFormatting.GRAY + "Used internal emote slots: " + EnumChatFormatting.AQUA + StreamEmote.getEmoteCount() + EnumChatFormatting.GRAY + "/65536"));
            StreamUtils.addMessages(sender, components.toArray(new IChatComponent[0]));
        } else {
            if (args[0].equalsIgnoreCase("animated")) {
                if (args.length == 1)
                    StreamUtils.addMessage(EnumChatFormatting.AQUA + "Animated emotes are currently " + (mod.config.allowAnimatedEmotes.getBoolean() ? EnumChatFormatting.GREEN + "enabled" : EnumChatFormatting.RED + "disabled"));
                else {
                    Boolean newState = StreamUtils.readStringAsBoolean(args[1]);
                    if (newState == null)
                        throw new CommandException("Invalid boolean value: " + args[1]);
                    else {
                        mod.config.allowAnimatedEmotes.set(newState);
                        FontRendererHook.setAllowAnimated(newState);
                        StreamUtils.addMessage(EnumChatFormatting.GREEN + "Animated emotes have been " + (newState ? "enabled" : "disabled"));
                    }
                }
                return;
            }
            if (args[0].equalsIgnoreCase("renderEverywhere")) {
                if (args.length == 1)
                    StreamUtils.addMessage(EnumChatFormatting.AQUA + "Twitch emotes are currently rendered " + (mod.config.showEmotesEverywhere.getBoolean() ? EnumChatFormatting.GREEN + "everywhere" : EnumChatFormatting.LIGHT_PURPLE + "only in Twitch chat"));
                else {
                    Boolean newState = StreamUtils.readStringAsBoolean(args[1]);
                    if (newState == null)
                        throw new CommandException("Invalid boolean value: " + args[1]);
                    else {
                        mod.config.showEmotesEverywhere.set(newState);
                        StreamUtils.addMessage(EnumChatFormatting.GREEN + "Twitch emotes are now rendered " + (newState ? "everywhere" : "only in Twitch chat"));
                    }
                }
                return;
            }
            List<StreamEmote.Type> types = Arrays.stream(StreamEmote.Type.values()).filter(type -> type.name().equalsIgnoreCase(args[0])).collect(Collectors.toList());
            if (types.size() < 1)
                StreamUtils.addMessage(EnumChatFormatting.RED + "Unknown emote type: " + args[0]);
            else {
                StreamEmote.Type type = types.get(0);
                if (args.length == 1)
                    StreamUtils.addMessage(EnumChatFormatting.AQUA + type.description + "s are currently " + (type.isEnabled(mod.config) ? EnumChatFormatting.GREEN + "enabled" : EnumChatFormatting.RED + "disabled"));
                else {
                    Boolean newState = StreamUtils.readStringAsBoolean(args[1]);
                    if (newState == null)
                        throw new CommandException("Invalid boolean value: " + args[1]);
                    else {
                        type.setEnabled(mod.config, newState);
                        StreamUtils.addMessage(EnumChatFormatting.GREEN + type.description + " rendering has been " + (newState ? "enabled" : "disabled"));
                    }
                }
            }
        }
    }

    @Override
    public List<String> getAutocompletions(String[] args) {
        if (args.length == 1) {
            List<String> matchingTypes = Stream.concat(Arrays.stream(StreamEmote.Type.values()).map(type -> type.name().toLowerCase()), Stream.of("animated", "rendereverywhere")).filter(name -> name.startsWith(args[0].toLowerCase())).collect(Collectors.toList());
            if (matchingTypes.size() == 1 && matchingTypes.get(0).equals(args[0]))
                matchingTypes = StreamUtils.singletonModifiableList(matchingTypes.get(0) + " ");
            return matchingTypes;
        }
        if (args.length == 2)
            return Stream.of("enable", "disable").filter(name -> name.startsWith(args[1].toLowerCase())).collect(Collectors.toList());
        return null;
    }
}

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

public class TwitchModeSubcommand extends TwitchSubcommand implements IHasAutocomplete {

    public TwitchModeSubcommand(StreamChatMod mod, ICommandNode<TwitchSubcommand> parentCommand) {
        super(mod, parentCommand);
    }

    @Override
    public @NotNull List<TwitchSubcommand> getSubcommands() {
        return Collections.emptyList();
    }

    @Override
    public @NotNull String getSubcommandName() {
        return "chatmode";
    }

    @Override
    public @NotNull List<String> getSubcommandAliases() {
        return Arrays.asList("mode", "redirect");
    }

    @Override
    public @NotNull String getSubcommandUsage() {
        return "mode [new-mode]";
    }

    @Override
    public @NotNull String getDescription() {
        return "Manages the destination of messages sent through Minecraft chat";
    }

    @Override
    public TwitchSubcommandCategory getCategory() {
        return TwitchSubcommandCategory.GENERAL;
    }

    @Override
    public void processSubcommand(ICommandSender sender, String[] args) throws CommandException {
        if (mod.twitch == null || !mod.config.twitchEnabled.getBoolean()) throw new CommandException("Twitch chat is disabled!");
        if (args.length == 0) {
            boolean redirectEnabled = mod.config.twitchMessageRedirectEnabled.getBoolean();
            StreamUtils.addMessages(new String[] {
                    EnumChatFormatting.AQUA + "Current Minecraft chat mode: " + (redirectEnabled ? EnumChatFormatting.DARK_PURPLE + "Redirect to selected Twitch channel" : EnumChatFormatting.GREEN + "Send to Minecraft server"),
                    EnumChatFormatting.GRAY + "Use " + EnumChatFormatting.DARK_AQUA + "/twitch mode " + (redirectEnabled ? "minecraft" : "twitch") + EnumChatFormatting.GRAY + " to send new Minecraft messages to the " + (redirectEnabled ? "Minecraft server" : "currently selected Twitch channel")
            });
        } else {
            Boolean newState = StreamUtils.readStringAsBoolean(args[0]);
            if (newState == null) {
                switch (args[0].toLowerCase()) {
                    case "twitch":
                    case "t":
                        newState = true;
                        break;
                    case "minecraft":
                    case "mc":
                    case "m":
                        newState = false;
                        break;
                    default:
                        throw new CommandException("Invalid mode: " + args[0]);
                }
            }
            mod.config.twitchMessageRedirectEnabled.set(newState);
            mod.config.saveIfChanged();
            StreamUtils.addMessage(EnumChatFormatting.AQUA + "Minecraft chat mode has been set to " + (newState ? EnumChatFormatting.DARK_PURPLE + "Redirect to selected Twitch channel" : EnumChatFormatting.GREEN + "Send to Minecraft server"));
        }
    }

    @Override
    public List<String> getAutocompletions(String[] args) {
        if (args.length > 1) return null;
        return Stream.of("twitch", "minecraft").filter(s -> s.startsWith(args[0])).collect(Collectors.toList());
    }
}

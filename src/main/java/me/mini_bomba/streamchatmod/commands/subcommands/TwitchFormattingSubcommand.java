package me.mini_bomba.streamchatmod.commands.subcommands;

import me.mini_bomba.streamchatmod.StreamChatMod;
import me.mini_bomba.streamchatmod.StreamUtils;
import me.mini_bomba.streamchatmod.commands.ICommandNode;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.EnumChatFormatting;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TwitchFormattingSubcommand extends TwitchSubcommand {

    public TwitchFormattingSubcommand(StreamChatMod mod, ICommandNode<TwitchSubcommand> parentCommand) {
        super(mod, parentCommand);
    }

    @Override
    public @NotNull List<TwitchSubcommand> getSubcommands() {
        return Collections.emptyList();
    }

    @Override
    public @NotNull String getSubcommandName() {
        return "formatting";
    }

    @Override
    public @NotNull List<String> getSubcommandAliases() {
        return Arrays.asList("allowformatting", "chatformatting", "format", "allowformat", "chatformat", "cf");
    }

    @Override
    public @NotNull String getSubcommandUsage() {
        return "formatting [enable/subonly/disable]";
    }

    @Override
    public @NotNull String getDescription() {
        return "Allows viewers to use chat formatting codes (ex. &7) to send formatted messages";
    }

    @Override
    public TwitchSubcommandCategory getCategory() {
        return TwitchSubcommandCategory.CONFIG;
    }

    @Override
    public void processSubcommand(ICommandSender sender, String[] args) throws CommandException {
        if (args.length == 0) {
            boolean formattingAllowed = mod.config.allowFormatting.getBoolean();
            boolean subOnlyFormatting = mod.config.subOnlyFormatting.getBoolean();
            StreamUtils.addMessages(sender, new String[]{
                    EnumChatFormatting.AQUA + "Chat formatting codes are currently " + (formattingAllowed ? (subOnlyFormatting ? EnumChatFormatting.GOLD + "for subscribers+ only" : EnumChatFormatting.GREEN + "enabled") : EnumChatFormatting.RED + "disabled"),
                    EnumChatFormatting.GRAY + "Use " + EnumChatFormatting.DARK_AQUA + "/twitch formatting " + (formattingAllowed ? "disable" : "enable") + EnumChatFormatting.GRAY + " to " + (formattingAllowed ? "disallow" : "allow") + " viewers to send formatted messages" + (formattingAllowed ? ", or use " + EnumChatFormatting.DARK_AQUA + "/twitch formatting "+ (subOnlyFormatting ? "enable" : "subonly") + EnumChatFormatting.GRAY + " to allow " + (subOnlyFormatting ? "everyone" : "only subs/vips/mods") + " to use formatting" : "") + "!"
            });
        } else {
            Boolean newState = StreamUtils.readStringAsBoolean(args[0]);
            boolean newSubOnly;
            if (newState == null) switch (args[0].toLowerCase()) {
                case "subonly":
                case "sub":
                case "viponly":
                case "vip":
                case "modonly":
                case "mod":
                    newSubOnly = true;
                    newState = true;
                    break;
                case "everyone":
                case "all":
                case "anyone":
                    newSubOnly = false;
                    newState = true;
                    break;
                default:
                    throw new CommandException("Invalid value: " + args[0]);
            }
            else newSubOnly = false;
            mod.config.allowFormatting.set(newState);
            mod.config.subOnlyFormatting.set(newSubOnly);
            mod.config.saveIfChanged();
            StreamUtils.addMessage(sender, EnumChatFormatting.GREEN + (newSubOnly ? "Only Subscribers, VIPs & moderators" : "Viewers") + " are " + (newState ? "now" : "no longer") + " allowed to use formatting codes in their messages!");
        }
    }
}

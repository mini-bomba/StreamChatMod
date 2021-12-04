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

public class TwitchMcChatPrefixSubcommand extends TwitchSubcommand implements IHasAutocomplete {
    private static final List<String> resetPrefixes = Collections.unmodifiableList(Arrays.asList("reset", "disable", "remove", "null", "nil", "delete", "none", "disabled"));

    public TwitchMcChatPrefixSubcommand(StreamChatMod mod, ICommandNode<TwitchSubcommand> parentCommand) {
        super(mod, parentCommand);
    }

    @Override
    public @NotNull List<TwitchSubcommand> getSubcommands() {
        return Collections.emptyList();
    }

    @Override
    public @NotNull String getSubcommandName() {
        return "mcchatprefix";
    }

    @Override
    public @NotNull List<String> getSubcommandAliases() {
        return Arrays.asList("minecraftchatprefix", "chatprefix", "mccprefix", "mccp", "cp", "modeprefix", "mp");
    }

    @Override
    public @NotNull String getSubcommandUsage() {
        return "mcchatprefix [new prefix]";
    }

    @Override
    public @NotNull String getDescription() {
        return "Shows or changes the Minecraft chat prefix";
    }

    @Override
    public TwitchSubcommandCategory getCategory() {
        return TwitchSubcommandCategory.CONFIG;
    }

    @Override
    public void processSubcommand(ICommandSender sender, String[] args) throws CommandException {
        if (args.length == 0) {
            String currentPrefix = mod.config.minecraftChatPrefix.getString();
            StreamUtils.addMessages(new String[]{
                    EnumChatFormatting.AQUA + "Current minecraft chat prefix: "+(currentPrefix.length() == 0 ? EnumChatFormatting.RED+"Disabled!" : EnumChatFormatting.GRAY+currentPrefix),
                    EnumChatFormatting.GRAY + "Use " + EnumChatFormatting.DARK_AQUA + "/twitch mcchatprefix <new prefix>" + EnumChatFormatting.GRAY + " to "+(currentPrefix.length() == 0 ? "set" : "change")+" the prefix!"
            });
            if (currentPrefix.length() != 0) StreamUtils.addMessage(EnumChatFormatting.GRAY + "Prepend your messages with " + EnumChatFormatting.DARK_AQUA + currentPrefix + EnumChatFormatting.GRAY + " while in 'Redirect to Twitch' mode to send your message to the Minecraft server instead!");
        } else {
            String newPrefix = String.join(" ", args);
            if (resetPrefixes.contains(newPrefix)) newPrefix = "";
            mod.config.minecraftChatPrefix.set(newPrefix);
            mod.config.saveIfChanged();
            if (newPrefix.length() != 0) StreamUtils.addMessages(new String[]{
                    EnumChatFormatting.GREEN + "Minecraft chat prefix has been set to: " + EnumChatFormatting.GRAY + newPrefix + EnumChatFormatting.GREEN + "!",
                    EnumChatFormatting.GRAY + "Prepend your messages with " + EnumChatFormatting.DARK_AQUA + newPrefix + EnumChatFormatting.GRAY + " while in 'Redirect to Twitch' mode to send your message to the Minecraft server instead!"
            });
            else StreamUtils.addMessage(EnumChatFormatting.GREEN + "Minecraft chat prefix has been disabled!");
        }
    }

    @Override
    public List<String> getAutocompletions(String[] args) {
        if (args.length > 1 || args[0].length() > 0) return null;
        return StreamUtils.singletonModifiableList(mod.config.minecraftChatPrefix.getString());
    }
}

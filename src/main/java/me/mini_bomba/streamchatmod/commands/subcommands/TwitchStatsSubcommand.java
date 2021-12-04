package me.mini_bomba.streamchatmod.commands.subcommands;

import me.mini_bomba.streamchatmod.StreamChatMod;
import me.mini_bomba.streamchatmod.StreamUtils;
import me.mini_bomba.streamchatmod.commands.ICommandNode;
import me.mini_bomba.streamchatmod.commands.IDrawsChatOutline;
import me.mini_bomba.streamchatmod.commands.IHasAutocomplete;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.EnumChatFormatting;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class TwitchStatsSubcommand extends TwitchSubcommand implements IDrawsChatOutline, IHasAutocomplete {

    public TwitchStatsSubcommand(StreamChatMod mod, ICommandNode<TwitchSubcommand> parentCommand) {
        super(mod, parentCommand);
    }

    @Override
    public @NotNull List<TwitchSubcommand> getSubcommands() {
        return Collections.emptyList();
    }

    @Override
    public @NotNull String getSubcommandName() {
        return "streamstats";
    }

    @Override
    public @NotNull List<String> getSubcommandAliases() {
        return Arrays.asList("stats", "streamstatus", "stream", "streamstatistics", "statistics");
    }

    @Override
    public @NotNull String getSubcommandUsage() {
        return "streamstats [streamer]";
    }

    @Override
    public @NotNull String getDescription() {
        return "Shows the stream stats of the currently selected user";
    }

    @Override
    public TwitchSubcommandCategory getCategory() {
        return TwitchSubcommandCategory.STREAMING;
    }

    @Override
    public void processSubcommand(ICommandSender sender, String[] args) throws CommandException {
        String channel = args.length >= 1 ? args[0] : mod.config.twitchSelectedChannel.getString();
        if (mod.twitch == null || !mod.config.twitchEnabled.getBoolean())
            throw new CommandException("Twitch chat is disabled!");
        if (channel.length() == 0)
            throw new CommandException("No selected channel. Use /twitch channels select <channel> to select one.");
        if (mod.twitchAsyncAction != null)
            throw new CommandException("An action for the Twitch Chat is currently pending, please wait.");
        mod.asyncShowTwitchStreamStats(channel);
    }

    @Override
    public void drawChatOutline(GuiChat gui, String[] args) {
        String channel = args.length >= 1 ? args[0] : mod.config.twitchSelectedChannel.getString();
        if (mod.twitch == null || !mod.config.twitchEnabled.getBoolean())
            StreamUtils.drawChatWarning(gui, StreamUtils.RED, StreamUtils.BACKGROUND, EnumChatFormatting.RED + "Twitch chat is disabled!");
        else if (channel.length() == 0)
            StreamUtils.drawChatWarning(gui, StreamUtils.RED, StreamUtils.BACKGROUND, EnumChatFormatting.RED + "No Twitch channel selected!");
        else
            StreamUtils.drawChatWarning(gui, StreamUtils.PURPLE, StreamUtils.BACKGROUND, EnumChatFormatting.LIGHT_PURPLE + "Querying " + EnumChatFormatting.AQUA + channel + EnumChatFormatting.LIGHT_PURPLE + "'s stream stats");
    }

    @Override
    public List<String> getAutocompletions(String[] args) {
        if (args.length > 1 || mod.twitch == null || !mod.config.twitchEnabled.getBoolean()) return null;
        return mod.twitch.getChat().getChannels().stream().filter(channel -> channel.startsWith(args[0])).collect(Collectors.toList());
    }
}

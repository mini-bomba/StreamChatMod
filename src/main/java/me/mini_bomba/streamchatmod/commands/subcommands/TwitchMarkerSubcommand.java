package me.mini_bomba.streamchatmod.commands.subcommands;

import me.mini_bomba.streamchatmod.StreamChatMod;
import me.mini_bomba.streamchatmod.StreamUtils;
import me.mini_bomba.streamchatmod.commands.ICommandNode;
import me.mini_bomba.streamchatmod.commands.IDrawsChatOutline;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.EnumChatFormatting;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class TwitchMarkerSubcommand extends TwitchSubcommand implements IDrawsChatOutline {

    public TwitchMarkerSubcommand(StreamChatMod mod, ICommandNode<TwitchSubcommand> parentCommand) {
        super(mod, parentCommand);
    }

    @Override
    public @NotNull List<TwitchSubcommand> getSubcommands() {
        return Collections.emptyList();
    }

    @Override
    public @NotNull String getSubcommandName() {
        return "marker";
    }

    @Override
    public @NotNull List<String> getSubcommandAliases() {
        return Collections.emptyList();
    }

    @Override
    public @NotNull String getSubcommandUsage() {
        return "marker [description]";
    }

    @Override
    public @NotNull String getDescription() {
        return "Creates a new stream marker on the selected channel's stream";
    }

    @Override
    public TwitchSubcommandCategory getCategory() {
        return TwitchSubcommandCategory.STREAMING;
    }

    @Override
    public void processSubcommand(ICommandSender sender, String[] args) throws CommandException {
        String channel = mod.config.twitchSelectedChannel.getString();
        if (mod.twitch == null || !mod.config.twitchEnabled.getBoolean()) throw new CommandException("Twitch chat is disabled!");
        if (channel.length() == 0) throw new CommandException("No selected channel. Use /twitch channels select <channel> to select one.");
        if (args.length == 0) mod.asyncCreateMarker();
        else mod.asyncCreateMarker(String.join(" ", args));
        StreamUtils.addMessage(EnumChatFormatting.GRAY+"Creating marker...");
    }

    @Override
    public void drawChatOutline(GuiChat gui, String[] args) {
        String channel = mod.config.twitchSelectedChannel.getString();
        if (mod.twitch == null || !mod.config.twitchEnabled.getBoolean())
            StreamUtils.drawChatWarning(gui, StreamUtils.RED, StreamUtils.BACKGROUND, EnumChatFormatting.RED+"Twitch chat is disabled!");
        else if (channel.length() == 0)
            StreamUtils.drawChatWarning(gui, StreamUtils.RED, StreamUtils.BACKGROUND, EnumChatFormatting.RED+"No Twitch channel selected!");
        else if (args.length == 0)
            StreamUtils.drawChatWarning(gui, StreamUtils.PURPLE, StreamUtils.BACKGROUND, EnumChatFormatting.LIGHT_PURPLE+"Creating a stream marker on "+EnumChatFormatting.AQUA+channel+EnumChatFormatting.LIGHT_PURPLE+"'s stream");
        else
            StreamUtils.drawChatWarning(gui, StreamUtils.PURPLE, StreamUtils.BACKGROUND, EnumChatFormatting.LIGHT_PURPLE+"Creating a stream marker on "+EnumChatFormatting.AQUA+channel+EnumChatFormatting.LIGHT_PURPLE+"'s chat with description \""+EnumChatFormatting.AQUA+String.join(" ", args)+EnumChatFormatting.LIGHT_PURPLE+"\"");
    }
}

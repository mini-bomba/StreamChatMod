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

public class TwitchClipSubcommand extends TwitchSubcommand implements IDrawsChatOutline {

    public TwitchClipSubcommand(StreamChatMod mod, ICommandNode<TwitchSubcommand> parentCommand) {
        super(mod, parentCommand);
    }

    @Override
    public @NotNull List<TwitchSubcommand> getSubcommands() {
        return Collections.emptyList();
    }

    @Override
    public @NotNull String getSubcommandName() {
        return "clip";
    }

    @Override
    public @NotNull List<String> getSubcommandAliases() {
        return Collections.emptyList();
    }

    @Override
    public @NotNull String getSubcommandUsage() {
        return "clip";
    }

    @Override
    public @NotNull String getDescription() {
        return "Creates a new clip on the selected channel's stream";
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
        mod.asyncCreateClip();
        StreamUtils.addMessage(EnumChatFormatting.GRAY+"Creating clip...");
    }

    @Override
    public void drawChatOutline(GuiChat gui, String[] args) {
        String channel = mod.config.twitchSelectedChannel.getString();
        if (mod.twitch == null || !mod.config.twitchEnabled.getBoolean())
            StreamUtils.drawChatWarning(gui, StreamUtils.RED, StreamUtils.BACKGROUND, EnumChatFormatting.RED+"Twitch chat is disabled!");
        else if (channel.length() == 0)
            StreamUtils.drawChatWarning(gui, StreamUtils.RED, StreamUtils.BACKGROUND, EnumChatFormatting.RED+"No Twitch channel selected!");
        else
            StreamUtils.drawChatWarning(gui, StreamUtils.PURPLE, StreamUtils.BACKGROUND, EnumChatFormatting.LIGHT_PURPLE+"Creating a clip on "+EnumChatFormatting.AQUA+channel+EnumChatFormatting.LIGHT_PURPLE+"'s stream");
    }
}

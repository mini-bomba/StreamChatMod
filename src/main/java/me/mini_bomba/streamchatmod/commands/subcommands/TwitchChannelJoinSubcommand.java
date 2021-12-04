package me.mini_bomba.streamchatmod.commands.subcommands;

import com.github.twitch4j.chat.TwitchChat;
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

public class TwitchChannelJoinSubcommand extends TwitchSubcommand {

    public TwitchChannelJoinSubcommand(StreamChatMod mod, ICommandNode<TwitchSubcommand> parentCommand) {
        super(mod, parentCommand);
    }

    @Override
    public @NotNull List<TwitchSubcommand> getSubcommands() {
        return Collections.emptyList();
    }

    @Override
    public @NotNull String getSubcommandName() {
        return "join";
    }

    @Override
    public @NotNull List<String> getSubcommandAliases() {
        return Arrays.asList("j", "+", "add");
    }

    @Override
    public @NotNull String getSubcommandUsage() {
        return "join <channel-name>";
    }

    @Override
    public @NotNull String getDescription() {
        return "Joins the specified Twitch channel";
    }

    @Override
    public TwitchSubcommandCategory getCategory() {
        return null;
    }

    @Override
    public boolean hasParameters() {
        return true;
    }

    @Override
    public void processSubcommand(ICommandSender sender, String[] args) throws CommandException {
        TwitchChat chat = mod.twitch != null ? mod.twitch.getChat() : null;
        if (chat == null) throw new CommandException("Please enable Twitch chat first!");
        if (args.length == 0) throw new CommandException("Missing parameter: channel to join");
        String channel = args[0];
        List<String> channelList = Arrays.asList(mod.config.twitchChannels.getStringList());
        if (channelList.contains(channel) && chat.isChannelJoined(channel))
            throw new CommandException("Channel " + channel + " is already joined!");
        if (mod.twitchAsyncAction != null)
            throw new CommandException("An action for the Twitch Chat is currently pending, please wait.");
        mod.asyncJoinTwitchChannel(channel);
        StreamUtils.addMessage(EnumChatFormatting.GRAY + "Joining channel...");
    }
}

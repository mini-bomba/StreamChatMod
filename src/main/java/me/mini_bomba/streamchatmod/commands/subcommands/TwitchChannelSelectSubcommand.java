package me.mini_bomba.streamchatmod.commands.subcommands;

import com.github.twitch4j.chat.TwitchChat;
import me.mini_bomba.streamchatmod.StreamChatMod;
import me.mini_bomba.streamchatmod.StreamUtils;
import me.mini_bomba.streamchatmod.commands.ICommandNode;
import me.mini_bomba.streamchatmod.commands.IHasAutocomplete;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.EnumChatFormatting;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class TwitchChannelSelectSubcommand extends TwitchSubcommand implements IHasAutocomplete {

    public TwitchChannelSelectSubcommand(StreamChatMod mod, ICommandNode<TwitchSubcommand> parentCommand) {
        super(mod, parentCommand);
    }

    @Override
    public @NotNull List<TwitchSubcommand> getSubcommands() {
        return Collections.emptyList();
    }

    @Override
    public @NotNull String getSubcommandName() {
        return "select";
    }

    @Override
    public @NotNull List<String> getSubcommandAliases() {
        return Collections.singletonList("s");
    }

    @Override
    public @NotNull String getSubcommandUsage() {
        return "select <channel-name>";
    }

    @Override
    public @NotNull String getDescription() {
        return "Selects the specified channel as the one to send messages to";
    }

    @Override
    public TwitchSubcommandCategory getCategory() {
        return null;
    }

    @Override
    public void processSubcommand(ICommandSender sender, String[] args) throws CommandException {
        TwitchChat chat = mod.twitch != null ? mod.twitch.getChat() : null;
        if (chat == null) throw new CommandException("Please enable Twitch chat first!");
        mod.config.twitchSelectedChannel.set(args.length == 0 ? "" : args[0]);
        mod.config.saveIfChanged();
        if (args.length == 0)
            StreamUtils.addMessage(sender, EnumChatFormatting.GREEN + "Unselected the stream chat channel!");
        else StreamUtils.addMessage(sender, EnumChatFormatting.GREEN + "Selected " + args[0] + "'s stream chat!");
    }

    @Override
    public List<String> getAutocompletions(String[] args) {
        if (args.length > 1 || mod.twitch == null || !mod.config.twitchEnabled.getBoolean()) return null;
        return mod.twitch.getChat().getChannels().stream().filter(channel -> channel.startsWith(args[0])).collect(Collectors.toList());
    }
}

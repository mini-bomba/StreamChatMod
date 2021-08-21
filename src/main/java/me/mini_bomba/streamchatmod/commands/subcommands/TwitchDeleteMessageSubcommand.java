package me.mini_bomba.streamchatmod.commands.subcommands;

import me.mini_bomba.streamchatmod.StreamChatMod;
import me.mini_bomba.streamchatmod.StreamUtils;
import me.mini_bomba.streamchatmod.commands.ICommandNode;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.EnumChatFormatting;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class TwitchDeleteMessageSubcommand extends TwitchSubcommand {

    public TwitchDeleteMessageSubcommand(StreamChatMod mod, ICommandNode<TwitchSubcommand> parentCommand) {
        super(mod, parentCommand);
    }

    @Override
    public @NotNull List<TwitchSubcommand> getSubcommands() {
        return Collections.emptyList();
    }

    @Override
    public @NotNull String getSubcommandName() {
        return "deletemessage";
    }

    @Override
    public @NotNull List<String> getSubcommandAliases() {
        return Collections.singletonList("delete");
    }

    @Override
    public @NotNull String getSubcommandUsage() {
        return "deletemessage <channel> <message id>";
    }

    @Override
    public @NotNull String getDescription() {
        return "Deletes the selected message. Click a twitch message to automatically generate this command";
    }

    @Override
    public TwitchSubcommandCategory getCategory() {
        return TwitchSubcommandCategory.MODERATION;
    }

    @Override
    public void processSubcommand(ICommandSender sender, String[] args) throws CommandException {
        if (mod.twitch == null || !mod.config.twitchEnabled.getBoolean()) throw new CommandException("Twitch chat is disabled!");
        if (args.length == 0) throw new CommandException("Missing required parameters: channel & id of the message to delete");
        if (args.length == 1) throw new CommandException("Missing required parameter: id of the message to delete");
        mod.twitch.getChat().delete(args[0], args[1]);
        StreamUtils.addMessage(EnumChatFormatting.GREEN + "Message deleted.");
    }
}

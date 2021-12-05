package me.mini_bomba.streamchatmod.commands;

import me.mini_bomba.streamchatmod.StreamChatMod;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class TwitchChatCommand extends CommandBase {

    private final StreamChatMod mod;

    public TwitchChatCommand(StreamChatMod mod) {
        this.mod = mod;
    }

    @Override
    public String getCommandName() {
        return "twitchchat";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/twitchchat <message to send>";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        if (mod.twitch == null || mod.twitchSender == null) throw new CommandException("Twitch chat is disabled or improperly configured!");
        String channel = mod.config.twitchSelectedChannel.getString();
        if (channel.length() == 0) throw new CommandException("No selected channel. Use /twitch channels select <channel> to select one.");
        if (args.length == 0) throw new CommandException("Usage: /twitchchat <message>");
        boolean success = mod.twitchSender.getChat().sendMessage(channel, String.join(" ", args));
        if (!success) throw new CommandException("Something went wrong. (Check token permissions maybe?)");
    }

    @Override
    public List<String> getCommandAliases() {
        return Collections.singletonList("tc");
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
        if (mod.twitch == null || mod.twitchSender == null || !args[args.length - 1].startsWith("@")) return null;
        return mod.getChatters(mod.config.twitchSelectedChannel.getString()).getAllViewers().stream().filter(user -> user.startsWith(args[args.length - 1].substring(1))).map(user -> "@" + user).collect(Collectors.toList());
    }
}

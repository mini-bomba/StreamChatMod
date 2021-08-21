package me.mini_bomba.streamchatmod.commands.subcommands;

import me.mini_bomba.streamchatmod.StreamChatMod;
import me.mini_bomba.streamchatmod.commands.ICommandNode;
import me.mini_bomba.streamchatmod.commands.Subcommand;

public abstract class TwitchSubcommand extends Subcommand<TwitchSubcommand> {

    public TwitchSubcommand(StreamChatMod mod, ICommandNode<TwitchSubcommand> parentCommand) {
        super(mod, parentCommand);
    }

    // TwitchSubcommands must have TwitchSubcommandCategory as their categories, not any SubcommandCategory
    @Override
    public abstract TwitchSubcommandCategory getCategory();
}

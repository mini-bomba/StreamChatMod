package me.mini_bomba.streamchatmod.commands;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface ICommandNode<T extends Subcommand<T>> {

    /**
     * Return the parent ICommandNode, or null if this is a root node
     */
    ICommandNode<T> getParentSubcommand();

    /**
     * Return a list of children ICommandNodes
     */
    @NotNull
    List<T> getSubcommands();
}

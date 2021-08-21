package me.mini_bomba.streamchatmod.commands;

import java.util.List;

/**
 * This interface should be implemented by SubcommandCategory Enums
 */
public interface ISubcommandCategory {
    String getName();
    List<String> getAliases();
}

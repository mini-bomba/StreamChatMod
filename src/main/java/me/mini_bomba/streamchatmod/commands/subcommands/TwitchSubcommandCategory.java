package me.mini_bomba.streamchatmod.commands.subcommands;

import me.mini_bomba.streamchatmod.commands.ISubcommandCategory;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public enum TwitchSubcommandCategory implements ISubcommandCategory {
    GENERAL("general", "g"),
    SETUP("setup", "s"),
    CONFIG("configuration", "config", "conf", "c"),
    MODERATION("moderation", "mod", "m"),
    STREAMING("streaming", "stream", "editor", "e", "broadcast", "b")
    ;
    private final String name;
    private final List<String> aliases;

    TwitchSubcommandCategory(String name, String... aliases) {
        this.name = name;
        this.aliases = Arrays.asList(aliases);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public List<String> getAliases() {
        return aliases;
    }

    public static @Nullable TwitchSubcommandCategory getCategoryByName(String name) {
        name = name.toLowerCase();
        for (TwitchSubcommandCategory category : TwitchSubcommandCategory.values()) {
            if (category.name.equals(name) || category.aliases.contains(name))
                return category;
        }
        return null;
    }

}

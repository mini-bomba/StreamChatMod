package me.mini_bomba.streamchatmod.commands.subcommands;

import me.mini_bomba.streamchatmod.commands.ISubcommandCategory;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public enum TwitchSubcommandCategory implements ISubcommandCategory {
    GENERAL("general", "g"),
    SETUP("setup", "s"),
    CONFIG("configuration", "config", "conf", "c"),
    MODERATION("moderation", "mod", "m"),
    STREAMING("streaming", "stream", "editor", "e", "broadcast", "b");
    private final String name;
    private final List<String> aliases;
    public static final Map<String, TwitchSubcommandCategory> categoryMap;

    static {
        Map<String, TwitchSubcommandCategory> tempMap = new HashMap<>();
        for (TwitchSubcommandCategory category : TwitchSubcommandCategory.values()) {
            assert !tempMap.containsKey(category.getName()) : "Duplicate category map entry: " + category.getName() + " (primary name)";
            tempMap.put(category.getName(), category);
            for (String alias : category.getAliases()) {
                assert !tempMap.containsKey(alias) : "Duplicate category map entry: " + alias + " (alias of " + category.getName() + ")";
                tempMap.put(alias, category);
            }
        }
        categoryMap = Collections.unmodifiableMap(tempMap);
    }

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
        return categoryMap.get(name.toLowerCase());
    }

}

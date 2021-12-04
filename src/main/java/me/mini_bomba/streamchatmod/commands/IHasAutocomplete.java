package me.mini_bomba.streamchatmod.commands;

import java.util.List;

public interface IHasAutocomplete {

    /**
     * Get a list of strings to autocomplete the command with.
     *
     * @param args current command arguments
     * @return list of possible autocompletions
     */
    List<String> getAutocompletions(String[] args);
}

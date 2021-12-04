package me.mini_bomba.streamchatmod.commands;

import me.mini_bomba.streamchatmod.StreamChatMod;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public abstract class Subcommand<T extends Subcommand<T>> implements ICommandNode<T> {

    protected final StreamChatMod mod;
    private final ICommandNode<T> parent;

    public Subcommand(StreamChatMod mod, ICommandNode<T> parentCommand) {
        this.mod = mod;
        this.parent = parentCommand;
    }

    @Override
    public ICommandNode<T> getParentSubcommand() {
        return parent;
    }

    /**
     * Return the main subcommand name
     */
    @NotNull
    public abstract String getSubcommandName();

    /**
     * Return a list of additional subcommand names
     */
    @NotNull
    public List<String> getSubcommandAliases() {
        return Collections.emptyList();
    }

    /**
     * Return a string representing the subcommand usage<br>
     * Example: `ban <user> [reason]`<br>
     * This should be checked by help subcommands
     */
    @NotNull
    public abstract String getSubcommandUsage();

    /**
     * Return a description of the command<br>
     * This should be checked by help subcommands
     */
    @NotNull
    public abstract String getDescription();

    /**
     * Return a SubcommandCategory Enum<br>
     * This should be checked by help subcommands to determine which category a subcommand belongs to
     */
    public abstract ISubcommandCategory getCategory();

    /**
     * Does the command take any parameters?<br>
     * Return true if it takes at least one,
     * return false if it does not take any
     * <p>
     * By default, any subcommand that extends IHasAutocomplete is assumed to have input parameters
     */
    public boolean hasParameters() {
        return this instanceof IHasAutocomplete;
    }

    /**
     * Process the subcommand invocation<br>
     * Parameters are passed from the parent (sub)command, but the first value in args is dropped
     */
    public abstract void processSubcommand(ICommandSender sender, String[] args) throws CommandException;


    /**
     * Utility method to create a map of subcommand name/alias -> subcommand object<br>
     * List of subcommands is grabbed from the getSubcommands() method<br>
     * No duplicate subcommand names/aliases are allowed
     */
    protected static <T extends Subcommand<T>> Map<String, T> createNameMap(List<T> subcommands) {
        HashMap<String, T> map = new HashMap<>();
        for (T cmd : subcommands) {
            assert !map.containsKey(cmd.getSubcommandName()) : "Duplicate subcommand map entry: " + cmd.getSubcommandName() + " (primary name)";
            map.put(cmd.getSubcommandName(), cmd);
            for (String alias : cmd.getSubcommandAliases()) {
                assert !map.containsKey(alias) : "Duplicate subcommand map entry: " + alias + " (alias of "+cmd.getSubcommandName()+")";
                map.put(alias, cmd);
            }
        }
        return Collections.unmodifiableMap(map);
    }

    protected Map<String, T> createNameMap() {
        return createNameMap(getSubcommands());
    }

    /**
     * Utility method to create a list of subcommand names.
     */
    protected static List<String> createNameList(List<? extends Subcommand<?>> subcommands) {
        return Collections.unmodifiableList(subcommands.stream().map(Subcommand::getSubcommandName).sorted().collect(Collectors.toList()));
    }

    protected List<String> createNameList() {
        return createNameList(getSubcommands());
    }

    /**
     * Utility method to create a list of subcommand names and aliases for autocompletions.
     * Names appear first, then subcommand aliases.
     * No duplicate subcommand names/aliases allowed.
     *
     * @param subcommands list of subcommands to index
     * @return list of possible autocompletions
     */
    protected static List<String> createAutocompletionList(List<? extends Subcommand<?>> subcommands) {
        List<String> autocompletions = subcommands.stream().map(Subcommand::getSubcommandName).sorted().collect(Collectors.toList());
        autocompletions.addAll(subcommands.stream().flatMap(subcommand -> subcommand.getSubcommandAliases().stream()).sorted().collect(Collectors.toList()));
        return Collections.unmodifiableList(autocompletions);
    }

    protected List<String> createAutocompletionList() {
        return createAutocompletionList(getSubcommands());
    }

    /**
     * Utility method to filter out subcommands that implement IHasAutocomplete from a subcommand name map
     *
     * @param subcommandMap map of name/alias -> subcommand (preferably created with createNameMap())
     * @return map of name/alias -> subcommand that implements IHasAutocomplete
     */
    protected static Map<String, IHasAutocomplete> createAutcompletableMap(Map<String, ? extends Subcommand<?>> subcommandMap) {
        return Collections.unmodifiableMap(subcommandMap.keySet().stream().filter(name -> subcommandMap.get(name) instanceof IHasAutocomplete).sorted().collect(Collectors.toMap(name -> name, name -> (IHasAutocomplete) subcommandMap.get(name))));
    }

    /**
     * The implementation of addTabCompletionOptions/getAutocompletions used in the /twitch command so IntelliJ will stop complaining about duplicated code
     *
     * @param args                          current command arguments
     * @param subcommandMap                 map of name/alias -> any subcommand
     * @param subcommandMapWithAutocomplete map of name/alias -> subcommand that has autocomplete options
     * @param subcommandNames               list of subcommand names (excluding aliases)
     * @param subcommandNamesAndAliases     list of subcommand names and aliases (preferably generated with createAutocompletionList)
     * @return list of appropriate autocompletions
     */
    public static List<String> getAutocompletions(String[] args, Map<String, ? extends Subcommand<?>> subcommandMap, Map<String, IHasAutocomplete> subcommandMapWithAutocomplete, List<String> subcommandNames, List<String> subcommandNamesAndAliases) {
        if (args.length > 1 && subcommandMapWithAutocomplete.containsKey(args[0]))
            return subcommandMapWithAutocomplete.get(args[0]).getAutocompletions(Arrays.copyOfRange(args, 1, args.length));
        if (args.length > 1) return null;
        if (args[0].length() == 0)
            return new ArrayList<>(subcommandNames);
        List<String> result = subcommandNamesAndAliases.stream().filter(name -> name.startsWith(args[0])).collect(Collectors.toList());
        if (result.stream().map(subcommandMap::get).distinct().count() == 1) {
            Subcommand<? extends Subcommand<?>> subcommand = subcommandMap.get(result.get(0));
            result = new ArrayList<>(Collections.singleton(subcommand.getSubcommandName() + (subcommand.hasParameters() ? " " : "")));
        }
        return result;
    }
}

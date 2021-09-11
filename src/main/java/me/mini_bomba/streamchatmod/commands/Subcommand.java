package me.mini_bomba.streamchatmod.commands;

import me.mini_bomba.streamchatmod.StreamChatMod;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

}

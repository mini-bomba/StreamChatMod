package me.mini_bomba.streamchatmod.commands.subcommands;

import me.mini_bomba.streamchatmod.StreamChatMod;
import me.mini_bomba.streamchatmod.StreamUtils;
import me.mini_bomba.streamchatmod.commands.ICommandNode;
import me.mini_bomba.streamchatmod.commands.IDrawsChatOutline;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.EnumChatFormatting;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TwitchTimeoutSubcommand extends TwitchSubcommand implements IDrawsChatOutline {

    public TwitchTimeoutSubcommand(StreamChatMod mod, ICommandNode<TwitchSubcommand> parentCommand) {
        super(mod, parentCommand);
    }

    @Override
    public @NotNull List<TwitchSubcommand> getSubcommands() {
        return Collections.emptyList();
    }

    @Override
    public @NotNull String getSubcommandName() {
        return "timeout";
    }

    @Override
    public @NotNull List<String> getSubcommandAliases() {
        return Arrays.asList("time", "mute");
    }

    @Override
    public @NotNull String getSubcommandUsage() {
        return "timeout <user> <time> [reason]";
    }

    @Override
    public @NotNull String getDescription() {
        return "Timeouts the user in the currently selected channel";
    }

    @Override
    public TwitchSubcommandCategory getCategory() {
        return TwitchSubcommandCategory.MODERATION;
    }

    @Override
    public boolean hasParameters() {
        return true;
    }

    @Override
    public void processSubcommand(ICommandSender sender, String[] args) throws CommandException {
        String channel = mod.config.twitchSelectedChannel.getString();
        if (mod.twitch == null || !mod.config.twitchEnabled.getBoolean())
            throw new CommandException("Twitch chat is disabled!");
        if (channel.length() == 0)
            throw new CommandException("No selected channel. Use /twitch channels select <channel> to select one.");
        if (args.length == 0)
            throw new CommandException("Missing required parameters: user to unban & time to timeout for");
        if (args.length == 1) throw new CommandException("Missing required parameter: time to timeout for");
        Duration dur = parseDuration(args[1]);
        if (dur == null)
            throw new CommandException("Could not parse " + args[1] + " to a Duration. Use a whole number of seconds or the ISO 8601 format.");
        mod.twitch.getChat().timeout(channel, args[0], dur, String.join(" ", Arrays.asList(args).subList(2, args.length)));
        StreamUtils.addMessage(EnumChatFormatting.GRAY + "Timing out " + EnumChatFormatting.BOLD + args[0] + EnumChatFormatting.GRAY + " from " + EnumChatFormatting.BOLD + channel + EnumChatFormatting.GRAY + "'s chat for " + dur.getSeconds() + " seconds..." + (args.length >= 3 ? " Reason: " + EnumChatFormatting.BOLD + String.join(" ", Arrays.asList(args).subList(2, args.length)) : ""));
    }

    @Override
    public void drawChatOutline(GuiChat gui, String[] args) {
        String channel = mod.config.twitchSelectedChannel.getString();
        Duration time = args.length > 1 ? parseDuration(args[1]) : null;
        String timeSeconds = time == null ? EnumChatFormatting.RED+"an invalid duration" : time.getSeconds()+" seconds";
        if (mod.twitch == null || !mod.config.twitchEnabled.getBoolean())
            StreamUtils.drawChatWarning(gui, StreamUtils.RED, StreamUtils.BACKGROUND, EnumChatFormatting.RED+"Twitch chat is disabled!");
        else if (channel.length() == 0)
            StreamUtils.drawChatWarning(gui, StreamUtils.RED, StreamUtils.BACKGROUND, EnumChatFormatting.RED+"No Twitch channel selected!");
        else if (args.length == 0)
            StreamUtils.drawChatWarning(gui, StreamUtils.RED, StreamUtils.BACKGROUND, EnumChatFormatting.LIGHT_PURPLE+"Timing out in "+EnumChatFormatting.AQUA+channel+EnumChatFormatting.LIGHT_PURPLE+"'s chat "+EnumChatFormatting.RED+"(missing user to timeout & duration parameters)");
        else if (args.length == 1)
            StreamUtils.drawChatWarning(gui, StreamUtils.RED, StreamUtils.BACKGROUND, EnumChatFormatting.LIGHT_PURPLE+"Timing out "+EnumChatFormatting.AQUA+args[0]+EnumChatFormatting.LIGHT_PURPLE+" from "+EnumChatFormatting.AQUA+channel+EnumChatFormatting.LIGHT_PURPLE+"'s chat "+EnumChatFormatting.RED+"(missing duration parameter)");
        else if (args.length == 2)
            StreamUtils.drawChatWarning(gui, time == null ? StreamUtils.RED : StreamUtils.PURPLE, StreamUtils.BACKGROUND, EnumChatFormatting.LIGHT_PURPLE+"Timing out "+EnumChatFormatting.AQUA+args[0]+EnumChatFormatting.LIGHT_PURPLE+" from "+EnumChatFormatting.AQUA+channel+EnumChatFormatting.LIGHT_PURPLE+"'s chat for "+EnumChatFormatting.AQUA+timeSeconds);
        else
            StreamUtils.drawChatWarning(gui, time == null ? StreamUtils.RED : StreamUtils.PURPLE, StreamUtils.BACKGROUND, EnumChatFormatting.LIGHT_PURPLE+"Timing out "+EnumChatFormatting.AQUA+args[0]+EnumChatFormatting.LIGHT_PURPLE+" from "+EnumChatFormatting.AQUA+channel+EnumChatFormatting.LIGHT_PURPLE+"'s chat for "+EnumChatFormatting.AQUA+timeSeconds+EnumChatFormatting.LIGHT_PURPLE+" with reason \""+EnumChatFormatting.AQUA+String.join(" ", Arrays.asList(args).subList(2, args.length))+EnumChatFormatting.LIGHT_PURPLE+"\"");
    }

    private static Duration parseDuration(String arg) {
        Duration dur;
        try {
            dur = Duration.parse(arg);
        } catch (DateTimeParseException e) {
            try {
                dur = Duration.ofSeconds(Integer.parseInt(arg));
            } catch (NumberFormatException ee) {
                return null;
            }
        }
        return dur;
    }
}

package me.mini_bomba.streamchatmod.commands.subcommands;

import com.github.twitch4j.chat.TwitchChat;
import me.mini_bomba.streamchatmod.StreamChatMod;
import me.mini_bomba.streamchatmod.StreamUtils;
import me.mini_bomba.streamchatmod.commands.ICommandNode;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class TwitchChannelListSubcommand extends TwitchSubcommand {

    public TwitchChannelListSubcommand(StreamChatMod mod, ICommandNode<TwitchSubcommand> parentCommand) {
        super(mod, parentCommand);
    }

    @Override
    public @NotNull List<TwitchSubcommand> getSubcommands() {
        return Collections.emptyList();
    }

    @Override
    public @NotNull String getSubcommandName() {
        return "list";
    }

    @Override
    public @NotNull List<String> getSubcommandAliases() {
        return Arrays.asList("show", "ls");
    }

    @Override
    public @NotNull String getSubcommandUsage() {
        return "list";
    }

    @Override
    public @NotNull String getDescription() {
        return "Lists the joined Twitch channels";
    }

    @Override
    public TwitchSubcommandCategory getCategory() {
        return null;
    }

    @Override
    public void processSubcommand(ICommandSender sender, String[] args) throws CommandException {
        TwitchChat chat = mod.twitch != null ? mod.twitch.getChat() : null;
        if (chat == null) throw new CommandException("Please enable Twitch chat first!");
        Set<String> channels = chat.getChannels();
        IChatComponent[] components = new IChatComponent[channels.size()];
        StreamUtils.addMessage(sender, EnumChatFormatting.GREEN+"Currently joined stream chats ("+channels.size()+"):");
        int i = 0;
        for (String channel : channels) {
            components[i] = new ChatComponentText(EnumChatFormatting.AQUA+"• "+channel);
            components[i].setChatStyle(new ChatStyle().setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText(EnumChatFormatting.RED+"Click to leave channel")))
                    .setChatClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/twitch channel leave "+channel)));
            i++;
        }
        StreamUtils.addMessages(sender, components);
    }
}

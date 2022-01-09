package me.mini_bomba.streamchatmod;

import com.github.twitch4j.helix.domain.User;
import me.mini_bomba.streamchatmod.commands.IDrawsChatOutline;
import me.mini_bomba.streamchatmod.events.LocalMessageEvent;
import me.mini_bomba.streamchatmod.runnables.TwitchMessageHandler;
import me.mini_bomba.streamchatmod.tweaker.TransformerField;
import me.mini_bomba.streamchatmod.utils.ChatComponentStreamEmote;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

public class StreamEvents {

    private final StreamChatMod mod;
    private final Logger LOGGER = LogManager.getLogger();
    private Field GuiChat_inputField = null;

    public StreamEvents(StreamChatMod mod) {
        this.mod = mod;
        try {
            GuiChat_inputField = GuiChat.class.getDeclaredField(TransformerField.GuiChat_inputField.getReflectorName());
            GuiChat_inputField.setAccessible(true);
        } catch (Exception e) {
            LOGGER.error("Failed to reflect GuiChat.inputField");
            e.printStackTrace();
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event){
        if (mod.loginMessageTimer == 0) {
            mod.printTwitchStatus(true);
        }
        if (mod.loginMessageTimer >= 0) mod.loginMessageTimer--;
    }

    @SubscribeEvent
    public void onEnterWorld(FMLNetworkEvent.ClientConnectedToServerEvent event) {
        mod.loginMessageTimer = 60;
    }

    @SubscribeEvent
    public void onGuiChatRender(GuiScreenEvent.DrawScreenEvent.Post event) {
        if (!(event.gui instanceof GuiChat)) return;
        GuiChat gui = (GuiChat) event.gui;
        String text = "";
        try {
            if (GuiChat_inputField != null) text = ((GuiTextField) GuiChat_inputField.get(gui)).getText();
        } catch (Exception ignored) {}
        String modePrefix = mod.config.minecraftChatPrefix.getString();
        if (!text.startsWith("/") && (modePrefix.length() == 0 || !text.startsWith(modePrefix)) && mod.config.twitchMessageRedirectEnabled.getBoolean()) {
            if (mod.twitch == null || mod.twitchSender == null || !mod.config.twitchEnabled.getBoolean())
                StreamUtils.drawChatWarning(gui, StreamUtils.RED, StreamUtils.BACKGROUND, EnumChatFormatting.RED+"Twitch chat is disabled!");
            else if (mod.config.twitchSelectedChannel.getString().length() == 0)
                StreamUtils.drawChatWarning(gui, StreamUtils.RED, StreamUtils.BACKGROUND, EnumChatFormatting.RED+"No Twitch channel selected!");
            else {
                String warning = EnumChatFormatting.LIGHT_PURPLE + "Twitch chat mode enabled - Messages forwarded to " + EnumChatFormatting.AQUA + mod.config.twitchSelectedChannel.getString() + EnumChatFormatting.LIGHT_PURPLE + "'s chat " + EnumChatFormatting.GRAY + "(/twitch mode)";
                StreamUtils.drawChatWarning(gui, StreamUtils.PURPLE, StreamUtils.BACKGROUND, warning);
            }
        } else if (modePrefix.length() != 0 && text.startsWith(modePrefix)) {
            String warning = EnumChatFormatting.GREEN + "Message starts with " + EnumChatFormatting.GRAY + modePrefix + EnumChatFormatting.GREEN + " - message will be sent to the server" + EnumChatFormatting.GRAY + " (/twitch mp)";
            StreamUtils.drawChatWarning(gui, StreamUtils.GREEN, StreamUtils.BACKGROUND, warning);
        } else if (text.startsWith("/tc") || text.startsWith("/twitchchat")) {
            if (mod.twitch == null || mod.twitchSender == null || !mod.config.twitchEnabled.getBoolean())
                StreamUtils.drawChatWarning(gui, StreamUtils.RED, StreamUtils.BACKGROUND, EnumChatFormatting.RED+"Twitch chat is disabled!");
            else if (mod.config.twitchSelectedChannel.getString().length() == 0)
                StreamUtils.drawChatWarning(gui, StreamUtils.RED, StreamUtils.BACKGROUND, EnumChatFormatting.RED+"No Twitch channel selected!");
            else {
                String warning = EnumChatFormatting.LIGHT_PURPLE + "Sending message to " + EnumChatFormatting.AQUA + mod.config.twitchSelectedChannel.getString() + EnumChatFormatting.LIGHT_PURPLE + "'s chat";
                StreamUtils.drawChatWarning(gui, StreamUtils.PURPLE, StreamUtils.BACKGROUND, warning);
            }
        } else if (text.startsWith("/twitch")) {
            String[] args = text.split(" ");
            if (args.length > 1 && mod.twitchCommand.subcommandMapWithChatOutlines.containsKey(args[1].toLowerCase())) {
                IDrawsChatOutline subcommand = mod.twitchCommand.subcommandMapWithChatOutlines.get(args[1].toLowerCase());
                subcommand.drawChatOutline(gui, Arrays.copyOfRange(args, 2, args.length));
            }
        }
    }

    @SubscribeEvent
    public void onLocalMinecraftMessage(LocalMessageEvent event) {
        if (event.message.startsWith("/")) return;
        if (!mod.config.twitchMessageRedirectEnabled.getBoolean()) return;
        String modePrefix = mod.config.minecraftChatPrefix.getString();
        if (modePrefix.length() != 0 && event.message.startsWith(modePrefix)) {
            event.message = event.message.substring(modePrefix.length());
            return;
        }
        event.setCanceled(true);
        if (mod.twitch == null || mod.twitchSender == null || !mod.config.twitchEnabled.getBoolean() || mod.twitchSender.getChat() == null) {
            StreamUtils.addMessage(EnumChatFormatting.RED + "The message was not sent anywhere: Chat mode is set to 'Redirect to Twitch', but Twitch chat (or part of it) is disabled!");
            return;
        }
        if (mod.config.twitchSelectedChannel.getString().length() == 0) {
            StreamUtils.addMessage(EnumChatFormatting.RED + "The message was not sent anywhere: Chat mode is set to 'Redirect to Twitch', but no channel is selected!");
            return;
        }
        mod.twitchSender.getChat().sendMessage(mod.config.twitchSelectedChannel.getString(), event.message);
    }

    @SubscribeEvent
    public void onMessage(ClientChatReceivedEvent event) {
        String channel = mod.config.twitchSelectedChannel.getString();
        if (channel.length() == 0) channel = mod.getTwitchUsername();
        if (mod.config.showEmotesEverywhere.getBoolean()) {
            User user = channel == null ? null : mod.getTwitchUserByName(channel);
            event.message = transformComponent(event.message, user == null ? null : user.getId());
        }
    }

    public IChatComponent transformComponent(IChatComponent component, String channelId) {
        IChatComponent newComponent;
        if (component instanceof ChatComponentText) {
            List<IChatComponent> components = TwitchMessageHandler.processEmotes(mod, component.getUnformattedTextForChat(), channelId);
            components.stream().filter(c -> !(c instanceof ChatComponentStreamEmote)).forEach(c -> c.setChatStyle(component.getChatStyle().createShallowCopy()));
            if (components.size() > 0) {
                newComponent = components.get(0);
                components.remove(0);
                for (IChatComponent c : components) newComponent.appendSibling(c);
            } else {
                newComponent = new ChatComponentText("");
                newComponent.setChatStyle(component.getChatStyle().createShallowCopy());
            }
        } else if (component instanceof ChatComponentTranslation) {
            ChatComponentTranslation castedComponent = (ChatComponentTranslation) component;
            newComponent = new ChatComponentTranslation(castedComponent.getKey(), Arrays.stream(castedComponent.getFormatArgs()).map(c -> c instanceof IChatComponent ? transformComponent((IChatComponent) c, channelId) : c).toArray());
            newComponent.setChatStyle(castedComponent.getChatStyle().createShallowCopy());
        } else {
            newComponent = component.createCopy();
            newComponent.getSiblings().clear();
        }
        for (IChatComponent sibling : component.getSiblings()) {
            newComponent.appendSibling(transformComponent(sibling, channelId));
        }
        return newComponent;
    }
}

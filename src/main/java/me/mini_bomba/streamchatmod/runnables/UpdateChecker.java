package me.mini_bomba.streamchatmod.runnables;

import me.mini_bomba.streamchatmod.StreamChatMod;
import me.mini_bomba.streamchatmod.StreamUtils;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static me.mini_bomba.streamchatmod.StreamChatMod.PRERELEASE;

public class UpdateChecker implements Runnable {
    private final StreamChatMod mod;
    private static final Logger LOGGER = LogManager.getLogger();

    public UpdateChecker(StreamChatMod mod) {
        this.mod = mod;
    }

    @Override
    public void run() {
        LOGGER.info("Update checker started");
        while (true) {
            try {
                Thread.sleep((long) (15*60000));
            } catch (InterruptedException e) {
                LOGGER.warn("UpdateChecker sleep interrupted: this may be an error, or user requested disable.");
                break;
            }
            String latestVersion = StreamUtils.getLatestVersion();
            StreamUtils.GitCommit latestCommit = null;
            if (PRERELEASE) {
                latestCommit = StreamUtils.getLatestCommit();
            }
            if ((latestVersion != null && mod.latestVersion != null && !latestVersion.equals(mod.latestVersion)) || (latestCommit != null && mod.latestCommit != null && !latestCommit.shortHash.equals(mod.latestCommit.shortHash))) {
                mod.latestVersion = latestVersion;
                mod.latestCommit = latestCommit;
                LOGGER.warn("New version available: " + latestVersion + (latestCommit != null ? "@" + latestCommit.shortHash : "") + "!");
                IChatComponent component1 = new ChatComponentText(EnumChatFormatting.DARK_PURPLE + "[TWITCH] " + EnumChatFormatting.GOLD + "New update published: " + latestVersion + (PRERELEASE ? "@" + latestCommit.shortHash : ""));
                IChatComponent component2 = null;
                if (PRERELEASE && latestCommit != null) component2 = new ChatComponentText(EnumChatFormatting.DARK_PURPLE + "[TWITCH] " +EnumChatFormatting.GRAY + "Update commit message: " + EnumChatFormatting.AQUA + latestCommit.shortMessage);
                IChatComponent component3 = new ChatComponentText("" + EnumChatFormatting.GRAY + EnumChatFormatting.ITALIC + "Want to check for updates only on startup? Click here!");
                ChatStyle style = new ChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://github.com/mini-bomba/StreamChatMod/releases"))
                                .setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText(EnumChatFormatting.GREEN + "Click here to see mod releases on GitHub!")));
                component1.setChatStyle(style);
                if (component2 != null) component2.setChatStyle(style);
                style = new ChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/twitch updatechecker disable"))
                                .setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText(EnumChatFormatting.GRAY + "Use " + EnumChatFormatting.DARK_GRAY + "/twitch updatechecker disable" + EnumChatFormatting.GRAY + " to disable, or " + EnumChatFormatting.DARK_GRAY + "/twitch updatechecker enable" + EnumChatFormatting.GRAY + " to enable")));
                component3.setChatStyle(style);
                if (component2 != null)
                    StreamUtils.queueAddMessages(new IChatComponent[]{component1, component2, component3});
                else
                    StreamUtils.queueAddMessages(new IChatComponent[]{component1, component3});
            } else
                LOGGER.info("Mod is up to date!");
        }
        if (Thread.currentThread() == mod.updateCheckerThread)
            mod.updateCheckerThread = null;
    }
}

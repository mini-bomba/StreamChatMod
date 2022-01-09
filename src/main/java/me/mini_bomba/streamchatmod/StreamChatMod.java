package me.mini_bomba.streamchatmod;

import com.github.philippheuer.credentialmanager.CredentialManager;
import com.github.philippheuer.credentialmanager.CredentialManagerBuilder;
import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.TwitchClientBuilder;
import com.github.twitch4j.auth.providers.TwitchIdentityProvider;
import com.github.twitch4j.chat.TwitchChat;
import com.github.twitch4j.chat.enums.NoticeTag;
import com.github.twitch4j.chat.events.channel.*;
import com.github.twitch4j.helix.domain.*;
import com.github.twitch4j.tmi.domain.Chatters;
import com.sun.net.httpserver.HttpServer;
import me.mini_bomba.streamchatmod.asm.hooks.FontRendererHook;
import me.mini_bomba.streamchatmod.commands.TwitchChatCommand;
import me.mini_bomba.streamchatmod.commands.TwitchCommand;
import me.mini_bomba.streamchatmod.runnables.TwitchFollowSoundScheduler;
import me.mini_bomba.streamchatmod.runnables.TwitchMessageHandler;
import me.mini_bomba.streamchatmod.utils.Cache;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.ModMetadata;
import net.minecraftforge.fml.common.ProgressManager;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLModDisabledEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import rx.schedulers.Timestamped;

import java.util.*;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@SuppressWarnings({"ConstantConditions", "unused"})
@Mod(modid = StreamChatMod.MODID, version = StreamChatMod.VERSION, clientSideOnly = true)
public class StreamChatMod {
    public static final String MODID = "streamchatmod";
    public static final String MODNAME = "StreamChatMod";
    public static final String VERSION = "@VERSION@";
    public static final String GIT_HASH = "@GIT_HASH@";
    @SuppressWarnings("MismatchedStringCase")
    public static final boolean PRERELEASE = "@PRERELEASE@".equals("true");
    private static final Logger LOGGER = LogManager.getLogger();
    public StreamConfig config;
    public StreamKeybinds keybinds;
    @Nullable
    public String latestVersion = null;
    // LatestCommit is set only on prerelease builds
    @Nullable
    public StreamUtils.GitCommit latestCommit = null;
    @Nullable
    public TwitchClient twitch = null;
    @Nullable
    public TwitchClient twitchSender = null;
    @Nullable
    private CredentialManager twitchCredentialManager = null;
    @Nullable
    private String twitchUsername = null;
    @Nullable
    private List<String> twitchScopes = null;
    @Nullable
    public HttpServer httpServer = null;
    public Thread httpShutdownScheduler = null;
    public int loginMessageTimer = -1;

    // Executor for async actions
    private ScheduledThreadPoolExecutor asyncExecutor;

    // Flag for scheduling actions that may break other actions, such as Twitch client stopping/starting
    private final AtomicBoolean importantActionScheduled = new AtomicBoolean(false);

    // The update checker future, scheduled via the asyncExecutor
    public ScheduledFuture<?> updateChecker = null;

    private final StreamEvents events;
    public final StreamEmotes emotes;
    protected final TwitchCommand twitchCommand;

    // Caches for Twitch clips, users, etc.
    public final Cache<String, Game> categoryCache = new Cache<>(8);
    public final Cache<String, Clip> clipCache = new Cache<>(16);
    public final Cache<String, User> userCache = new Cache<>(32);
    public final Cache<String, User> userCacheByNames = new Cache<>(32);
    public final Cache<String, Timestamped<Chatters>> timestampedChatterCache = new Cache<>(4);

    // Cooldown for /twitch clip
    private long lastClipCreated = 0;

    public StreamChatMod() {
        events = new StreamEvents(this);
        emotes = new StreamEmotes(this);
        keybinds = new StreamKeybinds(this);
        twitchCommand = new TwitchCommand(this);
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        ProgressManager.ProgressBar progress = ProgressManager.push("Starting up", 4);
        progress.step("Checking for updates");
        LOGGER.info("Checking for updates...");
        latestVersion = StreamUtils.getLatestVersion();
        if (PRERELEASE) {
            latestCommit = StreamUtils.getLatestCommit();
        }
        if ((latestVersion != null && !latestVersion.equals(VERSION)) || (latestCommit != null && !latestCommit.shortHash.equals(GIT_HASH)))
            LOGGER.warn("New version available: " + latestVersion + (latestCommit != null ? "@" + latestCommit.shortHash : "") + "!");
        else
            LOGGER.info("Mod is up to date!");
        progress.step("Starting Twitch client");
        startTwitch(false);
        progress.step("Starting async thread");
        asyncExecutor = new ScheduledThreadPoolExecutor(1);
        if (config.updateCheckerEnabled.getBoolean()) startUpdateChecker();
        progress.step("Syncing emote cache");
        if (twitch != null) {
            ProgressManager.ProgressBar emoteProgress = ProgressManager.push("Syncing emotes", 11);
            List<String> channelIds = Arrays.stream(config.twitchChannels.getStringList()).map(this::getTwitchUserByName).map(User::getId).collect(Collectors.toList());
            emotes.syncGlobalBadges(emoteProgress, false);
            emotes.syncAllChannelBadges(emoteProgress, channelIds, false);
            emotes.syncGlobalEmotes(emoteProgress, false);
            emotes.syncAllChannelEmotes(emoteProgress, channelIds, false);
            ProgressManager.pop(emoteProgress);
        }
        ProgressManager.pop(progress);
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        ClientCommandHandler commandHandler = ClientCommandHandler.instance;
        commandHandler.registerCommand(new TwitchChatCommand(this));
        commandHandler.registerCommand(twitchCommand);
        keybinds.registerKeybindings();

        MinecraftForge.EVENT_BUS.register(events);
        MinecraftForge.EVENT_BUS.register(keybinds);
    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        ModMetadata metadata = event.getModMetadata();
        metadata.autogenerated = false;
        metadata.name = MODNAME;
        metadata.authorList = Collections.singletonList("mini_bomba");
        metadata.description = "A Chat client for Twitch in minecraft because yes";
        metadata.url = "https://github.com/mini-bomba/StreamChatMod";
        if (PRERELEASE) {
            metadata.version = GIT_HASH;
            metadata.description += "\n\nYou are running SCM prerelease built from git commit " + GIT_HASH;
        } else {
            metadata.version = VERSION;
            metadata.description += "\n\nYou are running SCM release version " + VERSION + " built from git commit " + GIT_HASH;
        }
        config = new StreamConfig(event.getSuggestedConfigurationFile());
        FontRendererHook.setAllowAnimated(config.allowAnimatedEmotes.getBoolean());
    }

    @EventHandler
    public void stop(FMLModDisabledEvent event) {
        stopUpdateChecker();
        stopTwitch();
        config.saveIfChanged();
        asyncExecutor.shutdown();
        boolean terminated = false;
        try {
            terminated = asyncExecutor.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException ignored) {
        }
        if (!terminated) {
            LOGGER.warn("The async executor did not terminate after 5 seconds! Calling .shutdownNow()");
            asyncExecutor.shutdownNow();
            try {
                terminated = asyncExecutor.awaitTermination(10, TimeUnit.SECONDS);
            } catch (InterruptedException ignored) {
            }
            if (!terminated) LOGGER.error("The async executor did not terminate after 15 seconds!");
        }
    }

    public void checkUpdates() {
        String newLatestVersion = StreamUtils.getLatestVersion();
        StreamUtils.GitCommit newLatestCommit = null;
        if (PRERELEASE) {
            newLatestCommit = StreamUtils.getLatestCommit();
        }
        if ((newLatestVersion != null && latestVersion != null && !newLatestVersion.equals(latestVersion)) || (newLatestCommit != null && latestCommit != null && !newLatestCommit.shortHash.equals(latestCommit.shortHash))) {
            latestVersion = newLatestVersion;
            latestCommit = newLatestCommit;
            LOGGER.warn("New version available: " + newLatestVersion + (newLatestCommit != null ? "@" + newLatestCommit.shortHash : "") + "!");
            IChatComponent component1 = StreamUtils.createPrefixedComponent(config, EnumChatFormatting.GOLD + "New update published: " + newLatestVersion + (PRERELEASE ? "@" + newLatestCommit.shortHash : ""));
            IChatComponent component2 = null;
            if (PRERELEASE && newLatestCommit != null)
                component2 = StreamUtils.createPrefixedComponent(config, EnumChatFormatting.GRAY + "Update commit message: " + EnumChatFormatting.AQUA + newLatestCommit.shortMessage);
            IChatComponent component3 = StreamUtils.createPrefixedComponent(config, "" + EnumChatFormatting.GRAY + EnumChatFormatting.ITALIC + "Want to check for updates only on startup? Click here!");
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

    public void startUpdateChecker(boolean checkNow) {
        if (updateChecker == null)
            updateChecker = asyncExecutor.scheduleWithFixedDelay(this::checkUpdates, checkNow ? 0 : 15, 15, TimeUnit.MINUTES);
    }

    public void startUpdateChecker() {
        startUpdateChecker(false);
    }

    public void stopUpdateChecker() {
        if (updateChecker != null) {
            updateChecker.cancel(false);
            updateChecker = null;
        }
    }

    protected List<Emote> queryGlobalTwitchEmotes() {
        if (twitch == null) {
            LOGGER.warn("Could not get global Twitch emotes: Twitch client is disabled");
            return Collections.emptyList();
        }
        return twitch.getHelix().getGlobalEmotes(null).execute().getEmotes();
    }

    protected List<ChatBadgeSet> queryGlobalTwitchBadges() {
        if (twitch == null) {
            LOGGER.warn("Could not get global Twitch badges: Twitch client is disabled");
            return Collections.emptyList();
        }
        return twitch.getHelix().getGlobalChatBadges(null).execute().getBadgeSets();
    }

    protected List<ChatBadgeSet> queryChannelTwitchBadges(String channelId) {
        if (twitch == null) {
            LOGGER.warn("Could not get channel Twitch badges: Twitch client is disabled");
            return Collections.emptyList();
        }
        return twitch.getHelix().getChannelChatBadges(null, channelId).execute().getBadgeSets();
    }

    /**
     * Schedules an action to be run in another thread.<br>
     * <b>This will throw a ConcurrentModificationException if an important action is scheduled</b> (such as Twitch client stopping)<br>
     * Use isImportantActionScheduled() to check if this is the case.
     *
     * @param action      action to schedule
     * @param isImportant can this action cause other actions to fail?
     */
    private void asyncTwitchAction(Runnable action, boolean isImportant) throws ConcurrentModificationException {
        if (importantActionScheduled.get())
            throw new ConcurrentModificationException("An important async action is currently scheduled!");
        if (isImportant) {
            importantActionScheduled.set(true);
            Runnable oldAction = action;
            action = () -> {
                oldAction.run();
                importantActionScheduled.set(false);
            };
        }
        asyncExecutor.execute(action);
    }

    private void asyncTwitchAction(Runnable action) throws ConcurrentModificationException {
        asyncTwitchAction(action, false);
    }

    /**
     * Checks if an important async action is scheduled (such as Twitch client stopping)
     *
     * @return is an important action scheduled?
     */
    public boolean isImportantActionScheduled() {
        return importantActionScheduled.get();
    }

    public void asyncStartTwitch() throws ConcurrentModificationException {
        asyncTwitchAction(() -> {
            if (startTwitch()) {
                StreamUtils.queueAddMessage(EnumChatFormatting.GREEN + "Enabled the Twitch Chat!");
                checkScopes();
            } else
                StreamUtils.queueAddMessage(EnumChatFormatting.RED + "Could not start the Twitch client, the token may be invalid!");
        }, true);
    }

    public void asyncStopTwitch() throws ConcurrentModificationException {
        asyncTwitchAction(() -> {
            stopTwitch();
            StreamUtils.queueAddMessage(EnumChatFormatting.GREEN + "Disabled the Twitch Chat!");
        }, true);
    }

    public void asyncRestartTwitch() throws ConcurrentModificationException {
        asyncTwitchAction(() -> {
            stopTwitch();
            if (startTwitch()) {
                StreamUtils.queueAddMessage(EnumChatFormatting.GREEN + "Restarted the Twitch Chat!");
                checkScopes();
            } else
                StreamUtils.queueAddMessage(EnumChatFormatting.RED + "Could not restart the Twitch client, the token may be invalid!");
        }, true);
    }

    public void asyncRevokeTwitchToken() throws ConcurrentModificationException {
        asyncTwitchAction(() -> {
            stopTwitch();
            config.twitchEnabled.set(false);
            boolean revoked = config.revokeTwitchToken();
            if (revoked) {
                config.setTwitchToken("");
                StreamUtils.queueAddMessage(EnumChatFormatting.GREEN + "The token has been revoked!");
            } else {
                StreamUtils.queueAddMessage(EnumChatFormatting.RED + "Could not revoke the token! It may be invalid, or the request could not have been sent!");
            }
            config.saveIfChanged();
        }, true);
    }

    public void asyncJoinTwitchChannel(String channel) throws ConcurrentModificationException {
        asyncTwitchAction(() -> {
            if (twitch == null) {
                StreamUtils.queueAddMessage(EnumChatFormatting.RED + "Twitch chat is not enabled!");
                return;
            }
            TwitchChat chat = twitch.getChat();
            if (chat == null) {
                StreamUtils.queueAddMessage(EnumChatFormatting.RED + "Twitch chat is not enabled!");
                return;
            }
            chat.joinChannel(channel);
            if (!chat.isChannelJoined(channel)) {
                StreamUtils.queueAddMessage(EnumChatFormatting.RED + "Something went wrong: Could not join the channel.");
                return;
            }
            if (config.followEventEnabled.getBoolean()) twitch.getClientHelper().enableFollowEventListener(channel);
            config.twitchChannels.set(java.util.stream.Stream.concat(Arrays.stream(config.twitchChannels.getStringList()), java.util.stream.Stream.of(channel)).map(String::toLowerCase).distinct().toArray(String[]::new));
            config.saveIfChanged();
            StreamUtils.queueAddMessage(EnumChatFormatting.GRAY + "Syncing " + channel + "'s channel badges...");
            emotes.syncChannelBadges(getTwitchUserByName(channel).getId(), true);
            StreamUtils.queueAddMessage(EnumChatFormatting.GRAY + "Syncing " + channel + "'s channel emotes...");
            emotes.syncChannelEmotes(getTwitchUserByName(channel).getId(), true);
            StreamUtils.queueAddMessage(EnumChatFormatting.GREEN + "Joined " + channel + "'s chat!");
        });
    }

    public void asyncLeaveTwitchChannel(String channel) throws ConcurrentModificationException {
        asyncTwitchAction(() -> {
            if (twitch == null) { StreamUtils.queueAddMessage(EnumChatFormatting.RED + "Twitch chat is not enabled!"); return; }
            TwitchChat chat = twitch.getChat();
            if (chat == null) { StreamUtils.queueAddMessage(EnumChatFormatting.RED + "Twitch chat is not enabled!"); return; }
            chat.leaveChannel(channel);
            if (chat.isChannelJoined(channel)) { StreamUtils.queueAddMessage(EnumChatFormatting.RED + "Something went wrong: Could not leave the channel."); return; }
            if (config.followEventEnabled.getBoolean()) twitch.getClientHelper().disableFollowEventListener(channel);
            config.twitchChannels.set(Arrays.stream(config.twitchChannels.getStringList()).filter(c -> !c.equalsIgnoreCase(channel)).toArray(String[]::new));
            config.saveIfChanged();
            StreamUtils.queueAddMessage(EnumChatFormatting.GREEN+"Left "+channel+"'s chat!");
        });
    }

    public void asyncUpdateFollowEvents() throws ConcurrentModificationException {
        asyncTwitchAction(() -> {
            if (twitch == null) { StreamUtils.queueAddMessage(EnumChatFormatting.RED + "Twitch chat is not enabled!"); return; }
            List<String> channels = Arrays.asList(config.twitchChannels.getStringList());
            if (config.followEventEnabled.getBoolean())
                twitch.getClientHelper().enableFollowEventListener(channels);
            else
                twitch.getClientHelper().disableFollowEventListener(channels);
            StreamUtils.queueAddMessage(EnumChatFormatting.GREEN+"Follow event listeners updated!");
        });
    }

    public void createMarker(String description, String broadcasterId) {
        if (twitch == null) { StreamUtils.queueAddMessage(EnumChatFormatting.RED + "Twitch chat is not enabled!"); return; }
        User broadcaster = getTwitchUserById(broadcasterId);
        StreamMarker marker;
        try {
            Highlight highlight;
            if (description == null) {
                highlight = new Highlight(broadcasterId);
            } else {
                highlight = new Highlight(broadcasterId, description);
            }
            marker = twitch.getHelix().createStreamMarker(null, highlight).execute();
        } catch (Exception e) {
            StreamUtils.queueAddMessages(new String[]{
                    EnumChatFormatting.RED+"Failed to create marker on "+broadcaster.getDisplayName()+"'s stream: "+e.getClass().getName()+": "+e.getMessage(),
                    ""+EnumChatFormatting.GRAY+EnumChatFormatting.ITALIC+"Make sure they are streaming and that you have editor permissions on their channel!",
                    ""+EnumChatFormatting.GRAY+EnumChatFormatting.ITALIC+"If error persists, try regenerating your token using /twitch token."
            });
            return;
        }
        String seconds = String.valueOf(marker.getPositionSeconds() % 60);
        String minutes = String.valueOf(marker.getPositionSeconds() / 60 % 60);
        String hours = String.valueOf(marker.getPositionSeconds() / 3600);
        seconds = (seconds.length() < 2 ? "0" : "") + seconds;
        minutes = (minutes.length() < 2 ? "0" : "") + minutes;
        hours = (hours.length() < 2 ? "0" : "") + hours;
        StreamUtils.queueAddMessage(EnumChatFormatting.GREEN+"Successfully created a marker on "+broadcaster.getDisplayName()+"'s stream at "+hours+":"+minutes+":"+seconds);
    }

    public void createMarker(String description) {
        if (twitch == null) { StreamUtils.queueAddMessage(EnumChatFormatting.RED + "Twitch chat is not enabled!"); return; }
        User broadcaster = getSelectedChannelUser();
        if (broadcaster == null) StreamUtils.addMessage(EnumChatFormatting.RED+"Could not find ID of current selected channel, "+config.twitchSelectedChannel.getString());
        else createMarker(description, broadcaster.getId());
    }

    public void createMarker() {
        if (twitch == null) { StreamUtils.queueAddMessage(EnumChatFormatting.RED + "Twitch chat is not enabled!"); return; }
        User broadcaster = getSelectedChannelUser();
        if (broadcaster == null) StreamUtils.addMessage(EnumChatFormatting.RED+"Could not find ID of current selected channel, "+config.twitchSelectedChannel.getString());
        else createMarker(broadcaster.getId());
    }

    public void asyncCreateMarker(String description, String broadcasterId) throws ConcurrentModificationException {
        asyncTwitchAction(() -> createMarker(description, broadcasterId));
    }

    public void asyncCreateMarker(String description) throws ConcurrentModificationException {
        asyncTwitchAction(() -> createMarker(description));
    }

    public void asyncCreateMarker() throws ConcurrentModificationException {
        asyncTwitchAction(this::createMarker);
    }

    private void createClip(String broadcasterId, boolean copyToClipboard, boolean hasDelay) {
        if (lastClipCreated+(60000*2) > System.currentTimeMillis()) {
            StreamUtils.queueAddMessage(EnumChatFormatting.RED+"Please wait "+((lastClipCreated+(60000*2))-System.currentTimeMillis())/1000+" seconds before creating another clip.");
            return;
        }
        long lastClipCreatedCopy = lastClipCreated;
        lastClipCreated = System.currentTimeMillis();
        if (twitch == null) { StreamUtils.queueAddMessage(EnumChatFormatting.RED + "Twitch chat is not enabled!"); return; }
        try {
            List<CreateClip> newClips = twitch.getHelix().createClip(null, broadcasterId, hasDelay).execute().getData();
            if (newClips.size() == 0) {
                StreamUtils.queueAddMessage(EnumChatFormatting.RED+"Twitch API did not return any newClips! "+EnumChatFormatting.GRAY+"(Maybe try resetting your token with /twitch token?)");
                lastClipCreated = System.currentTimeMillis()-60*1000; // Set cooldown to 1 minute instead of 2 minutes
                return;
            }
            CreateClip newClip = newClips.get(0);
            List<Clip> clips = Collections.emptyList();
            for (int i = 0; i <= 15; i++) {
                clips = twitch.getHelix().getClips(null, null, null, newClip.getId(), null, null, null, null, null).execute().getData();
                if (clips.size() > 0) break;
                Thread.sleep(1000);
            }
            if (clips.size() == 0) {
                StreamUtils.queueAddMessage(EnumChatFormatting.RED+"Clip creation timed out :(");
                lastClipCreated = System.currentTimeMillis()-60*1000; // Set cooldown to 1 minute instead of 2 minutes
            }
            Clip clip = clips.get(0);
            IChatComponent mainComponent = new ChatComponentText(EnumChatFormatting.GREEN+"Clip created: ");
            IChatComponent clipComponent = new ChatComponentText(EnumChatFormatting.AQUA+clip.getUrl());
            ChatStyle style = new ChatStyle();
            style.setChatClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, clip.getUrl()));
            style.setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText(EnumChatFormatting.GREEN+"Click to open or copy clip URL")));
            clipComponent.setChatStyle(style);
            mainComponent.appendSibling(clipComponent);
            StreamUtils.queueAddMessage(mainComponent);
            if (copyToClipboard) GuiScreen.setClipboardString(clip.getUrl());
        } catch (Exception e) {
            lastClipCreated = System.currentTimeMillis() - 60 * 1000; // Set cooldown to 1 minute instead of 2 minutes
            LOGGER.error("Failed to create clip");
            e.printStackTrace();
        }
    }

    public void asyncCreateClip(String broadcasterId, boolean copyToClipboard, boolean hasDelay) throws ConcurrentModificationException {
        asyncTwitchAction(() -> createClip(broadcasterId, copyToClipboard, hasDelay));
    }

    public void asyncCreateClip(String broadcasterId, boolean copyToClipboard) throws ConcurrentModificationException {
        asyncTwitchAction(() -> createClip(broadcasterId, copyToClipboard, false));
    }

    public void asyncCreateClip(String broadcasterId) throws ConcurrentModificationException {
        asyncCreateClip(broadcasterId, false, false);
    }

    public void asyncCreateClip(boolean copyToClipboard, boolean hasDelay) throws ConcurrentModificationException {
        asyncTwitchAction(() -> {
            if (twitch == null) {
                StreamUtils.queueAddMessage(EnumChatFormatting.RED + "Twitch chat is not enabled!");
                return;
            }
            User broadcaster = getSelectedChannelUser();
            if (broadcaster == null)
                StreamUtils.addMessage(EnumChatFormatting.RED + "Could not find ID of current selected channel, " + config.twitchSelectedChannel.getString());
            else createClip(broadcaster.getId(), copyToClipboard, hasDelay);
        });
    }

    public void asyncCreateClip(boolean copyToClipboard) throws ConcurrentModificationException {
        asyncCreateClip(copyToClipboard, false);
    }

    public void asyncCreateClip() throws ConcurrentModificationException {
        asyncCreateClip(false, false);
    }

    private void showTwitchStreamStats(String broadcasterName) {
        if (twitch == null) {
            StreamUtils.queueAddMessage(EnumChatFormatting.RED + "Twitch chat is not enabled!");
            return;
        }
        try {
            List<Stream> streams = twitch.getHelix().getStreams(null, null, null, null, null, null, null, Collections.singletonList(broadcasterName)).execute().getStreams();
            if (streams.size() < 1) {
                StreamUtils.queueAddMessage("" + EnumChatFormatting.RED + EnumChatFormatting.BOLD + broadcasterName + EnumChatFormatting.RED + " is offline! " + EnumChatFormatting.GRAY + EnumChatFormatting.ITALIC + "(or doesn't exist)");
                return;
            }
            Stream stream = streams.get(0);
            long uptime = stream.getUptime().getSeconds();
            StreamUtils.queueAddMessages(new String[]{
                    "" + EnumChatFormatting.AQUA + EnumChatFormatting.BOLD + broadcasterName + EnumChatFormatting.DARK_AQUA + "'s stream stats:",
                    EnumChatFormatting.GRAY + "Stream Title: " + EnumChatFormatting.AQUA + stream.getTitle(),
                    EnumChatFormatting.GRAY + "Game: " + EnumChatFormatting.AQUA + stream.getGameName(),
                    EnumChatFormatting.GRAY + "Viewers: " + EnumChatFormatting.AQUA + stream.getViewerCount(),
                    EnumChatFormatting.GRAY + "Stream uptime: " + EnumChatFormatting.AQUA + (uptime / 3600) + " hours " + (uptime / 60 % 60) + " minutes " + (uptime % 60) + "seconds"
            });
        } catch (Exception e) {
            LOGGER.error("Failed to lookup stream");
            e.printStackTrace();
            StreamUtils.queueAddMessage(EnumChatFormatting.RED + "Failed to lookup " + broadcasterName + "'s stream, an unexpected error occurred.");
        }
    }

    public void asyncShowTwitchStreamStats(String broadcasterName) throws ConcurrentModificationException {
        asyncTwitchAction(() -> showTwitchStreamStats(broadcasterName));
    }

    public void asyncShowTwitchStreamStats() throws ConcurrentModificationException {
        String broadcaster = config.twitchSelectedChannel.getString();
        if (broadcaster.length() == 0) StreamUtils.addMessage(EnumChatFormatting.RED + "No channel is selected!");
        else asyncShowTwitchStreamStats(broadcaster);
    }

    public boolean startTwitch() {
        return startTwitch(true);
    }

    public boolean startTwitch(boolean syncEmotes) {
        if (twitch != null || !config.twitchEnabled.getBoolean()) return false;
        String token = config.twitchToken.getString();
        if (token.equals("")) return false;
        try {
            // Build the main TwitchClient
            twitchCredentialManager = CredentialManagerBuilder.builder().build();
            OAuth2Credential credential = new OAuth2Credential("twitch", token);
            twitch = TwitchClientBuilder.builder()
                    .withCredentialManager(twitchCredentialManager)
                    .withDefaultAuthToken(credential)
                    .withEnableChat(true)
                    .withChatAccount(credential)
                    .withEnableHelix(true)
                    .withEnableTMI(true)
                    .build();
            if (syncEmotes) {
                List<String> channelIds = Arrays.stream(config.twitchChannels.getStringList()).map(this::getTwitchUserByName).map(User::getId).collect(Collectors.toList());
                StreamUtils.queueAddMessage(EnumChatFormatting.GRAY + "Synchronising global badge cache...");
                emotes.syncGlobalBadges(null, true);
                StreamUtils.queueAddMessage(EnumChatFormatting.GRAY + "Synchronising channel badge cache...");
                emotes.syncAllChannelBadges(null, channelIds, true);
                StreamUtils.queueAddMessage(EnumChatFormatting.GRAY + "Synchronising global emote cache...");
                emotes.syncGlobalEmotes(null, true);
                StreamUtils.queueAddMessage(EnumChatFormatting.GRAY + "Synchronising channel emote cache...");
                emotes.syncAllChannelEmotes(null, channelIds, true);
            }
            twitch.getEventManager().onEvent(ChannelMessageEvent.class, this::onTwitchMessage);
            twitch.getEventManager().onEvent(FollowEvent.class, this::onTwitchFollow);
            twitch.getEventManager().onEvent(ChannelNoticeEvent.class, this::onTwitchNotice);
            twitch.getEventManager().onEvent(DeleteMessageEvent.class, this::onTwitchMessageDeleted);
            twitch.getEventManager().onEvent(ClearChatEvent.class, this::onTwitchChatClear);
            twitch.getEventManager().onEvent(UserTimeoutEvent.class, this::onUserTimedOut);
            twitch.getEventManager().onEvent(UserBanEvent.class, this::onUserBanned);
            TwitchChat chat = twitch.getChat();
            chat.connect();
            List<String> channels = Arrays.asList(config.twitchChannels.getStringList());
            for (String channel : chat.getChannels()) {
                if (!channels.contains(channel)) chat.leaveChannel(channel);
            }
            for (String channel : channels) {
                chat.joinChannel(channel);
            }
            if (config.followEventEnabled.getBoolean()) twitch.getClientHelper().enableFollowEventListener(channels);
            // Get username & scopes
            OAuth2Credential queriedCredential = twitchCredentialManager.getIdentityProviderByName("twitch").flatMap(provider -> provider instanceof TwitchIdentityProvider ? ((TwitchIdentityProvider) provider).getAdditionalCredentialInformation(credential) : Optional.empty()).orElse(null);
            if (queriedCredential != null) {
                twitchUsername = queriedCredential.getUserName();
                twitchScopes = queriedCredential.getScopes();
            }
            // Build the TwitchClient for sending messages (so they can be seen in-game)
            twitchSender = TwitchClientBuilder.builder()
                    .withDefaultAuthToken(credential)
                    .withEnableChat(true)
                    .withChatAccount(credential)
                    .build();
            TwitchChat senderChat = twitchSender.getChat();
            for (String channel : senderChat.getChannels())
                senderChat.leaveChannel(channel);
            return true;
        } catch (Exception e) {
            LOGGER.error("Failed to start Twitch client");
            e.printStackTrace();
            twitch = null;
            return false;
        }
    }

    private void onTwitchMessage(ChannelMessageEvent event) {
        Minecraft.getMinecraft().addScheduledTask(new TwitchMessageHandler(this, event));
    }

    private void onTwitchFollow(FollowEvent event) {
        boolean showChannel = config.forceShowChannelName.getBoolean() || (twitch != null && twitch.getChat().getChannels().size() > 1);
        StreamUtils.queueAddPrefixedMessage(config, "" + EnumChatFormatting.AQUA + EnumChatFormatting.BOLD + event.getUser().getName() + EnumChatFormatting.DARK_GREEN + " is now following " + EnumChatFormatting.AQUA + EnumChatFormatting.BOLD + event.getChannel().getName() + EnumChatFormatting.DARK_GREEN + "!", showChannel ? event.getChannel().getName() : null);
        if (this.config.playSoundOnFollow.getBoolean()) new Thread(new TwitchFollowSoundScheduler(this)).start();
    }

    private void onTwitchMessageDeleted(DeleteMessageEvent event) {
        StreamUtils.queueDeleteTwitchMessage(event.getMsgId());
    }

    private void onTwitchChatClear(ClearChatEvent event) {
        if (config.allowMessageDeletion.getBoolean()) StreamUtils.queueClearTwitchChat(event.getChannel().getId());
        boolean showChannel = config.forceShowChannelName.getBoolean() || (twitch != null && twitch.getChat().getChannels().size() > 1);
        StreamUtils.queueAddPrefixedMessage(config, "" + EnumChatFormatting.GRAY + "The chat has been cleared.", showChannel ? event.getChannel().getName() : null);
    }

    private void onUserTimedOut(UserTimeoutEvent event) {
        if (config.allowMessageDeletion.getBoolean())
            StreamUtils.queueClearTwitchUserMessages(event.getChannel().getId(), event.getUser().getId());
    }

    private void onUserBanned(UserBanEvent event) {
        if (config.allowMessageDeletion.getBoolean())
            StreamUtils.queueClearTwitchUserMessages(event.getChannel().getId(), event.getUser().getId());
    }

    private void onTwitchNotice(ChannelNoticeEvent event) {
        String message = event.getMessage();
        NoticeTag type = event.getType();
        boolean showChannel = config.forceShowChannelName.getBoolean() || (twitch != null && twitch.getChat().getChannels().size() > 1);
        if (type == null) return;
        switch (type) {
            // Chat mode updates
            case EMOTE_ONLY_OFF:
                StreamUtils.queueAddPrefixedMessage(config, EnumChatFormatting.GRAY + (message == null ? "Emote only mode has been disabled" : message), showChannel ? event.getChannel().getName() : null);
                break;
            case EMOTE_ONLY_ON:
                StreamUtils.queueAddPrefixedMessage(config, EnumChatFormatting.GRAY + (message == null ? "Emote only mode has been enabled" : message), showChannel ? event.getChannel().getName() : null);
                break;
            case FOLLOWERS_OFF:
                StreamUtils.queueAddPrefixedMessage(config, EnumChatFormatting.GRAY + (message == null ? "Followers only mode has been disabled" : message), showChannel ? event.getChannel().getName() : null);
                break;
            case FOLLOWERS_ON:
            case FOLLOWERS_ONZERO:
                StreamUtils.queueAddPrefixedMessage(config, EnumChatFormatting.GRAY + (message == null ? "Followers only mode has been enabled" : message), showChannel ? event.getChannel().getName() : null);
                break;
            case R9K_OFF:
                StreamUtils.queueAddPrefixedMessage(config, EnumChatFormatting.GRAY + (message == null ? "Unique only mode has been disabled" : message), showChannel ? event.getChannel().getName() : null);
                break;
            case R9K_ON:
                StreamUtils.queueAddPrefixedMessage(config, EnumChatFormatting.GRAY + (message == null ? "Unique only mode has been enabled" : message), showChannel ? event.getChannel().getName() : null);
                break;
            case SLOW_OFF:
                StreamUtils.queueAddPrefixedMessage(config, EnumChatFormatting.GRAY + (message == null ? "Slow mode has been disabled" : message), showChannel ? event.getChannel().getName() : null);
                break;
            case SLOW_ON:
                StreamUtils.queueAddPrefixedMessage(config, EnumChatFormatting.GRAY + (message == null ? "Slow mode has been enabled" : message), showChannel ? event.getChannel().getName() : null);
                break;
            case SUBS_OFF:
                StreamUtils.queueAddPrefixedMessage(config, EnumChatFormatting.GRAY + (message == null ? "Sub only mode has been disabled" : message), showChannel ? event.getChannel().getName() : null);
                break;
            case SUBS_ON:
                StreamUtils.queueAddPrefixedMessage(config, EnumChatFormatting.GRAY + (message == null ? "Sub only mode has been enabled" : message), showChannel ? event.getChannel().getName() : null);
                break;
            // /twitchchat
            case MSG_BANNED:
                StreamUtils.queueAddMessage(EnumChatFormatting.RED+"Message sending failed: "+(message == null ? "You are banned from this channel" : message));
                break;
            case MSG_BAD_CHARACTERS:
                StreamUtils.queueAddMessage(EnumChatFormatting.RED+"Message sending failed: "+(message == null ? "Your message contained too many weird characters" : message));
                break;
            case MSG_CHANNEL_BLOCKED:
                StreamUtils.queueAddMessage(EnumChatFormatting.RED+"Message sending failed: "+(message == null ? "You have been blocked from this channel" : message));
                break;
            case MSG_CHANNEL_SUSPENDED:
            case TOS_BAN:
                StreamUtils.queueAddMessage(EnumChatFormatting.RED+"Message sending failed: "+(message == null ? "This channel has been banned" : message));
                break;
            case MSG_DUPLICATE:
                StreamUtils.queueAddMessage(EnumChatFormatting.RED+"Message sending failed: "+(message == null ? "Your message was a duplicate of your previous message" : message));
                break;
            case MSG_EMOTEONLY:
                StreamUtils.queueAddMessage(EnumChatFormatting.RED+"Message sending failed: "+(message == null ? "This channel is currently in emote-only mode" : message));
                break;
            case MSG_FACEBOOK:
                StreamUtils.queueAddMessage(EnumChatFormatting.RED+"Message sending failed: "+(message == null ? "This channel requires that you give your personal information to Facebook" : message));
                break;
            case MSG_FOLLOWERSONLY:
            case MSG_FOLLOWERSONLY_FOLLOWED:
            case MSG_FOLLOWERSONLY_ZERO:
                StreamUtils.queueAddMessage(EnumChatFormatting.RED+"Message sending failed: "+(message == null ? "This channel is in follower only mode" : message));
                break;
            case MSG_R9K:
                StreamUtils.queueAddMessage(EnumChatFormatting.RED+"Message sending failed: "+(message == null ? "This channel is in unique mode and your message was not unique" : message));
                break;
            case MSG_RATELIMIT:
                StreamUtils.queueAddMessage(EnumChatFormatting.RED+"Message sending failed: "+(message == null ? "You are sending your messages too quickly!" : message));
                break;
            case MSG_REJECTED:
                StreamUtils.queueAddMessage(EnumChatFormatting.YELLOW+(message == null ? "Your message has been held by AutoMod and is being reviewed by channel mods." : message));
                break;
            case MSG_REJECTED_MANDATORY:
                StreamUtils.queueAddMessage(EnumChatFormatting.RED+"Message sending failed: "+(message == null ? "Your message has been blocked due this channel's moderation settings." : message));
                break;
            case MSG_SLOWMODE:
                StreamUtils.queueAddMessage(EnumChatFormatting.RED+"Message sending failed: "+(message == null ? "You are sending your messages too quickly; this channel has slow mode enabled" : message));
                break;
            case MSG_SUBSONLY:
                StreamUtils.queueAddMessage(EnumChatFormatting.RED+"Message sending failed: "+(message == null ? "This channel is in sub only mode" : message));
                break;
            case MSG_SUSPENDED:
                StreamUtils.queueAddMessage(EnumChatFormatting.RED+"Message sending failed: "+(message == null ? "Your account has been suspended" : message));
                break;
            case MSG_TIMEDOUT:
                StreamUtils.queueAddMessage(EnumChatFormatting.RED+"Message sending failed: "+(message == null ? "You are currently muted in this channel" : message));
                break;
            case MSG_VERIFIED_EMAIL:
                StreamUtils.queueAddMessage(EnumChatFormatting.RED+"Message sending failed: "+(message == null ? "This channel requires a verified email" : message));
                break;
            case MSG_BANNED_EMAIL_ALIAS:
                StreamUtils.queueAddMessage(EnumChatFormatting.RED+"Message sending failed: "+(message == null ? "Your email has been banned from this channel" : message));
                break;
            case MSG_REQUIRES_VERIFIED_PHONE_NUMBER:
                StreamUtils.queueAddMessage(EnumChatFormatting.RED+"Message sending failed: "+(message == null ? "This channel requires a verified phone number" : message));
                break;
            case MSG_ROOM_NOT_FOUND:
                StreamUtils.queueAddMessage(EnumChatFormatting.RED+"Message sending failed: "+(message == null ? "The selected channel was not found" : message));
                break;
            // General moderation
            case NO_PERMISSION:
                StreamUtils.queueAddMessage(EnumChatFormatting.RED+"Action failed: "+(message == null ? "You do not have the permission to do this!" : message));
                break;
            // /twitch delete
            case BAD_DELETE_MESSAGE_ERROR:
                StreamUtils.queueAddMessage(EnumChatFormatting.RED+"Message delete failed: "+(message == null ? "Invalid message" : message));
                break;
            case BAD_DELETE_MESSAGE_BROADCASTER:
                StreamUtils.queueAddMessage(EnumChatFormatting.RED+"Message delete failed: "+(message == null ? "You cannot delete broadcaster's messages!" : message));
                break;
            case BAD_DELETE_MESSAGE_MOD:
                StreamUtils.queueAddMessage(EnumChatFormatting.RED+"Message delete failed: "+(message == null ? "You cannot delete moderator's messages!" : message));
                break;
            case DELETE_MESSAGE_SUCCESS:
                StreamUtils.queueAddMessage(EnumChatFormatting.GREEN+(message == null ? "Message has been deleted successfully!" : message));
                break;
            // /twitch ban
            case BAD_BAN_ADMIN:
                StreamUtils.queueAddMessage(EnumChatFormatting.RED+"Ban failed: "+(message == null ? "You cannot ban a Twitch admin!" : message));
                break;
            case BAD_BAN_ANON:
                StreamUtils.queueAddMessage(EnumChatFormatting.RED+"Ban failed: "+(message == null ? "You cannot ban an anonymous user!" : message));
                break;
            case BAD_BAN_BROADCASTER:
                StreamUtils.queueAddMessage(EnumChatFormatting.RED+"Ban failed: "+(message == null ? "You cannot ban the broadcaster!" : message));
                break;
            case BAD_BAN_MOD:
                StreamUtils.queueAddMessage(EnumChatFormatting.RED+"Ban failed: "+(message == null ? "You cannot ban another channel moderator!" : message));
                break;
            case BAD_BAN_SELF:
                StreamUtils.queueAddMessage(EnumChatFormatting.RED+"Ban failed: "+(message == null ? "You cannot ban yourself!" : message));
                break;
            case BAD_BAN_STAFF:
                StreamUtils.queueAddMessage(EnumChatFormatting.RED+"Ban failed: "+(message == null ? "You cannot ban a Twitch staff member!" : message));
                break;
            case ALREADY_BANNED:
                StreamUtils.queueAddMessage(EnumChatFormatting.RED+"Ban failed: "+(message == null ? "User is already banned!" : message));
                break;
            case BAN_SUCCESS:
                StreamUtils.queueAddMessage(EnumChatFormatting.GREEN+(message == null ? "User has been banned successfully!" : message));
                break;
            // /twitch timeout
            case BAD_TIMEOUT_ADMIN:
                StreamUtils.queueAddMessage(EnumChatFormatting.RED+"Timeout failed: "+(message == null ? "You cannot timeout a Twitch admin!" : message));
                break;
            case BAD_TIMEOUT_ANON:
                StreamUtils.queueAddMessage(EnumChatFormatting.RED+"Timeout failed: "+(message == null ? "You cannot timeout an anonymous user!" : message));
                break;
            case BAD_TIMEOUT_BROADCASTER:
                StreamUtils.queueAddMessage(EnumChatFormatting.RED+"Timeout failed: "+(message == null ? "You cannot timeout the broadcaster!" : message));
                break;
            case BAD_TIMEOUT_DURATION:
                StreamUtils.queueAddMessage(EnumChatFormatting.RED+"Timeout failed: "+(message == null ? "You cannot timeout a user for this long!" : message));
                break;
            case BAD_TIMEOUT_MOD:
                StreamUtils.queueAddMessage(EnumChatFormatting.RED+"Timeout failed: "+(message == null ? "You cannot timeout another moderator!" : message));
                break;
            case BAD_TIMEOUT_SELF:
                StreamUtils.queueAddMessage(EnumChatFormatting.RED+"Timeout failed: "+(message == null ? "You cannot timeout yourself!" : message));
                break;
            case BAD_TIMEOUT_STAFF:
                StreamUtils.queueAddMessage(EnumChatFormatting.RED+"Timeout failed: "+(message == null ? "You cannot timeout a member of Twitch staff!" : message));
                break;
            case TIMEOUT_NO_TIMEOUT:
                StreamUtils.queueAddMessage(EnumChatFormatting.RED+"Timeout failed: "+(message == null ? "The user is not timed out" : message));
                break;
            case TIMEOUT_SUCCESS:
                StreamUtils.queueAddMessage(EnumChatFormatting.RED+(message == null ? "User has been timed out successfully!" : message));
                break;
            default:
                StreamUtils.queueAddMessage(EnumChatFormatting.YELLOW+"Unknown Twitch IRC Notice: Type: "+type+", message: "+message);
                IChatComponent cc = new ChatComponentText(EnumChatFormatting.GOLD+"Please report this to StreamChatMod's repository as an issue, including reproduction steps.");
                ChatStyle style = new ChatStyle();
                style.setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText(EnumChatFormatting.GREEN+"Click to open the issues page")));
                style.setChatClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://github.com/mini-bomba/StreamChatMod/issues"));
                cc.setChatStyle(style);
                StreamUtils.queueAddMessage(cc);
        }
    }

    public void printTwitchStatus() {
        printTwitchStatus(false);
    }

    public void printTwitchStatus(boolean includePrefix) {
        String prefix = includePrefix ? config.getFullTwitchPrefix() + " " : "";
        IChatComponent component = new ChatComponentText(prefix + EnumChatFormatting.GRAY + "Mod version: " + EnumChatFormatting.AQUA + EnumChatFormatting.BOLD + VERSION + (PRERELEASE ? EnumChatFormatting.GRAY + "@" + EnumChatFormatting.AQUA + GIT_HASH : "") + EnumChatFormatting.GRAY + " (" + (latestVersion == null || (PRERELEASE && latestCommit == null) ? EnumChatFormatting.RED + "Could not check latest version" : (latestVersion.equals(VERSION) && (!PRERELEASE || latestCommit.shortHash.equals(GIT_HASH)) ? EnumChatFormatting.GREEN + "Latest version" : EnumChatFormatting.GOLD + "Update available: " + latestVersion + (PRERELEASE ? "@" + latestCommit.shortHash : ""))) + EnumChatFormatting.GRAY + ")");
        IChatComponent commitMessage = null;
        if (latestVersion != null && !latestVersion.equals(VERSION) || (PRERELEASE && latestCommit != null && !latestCommit.shortHash.equals(GIT_HASH))) {
            IChatComponent changelog = new ChatComponentText(EnumChatFormatting.GRAY + " (" + EnumChatFormatting.YELLOW + "View Changes" + EnumChatFormatting.GRAY + ")");
            ChatStyle changelogStyle = new ChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://github.com/mini-bomba/StreamChatMod/compare/" + (PRERELEASE ? GIT_HASH : "v" + VERSION) + ".." + (PRERELEASE ? "latest" : "v" + latestVersion)));
            changelogStyle.setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText(EnumChatFormatting.GREEN + "Click here to view changes between your current & the latest version")));
            changelog.setChatStyle(changelogStyle);
            component.appendSibling(changelog);
            if (PRERELEASE && latestCommit != null)
                commitMessage = new ChatComponentText(prefix + EnumChatFormatting.GRAY + "Latest commit message: " + EnumChatFormatting.AQUA + latestCommit.shortMessage);
            ChatStyle style = new ChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://github.com/mini-bomba/StreamChatMod/releases"));
            style.setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText(EnumChatFormatting.GREEN + "Click here to see mod releases on GitHub!")));
            component.setChatStyle(style);
            if (commitMessage != null) commitMessage.setChatStyle(style);
        }
        StreamUtils.addMessage(component);
        if (commitMessage != null) StreamUtils.addMessage(commitMessage);
        if (config.twitchEnabled.getBoolean() && twitch != null) {
            String channel = config.twitchSelectedChannel.getString();
            StreamUtils.addMessages(new String[]{
                    prefix + EnumChatFormatting.GRAY + "Twitch Chat status: " + EnumChatFormatting.GREEN + "Enabled",
                    prefix + EnumChatFormatting.GRAY + "Logged in as: " + EnumChatFormatting.AQUA + EnumChatFormatting.BOLD + twitchUsername,
                    prefix + EnumChatFormatting.GRAY + "Channels joined: " + EnumChatFormatting.AQUA + EnumChatFormatting.BOLD + twitch.getChat().getChannels().size(),
                    prefix + EnumChatFormatting.GRAY + "Selected channel: " + (channel.length() > 0 ? "" + EnumChatFormatting.AQUA + EnumChatFormatting.BOLD + channel : EnumChatFormatting.RED + "None"),
                    prefix + EnumChatFormatting.GRAY + "Formatted messages: " + (config.allowFormatting.getBoolean() ? (config.subOnlyFormatting.getBoolean() ? EnumChatFormatting.GOLD + "Subscriber+ only" : EnumChatFormatting.GREEN + "Enabled") : EnumChatFormatting.RED + "Disabled"),
                    prefix + EnumChatFormatting.GRAY + "Minecraft chat mode: " + (config.twitchMessageRedirectEnabled.getBoolean() ? EnumChatFormatting.DARK_PURPLE + "Redirect to selected Twitch channel" : EnumChatFormatting.GREEN + "Send to Minecraft server") + EnumChatFormatting.GRAY + " (/twitch mode)"
            });
            // Warn about missing scopes
            checkScopes();
            if (config.twitchMessageRedirectEnabled.getBoolean()) {
                String minecraftPrefix = config.minecraftChatPrefix.getString();
                StreamUtils.addMessage(prefix + EnumChatFormatting.GRAY + "Minecraft chat prefix: " + (minecraftPrefix.length() == 0 ? EnumChatFormatting.RED + "Disabled!" : EnumChatFormatting.AQUA + minecraftPrefix));
            }
        } else {
            StreamUtils.addMessage(prefix + EnumChatFormatting.GRAY + "Twitch Chat status: " + EnumChatFormatting.RED + "Disabled" + (config.twitchEnabled.getBoolean() && config.twitchToken.getString().length() > 0 ? ", the token may be invalid!" : ""));
        }
    }

    public void checkScopes() {
        if (twitchScopes != null && !twitchScopes.containsAll(Arrays.asList("chat:read", "chat:edit", "channel:moderate", "channel:manage:broadcast", "user:edit:broadcast", "clips:edit")))
            StreamUtils.queueAddMessage(EnumChatFormatting.RED + "Warning: Your current token seems to be missing some required scopes. Please regenerate your token using " + EnumChatFormatting.GRAY + "/twitch token");
    }

    public void stopTwitch() {
        if (twitch != null) {
            TwitchClient twitchClient = this.twitch;
            this.twitch = null;
            TwitchChat chat = twitchClient.getChat();
            for (String channel : chat.getChannels()) {
                chat.leaveChannel(channel);
            }
            twitchClient.getClientHelper().disableFollowEventListener(Arrays.asList(config.twitchChannels.getStringList()));
            twitchClient.close();
        }
        if (twitchSender != null) {
            TwitchClient twitchClient = this.twitchSender;
            this.twitchSender = null;
            twitchClient.close();
        }
    }

    public User getSelectedChannelUser() {
        return getTwitchUserByName(config.twitchSelectedChannel.getString());
    }

    public User getTwitchUserById(String userId) {
        return userCache.getOptional(userId).orElseGet(() -> {
            if (twitch == null) {
                LOGGER.error("Twitch client was disabled during an user lookup!");
                return Optional.empty();
            }
            List<User> users = twitch.getHelix().getUsers(null, Collections.singletonList(userId), null).execute().getUsers();
            Optional<User> result = users.size() == 0 ? Optional.empty() : Optional.of(users.get(0));
            userCache.put(userId, result.orElse(null));
            result.ifPresent(user -> userCacheByNames.put(user.getLogin(), user));
            return result;
        }).orElse(null);
    }

    public User getTwitchUserByName(String userName) {
        return userCacheByNames.getOptional(userName.toLowerCase()).orElseGet(() -> {
            if (twitch == null) {
                LOGGER.error("Twitch client was disabled during an user lookup!");
                return Optional.empty();
            }
            List<User> users = twitch.getHelix().getUsers(null, null, Collections.singletonList(userName)).execute().getUsers();
            Optional<User> result = users.size() == 0 ? Optional.empty() : Optional.of(users.get(0));
            userCacheByNames.put(userName, result.orElse(null));
            result.ifPresent(user -> userCache.put(user.getId(), user));
            return result;
        }).orElse(null);
    }

    public Clip getTwitchClip(String clipId) {
        return clipCache.getOptional(clipId).orElseGet(() -> {
            if (twitch == null) {
                LOGGER.error("Twitch client was disabled during a clip lookup!");
                return Optional.empty();
            }
            List<Clip> clips = twitch.getHelix().getClips(null, null, null, clipId, null, null, 1, null, null).execute().getData();
            Optional<Clip> result = clips.size() == 0 ? Optional.empty() : Optional.of(clips.get(0));
            clipCache.put(clipId, result.orElse(null));
            return result;
        }).orElse(null);
    }

    public Game getTwitchCategory(String categoryId) {
        return categoryCache.getOptional(categoryId).orElseGet(() -> {
            if (twitch == null) {
                LOGGER.error("Twitch client was disabled during a category lookup!");
                return Optional.empty();
            }
            List<Game> categories = twitch.getHelix().getGames(null, Collections.singletonList(categoryId), null).execute().getGames();
            Optional<Game> result = categories.size() == 0 ? Optional.empty() : Optional.of(categories.get(0));
            categoryCache.put(categoryId, result.orElse(null));
            return result;
        }).orElse(null);
    }

    public Chatters getChatters(String channel) {
        String channelName = channel.toLowerCase();
        Timestamped<Chatters> cached = timestampedChatterCache.contains(channelName) ? timestampedChatterCache.get(channelName) : null;
        if (cached == null || cached.getTimestampMillis() + 60 * 1000 > System.currentTimeMillis()) {
            if (twitch == null) {
                LOGGER.error("Twitch client was disabled during a chatter list lookup!");
                return null;
            }
            Supplier<Chatters> lookup = () -> {
                Chatters chatters;
                try {
                    chatters = twitch.getMessagingInterface().getChatters(channelName).execute();
                } catch (Exception e) {
                    LOGGER.error("Failed to lookup chatters in channel " + channelName);
                    e.printStackTrace();
                    return null;
                }
                timestampedChatterCache.put(channelName, new Timestamped<>(System.currentTimeMillis(), chatters));
                return chatters;
            };
            // If no value cached - send request synchronously.
            // If a value is cached - return old value and start an async request, if there's none running right now.
            if (cached == null) return lookup.get();
            if (!isImportantActionScheduled()) asyncTwitchAction(lookup::get);
            return cached.getValue();
        }
        return cached.getValue();
    }
}

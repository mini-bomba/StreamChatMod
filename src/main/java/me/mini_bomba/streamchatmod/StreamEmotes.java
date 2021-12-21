package me.mini_bomba.streamchatmod;

import com.github.twitch4j.helix.domain.Emote;
import me.mini_bomba.streamchatmod.utils.*;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.ProgressManager;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

@SuppressWarnings({"ResultOfMethodCallIgnored", "ConstantConditions"})
public class StreamEmotes {
    private final StreamChatMod mod;
    private static final Logger LOGGER = LogManager.getLogger();
    private final Map<String, StreamEmote> namesToGlobalEmotes = new HashMap<>();
    private final List<TwitchEmote> twitchGlobalEmotes = new ArrayList<>();
    private final List<BTTVStreamEmote> bttvGlobalEmotes = new ArrayList<>();
    private final List<FFZStreamEmote> ffzGlobalEmotes = new ArrayList<>();
    private final Map<String, TwitchEmote> twitchEmotes = new HashMap<>();
    private final Map<String, BTTVStreamEmote> bttvEmotes = new HashMap<>();
    private final Map<String, FFZStreamEmote> ffzEmotes = new HashMap<>();
    private final Map<String, Map<String, StreamEmote>> channelEmotes = new HashMap<>();

    public StreamEmotes(StreamChatMod mod) {
        this.mod = mod;
    }

    public List<TwitchEmote> getTwitchGlobalEmotes() {
        return new ArrayList<>(twitchGlobalEmotes);
    }

    public List<BTTVStreamEmote> getBttvGlobalEmotes() {
        return new ArrayList<>(bttvGlobalEmotes);
    }

    public List<FFZStreamEmote> getFfzGlobalEmotes() {
        return new ArrayList<>(ffzGlobalEmotes);
    }

    public Map<String, StreamEmote> getChannelEmotes(String channelId) {
        if (!channelEmotes.containsKey(channelId)) return Collections.emptyMap();
        return new HashMap<>(channelEmotes.get(channelId));
    }

    public boolean isGlobalEmote(String name) {
        return namesToGlobalEmotes.containsKey(name);
    }

    public boolean isChannelEmote(String channelId, String name) {
        return channelEmotes.containsKey(channelId) && channelEmotes.get(channelId).containsKey(name);
    }

    public boolean isEmote(String channelId, String name) {
        return isGlobalEmote(name) || isChannelEmote(channelId, name);
    }

    public StreamEmote getGlobalEmote(String name) {
        if (isGlobalEmote(name)) return namesToGlobalEmotes.get(name);
        return null;
    }

    public StreamEmote getChannelEmote(String channelId, String name) {
        if (isChannelEmote(channelId, name)) return channelEmotes.get(channelId).get(name);
        return null;
    }

    public StreamEmote getEmote(String channelId, String name) {
        if (isChannelEmote(channelId, name)) return getChannelEmote(channelId, name);
        if (isGlobalEmote(name)) return getGlobalEmote(name);
        return null;
    }

    public void syncGlobalEmotes(ProgressManager.ProgressBar progress, boolean indexInMainThread) {
        // Twitch
        if (progress != null) progress.step("Twitch global emotes");
        File twitchGlobalsDir = new File("streamchatmod/emotes/twitch_global");
        twitchGlobalsDir.mkdirs();
        List<String> cachedTwitchGlobals = Arrays.stream(twitchGlobalsDir.list())
                .map(name -> (name.endsWith("_3x.png") || name.endsWith("_3x.gif") ? name.substring(0, name.length() - 7) : name))
                .collect(Collectors.toList());
        List<Emote> twitchGlobals = mod.queryGlobalTwitchEmotes();
        List<Emote> twitchEmotesToDownload = twitchGlobals.stream()
                .filter(emote -> !cachedTwitchGlobals.contains(emote.getId()))
                .collect(Collectors.toList());
        threadedDownload(progress != null, twitchEmotesToDownload.stream().map((Function<Emote, Function<ProgressManager.ProgressBar, Callable<Void>>>) emote -> downloadProgress -> () -> {
            try {
                FileUtils.copyURLToFile(
                        new URL(emote.getImages().getLargeImageUrl()),
                        new File("streamchatmod/emotes/twitch_global/" + emote.getId() + "_3x.png")
                );
            } catch (Exception e) {
                LOGGER.warn("Failed to download Twitch global emote " + emote.getName());
                e.printStackTrace();
            }
            if (downloadProgress != null) synchronized (downloadProgress) {
                downloadProgress.step("Downloaded " + emote.getName());
            }
            return null;
        }).collect(Collectors.toList()));

        // BTTV
        if (progress != null) progress.step("BetterTTV global emotes");
        File bttvGlobalsDir = new File("streamchatmod/emotes/bttv_global");
        bttvGlobalsDir.mkdirs();
        List<String> cachedBTTVGlobals = Arrays.stream(bttvGlobalsDir.list())
                .map(name -> (name.endsWith("_2x.png") || name.endsWith("_2x.gif") ? name.substring(0, name.length() - 7) : name))
                .collect(Collectors.toList());
        List<BTTVEmote> bttvGlobals = BTTVApi.getGlobalEmotes();
        List<BTTVEmote> bttvEmotesToDownload = bttvGlobals.stream()
                .filter(emote -> !cachedBTTVGlobals.contains(emote.id))
                .collect(Collectors.toList());
        threadedDownload(progress != null, bttvEmotesToDownload.stream().map((Function<BTTVEmote, Function<ProgressManager.ProgressBar, Callable<Void>>>) emote -> downloadProgress -> () -> {
            try {
                FileUtils.copyURLToFile(
                        new URL(emote.getMediumEmoteURL()),
                        new File("streamchatmod/emotes/bttv_global/" + emote.id + "_2x." + emote.imageType.name().toLowerCase())
                );
            } catch (Exception e) {
                LOGGER.warn("Failed to download BTTV global emote " + emote.name);
                e.printStackTrace();
            }
            if (downloadProgress != null) synchronized (downloadProgress) {
                downloadProgress.step("Downloaded " + emote.name);
            }
            return null;
        }).collect(Collectors.toList()));

        // FFZ
        if (progress != null) progress.step("FrankerFaceZ global emotes");
        File ffzGlobalsDir = new File("streamchatmod/emotes/ffz_global");
        ffzGlobalsDir.mkdirs();
        List<String> cachedFFZGlobals = Arrays.stream(ffzGlobalsDir.list())
                .map(name -> (name.endsWith("_2x.png") || name.endsWith("_2x.gif") ? name.substring(0, name.length() - 7) : name))
                .collect(Collectors.toList());
        List<FFZEmote> ffzGlobals = FFZApi.getGlobalEmotes();
        List<FFZEmote> ffzEmotesToDownload = ffzGlobals.stream()
                .filter(emote -> !cachedFFZGlobals.contains(String.valueOf(emote.id)))
                .collect(Collectors.toList());
        threadedDownload(progress != null, ffzEmotesToDownload.stream().map((Function<FFZEmote, Function<ProgressManager.ProgressBar, Callable<Void>>>) emote -> downloadProgress -> () -> {
            try {
                FileUtils.copyURLToFile(
                        new URL(emote.getMediumEmoteURL()),
                        new File("streamchatmod/emotes/ffz_global/" + emote.id + "_2x.png")
                );
            } catch (Exception e) {
                LOGGER.warn("Failed to download FFZ global emote " + emote.name);
                e.printStackTrace();
            }
            if (downloadProgress != null) synchronized (downloadProgress) {
                downloadProgress.step("Downloaded " + emote.name);
            }
            return null;
        }).collect(Collectors.toList()));

        // Indexing
        if (progress != null) progress.step("Indexing global emotes");
        Callable<Void> doIndex = () -> {
            java.util.stream.Stream<StreamEmote> stream1 = twitchGlobals.stream().map(emote -> {
                if (twitchEmotes.containsKey(emote.getId())) return twitchEmotes.get(emote.getId());
                try {
                    TwitchEmote wrappedEmote = new TwitchEmote(emote);
                    twitchEmotes.put(emote.getId(), wrappedEmote);
                    return wrappedEmote;
                } catch (IOException e) {
                    LOGGER.warn("Failed to wrap global twitch emote " + emote.getName() + " in TwitchEmote class");
                    e.printStackTrace();
                    return null;
                }
            });
            java.util.stream.Stream<StreamEmote> stream2 = bttvGlobals.stream().map(emote -> {
                if (bttvEmotes.containsKey(emote.id)) return bttvEmotes.get(emote.id);
                try {
                    BTTVStreamEmote wrappedEmote = new BTTVStreamEmote(emote, true);
                    bttvEmotes.put(emote.id, wrappedEmote);
                    return wrappedEmote;
                } catch (IOException e) {
                    LOGGER.warn("Failed to wrap global BTTV emote " + emote.name + " in BTTVStreamEmote class");
                    e.printStackTrace();
                    return null;
                }
            });
            java.util.stream.Stream<StreamEmote> stream3 = ffzGlobals.stream().map(emote -> {
                String id = String.valueOf(emote.id);
                if (ffzEmotes.containsKey(id)) return ffzEmotes.get(id);
                try {
                    FFZStreamEmote wrappedEmote = new FFZStreamEmote(emote, true);
                    ffzEmotes.put(id, wrappedEmote);
                    return wrappedEmote;
                } catch (IOException e) {
                    LOGGER.warn("Failed to wrap global FFZ emote " + emote.name + " in FFZStreamEmote class");
                    e.printStackTrace();
                    return null;
                }
            });
            twitchGlobalEmotes.clear();
            bttvGlobalEmotes.clear();
            ffzGlobalEmotes.clear();
            namesToGlobalEmotes.clear();
            java.util.stream.Stream.of(stream1, stream2, stream3).flatMap(s -> s).forEach(emote -> {
                if (emote == null) return;
                if (emote instanceof TwitchEmote) twitchGlobalEmotes.add((TwitchEmote) emote);
                else if (emote instanceof BTTVStreamEmote) bttvGlobalEmotes.add((BTTVStreamEmote) emote);
                else if (emote instanceof FFZStreamEmote) ffzGlobalEmotes.add((FFZStreamEmote) emote);
                if (!namesToGlobalEmotes.containsKey(emote.name)) namesToGlobalEmotes.put(emote.name, emote);
                else LOGGER.warn("Duplicate emote name: " + emote.name);
            });
            return null;
        };
        try {
            if (indexInMainThread) Minecraft.getMinecraft().addScheduledTask(doIndex).get();
            else doIndex.call();
        } catch (InterruptedException ignored) {
        } catch (Exception e) {
            LOGGER.error("Got error while indexing global emotes");
            e.printStackTrace();
        }
    }


    public void syncAllChannelEmotes(ProgressManager.ProgressBar progress, List<String> channelIds, boolean indexInMainThread) {
        // BTTV
        if (progress != null) progress.step("BetterTTV channel emotes");
        File bttvChannelDir = new File("streamchatmod/emotes/bttv_channel");
        bttvChannelDir.mkdirs();
        List<String> cachedBTTVChannelEmotes = Arrays.stream(bttvChannelDir.list())
                .map(name -> (name.endsWith("_2x.png") || name.endsWith("_2x.gif") ? name.substring(0, name.length() - 7) : name))
                .collect(Collectors.toList());
        Map<String, List<BTTVEmote>> bttvChannels = new HashMap<>();
        for (String channelId : channelIds) bttvChannels.put(channelId, BTTVApi.getChannelEmotes(channelId));
        List<BTTVEmote> bttvChannelEmotes = bttvChannels.values().stream().flatMap(List::stream).distinct().collect(Collectors.toList());
        List<BTTVEmote> bttvChannelEmotesToDownload = bttvChannelEmotes.stream()
                .filter(emote -> !cachedBTTVChannelEmotes.contains(emote.id))
                .collect(Collectors.toList());
        threadedDownload(progress != null, bttvChannelEmotesToDownload.stream().map((Function<BTTVEmote, Function<ProgressManager.ProgressBar, Callable<Void>>>) emote -> downloadProgress -> () -> {
            try {
                FileUtils.copyURLToFile(
                        new URL(emote.getMediumEmoteURL()),
                        new File("streamchatmod/emotes/bttv_channel/" + emote.id + "_2x." + emote.imageType.name().toLowerCase())
                );
            } catch (Exception e) {
                LOGGER.warn("Failed to download BTTV channel emote " + emote.name + " (id " + emote.id + ")");
                e.printStackTrace();
            }
            if (downloadProgress != null) synchronized (downloadProgress) {
                downloadProgress.step("Downloaded " + emote.name);
            }
            return null;
        }).collect(Collectors.toList()));

        // FFZ
        if (progress != null) progress.step("FrankerFaceZ channel emotes");
        File ffzChannelDir = new File("streamchatmod/emotes/ffz_channel");
        ffzChannelDir.mkdirs();
        List<String> cachedFFZChannelEmotes = Arrays.stream(ffzChannelDir.list())
                .map(name -> (name.endsWith("_2x.png") || name.endsWith("_2x.gif") ? name.substring(0, name.length() - 7) : name))
                .collect(Collectors.toList());
        Map<String, List<FFZEmote>> ffzChannels = FFZApi.getMultiChannelEmotes(channelIds);
        List<FFZEmote> ffzChannelEmotes = ffzChannels.values().stream().flatMap(List::stream).distinct().collect(Collectors.toList());
        List<FFZEmote> ffzChannelEmotesToDownload = ffzChannelEmotes.stream()
                .filter(emote -> !cachedFFZChannelEmotes.contains(String.valueOf(emote.id)))
                .collect(Collectors.toList());
        threadedDownload(progress != null, ffzChannelEmotesToDownload.stream().map((Function<FFZEmote, Function<ProgressManager.ProgressBar, Callable<Void>>>) emote -> downloadProgress -> () -> {
            try {
                FileUtils.copyURLToFile(
                        new URL(emote.getMediumEmoteURL()),
                        new File("streamchatmod/emotes/ffz_channel/" + emote.id + "_2x.png")
                );
            } catch (Exception e) {
                LOGGER.warn("Failed to download FFZ channel emote " + emote.name + " (id " + emote.id + ")");
                e.printStackTrace();
            }
            if (downloadProgress != null) synchronized (downloadProgress) {
                downloadProgress.step("Downloaded " + emote.name);
            }
            return null;
        }).collect(Collectors.toList()));

        // Indexing
        Callable<Void> doIndex = () -> {
            if (progress != null) progress.step("Indexing channel emotes");
            for (BTTVEmote channelEmote : bttvChannelEmotes) {
                if (channelEmote == null || bttvEmotes.containsKey(channelEmote.id)) continue;
                try {
                    BTTVStreamEmote wrappedEmote = new BTTVStreamEmote(channelEmote, false);
                    bttvEmotes.put(channelEmote.id, wrappedEmote);
                } catch (IOException e) {
                    LOGGER.warn("Failed to wrap channel BTTV emote " + channelEmote.name + " in BTTVStreamEmote class");
                    e.printStackTrace();
                }
            }
            for (FFZEmote channelEmote : ffzChannelEmotes) {
                if (channelEmote == null || ffzEmotes.containsKey(String.valueOf(channelEmote.id))) continue;
                try {
                    FFZStreamEmote wrappedEmote = new FFZStreamEmote(channelEmote, false);
                    ffzEmotes.put(String.valueOf(channelEmote.id), wrappedEmote);
                } catch (IOException e) {
                    LOGGER.warn("Failed to wrap channel FFZ emote " + channelEmote.name + " in FFZStreamEmote class");
                    e.printStackTrace();
                }
            }
            channelEmotes.clear();
            for (String channelId : channelIds) {
                Map<String, StreamEmote> wrappedChannelEmotes = new HashMap<>();
                for (BTTVEmote emote : bttvChannels.get(channelId))
                    if (wrappedChannelEmotes.containsKey(emote.name))
                        LOGGER.warn("Duplicate emote name for channel " + channelId + ": " + emote.name);
                    else if (!bttvEmotes.containsKey(emote.id)) LOGGER.warn("Missing BTTV emote with id " + emote.id);
                    else wrappedChannelEmotes.put(emote.name, bttvEmotes.get(emote.id));
                for (FFZEmote emote : ffzChannels.get(channelId))
                    if (wrappedChannelEmotes.containsKey(emote.name))
                        LOGGER.warn("Duplicate emote name for channel " + channelId + ": " + emote.name);
                    else if (!ffzEmotes.containsKey(String.valueOf(emote.id)))
                        LOGGER.warn("Missing FFZ emote with id " + emote.id);
                    else wrappedChannelEmotes.put(emote.name, ffzEmotes.get(String.valueOf(emote.id)));
                channelEmotes.put(channelId, wrappedChannelEmotes);
            }
            return null;
        };
        try {
            if (indexInMainThread) Minecraft.getMinecraft().addScheduledTask(doIndex).get();
            else doIndex.call();
        } catch (InterruptedException ignored) {
        } catch (Exception e) {
            LOGGER.error("Got error while indexing channel emotes");
            e.printStackTrace();
        }
    }

    public void syncChannelEmotes(String channelId, boolean indexInMainThread) {
        // BTTV
        File bttvChannelDir = new File("streamchatmod/emotes/bttv_channel");
        bttvChannelDir.mkdirs();
        List<String> cachedBTTVChannelEmotes = Arrays.stream(bttvChannelDir.list())
                .map(name -> (name.endsWith("_2x.png") || name.endsWith("_2x.gif") ? name.substring(0, name.length() - 7) : name))
                .collect(Collectors.toList());
        List<BTTVEmote> bttvChannelEmotes = BTTVApi.getChannelEmotes(channelId);
        List<BTTVEmote> bttvChannelEmotesToDownload = bttvChannelEmotes.stream()
                .filter(emote -> !cachedBTTVChannelEmotes.contains(emote.id))
                .collect(Collectors.toList());
        threadedDownload(bttvChannelEmotesToDownload.stream().map((Function<BTTVEmote, Callable<Void>>) emote -> () -> {
            try {
                FileUtils.copyURLToFile(
                        new URL(emote.getMediumEmoteURL()),
                        new File("streamchatmod/emotes/bttv_channel/" + emote.id + "_2x." + emote.imageType.name().toLowerCase())
                );
            } catch (Exception e) {
                LOGGER.warn("Failed to download BTTV channel emote " + emote.name + " (id " + emote.id + ")");
                e.printStackTrace();
            }
            return null;
        }).collect(Collectors.toList()));

        // FFZ
        File ffzChannelDir = new File("streamchatmod/emotes/ffz_channel");
        ffzChannelDir.mkdirs();
        List<String> cachedFFZChannelEmotes = Arrays.stream(ffzChannelDir.list())
                .map(name -> (name.endsWith("_2x.png") || name.endsWith("_2x.gif") ? name.substring(0, name.length() - 7) : name))
                .collect(Collectors.toList());
        List<FFZEmote> ffzChannelEmotes = FFZApi.getChannelEmotes(channelId);
        List<FFZEmote> ffzChannelEmotesToDownload = ffzChannelEmotes.stream()
                .filter(emote -> !cachedFFZChannelEmotes.contains(String.valueOf(emote.id)))
                .collect(Collectors.toList());
        threadedDownload(ffzChannelEmotesToDownload.stream().map((Function<FFZEmote, Callable<Void>>) emote -> () -> {
            try {
                FileUtils.copyURLToFile(
                        new URL(emote.getMediumEmoteURL()),
                        new File("streamchatmod/emotes/ffz_channel/" + emote.id + "_2x.png")
                );
            } catch (Exception e) {
                LOGGER.warn("Failed to download FFZ channel emote " + emote.name + " (id " + emote.id + ")");
                e.printStackTrace();
            }
            return null;
        }).collect(Collectors.toList()));

        // Indexing
        Callable<Void> doIndex = () -> {
            for (BTTVEmote channelEmote : bttvChannelEmotes) {
                if (channelEmote == null || bttvEmotes.containsKey(channelEmote.id)) continue;
                try {
                    BTTVStreamEmote wrappedEmote = new BTTVStreamEmote(channelEmote, false);
                    bttvEmotes.put(channelEmote.id, wrappedEmote);
                } catch (IOException e) {
                    LOGGER.warn("Failed to wrap channel BTTV emote " + channelEmote.name + " in BTTVStreamEmote class");
                    e.printStackTrace();
                }
            }
            for (FFZEmote channelEmote : ffzChannelEmotes) {
                if (channelEmote == null || ffzEmotes.containsKey(String.valueOf(channelEmote.id))) continue;
                try {
                    FFZStreamEmote wrappedEmote = new FFZStreamEmote(channelEmote, false);
                    ffzEmotes.put(String.valueOf(channelEmote.id), wrappedEmote);
                } catch (IOException e) {
                    LOGGER.warn("Failed to wrap channel FFZ emote " + channelEmote.name + " in FFZStreamEmote class");
                    e.printStackTrace();
                }
            }
            Map<String, StreamEmote> wrappedChannelEmotes = new HashMap<>();
            for (BTTVEmote emote : bttvChannelEmotes)
                if (wrappedChannelEmotes.containsKey(emote.name))
                    LOGGER.warn("Duplicate emote name for channel " + channelId + ": " + emote.name);
                else if (!bttvEmotes.containsKey(emote.id)) LOGGER.warn("Missing BTTV emote with id " + emote.id);
                else wrappedChannelEmotes.put(emote.name, bttvEmotes.get(emote.id));
            for (FFZEmote emote : ffzChannelEmotes)
                if (wrappedChannelEmotes.containsKey(emote.name))
                    LOGGER.warn("Duplicate emote name for channel " + channelId + ": " + emote.name);
                else if (!ffzEmotes.containsKey(String.valueOf(emote.id)))
                    LOGGER.warn("Missing FFZ emote with id " + emote.id);
                else wrappedChannelEmotes.put(emote.name, ffzEmotes.get(String.valueOf(emote.id)));
            channelEmotes.put(channelId, wrappedChannelEmotes);
            return null;
        };
        try {
            if (indexInMainThread) Minecraft.getMinecraft().addScheduledTask(doIndex).get();
            else doIndex.call();
        } catch (InterruptedException ignored) {
        } catch (Exception e) {
            LOGGER.error("Got error while indexing " + channelId + "'s emotes");
            e.printStackTrace();
        }
    }

    private static void threadedDownload(boolean showProgress, List<Function<ProgressManager.ProgressBar, Callable<Void>>> downloads) {
        if (downloads.size() == 0) return;
        ProgressManager.ProgressBar downloadProgress = showProgress ? ProgressManager.push("Downloading emotes", downloads.size()) : null;
        threadedDownload(downloads.stream().map(d -> d.apply(downloadProgress)).collect(Collectors.toList()));
        if (downloadProgress != null) synchronized (downloadProgress) {
            ProgressManager.pop(downloadProgress);
        }
    }

    private static void threadedDownload(List<Callable<Void>> downloads) {
        if (downloads.size() == 0) return;
        ExecutorService downloadExecutor = Executors.newFixedThreadPool(Math.min(Runtime.getRuntime().availableProcessors(), downloads.size()));
        try {
            downloadExecutor.invokeAll(downloads);
        } catch (InterruptedException ignored) {
        }
        downloadExecutor.shutdown();
        try {
            //noinspection ResultOfMethodCallIgnored
            downloadExecutor.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException ignored) {
        }
    }
}

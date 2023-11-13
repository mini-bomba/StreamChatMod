package me.mini_bomba.streamchatmod.utils;

import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BTTVApi {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Map<String, EmoteOwner> bttvUsers = new HashMap<>();

    public static List<BTTVEmote> getGlobalEmotes() {
        Gson gson = new Gson();
        try {
            LocalEmote[] emotes = gson.fromJson(new InputStreamReader(new URL("https://api.betterttv.net/3/cached/emotes/global").openStream()), LocalEmote[].class);
            return Arrays.stream(emotes).map(emote -> new BTTVEmote(emote.id, emote.code, getImageType(emote.imageType), emote.userId)).collect(Collectors.toList());
        } catch (Exception e) {
            LOGGER.error("Failed to fetch BTTV global emotes:");
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public static List<BTTVEmote> getChannelEmotes(String twitchId) {
        Gson gson = new Gson();
        try {
            CachedTwitchUser twitchUser = gson.fromJson(new InputStreamReader(new URL("https://api.betterttv.net/3/cached/users/twitch/" + twitchId).openStream()), CachedTwitchUser.class);
            return Stream.concat(
                    twitchUser.channelEmotes.stream().map(emote -> new BTTVEmote(emote.id, emote.code, getImageType(emote.imageType), emote.userId)),
                    twitchUser.sharedEmotes.stream().map(emote -> {
                        bttvUsers.put(emote.user.id, emote.user);
                        return new BTTVEmote(emote.id, emote.code, getImageType(emote.imageType), emote.user.id);
                    })
            ).collect(Collectors.toList());
        } catch (Exception e) {
            LOGGER.error("Failed to fetch BTTV channel emotes:");
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public static EmoteOwner getUser(String userId) {
        if (bttvUsers.containsKey(userId))
            return bttvUsers.get(userId);
        BTTVUser bttvUser = getBTTVUser(userId);
        if (bttvUser == null) return null;
        EmoteOwner user = new EmoteOwner(bttvUser.id, bttvUser.name, bttvUser.displayName, bttvUser.providerId);
        bttvUsers.put(userId, user);
        return user;
    }

    private static BTTVEmote.ImageType getImageType(String type) {
        return BTTVEmote.ImageType.valueOf(type.toUpperCase());
    }

    private static BTTVUser getBTTVUser(String userId) {
        Gson gson = new Gson();
        try {
            return gson.fromJson(new InputStreamReader(new URL("https://api.betterttv.net/3/users/" + userId).openStream()), BTTVUser.class);
        } catch (Exception e) {
            LOGGER.error("Failed to fetch BTTV user");
            e.printStackTrace();
            return null;
        }
    }

    private static class LocalEmote {
        public final String id;
        public final String code;
        public final String imageType;
        public final String userId;

        private LocalEmote(String id, String code, String imageType, String userId) {
            this.id = id;
            this.code = code;
            this.imageType = imageType;
            this.userId = userId;
        }
    }

    private static class SharedEmote {
        public final String id;
        public final String code;
        public final String imageType;
        public final EmoteOwner user;

        private SharedEmote(String id, String code, String imageType, EmoteOwner user) {
            this.id = id;
            this.code = code;
            this.imageType = imageType;
            this.user = user;
        }
    }

    public static class EmoteOwner {
        public final String id;
        public final String name;
        public final String displayName;
        public final String providerId;

        private EmoteOwner(String id, String name, String displayName, String providerId) {
            this.id = id;
            this.name = name;
            this.displayName = displayName;
            this.providerId = providerId;
        }
    }

    private static class CachedTwitchUser {
        public final String id;
        public final List<String> bots;
        public final String avatar;
        public final List<LocalEmote> channelEmotes;
        public final List<SharedEmote> sharedEmotes;

        private CachedTwitchUser(String id, List<String> bots, String avatar, List<LocalEmote> channelEmotes, List<SharedEmote> sharedEmotes) {
            this.id = id;
            this.bots = bots;
            this.avatar = avatar;
            this.channelEmotes = channelEmotes;
            this.sharedEmotes = sharedEmotes;
        }
    }

    private static class BTTVUser {
        public final String id;
        public final String name;
        public final String displayName;
        public final String providerId;
        public final List<String> bots;
        public final List<?> channelEmotes;
        public final List<?> sharedEmotes;

        private BTTVUser(String id, String name, String displayName, String providerId, List<String> bots, List<?> channelEmotes, List<?> sharedEmotes) {
            this.id = id;
            this.name = name;
            this.displayName = displayName;
            this.providerId = providerId;
            this.bots = bots;
            this.channelEmotes = channelEmotes;
            this.sharedEmotes = sharedEmotes;
        }
    }
}

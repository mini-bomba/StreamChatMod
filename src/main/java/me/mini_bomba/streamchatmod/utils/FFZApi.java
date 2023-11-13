package me.mini_bomba.streamchatmod.utils;

import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStreamReader;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FFZApi {
    private static final Logger LOGGER = LogManager.getLogger();

    public static List<FFZEmote> getGlobalEmotes() {
        Gson gson = new Gson();
        try {
            FFZDefaultSets sets = gson.fromJson(new InputStreamReader(new URL("https://api.frankerfacez.com/v1/set/global").openStream()), FFZDefaultSets.class);
            return sets.sets.get("3").emoticons;
        } catch (Exception e) {
            LOGGER.error("Failed to fetch FFZ global emotes:");
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public static List<FFZEmote> getChannelEmotes(String channelId) {
        Gson gson = new Gson();
        try {
            FFZRoomResponse resp = gson.fromJson(new InputStreamReader(new URL("https://api.frankerfacez.com/v1/room/id/" + channelId).openStream()), FFZRoomResponse.class);
            if (resp.room == null || !resp.sets.containsKey(String.valueOf(resp.room.set)))
                return Collections.emptyList();
            return resp.sets.get(String.valueOf(resp.room.set)).emoticons;
        } catch (Exception e) {
            LOGGER.error("Failed to fetch FFZ channel emotes for id " + channelId + ":");
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public static Map<String, List<FFZEmote>> getMultiChannelEmotes(List<String> channelIds) {
        Gson gson = new Gson();
        try {
            FFZMultiRoomResponse resp = gson.fromJson(new InputStreamReader(new URL("https://api.frankerfacez.com/v1/multi_room/id/" + String.join(",", channelIds)).openStream()), FFZMultiRoomResponse.class);
            return resp.rooms.stream().collect(Collectors.toMap(room -> String.valueOf(room.twitch_id), room -> resp.sets.containsKey(String.valueOf(room.set)) ? resp.sets.get(String.valueOf(room.set)).emoticons : Collections.emptyList()));
        } catch (Exception e) {
            LOGGER.error("Failed to fetch FFZ channel emotes for ids " + String.join(",", channelIds) + ":");
            e.printStackTrace();
            return channelIds.stream().collect(Collectors.toMap(i -> i, i -> Collections.emptyList()));
        }
    }

    private static class FFZEmoteSet {
        public final int id;
        public final int _type;
        public final String icon;
        public final String title;
        public final String description;
        public final String css;
        public final List<FFZEmote> emoticons;

        private FFZEmoteSet(int id, int _type, String icon, String title, String description, String css, List<FFZEmote> emoticons) {
            this.id = id;
            this._type = _type;
            this.icon = icon;
            this.title = title;
            this.description = description;
            this.css = css;
            this.emoticons = emoticons;
        }
    }

    private static class FFZDefaultSets {
        public final Map<String, FFZEmoteSet> sets;
        public final Map<String, List<String>> users;

        private FFZDefaultSets(int[] default_sets, Map<String, FFZEmoteSet> sets, Map<String, List<String>> users) {
            this.sets = sets;
            this.users = users;
        }
    }

    private static class FFZRoom {
        public final int _id;
        public final int twitch_id;
        public final String youtube_id;
        public final String id;
        public final boolean is_group;
        public final String display_name;
        public final int set;
        public final String moderator_badge;
        public final Object vip_badge;
        public final Object mod_urls;
        public final Object user_badges;
        public final Object user_badge_ids;
        public final String css;

        private FFZRoom(int _id, int twitch_id, String youtube_id, String id, boolean is_group, String display_name, int set, String moderator_badge, Object vip_badge, Object mod_urls, Object user_badges, Object user_badge_ids, String css) {
            this._id = _id;
            this.twitch_id = twitch_id;
            this.youtube_id = youtube_id;
            this.id = id;
            this.is_group = is_group;
            this.display_name = display_name;
            this.set = set;
            this.moderator_badge = moderator_badge;
            this.vip_badge = vip_badge;
            this.mod_urls = mod_urls;
            this.user_badges = user_badges;
            this.user_badge_ids = user_badge_ids;
            this.css = css;
        }
    }

    private static class FFZRoomResponse {
        public final FFZRoom room;
        public final Map<String, FFZEmoteSet> sets;

        private FFZRoomResponse(FFZRoom room, Map<String, FFZEmoteSet> sets) {
            this.room = room;
            this.sets = sets;
        }
    }

    private static class FFZMultiRoomResponse {
        public final List<FFZRoom> rooms;
        public final Map<String, FFZEmoteSet> sets;

        private FFZMultiRoomResponse(List<FFZRoom> rooms, Map<String, FFZEmoteSet> sets) {
            this.rooms = rooms;
            this.sets = sets;
        }
    }
}

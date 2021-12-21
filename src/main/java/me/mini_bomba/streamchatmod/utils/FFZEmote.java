package me.mini_bomba.streamchatmod.utils;

import java.util.Collections;
import java.util.Map;

public class FFZEmote {
    public final int id;
    public final String name;
    public final int height;
    public final int width;
    public final boolean isPublic;
    public final boolean hidden;
    public final boolean modifier;
    public final Owner owner;
    public final Map<String, String> urls;
    public final int status;
    public final int usage_count;

    protected FFZEmote(int id, String name, int height, int width, boolean isPublic, boolean hidden, boolean modifier, Owner owner, Map<String, String> urls, int status, int usage_count) {
        this.id = id;
        this.name = name;
        this.height = height;
        this.width = width;
        this.isPublic = isPublic;
        this.hidden = hidden;
        this.modifier = modifier;
        this.owner = owner;
        this.urls = Collections.unmodifiableMap(urls);
        this.status = status;
        this.usage_count = usage_count;
    }

    public String getSmallEmoteURL() {
        return "https:" + urls.get("1");
    }

    public String getMediumEmoteURL() {
        return urls.containsKey("2") ? "https:" + urls.get("2") : getSmallEmoteURL();
    }

    public String getLargeEmoteURL() {
        return urls.containsKey("4") ? "https:" + urls.get("4") : getMediumEmoteURL();
    }

    public static class Owner {
        public final String name;
        public final String displayName;

        protected Owner(String name, String displayName) {
            this.name = name;
            this.displayName = displayName;
        }
    }
}

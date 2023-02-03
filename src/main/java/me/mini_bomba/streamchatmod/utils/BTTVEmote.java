package me.mini_bomba.streamchatmod.utils;

public class BTTVEmote {
    public final String id;
    public final String name;
    public final ImageType imageType;
    public final String user;

    public BTTVEmote(String id, String name, ImageType imageType, String user) {
        this.id = id;
        this.name = name;
        this.imageType = imageType;
        this.user = user;
    }

    public BTTVApi.EmoteOwner getUser() {
        return BTTVApi.getUser(user);
    }

    public String getSmallEmoteURL() {
        return "https://cdn.betterttv.net/emote/" + id + "/1x";
    }

    public String getMediumEmoteURL() {
        return "https://cdn.betterttv.net/emote/" + id + "/2x";
    }

    public String getLargeEmoteURL() {
        return "https://cdn.betterttv.net/emote/" + id + "/3x";
    }

    public String getUserName() {
        BTTVApi.EmoteOwner owner = getUser();
        return owner != null ? owner.displayName : null;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof BTTVEmote && ((BTTVEmote) o).id.equals(this.id);
    }

    public enum ImageType {
        PNG("png"),
        GIF("gif");
        public final String extension;

        ImageType(String extension) {
            this.extension = extension;
        }
    }
}

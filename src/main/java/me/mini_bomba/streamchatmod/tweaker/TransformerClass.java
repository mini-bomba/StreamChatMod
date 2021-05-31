package me.mini_bomba.streamchatmod.tweaker;

public enum TransformerClass {

    GuiScreen("net/minecraft/client/gui/GuiScreen", "axu");

    private String name;
    private String srgClass;
    private String notchClass;

    TransformerClass(String srgClass, String notchClass) {
        this.srgClass = srgClass;
        this.notchClass = notchClass;
        name = StreamChatModTransformer.isDeobfuscated() ? srgClass : notchClass;
    }

    public String getNameRaw() {
        return name;
    }

    public String getName() {
        return "L"+name+";";
    }

    public String getTransformerName() {
        return srgClass.replaceAll("/", ".");
    }
}

package me.mini_bomba.streamchatmod.tweaker;

public enum TransformerClass {
    // Vanilla
    GuiScreen("net/minecraft/client/gui/GuiScreen", "axu"),

    // Vanilla Enhancements
    VE_BetterChat("com/orangemarshall/enhancements/modules/chat/BetterChat"),
    VE_BetterChatWithTabs("com/orangemarshall/enhancements/modules/chat/tab/BetterChatWithTabs"),
    VE_ChatTab("com/orangemarshall/enhancements/modules/chat/tab/ChatTab"),
    VE_GuiChatExtended("com/orangemarshall/enhancements/modules/chat/GuiChatExtended")
    ;

    private final String name;
    private final String srgClass;

    TransformerClass(String name) {
        this(name, name);
    }

    TransformerClass(String srgClass, String notchClass) {
        this.srgClass = srgClass;
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

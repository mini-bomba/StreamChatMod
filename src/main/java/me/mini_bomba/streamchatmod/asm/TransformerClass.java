package me.mini_bomba.streamchatmod.asm;

public enum TransformerClass {
    // Vanilla
    GuiScreen("net/minecraft/client/gui/GuiScreen", "axu"),
    FontRenderer("net/minecraft/client/gui/FontRenderer", "avn"),
    Minecraft("net/minecraft/client/Minecraft", "ave"),
    EntityPlayerSP("net/minecraft/client/entity/EntityPlayerSP", "bew"),

    // StreamChatMod
    GuiScreenHook("me/mini_bomba/streamchatmod/asm/hooks/GuiScreenHook"),
    FontRendererHook("me/mini_bomba/streamchatmod/asm/hooks/FontRendererHook"),

    // Vanilla Enhancements
    VE_BetterChat("com/orangemarshall/enhancements/modules/chat/BetterChat"),
    VE_BetterChatWithTabs("com/orangemarshall/enhancements/modules/chat/tab/BetterChatWithTabs"),
    VE_ChatTab("com/orangemarshall/enhancements/modules/chat/tab/ChatTab"),
    VE_GuiChatExtended("com/orangemarshall/enhancements/modules/chat/GuiChatExtended");

    private final String name;
    private final String srgClass;

    TransformerClass(String name) {
        this(name, name);
    }

    TransformerClass(String srgClass, String notchClass) {
        this.srgClass = srgClass;
        name = MainTransformer.isDeobfuscated() ? srgClass : notchClass;
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

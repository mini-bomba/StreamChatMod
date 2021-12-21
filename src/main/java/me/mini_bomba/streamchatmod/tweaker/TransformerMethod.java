package me.mini_bomba.streamchatmod.tweaker;

import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

public enum TransformerMethod {
    // Constructor
    init("<init>", "<init>", "<init>", "()V"),

    // GuiScreen
    GuiScreen_sendChatMessage("sendChatMessage", "func_175281_b", "b", "(Ljava/lang/String;Z)V"),

    // EntityPlayerSP
    EntityPlayerSP_sendChatMessage("sendChatMessage", "func_71165_d", "e", "(Ljava/lang/String;)V"),

    // FontRenderer
//    FontRenderer_renderStringAtPos("renderStringAtPos", "func_78255_a", "a", "(Ljava/lang/String;Z)V"),
    FontRenderer_getCharWidth("getCharWidth", "func_78263_a", "a", "(C)I"),
    FontRenderer_renderChar("renderChar", "func_181559_a", "a", "(CZ)F"),

    // Vanilla Enhancements
    VE_GuiChatExtended_keyTyped("func_73869_a", "(CI)V"),
    ;

    private final String name;
    private final String description;

    TransformerMethod(String name, String desc) {
        this(name, name, name, desc, desc);
    }

    TransformerMethod(String deobfName, String srgName, String notchName, String srgDesc) {
        this(deobfName, srgName, notchName, srgDesc, srgDesc);
    }

    TransformerMethod(String deobfName, String srgName, String notchName, String srgDesc, String notchDesc) {
        if (StreamChatModTransformer.isDeobfuscated()) {
            name = deobfName;
            description = srgDesc;
        } else {
            name = notchName;
            description = notchDesc;
        }
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
    
    public boolean matches(MethodInsnNode node) {
        return name.equals(node.name) && description.equals(node.desc);
    }
    
    public boolean matches(MethodNode node) {
        return name.equals(node.name) && (description.equals(node.desc) || this == init);
    }
}

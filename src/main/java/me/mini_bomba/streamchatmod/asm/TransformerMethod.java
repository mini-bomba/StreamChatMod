package me.mini_bomba.streamchatmod.asm;

import org.objectweb.asm.Opcodes;
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
    FontRenderer_renderStringAtPos("renderStringAtPos", "func_78255_a", "a", "(Ljava/lang/String;Z)V"),
    FontRenderer_getStringWidth("getStringWidth", "func_78256_a", "a", "(Ljava/lang/String;)I"),
    FontRenderer_sizeStringToWidth("sizeStringToWidth", "func_78259_e", "e", "(Ljava/lang/String;I)I"),
    FontRenderer_doDraw("doDraw", "(F)V"),

    // StreamChatMod ASM Hooks
    GuiScreenHook_redirectMessage("redirectMessage", "(Ljava/lang/String;)Ljava/lang/String;"),
    FontRendererHook_renderEmote("renderEmote", "(CCFF)F"),
    FontRendererHook_getEmoteWidth("getEmoteWidth", "(CC)I"),

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
        if (MainTransformer.isDeobfuscated()) {
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

    private MethodInsnNode invoke(int opcode, TransformerClass owner, boolean itf) {
        return new MethodInsnNode(opcode, owner.getNameRaw(), this.getName(), this.getDescription(), itf);
    }

    public MethodInsnNode invokeVirtual(TransformerClass owner, boolean itf) {
        return invoke(Opcodes.INVOKEVIRTUAL, owner, itf);
    }

    public MethodInsnNode invokeStatic(TransformerClass owner, boolean itf) {
        return invoke(Opcodes.INVOKESTATIC, owner, itf);
    }

    public boolean matches(MethodInsnNode node) {
        return name.equals(node.name) && description.equals(node.desc);
    }

    public boolean matches(MethodNode node) {
        return name.equals(node.name) && (description.equals(node.desc) || this == init);
    }
}

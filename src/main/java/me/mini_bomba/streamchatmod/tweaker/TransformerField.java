package me.mini_bomba.streamchatmod.tweaker;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.FieldInsnNode;

public enum TransformerField {
    // GuiScreen
    GuiScreen_mc("mc", "j", "Lave;", "Lnet/minecraft/client/Minecraft;"),

    // Minecraft
    Minecraft_thePlayer("thePlayer", "h", "Lbew;", "Lnet/minecraft/client/entity/EntityPlayerSP;");


    private String name;
    private String type;

    TransformerField(String deobfName, String notchName, String srgType) {
        this(deobfName, notchName, srgType, srgType);
    }

    TransformerField(String deobfName, String notchName, String notchType, String srgType) {

        if (StreamChatModTransformer.isDeobfuscated()) {
            name = deobfName;
            type = srgType;
        } else {
            name = notchName;
            type = notchType;
        }
    }

    public FieldInsnNode getField(TransformerClass currentClass) {
        return new FieldInsnNode(Opcodes.GETFIELD, currentClass.getNameRaw(), name, type);
    }

    public FieldInsnNode putField(TransformerClass currentClass) {
        return new FieldInsnNode(Opcodes.PUTFIELD, currentClass.getNameRaw(), name, type);
    }

    public boolean matches(FieldInsnNode fieldInsnNode) {
        return this.name.equals(fieldInsnNode.name) && this.type.equals(fieldInsnNode.desc);
    }
}

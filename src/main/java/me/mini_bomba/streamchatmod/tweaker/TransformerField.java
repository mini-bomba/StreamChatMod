package me.mini_bomba.streamchatmod.tweaker;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.FieldInsnNode;

public enum TransformerField {
    // GuiScreen
    GuiScreen_mc("mc", "field_146297_k", "j", "Lave;", "Lnet/minecraft/client/Minecraft;"),

    // GuiChat
    GuiChat_inputField("inputField", "field_146415_a", "a", "Lavw;", "Lnet/minecraft/client/gui/GuiTextField;"),

    // Minecraft
    Minecraft_thePlayer("thePlayer", "field_71439_g", "h", "Lbew;", "Lnet/minecraft/client/entity/EntityPlayerSP;");


    private String transformerName;
    private String reflectorName;
    private String type;

    TransformerField(String deobfName, String srgName, String notchName, String srgType) {
        this(deobfName, srgName, notchName, srgType, srgType);
    }

    TransformerField(String deobfName, String srgName, String notchName, String notchType, String srgType) {

        if (StreamChatModTransformer.isDeobfuscated()) {
            transformerName = deobfName;
            reflectorName = deobfName;
            type = srgType;
        } else {
            transformerName = notchName;
            reflectorName = srgName;
            type = notchType;
        }
    }

    public String getTransformerName() {
        return transformerName;
    }

    public String getReflectorName() {
        return reflectorName;
    }

    public FieldInsnNode getField(TransformerClass currentClass) {
        return new FieldInsnNode(Opcodes.GETFIELD, currentClass.getNameRaw(), transformerName, type);
    }

    public FieldInsnNode putField(TransformerClass currentClass) {
        return new FieldInsnNode(Opcodes.PUTFIELD, currentClass.getNameRaw(), transformerName, type);
    }

    public boolean matches(FieldInsnNode fieldInsnNode) {
        return this.transformerName.equals(fieldInsnNode.name) && this.type.equals(fieldInsnNode.desc);
    }
}

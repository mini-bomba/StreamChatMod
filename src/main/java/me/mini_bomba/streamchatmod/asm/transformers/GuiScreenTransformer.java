package me.mini_bomba.streamchatmod.asm.transformers;

import me.mini_bomba.streamchatmod.tweaker.IStreamTransformer;
import me.mini_bomba.streamchatmod.tweaker.TransformerClass;
import me.mini_bomba.streamchatmod.tweaker.TransformerField;
import me.mini_bomba.streamchatmod.tweaker.TransformerMethod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.Iterator;

public class GuiScreenTransformer implements IStreamTransformer {

    private static final Logger LOGGER = LogManager.getLogger();

    @Override
    public String[] getClassName() {
        return new String[]{TransformerClass.GuiScreen.getTransformerName()};
    }

    @Override
    public void transform(ClassNode classNode, String name) {
        try {
            for (MethodNode methodNode : classNode.methods) {
                if (TransformerMethod.GuiScreen_sendChatMessage.matches(methodNode)) {
                    // Insert GuiScreenHook.redirectMessage(msg) before the sendChatMessage call
                    Iterator<AbstractInsnNode> iterator = methodNode.instructions.iterator();
                    boolean inserted = false;
                    while (iterator.hasNext()) {
                        AbstractInsnNode abstractNode = iterator.next();
                        if (verifyRedirectMessageLocation(abstractNode)) {
                            methodNode.instructions.insertBefore(abstractNode, insertRedirectMessage());
                            LOGGER.info("GuiScreenHook.redirectMessage() inserted");
                            inserted = true;
                            break;
                        }
                    }
                    if (!inserted) LOGGER.warn("GuiScreenHook.redirectMessage() was not inserted!");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static InsnList insertRedirectMessage() {
        // Insert instructions:
        // ALOAD 1
        // INVOKESTATIC me/mini_bomba/asm/hooks/GuiScreenHook redirectMessage
        // IFEQ <skipReturnLabel>
        // RETURN
        // <skipReturnLabel>
        // Java Code inserted:
        // if (me.mini_bomba.streamchatmod.asm.hooks.GuiScreenHook.redirectMessage(msg)) return;
        InsnList list = new InsnList();
        LabelNode skipReturnLabel = new LabelNode();

        list.add(new VarInsnNode(Opcodes.ALOAD, 1));
        list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "me/mini_bomba/streamchatmod/asm/hooks/GuiScreenHook", "redirectMessage", "(Ljava/lang/String;)Z", false));
        list.add(new JumpInsnNode(Opcodes.IFEQ, skipReturnLabel));
        list.add(new InsnNode(Opcodes.RETURN));
        list.add(skipReturnLabel);

        return list;
    }

    private static boolean verifyRedirectMessageLocation(AbstractInsnNode node) {
        // Looking for instructions:
        // ALOAD 0
        // GETFIELD mc
        // GETFIELD thePlayer
        // ALOAD 1
        // INVOKEVIRTUAL sendChatMessage
        if (!(node instanceof VarInsnNode && node.getOpcode() == Opcodes.ALOAD && ((VarInsnNode) node).var == 0)) return false;

        node = node.getNext();
        if (!(node instanceof FieldInsnNode && node.getOpcode() == Opcodes.GETFIELD && TransformerField.GuiScreen_mc.matches((FieldInsnNode) node))) return false;

        node = node.getNext();
        if (!(node instanceof FieldInsnNode && node.getOpcode() == Opcodes.GETFIELD && TransformerField.Minecraft_thePlayer.matches((FieldInsnNode) node))) return false;

        node = node.getNext();
        if (!(node instanceof VarInsnNode && node.getOpcode() == Opcodes.ALOAD && ((VarInsnNode) node).var == 1)) return false;

        node = node.getNext();
        return node instanceof MethodInsnNode && node.getOpcode() == Opcodes.INVOKEVIRTUAL && TransformerMethod.EntityPlayerSP_sendChatMessage.matches((MethodInsnNode) node);
    }
}

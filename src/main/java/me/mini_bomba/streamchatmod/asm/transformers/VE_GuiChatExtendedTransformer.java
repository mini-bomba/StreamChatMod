package me.mini_bomba.streamchatmod.asm.transformers;

import me.mini_bomba.streamchatmod.tweaker.IStreamTransformer;
import me.mini_bomba.streamchatmod.tweaker.TransformerClass;
import me.mini_bomba.streamchatmod.tweaker.TransformerMethod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.Iterator;

public class VE_GuiChatExtendedTransformer implements IStreamTransformer {

    private static final Logger LOGGER = LogManager.getLogger();

    @Override
    public String[] getClassName() {
        return new String[]{TransformerClass.VE_GuiChatExtended.getTransformerName()};
    }

    @Override
    public void transform(ClassNode classNode, String name) {
        try {
            for (MethodNode methodNode : classNode.methods) {
                if (TransformerMethod.VE_GuiChatExtended_keyTyped.matches(methodNode)) {
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
            LOGGER.error("An unexpected exception was encoutered while transforming GuiChatExtended");
            e.printStackTrace();
        }
    }

    private static boolean verifyRedirectMessageLocation(AbstractInsnNode node) {
        // Looking for instructions:
        // NEW net/minecraft/network/play/client/C01PacketChatMessage
        return node instanceof TypeInsnNode && node.getOpcode() == Opcodes.NEW && ((TypeInsnNode) node).desc.equals("net/minecraft/network/play/client/C01PacketChatMessage");
    }

    private static InsnList insertRedirectMessage() {
        /* Insert instructions:
         * ALOAD 3
         * INVOKESTATIC me/mini_bomba/asm/hooks/GuiScreenHook redirectMessage
         * ASTORE 3
         * ALOAD 3
         * INVOKEVIRTUAL java/lang/String length
         * IFNE <skipReturnLabel>
         * ALOAD 0
         * ILOAD 1
         * I_CONST_1
         * INVOKEVIRTUAL GuiChatExtended.keyTyped
         * RETURN
         * <skipReturnLabel>
         *
         * Java Code inserted:
         * msg = me.mini_bomba.streamchatmod.asm.hooks.GuiScreenHook.redirectMessage(msg)
         * if (msg.length() == 0) {
         *  this.keyTyped(typedChar, 1);
         *  return;
         * };
         */
        InsnList list = new InsnList();
        LabelNode skipReturnLabel = new LabelNode();

        list.add(new VarInsnNode(Opcodes.ALOAD, 3));
        list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "me/mini_bomba/streamchatmod/asm/hooks/GuiScreenHook", "redirectMessage", "(Ljava/lang/String;)Ljava/lang/String;", false));
        list.add(new VarInsnNode(Opcodes.ASTORE, 3));
        list.add(new VarInsnNode(Opcodes.ALOAD, 3));
        list.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/String", "length", "()I", false));
        list.add(new JumpInsnNode(Opcodes.IFNE, skipReturnLabel));
        list.add(new VarInsnNode(Opcodes.ALOAD, 0));
        list.add(new VarInsnNode(Opcodes.ILOAD, 1));
        list.add(new InsnNode(Opcodes.ICONST_1));
        list.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, TransformerClass.VE_GuiChatExtended.getNameRaw(), TransformerMethod.VE_GuiChatExtended_keyTyped.getName(), TransformerMethod.VE_GuiChatExtended_keyTyped.getDescription(), false));
        list.add(new InsnNode(Opcodes.RETURN));
        list.add(skipReturnLabel);

        return list;
    }
}

package me.mini_bomba.streamchatmod.asm.transformers;

import me.mini_bomba.streamchatmod.tweaker.IStreamTransformer;
import me.mini_bomba.streamchatmod.tweaker.TransformerClass;
import me.mini_bomba.streamchatmod.tweaker.TransformerField;
import me.mini_bomba.streamchatmod.tweaker.TransformerMethod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

public class FontRendererTransformer implements IStreamTransformer {

    private static final Logger LOGGER = LogManager.getLogger();

    @Override
    public String[] getClassName() {
        return new String[]{TransformerClass.FontRenderer.getTransformerName()};
    }

    @Override
    public void transform(ClassNode classNode, String name) {
        try {
            for (MethodNode methodNode : classNode.methods) {
                if (TransformerMethod.FontRenderer_renderChar.matches(methodNode)) {
                    methodNode.instructions.insertBefore(methodNode.instructions.getFirst(), insertRenderEmote());
                    LOGGER.info("renderEmote() inserted");
                    // Prepend a new if statement to the if block in the for loop, calling FontRendererHook.renderEmote()
//                    Iterator<AbstractInsnNode> iterator = methodNode.instructions.iterator();
//                    int jumpInsns = 0;
//                    LabelNode targetLabel = null;
//                    AbstractInsnNode abstractNode = null;
//                    while (iterator.hasNext() && targetLabel == null) {
//                        abstractNode = iterator.next();
//                        if (abstractNode instanceof JumpInsnNode && ++jumpInsns == 2)
//                            targetLabel = ((JumpInsnNode) abstractNode).label;
//                    }
//                    LabelNode exitLabel = null;
//                    while (iterator.hasNext() && abstractNode != targetLabel) {
//                        abstractNode = iterator.next();
//                        if (abstractNode instanceof JumpInsnNode && abstractNode.getOpcode() == Opcodes.GOTO)
//                            exitLabel = ((JumpInsnNode) abstractNode).label;
//                    }
//                    if (targetLabel == null) LOGGER.warn("Could not find the 'else' block of the if statement!");
//                    else if (exitLabel == null) LOGGER.warn("Could not find the return label!");
//                    else methodNode.instructions.insert(targetLabel, insertRenderEmote(exitLabel));
                } else if (TransformerMethod.FontRenderer_getCharWidth.matches(methodNode)) {
                    methodNode.instructions.insertBefore(methodNode.instructions.getFirst(), insertGetEmoteWidth());
                }
            }
        } catch (Exception e) {
            LOGGER.error("An unexpected exception was encoutered while transforming FontRenderer");
            e.printStackTrace();
        }
    }

    private static InsnList insertRenderEmote() {
        /* Insert instructions:
         * ILOAD 1
         * LDC 0xe800
         * IF_ICMPLT <elseLabel>
         * ILOAD 1
         * LDC 0xf000
         * IF_ICMPGE <elseLabel>
         * ILOAD 1
         * ALOAD 0
         * GETFIELD this.posX
         * ALOAD 0
         * GETFIELD this.posY
         * INVOKESTATIC me.mini_bomba.streamchatmod.asm.hooks.FontRendererHook.renderEmote()
         * RETURN
         * <elseLabel>
         *
         * exitNode is referring to the label provided to this function, the one skipping the else block
         *
         * Java Code inserted:
         * if (c0 >= 0xe800 && c0 < 0xf000) {
         *  me.mini_bomba.streamchatmod.asm.hooks.FontRendererHook.renderEmote(c0, this.posX, this.posY)
         * } else ...
         */
        InsnList list = new InsnList();
        LabelNode elseLabel = new LabelNode();

        list.add(new VarInsnNode(Opcodes.ILOAD, 1));
        list.add(new LdcInsnNode(0xe800));
        list.add(new JumpInsnNode(Opcodes.IF_ICMPLT, elseLabel));
        list.add(new VarInsnNode(Opcodes.ILOAD, 1));
        list.add(new LdcInsnNode(0xf000));
        list.add(new JumpInsnNode(Opcodes.IF_ICMPGE, elseLabel));
        list.add(new VarInsnNode(Opcodes.ILOAD, 1));
        list.add(new VarInsnNode(Opcodes.ALOAD, 0));
        list.add(TransformerField.FontRenderer_posX.getField(TransformerClass.FontRenderer));
        list.add(new VarInsnNode(Opcodes.ALOAD, 0));
        list.add(TransformerField.FontRenderer_posY.getField(TransformerClass.FontRenderer));
        list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "me/mini_bomba/streamchatmod/asm/hooks/FontRendererHook", "renderEmote", "(CFF)F", false));

        list.add(new InsnNode(Opcodes.FRETURN));
        list.add(elseLabel);

        return list;
    }

    private static InsnList insertGetEmoteWidth() {
        /* Insert instructions:
         * ILOAD 1
         * LDC 0xe800
         * IF_ICMPLT <falseLabel>
         * ILOAD 1
         * LDC 0xf000
         * IF_ICMPGE <falseLabel>
         * ILOAD 1
         * INVOKESTATIC me.mini_bomba.streamchatmod.asm.hooks.FontRendererHook.getEmoteWidth()
         * IRETURN
         * <falseLabel>
         *
         * Java Code inserted:
         * if (character >= 0xe800 && character < 0xf000) {
         *  return me.mini_bomba.streamchatmod.asm.hooks.FontRendererHook.getEmoteWidth(character);
         * } else ...
         */
        InsnList list = new InsnList();
        LabelNode falseLabel = new LabelNode();

        list.add(new VarInsnNode(Opcodes.ILOAD, 1));
        list.add(new LdcInsnNode(0xe800));
        list.add(new JumpInsnNode(Opcodes.IF_ICMPLT, falseLabel));
        list.add(new VarInsnNode(Opcodes.ILOAD, 1));
        list.add(new LdcInsnNode(0xf000));
        list.add(new JumpInsnNode(Opcodes.IF_ICMPGE, falseLabel));
        list.add(new VarInsnNode(Opcodes.ILOAD, 1));
        list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "me/mini_bomba/streamchatmod/asm/hooks/FontRendererHook", "getEmoteWidth", "(C)I", false));
        list.add(new InsnNode(Opcodes.IRETURN));
        list.add(falseLabel);
        return list;
    }
}

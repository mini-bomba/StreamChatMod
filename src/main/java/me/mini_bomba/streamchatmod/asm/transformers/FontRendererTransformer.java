package me.mini_bomba.streamchatmod.asm.transformers;

import me.mini_bomba.streamchatmod.asm.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.Iterator;

public class FontRendererTransformer implements IStreamTransformer {

    private static final Logger LOGGER = LogManager.getLogger();

    @Override
    public String[] getClassName() {
        return new String[]{TransformerClass.FontRenderer.getTransformerName()};
    }

    @Override
    public void transform(ClassNode classNode, String name) {
        try {
            LOGGER.info("Begin FontRenderer transformer");
            for (MethodNode methodNode : classNode.methods) {
                // Prepend a new if statement to the if block in the for loop, calling FontRendererHook.renderEmote()
                if (TransformerMethod.FontRenderer_renderStringAtPos.matches(methodNode)) {
                    Iterator<AbstractInsnNode> iterator = methodNode.instructions.iterator();
                    LabelNode returnLabel = null;
                    while (iterator.hasNext()) {
                        AbstractInsnNode node = iterator.next();
                        if (node instanceof VarInsnNode) {
                            if (returnLabel == null && node.getOpcode() == Opcodes.ILOAD && ((VarInsnNode) node).var == 3) {
                                if (node.getPrevious() instanceof LabelNode)
                                    returnLabel = (LabelNode) node.getPrevious();
                                else {
                                    returnLabel = new LabelNode();
                                    methodNode.instructions.insertBefore(node, returnLabel);
                                }
                            } else if (node.getOpcode() == Opcodes.ISTORE && ((VarInsnNode) node).var == 4) {
                                if (returnLabel != null) {
                                    methodNode.instructions.insert(node, insertRenderEmote(returnLabel));
                                    LOGGER.info("renderEmote() inserted");
                                } else
                                    LOGGER.warn("Found place to insert renderEmote(), but returnLabel was not found!");
                                break;
                            }
                        }
                    }
                } else if (TransformerMethod.FontRenderer_getStringWidth.matches(methodNode)) {
                    Iterator<AbstractInsnNode> iterator = methodNode.instructions.iterator();
                    while (iterator.hasNext()) {
                        AbstractInsnNode node = iterator.next();
                        LabelNode label = verifyPositionForGetEmoteWidth(node);
                        if (label != null) {
                            methodNode.instructions.insertBefore(node, insertGetEmoteWidth_getStringWidth(label));
                            LOGGER.info("insertGetEmoteWidth() inserted into getStringWidth()");
                            break;
                        }
                    }
                } else if (TransformerMethod.FontRenderer_sizeStringToWidth.matches(methodNode)) {
                    // TODO: Check if sizeStringToWidth() is working properly on emotes
                    Iterator<AbstractInsnNode> iterator = methodNode.instructions.iterator();
                    while (iterator.hasNext()) {
                        AbstractInsnNode node = iterator.next();
                        if (!(node instanceof LookupSwitchInsnNode)) continue;
                        LookupSwitchInsnNode castedNode = (LookupSwitchInsnNode) node;
                        AbstractInsnNode defaultBlockStart = castedNode.dflt.getNext().getNext();
                        LabelNode skipLabel = new LabelNode();
                        methodNode.instructions.insert(defaultBlockStart.getNext().getNext().getNext().getNext().getNext().getNext(), skipLabel);
                        methodNode.instructions.insert(defaultBlockStart, insertGetEmoteWidth_sizeStringToWidth(skipLabel));
                        LOGGER.info("insertGetEmoteWidth() inserted into sizeStringToWidth()");
                    }
                }
                // TODO: Hook into trimStringToWidth()
            }
        } catch (Exception e) {
            LOGGER.error("An unexpected exception was encountered while transforming FontRenderer");
            e.printStackTrace();
        }
        LOGGER.info("End FontRenderer transformer");
    }

    private static LabelNode verifyPositionForGetEmoteWidth(AbstractInsnNode node) {
        // We're looking for if (k < 0 && j < text.length() - 1)
        // also return the end-if label
        if (!TransformerUtils.verifyNode(node, new VarInsnNode(Opcodes.ILOAD, 6))) return null; //k

        node = node.getNext();
        if (!TransformerUtils.verifyNode(node, new JumpInsnNode(Opcodes.IFGE, null))) return null;
        LabelNode label = ((JumpInsnNode) node).label; // save the end-if label

        node = node.getNext();
        if (!TransformerUtils.verifyNode(node, new VarInsnNode(Opcodes.ILOAD, 4))) return null; // j

        node = node.getNext();
        if (!TransformerUtils.verifyNode(node, new VarInsnNode(Opcodes.ALOAD, 1))) return null; // text

        node = node.getNext();
        if (!TransformerUtils.verifyNode(node, new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/String", "length", "()I", false)))
            return null; // text.length()

        node = node.getNext();
        if (!TransformerUtils.verifyNode(node, new InsnNode(Opcodes.ICONST_1))) return null;

        node = node.getNext();
        if (!TransformerUtils.verifyNode(node, new InsnNode(Opcodes.ISUB))) return null;

        node = node.getNext();
        if (!TransformerUtils.verifyNode(node, new JumpInsnNode(Opcodes.IF_ICMPGE, null))) return null;
        return label;
    }

    private static InsnList insertGetEmoteWidth_getStringWidth(LabelNode skipLabel) {
        /* Equivalent of Java code:
         * if (c0 >= 0xDBC0 && c0 < 0xDC00 && j+1 < text.length() && text.charAt(j+1) >= 0xDC00 && text.charAt(j+1) < 0xE000) {
         *   ++j;
         *   k = me.mini_bomba.streamchatmod.asm.hooks.FontRendererHook.getEmoteWidth(c0, text.charAt(i+1))
         * }
         */
        InsnList list = new InsnList();
        LabelNode falseLabel = new LabelNode();

        // if (c0 >= 0xDBC0 && c0 < 0xDC00 && j+1 < text.length() && text.charAt(j+1) >= 0xDC00 && text.charAt(j+1) < 0xE000)
        list.add(insertVerifyPUAEmote(falseLabel, 1, 4, 5));

        // ++j;
        list.add(new IincInsnNode(4, 1));
        // k = me.mini_bomba.streamchatmod.asm.hooks.FontRendererHook.getEmoteWidth(c0, text.charAt(i+1))
        list.add(new VarInsnNode(Opcodes.ILOAD, 5));
        list.add(new VarInsnNode(Opcodes.ALOAD, 1));
        list.add(new VarInsnNode(Opcodes.ILOAD, 4));
        list.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/String", "charAt", "(I)C", false));
        list.add(TransformerMethod.FontRendererHook_getEmoteWidth.invokeStatic(TransformerClass.FontRendererHook, false));
        list.add(new VarInsnNode(Opcodes.ISTORE, 6));

        list.add(new JumpInsnNode(Opcodes.GOTO, skipLabel));
        list.add(falseLabel);

        return list;
    }

    private static InsnList insertGetEmoteWidth_sizeStringToWidth(LabelNode skipLabel) {
        /* Equivalent of Java code:
         * if (c0 >= 0xDBC0 && c0 < 0xDC00 && j+1 < text.length() && text.charAt(j+1) >= 0xDC00 && text.charAt(j+1) < 0xE000) {
         *   ++j;
         *   k = me.mini_bomba.streamchatmod.asm.hooks.FontRendererHook.getEmoteWidth(c0, text.charAt(i+1))
         * }
         */
        InsnList list = new InsnList();
        LabelNode falseLabel = new LabelNode();

        // if (c0 >= 0xDBC0 && c0 < 0xDC00 && j+1 < str.length() && str.charAt(k+1) >= 0xDC00 && str.charAt(k+1) < 0xE000)
        list.add(insertVerifyPUAEmote(falseLabel, 1, 5, 8));

        // ++k;
        list.add(new IincInsnNode(5, 1));
        // l += me.mini_bomba.streamchatmod.asm.hooks.FontRendererHook.getEmoteWidth(c0, text.charAt(i+1))
        list.add(new VarInsnNode(Opcodes.ILOAD, 4));
        list.add(new VarInsnNode(Opcodes.ILOAD, 8));
        list.add(new VarInsnNode(Opcodes.ALOAD, 1));
        list.add(new VarInsnNode(Opcodes.ILOAD, 5));
        list.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/String", "charAt", "(I)C", false));
        list.add(TransformerMethod.FontRendererHook_getEmoteWidth.invokeStatic(TransformerClass.FontRendererHook, false));
        list.add(new InsnNode(Opcodes.IADD));
        list.add(new VarInsnNode(Opcodes.ISTORE, 4));

        list.add(new JumpInsnNode(Opcodes.GOTO, skipLabel));
        list.add(falseLabel);

        return list;
    }

    private static InsnList insertRenderEmote(LabelNode continueLabel) {
        /* Equivalent of Java code:
         * if (c0 >= 0xDBC0 && c0 < 0xDC00 && i+1 < text.length() && text.charAt(i+1) >= 0xDC00 && text.charAt(i+1) < 0xE000) {
         *   doDraw(me.mini_bomba.streamchatmod.asm.hooks.FontRendererHook.renderEmote(c0, text.charAt(i+1), this.posX, this.posY));
         *   i+=2;
         *   continue
         * } else ...
         */
        InsnList list = new InsnList();
        LabelNode falseLabel = new LabelNode();

        // if (c0 >= 0xDBC0 && c0 < 0xDC00 && i+1 < text.length() && text.charAt(i+1) >= 0xDC00 && text.charAt(i+1) < 0xE000)
        list.add(insertVerifyPUAEmote(falseLabel, 1, 3, 4));

        // doDraw(
        list.add(new VarInsnNode(Opcodes.ALOAD, 0));
        // me.mini_bomba.streamchatmod.asm.hooks.FontRendererHook.renderPUACharacter(c0, text.charAt(i+1), this.posX, this.posY)
        list.add(new VarInsnNode(Opcodes.ILOAD, 4));
        list.add(insertString_charAt_plus1(1, 3));
        list.add(new VarInsnNode(Opcodes.ALOAD, 0));
        list.add(TransformerField.FontRenderer_posX.getField(TransformerClass.FontRenderer));
        list.add(new VarInsnNode(Opcodes.ALOAD, 0));
        list.add(TransformerField.FontRenderer_posY.getField(TransformerClass.FontRenderer));
        list.add(TransformerMethod.FontRendererHook_renderEmote.invokeStatic(TransformerClass.FontRendererHook, false));
        // );
        list.add(TransformerMethod.FontRenderer_doDraw.invokeVirtual(TransformerClass.FontRenderer, false));

        // i+=2
        list.add(new IincInsnNode(3, 2));
        // continue
        list.add(new JumpInsnNode(Opcodes.GOTO, continueLabel));

        list.add(falseLabel);
        return list;
    }

    private static InsnList insertVerifyPUAEmote(LabelNode falseLabel, int stringVar, int indexVar, int currentCharVar) {
        InsnList list = new InsnList();

        // if ( c0 >= 0xDBC0
        list.add(new VarInsnNode(Opcodes.ILOAD, currentCharVar));
        list.add(new LdcInsnNode(0xDBC0));
        list.add(new JumpInsnNode(Opcodes.IF_ICMPLT, falseLabel));
        // c0 < 0xDC00
        list.add(new VarInsnNode(Opcodes.ILOAD, currentCharVar));
        list.add(new LdcInsnNode(0xDC00));
        list.add(new JumpInsnNode(Opcodes.IF_ICMPGE, falseLabel));
        // i+1 < text.length()
        list.add(new VarInsnNode(Opcodes.ILOAD, indexVar));
        list.add(new InsnNode(Opcodes.ICONST_1));
        list.add(new InsnNode(Opcodes.IADD));
        list.add(new VarInsnNode(Opcodes.ALOAD, stringVar));
        list.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/String", "length", "()I", false));
        list.add(new JumpInsnNode(Opcodes.IF_ICMPGE, falseLabel));
        // text.charAt(i+1) >= 0xDC00
        list.add(insertString_charAt_plus1(stringVar, indexVar));
        list.add(new LdcInsnNode(0xDC00));
        list.add(new JumpInsnNode(Opcodes.IF_ICMPLT, falseLabel));
        // text.charAt(i+1) < 0xE000 )
        list.add(insertString_charAt_plus1(stringVar, indexVar));
        list.add(new LdcInsnNode(0xE000));
        list.add(new JumpInsnNode(Opcodes.IF_ICMPGE, falseLabel));

        return list;
    }

    private static InsnList insertString_charAt_plus1(int stringVar, int indexVar) {
        InsnList list = new InsnList();
        list.add(new VarInsnNode(Opcodes.ALOAD, stringVar));
        list.add(new VarInsnNode(Opcodes.ILOAD, indexVar));
        list.add(new InsnNode(Opcodes.ICONST_1));
        list.add(new InsnNode(Opcodes.IADD));
        list.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/String", "charAt", "(I)C", false));
        return list;
    }
}

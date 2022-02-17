package me.mini_bomba.streamchatmod.asm;

import org.apache.commons.lang.NotImplementedException;
import org.objectweb.asm.tree.*;

public class TransformerUtils {
    public static boolean verifyNode(AbstractInsnNode node, AbstractInsnNode template) {
        if (!template.getClass().isInstance(node) || node.getOpcode() != template.getOpcode()) return false;
        if (node instanceof InsnNode || node instanceof JumpInsnNode)
            return true;
        if (node instanceof VarInsnNode)
            return ((VarInsnNode) node).var == ((VarInsnNode) template).var;
        if (node instanceof MethodInsnNode) {
            MethodInsnNode castedNode = (MethodInsnNode) node;
            MethodInsnNode castedTemplate = (MethodInsnNode) template;
            return castedNode.name.equals(castedTemplate.name) && castedNode.desc.equals(castedTemplate.desc)
                    && castedNode.owner.equals(castedTemplate.owner) && castedNode.itf == castedTemplate.itf;
        }
        if (node instanceof FieldInsnNode) {
            FieldInsnNode castedNode = (FieldInsnNode) node;
            FieldInsnNode castedTemplate = (FieldInsnNode) template;
            return castedNode.name.equals(castedTemplate.name) && castedNode.desc.equals(castedTemplate.desc)
                    && castedNode.owner.equals(castedTemplate.owner);
        }
        throw new NotImplementedException();
    }

    public static String nodeToString(AbstractInsnNode node) {
        if (node instanceof LineNumberNode) return ":Line " + ((LineNumberNode) node).line;
        String result = node.getClass().getSimpleName() + " " + node.getOpcode();
        if (node instanceof VarInsnNode) {
            result += " " + ((VarInsnNode) node).var;
        } else if (node instanceof MethodInsnNode) {
            MethodInsnNode castedNode = (MethodInsnNode) node;
            result += " " + castedNode.owner + "." + castedNode.name + castedNode.desc;
        } else if (node instanceof FieldInsnNode) {
            FieldInsnNode castedNode = (FieldInsnNode) node;
            result += " " + castedNode.owner + "." + castedNode.name + " = " + castedNode.desc;
        } else if (node instanceof LabelNode) {
            result += " " + ((LabelNode) node).getLabel().toString();
        } else if (node instanceof JumpInsnNode) {
            result += " " + ((JumpInsnNode) node).label.getLabel().toString();
        }
        return result;
    }
}

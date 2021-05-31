package me.mini_bomba.streamchatmod.tweaker;

import org.objectweb.asm.tree.ClassNode;

public interface IStreamTransformer {
    String[] getClassName();

    void transform(ClassNode classNode, String name);

    default boolean nameMatches(String method, String... names) {
        for (String name : names)
            if (method.equals(name))
                return true;
        return false;
    }
}

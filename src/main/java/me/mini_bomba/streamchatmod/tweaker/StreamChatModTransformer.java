package me.mini_bomba.streamchatmod.tweaker;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import lombok.Getter;
import me.mini_bomba.streamchatmod.asm.transformers.GuiScreenTransformer;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.fml.relauncher.FMLRelaunchLog;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.logging.log4j.Level;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Collection;

public class StreamChatModTransformer implements IClassTransformer {

    @Getter
    private static boolean deobfuscated = (boolean) Launch.blackboard.get("fml.deobfuscatedEnvironment");

    private final Multimap<String, IStreamTransformer> transformerMap = ArrayListMultimap.create();

    public StreamChatModTransformer() {
        registerTransformer(new GuiScreenTransformer());
    }

    private void registerTransformer(IStreamTransformer transformer) {
        for (String cls : transformer.getClassName())
            transformerMap.put(cls, transformer);
    }

    @Override
    public byte[] transform(String name, String transformedName, byte[] bytes) {
        if (bytes == null) return null;

        Collection<IStreamTransformer> transformers = transformerMap.get(transformedName);
        if (transformers.isEmpty()) return bytes;

        log(Level.INFO, "Found " + transformers.size() + " transformers for " + transformedName);

        ClassReader reader = new ClassReader(bytes);
        ClassNode node = new ClassNode();
        reader.accept(node, ClassReader.EXPAND_FRAMES);

        MutableInt classWriterFlags = new MutableInt(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);

        transformers.forEach(transformer -> {
            log(Level.INFO, "Applying transformer " + transformer.getClass().getName() + " on " + transformedName);
            transformer.transform(node, transformedName);
        });

        ClassWriter writer = new ClassWriter(classWriterFlags.getValue());

        try {
            node.accept(writer);
        } catch (Throwable e) {
            log(Level.ERROR, "Could not transform " + transformedName);
            e.printStackTrace();
            outputByteCode(transformedName, writer);
            return bytes;
        }

        outputByteCode(transformedName, writer);

        return writer.toByteArray();
    }

    private void outputByteCode(String transformedName, ClassWriter writer) {
        try {
            File bytecodeDirectory = new File("bytecode");
            if (!bytecodeDirectory.exists()) return;

            File bytecodeOutput = new File(bytecodeDirectory, transformedName + ".class");
            if (!bytecodeOutput.exists()) bytecodeOutput.createNewFile();

            FileOutputStream os = new FileOutputStream(bytecodeOutput);
            os.write(writer.toByteArray());
            os.close();
        } catch (Exception e) {
            log(Level.ERROR, "Could not write bytecode of transformed class \"" + transformedName + "\" to file");
            e.printStackTrace();
        }
    }

    public void log(Level level, String message) {
        String name = "StreamChatMod/"+this.getClass().getSimpleName();
        FMLRelaunchLog.log(name, level, (StreamChatModTransformer.isDeobfuscated() ? "" : "[" + name + "] ") + message);
    }
}

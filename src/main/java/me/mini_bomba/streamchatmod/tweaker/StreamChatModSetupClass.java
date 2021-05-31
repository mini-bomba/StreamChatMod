package me.mini_bomba.streamchatmod.tweaker;

import net.minecraftforge.fml.relauncher.IFMLCallHook;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;
import java.util.Map;

public class StreamChatModSetupClass implements IFMLCallHook {

    private static final Logger LOGGER = LogManager.getLogger();

    @Override
    public void injectData(Map<String, Object> data) {

    }

    @Override
    public Void call() {
        LOGGER.info( "Looking for duplicate StreamChatMod's");

        try {
            Field nameField = Class.forName("net.minecraftforge.fml.relauncher.CoreModManager$FMLPluginWrapper").getField("name");
            boolean coreFound = false;

            nameField.setAccessible(true);

            for (Object coreMod : StreamChatModLoadingPlugin.coremodList) {
                String name = (String) nameField.get(coreMod);

                if (name.equals(StreamChatModLoadingPlugin.class.getSimpleName())) {
                    if (coreFound)
                        throw new RuntimeException("Launch failed due to a duplicate StreamChatMod installation found. Please remove the duplicate & restart Minecraft!");
                    coreFound = true;
                }
            }

            nameField.setAccessible(false);
            LOGGER.info("No duplicate installations found!");
        } catch (ReflectiveOperationException e) {
            LOGGER.error("Could not check for duplicate StreamChatMod's!");
            e.printStackTrace();
        }

        return null;
    }
}

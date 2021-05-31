package me.mini_bomba.streamchatmod.tweaker;

import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

import java.util.List;
import java.util.Map;

@IFMLLoadingPlugin.MCVersion(ForgeVersion.mcVersion)
public class StreamChatModLoadingPlugin implements IFMLLoadingPlugin {

    static List<Object> coremodList;

    @Override
    public String[] getASMTransformerClass() {
        return new String[]{StreamChatModTransformer.class.getName()};
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Override
    public String getSetupClass() {
        return StreamChatModSetupClass.class.getName();
    }

    @Override
    public void injectData(Map<String, Object> data) {
        coremodList = (List<Object>) data.get("coremodList");
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}

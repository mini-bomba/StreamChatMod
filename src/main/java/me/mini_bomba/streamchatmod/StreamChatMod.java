package me.mini_bomba.streamchatmod;

import net.minecraft.init.Blocks;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

@Mod(modid = StreamChatMod.MODID, version = StreamChatMod.VERSION, clientSideOnly = true, name = StreamChatMod.MODNAME)
public class StreamChatMod
{
    public static final String MODID = "streamchatmod";
    public static final String MODNAME = "StreamChat";
    public static final String VERSION = "1.0";
    
    @EventHandler
    public void init(FMLInitializationEvent event)
    {
		// some example code
        System.out.println("DIRT BLOCK >> "+Blocks.dirt.getUnlocalizedName());
    }
}

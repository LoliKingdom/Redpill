package zone.rong.redpill;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.*;
import org.apache.logging.log4j.Logger;
import zone.rong.redpill.module.BlockStateCounter;

@Mod(modid = Redpill.MOD_ID, name = Redpill.NAME, version = "1.0", dependencies = "after:*")
@Mod.EventBusSubscriber
public class Redpill {

    public static final String MOD_ID = "redpill";
    public static final String NAME = "Redpill";

    public static Logger LOGGER;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        LOGGER = event.getModLog();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
    }

    @Mod.EventHandler
    public void loadComplete(FMLLoadCompleteEvent event) throws Throwable {
        if (RedpillConfig.BLOCKSTATE_COUNTER.enable) {
            BlockStateCounter.count();
        }
    }

}

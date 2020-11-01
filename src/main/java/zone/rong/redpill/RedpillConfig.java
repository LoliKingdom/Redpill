package zone.rong.redpill;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import static zone.rong.redpill.Redpill.MOD_ID;

@Mod.EventBusSubscriber
@Config(modid = MOD_ID)
public class RedpillConfig {

    @SubscribeEvent
    public static void onConfigChangedEvent(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (event.getModID().equals(MOD_ID)) {
            ConfigManager.sync(MOD_ID, Config.Type.INSTANCE);
        }
    }

    @Config.Comment("BlockState Counter")
    @Config.LangKey("config." + MOD_ID + ".block_state_counter")
    public static final BlockStateCounterCFG BLOCKSTATE_COUNTER = new BlockStateCounterCFG();

    public static final class BlockStateCounterCFG {

        @Config.Comment("Enable counting blockstates and printing of information into the log.")
        @Config.LangKey("config." + MOD_ID + ".block_state_counter.enable")
        public boolean enable = true;

        @Config.Comment("Specify the minimum of BlockStates each block has to have to print to log. This is to stop blocks with simple BlockStateContainers from being printed")
        @Config.LangKey("config." + MOD_ID + ".block_state_counter.stateLimit")
        public int stateLimit = 16;

        @Config.Comment("For each BlockState logged, calculate its approximate memory footprint. This only works with HotSpot VMs!")
        @Config.LangKey("config." + MOD_ID + ".block_state_counter.estimateMemory")
        public boolean estimateMemory = false;

    }

}
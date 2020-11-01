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

        @Config.Comment("Enable to stop those mods with no registered blocks to be logged")
        @Config.LangKey("config." + MOD_ID + ".block_state_counter.disregardZeros")
        public boolean disregardZeros = true;

        @Config.Comment("Specify the minimum of BlockStates each block has to have to be logged. This is to stop blocks with simple BlockStateContainers from being printed")
        @Config.LangKey("config." + MOD_ID + ".block_state_counter.stateLimit")
        public int stateLimit = 32;

        @Config.Comment("Enable to log names of IUnlistedProperty.")
        @Config.LangKey("config." + MOD_ID + ".block_state_counter.logUnlistedPropertyNames")
        public boolean logUnlistedPropertyNames = true;

        @Config.Comment("Specify the minimum of IUnlistedProperty each block has to have to be logged.")
        @Config.LangKey("config." + MOD_ID + ".block_state_counter.unlistedPropertiesLimit")
        public int unlistedPropertiesLimit = 1;

        @Config.Comment("Enable to calculate the logged BlockStates' approximate memory footprint. This only works on HotSpot VMs!")
        @Config.LangKey("config." + MOD_ID + ".block_state_counter.estimateMemory")
        public boolean estimateMemory = false;

    }

}

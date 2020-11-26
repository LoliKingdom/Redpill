package zone.rong.redpill.module;

import jdk.nashorn.internal.ir.debug.ObjectSizeCalculator;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.client.renderer.block.statemap.IStateMapper;
import net.minecraft.client.renderer.block.statemap.StateMap;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.registries.IRegistryDelegate;
import zone.rong.redpill.Redpill;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static zone.rong.redpill.RedpillConfig.BLOCKSTATE_COUNTER;

@SuppressWarnings({"unchecked", "ConstantConditions"})
public class BlockStateCounter {

    private static final Field stateMap$ignored;

    private static Map<IRegistryDelegate<Block>, IStateMapper> stateMappers;

    private static ObjectSizeCalculator calculator;

    static {
        Field ignored = null;
        try {
            Field customStateMappers = ModelLoader.class.getDeclaredField("customStateMappers"); // Non-obf
            customStateMappers.setAccessible(true);
            stateMappers = (Map<IRegistryDelegate<Block>, IStateMapper>) customStateMappers.get(null);
            ignored = ObfuscationReflectionHelper.findField(StateMap.class, "field_178140_d"); // Obf, ignored
            ignored.setAccessible(true);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        stateMap$ignored = ignored;
    }

    public static void count() {
        Loader.instance().getActiveModList().stream().map(ModContainer::getModId).forEach(id -> {
            final AtomicInteger modBlocks = new AtomicInteger(0), modNormalStates = new AtomicInteger(0);
            ForgeRegistries.BLOCKS.getKeys().stream().filter(r -> r.getResourceDomain().equals(id)).forEach(r -> {
                modBlocks.incrementAndGet();
                BlockStateContainer container = ForgeRegistries.BLOCKS.getValue(r).getBlockState();
                int normalStateSize = container.getValidStates().size();
                modNormalStates.addAndGet(normalStateSize);
                if (container instanceof IExtendedBlockState) {
                    int extendedStateCount = ((IExtendedBlockState) container).getUnlistedNames().size();
                    if (extendedStateCount >= BLOCKSTATE_COUNTER.unlistedPropertiesLimit) {
                        ResourceLocation blockName = container.getBlock().getRegistryName();
                        Redpill.LOGGER.warn("{} has {} unlisted properties.", blockName, extendedStateCount);
                        if (BLOCKSTATE_COUNTER.logUnlistedPropertyNames) {
                            Redpill.LOGGER.warn("Here are the following unlisted properties for {}:", blockName);
                            ((IExtendedBlockState) container).getUnlistedNames().forEach(p -> Redpill.LOGGER.warn(p.getName()));
                        }
                    }
                }
                if (container.getValidStates().size() > BLOCKSTATE_COUNTER.stateLimit) {
                    Redpill.LOGGER.warn("{} has {} blockstates", r, container.getValidStates().size());
                    if (BLOCKSTATE_COUNTER.estimateMemory) {
                        if (calculator == null) {
                            calculator = new ObjectSizeCalculator(ObjectSizeCalculator.getEffectiveMemoryLayoutSpecification());
                        }
                        Redpill.LOGGER.warn("That is approximately {} bytes taken up,", calculator.calculateObjectSize(container));
                    }
                }
            });
            if (modNormalStates.get() != 0 || !BLOCKSTATE_COUNTER.disregardZeros) {
                Redpill.LOGGER.fatal("{} has {} blockstates from {} total blocks.", id, modNormalStates.get(), modBlocks.get());
            }
        });
    }

}

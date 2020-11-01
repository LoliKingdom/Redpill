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

    private static final Field modelLoader$customStateMappers, stateMap$ignored;

    private static ObjectSizeCalculator calculator;

    static {
        Field customStateMappers = null;
        Field ignored = null;
        try {
            customStateMappers = ModelLoader.class.getDeclaredField("customStateMappers"); // Non-obf
            customStateMappers.setAccessible(true);
            ignored = ObfuscationReflectionHelper.findField(StateMap.class, "field_178140_d"); // Obf, ignored
            ignored.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        modelLoader$customStateMappers = customStateMappers;
        stateMap$ignored = ignored;
    }

    public static void count() throws Throwable {
        Loader.instance().getActiveModList().stream().map(ModContainer::getName).forEach(id -> {
            final AtomicInteger blocks = new AtomicInteger(0), normalStates = new AtomicInteger(0);
            ForgeRegistries.BLOCKS.getKeys().stream().filter(r -> r.getResourceDomain().equals(id)).forEach(r -> {
                blocks.incrementAndGet();
                BlockStateContainer container = ForgeRegistries.BLOCKS.getValue(r).getBlockState();
                int normalStateSize = container.getValidStates().size();
                normalStates.addAndGet(normalStateSize);
                if (container instanceof IExtendedBlockState) {
                    int extendedStateCount = ((IExtendedBlockState) container).getUnlistedNames().size();
                    if (extendedStateCount >= BLOCKSTATE_COUNTER.unlistedPropertiesLimit) {
                        ResourceLocation blockName = container.getBlock().getRegistryName();
                        Redpill.LOGGER.info("{} has {} unlisted properties.", blockName, extendedStateCount);
                        if (BLOCKSTATE_COUNTER.logUnlistedPropertyNames) {
                            Redpill.LOGGER.warn("Here are the following unlisted properties for {}:", blockName);
                            ((IExtendedBlockState) container).getUnlistedNames().forEach(p -> Redpill.LOGGER.info(p.getName()));
                        }
                    }
                }
            });
            if (normalStates.get() != 0 || !BLOCKSTATE_COUNTER.disregardZeros) {
                Redpill.LOGGER.info("{} has {} blockstates from {} total blocks.", id, normalStates.get(), blocks.get());
            }
        });
        Map<IRegistryDelegate<Block>, IStateMapper> map = (Map<IRegistryDelegate<Block>, IStateMapper>) modelLoader$customStateMappers.get(null);
        ForgeRegistries.BLOCKS.forEach(b -> {
            if (b.getBlockState().getValidStates().size() > BLOCKSTATE_COUNTER.stateLimit) {
                Redpill.LOGGER.warn("{} has {} blockstates", b.getRegistryName(), b.getBlockState().getValidStates().size());
                if (BLOCKSTATE_COUNTER.estimateMemory) {
                    if (calculator == null) {
                        calculator = new ObjectSizeCalculator(ObjectSizeCalculator.getEffectiveMemoryLayoutSpecification());
                    }
                    Redpill.LOGGER.warn("That is approximately {} bytes taken up,", calculator.calculateObjectSize(b.getBlockState()));
                }
                if (map.containsKey(b.delegate)) {
                    IStateMapper stateMapper = map.get(b.delegate);
                    if (stateMapper instanceof StateMap) {
                        try {
                            List<IProperty<?>> ignored = (List<IProperty<?>>) stateMap$ignored.get(stateMapper);
                            if (!ignored.isEmpty()) {
                                for (IProperty<?> property : ignored) {
                                    Redpill.LOGGER.warn("{} has {} ignored for model generation.", b.getRegistryName(), property);
                                }
                            }
                        } catch (Throwable throwable) {
                            throwable.printStackTrace();
                        }
                    }
                }
            }
        });
    }

}

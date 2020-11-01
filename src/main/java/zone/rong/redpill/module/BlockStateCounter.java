package zone.rong.redpill.module;

import jdk.nashorn.internal.ir.debug.ObjectSizeCalculator;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.client.renderer.block.statemap.IStateMapper;
import net.minecraft.client.renderer.block.statemap.StateMap;
import net.minecraftforge.client.model.ModelLoader;
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

public class BlockStateCounter {

    private static final Field modelLoader$customStateMappers, stateMap$ignored;

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
        Loader.instance().getActiveModList().stream().map(ModContainer::getModId).forEach(id -> {
            final AtomicInteger count = new AtomicInteger(0);
            ForgeRegistries.BLOCKS.getKeys().stream().filter(r -> r.getResourceDomain().equals(id)).forEach(r -> {
                ForgeRegistries.BLOCKS.getValue(r).getBlockState().getValidStates().forEach(s -> {
                    count.incrementAndGet();
                });
            });
            Redpill.LOGGER.info("{} has {} blockstates.", id, count.get());
        });
        Map<IRegistryDelegate<Block>, IStateMapper> map = (Map<IRegistryDelegate<Block>, IStateMapper>) modelLoader$customStateMappers.get(null);
        ForgeRegistries.BLOCKS.forEach(b -> {
            if (b.getBlockState().getValidStates().size() > BLOCKSTATE_COUNTER.stateLimit) {
                Redpill.LOGGER.warn("{} has {} blockstates", b.getRegistryName(), b.getBlockState().getValidStates().size());
                if (BLOCKSTATE_COUNTER.estimateMemory) {
                    Redpill.LOGGER.warn("That is approximately {} bytes taken up,", new ObjectSizeCalculator(ObjectSizeCalculator.getEffectiveMemoryLayoutSpecification()).calculateObjectSize(b.getBlockState()));
                }
                if (map.containsKey(b.delegate)) {
                    IStateMapper stateMapper = map.get(b.delegate);
                    if (stateMapper instanceof StateMap) {
                        try {
                            List<IProperty<?>> ignored = (List<IProperty<?>>) stateMap$ignored.get(stateMapper);
                            if (!ignored.isEmpty()) {
                                for (IProperty<?> property : ignored) {
                                    Redpill.LOGGER.error("{} has {} property disabled for model generation.", b.getRegistryName(), property);
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

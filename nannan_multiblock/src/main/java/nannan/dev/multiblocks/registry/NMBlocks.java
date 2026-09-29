package nannan.dev.multiblocks.registry;

import mekanism.common.registration.impl.BlockDeferredRegister;
import mekanism.common.registration.impl.BlockRegistryObject;
import nannan.dev.multiblocks.NannanMultiblocks;
import nannan.dev.multiblocks.phasechange.PhaseChangeBlock;
import nannan.dev.multiblocks.phasechange.PhaseChangeCasingTile;
import nannan.dev.multiblocks.phasechange.PhaseChangeControllerTile;
import nannan.dev.multiblocks.phasechange.PhaseChangeCoreTile;
import nannan.dev.multiblocks.phasechange.PhaseChangeValveTile;
import nannan.dev.multiblocks.phasechange.PhaseChangeValveBlock;
import net.minecraft.world.item.BlockItem;
import net.minecraftforge.eventbus.api.IEventBus;

/**
 * 方块注册。
 *
 * <p>用 Mekanism 的 {@link BlockDeferredRegister}（而不是原生 DeferredRegister），因为
 * {@code BlockBasicMultiblock} 与方块实体都需要 Mekanism 的 {@code IBlockProvider} 体系；
 * 属性先用 {@code properties -> properties} 原样返回，贴图/外观原型期直接复用裂变反应堆外壳模型
 * （见 {@code assets/nannan_multiblock/blockstates/}）。</p>
 */
public final class NMBlocks {

    public static final BlockDeferredRegister BLOCKS = new BlockDeferredRegister(NannanMultiblocks.MODID);

    public static final BlockRegistryObject<PhaseChangeBlock<PhaseChangeCasingTile>, BlockItem>
          CONTROL_FURNACE_CASING = BLOCKS.register("control_furnace_casing",
          () -> new PhaseChangeBlock<>(NMBlockTypes.CONTROL_FURNACE_CASING, properties -> properties));

    public static final BlockRegistryObject<PhaseChangeBlock<PhaseChangeControllerTile>, BlockItem>
          CONTROL_FURNACE_CONTROLLER = BLOCKS.register("control_furnace_controller",
          () -> new PhaseChangeBlock<>(NMBlockTypes.CONTROL_FURNACE_CONTROLLER, properties -> properties));

    /** 阀门用专门的方块类：它带 `mode` 方块状态属性，每个模式一套模型。 */
    public static final BlockRegistryObject<PhaseChangeValveBlock, BlockItem>
          CONTROL_FURNACE_VALVE = BLOCKS.register("control_furnace_valve",
          () -> new PhaseChangeValveBlock(NMBlockTypes.CONTROL_FURNACE_VALVE, properties -> properties));

    public static final BlockRegistryObject<PhaseChangeBlock<PhaseChangeCoreTile>, BlockItem>
          CONTROL_FURNACE_CORE = BLOCKS.register("control_furnace_core",
          () -> new PhaseChangeBlock<>(NMBlockTypes.CONTROL_FURNACE_CORE, properties -> properties));

    private NMBlocks() {
    }

    public static void register(IEventBus modBus) {
        BLOCKS.register(modBus);
    }
}

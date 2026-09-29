package nannan.dev.multiblocks.registry;

import mekanism.common.registration.impl.TileEntityTypeDeferredRegister;
import mekanism.common.registration.impl.TileEntityTypeRegistryObject;
import nannan.dev.multiblocks.NannanMultiblocks;
import nannan.dev.multiblocks.phasechange.PhaseChangeCasingTile;
import nannan.dev.multiblocks.phasechange.PhaseChangeControllerTile;
import nannan.dev.multiblocks.phasechange.PhaseChangeCoreTile;
import nannan.dev.multiblocks.phasechange.PhaseChangeValveTile;
import net.minecraftforge.eventbus.api.IEventBus;

/**
 * 方块实体类型注册。
 *
 * <p>这里必须用 Mekanism 的 {@link TileEntityTypeDeferredRegister}：它产出的
 * {@code TileEntityTypeRegistryObject} 才是 {@code BlockTypeTile} 需要的类型，
 * 而且 Mekanism 的 ticker/能力挂载都依赖这套注册对象。</p>
 */
public final class NMTiles {

    public static final TileEntityTypeDeferredRegister TILES =
          new TileEntityTypeDeferredRegister(NannanMultiblocks.MODID);

    public static final TileEntityTypeRegistryObject<PhaseChangeCasingTile> CONTROL_FURNACE_CASING =
          TILES.register(NMBlocks.CONTROL_FURNACE_CASING,
                (pos, state) -> new PhaseChangeCasingTile(NMBlocks.CONTROL_FURNACE_CASING, pos, state));

    public static final TileEntityTypeRegistryObject<PhaseChangeControllerTile> CONTROL_FURNACE_CONTROLLER =
          TILES.register(NMBlocks.CONTROL_FURNACE_CONTROLLER,
                (pos, state) -> new PhaseChangeControllerTile(NMBlocks.CONTROL_FURNACE_CONTROLLER, pos, state));

    public static final TileEntityTypeRegistryObject<PhaseChangeValveTile> CONTROL_FURNACE_VALVE =
          TILES.register(NMBlocks.CONTROL_FURNACE_VALVE,
                (pos, state) -> new PhaseChangeValveTile(NMBlocks.CONTROL_FURNACE_VALVE, pos, state));

    public static final TileEntityTypeRegistryObject<PhaseChangeCoreTile> CONTROL_FURNACE_CORE =
          TILES.register(NMBlocks.CONTROL_FURNACE_CORE,
                (pos, state) -> new PhaseChangeCoreTile(NMBlocks.CONTROL_FURNACE_CORE, pos, state));

    private NMTiles() {
    }

    public static void register(IEventBus modBus) {
        TILES.register(modBus);
    }
}

package nannan.dev.multiblocks.registry;

import mekanism.common.content.blocktype.BlockTypeTile;
import nannan.dev.multiblocks.phasechange.PhaseChangeCasingTile;
import nannan.dev.multiblocks.phasechange.PhaseChangeControllerTile;
import nannan.dev.multiblocks.phasechange.PhaseChangeCoreTile;
import nannan.dev.multiblocks.phasechange.PhaseChangeValveTile;

/**
 * 方块类型定义：Mekanism 用 {@code BlockType} 挂载属性、说明文本与方块实体类型。
 *
 * <p>{@code externalMultiblock()} 会加上 {@code AttributeMultiblock.EXTERNAL}，表示该方块参与多方块结构；
 * {@code createBlock} 的方块实体类型用 lambda 延迟取（{@code NMTiles} 此时还没初始化，
 * 只有真正需要时才求值，避免类初始化循环）。</p>
 */
public final class NMBlockTypes {

    public static final BlockTypeTile<PhaseChangeCasingTile> CONTROL_FURNACE_CASING = BlockTypeTile.BlockTileBuilder
          .createBlock(() -> NMTiles.CONTROL_FURNACE_CASING, NMLang.DESCRIPTION_CONTROL_FURNACE_CASING)
          .externalMultiblock()
          // 任意外壳 / 控制器 / 阀门右键都能打开 GUI（与 Mekanism 多方块一致：点哪块都一样）；
          // 第二个参数是自定义标题，不传的话标题会取 "container.<modid>.<方块名>" 这个键（实机反馈里的未翻译标题）。
          .withGui(() -> NMContainers.CONTROL_FURNACE, NMLang.CONTROL_FURNACE)
          .build();

    public static final BlockTypeTile<PhaseChangeControllerTile> CONTROL_FURNACE_CONTROLLER = BlockTypeTile.BlockTileBuilder
          .createBlock(() -> NMTiles.CONTROL_FURNACE_CONTROLLER, NMLang.DESCRIPTION_CONTROL_FURNACE_CONTROLLER)
          .externalMultiblock()
          .withGui(() -> NMContainers.CONTROL_FURNACE, NMLang.CONTROL_FURNACE)
          .build();

    /** 阀门：右键开 GUI，潜行右键循环阀门模式（见 PhaseChangeBlock）。 */
    public static final BlockTypeTile<PhaseChangeValveTile> CONTROL_FURNACE_VALVE = BlockTypeTile.BlockTileBuilder
          .createBlock(() -> NMTiles.CONTROL_FURNACE_VALVE, NMLang.DESCRIPTION_CONTROL_FURNACE_VALVE)
          .externalMultiblock()
          .withGui(() -> NMContainers.CONTROL_FURNACE, NMLang.CONTROL_FURNACE)
          .build();

    /** 内部核心：用 internalMultiblock()，它属于 INTERNAL 而不是结构外壳。 */
    public static final BlockTypeTile<PhaseChangeCoreTile> CONTROL_FURNACE_CORE = BlockTypeTile.BlockTileBuilder
          .createBlock(() -> NMTiles.CONTROL_FURNACE_CORE, NMLang.DESCRIPTION_CONTROL_FURNACE_CORE)
          .internalMultiblock()
          .build();

    private NMBlockTypes() {
    }
}

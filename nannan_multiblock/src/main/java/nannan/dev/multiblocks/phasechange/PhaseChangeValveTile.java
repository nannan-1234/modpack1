package nannan.dev.multiblocks.phasechange;

import mekanism.api.IContentsListener;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.providers.IBlockProvider;
import mekanism.common.capabilities.holder.chemical.IChemicalTankHolder;
import mekanism.common.capabilities.holder.fluid.IFluidTankHolder;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.tile.base.SubstanceType;
import nannan.dev.multiblocks.registry.NMBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/**
 * 相变控制炉阀门：把多方块数据里的水罐与冷却剂罐暴露给管道（与锅炉阀门同一套写法）。
 *
 * <p>一个方块承担进水/进钠/出蒸汽/出过热钠四种用途，具体由管道的侧面配置决定；
 * 罐的"只收水/只收 CooledCoolant 气体/只出蒸汽或过热钠"约束在
 * {@link PhaseChangeMultiblockData} 的罐定义里。</p>
 */
public class PhaseChangeValveTile extends PhaseChangePartTile {

    private static final String NBT_MODE = "valveMode";

    private ValveMode mode = ValveMode.INPUT_MATERIAL;

    public PhaseChangeValveTile(BlockPos pos, BlockState state) {
        this(NMBlocks.CONTROL_FURNACE_VALVE, pos, state);
    }

    public PhaseChangeValveTile(IBlockProvider blockProvider, BlockPos pos, BlockState state) {
        super(blockProvider, pos, state);
    }

    @Override
    public IFluidTankHolder getInitialFluidTanks(IContentsListener listener) {
        // 只有"输入冷却剂"模式才暴露水罐；其余模式对外没有流体能力。
        return side -> mode == ValveMode.INPUT_COOLANT
              ? getMultiblock().getCoolantInputFluidTanks()
              : List.of();
    }

    @Override
    public IChemicalTankHolder<Gas, GasStack, IGasTank> getInitialGasTanks(IContentsListener listener) {
        return side -> switch (mode) {
            case INPUT_FUEL -> getMultiblock().getFuelTanksOnly();
            case INPUT_COOLANT -> getMultiblock().getCoolantInputGasTanks();
            case OUTPUT_COOLANT -> getMultiblock().getCoolantProductTanks();
            default -> List.of();
        };
    }

    /** 物品槽按模式暴露：原料输入 / 产物输出。 */
    @Override
    public IInventorySlotHolder getInitialInventory(IContentsListener listener) {
        return side -> switch (mode) {
            case INPUT_MATERIAL -> getMultiblock().getMaterialInputSlots();
            case OUTPUT_PRODUCT -> getMultiblock().getProductOutputSlots();
            default -> List.of();
        };
    }

    public ValveMode getMode() {
        return mode;
    }

    public void setMode(ValveMode newMode) {
        if (mode != newMode) {
            mode = newMode;
            markForSave();
            invalidateCachedCapabilities();
            syncBlockState(newMode);
        }
    }

    /**
     * 把模式写进方块状态，让外观（每模式一套模型）跟着变。
     *
     * <p>方块状态是顺手写进去的：模式本身仍然以 NBT 为准（旧存档兼容），方块状态只负责渲染；
     * 只在"属性值与 NBT 不一致"时才调用 setBlock，避免无谓的方块更新。</p>
     */
    private void syncBlockState(ValveMode newMode) {
        // BlockEntity.getLevel() = m_58904_()、Level.isClientSide = f_46443_、getBlockPos() = m_58899_()。
        Level level = m_58904_();
        if (level == null || level.f_46443_) {
            return;
        }
        BlockState state = m_58900_();
        if (state.m_61138_(PhaseChangeValveBlock.MODE)
              && state.m_61143_(PhaseChangeValveBlock.MODE) != newMode) {
            // Level.setBlock(pos, state, flags) 的 SRG 名是 m_7731_；flags=3 就是原版 Block.UPDATE_ALL。
            level.m_7731_(m_58899_(), state.m_61124_(PhaseChangeValveBlock.MODE, newMode), 3);
        }
    }

    public ValveMode cycleMode() {
        setMode(mode.next());
        return mode;
    }

    @Override
    public void m_183515_(CompoundTag nbt) {
        super.m_183515_(nbt);
        nbt.m_128359_(NBT_MODE, mode.getName());   // CompoundTag.putString = m_128359_
    }

    @Override
    public void m_142466_(CompoundTag nbt) {
        super.m_142466_(nbt);
        if (nbt.m_128441_(NBT_MODE)) {             // CompoundTag.contains(String) = m_128441_
            mode = ValveMode.byName(nbt.m_128461_(NBT_MODE));   // getString = m_128461_
        }
    }

    /** 流体/气体内容由多方块数据持有，不由单个方块实体持久化（与锅炉阀门一致）。 */
    @Override
    public boolean persists(SubstanceType type) {
        return type != SubstanceType.FLUID && type != SubstanceType.GAS && super.persists(type);
    }
}

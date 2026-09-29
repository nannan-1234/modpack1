package nannan.dev.multiblocks.phasechange;

import mekanism.common.Mekanism;
import mekanism.common.lib.multiblock.MultiblockCache;
import mekanism.common.util.NBTUtils;
import net.minecraft.nbt.CompoundTag;

/**
 * 多方块内容缓存：结构被打散再重新成形时，用它把内容带回新结构。
 *
 * <p>父类 {@link MultiblockCache} 已经负责物品/流体/气体/热量这些"标准物质"的搬运
 * （它按 {@code CacheSubstance} 遍历数据里的容器列表），这里只补充本机自己的标量状态，
 * 并加上诊断日志：重新成形时把恢复后的罐量打进日志，便于确认内容到底有没有被带回来。</p>
 */
public class PhaseChangeCache extends MultiblockCache<PhaseChangeMultiblockData> {

    private static final String NBT_SCRAMMED = "scrammed";

    private boolean scrammed;
    private long lastLoggedWater = -1;
    private long lastLoggedGas = -1;

    @Override
    public void sync(PhaseChangeMultiblockData data) {
        super.sync(data);
        scrammed = data.isScrammed();
        long water = data.fluidCoolantTank.getFluidAmount();
        long gas = data.gasCoolantTank.getStored();
        if ((water != lastLoggedWater || gas != lastLoggedGas) && (water > 0 || gas > 0)) {
            lastLoggedWater = water;
            lastLoggedGas = gas;
            Mekanism.logger.info("[nannan_multiblock] cache sync: water={} mB, coolantGas={} mB, product={} mB, T={}K",
                  water, gas, data.heatedCoolantTank.getStored(), String.format("%.1f", data.getTemperature()));
        }
    }

    @Override
    public void apply(PhaseChangeMultiblockData data) {
        super.apply(data);
        data.restoreScrammed(scrammed);
        Mekanism.logger.info("[nannan_multiblock] cache apply: water={} mB, coolantGas={} mB, product={} mB, T={}K, scrammed={}",
              data.fluidCoolantTank.getFluidAmount(), data.gasCoolantTank.getStored(),
              data.heatedCoolantTank.getStored(), String.format("%.1f", data.getTemperature()), scrammed);
    }

    @Override
    public void merge(MultiblockCache<PhaseChangeMultiblockData> mergeCache, RejectContents rejectContents) {
        super.merge(mergeCache, rejectContents);
        if (mergeCache instanceof PhaseChangeCache other) {
            scrammed |= other.scrammed;
        }
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        // 读用 Mekanism 的 NBTUtils（内部处理缺键），写必须用 SRG 名 m_128379_ = CompoundTag.putBoolean。
        NBTUtils.setBooleanIfPresent(nbt, NBT_SCRAMMED, value -> scrammed = value);
    }

    @Override
    public void save(CompoundTag nbt) {
        super.save(nbt);
        nbt.m_128379_(NBT_SCRAMMED, scrammed);
    }
}

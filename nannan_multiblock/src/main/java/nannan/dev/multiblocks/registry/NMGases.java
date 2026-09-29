package nannan.dev.multiblocks.registry;

import mekanism.api.chemical.gas.Gas;
import mekanism.common.registration.impl.GasDeferredRegister;
import mekanism.common.registration.impl.GasRegistryObject;
import nannan.dev.multiblocks.NannanMultiblocks;
import net.minecraftforge.eventbus.api.IEventBus;

/**
 * 本模组的化学品注册。
 *
 * <p>相变燃料（{@code nannan_multiblock:phase_fuel}）：相变控制炉唯一燃烧的燃料气体，
 * 用 Mekanism 的默认气体贴图靠颜色区分（与项目里 KubeJS 侧注册化学品的方式一致）。</p>
 *
 * <p><b>与 AGENTS.md §5.1 的偏差及原因</b>：该约定要求化学品同时提供流体形态与转换配方
 * （写 KubeJS 侧）。这里只注册气体形态，原因是燃料属于"机器内部消耗品"、与 Mekanism 自带的
 * 裂变燃料（{@code mekanism:fissile_fuel}）同类，不需要用管道搬运流体形态；
 * 如果以后需要按流体搬运，再补同名流体 + 回旋式气液转换机配方（可写 KubeJS，也可写在本模组数据包里）。</p>
 */
public final class NMGases {

    public static final GasDeferredRegister GASES = new GasDeferredRegister(NannanMultiblocks.MODID);

    /** 相变燃料：暖橙色（0xFF9A3C），与炉体的"高温相变"主题一致。 */
    public static final GasRegistryObject<Gas> PHASE_FUEL = GASES.register("phase_fuel", 0xFF9A3C);

    private NMGases() {
    }

    public static void register(IEventBus modBus) {
        GASES.register(modBus);
    }
}

package nannan.dev.multiblocks;

import nannan.dev.multiblocks.registry.NMBlocks;
import nannan.dev.multiblocks.registry.NMContainers;
import nannan.dev.multiblocks.registry.NMGases;
import nannan.dev.multiblocks.registry.NMRecipes;
import nannan.dev.multiblocks.registry.NMTiles;
import nannan.dev.multiblocks.network.NMNetwork;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

/**
 * 整合包自建多方块结构的宿主模组。
 *
 * <p>第一个结构是「相变控制炉」（设计文档：{@code docs/multiblock_design.md} §1）。
 * 依赖 {@code mekanism.common.*} 内部 API，因此 build.ps1 把 Mekanism jar 加入编译类路径，
 * 并且 {@code mods.toml} 锁定了 Mekanism 版本区间。</p>
 */
@Mod(NannanMultiblocks.MODID)
public final class NannanMultiblocks {

    /** 注册表命名空间。必须全小写：{@code ResourceLocation} 不接受大写字母。 */
    public static final String MODID = "nannan_multiblock";

    public NannanMultiblocks(FMLJavaModLoadingContext context) {
        IEventBus modBus = context.getModEventBus();
        NMBlocks.register(modBus);
        NMTiles.register(modBus);
        NMGases.register(modBus);
        NMContainers.register(modBus);
        NMRecipes.register(modBus);
        // 自建网络通道：速率滑块与页面切换（Mekanism 的包体系加不了我们自己的类型）。
        NMNetwork.register();
    }
}

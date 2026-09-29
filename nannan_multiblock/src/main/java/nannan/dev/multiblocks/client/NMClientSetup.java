package nannan.dev.multiblocks.client;

import mekanism.client.ClientRegistrationUtil;
import nannan.dev.multiblocks.NannanMultiblocks;
import nannan.dev.multiblocks.registry.NMContainers;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

/**
 * 客户端注册：把两个容器类型各自绑到一个 Screen 上。
 *
 * <p>{@code value = Dist.CLIENT} 让 Forge 只在客户端加载并注册这个类，专用服务器不会碰到
 * 任何 {@code net.minecraft.client.*} 类（AGENTS.md §11）。</p>
 */
@Mod.EventBusSubscriber(modid = NannanMultiblocks.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class NMClientSetup {

    private NMClientSetup() {
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        ClientRegistrationUtil.registerScreen(NMContainers.CONTROL_FURNACE, GuiPhaseChangeFurnace::new);
        ClientRegistrationUtil.registerScreen(NMContainers.CONTROL_FURNACE_STATS, GuiPhaseChangeStats::new);
    }
}

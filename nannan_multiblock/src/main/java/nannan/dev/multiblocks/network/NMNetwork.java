package nannan.dev.multiblocks.network;

import java.util.function.Supplier;
import nannan.dev.multiblocks.NannanMultiblocks;
import nannan.dev.multiblocks.phasechange.PhaseChangeMultiblockData;
import nannan.dev.multiblocks.phasechange.PhaseChangePartTile;
import nannan.dev.multiblocks.registry.NMContainers;
import nannan.dev.multiblocks.registry.NMLang;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import mekanism.common.util.WorldUtils;

/**
 * 本模组自己的网络包（Mekanism 的包都是给它自己的机器用的，加不了新类型，所以自建一条通道）。
 *
 * <p>目前只有两个包，都是客户端 → 服务端：</p>
 * <ul>
 *   <li>{@link SetBurnRate}：速率滑块拖动时把 0..1 的比例发给服务端；</li>
 *   <li>{@link OpenPage}：点左侧"主页 / 统计"标签时请求服务端换一个容器（与裂变反应堆的
 *       {@code ClickedTileButton.TAB_STATS} 同思路，只是换成我们自己的容器类型）。</li>
 * </ul>
 * 两个包都只带方块坐标，服务端自己从坐标取方块实体，并对"距离"做一次基本校验。
 */
public final class NMNetwork {

    private static final String PROTOCOL = "1";
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
          new ResourceLocation(NannanMultiblocks.MODID, "main"),
          () -> PROTOCOL, PROTOCOL::equals, PROTOCOL::equals);
    /** 与方块交互的距离上限（平方）：原版方块交互约 6 格，稍微放宽一点。 */
    private static final double MAX_DISTANCE_SQR = 64.0;

    private NMNetwork() {
    }

    public static void register() {
        int id = 0;
        CHANNEL.messageBuilder(SetBurnRate.class, id++, NetworkDirection.PLAY_TO_SERVER)
              .encoder(SetBurnRate::encode)
              .decoder(SetBurnRate::decode)
              .consumerMainThread(SetBurnRate::handle)
              .add();
        CHANNEL.messageBuilder(OpenPage.class, id, NetworkDirection.PLAY_TO_SERVER)
              .encoder(OpenPage::encode)
              .decoder(OpenPage::decode)
              .consumerMainThread(OpenPage::handle)
              .add();
    }

    public static void setBurnRate(BlockPos pos, double ratio) {
        CHANNEL.sendToServer(new SetBurnRate(pos, ratio));
    }

    public static void openPage(BlockPos pos, int page) {
        CHANNEL.sendToServer(new OpenPage(pos, page));
    }

    /**
     * 服务端用坐标找回结构件方块实体；顺带校验玩家确实在附近（坐标是客户端给的，不能盲信）。
     * 用结构件基类而不是控制器类型：GUI 现在可以从外壳 / 阀门等任意结构件打开，包里给的坐标就是那一块。
     */
    private static PhaseChangePartTile resolve(ServerPlayer player, BlockPos pos) {
        if (player == null) {
            return null;
        }
        // Entity.level() 的 SRG 名是 m_9236_，Vec3i.getX/Y/Z 是 m_123341_/m_123342_/m_123343_。
        if (player.m_20275_(pos.m_123341_() + 0.5D, pos.m_123342_() + 0.5D, pos.m_123343_() + 0.5D)
              > MAX_DISTANCE_SQR) {
            return null;
        }
        return WorldUtils.getTileEntity(PhaseChangePartTile.class, player.m_9236_(), pos);
    }

    public static final class SetBurnRate {

        private final BlockPos pos;
        private final double ratio;

        SetBurnRate(BlockPos pos, double ratio) {
            this.pos = pos;
            this.ratio = ratio;
        }

        static void encode(SetBurnRate msg, FriendlyByteBuf buf) {
            // 原版成员的 SRG 名：FriendlyByteBuf.writeBlockPos = m_130064_、readBlockPos = m_130135_、
            // writeVarInt = m_130130_、readVarInt = m_130242_（writeDouble / readDouble 来自 netty 的 ByteBuf，名字不变）。
            buf.m_130064_(msg.pos);
            buf.writeDouble(msg.ratio);
        }

        static SetBurnRate decode(FriendlyByteBuf buf) {
            return new SetBurnRate(buf.m_130135_(), buf.readDouble());
        }

        static void handle(SetBurnRate msg, Supplier<NetworkEvent.Context> context) {
            NetworkEvent.Context ctx = context.get();
            ctx.enqueueWork(() -> {
                PhaseChangePartTile tile = resolve(ctx.getSender(), msg.pos);
                if (tile != null) {
                    PhaseChangeMultiblockData data = tile.getMultiblock();
                    data.setBurnRateLimit(msg.ratio);
                }
            });
            ctx.setPacketHandled(true);
        }
    }

    public static final class OpenPage {

        private final BlockPos pos;
        private final int page;

        OpenPage(BlockPos pos, int page) {
            this.pos = pos;
            this.page = page;
        }

        static void encode(OpenPage msg, FriendlyByteBuf buf) {
            buf.m_130064_(msg.pos);
            buf.m_130130_(msg.page);
        }

        static OpenPage decode(FriendlyByteBuf buf) {
            return new OpenPage(buf.m_130135_(), buf.m_130242_());
        }

        static void handle(OpenPage msg, Supplier<NetworkEvent.Context> context) {
            NetworkEvent.Context ctx = context.get();
            ctx.enqueueWork(() -> {
                PhaseChangePartTile tile = resolve(ctx.getSender(), msg.pos);
                if (tile != null) {
                    // 必须用 NetworkHooks.openScreen(player, provider, pos)：它会把方块坐标写进"额外数据"，
                    // 而 Mekanism 的 MekanismContainerType.getTileFromBuf 在客户端要靠这个坐标找回方块实体。
                    // 只调 Player.openMenu(provider) 的话客户端拿到的是空 buffer，直接
                    // IllegalArgumentException: Null packet buffer，表现为"点标签后 GUI 直接关掉"（实机反馈）。
                    if (msg.page == PAGE_STATS) {
                        NetworkHooks.openScreen(ctx.getSender(), NMContainers.CONTROL_FURNACE_STATS.getProvider(
                              NMLang.GUI_STATS_TITLE.translate(), tile), tile.m_58899_());
                    } else {
                        NetworkHooks.openScreen(ctx.getSender(), NMContainers.CONTROL_FURNACE.getProvider(
                              NMLang.CONTROL_FURNACE.translate(), tile), tile.m_58899_());
                    }
                }
            });
            ctx.setPacketHandled(true);
        }
    }

    public static final int PAGE_MAIN = 0;
    public static final int PAGE_STATS = 1;
}

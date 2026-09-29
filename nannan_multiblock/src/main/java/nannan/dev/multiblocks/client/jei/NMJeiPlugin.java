package nannan.dev.multiblocks.client.jei;

import java.util.List;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import nannan.dev.multiblocks.NannanMultiblocks;
import nannan.dev.multiblocks.phasechange.recipe.PhaseChangeRecipe;
import nannan.dev.multiblocks.registry.NMBlocks;
import nannan.dev.multiblocks.registry.NMRecipes;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

/**
 * JEI 集成：把 `phasechange:conversion` 配方展示成"输入 → 输出 + 温度区间"。
 *
 * <p>配方列表直接读客户端的 {@code RecipeManager}（客户端有同步过来的全量配方），所以不需要自己发数据。
 * {@code @JeiPlugin} 由 JEI 自己扫描 mod 类发现（只在客户端加载，专用服务器上这个类不会被碰）。</p>
 */
@JeiPlugin
public class NMJeiPlugin implements IModPlugin {

    /** JEI 侧的配方类型 id 用 modid（和配方 JSON 里的 `phasechange:conversion` 是两套命名，互不影响）。 */
    public static final RecipeType<PhaseChangeRecipe> CONVERSION =
          RecipeType.create(NannanMultiblocks.MODID, "conversion", PhaseChangeRecipe.class);

    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(NannanMultiblocks.MODID, "jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new PhaseChangeRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addRecipes(CONVERSION, allRecipes());
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        // 外壳与控制器都能点开，都登记成催化剂
        registration.addRecipeCatalyst(NMBlocks.CONTROL_FURNACE_CONTROLLER.getBlock(), CONVERSION);
        registration.addRecipeCatalyst(NMBlocks.CONTROL_FURNACE_CASING.getBlock(), CONVERSION);
    }

    private static List<PhaseChangeRecipe> allRecipes() {
        // Minecraft.getInstance() / Minecraft.level 都是原版成员，SRG 名分别是 m_91087_() / f_91073_。
        Level level = Minecraft.m_91087_().f_91073_;
        if (level == null) {
            return List.of();
        }
        return level.m_7465_().m_44013_(NMRecipes.CONVERSION_TYPE);
    }
}

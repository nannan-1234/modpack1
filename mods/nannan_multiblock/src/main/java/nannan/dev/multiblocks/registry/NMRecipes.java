package nannan.dev.multiblocks.registry;

import mekanism.common.Mekanism;
import nannan.dev.multiblocks.phasechange.recipe.PhaseChangeRecipe;
import nannan.dev.multiblocks.phasechange.recipe.PhaseChangeRecipeSerializer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;

/**
 * 配方类型与序列化器注册。
 *
 * <p>类型 id 按设计文档 §1.7 定为 **`phasechange:conversion`**（命名空间故意不是 modid，
 * 因为文档和用户都按这个名字在写配方；配方 JSON 文件本身仍然放在 `data/nannan_multiblock/recipes/` 下）。</p>
 *
 * <p>注册走 Forge 的 {@link RegisterEvent}（`ForgeRegistries.Keys.RECIPE_TYPES` / `RECIPE_SERIALIZERS`），
 * 而不是 `DeferredRegister`——后者的命名空间固定是 modid，拿不到 `phasechange:` 这个前缀。</p>
 */
public final class NMRecipes {

    public static final ResourceLocation CONVERSION_ID = new ResourceLocation("phasechange", "conversion");

    public static final RecipeType<PhaseChangeRecipe> CONVERSION_TYPE = new RecipeType<>() {
        @Override
        public String toString() {
            return CONVERSION_ID.toString();
        }
    };

    public static final RecipeSerializer<PhaseChangeRecipe> CONVERSION_SERIALIZER = new PhaseChangeRecipeSerializer();

    private NMRecipes() {
    }

    public static void register(IEventBus modBus) {
        modBus.addListener((RegisterEvent event) -> {
            event.register(ForgeRegistries.Keys.RECIPE_TYPES, CONVERSION_ID, () -> CONVERSION_TYPE);
            event.register(ForgeRegistries.Keys.RECIPE_SERIALIZERS, CONVERSION_ID, () -> CONVERSION_SERIALIZER);
            Mekanism.logger.info("[nannan_multiblock] recipe type registered: {}", CONVERSION_ID);
        });
    }
}

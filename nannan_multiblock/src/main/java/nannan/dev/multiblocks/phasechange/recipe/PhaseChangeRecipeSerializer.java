package nannan.dev.multiblocks.phasechange.recipe;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * 配方 JSON 的读写。
 *
 * <p>JSON 用 **Gson 自己的 API** 读（`JsonObject.get(...)`），不走 `GsonHelper`——后者是原版类，
 * 我们直接对 SRG 编译，方法名全被映射成 `m_xxxxx_`，用裸 Gson 反而更稳、也不用查表。
 * 唯一的例外是 `Ingredient`：它的 `fromJson` 是原版方法，SRG 名是 `m_43917_`。</p>
 *
 * <p>网络同步（`fromNetwork` / `toNetwork`）只需要带上机器真正用到的字段：输入、产物、窗口、通量、稳定数。</p>
 */
public class PhaseChangeRecipeSerializer implements RecipeSerializer<PhaseChangeRecipe> {

    /** 处理 1 个物品需要的默认反应通量（决策 7：1 锭 = 90 mB）。 */
    public static final int DEFAULT_MB_PER_ITEM = 90;
    /** 默认稳定 tick 数（决策 9）。 */
    public static final int DEFAULT_STABLE_TICKS = 20;

    private static double readDouble(JsonObject json, String key) {
        JsonElement element = json.get(key);
        if (element == null || !element.isJsonPrimitive()) {
            throw new JsonSyntaxException("配方缺少必需的数值字段 " + key);
        }
        return element.getAsDouble();
    }

    @Override
    public PhaseChangeRecipe m_6729_(ResourceLocation id, JsonObject json) {
        JsonElement inputElement = json.get("input");
        if (inputElement == null) {
            throw new JsonSyntaxException("配方缺少 input");
        }
        Ingredient input = Ingredient.m_43917_(inputElement);

        ItemStack output = ItemStack.f_41583_;   // ItemStack.EMPTY（SRG 名）
        JsonElement outputElement = json.get("output");
        if (outputElement != null && outputElement.isJsonObject()) {
            JsonObject outputObject = outputElement.getAsJsonObject();
            JsonElement itemElement = outputObject.get("item");
            if (itemElement == null) {
                throw new JsonSyntaxException("output 缺少 item");
            }
            // 用 Forge 的注册表查（Forge 类的方法名不受 SRG 映射影响；原版静态字段如 BuiltInRegistries.ITEM 会被映射）
            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemElement.getAsString()));
            if (item == null) {
                throw new JsonSyntaxException("output 里的物品不存在：" + itemElement.getAsString());
            }
            JsonElement countElement = outputObject.get("count");
            int count = countElement == null ? 1 : countElement.getAsInt();
            output = new ItemStack(item, Math.max(1, count));
        }

        double minTemperature = readDouble(json, "min_temperature");
        double maxTemperature = readDouble(json, "max_temperature");
        if (maxTemperature <= minTemperature) {
            throw new JsonSyntaxException("max_temperature 必须大于 min_temperature");
        }
        int mbPerItem = json.has("mb_per_item") ? Math.max(1, json.get("mb_per_item").getAsInt()) : DEFAULT_MB_PER_ITEM;
        int stableTicks = json.has("stable_ticks") ? Math.max(1, json.get("stable_ticks").getAsInt()) : DEFAULT_STABLE_TICKS;
        int priority = json.has("priority") ? json.get("priority").getAsInt() : 0;
        // heat_cost 目前是预留字段：写了也不会报错，但机器还不会用它。
        return new PhaseChangeRecipe(id, input, output, minTemperature, maxTemperature, mbPerItem, stableTicks, priority);
    }

    @Override
    public PhaseChangeRecipe m_8005_(ResourceLocation id, FriendlyByteBuf buffer) {
        Ingredient input = Ingredient.m_43940_(buffer);
        ItemStack output = buffer.m_130267_();
        double minTemperature = buffer.readDouble();
        double maxTemperature = buffer.readDouble();
        int mbPerItem = buffer.m_130242_();
        int stableTicks = buffer.m_130242_();
        int priority = buffer.m_130242_();
        return new PhaseChangeRecipe(id, input, output, minTemperature, maxTemperature, mbPerItem, stableTicks, priority);
    }

    @Override
    public void m_6178_(FriendlyByteBuf buffer, PhaseChangeRecipe recipe) {
        recipe.getInput().m_43923_(buffer);
        buffer.m_130055_(recipe.getOutput());
        buffer.writeDouble(recipe.getMinTemperature());
        buffer.writeDouble(recipe.getMaxTemperature());
        buffer.m_130130_(recipe.getMbPerItem());
        buffer.m_130130_(recipe.getStableTicks());
        buffer.m_130130_(recipe.getPriority());
    }
}

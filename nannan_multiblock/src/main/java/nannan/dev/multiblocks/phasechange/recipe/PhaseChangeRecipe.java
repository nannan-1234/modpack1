package nannan.dev.multiblocks.phasechange.recipe;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import nannan.dev.multiblocks.registry.NMRecipes;

/**
 * 相变控制炉的转换配方（决策 7 / §1.7）：`输入物品 + 温度窗口 → 输出物品`。
 *
 * <p>JSON 字段（`type` 固定为 `phasechange:conversion`）：</p>
 *
 * <pre>
 * input            : 原料（原版 Ingredient 写法：{"item": ...} / {"tag": ...}）
 * output           : 产物 {"item": ..., "count": n}；**可省略**，表示"只做热处理、不产出"
 * min_temperature  : 窗口下限（K，闭区间）
 * max_temperature  : 窗口上限（K，**开区间**）
 * mb_per_item      : 可选，处理 1 个物品需要多少 mB 的反应通量（默认 90，即"1 锭 = 90 mB"）
 * stable_ticks     : 可选，温度进入窗口后需要稳定的 tick 数（默认 20，决策 9）
 * priority         : 可选，多窗口重叠时的优先级（默认 0，大者优先；决策 16）
 * heat_cost        : 预留字段（吸热反应，暂未实现）
 * </pre>
 *
 * <p>窗口按**左闭右开**判定，所以 373.15–773 与 773–1500 这两条天然不重叠（决策 16 里用的例子就是它们）。
 * 多条配方同时命中时，机器按决策 16 的四层顺序选一条：priority → 输入更具体（单一物品 &gt; tag）
 * → 窗口更窄 → 配方 id 字典序。</p>
 */
public class PhaseChangeRecipe implements Recipe<Container> {

    private final ResourceLocation id;
    private final Ingredient input;
    private final ItemStack output;
    private final double minTemperature;
    private final double maxTemperature;
    private final int mbPerItem;
    private final int stableTicks;
    private final int priority;

    public PhaseChangeRecipe(ResourceLocation id, Ingredient input, ItemStack output, double minTemperature,
          double maxTemperature, int mbPerItem, int stableTicks, int priority) {
        this.id = id;
        this.input = input;
        this.output = output;
        this.minTemperature = minTemperature;
        this.maxTemperature = maxTemperature;
        this.mbPerItem = mbPerItem;
        this.stableTicks = stableTicks;
        this.priority = priority;
    }

    // ---- 相变控制炉自己用的判定 ----

    public boolean matchesInput(ItemStack stack) {
        return input.test(stack);
    }

    /** 温度窗口：左闭右开。 */
    public boolean matchesTemperature(double temperature) {
        return temperature >= minTemperature && temperature < maxTemperature;
    }

    public Ingredient getInput() {
        return input;
    }

    public ItemStack getOutput() {
        return output;
    }

    public double getMinTemperature() {
        return minTemperature;
    }

    public double getMaxTemperature() {
        return maxTemperature;
    }

    public double getWindowWidth() {
        return maxTemperature - minTemperature;
    }

    public int getMbPerItem() {
        return mbPerItem;
    }

    public int getStableTicks() {
        return stableTicks;
    }

    public int getPriority() {
        return priority;
    }

    /** 决策 16 的第二层：单一物品（具体）优先于 tag（通用）。 */
    public boolean isSpecificInput() {
        return input.m_43908_().length == 1;
    }

    // ---- Recipe 接口：机器的逻辑不在 matches/assemble 里（温度与通量都在多方块数据上），
    //      实现它们只是为了满足接口并让 JEI 能取到结果物品。 ----

    @Override
    public boolean m_5818_(Container container, Level level) {
        return false;
    }

    @Override
    public ItemStack m_5874_(Container container, RegistryAccess registryAccess) {
        return output.m_41777_();
    }

    @Override
    public boolean m_8004_(int width, int height) {
        return true;
    }

    @Override
    public ItemStack m_8043_(RegistryAccess registryAccess) {
        return output;
    }

    @Override
    public ResourceLocation m_6423_() {
        return id;
    }

    @Override
    public RecipeSerializer<?> m_7707_() {
        return NMRecipes.CONVERSION_SERIALIZER;
    }

    @Override
    public RecipeType<?> m_6671_() {
        return NMRecipes.CONVERSION_TYPE;
    }

    /** JEI 展示用：只做热处理的配方没有产物。 */
    public boolean hasOutput() {
        return !output.m_41619_();
    }

}

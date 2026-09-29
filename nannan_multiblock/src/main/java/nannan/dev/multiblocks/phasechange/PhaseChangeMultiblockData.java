package nannan.dev.multiblocks.phasechange;

import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.chemical.gas.attribute.GasAttributes;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.math.MathUtils;
import mekanism.common.Mekanism;
import mekanism.common.capabilities.chemical.multiblock.MultiblockChemicalTankBuilder;
import mekanism.common.capabilities.fluid.VariableCapacityFluidTank;
import mekanism.common.capabilities.heat.VariableHeatCapacitor;
import mekanism.common.inventory.container.sync.dynamic.ContainerSync;
import mekanism.common.inventory.slot.BasicInventorySlot;
import mekanism.common.inventory.slot.InputInventorySlot;
import mekanism.common.inventory.slot.OutputInventorySlot;
import mekanism.common.lib.multiblock.MultiblockCache;
import mekanism.common.lib.multiblock.MultiblockData;
import mekanism.common.lib.multiblock.MultiblockManager;
import mekanism.common.registries.MekanismGases;
import mekanism.common.tags.MekanismTags;
import mekanism.common.util.HeatUtils;
import nannan.dev.multiblocks.registry.NMGases;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;
import nannan.dev.multiblocks.phasechange.recipe.PhaseChangeRecipe;
import nannan.dev.multiblocks.registry.NMRecipes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/**
 * 相变控制炉的多方块数据本体。
 *
 * <p>里程碑③：温度骨架。热容 {@code C = 50 × 外壳方块数}、{@code T = heat / C}、
 * 极慢的环境散热（分母 20010）与 1500 K 强制停堆已经就位；燃料、两种冷却剂与温度带配方
 * 属于里程碑④⑤，公式见 {@code docs/multiblock_design.md} §1.5 与 {@code docs/kubejs_mekanism_notes.md} §6。</p>
 */
public class PhaseChangeMultiblockData extends MultiblockData {

    /** 每外壳方块提供的热容（冻结参数 v1）。 */
    public static final double CASING_HEAT_CAPACITY = 50.0;
    /** 强制停堆温度（决策 8）与自动复位温度（100 K 滞回，避免在阈值上反复启停）。 */
    public static final double MAX_TEMPERATURE = 1500.0;
    public static final double REARM_TEMPERATURE = 1400.0;
    /**
     * 反传导 / 反绝缘。决策 14 规定本机不主动与邻居换热（不调用 {@code simulateAdjacent}），
     * 这两个值只用于环境散热的分母——与裂变堆同样的 20010（10000 空气 + 10000 反绝缘 + 10 反传导）。
     */
    private static final double INVERSE_CONDUCTION = 10.0;
    private static final double INVERSE_INSULATION = 10000.0;
    private static final double ENVIRONMENT_DENOMINATOR = 10000.0 + INVERSE_INSULATION + INVERSE_CONDUCTION;

    /**
     * 冷却剂传导率（决策 15，冻结 v1）：水沿用裂变堆实测的 0.5；钠取 <b>1.35</b>，
     * 低于 Mekanism 自带的 5.0 —— 这样大机器满功率的钠冷平衡温度会逼近 773 K 分界，
     * "钠供应不足"就自然变成温度惩罚，而不是像 5.0 那样把温度钉死在 475 K。
     */
    public static final double WATER_CONDUCTIVITY = 0.5;
    public static final double SODIUM_CONDUCTIVITY = 1.35;
    /** 水罐容量：每方块 64000 mB；钠与产物罐每方块 1,000,000 mB（钠每 mB 只带 1 热，流量是水的 50 倍）。 */
    public static final int FLUID_TANK_PER_BLOCK = 64_000;
    public static final long COOLANT_TANK_PER_BLOCK = 1_000_000L;
    /** 燃料罐容量（每方块 mB）。 */
    public static final long FUEL_TANK_PER_BLOCK = 64_000L;
    /**
     * 每 mB 燃料释放的热量。取与 {@code heatPerMaterialMb}（冻结参数 500k）相同的值，
     * 于是"燃烧速率（mB/t）"与"反应通量（mB/t）"是同一个数——这就是决策 10 的单滑块设计。
     */
    public static final double HEAT_PER_FUEL_MB = 500_000.0;
    /** 发热上限的参数（A+B 决策 2）：每个外壳方块 2000 热/t，每个内部核心 10000 热/t。 */
    public static final double BASE_HEAT_PER_SHELL = 2_000.0;
    public static final double MAX_HEAT_PER_CORE = 10_000.0;
    /** 默认燃烧速率比例（0..1，占最大速率）；GUI 做好后由滑块接管。 */
    public static final double DEFAULT_BURN_RATE_LIMIT = 0.1;
    /** 决策 9："温度进入可用区间后还需稳定 20 tick 才启动配方"。配方系统未接入前，这个计数器先在 GUI 上显示。 */
    public static final int STABLE_TICKS_TARGET = 20;
    /** 临时用潜行右键切换的档位（GUI 之前的过渡手段）。 */
    private static final double[] BURN_RATE_STEPS = {0.0, 0.05, 0.1, 0.25, 0.5, 0.75, 1.0};

    /**
     * manager 名（同时也是 JEI/调试标识）。注册动作发生在构造 {@link MultiblockManager} 时：
     * 它的构造函数会把自身加入 Mekanism 的静态 manager 集合。
     */
    public static final String MANAGER_NAME = "phaseChangeFurnace";

    public static final MultiblockManager<PhaseChangeMultiblockData> manager =
          new MultiblockManager<>(MANAGER_NAME, PhaseChangeCache::new, PhaseChangeValidator::new);

    @ContainerSync
    public final VariableHeatCapacitor heatCapacitor;
    /** 水冷输入（只收水，且气体罐必须为空，避免水钠混用）。 */
    @ContainerSync
    public final VariableCapacityFluidTank fluidCoolantTank;
    /** 钠冷输入（只收带 {@code CooledCoolant} 属性的气体，且水罐必须为空）。 */
    @ContainerSync
    public final IGasTank gasCoolantTank;
    /** 产物输出：蒸汽（水冷）或过热钠（钠冷）。 */
    @ContainerSync
    public final IGasTank heatedCoolantTank;
    /** 燃料输入：只收本模组的相变燃料。 */
    @ContainerSync
    public final IGasTank fuelTank;
    /** 两个物品槽（GUI 会显示它们；阀门按模式分别暴露）。坐标是 GUI 相对坐标，容器直接取用。 */
    public final BasicInventorySlot materialInputSlot;
    public final BasicInventorySlot productOutputSlot;

    private double biomeAmbientTemp = 300.0;
    private int fluidCoolantCapacity = FLUID_TANK_PER_BLOCK;
    private long gasCoolantCapacity = COOLANT_TANK_PER_BLOCK;
    private long heatedCoolantCapacity = COOLANT_TANK_PER_BLOCK;
    private long fuelCapacity = FUEL_TANK_PER_BLOCK;
    /** 燃烧速率比例（0..1，占最大速率）与小数余量（让 <1 mB/t 的慢烧也能累积）。 */
    @ContainerSync
    public double burnRateLimit = DEFAULT_BURN_RATE_LIMIT;
    private double burnRemaining;
    /** 上一 tick 的环境散热量（正数=对外散热），GUI/JEI 用。 */
    @ContainerSync
    public double lastEnvironmentLoss;
    /** 上一 tick 的冷却剂处理量（mB/tick）。 */
    @ContainerSync
    public long lastBoilRate;
    /** 上一 tick 的目标燃烧速率（mB/tick，含小数；与裂变堆一样报告"意图"而不是整数消耗量）。 */
    @ContainerSync
    public double lastBurnRate;
    /** 最近 20 tick 的实测平均燃烧速率（mB/tick），用来确认燃料真的在烧。 */
    @ContainerSync
    public double averageBurnRate;
    private double burnedAccumulator;
    private int burnedTicks;
    @ContainerSync
    public boolean scrammed;
    /** 内部核心数量，由校验器在成形时统计（每个核心 +10000 热/t）。 */
    @ContainerSync
    public int coreCount;
    /**
     * 外壳方块数（热容的来源）。裂变堆的做法是把 GUI 需要的标量各自开一个 {@code @ContainerSync}
     * 字段（它的 {@code surfaceArea} 就是这样），因为客户端拿不到服务端的 {@code locations}。
     */
    @ContainerSync
    public int shellCount;
    /**
     * 当前温度（K）。每 tick 从热容量器抄一份到普通字段再同步：只依赖最普通的 double 同步通道，
     * 不依赖热容量器的特殊处理器，客户端 GUI 读到的值最稳。
     */
    @ContainerSync
    public double temperatureK = 300.0;
    /** 温度停留在可用区间（≥ 沸点）的连续 tick 数（决策 9 的"稳定 20 tick"，配方系统接入前的预演）。 */
    @ContainerSync
    public int stableTicks;
    /** 当前配方的稳定门槛（默认 20，配方可以用 stable_ticks 覆盖），GUI 显示 "x / y t" 用。 */
    @ContainerSync
    public int activeRecipeStableTicks = STABLE_TICKS_TARGET;
    /** 当前配方的产物（空表示"只做热处理、不产出"）；同步给客户端用于显示与 JEI 对照。 */
    @ContainerSync
    public ItemStack activeRecipeProduct = ItemStack.f_41583_;   // ItemStack.EMPTY（SRG 名，静态字段会被映射）
    /** 当前配方的温度窗口（K）与进度（0..1），GUI 的温度条 / 曲线要用。 */
    @ContainerSync
    public double activeRecipeMinTemp;
    @ContainerSync
    public double activeRecipeMaxTemp;
    @ContainerSync
    public double recipeProgress;
    /** 当前温度是否就在显示的那条配方窗口里（false = 界面显示的是"等待温度"的目标窗口）。 */
    @ContainerSync
    public boolean activeRecipeInWindow;
    /** 服务端记录"当前跑的是哪条配方"（换配方要清进度）；不需要同步给客户端。 */
    private ResourceLocation activeRecipeId;

    public PhaseChangeMultiblockData(BlockEntity tile) {
        super(tile);
        heatCapacitor = VariableHeatCapacitor.create(CASING_HEAT_CAPACITY,
              () -> INVERSE_CONDUCTION, () -> INVERSE_INSULATION, () -> biomeAmbientTemp, this);
        heatCapacitors.add(heatCapacitor);
        // 两个冷却剂罐互斥（与裂变堆一致）：水罐只在气体罐空时接受水，钠罐只在空水罐时接受 CooledCoolant 气体。
        // 注意：这两个罐互相引用，而 final 字段在构造函数里不能被子句直接引用（definite assignment），
        // 所以互斥判断走下面两个私有方法。
        fluidCoolantTank = VariableCapacityFluidTank.input(this, () -> fluidCoolantCapacity,
              stack -> MekanismTags.Fluids.WATER_LOOKUP.contains(stack.getFluid()) && isGasCoolantTankEmpty(), this);
        fluidTanks.add(fluidCoolantTank);
        gasCoolantTank = (IGasTank) MultiblockChemicalTankBuilder.GAS.input(this, () -> gasCoolantCapacity,
              gas -> gas.has(GasAttributes.CooledCoolant.class) && isFluidCoolantTankEmpty(), this);
        heatedCoolantTank = (IGasTank) MultiblockChemicalTankBuilder.GAS.output(this, () -> heatedCoolantCapacity,
              gas -> gas == MekanismGases.STEAM.getChemical() || gas.has(GasAttributes.HeatedCoolant.class), this);
        gasTanks.add(gasCoolantTank);
        gasTanks.add(heatedCoolantTank);
        fuelTank = (IGasTank) MultiblockChemicalTankBuilder.GAS.input(this, () -> fuelCapacity,
              gas -> gas == NMGases.PHASE_FUEL.getChemical(), this);
        gasTanks.add(fuelTank);
        // 输入槽：允许外部插入、禁止外部抽取；输出槽相反（Mekanism 的 slot 谓词约定）。
        // 坐标即 GUI 里的位置（InventoryContainerSlot 直接取槽位自带的 x/y），与设计稿一致。
        materialInputSlot = InputInventorySlot.at(this, 6, 86);
        productOutputSlot = OutputInventorySlot.at(this, 46, 86);
        inventorySlots.add(materialInputSlot);
        inventorySlots.add(productOutputSlot);
    }

    @Override
    public void onCreated(Level world) {
        super.onCreated(world);
        biomeAmbientTemp = calculateAverageAmbientTemperature(world);
        heatCapacitor.setHeatCapacity(CASING_HEAT_CAPACITY * locations.size(), true);
        scrammed = false;
        shellCount = locations.size();
        temperatureK = heatCapacitor.getTemperature();
        stableTicks = 0;
        Mekanism.logger.info("[nannan_multiblock] control_furnace formed: shellBlocks={}, heatCapacity={}, ambient={}K",
              locations.size(), heatCapacitor.getHeatCapacity(), biomeAmbientTemp);
    }

    @Override
    public boolean tick(Level world) {
        boolean needsUpdate = super.tick(world);
        // 顺序与裂变堆一致：先烧燃料发热，再处理冷却剂降温，最后做超温判定。
        burnFuel();
        lastEnvironmentLoss = simulateEnvironment();
        handleCoolant();
        handleOverheat();
        processRecipe(world);
        syncGuiState();
        updateHeatCapacitors(null);
        return needsUpdate;
    }

    /**
     * 把客户端 GUI 要显示的派生值抄进普通字段（它们带 {@code @ContainerSync}，由容器自动下发）。
     * 放在 {@code updateHeatCapacitors} 之前无所谓：这里读的是本 tick 已经结算过的温度。
     * （稳定 tick 数现在由 {@link #processRecipe} 按"当前配方窗口"维护。）
     */
    private void syncGuiState() {
        temperatureK = heatCapacitor.getTemperature();
        shellCount = locations.size();
    }

    /**
     * 相变转换：把输入槽里的物品按温度窗口转成输出槽里的产物（决策 7 / 9 / 10 / 13 / 16）。
     *
     * <ol>
     *   <li>按输入物品 + 当前温度筛配方；窗口**左闭右开**，所以相邻窗口天然不重叠；</li>
     *   <li>多条命中时按 priority → 输入更具体（单一物品 &gt; tag）→ 窗口更窄 → 配方 id 字典序 选一条；</li>
     *   <li>温度进入窗口后要连续稳定 {@code stable_ticks}（默认 20）才启动（决策 9）；</li>
     *   <li>推进速度 = 燃烧速率 / {@code mb_per_item}（默认 90 mB = 1 个物品），即决策 10 的"反应通量"；</li>
     *   <li>满 1 个物品时：先看输出槽能不能放下（放不下就暂停、**不吞材料**），再扣 1 个输入、保留小数余量；</li>
     *   <li>温度离开窗口 / 没烧燃料 / 已停堆：**暂停并保留进度**（决策 13）；换成别的配方或输入没有配方才清零。</li>
     * </ol>
     */
    private void processRecipe(Level world) {
        ItemStack input = materialInputSlot.getStack();
        if (input.m_41619_()) {
            resetRecipe();
            return;
        }
        double temperature = heatCapacitor.getTemperature();
        List<PhaseChangeRecipe> inputMatches = new ArrayList<>();
        List<PhaseChangeRecipe> bandMatches = new ArrayList<>();
        // Level.getRecipeManager() 的 SRG 名是 m_7465_，RecipeManager.getAllRecipesFor 是 m_44013_。
        for (PhaseChangeRecipe recipe : world.m_7465_().m_44013_(NMRecipes.CONVERSION_TYPE)) {
            if (recipe.matchesInput(input)) {
                inputMatches.add(recipe);
                if (recipe.matchesTemperature(temperature)) {
                    bandMatches.add(recipe);
                }
            }
        }
        if (inputMatches.isEmpty()) {
            resetRecipe();
            return;
        }
        if (bandMatches.isEmpty()) {
            // 输入有配方、但当前温度不在任何窗口里：暂停 + 保留进度（决策 13），
            // 界面显示"离当前温度最近"的那条窗口，玩家一眼能看出该升温还是降温。
            stableTicks = 0;
            PhaseChangeRecipe nearest = pickNearest(inputMatches, temperature);
            activeRecipeProduct = nearest.getOutput();
            activeRecipeMinTemp = nearest.getMinTemperature();
            activeRecipeMaxTemp = nearest.getMaxTemperature();
            activeRecipeStableTicks = nearest.getStableTicks();
            activeRecipeInWindow = false;
            return;
        }
        PhaseChangeRecipe recipe = pickRecipe(bandMatches);
        ResourceLocation recipeId = recipe.m_6423_();
        if (activeRecipeId == null || !activeRecipeId.equals(recipeId)) {
            // 换了配方（例如温度跨过 773 K 分界）：从头开始，避免用旧进度白拿产物
            activeRecipeId = recipeId;
            recipeProgress = 0;
            stableTicks = 0;
        }
        activeRecipeProduct = recipe.getOutput();
        activeRecipeMinTemp = recipe.getMinTemperature();
        activeRecipeMaxTemp = recipe.getMaxTemperature();
        activeRecipeStableTicks = recipe.getStableTicks();
        activeRecipeInWindow = true;
        if (stableTicks < recipe.getStableTicks()) {
            stableTicks++;
            return;
        }
        if (scrammed || lastBurnRate <= 0) {
            // 没在烧燃料（或已停堆）→ 反应通量为 0，进度原地等待
            return;
        }
        recipeProgress += lastBurnRate / recipe.getMbPerItem();
        if (recipeProgress < 1.0D) {
            return;
        }
        if (recipe.hasOutput()) {
            // 先模拟插入：放不下就停在"刚好满一件"，不吞输入材料
            ItemStack remainder = productOutputSlot.insertItem(recipe.getOutput().m_41777_(), Action.SIMULATE,
                  AutomationType.INTERNAL);
            if (!remainder.m_41619_()) {
                recipeProgress = 1.0D;
                return;
            }
            productOutputSlot.insertItem(recipe.getOutput().m_41777_(), Action.EXECUTE, AutomationType.INTERNAL);
        }
        materialInputSlot.extractItem(1, Action.EXECUTE, AutomationType.INTERNAL);
        recipeProgress -= 1.0D;
        markDirty();
    }

    /** 决策 16 的四层确定性顺序：priority → 输入更具体 → 窗口更窄 → 配方 id 字典序。 */
    private static PhaseChangeRecipe pickRecipe(List<PhaseChangeRecipe> candidates) {
        return candidates.stream().min((a, b) -> {
            if (a.getPriority() != b.getPriority()) {
                return Integer.compare(b.getPriority(), a.getPriority());
            }
            if (a.isSpecificInput() != b.isSpecificInput()) {
                return a.isSpecificInput() ? -1 : 1;
            }
            if (a.getWindowWidth() != b.getWindowWidth()) {
                return Double.compare(a.getWindowWidth(), b.getWindowWidth());
            }
            return a.m_6423_().compareTo(b.m_6423_());
        }).orElseThrow();
    }

    private void resetRecipe() {
        activeRecipeId = null;
        activeRecipeProduct = ItemStack.f_41583_;
        activeRecipeMinTemp = 0.0D;
        activeRecipeMaxTemp = 0.0D;
        activeRecipeStableTicks = STABLE_TICKS_TARGET;
        recipeProgress = 0.0D;
        stableTicks = 0;
        activeRecipeInWindow = false;
    }

    /** 温度不在窗口里时，挑离当前温度最近的一条（只用于界面显示，不影响进度与所选配方）。 */
    private static PhaseChangeRecipe pickNearest(List<PhaseChangeRecipe> candidates, double temperature) {
        return candidates.stream()
              .min(Comparator.comparingDouble(recipe -> distanceToWindow(recipe, temperature)))
              .orElseThrow();
    }

    private static double distanceToWindow(PhaseChangeRecipe recipe, double temperature) {
        if (temperature < recipe.getMinTemperature()) {
            return recipe.getMinTemperature() - temperature;
        }
        if (temperature >= recipe.getMaxTemperature()) {
            return temperature - recipe.getMaxTemperature();
        }
        return 0.0D;
    }

    /** 结构规模变化时同步罐容量（与裂变堆一样按体积换算）。 */
    @Override
    public void setVolume(int volume) {
        if (getVolume() != volume) {
            super.setVolume(volume);
            fluidCoolantCapacity = FLUID_TANK_PER_BLOCK * volume;
            gasCoolantCapacity = COOLANT_TANK_PER_BLOCK * volume;
            heatedCoolantCapacity = COOLANT_TANK_PER_BLOCK * volume;
            fuelCapacity = FUEL_TANK_PER_BLOCK * volume;
            // 校验器报告的 volume 就是外壳方块数（与裂变堆按体积换算罐容量一致），顺手同步给 GUI。
            shellCount = volume;
        }
    }

    /** 最大燃烧速率（mB/t）= 发热上限 / 每 mB 燃料热量；发热上限按 A+B 缩放。 */
    public double getMaxBurnRateMb() {
        return getMaxHeatPerTick() / HEAT_PER_FUEL_MB;
    }

    public int getCoreCount() {
        return coreCount;
    }

    // ---- 阀门按模式暴露的"局部容器"（每种只返回对应的那一个容器） ----

    public List<IInventorySlot> getMaterialInputSlots() {
        return List.of(materialInputSlot);
    }

    public List<IInventorySlot> getProductOutputSlots() {
        return List.of(productOutputSlot);
    }

    public List<IGasTank> getFuelTanksOnly() {
        return List.of(fuelTank);
    }

    public List<IExtendedFluidTank> getCoolantInputFluidTanks() {
        return List.of(fluidCoolantTank);
    }

    public List<IGasTank> getCoolantInputGasTanks() {
        return List.of(gasCoolantTank);
    }

    public List<IGasTank> getCoolantProductTanks() {
        return List.of(heatedCoolantTank);
    }

    public void setCoreCount(int cores) {
        coreCount = Math.max(0, cores);
    }

    /** 满功率发热上限（heat/t），读数与将来的 GUI 都用它。 */
    public double getMaxHeatPerTick() {
        return BASE_HEAT_PER_SHELL * shellCount + MAX_HEAT_PER_CORE * coreCount;
    }

    /**
     * 烧燃料（决策 10 的单滑块）：燃烧速率同时决定发热与反应通量。
     *
     * <p>与裂变堆 {@code burnFuel} 完全同构：<b>速率是小数</b>（例如 3³ 满速只有 0.224 mB/t），
     * 所以把"整数部分"写回燃料罐、小数部分留在 {@code burnRemaining} 里累加到下一 tick，
     * 而对外报告的 {@code lastBurnRate} 是<b>本 tick 的目标速率</b>（不是整数消耗量）——
     * 否则像 3³ 这种慢烧会在 4/5 的 tick 里显示成 0。</p>
     */
    private void burnFuel() {
        if (scrammed) {
            burnRemaining = 0;
            lastBurnRate = 0;
            return;
        }
        double available = fuelTank.getStored() + burnRemaining;
        double burnAmount = Math.min(getMaxBurnRateMb() * burnRateLimit, available);
        double leftover = available - burnAmount;
        // 整数量写回罐里，小数留到下一 tick 继续累积；没有燃料时也自然归零。
        fuelTank.setStackSize((long) leftover, Action.EXECUTE);
        burnRemaining = leftover % 1.0;
        if (burnAmount > 0) {
            heatCapacitor.handleHeat(burnAmount * HEAT_PER_FUEL_MB);
        }
        lastBurnRate = burnAmount;
        burnedAccumulator += burnAmount;
        if (++burnedTicks >= 20) {
            averageBurnRate = burnedAccumulator / burnedTicks;
            burnedAccumulator = 0;
            burnedTicks = 0;
        }
        markDirty();
    }

    public double getBurnRateLimit() {
        return burnRateLimit;
    }

    public void setBurnRateLimit(double limit) {
        burnRateLimit = Math.max(0.0, Math.min(1.0, limit));
        markDirty();
    }

    /** 潜行右键循环速率档位：GUI 之前用来测试燃烧速率的手段。 */
    public double cycleBurnRateLimit() {
        for (double step : BURN_RATE_STEPS) {
            if (step > burnRateLimit + 1.0E-6) {
                setBurnRateLimit(step);
                return burnRateLimit;
            }
        }
        setBurnRateLimit(BURN_RATE_STEPS[0]);
        return burnRateLimit;
    }

    private boolean isGasCoolantTankEmpty() {
        return gasCoolantTank.isEmpty();
    }

    private boolean isFluidCoolantTankEmpty() {
        return fluidCoolantTank.isEmpty();
    }

    /**
     * 冷却剂处理：公式与裂变堆完全一致（{@code docs/kubejs_mekanism_notes.md} §6.6），冷却效率固定 100%（决策 3）。
     *
     * <pre>
     * boilable = (T - 373.15) × C
     * 水：产汽量 = boilable × 0.5 × 0.2 / 10，扣热 = 产汽量 × 50      （1 mB 蒸汽 = 50 热）
     * 钠：转化量 = boilable × 1.35 / 焓值，扣热 = 转化量 × 焓值        （焓值取 Mekanism 的 1.0）
     * </pre>
     */
    private void handleCoolant() {
        double boilable = (heatCapacitor.getTemperature() - HeatUtils.BASE_BOIL_TEMP) * heatCapacitor.getHeatCapacity();
        if (boilable <= 0) {
            lastBoilRate = 0;
            return;
        }
        if (!fluidCoolantTank.isEmpty()) {
            double rate = boilable * WATER_CONDUCTIVITY * HeatUtils.getSteamEnergyEfficiency()
                  / HeatUtils.getWaterThermalEnthalpy();
            // 只受冷却剂存量限制：产物罐满时多余产物会被丢弃，但冷却必须继续（与裂变堆一致，
            // 否则产物没及时导出就会导致温度失控停堆，不符合逻辑）。
            long produced = clampCoolantHeated(rate, fluidCoolantTank.getFluidAmount());
            lastBoilRate = produced;
            if (produced > 0) {
                fluidCoolantTank.shrinkStack((int) produced, Action.EXECUTE);
                heatedCoolantTank.insert(MekanismGases.STEAM.getStack(produced), Action.EXECUTE, AutomationType.INTERNAL);
                heatCapacitor.handleHeat(-(produced * HeatUtils.getWaterThermalEnthalpy()
                      / HeatUtils.getSteamEnergyEfficiency()));
            }
        } else if (!gasCoolantTank.isEmpty()) {
            gasCoolantTank.getStack().ifAttributePresent(GasAttributes.CooledCoolant.class, coolant -> {
                double rate = boilable * SODIUM_CONDUCTIVITY / coolant.getThermalEnthalpy();
                // 同上：只按钠存量夹取，产物罐满不阻断冷却。
                long converted = clampCoolantHeated(rate, gasCoolantTank.getStored());
                lastBoilRate = converted;
                if (converted > 0) {
                    gasCoolantTank.shrinkStack(converted, Action.EXECUTE);
                    heatedCoolantTank.insert(coolant.getHeatedGas().getStack(converted), Action.EXECUTE,
                          AutomationType.INTERNAL);
                    heatCapacitor.handleHeat(-(converted * coolant.getThermalEnthalpy()));
                }
            });
        } else {
            lastBoilRate = 0;
        }
    }

    /** 冷却剂处理量取整并夹在 [0, 可用量]（与裂变堆的 clampCoolantHeated 同义）。 */
    private static long clampCoolantHeated(double amount, long available) {
        long clamped = MathUtils.clampToLong(amount);
        return Math.max(0, Math.min(clamped, available));
    }

    /**
     * 环境散热：每 tick 温差按 {@code T / (10000 + 反绝缘 + 反传导)} 流失，
     * 与裂变堆同配方（时间常数约 20010 tick，实际几乎不散热），保证"无冷却剂时温度会一路涨到停堆"。
     */
    @Override
    public double simulateEnvironment() {
        double capacity = heatCapacitor.getHeatCapacity();
        double tempToTransfer = (heatCapacitor.getTemperature() - biomeAmbientTemp) / ENVIRONMENT_DENOMINATOR;
        heatCapacitor.handleHeat(-tempToTransfer * capacity);
        return Math.max(tempToTransfer, 0);
    }

    /** 超过 1500 K 强制停堆并把热量钳在上限；降到 1400 K 以下自动复位（决策 8：不爆炸、不损伤）。 */
    private void handleOverheat() {
        double capacity = heatCapacitor.getHeatCapacity();
        double temperature = heatCapacitor.getTemperature();
        if (temperature >= MAX_TEMPERATURE) {
            heatCapacitor.setHeat(MAX_TEMPERATURE * capacity);
            if (!scrammed) {
                scrammed = true;
                markDirty();
                Mekanism.logger.warn("[nannan_multiblock] control_furnace overheat: {}K >= {}K, SCRAM",
                      String.format("%.1f", temperature), MAX_TEMPERATURE);
            }
        } else if (scrammed && temperature <= REARM_TEMPERATURE) {
            scrammed = false;
            markDirty();
            Mekanism.logger.info("[nannan_multiblock] control_furnace re-armed at {}K",
                  String.format("%.1f", temperature));
        }
    }

    public double getTemperature() {
        return heatCapacitor.getTemperature();
    }

    public boolean isScrammed() {
        return scrammed;
    }

    /** 由 {@link PhaseChangeCache} 在重新成形时恢复停堆状态。 */
    public void restoreScrammed(boolean value) {
        scrammed = value;
    }

    /**
     * 温度 → 比较器信号（0..15）。里程碑③还没有 GUI，先用比较器/红石灯把温度"读"出来：
     * 信号 = round(T / 1500 × 15)。
     */
    @Override
    protected int getMultiblockRedstoneLevel() {
        int level = (int) Math.round(getTemperature() / MAX_TEMPERATURE * 15.0);
        return Math.max(0, Math.min(15, level));
    }
}

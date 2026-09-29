// AdvancedAE 反应仓（advanced_ae:reaction_chamber）配方
//
// 来源：thermalRecipe.js 里全部 smelter（热力感应炉）配方，统一改写成反应仓版本。
// 规格：一次产出 32 个、每炉 100000 AE（= 200000 FE）、催化剂液体 100mb 水；
//       输入同样按 32 倍批次放大（原配方里 1 个 → 32 个，2 个 → 64 个，4 个 → 128 个）。
// 原感应炉配方保留不动，两种机器并存；若要改成只由反应仓产出，在 RecipesDelete.js 里
// 按 output 把对应的 smelter 配方 remove 掉即可。
//
// 输入数量与单槽上限：反应仓输入槽每个上限 64（AppEngInternalInventory(this, 9, 64)），
// 且匹配时要求某一条 input_items 在同一槽里凑够 amount，因此单条输入最多写 64。
// 4×32 = 128 的原料（紫颂果、冰）拆成两条 64 —— 注意 AdvancedAE 的匹配不会"占用"槽位，
// 所以两条同种原料只要有一个满 64 的槽就能匹配上，实际扣料也只扣 64，这是模组本身的限制。
//
// 字段含义与代码标准见 AGENTS.md §5.3，依据 AdvancedAE-1.3.6 的 ReactionChamberRecipeSerializer
// 与 ae2addonlib 的 IngredientStack：
//   output      : AE2 GenericStack 的 NBT 形式 { "#": 数量, "#c": "ae2:i", "id": 物品id }
//   input_items : [{ amount: 数量, ingredient: <Ingredient> }, ...]，全部列出的输入都必须凑齐
//   fluid       : { fluidStack: { FluidName, Amount } }，反应仓的流体是必填字段（催化剂）
//   energy      : 一次操作消耗的总能量，单位是 AE2 的 AE；本整合包 AE2 配置 PowerRatios.ForgeEnergy = 0.5，
//                 即 1 AE = 2 FE，所以 200000 FE 记作 100000
//
// 带 NBT 的输入（匠魂小刀条）用 forge:partial_nbt 表达，与脚本里的
// Item.of('tconstruct:small_blade', '{Material:"..."}').weakNBT() 等价
// （KubeJS 的 weakNBT 内部就是 Forge 的 PartialNBTIngredient）。
const reactionChamberEnergy = 100000; // 200000 FE
const reactionChamberOutputCount = 32;
const Water = { fluidStack: { FluidName: 'minecraft:water', Amount: 100 } };
const smallBladeIron = { type: 'forge:partial_nbt', item: 'tconstruct:small_blade', nbt: '{Material:"tconstruct:iron"}' };
const smallBladeSteel = { type: 'forge:partial_nbt', item: 'tconstruct:small_blade', nbt: '{Material:"tconstruct:steel"}' };
const ha_proudsoul = Amount => ({
    fluidStack: {FluidName: "kubejs:highly_active_proudsoul_reagent",Amount}});

// 反应仓输出的 GenericStack 形式：一次 32 个指定物品
const reactionOutput = itemId => ({ '#': reactionChamberOutputCount, '#c': 'ae2:i', id: itemId });

ServerEvents.recipes(event => {
    // 1. 铬钼钢粗胚：铬锭 + 钼锭 + 钢锭
    event.custom({
        type: 'advanced_ae:reaction',
        energy: reactionChamberEnergy,
        fluid: Water,
        input_items: [
            { amount: 32, ingredient: { item: 'kablade:chromium_ingot' } },
            { amount: 32, ingredient: { item: 'kablade:molybdenum_ingot' } },
            { amount: 32, ingredient: { tag: 'forge:ingots/steel' } }
        ],
        output: reactionOutput('kablade:crude_chromoly')
    });

    // 2. 未完成刀条 1：耀魂锭 + 铁小刀条
    event.custom({
        type: 'advanced_ae:reaction',
        energy: reactionChamberEnergy,
        fluid: Water,
        input_items: [
            { amount: 32, ingredient: { item: 'slashblade:proudsoul_ingot' } },
            { amount: 32, ingredient: smallBladeIron }
        ],
        output: reactionOutput('last_smith:blade_unfinished_1')
    });

    // 3. 未完成刀条 2：钢锭 + 未完成刀条 1
    event.custom({
        type: 'advanced_ae:reaction',
        energy: reactionChamberEnergy,
        fluid: Water,
        input_items: [
            { amount: 32, ingredient: { tag: 'forge:ingots/steel' } },
            { amount: 32, ingredient: { item: 'last_smith:blade_unfinished_1' } }
        ],
        output: reactionOutput('last_smith:blade_unfinished_2')
    });

    // 4. 魂樱钢锭：耀魂宝珠 + 魂樱 + 铬钼钢锭
    event.custom({
        type: 'advanced_ae:reaction',
        energy: reactionChamberEnergy,
        fluid: Water,
        input_items: [
            { amount: 32, ingredient: { item: 'slashblade:proudsoul_sphere' } },
            { amount: 32, ingredient: { item: 'last_smith:sakura_full' } },
            { amount: 32, ingredient: { item: 'kablade:chromoly_ingot' } }
        ],
        output: reactionOutput('last_smith:sakura_steel_ingot')
    });

    // 5. 魂樱刀条 1：耀魂宝珠 + 魂樱钢锭 + 钢小刀条
    event.custom({
        type: 'advanced_ae:reaction',
        energy: reactionChamberEnergy,
        fluid: Water,
        input_items: [
            { amount: 32, ingredient: { item: 'slashblade:proudsoul_sphere' } },
            { amount: 32, ingredient: { item: 'last_smith:sakura_steel_ingot' } },
            { amount: 32, ingredient: smallBladeSteel }
        ],
        output: reactionOutput('last_smith:blade_sakura_unfinished_1')
    });

    // 6. 魂樱刀条 2：魂樱刀条 1 + 魂樱钢锭 + 耀魂宝珠
    event.custom({
        type: 'advanced_ae:reaction',
        energy: reactionChamberEnergy,
        fluid: Water,
        input_items: [
            { amount: 32, ingredient: { item: 'last_smith:blade_sakura_unfinished_1' } },
            { amount: 32, ingredient: { item: 'last_smith:sakura_steel_ingot' } },
            { amount: 32, ingredient: { item: 'slashblade:proudsoul_sphere' } }
        ],
        output: reactionOutput('last_smith:blade_sakura_unfinished_2')
    });

    // 7. 烈焰合金：2 钢锭 + 烈焰棒
    event.custom({
        type: 'advanced_ae:reaction',
        energy: reactionChamberEnergy,
        fluid: Water,
        input_items: [
            { amount: 64, ingredient: { tag: 'forge:ingots/steel' } },
            { amount: 32, ingredient: { tag: 'forge:rods/blaze' } }
        ],
        output: reactionOutput('rainbowcompound:blazeite_ingot')
    });

    // 8. 紫颂合金：4 紫颂果 + 2 钢锭 + 末影珍珠粉
    event.custom({
        type: 'advanced_ae:reaction',
        energy: reactionChamberEnergy,
        fluid: Water,
        input_items: [
            // 4×32 = 128，单槽放不下，拆成两条 64
            { amount: 64, ingredient: { item: 'minecraft:chorus_fruit' } },
            { amount: 64, ingredient: { item: 'minecraft:chorus_fruit' } },
            { amount: 64, ingredient: { tag: 'forge:ingots/steel' } },
            { amount: 32, ingredient: { tag: 'forge:dusts/ender_pearl' } }
        ],
        output: reactionOutput('rainbowcompound:chorusite_ingot')
    });

    // 9. 粘性合金：2 粘液块 + 2 钢锭
    event.custom({
        type: 'advanced_ae:reaction',
        energy: reactionChamberEnergy,
        fluid: Water,
        input_items: [
            { amount: 64, ingredient: { item: 'minecraft:slime_block' } },
            { amount: 64, ingredient: { tag: 'forge:ingots/steel' } }
        ],
        output: reactionOutput('rainbowcompound:slimeite_ingot')
    });

    // 10. 萤石合金：2 强化萤石锭 + 2 金锭
    event.custom({
        type: 'advanced_ae:reaction',
        energy: reactionChamberEnergy,
        fluid: Water,
        input_items: [
            { amount: 64, ingredient: { item: 'mekanism:ingot_refined_glowstone' } },
            { amount: 64, ingredient: { tag: 'forge:ingots/gold' } }
        ],
        output: reactionOutput('rainbowcompound:glowstoneite_ingot')
    });

    // 11. 霜冻合金：4 冰 + 2 钢锭 + 暴雪粉
    event.custom({
        type: 'advanced_ae:reaction',
        energy: reactionChamberEnergy,
        fluid: Water,
        input_items: [
            // 4×32 = 128，单槽放不下，拆成两条 64
            { amount: 64, ingredient: { item: 'minecraft:ice' } },
            { amount: 64, ingredient: { item: 'minecraft:ice' } },
            { amount: 64, ingredient: { tag: 'forge:ingots/steel' } },
            { amount: 32, ingredient: { item: 'thermal:blizz_powder' } }
        ],
        output: reactionOutput('rainbowcompound:frostite_ingot')
    });

    // 12. 下界疣合金：2 下界砖 + 2 下界疣块 + 岩石粉
    event.custom({
        type: 'advanced_ae:reaction',
        energy: reactionChamberEnergy,
        fluid: Water,
        input_items: [
            { amount: 64, ingredient: { item: 'minecraft:nether_brick' } },
            { amount: 64, ingredient: { item: 'minecraft:nether_wart_block' } },
            { amount: 32, ingredient: { item: 'thermal:basalz_powder' } }
        ],
        output: reactionOutput('rainbowcompound:netherwartite_ingot')
    });

    // 13. 诡异疣合金：2 蘑菇 + 2 下界砖 + 狂风粉
    event.custom({
        type: 'advanced_ae:reaction',
        energy: reactionChamberEnergy,
        fluid: Water,
        input_items: [
            { amount: 64, ingredient: { tag: 'forge:mushrooms' } },
            { amount: 64, ingredient: { item: 'minecraft:nether_brick' } },
            { amount: 32, ingredient: { item: 'thermal:blitz_powder' } }
        ],
        output: reactionOutput('rainbowcompound:warpedite_ingot')
    });

    // 14. 彩虹化合物：可疑的彩色锭 + 耀魂宝珠 + 魂樱钢锭
    event.custom({
        type: 'advanced_ae:reaction',
        energy: reactionChamberEnergy,
        fluid: Water,
        input_items: [
            { amount: 32, ingredient: { item: 'rainbowcompound:strange_colored_ingot' } },
            { amount: 32, ingredient: { item: 'slashblade:proudsoul_sphere' } },
            { amount: 32, ingredient: { item: 'last_smith:sakura_steel_ingot' } }
        ],
        output: reactionOutput('rainbowcompound:rainbow_compound')
    });
        //15. 双足飞龙核心 龙芯+下界之星+陨钢锭
    reactionChamberOutputCount = 8;
    event.custom({
        type:"advanced_ae:reaction",
        energy:reactionChamberEnergy,
        fluid:ha_proudsoul("640"),
        input_items:[
            {amount:16,ingredient:{item:'draconicevolution:draconium_core'}},
            {amount:8,ingredient:{item:"minecraft:nether_star"}},
            {amount:24,ingredient:{item:'megacells:sky_steel_ingot'}},
        ],
        output: reactionOutput("draconicevolution:wyvern_core")
        
    });
    //16：陨石
    reactionChamberOutputCount = 64;
    event.custom({
        type:"advanced_ae:reaction",
        energy:reactionChamberEnergy,
        fluid:ha_proudsoul("1000"),
        input_items:[
            {amount:64,ingredient:{item:"minecraft:stone"}},
            {amount:64,ingredient:{item:'ae2:charged_certus_quartz_crystal'}},
        ],
        output:reactionOutput('ae2:sky_stone_block')
    });
});

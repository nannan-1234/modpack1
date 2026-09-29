// Mekanism 配方：吸附基体系列、精制耀魂，以及后续的复合酸/彩虹化合物链
//
// 流程：
//   聚丙烯片 + 石墨焦粉 --融合机--> 聚合物复合吸附基体
//   聚合物复合吸附基体 + 40mb 活化耀魂（灌注类型）--冶金灌注机--> 载耀魂吸附基体
//   载耀魂吸附基体 + 氯化氢 + 氧气 --加压反应室--> 精制耀魂 + 聚合物复合吸附基体
//   耀魂宝珠 --灌注类型转换--> 120mb 活化耀魂（灌注类型）
//   氯化氢 + 硫酸 --化学灌注器--> 复合酸混合物（气体）
//   活化彩钢板 + 40mb 复合酸混合物 + 40mb 精制耀魂 --加压反应室--> 彩虹化合物 + 高活性耀魂试剂
//   彩虹化合物 + 60mb 活化耀魂（灌注类型）--冶金灌注机--> 活化彩钢板
//   1 个 AE2 奇点 --灌注类型转换--> 1mb 奇点灌注液（灌注类型）
//   原子合金 + 150mb 奇点灌注液（灌注类型）--冶金灌注机--> 超致密锭
//   各化学品的气体 ⇄ 流体 --回旋式气液转换机--> 双向转换（AGENTS.md 5.1 的化学品配套约定）
//
// 配方类型与写法：
//   融合机（Combiner，mekanism:combining）：脚本 schema 可用，直接调用
//   冶金灌注机（Metallurgic Infuser，mekanism:metallurgic_infusing）：脚本 schema 可用，直接调用
//   化学灌注器（Chemical Infuser，mekanism:chemical_infusing）：脚本 schema 可用，直接调用
//   加压反应室（PRC，mekanism:reaction）、灌注类型转换（mekanism:infusion_conversion）、
//   回旋式气液转换机（mekanism:rotary）：wiki 列为不可用类型，改用 event.custom 写原始 JSON
//
// 化学品数量口径（详见 docs/kubejs_mekanism_notes.md 2.6 / 2.7）：
//   冶金灌注机与化学灌注器按配方里的实际数量消耗，不放大，所以这里写多少就是多少；
//   化学压射室会把数量按"每 tick"口径×200，所以本文件不使用压射室。
//
// 加压反应室的限制：一条配方只有一个气体槽（gasInput）和一个流体槽（fluidInput），
// 无法同时消耗两种气体；需要两种气体时，把其中一个改用流体形态走 fluidInput。
// 另外 PRC 的 itemInput / fluidInput / gasInput / duration 都是必填字段。
// 化学品字段的键名与取值范围见 docs/kubejs_mekanism_notes.md 第 2 节。
ServerEvents.recipes(event => {
    // 1. 融合机：1 个聚丙烯片 + 1 个石墨焦粉 → 1 个聚合物复合吸附基体
    //    第三个参数是额外输入（额外输入才带数量概念），这里默认 1 个
    event.recipes.mekanism.combining(
        'kubejs:polymer_adsorption_substrate',
        'kubejs:polypropylene',
        'immersivepetroleum:petcoke_dust'
    );

    // 2. 冶金灌注机：1 个聚合物复合吸附基体 + 40mb 活化耀魂（灌注类型） → 1 个载耀魂吸附基体
    //    灌注类型与气体、流体同 id（属于不同注册表），注册见 startup_scripts/mekanismChemicalRegister.js。
    //    冶金灌注机走 TwoInputCachedRecipe，数量按实际值消耗（无 ×200 倍率），所以这里就是 40mb/次。
    //    化学品写法 '40x <id>' 与 { infuse_type: '...', amount: 40 } 等价。
    event.recipes.mekanism.metallurgic_infusing(
        'kubejs:proudsoul_adsorption_substrate',
        'kubejs:polymer_adsorption_substrate',
        '40x kubejs:excited_proudsoul'
    );

    // 3. 加压反应室：1 个载耀魂吸附基体 + 200mb 氯化氢 + 100mb 氧气
    //    → 100mb 精制耀魂（气体）+ 1 个聚合物复合吸附基体
    //    基体在这一步循环使用：载耀魂基体被拆掉后还原成聚合物复合吸附基体，耀魂以精制耀魂形式产出
    //    duration 为一次操作的耗时（tick）；energyRequired 是"每 tick 额外耗能"（FE/t），
    //    会叠加在机器基础耗能之上（Mekanism 的 PRCEnergyContainer.getBaseEnergyPerTick）。
    //    这里 100 tick × 500 FE/t，可按平衡调整；Mekanism 自带 PRC 配方大多不写这个字段（即 0）
    event.custom({
        type: 'mekanism:reaction',
        duration: 100,
        energyRequired: 500,
        itemInput: { ingredient: { item: 'kubejs:proudsoul_adsorption_substrate' } },
        fluidInput: { amount: 40, fluid: 'mekanism:hydrogen_chloride' },
        gasInput: { amount: 20, gas: 'mekanism:oxygen' },
        itemOutput: { item: 'kubejs:polymer_adsorption_substrate' },
        gasOutput: { amount: 40, gas: 'kubejs:refined_proudsoul' }
    });

    // 4. 灌注类型转换：1 个耀魂宝珠 → 120mb 活化耀魂（灌注类型）
    //    mekanism:infusion_conversion 属 wiki 不可用类型，用 event.custom；
    //    输出字段是 output.{amount, infuse_type}
    event.custom({
        type: 'mekanism:infusion_conversion',
        input: { ingredient: { item: 'slashblade:proudsoul_sphere' } },
        output: { amount: 120, infuse_type: 'kubejs:excited_proudsoul' }
    });

    // 5. 化学灌注器：1mb 氯化氢 + 1mb 硫酸 → 1mb 复合酸混合物（气体）
    //    参数顺序为 (输出, 左输入, 右输入)；化学灌注器按实际数量消耗，无倍率
    event.recipes.mekanism.chemical_infusing(
        '1x kubejs:composite_acid_mixture',
        '1x mekanism:hydrogen_chloride',
        '1x mekanism:sulfuric_acid'
    );

    // 6. 加压反应室：1 个活化彩钢板 + 40mb 复合酸混合物（流体）+ 40mb 精制耀魂（气体）
    //    → 1 个彩虹化合物 + 40mb 高活性耀魂试剂（气体）
    //    duration / energyRequired 按平衡可调（energyRequired 是每 tick 额外耗能）
    event.custom({
        type: 'mekanism:reaction',
        duration: 200,
        energyRequired: 1000,
        itemInput: { ingredient: { item: 'tinkers_advanced:activated_chromatic_steel' } },
        fluidInput: { amount: 40, fluid: 'kubejs:composite_acid_mixture' },
        gasInput: { amount: 40, gas: 'kubejs:refined_proudsoul' },
        itemOutput: { item: 'rainbowcompound:rainbow_compound' },
        gasOutput: { amount: 40, gas: 'kubejs:highly_active_proudsoul_reagent' }
    });

    // 7. 冶金灌注机：1 个彩虹化合物 + 60mb 活化耀魂（灌注类型） → 1 个活化彩钢板
    event.recipes.mekanism.metallurgic_infusing(
        'tinkers_advanced:activated_chromatic_steel',
        'rainbowcompound:rainbow_compound',
        '60x kubejs:excited_proudsoul'
    );
    

    

    // 8. 回旋式气液转换机：活化耀魂（流体 ⇄ 气体）
    //    按 AGENTS.md 5.1，每个化学品都要有对应的流体形态与转换配方。
    //    fluidInput + gasOutput 是蒸发方向，gasInput + fluidOutput 是冷凝方向，四个字段都写才是双向。
    //    数量统一取 1，与 Mekanism 自带回旋式配方一致（吞吐由机器每 tick 的操作数决定）
    event.custom({
        type: 'mekanism:rotary',
        fluidInput: { amount: 1, fluid: 'kubejs:excited_proudsoul' },
        gasOutput: { amount: 1, gas: 'kubejs:excited_proudsoul' },
        gasInput: { amount: 1, gas: 'kubejs:excited_proudsoul' },
        fluidOutput: { amount: 1, fluid: 'kubejs:excited_proudsoul' }
    });

    // 9. 回旋式气液转换机：精制耀魂（流体 ⇄ 气体）
    event.custom({
        type: 'mekanism:rotary',
        fluidInput: { amount: 1, fluid: 'kubejs:refined_proudsoul' },
        gasOutput: { amount: 1, gas: 'kubejs:refined_proudsoul' },
        gasInput: { amount: 1, gas: 'kubejs:refined_proudsoul' },
        fluidOutput: { amount: 1, fluid: 'kubejs:refined_proudsoul' }
    });

    // 10. 回旋式气液转换机：复合酸混合物（流体 ⇄ 气体）
    event.custom({
        type: 'mekanism:rotary',
        fluidInput: { amount: 1, fluid: 'kubejs:composite_acid_mixture' },
        gasOutput: { amount: 1, gas: 'kubejs:composite_acid_mixture' },
        gasInput: { amount: 1, gas: 'kubejs:composite_acid_mixture' },
        fluidOutput: { amount: 1, fluid: 'kubejs:composite_acid_mixture' }
    });

    // 11. 回旋式气液转换机：高活性耀魂试剂（流体 ⇄ 气体）
    event.custom({
        type: 'mekanism:rotary',
        fluidInput: { amount: 1, fluid: 'kubejs:highly_active_proudsoul_reagent' },
        gasOutput: { amount: 1, gas: 'kubejs:highly_active_proudsoul_reagent' },
        gasInput: { amount: 1, gas: 'kubejs:highly_active_proudsoul_reagent' },
        fluidOutput: { amount: 1, fluid: 'kubejs:highly_active_proudsoul_reagent' }
    });

    // 12. 灌注类型转换：1 个 AE2 奇点 → 1mb 奇点灌注液（灌注类型）
    //     奇点由 AE2 物质凝聚器消耗 256000 个物品产出（config/ae2/common.json 的 Condenser.Singularity），
    //     换算关系为 1 奇点 = 1mb，即消耗 N mb 的配方需要 N 个奇点——这是刻意的高价值设定，
    //     规模化产出奇点由玩家自行解决，不作为平衡问题处理。
    event.custom({
        type: 'mekanism:infusion_conversion',
        input: { ingredient: { item: 'ae2:singularity' } },
        output: { amount: 1, infuse_type: 'kubejs:singularity_infusion' }
    });

    // 13. 冶金灌注机：1 个原子合金 + 150mb 奇点灌注液（灌注类型） → 1 个超致密锭
    event.recipes.mekanism.metallurgic_infusing(
        'tinkers_advanced:densium_ingot',
        'mekanism:alloy_atomic',
        '150x kubejs:singularity_infusion'
    );
});

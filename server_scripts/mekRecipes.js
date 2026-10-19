// Mekanism 配方，和自定义多方块结构配方。
///
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
    //14:磨粉机：石油焦粉
    event.recipes.mekanism.crushing("immersivepetroleum:petcoke_dust","immersivepetroleum:petcoke");
        //15:氧化机：精致碳
    event.custom({
        "type":"mekanism:oxidizing",
        // 注意：mekanism:carbon 只是灌注类型，没有同名物品；这里用真实存在的富集碳
        //（备选：minecraft:charcoal、#forge:dusts/coal）
        "input":{ingredient:{"item":"mekanism:enriched_carbon"}},
        "output":{amount:40,gas:"kubejs:carbon"}
    });
    //16:化学灌注器：精制碳 + 氢气 → 碳氢燃料（气体）
    // event.custom 写出的是原始 JSON，塞不了 KubeJS 的 "1x xxx" 字符串，
    // 这类配方要用脚本 schema（参数顺序：输出, 左输入, 右输入）
    event.recipes.mekanism.chemical_infusing(
        '1x kubejs:hydrocarbon_fuel',
        '1x kubejs:carbon',
        '1x mekanism:hydrogen'
    );
    
    // 17. 回旋式气液转换机：碳氢燃料（流体 ⇄ 气体）
    //     按 AGENTS.md 5.1，化学品要有同名流体形态与对应转换配方
    event.custom({
        type: 'mekanism:rotary',
        fluidInput: { amount: 1, fluid: 'kubejs:hydrocarbon_fuel' },
        gasOutput: { amount: 1, gas: 'kubejs:hydrocarbon_fuel' },
        gasInput: { amount: 1, gas: 'kubejs:hydrocarbon_fuel' },
        fluidOutput: { amount: 1, fluid: 'kubejs:hydrocarbon_fuel' }
    });
    //18.回旋式气液转换机：以太气体（流体 ⇄ 气体）
    event.custom({
        type: 'mekanism:rotary',
        fluidInput: { amount: 1, fluid: 'industrialforegoing:ether_gas' },
        gasOutput: { amount: 1, gas: 'kubejs:ether_gas' },
        gasInput: { amount: 1, gas: 'kubejs:ether_gas' },
        fluidOutput: { amount: 1, fluid: 'industrialforegoing:ether_gas' }
    });
    //19.化学灌注器：以太气体 + 碳氢燃料 → 以太碳氢燃料（气体）
    event.recipes.mekanism.chemical_infusing(
        '1x kubejs:etheral_hydrocarbon_fuel',
        '1x kubejs:ether_gas',
        '1x kubejs:hydrocarbon_fuel'
    );
    //20.加压反应器：以太碳氢燃料 +液态氯化氢 + 煤炭 → 含碳以太粉尘 +相变燃料
    event.custom({
        type:"mekanism:reaction",
        duration:200,
        energyRequired:1000,
        itemInput:{ingredient:{item:"minecraft:coal"}},
        fluidInput:{amount:40,fluid:"mekanism:hydrogen_chloride"},
        gasInput:{amount:40,gas:"kubejs:etheral_hydrocarbon_fuel"},
        itemOutput:{item:"kubejs:carbon_ether_dust"},
        gasOutput:{amount:40,gas:"nannan_multiblock:phase_fuel"}
    });
    //21.相变控制炉：超致密锭→硬相合金锭
    event.custom({
        type:"phasechange:conversion"
        ,input:{item:"tinkers_advanced:densium_ingot"}
        ,output:{item:"kubejs:durusium_ingot"},
        min_temperature:1000,
        max_temperature:1500,
    }).id("phasechange:densium_to_durusium");
    //22.相变控制炉 超致密→中相合金锭
    event.custom({
        type:"phasechange:conversion"
        ,input:{item:"tinkers_advanced:densium_ingot"}
        ,output:{item:"kubejs:moderatium_ingot"},
        min_temperature:773,
        max_temperature:1000,
    }).id("phasechange:densium_to_moderatium");
    //23.相变控制炉：超致密→韧相合金锭
    event.custom({
        type:"phasechange:conversion"
        ,input:{item:"tinkers_advanced:densium_ingot"}
        ,output:{item:"kubejs:lentusium_ingot"},
        min_temperature:373.15,
        max_temperature:773,
    }).id("phasechange:densium_to_lentussium");
    //24.相变控制炉：煤炭→石油焦
    event.custom({
        type:"phasechange:conversion"
        ,input:{item:"minecraft:coal"}
        ,output:{item:"immersivepetroleum:petcoke"},
        min_temperature:373.15,
        max_temperature:773,
    }).id("phasechange:coal_to_petcoke");
    //25.相变控制炉：煤炭→钻石
    event.custom({
        type:"phasechange:conversion"
        ,input:{item:"minecraft:coal"}
        ,output:{item:"minecraft:diamond"},
        min_temperature:773,
        max_temperature:1173,
    }).id("phasechange:coal_to_diamond");
    //26.相变控制炉：煤炭→神能晶
    event.custom({
        type:"phasechange:conversion"
        ,input:{item:"minecraft:coal"}
        ,output:{item:"aether:ambrosium_shard"},
        min_temperature:1173,
        max_temperature:1473,
    }).id("phasechange:ambrosium_shard");
    //27.福鲁伊克斯添加剂 化学溶解室
    // 参数顺序是 (输出, 气体输入, 物品输入)；原先第 2、3 个参数写反了（把物品当成气体、气体当成物品）
    // 注意：这里的"输出"用的是 BoxedChemicalOutput 组件（MekComponents$3），
    // 只接受映射写法 { gas/slurry: id, amount: n } 或原生 ChemicalStack，
    // 不接受 '1x id' 这种字符串简写（会报 Unable to cast ... to 'unknown'）。
    event.recipes.mekanism.dissolution(
        { gas: 'kubejs:fluix_adder', amount: 1 },
        '1x kubejs:highly_active_proudsoul_reagent',
        '1x ae2:fluix_crystal'
    );
    //28.萤耀通量合金 加压反应室
    event.custom({
        type:"mekanism:reaction",
        duration:200,
        energyRequired:1000,
        itemInput:{ingredient:{item:'tinkers_advanced:osgloglas_ingot'}},
        fluidInput:{amount:100,fluid:"minecraft:water"},
        gasInput:{amount:100,gas:"kubejs:fluix_adder"},
        itemOutput:{item:"kubejs:fluix_osgloglas_ingot"},
        gasOutput:{amount:40,gas:"kubejs:liquid_proudsoul"}
    })

    // ======== Thermal 耀魂配方的 Mek 版本 ========

    // 29–31. 回旋式：三个新气体与对应流体互转（活化耀魂早在第 8 条已有）
    event.custom({
        type: 'mekanism:rotary',
        fluidInput: { amount: 1, fluid: 'kubejs:liquid_proudsoul' },
        gasOutput: { amount: 1, gas: 'kubejs:liquid_proudsoul' },
        gasInput: { amount: 1, gas: 'kubejs:liquid_proudsoul' },
        fluidOutput: { amount: 1, fluid: 'kubejs:liquid_proudsoul' }
    });
    event.custom({
        type: 'mekanism:rotary',
        fluidInput: { amount: 1, fluid: 'kubejs:molten_chromoly' },
        gasOutput: { amount: 1, gas: 'kubejs:molten_chromoly' },
        gasInput: { amount: 1, gas: 'kubejs:molten_chromoly' },
        fluidOutput: { amount: 1, fluid: 'kubejs:molten_chromoly' }
    });
    // 跨命名空间：气体是我们注册的，流体是匠艺自带的
    event.custom({
        type: 'mekanism:rotary',
        fluidInput: { amount: 1, fluid: 'tinkers_advanced:molten_activated_chromatic_steel' },
        gasOutput: { amount: 1, gas: 'kubejs:molten_activated_chromatic_steel' },
        gasInput: { amount: 1, gas: 'kubejs:molten_activated_chromatic_steel' },
        fluidOutput: { amount: 1, fluid: 'tinkers_advanced:molten_activated_chromatic_steel' }
    });

    // 32–34. 氧化机：对应热力熔炼炉的两条液态耀魂配方与铬钼锭熔融
    //   耀魂碎片 → 100mb、破碎的耀魂 → 25mb、铬钼锭 → 90mb（数量与 thermalRecipe.js 一致）
    event.custom({
        type: 'mekanism:oxidizing',
        input: { ingredient: { item: 'slashblade:proudsoul' } },
        output: { amount: 100, gas: 'kubejs:liquid_proudsoul' }
    });
    event.custom({
        type: 'mekanism:oxidizing',
        input: { ingredient: { item: 'slashblade:proudsoul_tiny' } },
        output: { amount: 25, gas: 'kubejs:liquid_proudsoul' }
    });
    event.custom({
        type: 'mekanism:oxidizing',
        input: { ingredient: { item: 'kablade:chromoly_ingot' } },
        output: { amount: 90, gas: 'kubejs:molten_chromoly' }
    });

    // 35–36. 结晶器：对应热力冷却器的两条（气体 → 物品）
    //   90mb 熔融活化彩钢 → 活化彩钢板；360mb 熔融铬钼 → 铬钼钢球
    //   注意：结晶器没有模具概念，热力版用到的板模/球模在这里不消耗
    event.recipes.mekanism.crystallizing(
        'gas',
        'tinkers_advanced:activated_chromatic_steel',
        { gas: 'kubejs:molten_activated_chromatic_steel', amount: 90 }
    );
    event.recipes.mekanism.crystallizing(
        'gas',
        'kubejs:chromoly_steel_ball',
        { gas: 'kubejs:molten_chromoly', amount: 360 }
    );

    // 37–40. 加压反应室：thermalRecipe.js 里剩下的四条耀魂配方
    //   热力的酿造机/装瓶机是"流体+物品→流体/物品"，Mekanism 没有对应机器，
    //   按需求统一改成"物品 + 化学品(气体) → …"并补一个水槽走 PRC；
    //   数量与热力版一致，energyRequired 按热力版总耗能 ÷ duration 折算。

    // 37. 活化耀魂（对应热力酿造机）：铬钼锭 + 200mb 液态耀魂(气体) + 水 → 100mb 活化耀魂(气体)
    event.custom({
        type: 'mekanism:reaction',
        duration: 20,
        energyRequired: 200,          // 4000 FE / 20 tick
        itemInput: { ingredient: { item: 'kablade:chromoly_ingot' } },
        fluidInput: { amount: 100, fluid: 'minecraft:water' },
        gasInput: { amount: 200, gas: 'kubejs:liquid_proudsoul' },
        gasOutput: { amount: 100, gas: 'kubejs:excited_proudsoul' }
    });

    // 38. 耀魂宝珠（对应热力装瓶机）：铬钼钢球 + 300mb 活化耀魂(气体) + 水 → 1 耀魂宝珠
    event.custom({
        type: 'mekanism:reaction',
        duration: 100,
        energyRequired: 160,          // 16000 FE / 100 tick
        itemInput: { ingredient: { item: 'kubejs:chromoly_steel_ball' } },
        fluidInput: { amount: 100, fluid: 'minecraft:water' },
        gasInput: { amount: 300, gas: 'kubejs:excited_proudsoul' },
        itemOutput: { item: 'slashblade:proudsoul_sphere' }
    });

    // 39. 魂樱（对应热力装瓶机）：樱花原木 + 25mb 液态耀魂(气体) + 水 → 1 魂樱
    event.custom({
        type: 'mekanism:reaction',
        duration: 20,
        energyRequired: 50,           // 1000 FE / 20 tick
        itemInput: { ingredient: { item: 'minecraft:cherry_log' } },
        fluidInput: { amount: 100, fluid: 'minecraft:water' },
        gasInput: { amount: 25, gas: 'kubejs:liquid_proudsoul' },
        itemOutput: { item: 'last_smith:sakura' }
    });

    // 40. 熔融活化彩钢（对应热力酿造机）：彩虹化合物 + 100mb 活化耀魂(气体) + 水 → 90mb 熔融活化彩钢(气体)
    event.custom({
        type: 'mekanism:reaction',
        duration: 60,
        energyRequired: 667,          // 40000 FE / 60 tick
        itemInput: { ingredient: { item: 'rainbowcompound:rainbow_compound' } },
        fluidInput: { amount: 100, fluid: 'minecraft:water' },
        gasInput: { amount: 100, gas: 'kubejs:excited_proudsoul' },
        gasOutput: { amount: 90, gas: 'kubejs:molten_activated_chromatic_steel' }
    });
})


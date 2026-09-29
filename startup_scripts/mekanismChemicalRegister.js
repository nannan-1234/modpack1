// Mekanism 化学品注册（气体与灌注类型）。
//
// 说明：
// - 化学品属于注册表内容，只在 startup 阶段注册，新增或改名后必须重启游戏，/reload 不会生效。
// - 未调用 .texture(...) 时沿用 Mekanism 默认贴图（气体 mekanism:liquid/liquid、
//   灌注类型 mekanism:infuse_type/base），靠 .color(...) 染色，Mekanism 自带化学品也是这个做法。
// - 按 AGENTS.md 5.1：化学品要有同名流体形态；气体的互转用回旋式气液转换机（配方见 mekRecipes.js），
//   灌注类型没有回旋式转换，改用物品 → 灌注类型（mekanism:infusion_conversion）配方。
// - 写法与踩坑记录见 docs/kubejs_mekanism_notes.md 第 2、3 节。
StartupEvents.registry('mekanism:gas', event => {
    // 活化耀魂（气体形态）：与同名流体、灌注类型同色
    event.create('excited_proudsoul')
        .color(0xD8C5FF)

    // 精制耀魂（气体形态）：灌注链的产物，流体形态见 fluidRegister.js
    event.create('refined_proudsoul')
        .color(0xE29BFF)

    // 复合酸混合物（气体形态）：氯化氢 + 硫酸在化学灌注器中生成；
    // 下一步在加压反应室用的是它的流体形态
    event.create('composite_acid_mixture')
        .color(0xCFE86B)

    // 高活性耀魂试剂（气体形态）：加压反应室产出，流体形态见 fluidRegister.js
    event.create('highly_active_proudsoul_reagent')
        .color(0xD46BFF)

    // 奇点灌注液（气体形态）：颜色取自 AE2 奇点的深紫
    // 注意：按当前需求暂不注册流体形态，因此也暂不补回旋式转换配方
    //（AGENTS.md 5.1 的"特殊说明"例外；后续补流体时再一起加转换配方）
    event.create('singularity_infusion')
        .color(0x8B2FC9)
})

// 灌注类型：冶金灌注机使用（物品 + 灌注类型 → 物品，数量按实际值不放大），
// 来源是耀魂宝珠 →120mb，配方见 server_scripts/mekRecipes.js 第 4 条
StartupEvents.registry('mekanism:infuse_type', event => {
    event.create('excited_proudsoul')
        .color(0xD8C5FF)

    // 奇点灌注液（灌注类型形态）：来源是 AE2 奇点，配方见 server_scripts/mekRecipes.js 第 12 条
    event.create('singularity_infusion')
        .color(0x8B2FC9)
})

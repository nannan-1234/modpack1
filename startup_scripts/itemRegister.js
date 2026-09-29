StartupEvents.registry("item",event => {
    event.create("mineral_extractor")
        .displayName("矿脉提取器")
        .maxStackSize(1)
        .rarity('epic')
        .use((level, player, hand) => true)
})

// 工业精炼链的中间粉末产物
StartupEvents.registry("item", event => {
    // 采矿维度传送石：在主世界与采矿维度之间往返
    event.create("mining_dimension_teleporter")
        .displayName(Component.translatable("item.kubejs.mining_dimension_teleporter"))
        .maxStackSize(1)
        .rarity('epic')
        .texture("kubejs:item/mining_dimension_teleporter")

    event.create("sulfur_rich_ore_powder")
        .displayName(Component.translatable("item.kubejs.sulfur_rich_ore_powder"))
        .texture("kubejs:item/sulfur_rich_ore_powder")

    event.create("metal_rich_powder")
        .displayName(Component.translatable("item.kubejs.metal_rich_powder"))
        .texture("kubejs:item/metal_rich_powder")

    event.create("mineral_rich_powder")
        .displayName(Component.translatable("item.kubejs.mineral_rich_powder"))
        .texture("kubejs:item/mineral_rich_powder")

    event.create("crystal_rich_powder")
        .displayName(Component.translatable("item.kubejs.crystal_rich_powder"))
        .texture("kubejs:item/crystal_rich_powder")

    // 铬钼钢球
    event.create("chromoly_steel_ball")
        .displayName(Component.translatable("item.kubejs.chromoly_steel_ball"))
        .texture("kubejs:item/chromoly_steel_ball")

    // 三种塑料（片状）：材质基于 mekanism:hdpe_sheet 的形状，色调区分：聚乙烯近原样、聚丙烯偏暖乳白、PET 偏冷透蓝
    event.create("polyethylene")
        .displayName(Component.translatable("item.kubejs.polyethylene"))
        .texture("kubejs:item/polyethylene")

    event.create("polypropylene")
        .displayName(Component.translatable("item.kubejs.polypropylene"))
        .texture("kubejs:item/polypropylene")

    event.create("polyethylene_terephthalate")
        .displayName(Component.translatable("item.kubejs.polyethylene_terephthalate"))
        .texture("kubejs:item/polyethylene_terephthalate")

    // 吸附基体系列：聚丙烯片与石墨焦粉在融合机中压合而成。
    // 材质沿用工业先锋塑料片的等距层叠形状：上层为石墨焦粉黑（载耀魂形态换为活化耀魂色），
    // 下层保留聚丙烯片的暖白配色与层叠条纹。
    event.create("polymer_adsorption_substrate")
        .displayName(Component.translatable("item.kubejs.polymer_adsorption_substrate"))
        .texture("kubejs:item/polymer_adsorption_substrate")

    event.create("proudsoul_adsorption_substrate")
        .displayName(Component.translatable("item.kubejs.proudsoul_adsorption_substrate"))
        .texture("kubejs:item/proudsoul_adsorption_substrate")
})

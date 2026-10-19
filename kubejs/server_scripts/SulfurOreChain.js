// 工业精炼链：复合硫化矿 → 耀魂熔岩 + 四种粉末
// 1-3 步（搅拌机、IE 精炼厂、沉浸原油高压精炼装置）为数据包 JSON 配方：
//   data/kubejs/recipes/mixer/dirty_lava.json
//   data/kubejs/recipes/refinery/proudsoufied_dirty_lava.json
//   data/kubejs/recipes/hydrotreater/proudsoul_refining.json
// （kubejs-immersive-engineering 的 mixer/refinery schema 在 2001.5.29 中不可用，
//   且 IE 的 FluidTagInput 只接受 "tag" 字段，输入流体需通过 kubejs data 里的 fluid tag 指定）
// 4-7 步：离心分离机（热力膨胀）
ServerEvents.recipes(event => {
    // 4. 离心分离机：含硫富矿粉末 → 硫磺粉 + 富金属粉末 + 富矿物粉末 + 富晶体粉末
    event.recipes.thermal.centrifuge(
        [
            OutputItem.of('mekanism:dust_sulfur'),
            OutputItem.of('kubejs:metal_rich_powder'),
            OutputItem.of('kubejs:mineral_rich_powder'),
            OutputItem.of('kubejs:crystal_rich_powder')
        ],
        ['kubejs:sulfur_rich_ore_powder']
    );

    // 5. 富矿物粉末 → 红石粉*5 朱砂粉*2 赛特斯石英粉*5 硝石*2
    event.recipes.thermal.centrifuge(
        [
            OutputItem.of('minecraft:redstone', 5),
            OutputItem.of('thermal:cinnabar', 2),
            OutputItem.of('ae2:certus_quartz_dust', 5),
            OutputItem.of('thermal:niter', 2)
        ],
        ['kubejs:mineral_rich_powder']
    );

    // 6. 富金属粉末 → 铁粉*4 铜粉*4 锡粉*4 金粉*4
    event.recipes.thermal.centrifuge(
        [
            OutputItem.of('mekanism:dust_iron', 4),
            OutputItem.of('mekanism:dust_copper', 4),
            OutputItem.of('mekanism:dust_tin', 4),
            OutputItem.of('mekanism:dust_gold', 4)
        ],
        ['kubejs:metal_rich_powder']
    );

    // 7. 富晶体粉末 → 钻石粉*1 赛特斯石英*5 石英*5 萤石粉*8
    event.recipes.thermal.centrifuge(
        [
            OutputItem.of('mekanism:dust_diamond', 1),
            OutputItem.of('ae2:certus_quartz_crystal', 5),
            OutputItem.of('minecraft:quartz', 5),
            OutputItem.of('minecraft:glowstone_dust', 8)
        ],
        ['kubejs:crystal_rich_powder']
    );
});

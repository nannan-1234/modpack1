StartupEvents.registry("fluid", event => {
    event.create("molten_proudsoul")
    .color(0xFFB6C1).formattedDisplayName("fluid.kubejs.molten_proudsoul").thickTexture(0xFFB6C1)
    .bucketColor(0xFFB6C1).temperature(1500);
    event.create("molten_chromoly")
    .color(0xece4ff).formattedDisplayName("fluid.kubejs.molten_chromoly").thickTexture(0xece4ff)
    .bucketColor(0xece4ff).temperature(1500);
});
StartupEvents.registry("fluid",event => {
    //液态耀魂
    event.create("liquid_proudsoul")
    .color(0xF3C4FF).formattedDisplayName("fluid.kubejs.liquid_proudsoul").noBucket().thinTexture(0xF3C4FF);
    //活化耀魂
    event.create("excited_proudsoul")
    .color(0XD8C5FF).formattedDisplayName("fluid.kubejs.excited_proudsoul").noBucket().thinTexture(0xD8C5FF);
    //精制耀魂：载耀魂吸附基体在加压反应室中再生的产物，另有同名化学品（气体）形态
    event.create("refined_proudsoul")
    .color(0xE29BFF).formattedDisplayName("fluid.kubejs.refined_proudsoul").noBucket().thinTexture(0xE29BFF);
})
StartupEvents.registry("fluid", event => {
    // 复合酸混合物：氯化氢 + 硫酸在化学灌注器中生成的酸性混合物，
    // 在加压反应室中以流体形态参与反应
    event.create("composite_acid_mixture")
    .color(0xCFE86B).formattedDisplayName("fluid.kubejs.composite_acid_mixture").thinTexture(0xCFE86B).bucketColor(0xCFE86B);

    // 高活性耀魂试剂：加压反应室产物
    event.create("highly_active_proudsoul_reagent")
    .color(0xD46BFF).formattedDisplayName("fluid.kubejs.highly_active_proudsoul_reagent").noBucket().thinTexture(0xD46BFF);
})
StartupEvents.registry("fluid",event =>{
    event.create("dirty_lava")
    .color(0x8b041f).formattedDisplayName("fluid.kubejs.dirty_lava").thickTexture(0x8b041f).bucketColor(0x8b041f);

    event.create("proudsoufied_dirty_lava")
    .color(0xb90428).formattedDisplayName("fluid.kubejs.proudsoufied_dirty_lava").thickTexture(0xb90428).bucketColor(0xb90428);
    //耀魂熔岩
    event.create("proudsoufied_lava")
    .color(0xff94a9).formattedDisplayName("fluid.kubejs.proudsoufied_lava").thickTexture(0xff94a9).bucketColor(0xff94a9);
//复合硫化矿+1000mb熔岩 搅拌机→1000mb污浊的含矿硫化熔岩
//1000mb污浊的含矿硫化熔岩+90mb液态耀魂→精炼厂1000mb耀魂化的含矿硫化熔岩
//1000mb耀魂化的含矿硫化熔岩+500mb熔岩→高压精炼装置1500mb耀魂熔岩+含硫富矿粉末*1
//含硫富矿粉末 离心分离机→硫磺粉+富金属粉末+富矿物粉末 +富晶体粉末
//富矿物粉末 离心分离机 → 红石粉*5 朱砂粉*2 赛特斯石英粉*5 硝石*2
//富矿物粉末 离心分离机 → 铁粉*4 铜粉*4 锡粉*4 金粉*4
//富晶体粉末 离心分离机 → 钻石粉*1 赛特斯石英*5 石英*5  萤石粉*8
})
StartupEvents.registry("fluid", event => {
    // 丙烯：石油气在蒸馏塔中裂解的产物
    event.create("propylene")
    .color(0xFFFFFF).formattedDisplayName("fluid.kubejs.propylene").thinTexture(0xFFFFFF).bucketColor(0xFFFFFF);
});
StartupEvents.registry("fluid", event =>{
    //烯烃
    event.create("Alkene")
    .color(0xf7ffe4).formattedDisplayName("fluid.kubejs.Alkene").thinTexture(0xf7ffe4).bucketColor(0xf7ffe4);
})
StartupEvents.registry("fluid",event=>{
    //苯与二甲苯的混合物
    event.create("aromatic_hydrocarbons")
    .color(0xe4fff9).formattedDisplayName("fluid.kubejs.aromatic_hydrocarbons").thinTexture(0xe4fff9).bucketColor(0xf7ffe4);
    //二甲苯
    event.create("xylene")
    .color(0xf7ffe4).formattedDisplayName("fluid.kubejs.xylene").thinTexture(0xf7ffe4).bucketColor(0xf7ffe4);
})
StartupEvents.registry("fluid", event => {
    // 聚合物熔体：IE 精炼厂在活化彩钢催化下聚合生成（催化剂不消耗）
    event.create("polyethylene")
    .color(0xF6F2E4).formattedDisplayName("fluid.kubejs.polyethylene").thickTexture(0xF6F2E4).bucketColor(0xF6F2E4);

    event.create("polypropylene")
    .color(0xF4EAD0).formattedDisplayName("fluid.kubejs.polypropylene").thickTexture(0xF4EAD0).bucketColor(0xF4EAD0);

    event.create("polyethylene_terephthalate")
    .color(0xDCEAF0).formattedDisplayName("fluid.kubejs.polyethylene_terephthalate").thickTexture(0xDCEAF0).bucketColor(0xDCEAF0);

    // 对苯二甲酸：二甲苯在精炼厂催化氧化的产物，参与 PET 缩聚
    event.create("terephthalic_acid")
    .color(0xEFEAD8).formattedDisplayName("fluid.kubejs.terephthalic_acid").thickTexture(0xEFEAD8).bucketColor(0xEFEAD8);

    // 乙二醇：乙烯水合产物，参与 PET 缩聚
    event.create("ethylene_glycol")
    .color(0xDAE9F2).formattedDisplayName("fluid.kubejs.ethylene_glycol").thinTexture(0xDAE9F2).bucketColor(0xDAE9F2);
    event.create("bio")
    .color(0x24EBA5).formattedDisplayName("fluid.kubejs.bio").thinTexture(0x24EBA5).bucketColor(0xDAE9F2);
})

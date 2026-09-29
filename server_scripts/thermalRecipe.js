ServerEvents.recipes(event => {
    //铬钼钢粗胚
    event.recipes.thermal.smelter("kablade:crude_chromoly",['kablade:chromium_ingot','kablade:molybdenum_ingot','#forge:ingots/steel'])
    //未完成刀条
    event.recipes.thermal.chiller(Item.of('tconstruct:small_blade','{Material:"tconstruct:iron"}').weakNBT(),[Fluid.of("tconstruct:molten_iron",180),'tconstruct:small_blade_cast'])
    event.recipes.thermal.smelter("last_smith:blade_unfinished_1",["slashblade:proudsoul_ingot",Item.of('tconstruct:small_blade', '{Material:"tconstruct:iron"}').weakNBT()])
    event.recipes.thermal.smelter("last_smith:blade_unfinished_2",['#forge:ingots/steel',"last_smith:blade_unfinished_1"])
    event.recipes.thermal.press("last_smith:blade_unfinished_3",["last_smith:blade_unfinished_2"])
    event.recipes.thermal.bottler("last_smith:blade_unfinished_4",[Fluid.of("minecraft:water",200),"last_smith:blade_unfinished_3"])
    //液态耀魂
    event.recipes.thermal.crucible(Fluid.of("kubejs:liquid_proudsoul",100),"slashblade:proudsoul",20,20000);
    event.recipes.thermal.crucible(Fluid.of("kubejs:liquid_proudsoul",25),"slashblade:proudsoul_tiny",20,5000);
    //活化耀魂
    event.recipes.thermal.brewer([Fluid.of("kubejs:excited_proudsoul",100)],[Fluid.of("kubejs:liquid_proudsoul",200),'kablade:chromoly_ingot'],20,4000);
    //耀魂宝珠
    event.recipes.thermal.crucible(Fluid.of("kubejs:molten_chromoly", 90),"kablade:chromoly_ingot",20,40000);
    event.recipes.thermal.chiller('kubejs:chromoly_steel_ball',[Fluid.of("kubejs:molten_chromoly",360),'thermal:chiller_ball_cast'],0.0,4000);
    event.recipes.thermal.bottler(["slashblade:proudsoul_sphere"],[Fluid.of("kubejs:excited_proudsoul",300),'kubejs:chromoly_steel_ball'],100,16000)
    //裂化石脑油分解
    event.recipes.thermal.refinery([Fluid.of("kubejs:Alkene",100),Fluid.of("kubejs:aromatic_hydrocarbons",100)],["immersivepetroleum:petroleum_gas"],40,12000);
    event.recipes.thermal.refinery([Fluid.of("mekanism:ethene",50),Fluid.of("kubejs:propylene",50)],[Fluid.of("kubejs:Alkene",100)],40,4000);
    event.recipes.thermal.refinery([Fluid.of("kubejs:xylene",50),Fluid.of("immersivepetroleum:benzol",50)],[Fluid.of("kubejs:aromatic_hydrocarbons",100)],40,4000);
    //魂樱
    event.recipes.thermal.bottler(['last_smith:sakura'],[Fluid.of("kubejs:liquid_proudsoul",25),'minecraft:cherry_log'],20,1000)
    event.recipes.thermal.smelter(['last_smith:sakura_steel_ingot'],['slashblade:proudsoul_sphere','last_smith:sakura_full',"kablade:chromoly_ingot"],120,16000);
    //魂樱刀条
    event.recipes.thermal.chiller([Item.of('tconstruct:small_blade', '{Material:"tconstruct:steel"}').weakNBT()],[Fluid.of("tconstruct:molten_steel",180),'tconstruct:small_blade_cast'])
    event.recipes.thermal.smelter(['last_smith:blade_sakura_unfinished_1'],['slashblade:proudsoul_sphere','last_smith:sakura_steel_ingot',Item.of('tconstruct:small_blade', '{Material:"tconstruct:steel"}').weakNBT()],120,12000);
    event.recipes.thermal.smelter(['last_smith:blade_sakura_unfinished_2'],['last_smith:blade_sakura_unfinished_1','last_smith:sakura_steel_ingot','slashblade:proudsoul_sphere'],120,12000);
    event.recipes.thermal.press(['last_smith:blade_sakura_unfinished_3'],['last_smith:blade_sakura_unfinished_2'])
    event.recipes.thermal.bottler(["last_smith:blade_sakura_unfinished_4"],[Fluid.of("minecraft:water",200),"last_smith:blade_sakura_unfinished_3"],120,12000);

    //======== 彩虹化合物 8 合金：热力感应炉（经验20 / 电量8000） ========
    //烈焰合金：2钢锭 + 1烈焰棒
    event.recipes.thermal.smelter(['rainbowcompound:blazeite_ingot'],[Ingredient.of('#forge:ingots/steel').withCount(2),'#forge:rods/blaze'],20,8000);
    //紫颂合金：4紫颂果 + 2钢锭 + 1末影珍珠粉
    event.recipes.thermal.smelter(['rainbowcompound:chorusite_ingot'],[Item.of('minecraft:chorus_fruit',4),Ingredient.of('#forge:ingots/steel').withCount(2),'#forge:dusts/ender_pearl'],20,8000);
    //末影合金：4末影珍珠粉 + 2钢锭
    event.recipes.thermal.bottler(['rainbowcompound:enderite_ingot'],[Fluid.of("thermal:ender",500),Ingredient.of('#forge:ingots/steel')]);
    //粘性合金：2粘液块 + 2钢锭
    event.recipes.thermal.smelter(['rainbowcompound:slimeite_ingot'],[Item.of('minecraft:slime_block',2),Ingredient.of('#forge:ingots/steel').withCount(2)],20,8000);
    //萤石合金：2强化萤石锭 + 2金锭
    event.recipes.thermal.crucible([Fluid.of("tconstruct:molten_osmium",90)],['mekanism:ingot_osmium'])
    event.recipes.thermal.bottler(['mekanism:ingot_refined_glowstone'],[Fluid.of("tconstruct:molten_osmium",90),"minecraft:glowstone_dust"]);
    event.recipes.thermal.smelter(['rainbowcompound:glowstoneite_ingot'],[Item.of('mekanism:ingot_refined_glowstone',2),Ingredient.of('#forge:ingots/gold').withCount(2)],20,8000);
    //霜冻合金：4冰 + 2钢锭 + 1暴雪粉
    event.recipes.thermal.smelter(['rainbowcompound:frostite_ingot'],[Item.of('minecraft:ice',4),Ingredient.of('#forge:ingots/steel').withCount(2),'thermal:blizz_powder'],20,8000);
    //下界疣合金：2下界砖 + 2下界疣块 + 1岩石粉
    event.recipes.thermal.smelter(['rainbowcompound:netherwartite_ingot'],[Item.of('minecraft:nether_brick',2),Item.of('minecraft:nether_wart_block',2),'thermal:basalz_powder'],20,8000);
    //诡异疣合金：2蘑菇 + 2下界砖 + 1狂风粉
    event.recipes.thermal.smelter(['rainbowcompound:warpedite_ingot'],[Ingredient.of('#forge:mushrooms').withCount(2),Item.of('minecraft:nether_brick',2),'thermal:blitz_powder'],20,8000);
    //烈焰棒
    event.recipes.thermal.chiller(["minecraft:blze_rod"],['thermal:chiller_rod_cast',Fluid.of("tconstruct:blazing_blood",100)]);
    //熔融活化彩钢
    event.recipes.thermal.brewer([Fluid.of("tinkers_advanced:molten_activated_chromatic_steel",90)],[Fluid.of("kubejs:excited_proudsoul",100),"rainbowcompound:rainbow_compound"],60,40000);
    event.recipes.thermal.chiller(['tinkers_advanced:activated_chromatic_steel'],[Fluid.of("tinkers_advanced:molten_activated_chromatic_steel",90),])
    event.recipes.thermal.smelter(['rainbowcompound:rainbow_compound'],['rainbowcompound:strange_colored_ingot','slashblade:proudsoul_sphere','last_smith:sakura_steel_ingot'],60,80000)

    //熔融塑料 → 塑料片（板铸模，与 IE 精炼厂 250mb 批次对接）
    event.recipes.thermal.chiller("kubejs:polyethylene",[Fluid.of("kubejs:polyethylene",250),'tconstruct:plate_cast'],0.0,4000);
    event.recipes.thermal.chiller("kubejs:polypropylene",[Fluid.of("kubejs:polypropylene",250),'tconstruct:plate_cast'],0.0,4000);
    event.recipes.thermal.chiller("kubejs:polyethylene_terephthalate",[Fluid.of("kubejs:polyethylene_terephthalate",250),'tconstruct:plate_cast'],0.0,4000);
    //生物质→流体生物质
    event.recipes.thermal.crucible([Fluid.of("kubejs:bio",100)],['#forge:fuels/bio'],10,200);
})  

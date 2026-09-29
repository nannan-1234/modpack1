ServerEvents.recipes(event => {
    let steelIngots = Ingredient.of('#forge:ingots/steel').stacks;
    const {tconstruct} = event.recipes;
    tconstruct.melting(Fluid.of("kubejs:molten_proudsoul", 100), "slashblade:proudsoul",1500, 200
    );
    tconstruct.casting_table('slashblade:proudsoul_ingot',Fluid.of("kubejs:molten_proudsoul", 400),steelIngots,true,200);
    tconstruct.melting(Fluid.of("kubejs:molten_proudsoul",25), "slashblade:proudsoul_tiny",1500,50);
    //耀魂合金
    tconstruct.alloy(Fluid.of("kubejs:molten_proudsoul", 100),
    [Fluid.of("tconstruct:liquid_soul", 1000),
        Fluid.of("tconstruct:molten_diamond",45),
        Fluid.of("tconstruct:molten_amethyst", 200)],1500);
    //铬钼钢
    tconstruct.melting(Fluid.of("kubejs:molten_chromoly", 90), "kablade:chromoly_ingot",1500, 200);
    //铬钼钢球
    tconstruct.casting_table("kubejs:chromoly_steel_ball",Fluid.of("kubejs:molten_chromoly", 360),'thermal:chiller_ball_cast',false,200);


});

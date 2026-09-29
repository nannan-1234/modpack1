ServerEvents.recipes(event =>{
    event.recipes.reiryoku_expansion.hokora_fuel('earth', 'minecraft:apple', 10, 50);
    event.recipes.urushi.earth_element_tier1_crafting(
        ["slashblade:proudsoul","slashblade:proudsoul","slashblade:proudsoul",Ingredient.of('#forge:ingots/steel')]
        ,"slashblade:proudsoul_ingot",50);
    event.recipes.urushi.earth_element_tier2_crafting(
        ["slashblade:proudsoul","slashblade:proudsoul","slashblade:proudsoul",Ingredient.of('#forge:ingots/steel')]
        ,"slashblade:proudsoul_ingot",50);
    event.recipes.urushi.wood_element_tier2_crafting(
        ["slashblade:proudsoul_ingot","slashblade:proudsoul_ingot","urushi:wood_enhanced_jadeite_brick",Ingredient.of('#forge:ingots/steel')]
        ,"slashblade:proudsoul_sphere",150);
    

})

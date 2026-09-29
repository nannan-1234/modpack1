const incompleteblade = "last_smith:blade_unfinished_2";
const steelIngots = Ingredient.of('#forge:ingots/steel');

ServerEvents.recipes(event => {
    //铁刀刃序列组装
    const create = event.recipes.create;
    create.sequenced_assembly(
        
        'last_smith:blade_unfinished_4',
        'last_smith:blade_unfinished_1',
        [
            create.deploying(incompleteblade,[incompleteblade, steelIngots]),
            create.filling(incompleteblade, [incompleteblade,Fluid.of("tconstruct:blazing_blood", 200)]),
            create.pressing(incompleteblade, incompleteblade),
            create.filling(incompleteblade, [incompleteblade,Fluid.water(200)])
            
        ]
).loops(3).transitionalItem(incompleteblade);

    create.mechanical_crafting("avaritia:nether_crafting_table",
        [
            "AABAA",
            'CDEDC',
            'BFGFB',
            'CDEDC',
            'AABAA'
        ],{
            "A":"minecraft:nether_bricks",
            "B":"slashblade:proudsoul_ingot",
            "C":"create:brass_ingot",
            "D":"minecraft:blaze_rod",
            "E":"create:mechanical_crafter",
            "F":"create:sturdy_sheet",
            "G":"create:precision_mechanism"
        }
    )
});
/*ServerEvents.recipes(event =>
    event.recipes.minecraft.shaped("slashblade:slashblade",[
        "FEB",
        "DAG",
        "ACH"
    ],{
        "B":"last_smith:blade",
        "A":"slashblade:proudsoul_ingot",
        "C":"slashblade:slashblade_white",
        "D":"slashblade:slashblade_silverbamboo",
        "E":"botania:blaze_block",
        "F":"minecraft:iron_block",
        "G":"minecraft:lapis_block",
        "H":"minecraft:gold_block"
    }
        
    

)
)
*/
const num =1;
ServerEvents.recipes(event => {
    event.shapeless("last_smith:blade",[
        "last_smith:blade_unfinished_4","minecraft:gold_ingot"
    ]);
    event.shapeless("last_smith:blade_unfinished_1",[
        Item.of('tconstruct:small_blade','{Material:"tconstruct:iron"}').weakNBT(),"slashblade:proudsoul_ingot"
    ]);
    event.shapeless('last_smith:blade_sakura',[
        "last_smith:blade_sakura_unfinished_4","minecraft:gold_ingot"
    ]);
    event.shapeless("immersiveengineering:blastbrick_reinforced",[
        'immersiveengineering:blastbrick','#forge:plates/iron'
    ]);
    event.shapeless("mekanism:hdpe_sheet",[
        'mekanism:hdpe_pellet','mekanism:hdpe_pellet','mekanism:hdpe_pellet','mekanism:hdpe_pellet','mekanism:hdpe_pellet','mekanism:hdpe_pellet'
    ]);
    event.shaped("thermal:machine_frame",[
        "ABA",
        "BCB",
        "ABA"
    ],{
        "A":'#forge:ingots/steel',
        "B":'#forge:glass',
        "C":'#forge:gears/tin'
    })
    event.replaceInput({"input":"industrialforegoing:plastic"},"industrialforegoing:plastic","mekanism:hdpe_sheet")
})
ServerEvents.recipes(event =>{
    //可疑的彩色锭
    event.recipes.avaritia.shaped_table(2,'rainbowcompound:strange_colored_ingot',
        [
            "ABCDE",
            "F   G",
            "I H K",
            "Q   S",
            "LMNOP"
        ],
        {
            "A":"rainbowcompound:blazeite_ingot",
            "B":'rainbowcompound:enderite_ingot',
            "C":'rainbowcompound:slimeite_ingot',
            "D":'rainbowcompound:glowstoneite_ingot',
            "E":'rainbowcompound:frostite_ingot',
            "F":'slashblade:proudsoul_ingot',
            "G":'rainbowcompound:netherwartite_ingot',
            "H":'last_smith:sakura_steel_ingot',
            "I":'rainbowcompound:warpedite_ingot',
            "K":'#forge:ingots/rose_gold',
            "Q":'rainbowcompound:chorusite_ingot',
            "S":'#forge:ingots/bronze',
            "M":'tinkers_advanced:blizz_enderium',
            'L':'#forge:ingots/amethyst_bronze',
            "N":'tinkers_advanced:blitz_lumium',
            "O":'tinkers_advanced:basalz_signalum',
            "P":'kablade:chromoly_ingot'


        }

    );
    event.recipes.avaritia.shaped_table(2,"avaritia:end_crafting_table",
        [
            "ABCBA",
            "BDEDB",
            "CEFEC",
            "BDEDB",
            "ABCBA"
        ],
        {
            "A":'last_smith:sakura_steel_ingot',
            "B":"tinkers_advanced:blizz_enderium",
            "C":"mekanism:hdpe_sheet",
            "D":'rainbowcompound:chorusite_ingot',
            "E":"tinkers_advanced:activated_chromatic_steel",
            "F":'powah:ender_core'
        }
    );
    event.recipes.avaritia.shaped_table(2,"mekanism:steel_casing",
        [
            "ABBBA",
            "BCDEB",
            "BDFDB",
            "BGDHB",
            "ABBBA"
        ],{
            "A":"mekanism:hdpe_sheet",
            "B":'#forge:plates/steel',
            "C":'kubejs:polyethylene',
            "D":'mekanism:ingot_osmium',
            "E":'kubejs:polypropylene',
            "F":"#forge:storage_blocks/steel",
            "G":'immersiveengineering:plate_duroplast',
            "H":'kubejs:polyethylene_terephthalate'
        }
    )
    event.recipes.avaritia.shaped_table(2,"mekanism:steel_casing",
        [
            "ABBBA",
            "BCDEB",
            "BDFDB",
            "BGDHB",
            "ABBBA"
        ],{
            "A":"mekanism:hdpe_sheet",
            "B":'#forge:plates/steel',
            "C":'kubejs:polyethylene',
            "D":'mekanism:ingot_osmium',
            "E":'kubejs:polypropylene',
            "F":"thermal:machine_frame",
            "G":'immersiveengineering:plate_duroplast',
            "H":'kubejs:polyethylene_terephthalate'
        }
    )
})

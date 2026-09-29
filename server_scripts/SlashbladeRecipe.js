//const SlashBlade = "SlashBlade:slashblade";
/*
ServerEvents.recipes(event => {

    event.recipes.slashblade.slashblade_shaped_recipe("slashblade:slashblade", [
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
        , "slashblade:slashblade")
     })

*/
ServerEvents.recipes(event => {
    event.shaped(
        "slashblade:slashblade", // 输出的拔刀剑
        [
            "FEB",
            "DAG",
            "ACH"
        ],
        {
            "B": "last_smith:blade",
            "A": "slashblade:proudsoul_ingot",
            "C": "slashblade:slashblade_white",
            "D": "slashblade:slashblade_silverbamboo",
            "E": "botania:blaze_block",
            "F": "minecraft:iron_block",
            "G": "minecraft:lapis_block",
            "H": "minecraft:gold_block"
        }
    );
});
const RemoveOut=output =>(event.remove({"output":output}));
ServerEvents.recipes(event => {
    event.remove("slashblade:material/ingot");
    event.remove({"output": "last_smith:blade_unfinished_1"});
    event.remove({"output": "last_smith:blade_unfinished_2"});
    event.remove({"output": "last_smith:blade_unfinished_3"});
    event.remove({"output": "last_smith:blade_unfinished_4"});
    event.remove("last_smith:slashblade_from_bamboolight");
    event.remove("slashblade:slashblade_white");
    event.remove("last_smith:blade_step_5");
    event.remove("immersiveengineering:blastfurnace/steel");
    event.remove("immersiveengineering:blastfurnace/steel_block");
    event.remove({"output":"kablade:crude_chromoly"});
    event.remove({"output":"last_smith:sakura_steel_ingot"});
    event.remove({"output":"slashblade:proudsoul_sphere"});
    event.remove({'output':'last_smith:sakura'});
    event.remove({"output":"last_smith:blade_sakura_unfinished_1"});
    event.remove({"output":"last_smith:blade_sakura_unfinished_2"});
    event.remove({"output":"last_smith:blade_sakura_unfinished_3"});
    event.remove({"output":"last_smith:blade_sakura_unfinished_4"});
    event.remove({"output":"last_smith:blade_sakura"});
    event.remove({"output":"rainbowcompound:strange_colored_ingot"});
    event.remove({"output":"tinkers_advanced:molten_activated_chromatic_steel"});
    event.remove("tinkers_advanced:materials/activated_chromatic_steel/activated_chromatic_steel_alloy");
    event.remove({"output":'rainbowcompound:rainbow_compound'});
    event.remove({"output":"mekanism:steel_casing"});
    event.remove({"output":"thermal:machine_frame"});
    RemoveOut("draconicevolution:wyvern_core")
})
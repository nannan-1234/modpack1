
ServerEvents.recipes(event => {
    var RemoveOut=output =>(event.remove({"output":output}));
    var Remove= id =>event.remove(id);
    Remove("slashblade:material/ingot");
    RemoveOut("last_smith:blade_unfinished_1");
    RemoveOut("last_smith:blade_unfinished_2");
    RemoveOut("last_smith:blade_unfinished_3");
    RemoveOut("last_smith:blade_unfinished_4");
    RemoveOut("last_smith:slashblade_from_bamboolight");
    RemoveOut("slashblade:slashblade_white");
    RemoveOut("last_smith:blade_step_5");
    Remove("immersiveengineering:blastfurnace/steel");
    Remove("immersiveengineering:blastfurnace/steel_block");
    RemoveOut("kablade:crude_chromoly");
    RemoveOut("last_smith:sakura_steel_ingot");
    RemoveOut("slashblade:proudsoul_sphere");
    RemoveOut("last_smith:sakura");
    RemoveOut("last_smith:blade_sakura_unfinished_1");
    RemoveOut("last_smith:blade_sakura_unfinished_2");
    RemoveOut("last_smith:blade_sakura_unfinished_3");
    RemoveOut("last_smith:blade_sakura_unfinished_4");
    RemoveOut("last_smith:blade_sakura");
    RemoveOut("rainbowcompound:strange_colored_ingot");
    RemoveOut("tinkers_advanced:molten_activated_chromatic_steel");
    Remove("tinkers_advanced:materials/activated_chromatic_steel/activated_chromatic_steel_alloy");
    RemoveOut("rainbowcompound:rainbow_compound");
    RemoveOut("mekanism:steel_casing");
    RemoveOut("thermal:machine_frame");
    RemoveOut("draconicevolution:wyvern_core")
})
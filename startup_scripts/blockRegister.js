StartupEvents.registry("block",event =>{
    event.create("polymetallic_complex_ore")
    .requiresTool(true)
    .tagBlock("minecraft:mineable/pickaxe")
    .hardness(50)
    .formattedDisplayName(Component.translatable("block.kubejs.polymetallic_complex_ore"))
    .requiresTool(true)
})

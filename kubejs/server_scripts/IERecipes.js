//沉浸工程的配方。
//一些沉浸工程的配方由数据包实现，在data/immersiveengineering/recipes下，主要是一些机器的配方和一些合金的配方。
//沉浸工程的许多配方只支持tag输入，需要自己用kubejs或者数据包注册tag。
ServerEvents.recipes(event =>{
    event.recipes.immersiveengineering.blast_furnace('immersiveengineering:ingot_steel',"minecraft:iron_ingot","immersiveengineering:slag",200);
    event.recipes.immersiveengineering.blast_furnace("immersiveengineering:storage_steel","minecraft:iron_block",Item.of("immersiveengineering:slag",1800),);
    
}
)


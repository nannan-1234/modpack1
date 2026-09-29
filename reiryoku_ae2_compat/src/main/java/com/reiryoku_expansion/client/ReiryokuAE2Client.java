package com.reiryoku_expansion.client;

import com.reiryoku_expansion.ae2.ReiryokuKey;
import com.reiryoku_expansion.ae2.ReiryokuKeyType;

import appeng.api.client.AEKeyRendering;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

/**
 * Client-side registration. The key render handler must exist before any ME
 * screen tries to draw / describe a Reiryoku entry.
 */
public final class ReiryokuAE2Client {

    private ReiryokuAE2Client() {
    }

    public static void init() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener((FMLClientSetupEvent event) -> event.enqueueWork(() ->
                AEKeyRendering.register(ReiryokuKeyType.INSTANCE, ReiryokuKey.class, new ReiryokuKeyRenderer())));
    }
}

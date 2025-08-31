package com.komikan.betternitwit.client;

import com.komikan.betternitwit.entity.ModEntities;
import com.komikan.betternitwit.entity.custom.BetterNitwitRenderer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

public class ClientEventBusSubscriber {

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.BETTER_NITWIT.get(), BetterNitwitRenderer::new);
    }
}
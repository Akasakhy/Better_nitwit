package com.komikan.betternitwit.client;

import com.komikan.betternitwit.BetterNitwit;
import com.komikan.betternitwit.entity.ModEntities;
import com.komikan.betternitwit.entity.custom.BetterNitwitRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = BetterNitwit.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientEventBusSubscriber {

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.BETTER_NITWIT.get(), BetterNitwitRenderer::new);
    }
}
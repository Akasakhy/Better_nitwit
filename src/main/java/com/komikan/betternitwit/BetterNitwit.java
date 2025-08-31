package com.komikan.betternitwit;

import com.komikan.betternitwit.client.ClientEventBusSubscriber;
import com.komikan.betternitwit.entity.ModEntities;
import com.komikan.betternitwit.entity.custom.BetterNitwitEntity;
import com.komikan.betternitwit.event.VillagerReplacementHandler;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(BetterNitwit.MOD_ID)
public class BetterNitwit {
    public static final String MOD_ID = "better_nitwit";
    public static final Logger LOGGER = LoggerFactory.getLogger(BetterNitwit.class);

    public BetterNitwit(IEventBus modEventBus) {
        LOGGER.info("Better Nitwit is loading...");

        // Register entities
        ModEntities.ENTITIES.register(modEventBus);

        // Register mod event subscribers
        modEventBus.register(ModEventBusSubscriber.class);

        // Register client events (only on client side)
        if (FMLEnvironment.dist == Dist.CLIENT) {
            modEventBus.register(ClientEventBusSubscriber.class);
        }

        // Register game events
        NeoForge.EVENT_BUS.register(new VillagerReplacementHandler());

        LOGGER.info("Better Nitwit loaded successfully!");
    }

    @EventBusSubscriber(modid = MOD_ID)
    public static class ModEventBusSubscriber {

        @SubscribeEvent
        public static void onEntityAttributeCreation(EntityAttributeCreationEvent event) {
            event.put(ModEntities.BETTER_NITWIT.get(), BetterNitwitEntity.createAttributes().build());
        }
    }
}
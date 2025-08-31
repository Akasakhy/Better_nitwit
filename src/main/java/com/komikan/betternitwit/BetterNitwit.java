package com.komikan.betternitwit;

import com.komikan.betternitwit.entity.ModEntities;
import com.komikan.betternitwit.event.VillagerReplacementHandler;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
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

        // Register game events
        NeoForge.EVENT_BUS.register(new VillagerReplacementHandler());

        LOGGER.info("Better Nitwit loaded successfully!");
    }
}
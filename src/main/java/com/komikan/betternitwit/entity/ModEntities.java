package com.komikan.betternitwit.entity;

import com.komikan.betternitwit.BetterNitwit;
import com.komikan.betternitwit.entity.custom.BetterNitwitEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(Registries.ENTITY_TYPE, BetterNitwit.MOD_ID);

    public static final DeferredHolder<EntityType<?>, EntityType<BetterNitwitEntity>> BETTER_NITWIT =
            ENTITIES.register("better_nitwit", () -> EntityType.Builder.of(BetterNitwitEntity::new, MobCategory.AMBIENT) // CREATUREからAMBIENTに変更
                    .sized(0.6F, 1.95F)
                    .clientTrackingRange(10)
                    .updateInterval(3) // 更新頻度を調整
                    .build("better_nitwit"));
}
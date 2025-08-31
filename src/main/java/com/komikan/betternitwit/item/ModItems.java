package com.komikan.betternitwit.item;

import com.komikan.betternitwit.BetterNitwit;
import com.komikan.betternitwit.entity.ModEntities;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(BetterNitwit.MOD_ID);

    // スポーンエッグ
    public static final DeferredHolder<Item, DeferredSpawnEggItem> BETTER_NITWIT_SPAWN_EGG =
            ITEMS.register("better_nitwit_spawn_egg", () -> new DeferredSpawnEggItem(
                    ModEntities.BETTER_NITWIT,
                    0x563C33, // プライマリカラー（茶色）
                    0x9E7B68, // セカンダリカラー（薄茶色）
                    new Item.Properties()
            ));
}
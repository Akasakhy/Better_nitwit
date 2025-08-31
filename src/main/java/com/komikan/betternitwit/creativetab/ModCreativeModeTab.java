package com.komikan.betternitwit.creativetab;

import com.komikan.betternitwit.BetterNitwit;
import com.komikan.betternitwit.item.ModItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModCreativeModeTab {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, BetterNitwit.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> BETTER_NITWIT_TAB =
            CREATIVE_MODE_TABS.register("better_nitwit_tab", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup." + BetterNitwit.MOD_ID + ".better_nitwit_tab"))
                    .icon(() -> new ItemStack(ModItems.BETTER_NITWIT_SPAWN_EGG.get()))
                    .displayItems((params, output) -> {
                        output.accept(ModItems.BETTER_NITWIT_SPAWN_EGG.get());
                    })
                    .build());
}
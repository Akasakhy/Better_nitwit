package com.komikan.betternitwit.entity.custom;

import com.komikan.betternitwit.BetterNitwit;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class BetterNitwitModel extends GeoModel<BetterNitwitEntity> {
    // ファイル名を小文字に統一
    private static final ResourceLocation MODEL_LOCATION =
            ResourceLocation.fromNamespaceAndPath(BetterNitwit.MOD_ID, "geo/nitwit.geo.json");
    private static final ResourceLocation TEXTURE_LOCATION =
            ResourceLocation.fromNamespaceAndPath(BetterNitwit.MOD_ID, "textures/entity/villager/nitwit.png");
    // animationsディレクトリに変更
    private static final ResourceLocation ANIMATION_LOCATION =
            ResourceLocation.fromNamespaceAndPath(BetterNitwit.MOD_ID, "animations/nitwit.animation.json");

    @Override
    public ResourceLocation getModelResource(BetterNitwitEntity animatable) {
        return MODEL_LOCATION;
    }

    @Override
    public ResourceLocation getTextureResource(BetterNitwitEntity animatable) {
        return TEXTURE_LOCATION;
    }

    @Override
    public ResourceLocation getAnimationResource(BetterNitwitEntity animatable) {
        return ANIMATION_LOCATION;
    }
}
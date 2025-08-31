package com.komikan.betternitwit.entity.custom;

import com.komikan.betternitwit.BetterNitwit;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class BetterNitwitRenderer extends GeoEntityRenderer<BetterNitwitEntity> {

    public BetterNitwitRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new BetterNitwitModel());

        // 3層テクスチャレイヤーを追加
        addRenderLayer(new BaseLayer(this));
        addRenderLayer(new PlainsLayer(this));
        addRenderLayer(new NitwitLayer(this));
    }

    // ベースレイヤー（最下位）
    public static class BaseLayer extends GeoRenderLayer<BetterNitwitEntity> {
        private static final ResourceLocation BASE_TEXTURE =
                ResourceLocation.fromNamespaceAndPath(BetterNitwit.MOD_ID, "textures/entity/villager/base.png");

        public BaseLayer(GeoEntityRenderer<BetterNitwitEntity> entityRenderer) {
            super(entityRenderer);
        }

        @Override
        public void render(PoseStack poseStack, BetterNitwitEntity animatable, BakedGeoModel bakedModel,
                           RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer,
                           float partialTick, int packedLight, int packedOverlay) {

            RenderType baseRenderType = RenderType.entityCutoutNoCull(BASE_TEXTURE);
            getRenderer().reRender(getDefaultBakedModel(animatable), poseStack, bufferSource,
                    animatable, baseRenderType, bufferSource.getBuffer(baseRenderType), partialTick,
                    packedLight, packedOverlay, 0xFFFFFFFF);
        }
    }

    // プレインズレイヤー（中間）
    public static class PlainsLayer extends GeoRenderLayer<BetterNitwitEntity> {
        private static final ResourceLocation PLAINS_TEXTURE =
                ResourceLocation.fromNamespaceAndPath(BetterNitwit.MOD_ID, "textures/entity/villager/plains.png");

        public PlainsLayer(GeoEntityRenderer<BetterNitwitEntity> entityRenderer) {
            super(entityRenderer);
        }

        @Override
        public void render(PoseStack poseStack, BetterNitwitEntity animatable, BakedGeoModel bakedModel,
                           RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer,
                           float partialTick, int packedLight, int packedOverlay) {

            RenderType plainsRenderType = RenderType.entityCutoutNoCull(PLAINS_TEXTURE);
            getRenderer().reRender(getDefaultBakedModel(animatable), poseStack, bufferSource,
                    animatable, plainsRenderType, bufferSource.getBuffer(plainsRenderType), partialTick,
                    packedLight, packedOverlay, 0xFFFFFFFF);
        }
    }

    // ニットウィットレイヤー（最上位）
    public static class NitwitLayer extends GeoRenderLayer<BetterNitwitEntity> {
        private static final ResourceLocation NITWIT_TEXTURE =
                ResourceLocation.fromNamespaceAndPath(BetterNitwit.MOD_ID, "textures/entity/villager/nitwit.png");

        public NitwitLayer(GeoEntityRenderer<BetterNitwitEntity> entityRenderer) {
            super(entityRenderer);
        }

        @Override
        public void render(PoseStack poseStack, BetterNitwitEntity animatable, BakedGeoModel bakedModel,
                           RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer,
                           float partialTick, int packedLight, int packedOverlay) {

            RenderType nitwitRenderType = RenderType.entityCutoutNoCull(NITWIT_TEXTURE);
            getRenderer().reRender(getDefaultBakedModel(animatable), poseStack, bufferSource,
                    animatable, nitwitRenderType, bufferSource.getBuffer(nitwitRenderType), partialTick,
                    packedLight, packedOverlay, 0xFFFFFFFF);
        }
    }
}
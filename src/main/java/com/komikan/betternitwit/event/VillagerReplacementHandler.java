package com.komikan.betternitwit.event;

import com.komikan.betternitwit.BetterNitwit;
import com.komikan.betternitwit.entity.ModEntities;
import com.komikan.betternitwit.entity.custom.BetterNitwitEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class VillagerReplacementHandler {
    // 処理済みのエンティティを追跡（無限ループ防止）
    private static final Set<UUID> processedEntities = new HashSet<>();

    // 置き換え処理の制限（同一チャンクでの連続処理を防ぐ）
    private static long lastReplacementTime = 0;
    private static final long REPLACEMENT_COOLDOWN = 1000; // 1秒のクールダウン

    @SubscribeEvent
    public void onEntityJoinLevel(EntityJoinLevelEvent event) {
        // サーバーサイドでのみ実行
        if (event.getLevel().isClientSide) {
            return;
        }

        // 村人かどうかをチェック
        if (!(event.getEntity() instanceof Villager villager)) {
            return;
        }

        // 無職村人かどうかをチェック
        if (villager.getVillagerData().getProfession() != VillagerProfession.NITWIT) {
            return;
        }

        // 既に処理済みかチェック
        UUID entityId = villager.getUUID();
        if (processedEntities.contains(entityId)) {
            return;
        }

        // クールダウンチェック（過度な置き換えを防ぐ）
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastReplacementTime < REPLACEMENT_COOLDOWN) {
            return;
        }

        // 自然スポーンの村人のみを対象とする（構造物生成やコマンド生成を除外）
        if (villager.getSpawnType() != net.minecraft.world.entity.MobSpawnType.NATURAL) {
            return;
        }

        try {
            // 処理済みとしてマーク
            processedEntities.add(entityId);
            lastReplacementTime = currentTime;

            // カスタム村人エンティティを作成
            BetterNitwitEntity betterNitwit = new BetterNitwitEntity(
                    ModEntities.BETTER_NITWIT.get(),
                    event.getLevel()
            );

            // 位置と基本データをコピー
            betterNitwit.setPos(villager.getX(), villager.getY(), villager.getZ());
            betterNitwit.setYRot(villager.getYRot());
            betterNitwit.setYHeadRot(villager.getYHeadRot());
            betterNitwit.setXRot(villager.getXRot());

            // 村人データをコピー
            betterNitwit.setVillagerData(villager.getVillagerData());

            // 名前をコピー（存在する場合）
            if (villager.hasCustomName()) {
                betterNitwit.setCustomName(villager.getCustomName());
            }

            // 年齢をコピー
            if (villager.isBaby()) {
                betterNitwit.setBaby(true);
            }

            // 健康状態をコピー
            betterNitwit.setHealth(villager.getHealth());

            // スポーンタイプは内部で管理されるため設定不要

            // 既存の村人を削除
            villager.discard();

            // カスタム村人を追加
            event.getLevel().addFreshEntity(betterNitwit);

            BetterNitwit.LOGGER.debug("Replaced Nitwit villager at {}",
                    betterNitwit.blockPosition());

            // メモリ使用量制御（古いエントリを削除）
            if (processedEntities.size() > 1000) {
                processedEntities.clear();
                BetterNitwit.LOGGER.debug("Cleared processed entities cache");
            }

        } catch (Exception e) {
            BetterNitwit.LOGGER.error("Failed to replace Nitwit villager: {}", e.getMessage());
            // エラー時は処理済みマークを除去
            processedEntities.remove(entityId);
        }
    }
}
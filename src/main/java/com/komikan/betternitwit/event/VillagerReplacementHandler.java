package com.komikan.betternitwit.event;

import com.komikan.betternitwit.BetterNitwit;
import com.komikan.betternitwit.entity.ModEntities;
import com.komikan.betternitwit.entity.custom.BetterNitwitEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class VillagerReplacementHandler {
    // 処理済みのエンティティを追跡（無限ループ防止）
    private static final Set<UUID> processedEntities = new HashSet<>();

    // 置き換え処理の制限
    private static long lastReplacementTime = 0;
    private static final long REPLACEMENT_COOLDOWN = 100; // 0.1秒のクールダウン

    /**
     * 通常の村人エンティティが追加される際の置き換え処理
     * 全ての無職村人を確実にBetter Nitwitに置き換え
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST) // 最高優先度で確実に捕捉
    public void onEntityJoinLevel(EntityJoinLevelEvent event) {
        // サーバーサイドでのみ実行
        if (event.getLevel().isClientSide()) {
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

        // クールダウンチェック（軽量化）
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastReplacementTime < REPLACEMENT_COOLDOWN) {
            return;
        }

        try {
            // ★ イベントをキャンセルして元の村人の追加を阻止
            event.setCanceled(true);

            // 処理済みとしてマーク
            processedEntities.add(entityId);
            lastReplacementTime = currentTime;

            // カスタム村人エンティティを作成
            BetterNitwitEntity betterNitwit = new BetterNitwitEntity(
                    ModEntities.BETTER_NITWIT.get(),
                    event.getLevel()
            );

            // 位置と基本データを完全にコピー
            betterNitwit.setPos(villager.getX(), villager.getY(), villager.getZ());
            betterNitwit.setYRot(villager.getYRot());
            betterNitwit.setYHeadRot(villager.getYHeadRot());
            betterNitwit.setXRot(villager.getXRot());
            betterNitwit.setDeltaMovement(villager.getDeltaMovement());

            // 村人データをコピー
            betterNitwit.setVillagerData(villager.getVillagerData());

            // 名前をコピー（存在する場合）
            if (villager.hasCustomName()) {
                betterNitwit.setCustomName(villager.getCustomName());
                betterNitwit.setCustomNameVisible(villager.isCustomNameVisible());
            }

            // 年齢をコピー
            if (villager.isBaby()) {
                betterNitwit.setBaby(true);
            }

            // 健康状態をコピー
            betterNitwit.setHealth(villager.getHealth());

            // 無敵状態をコピー
            if (villager.isInvulnerable()) {
                betterNitwit.setInvulnerable(true);
            }

            // スポーンタイプに応じた最終化
            net.minecraft.world.entity.MobSpawnType spawnType = villager.getSpawnType();
            if (spawnType != null) {
                betterNitwit.finalizeSpawn(
                        (net.minecraft.server.level.ServerLevel) event.getLevel(),
                        event.getLevel().getCurrentDifficultyAt(villager.blockPosition()),
                        spawnType,
                        null
                );
            }

            // ★ 元の村人が既にワールドに追加されている場合の削除処理
            if (!villager.isRemoved()) {
                // 1. 無効状態にマーク
                villager.setRemoved(net.minecraft.world.entity.Entity.RemovalReason.DISCARDED);

                // 2. 移動とAIを停止
                villager.getNavigation().stop();
                villager.goalSelector.removeAllGoals(goal -> true);
                villager.setDeltaMovement(0, 0, 0);

                // 3. 非表示化
                villager.setInvisible(true);
                villager.setSilent(true);

                // 4. 確実に削除
                villager.discard();

                BetterNitwit.LOGGER.debug("Forcibly removed existing nitwit villager");
            }

            // カスタム村人を追加
            event.getLevel().addFreshEntity(betterNitwit);

            // ★ 遅延削除処理（1tick後に再度削除を試行）
            if (event.getLevel() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                UUID villagerToRemove = villager.getUUID();
                serverLevel.getServer().execute(() -> {
                    try {
                        // 1tick後に残っている元の村人を再度削除
                        var remainingVillagers = serverLevel.getEntitiesOfClass(Villager.class,
                                new net.minecraft.world.phys.AABB(betterNitwit.blockPosition()).inflate(2.0),
                                v -> v.getUUID().equals(villagerToRemove) &&
                                        v.getVillagerData().getProfession() == VillagerProfession.NITWIT);

                        for (Villager remaining : remainingVillagers) {
                            if (!remaining.isRemoved()) {
                                remaining.setRemoved(net.minecraft.world.entity.Entity.RemovalReason.DISCARDED);
                                remaining.discard();
                                BetterNitwit.LOGGER.debug("Removed lingering nitwit villager on delayed cleanup");
                            }
                        }
                    } catch (Exception cleanupEx) {
                        BetterNitwit.LOGGER.debug("Delayed cleanup failed: {}", cleanupEx.getMessage());
                    }
                });
            }

            BetterNitwit.LOGGER.debug("Successfully replaced Nitwit villager at {} (spawn type: {})",
                    betterNitwit.blockPosition(), spawnType);

            // メモリ使用量制御
            if (processedEntities.size() > 500) {
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
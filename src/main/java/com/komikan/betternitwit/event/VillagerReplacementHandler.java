package com.komikan.betternitwit.event;

import com.komikan.betternitwit.BetterNitwit;
import com.komikan.betternitwit.entity.ModEntities;
import com.komikan.betternitwit.entity.custom.BetterNitwitEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

public class VillagerReplacementHandler {

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

        try {
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

            // 村人データをコピー（手動実装したsetVillagerDataメソッドを使用）
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

            // 既存の村人を削除
            villager.discard();

            // カスタム村人を追加
            event.getLevel().addFreshEntity(betterNitwit);

            BetterNitwit.LOGGER.debug("Replaced Nitwit villager at {}", betterNitwit.blockPosition());

        } catch (Exception e) {
            BetterNitwit.LOGGER.error("Failed to replace Nitwit villager: {}", e.getMessage());
        }
    }
}
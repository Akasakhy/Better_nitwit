package com.komikan.betternitwit.entity.custom;

import com.komikan.betternitwit.entity.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.util.GeckoLibUtil;

public class BetterNitwitEntity extends AbstractVillager implements GeoEntity {
    // アニメーション状態管理
    private static final EntityDataAccessor<Integer> IDLE_TIME =
            SynchedEntityData.defineId(BetterNitwitEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<String> CURRENT_ANIMATION =
            SynchedEntityData.defineId(BetterNitwitEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Boolean> ANIMATION_LOCKED =
            SynchedEntityData.defineId(BetterNitwitEntity.class, EntityDataSerializers.BOOLEAN);

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private int idleCounter = 0;
    private boolean isPlayingSpecialAnimation = false;
    private String lastAnimation = "";

    // アニメーション制御用の変数
    private int animationStartTime = 0;
    private int animationDuration = 0;
    private boolean animationLocked = false;

    // AIゴール制御用
    private boolean goalsDisabled = false;

    // 村人データを保持（AbstractVillagerから削除されたため手動管理）
    private VillagerData villagerData = new VillagerData(VillagerType.PLAINS, VillagerProfession.NITWIT, 1);

    // アニメーション定義
    private static final RawAnimation YAWN = RawAnimation.begin().then("yawn", Animation.LoopType.PLAY_ONCE);
    private static final RawAnimation DOZE_OFF = RawAnimation.begin().then("doze_off", Animation.LoopType.LOOP);
    private static final RawAnimation DAWDLE = RawAnimation.begin().then("dawdle", Animation.LoopType.LOOP);
    private static final RawAnimation SIT_IN = RawAnimation.begin().then("sit_in", Animation.LoopType.HOLD_ON_LAST_FRAME);
    private static final RawAnimation DENY = RawAnimation.begin().then("deny", Animation.LoopType.PLAY_ONCE);
    private static final RawAnimation IDLE = RawAnimation.begin().then("idle", Animation.LoopType.LOOP);

    public BetterNitwitEntity(EntityType<? extends AbstractVillager> entityType, Level level) {
        super(entityType, level);
        ((GroundPathNavigation) this.getNavigation()).setCanOpenDoors(true);
        this.getNavigation().setCanFloat(true);
        setCanPickUpLoot(true);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(IDLE_TIME, 0);
        builder.define(CURRENT_ANIMATION, "idle");
        builder.define(ANIMATION_LOCKED, false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new TradeWithPlayerGoal(this));
        this.goalSelector.addGoal(1, new AvoidEntityGoal<>(this, net.minecraft.world.entity.monster.Zombie.class, 8.0F, 0.5, 0.5));
        this.goalSelector.addGoal(1, new AvoidEntityGoal<>(this, net.minecraft.world.entity.monster.Evoker.class, 12.0F, 0.5, 0.5));
        this.goalSelector.addGoal(1, new AvoidEntityGoal<>(this, net.minecraft.world.entity.monster.Vindicator.class, 8.0F, 0.5, 0.5));
        this.goalSelector.addGoal(1, new AvoidEntityGoal<>(this, net.minecraft.world.entity.monster.Vex.class, 8.0F, 0.6, 0.6));
        this.goalSelector.addGoal(1, new AvoidEntityGoal<>(this, net.minecraft.world.entity.monster.Pillager.class, 15.0F, 0.5, 0.5));
        this.goalSelector.addGoal(1, new AvoidEntityGoal<>(this, net.minecraft.world.entity.monster.Illusioner.class, 12.0F, 0.5, 0.5));
        this.goalSelector.addGoal(1, new PanicGoal(this, 0.5));
        this.goalSelector.addGoal(2, new GolemRandomStrollInVillageGoal(this, 0.6));
        this.goalSelector.addGoal(3, new MoveThroughVillageGoal(this, 0.6, false, 4, () -> false));
        this.goalSelector.addGoal(4, new GolemRandomStrollInVillageGoal(this, 0.6));
        this.goalSelector.addGoal(5, new RandomStrollGoal(this, 0.6));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 3.0F, 1.0F));
        this.goalSelector.addGoal(7, new LookAtTradingPlayerGoal(this));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
    }

    // ★ 1.21.1で必須になった抽象メソッドの実装
    @Override
    protected void updateTrades() {
        // ニットウィットは取引を行わないため空実装
    }

    @Override
    protected void rewardTradeXp(net.minecraft.world.item.trading.MerchantOffer offer) {
        // ニットウィットは取引経験値を付与しないため空実装
    }

    public static AttributeSupplier.Builder createAttributes() {
        return AbstractVillager.createMobAttributes()
                .add(Attributes.MOVEMENT_SPEED, 0.4)
                .add(Attributes.FOLLOW_RANGE, 48.0);
    }

    @Override
    public void tick() {
        // アニメーションロック中は移動を強制停止
        if (entityData.get(ANIMATION_LOCKED)) {
            // 位置を固定（移動を完全に防ぐ）
            this.setDeltaMovement(this.getDeltaMovement().multiply(0, 1, 0)); // X,Z軸の移動を0に
            this.getNavigation().stop(); // ナビゲーション停止
        }

        super.tick();

        if (!level().isClientSide) {
            updateAnimationState();
        }
    }

    private void updateAnimationState() {
        boolean isMoving = !this.getNavigation().isDone();

        // アニメーションロック中の特別処理
        if (entityData.get(ANIMATION_LOCKED)) {
            // 移動を強制停止（追加の安全措置）
            this.getNavigation().stop();
            this.setDeltaMovement(this.getDeltaMovement().multiply(0, 1, 0));

            // アニメーション時間経過チェック
            if (tickCount - animationStartTime >= animationDuration) {
                endSpecialAnimation();
            }
            return; // ロック中は以降の処理をスキップ
        }

        // 移動開始時のリセット処理
        if (isMoving) {
            idleCounter = 0;
            if (animationLocked) {
                endSpecialAnimation();
            }
        } else {
            // 停止中のカウント
            idleCounter++;
        }

        // アニメーション決定ロジック
        String newAnimation = determineAnimation(isMoving);

        if (!newAnimation.equals(lastAnimation)) {
            entityData.set(CURRENT_ANIMATION, newAnimation);
            lastAnimation = newAnimation;

            // 特殊アニメーション開始時の設定
            if (!newAnimation.equals("idle") && !newAnimation.equals("dawdle")) {
                startSpecialAnimation(newAnimation);
            }
        }

        entityData.set(IDLE_TIME, idleCounter);
    }

    private void startSpecialAnimation(String animationType) {
        entityData.set(ANIMATION_LOCKED, true);
        animationLocked = true;
        isPlayingSpecialAnimation = true;
        animationStartTime = tickCount;

        // アニメーション別の継続時間設定（tick単位）
        switch (animationType) {
            case "yawn" -> animationDuration = 50; // 2.5秒
            case "deny" -> animationDuration = 35; // 1.75秒
            case "sit_in" -> {
                animationDuration = 200; // 5秒間座る
                // 座り込み中は移動を完全停止
                this.getNavigation().stop();
                disableMovementGoals();
            }
            case "doze_off" -> {
                animationDuration = 100; // 5秒間居眠り
                // 居眠り中も移動を停止
                this.getNavigation().stop();
                disableMovementGoals();
            }
            default -> animationDuration = 40;
        }
    }

    // 移動関連のAIゴールを無効化
    private void disableMovementGoals() {
        if (!goalsDisabled) {
            // より確実な移動停止
            this.getNavigation().stop();
            this.getNavigation().setCanFloat(false); // 水中移動も停止

            // 移動系ゴールのみを無効化（Predicateを使用）
            this.goalSelector.removeAllGoals(goal ->
                    goal instanceof RandomStrollGoal ||
                            goal instanceof GolemRandomStrollInVillageGoal ||
                            goal instanceof MoveThroughVillageGoal ||
                            goal instanceof RandomLookAroundGoal ||
                            goal instanceof LookAtPlayerGoal ||
                            goal instanceof LookAtTradingPlayerGoal
            );
            goalsDisabled = true;
        }
    }

    // アニメーション終了処理メソッド
    private void endSpecialAnimation() {
        entityData.set(ANIMATION_LOCKED, false);
        entityData.set(CURRENT_ANIMATION, "idle");
        isPlayingSpecialAnimation = false;
        animationLocked = false;

        // AIゴールを再登録（移動可能にする）
        if (goalsDisabled) {
            restoreMovementGoals();
            goalsDisabled = false;
        }
    }

    // 移動関連のAIゴールを復活
    private void restoreMovementGoals() {
        // ナビゲーション機能を復活
        this.getNavigation().setCanFloat(true);

        // 移動系ゴールのみを再登録
        this.goalSelector.addGoal(2, new GolemRandomStrollInVillageGoal(this, 0.6));
        this.goalSelector.addGoal(3, new MoveThroughVillageGoal(this, 0.6, false, 4, () -> false));
        this.goalSelector.addGoal(4, new GolemRandomStrollInVillageGoal(this, 0.6));
        this.goalSelector.addGoal(5, new RandomStrollGoal(this, 0.6));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 3.0F, 1.0F));
        this.goalSelector.addGoal(7, new LookAtTradingPlayerGoal(this));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
    }

    private String determineAnimation(boolean isMoving) {
        // 1. 移動中：25%の確率でだらだら歩き
        if (isMoving) {
            return random.nextFloat() < 1.0f ? "dawdle" : "idle";
        }

        // 2. 座り込み条件：2.5秒停止で1回だけ判定
        if (idleCounter == 50) { // ちょうど2秒時点で1回だけ判定
            if (random.nextFloat() < 0.7f) { // 40%の確率
                return "sit_in";
            }
        }

        // 3. 居眠り条件：2秒停止で1回だけ判定
        if (idleCounter == 40) { // ちょうど2秒時点で1回だけ判定
            if (random.nextFloat() < 0.4f) { // 50%の確率
                return "doze_off";
            }
        }

        // 4. あくび条件：2.0秒停止時に1回だけ判定
        if (idleCounter == 40) { // ちょうど2.0秒時点で1回だけ判定
            if (random.nextFloat() < 0.6f) { // 60%の確率
                return "yawn";
            }
        }

        // 5. 長時間停止時の追加判定（10秒以降、5秒ごと）
        if (idleCounter > 200 && (idleCounter - 200) % 100 == 0) {
            float chance = random.nextFloat();
            if (chance < 0.3f) {
                return "sit_in";
            } else if (chance < 0.6f) {
                return "doze_off";
            } else if (chance < 0.8f) {
                return "yawn";
            }
        }

        return "idle";
    }

    private boolean isNearWater() {
        BlockPos pos = blockPosition();
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                BlockState state = level().getBlockState(pos.offset(x, 0, z));
                if (state.getFluidState().is(FluidTags.WATER)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isNearBed() {
        BlockPos pos = blockPosition();
        for (int x = -3; x <= 3; x++) {
            for (int y = -1; y <= 2; y++) {
                for (int z = -3; z <= 3; z++) {
                    BlockState state = level().getBlockState(pos.offset(x, y, z));
                    if (state.is(Blocks.RED_BED) || state.getBlock().getName().getString().contains("bed")) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!level().isClientSide) {
            // 右クリック時に拒否アニメーション（ロック機能付き）
            if (!entityData.get(ANIMATION_LOCKED)) {
                entityData.set(CURRENT_ANIMATION, "deny");
                startSpecialAnimation("deny");
                idleCounter = 0;
            }
        }
        return super.mobInteract(player, hand);
    }

    // ★ VillagerData関連メソッド（手動実装）
    public VillagerData getVillagerData() {
        return this.villagerData;
    }

    public void setVillagerData(VillagerData villagerData) {
        VillagerData oldData = this.villagerData;
        this.villagerData = villagerData;
        // 職業が変更された場合の処理（必要に応じて）
        if (oldData.getProfession() != villagerData.getProfession()) {
            // 職業変更時の処理を追加可能
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("IdleTime", idleCounter);
        compound.putString("CurrentAnimation", entityData.get(CURRENT_ANIMATION));
        compound.putBoolean("AnimationLocked", entityData.get(ANIMATION_LOCKED));
        compound.putInt("AnimationStartTime", animationStartTime);
        compound.putInt("AnimationDuration", animationDuration);
        compound.putBoolean("GoalsDisabled", goalsDisabled);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        idleCounter = compound.getInt("IdleTime");
        entityData.set(CURRENT_ANIMATION, compound.getString("CurrentAnimation"));
        entityData.set(ANIMATION_LOCKED, compound.getBoolean("AnimationLocked"));
        animationStartTime = compound.getInt("AnimationStartTime");
        animationDuration = compound.getInt("AnimationDuration");
        goalsDisabled = compound.getBoolean("GoalsDisabled");
        // VillagerDataは常に固定値で再初期化
        this.villagerData = new VillagerData(VillagerType.PLAINS, VillagerProfession.NITWIT, 1);
    }

    // スポーン条件チェック（村人と同じ条件）
    public static boolean checkSpawnRules(EntityType<BetterNitwitEntity> entityType,
                                          net.minecraft.world.level.ServerLevelAccessor level,
                                          net.minecraft.world.entity.MobSpawnType spawnType,
                                          BlockPos pos,
                                          net.minecraft.util.RandomSource random) {
        // 自然スポーンのみを許可（コマンドやスポーンエッグは除外）
        if (spawnType != net.minecraft.world.entity.MobSpawnType.NATURAL) {
            return spawnType == net.minecraft.world.entity.MobSpawnType.SPAWN_EGG ||
                    spawnType == net.minecraft.world.entity.MobSpawnType.COMMAND;
        }

        // 村人と同様の基本条件
        if (pos.getY() < level.getSeaLevel() || pos.getY() > level.getMaxBuildHeight() - 10) {
            return false;
        }

        // 地面の安定性チェック
        BlockPos belowPos = pos.below();
        net.minecraft.world.level.block.state.BlockState groundState = level.getBlockState(belowPos);
        if (!groundState.isValidSpawn(level, belowPos, entityType)) {
            return false;
        }

        // 明るさチェック（村人より厳しく）
        int lightLevel = level.getRawBrightness(pos, 0);
        if (lightLevel < 7) { // 村人は8、Better Nitwitは7以上
            return false;
        }

        // 確率的なスポーン制限（自然スポーン数を減らす）
        if (random.nextFloat() > 0.15f) { // 15%の確率でのみスポーン
            return false;
        }

        // 周囲のBetter Nitwitの数をチェック（過密防止）
        int nearbyCount = level.getEntitiesOfClass(BetterNitwitEntity.class,
                        new net.minecraft.world.phys.AABB(pos).inflate(32.0))
                .size();

        return nearbyCount < 2; // 32ブロック範囲内に2体まで
    }

    // GeckoLib実装
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, this::predicate));
    }

    private PlayState predicate(AnimationState<BetterNitwitEntity> animationState) {
        String currentAnim = entityData.get(CURRENT_ANIMATION);

        switch (currentAnim) {
            case "yawn" -> animationState.getController().setAnimation(YAWN);
            case "doze_off" -> animationState.getController().setAnimation(DOZE_OFF);
            case "dawdle" -> animationState.getController().setAnimation(DAWDLE);
            case "sit_in" -> animationState.getController().setAnimation(SIT_IN);
            case "deny" -> animationState.getController().setAnimation(DENY);
            default -> animationState.getController().setAnimation(IDLE);
        }

        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    // 村人の基本機能
    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.VILLAGER_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.VILLAGER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.VILLAGER_DEATH;
    }

    @Override
    public SoundEvent getNotifyTradeSound() {
        return SoundEvents.VILLAGER_TRADE;
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        BetterNitwitEntity baby = new BetterNitwitEntity(ModEntities.BETTER_NITWIT.get(), level);
        baby.finalizeSpawn(level, level.getCurrentDifficultyAt(baby.blockPosition()),
                net.minecraft.world.entity.MobSpawnType.BREEDING, null);
        return baby;
    }
}
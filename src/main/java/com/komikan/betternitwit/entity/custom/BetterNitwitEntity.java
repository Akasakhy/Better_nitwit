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
import net.minecraft.world.item.trading.MerchantOffer;
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

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private int idleCounter = 0;
    private boolean isPlayingSpecialAnimation = false;
    private String lastAnimation = "";

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
        // villagerDataは初期化時に設定済み
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(IDLE_TIME, 0);
        builder.define(CURRENT_ANIMATION, "idle");
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
        // 必要に応じてカスタム取引ロジックを追加可能
    }

    @Override
    protected void rewardTradeXp(net.minecraft.world.item.trading.MerchantOffer offer) {
        // ニットウィットは取引経験値を付与しないため空実装
        // 通常の村人では経験値やレベルアップ処理が行われる
    }

    public static AttributeSupplier.Builder createAttributes() {
        return AbstractVillager.createMobAttributes()
                .add(Attributes.MOVEMENT_SPEED, 0.4)
                .add(Attributes.FOLLOW_RANGE, 48.0);
    }

    @Override
    public void tick() {
        super.tick();

        if (!level().isClientSide) {
            updateAnimationState();
        }
    }

    private void updateAnimationState() {
        boolean isMoving = !this.getNavigation().isDone();
        boolean isNight = level().isNight();
        boolean nearWater = isNearWater();
        boolean nearBed = isNearBed();

        // アイドル時間カウント
        if (!isMoving && !isPlayingSpecialAnimation) {
            idleCounter++;
        } else if (isMoving) {
            idleCounter = 0;
            isPlayingSpecialAnimation = false;
        }

        // アニメーション決定ロジック
        String newAnimation = determineAnimation(isMoving, isNight, nearWater, nearBed);

        if (!newAnimation.equals(lastAnimation)) {
            entityData.set(CURRENT_ANIMATION, newAnimation);
            lastAnimation = newAnimation;
        }

        entityData.set(IDLE_TIME, idleCounter);
    }

    private String determineAnimation(boolean isMoving, boolean isNight, boolean nearWater, boolean nearBed) {
        // 移動中：25%の確率でだらだら歩き
        if (isMoving) {
            return random.nextFloat() < 1.0f ? "dawdle" : "idle";
        }

        // 座り込み条件
        if ((nearWater || nearBed) && idleCounter > 20) {
            isPlayingSpecialAnimation = true;
            return "sit_in";
        }

        // 居眠り条件（夜間または長時間待機）
        if ((isNight || idleCounter > 200) && idleCounter > 60) {
            isPlayingSpecialAnimation = true;
            return "doze_off";
        }

        // あくび条件（6秒=120tick）
        if (idleCounter == 60) {
            isPlayingSpecialAnimation = true;
            return "yawn";
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
            // 右クリック時に拒否アニメーション
            entityData.set(CURRENT_ANIMATION, "deny");
            isPlayingSpecialAnimation = true;
            idleCounter = 0;

            // 1.75秒後（35tick）にアニメーションリセット
            level().scheduleTick(blockPosition(), Blocks.AIR, 1);
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
        // VillagerDataは再生成時に自動設定されるため保存不要
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        idleCounter = compound.getInt("IdleTime");
        entityData.set(CURRENT_ANIMATION, compound.getString("CurrentAnimation"));
        // VillagerDataは常に固定値で再初期化
        this.villagerData = new VillagerData(VillagerType.PLAINS, VillagerProfession.NITWIT, 1);
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
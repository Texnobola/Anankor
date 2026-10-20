package net.mcreator.anankor.vfx;

import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.animation.object.LoopType;
import com.geckolib.util.GeckoLibUtil;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;

/**
 * A lightweight, no-physics, transient entity used purely for rendering
 * animated 3D VFX meshes via GeckoLib 5.
 *
 * Supports dynamic texture variants and animation names via SynchedEntityData,
 * allowing a single entity class to represent slashes, bursts, shockwaves,
 * shrouds, and any other nature-specific visual effect.
 */
public class AnankorVfxEntity extends Entity implements GeoEntity {

    // ----- Synched Data Keys -----
    /** Which animation to play (e.g. "slash_arc", "burst_radial", "wave_expand") */
    private static final EntityDataAccessor<String> ANIM_KEY = SynchedEntityData.defineId(AnankorVfxEntity.class,
            EntityDataSerializers.STRING);

    /** Texture variant name. The model resolves this to a texture path. */
    private static final EntityDataAccessor<String> TEXTURE_VARIANT = SynchedEntityData.defineId(AnankorVfxEntity.class,
            EntityDataSerializers.STRING);

    /** Uniform scale multiplier for the rendered model */
    private static final EntityDataAccessor<Float> VFX_SCALE = SynchedEntityData.defineId(AnankorVfxEntity.class,
            EntityDataSerializers.FLOAT);

    /**
     * Lifespan in ticks; synched to client to discard entity without network
     * latency
     */
    private static final EntityDataAccessor<Integer> LIFESPAN = SynchedEntityData.defineId(AnankorVfxEntity.class,
            EntityDataSerializers.INT);

    /**
     * Custom loop type that loops when loop=true in json, or holds on last frame
     * when loop=false
     */
    public static final LoopType VFX_LOOP_OR_HOLD = LoopType.register("vfx_loop_or_hold",
            (animatable, point, stage, state, controller) -> {
                if (point.animation().loopType() == LoopType.LOOP) {
                    controller.setTimelineTime(
                            stage.startTime() + (controller.getCurrentTimelineTime() - stage.endTime()));
                    return true;
                }
                controller.setTimelineTime(stage.endTime());
                return true;
            });

    // ----- GeckoLib Cache -----
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    // ======================== Constructors ========================

    public AnankorVfxEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    // ======================== Data Definition ========================

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(ANIM_KEY, "idle");
        builder.define(TEXTURE_VARIANT, "flux");
        builder.define(VFX_SCALE, 1.0f);
        builder.define(LIFESPAN, 12);
    }

    private Entity owner = null;
    private double offsetY = 0.0;

    public AnankorVfxEntity withOwner(Entity owner, double offsetY) {
        this.owner = owner;
        this.offsetY = offsetY;
        return this;
    }

    // ======================== Tick ========================

    @Override
    public void tick() {
        super.tick();
        if (this.owner != null && this.owner.isAlive()) {
            this.setPos(this.owner.getX(), this.owner.getY() + this.offsetY, this.owner.getZ());
            this.setYRot(this.owner.getYRot());
        }
        // Discard on both client and server when lifespan is reached to prevent stale
        // frames
        int lifespan = this.entityData.get(LIFESPAN);
        if (lifespan > 0 && this.tickCount >= lifespan) {
            this.discard();
        }
    }

    // ======================== GeckoLib Animation ========================

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>("vfx_controller", 0, test -> {
            String animName = this.entityData.get(ANIM_KEY);
            return test.setAndContinue(RawAnimation.begin().then(animName, VFX_LOOP_OR_HOLD));
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    // ======================== Distance & Culling ========================

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < 64.0 * 64.0;
    }

    // ======================== Public Setters ========================

    public AnankorVfxEntity withAnimation(String animName) {
        this.entityData.set(ANIM_KEY, animName);
        return this;
    }

    public AnankorVfxEntity withTextureVariant(String variant) {
        this.entityData.set(TEXTURE_VARIANT, variant);
        return this;
    }

    public AnankorVfxEntity withVfxScale(float scale) {
        this.entityData.set(VFX_SCALE, scale);
        return this;
    }

    public AnankorVfxEntity withLifespan(int ticks) {
        this.entityData.set(LIFESPAN, ticks);
        return this;
    }

    // ======================== Public Getters ========================

    public String getTextureVariant() {
        return this.entityData.get(TEXTURE_VARIANT);
    }

    public float getVfxScale() {
        return this.entityData.get(VFX_SCALE);
    }

    public String getAnimKey() {
        return this.entityData.get(ANIM_KEY);
    }

    public int getLifespan() {
        return this.entityData.get(LIFESPAN);
    }

    // ======================== Combat / Damage ========================

    @Override
    public boolean hurtServer(net.minecraft.server.level.ServerLevel level,
            net.minecraft.world.damagesource.DamageSource source, float amount) {
        return false; // Visual entity cannot be damaged
    }

    // ======================== Persistence (no-op for transient VFX)
    // ========================

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        // Transient visual entity - nothing to persist
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        // Transient visual entity - nothing to persist
    }

    // ======================== Static Spawn Helper ========================

    /**
     * Spawns a VFX entity at a given position with rotation matching the executor's
     * look.
     * Call from the server side only.
     */
    public static AnankorVfxEntity spawn(Level level, EntityType<AnankorVfxEntity> type,
            double x, double y, double z,
            float yaw, float pitch,
            String animation, String textureVariant,
            float scale, int lifespan) {
        if (level.isClientSide())
            return null;

        AnankorVfxEntity vfx = new AnankorVfxEntity(type, level);
        vfx.setPos(x, y, z);
        vfx.setYRot(yaw);
        vfx.setXRot(pitch);
        vfx.withAnimation(animation)
                .withTextureVariant(textureVariant)
                .withVfxScale(scale)
                .withLifespan(lifespan);

        level.addFreshEntity(vfx);
        return vfx;
    }
}

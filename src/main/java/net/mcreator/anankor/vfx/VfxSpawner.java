package net.mcreator.anankor.vfx;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/**
 * Convenience methods for spawning VFX entities from technique code.
 * All methods are server-side only (they no-op on the client).
 */
public final class VfxSpawner {

    private VfxSpawner() {}

    // ======================== Directional Slash ========================

    /**
     * Spawns a directional slash/arc VFX in front of the executor.
     * The VFX is positioned slightly ahead, aligned with the executor's look direction.
     *
     * @param executor       The entity casting the ability
     * @param textureVariant Texture variant name (e.g. "fire", "water", "ice", "flux")
     * @param animation      Animation name to play (e.g. "slash_horizontal", "slash_diagonal")
     * @param scale          Visual scale of the effect
     * @param offsetForward  How far in front of the executor to spawn
     * @param lifespanTicks  How many ticks the VFX lives
     */
    public static void spawnSlash(LivingEntity executor, String textureVariant,
                                   String animation, float scale,
                                   double offsetForward, int lifespanTicks) {
        if (executor.level().isClientSide()) return;

        Vec3 look = executor.getLookAngle();
        Vec3 pos = executor.getEyePosition().add(look.scale(offsetForward));

        AnankorVfxEntity.spawn(
                executor.level(),
                AnankorVfxEntities.ANANKOR_VFX.get(),
                pos.x, pos.y - 0.3, pos.z,
                executor.getYRot(), executor.getXRot(),
                animation, textureVariant,
                scale, lifespanTicks
        );
    }

    // ======================== Radial Burst ========================

    /**
     * Spawns a radial burst VFX centered on the executor (shockwave, explosion, aura).
     *
     * @param executor       The entity casting the ability
     * @param textureVariant Texture variant name
     * @param animation      Animation name (e.g. "burst_expand", "shockwave")
     * @param scale          Visual scale
     * @param lifespanTicks  Duration in ticks
     */
    public static void spawnBurst(LivingEntity executor, String textureVariant,
                                   String animation, float scale, int lifespanTicks) {
        if (executor.level().isClientSide()) return;

        AnankorVfxEntity.spawn(
                executor.level(),
                AnankorVfxEntities.ANANKOR_VFX.get(),
                executor.getX(), executor.getY() + 0.1, executor.getZ(),
                executor.getYRot(), 0f,  // Bursts face upward, no pitch
                animation, textureVariant,
                scale, lifespanTicks
        );
    }

    // ======================== Position-based VFX ========================

    /**
     * Spawns a VFX at an arbitrary world position with a specific rotation.
     *
     * @param executor       The source entity (used for level reference)
     * @param textureVariant Texture variant name
     * @param animation      Animation name
     * @param x, y, z        World position
     * @param yaw, pitch     Rotation
     * @param scale          Visual scale
     * @param lifespanTicks  Duration
     */
    public static void spawnAt(LivingEntity executor, String textureVariant,
                                String animation,
                                double x, double y, double z,
                                float yaw, float pitch,
                                float scale, int lifespanTicks) {
        if (executor.level().isClientSide()) return;

        AnankorVfxEntity.spawn(
                executor.level(),
                AnankorVfxEntities.ANANKOR_VFX.get(),
                x, y, z,
                yaw, pitch,
                animation, textureVariant,
                scale, lifespanTicks
        );
    }

    // ======================== Trail VFX ========================

    /**
     * Spawns a trail of VFX entities along a direction (for dashes, lightning paths, etc).
     *
     * @param executor       The entity casting the ability
     * @param textureVariant Texture variant name
     * @param animation      Animation per trail segment
     * @param trailLength    Number of VFX segments to spawn
     * @param spacing        Distance between each segment
     * @param scale          Visual scale per segment
     * @param lifespanTicks  Duration per segment (staggered by index)
     */
    public static void spawnTrail(LivingEntity executor, String textureVariant,
                                   String animation, int trailLength,
                                   double spacing, float scale, int lifespanTicks) {
        if (executor.level().isClientSide()) return;

        Vec3 look = executor.getLookAngle();
        Vec3 start = executor.getEyePosition();

        for (int i = 0; i < trailLength; i++) {
            double progress = (i + 1) * spacing;
            Vec3 pos = start.add(look.scale(progress));

            AnankorVfxEntity vfx = AnankorVfxEntity.spawn(
                    executor.level(),
                    AnankorVfxEntities.ANANKOR_VFX.get(),
                    pos.x, pos.y - 0.3, pos.z,
                    executor.getYRot(), executor.getXRot(),
                    animation, textureVariant,
                    scale * (1.0f - (i * 0.05f)),  // Slight scale falloff
                    lifespanTicks + i * 2            // Staggered disappearance
            );
        }
    }

    // ======================== Sustained Aura VFX ========================

    /**
     * Spawns a sustained living elemental vortex loop around the executor.
     *
     * @param executor       The entity enveloped in the effect
     * @param textureVariant Texture variant (e.g. "dark", "fire", "ice")
     * @param scale          Visual scale
     * @param durationTicks  Duration in ticks
     */
    public static void spawnSustained(LivingEntity executor, String textureVariant,
                                      float scale, int durationTicks) {
        if (executor.level().isClientSide()) return;

        AnankorVfxEntity.spawn(
                executor.level(),
                AnankorVfxEntities.ANANKOR_VFX.get(),
                executor.getX(), executor.getY() + 0.1, executor.getZ(),
                executor.getYRot(), 0f,
                "sustained", textureVariant,
                scale, durationTicks
        );
    }

    /**
     * Spawns an attached VFX entity that dynamically moves and turns with the executor.
     */
    public static AnankorVfxEntity spawnAttached(LivingEntity executor, String textureVariant,
                                                 String animation, float scale, int lifespanTicks, double offsetY) {
        if (executor.level().isClientSide()) return null;

        AnankorVfxEntity vfx = AnankorVfxEntity.spawn(
                executor.level(),
                AnankorVfxEntities.ANANKOR_VFX.get(),
                executor.getX(), executor.getY() + offsetY, executor.getZ(),
                executor.getYRot(), 0f,
                animation, textureVariant,
                scale, lifespanTicks
        );
        if (vfx != null) {
            vfx.withOwner(executor, offsetY);
        }
        return vfx;
    }
}

package net.mcreator.anankor.nature.techniques.space;

import net.mcreator.anankor.AnankorMod;
import net.mcreator.anankor.nature.NatureType;
import net.mcreator.anankor.nature.TechniqueCategory;
import net.mcreator.anankor.nature.techniques.FunctionalTechnique;
import net.mcreator.anankor.nature.techniques.INatureTechnique;
import net.mcreator.anankor.nature.techniques.SpaceTechnique;
import net.mcreator.anankor.vfx.VfxSpawner;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Dedicated registration container and factory for all Space Nature techniques.
 */
public final class SpaceTechniques {

	public static final Identifier ID_SPATIAL_FOLD = Identifier.fromNamespaceAndPath(AnankorMod.MODID,
			"space/spatial_fold");
	public static final Identifier ID_GRAVITON_COLLAPSE = Identifier.fromNamespaceAndPath(AnankorMod.MODID,
			"space/graviton_collapse");
	public static final Identifier ID_SPATIAL_DISTORTION = Identifier.fromNamespaceAndPath(AnankorMod.MODID,
			"space/spatial_distortion");
	public static final Identifier ID_EVENT_HORIZON = Identifier.fromNamespaceAndPath(AnankorMod.MODID,
			"space/event_horizon");

	public static final INatureTechnique SPATIAL_FOLD = new SpaceTechnique();

	public static final INatureTechnique GRAVITON_COLLAPSE = new FunctionalTechnique(
			ID_GRAVITON_COLLAPSE,
			NatureType.SPACE,
			"Graviton Collapse",
			"Collapses local space coordinates at target, pulling entities into a void crush.",
			28,
			55,
			TechniqueCategory.STRIKE,
			(executor, target) -> {
				executor.level().playSound(null, target.x, target.y, target.z,
						SoundEvents.RESPAWN_ANCHOR_DEPLETE.value(), SoundSource.PLAYERS, 1.2f, 1.4f);
				executor.level().playSound(null, target.x, target.y, target.z,
						SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 1.0f, 1.8f);
				if (executor.level() instanceof ServerLevel sl) {
					// Spawn GeckoLib 3D Graviton Collapse Singularity (20 ticks = 1.0s)
					VfxSpawner.spawnAt(executor, "space_collapse", "collapse",
							target.x, target.y + 0.3, target.z,
							0f, 0f,
							1.5f, 20);

					sl.sendParticles(ParticleTypes.PORTAL, target.x, target.y + 0.5, target.z, 40, 1.0, 1.0, 1.0, 0.2);
					sl.sendParticles(ParticleTypes.REVERSE_PORTAL, target.x, target.y + 0.5, target.z, 25, 0.8, 0.8,
							0.8, 0.15);

					AABB area = new AABB(target.x - 3.5, target.y - 1.5, target.z - 3.5, target.x + 3.5,
							target.y + 2.5, target.z + 3.5);
					for (LivingEntity e : executor.level().getEntitiesOfClass(LivingEntity.class, area)) {
						if (e != executor) {
							Vec3 dir = target.subtract(e.position()).normalize().scale(1.2);
							e.setDeltaMovement(dir.x, 0.3, dir.z);
							e.hurt(executor.damageSources().magic(), 10.0f);
						}
					}
				}
			});

	public static final INatureTechnique SPATIAL_DISTORTION = new FunctionalTechnique(
			ID_SPATIAL_DISTORTION,
			NatureType.SPACE,
			"Spatial Distortion",
			"Warps local space, deflecting threats and accelerating the caster's flow.",
			24,
			70,
			TechniqueCategory.WARD,
			(executor, target) -> {
				executor.addEffect(new MobEffectInstance(MobEffects.SPEED, 140, 1));
				executor.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 140, 0));
				executor.level().playSound(null, executor.getX(), executor.getY(), executor.getZ(),
						SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.9f, 1.6f);
				executor.level().playSound(null, executor.getX(), executor.getY(), executor.getZ(),
						SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 1.2f, 1.9f);
				if (executor.level() instanceof ServerLevel sl) {
					// Spawn GeckoLib 3D gyroscopic dimensional warp cage (140 ticks = 7s)
					VfxSpawner.spawnAttached(executor, "space_distortion", "orbit", 1.2f, 140, 0.2);

					sl.sendParticles(ParticleTypes.REVERSE_PORTAL, executor.getX(), executor.getY() + 1.0,
							executor.getZ(), 25, 0.5, 0.8, 0.5, 0.1);
				}
			});

	public static final INatureTechnique EVENT_HORIZON = new FunctionalTechnique(
			ID_EVENT_HORIZON,
			NatureType.SPACE,
			"Event Horizon",
			"Anchors a miniature singularity that suspends foes within temporal stasis.",
			50,
			120,
			TechniqueCategory.EXTENSION,
			(executor, target) -> {
				Vec3 center = executor.position();
				executor.level().playSound(null, center.x, center.y, center.z,
						SoundEvents.END_PORTAL_SPAWN, SoundSource.PLAYERS, 1.2f, 1.2f);
				executor.level().playSound(null, center.x, center.y, center.z,
						SoundEvents.RESPAWN_ANCHOR_CHARGE, SoundSource.PLAYERS, 1.0f, 0.7f);
				if (executor.level() instanceof ServerLevel sl) {
					// Spawn GeckoLib 3D Cosmic Black Hole & Accretion Singularity (120 ticks = 6s)
					VfxSpawner.spawnAt(executor, "space_singularity", "singularity",
							center.x, center.y + 0.1, center.z,
							0f, 0f,
							1.8f, 120);

					sl.sendParticles(ParticleTypes.PORTAL, center.x, center.y + 1.0, center.z,
							80, 4.0, 2.0, 4.0, 0.5);
					sl.sendParticles(ParticleTypes.REVERSE_PORTAL, center.x, center.y + 1.2, center.z,
							50, 3.5, 1.5, 3.5, 0.3);

					AABB area = executor.getBoundingBox().inflate(7.0);
					for (LivingEntity e : executor.level().getEntitiesOfClass(LivingEntity.class, area)) {
						if (e != executor) {
							e.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 60, 0));
							e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 120, 3));
							e.hurt(executor.damageSources().magic(), 12.0f);
						}
					}
				}
			});

	private static final List<INatureTechnique> TECHNIQUES;

	static {
		List<INatureTechnique> list = new ArrayList<>();
		list.add(SPATIAL_FOLD);
		list.add(GRAVITON_COLLAPSE);
		list.add(SPATIAL_DISTORTION);
		list.add(EVENT_HORIZON);
		TECHNIQUES = Collections.unmodifiableList(list);
	}

	public static List<INatureTechnique> getTechniques() {
		return TECHNIQUES;
	}

	private SpaceTechniques() {
	}
}

package net.mcreator.anankor.nature.techniques.dark;

import net.mcreator.anankor.AnankorMod;
import net.mcreator.anankor.nature.NatureAbilityDispatcher;
import net.mcreator.anankor.nature.NatureType;
import net.mcreator.anankor.nature.TechniqueCategory;
import net.mcreator.anankor.nature.techniques.DarkTechnique;
import net.mcreator.anankor.nature.techniques.FunctionalTechnique;
import net.mcreator.anankor.nature.techniques.INatureTechnique;
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
 * Dedicated registration container and factory for all Dark Nature techniques.
 */
public final class DarkTechniques {

	public static final Identifier ID_SHADOW_STEP = Identifier.fromNamespaceAndPath(AnankorMod.MODID,
			"dark/shadow_step");
	public static final Identifier ID_VOID_TENDRILS = Identifier.fromNamespaceAndPath(AnankorMod.MODID,
			"dark/void_tendrils");
	public static final Identifier ID_ABYSSAL_SHROUD = Identifier.fromNamespaceAndPath(AnankorMod.MODID,
			"dark/abyssal_shroud");
	public static final Identifier ID_ECLIPSE_SINGULARITY = Identifier.fromNamespaceAndPath(AnankorMod.MODID,
			"dark/eclipse_singularity");

	public static final INatureTechnique SHADOW_STEP = new FunctionalTechnique(
			ID_SHADOW_STEP,
			NatureType.DARK,
			"Shadow Step",
			"Melts into shadows, slipping seamlessly behind line of sight.",
			18,
			35,
			TechniqueCategory.MOBILITY,
			(executor, target) -> {
				Vec3 origin = executor.position();
				Vec3 dest = NatureAbilityDispatcher.findSafeTeleportDestination(executor, 6.5);

				if (executor.level() instanceof ServerLevel sl) {
					// Spawn departure shadow pool & claw collapse
					VfxSpawner.spawnAt(executor, "dark_step", "step",
							origin.x, origin.y + 0.05, origin.z,
							executor.getYRot(), 0.0f,
							1.3f, 10);
					sl.sendParticles(ParticleTypes.SQUID_INK, origin.x, origin.y + 0.8,
							origin.z, 20, 0.4, 0.4, 0.4, 0.05);
				}

				executor.teleportTo(dest.x, dest.y, dest.z);
				executor.resetFallDistance();
				executor.setDeltaMovement(0, 0, 0);
				executor.level().playSound(null, dest.x, dest.y, dest.z,
						SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.9f, 0.6f);

				if (executor.level() instanceof ServerLevel sl) {
					// Spawn arrival shadow emergence
					VfxSpawner.spawnAt(executor, "dark_step", "step",
							dest.x, dest.y + 0.05, dest.z,
							executor.getYRot(), 0.0f,
							1.4f, 10);
					sl.sendParticles(ParticleTypes.SQUID_INK, dest.x, dest.y + 0.8,
							dest.z, 25, 0.5, 0.5, 0.5, 0.05);
				}
			});

	public static final INatureTechnique VOID_TENDRILS = new FunctionalTechnique(
			ID_VOID_TENDRILS,
			NatureType.DARK,
			"Void Tendrils",
			"Commands dark abyssal tendrils to pierce from the ground, afflicting targets with Wither.",
			24,
			50,
			TechniqueCategory.STRIKE,
			(executor, target) -> {
				Vec3 look = executor.getLookAngle();
				executor.level().playSound(null, executor.getX(), executor.getY(), executor.getZ(),
						SoundEvents.WITHER_SHOOT, SoundSource.PLAYERS, 0.8f, 0.9f);
				executor.level().playSound(null, executor.getX(), executor.getY(), executor.getZ(),
						SoundEvents.SCULK_SHRIEKER_SHRIEK, SoundSource.PLAYERS, 0.8f, 1.2f);

				if (executor.level() instanceof ServerLevel sl) {
					// Spawn 3D directional Void Tendrils piercing forward along look direction (17 ticks = 0.85s)
					VfxSpawner.spawnAt(executor, "dark_tendrils", "strike",
							executor.getX(), executor.getY() + 0.05, executor.getZ(),
							executor.getYRot(), 0.0f,
							1.4f, 17);

					for (int i = 0; i < 18; i++) {
						double dist = 1.0 + i * 0.35;
						sl.sendParticles(ParticleTypes.SOUL, executor.getX() + look.x * dist,
								executor.getEyeY() + look.y * dist, executor.getZ() + look.z * dist, 3, 0.1, 0.1,
								0.1, 0.02);
					}
					AABB hitBox = executor.getBoundingBox().move(look.scale(3.0)).inflate(2.0);
					for (LivingEntity e : executor.level().getEntitiesOfClass(LivingEntity.class, hitBox)) {
						if (e != executor) {
							e.hurt(executor.damageSources().wither(), 9.0f);
							e.addEffect(new MobEffectInstance(MobEffects.WITHER, 100, 1));
							e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 80, 1));
						}
					}
				}
			});

	public static final INatureTechnique ABYSSAL_SHROUD = new DarkTechnique();

	public static final INatureTechnique ECLIPSE_SINGULARITY = new FunctionalTechnique(
			ID_ECLIPSE_SINGULARITY,
			NatureType.DARK,
			"Eclipse Singularity",
			"Manifests a dark void domain that leeches vital essence from all trapped foes.",
			50,
			120,
			TechniqueCategory.EXTENSION,
			(executor, target) -> {
				Vec3 center = executor.position();
				executor.level().playSound(null, executor.getX(), executor.getY(), executor.getZ(),
						SoundEvents.WITHER_DEATH, SoundSource.PLAYERS, 0.9f, 0.7f);
				executor.level().playSound(null, executor.getX(), executor.getY(), executor.getZ(),
						SoundEvents.RESPAWN_ANCHOR_DEPLETE.value(), SoundSource.PLAYERS, 1.1f, 0.6f);

				if (executor.level() instanceof ServerLevel sl) {
					// Spawn 3D hovering Eclipse Singularity with rotating accretion disc (50 ticks = 2.5s)
					VfxSpawner.spawnAt(executor, "dark_singularity", "singularity",
							center.x, center.y + 1.2, center.z,
							0.0f, 0.0f,
							1.6f, 50);

					sl.sendParticles(ParticleTypes.LARGE_SMOKE, executor.getX(), executor.getY() + 1.0,
							executor.getZ(), 50, 3.5, 1.5, 3.5, 0.05);
					sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, executor.getX(), executor.getY() + 1.0,
							executor.getZ(), 30, 2.5, 1.2, 2.5, 0.04);

					AABB area = executor.getBoundingBox().inflate(7.0);
					float totalHeal = 0;
					for (LivingEntity e : executor.level().getEntitiesOfClass(LivingEntity.class, area)) {
						if (e != executor) {
							e.hurt(executor.damageSources().wither(), 12.0f);
							e.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 0));
							e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 80, 2));
							totalHeal += 3.0f;
						}
					}
					if (totalHeal > 0) {
						executor.heal(Math.min(totalHeal, 10.0f));
					}
				}
			});

	private static final List<INatureTechnique> TECHNIQUES;

	static {
		List<INatureTechnique> list = new ArrayList<>();
		list.add(SHADOW_STEP);
		list.add(VOID_TENDRILS);
		list.add(ABYSSAL_SHROUD);
		list.add(ECLIPSE_SINGULARITY);
		TECHNIQUES = Collections.unmodifiableList(list);
	}

	public static List<INatureTechnique> getTechniques() {
		return TECHNIQUES;
	}

	private DarkTechniques() {}
}

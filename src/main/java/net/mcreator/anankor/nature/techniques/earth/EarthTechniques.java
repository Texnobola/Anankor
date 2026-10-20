package net.mcreator.anankor.nature.techniques.earth;

import net.mcreator.anankor.AnankorMod;
import net.mcreator.anankor.nature.NatureType;
import net.mcreator.anankor.nature.TechniqueCategory;
import net.mcreator.anankor.nature.techniques.EarthTechnique;
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
 * Dedicated registration container and factory for all Earth Nature techniques.
 */
public final class EarthTechniques {

	public static final Identifier ID_TECTONIC_VAULT = Identifier.fromNamespaceAndPath(AnankorMod.MODID,
			"earth/tectonic_vault");
	public static final Identifier ID_SEISMIC_BREAKER = Identifier.fromNamespaceAndPath(AnankorMod.MODID,
			"earth/seismic_breaker");
	public static final Identifier ID_GRANITE_AEGIS = Identifier.fromNamespaceAndPath(AnankorMod.MODID,
			"earth/granite_aegis");
	public static final Identifier ID_CATACLYSMIC_FISSURE = Identifier.fromNamespaceAndPath(AnankorMod.MODID,
			"earth/cataclysmic_fissure");

	public static final INatureTechnique TECTONIC_VAULT = new FunctionalTechnique(
			ID_TECTONIC_VAULT,
			NatureType.EARTH,
			"Tectonic Vault",
			"Slams the ground to vault into the air with heavy stone propulsion.",
			18,
			40,
			TechniqueCategory.MOBILITY,
			(executor, target) -> {
				Vec3 look = executor.getLookAngle();
				executor.setDeltaMovement(look.x * 1.4, 0.85, look.z * 1.4);
				executor.syncVelocity = true;
				executor.level().playSound(null, executor.getX(), executor.getY(), executor.getZ(),
						SoundEvents.STONE_BREAK, SoundSource.PLAYERS, 1.0f, 0.8f);
				executor.level().playSound(null, executor.getX(), executor.getY(), executor.getZ(),
						SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 0.7f, 1.2f);
				if (executor.level() instanceof ServerLevel sl) {
					// Spawn GeckoLib 3D erupting basalt monolith & rubble (16 ticks = 0.8s)
					VfxSpawner.spawnAt(executor, "earth_vault", "vault",
							executor.getX(), executor.getY() + 0.05, executor.getZ(),
							executor.getYRot(), 0.0f,
							1.4f, 16);

					sl.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, executor.getX(), executor.getY() + 0.2,
							executor.getZ(), 20, 0.5, 0.2, 0.5, 0.08);
					sl.sendParticles(ParticleTypes.POOF, executor.getX(), executor.getY() + 0.3,
							executor.getZ(), 15, 0.4, 0.2, 0.4, 0.05);
				}
			});

	public static final INatureTechnique SEISMIC_BREAKER = new EarthTechnique();

	public static final INatureTechnique GRANITE_AEGIS = new FunctionalTechnique(
			ID_GRANITE_AEGIS,
			NatureType.EARTH,
			"Granite Aegis",
			"Draws tectonic bedrock plates around the caster, granting Absorption and fortitude.",
			24,
			65,
			TechniqueCategory.WARD,
			(executor, target) -> {
				executor.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 200, 1));
				executor.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 160, 0));
				executor.level().playSound(null, executor.getX(), executor.getY(), executor.getZ(),
						SoundEvents.ANVIL_USE, SoundSource.PLAYERS, 0.8f, 1.2f);
				executor.level().playSound(null, executor.getX(), executor.getY(), executor.getZ(),
						SoundEvents.DECORATED_POT_SHATTER, SoundSource.PLAYERS, 1.0f, 0.7f);
				if (executor.level() instanceof ServerLevel sl) {
					// Spawn GeckoLib 3D orbiting granite shields & geomantic crystals (160 ticks = 8s)
					VfxSpawner.spawnAttached(executor, "earth_aegis", "orbit", 1.15f, 160, 0.2);

					sl.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, executor.getX(), executor.getY() + 0.8,
							executor.getZ(), 20, 0.8, 0.6, 0.8, 0.04);
				}
			});

	public static final INatureTechnique CATACLYSMIC_FISSURE = new FunctionalTechnique(
			ID_CATACLYSMIC_FISSURE,
			NatureType.EARTH,
			"Cataclysmic Fissure",
			"Rips open a deep seismic fault line, blasting rock shards and crushing armor.",
			45,
			100,
			TechniqueCategory.EXTENSION,
			(executor, target) -> {
				Vec3 look = executor.getLookAngle();
				executor.level().playSound(null, executor.getX(), executor.getY(), executor.getZ(),
						SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.2f, 0.5f);
				executor.level().playSound(null, executor.getX(), executor.getY(), executor.getZ(),
						SoundEvents.STONE_BREAK, SoundSource.PLAYERS, 1.3f, 0.6f);
				if (executor.level() instanceof ServerLevel sl) {
					// Spawn GeckoLib 3D directional seismic fault rupture (28 ticks = 1.4s)
					VfxSpawner.spawnAt(executor, "earth_fissure", "fissure",
							executor.getX(), executor.getY() + 0.05, executor.getZ(),
							executor.getYRot(), 0.0f,
							1.3f, 28);

					for (int i = 1; i <= 8; i++) {
						double px = executor.getX() + look.x * i;
						double py = executor.getY();
						double pz = executor.getZ() + look.z * i;
						sl.sendParticles(ParticleTypes.EXPLOSION, px, py + 0.5, pz, 1, 0, 0, 0, 0);
						sl.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, px, py + 0.2, pz, 6, 0.3, 0.2, 0.3, 0.04);
					}
					AABB fissureBox = executor.getBoundingBox().move(look.scale(4.0)).inflate(2.5, 1.5, 2.5);
					for (LivingEntity e : executor.level().getEntitiesOfClass(LivingEntity.class, fissureBox)) {
						if (e != executor) {
							e.hurt(executor.damageSources().magic(), 15.0f);
							e.setDeltaMovement(0, 0.7, 0);
						}
					}
				}
			});

	private static final List<INatureTechnique> TECHNIQUES;

	static {
		List<INatureTechnique> list = new ArrayList<>();
		list.add(TECTONIC_VAULT);
		list.add(SEISMIC_BREAKER);
		list.add(GRANITE_AEGIS);
		list.add(CATACLYSMIC_FISSURE);
		TECHNIQUES = Collections.unmodifiableList(list);
	}

	public static List<INatureTechnique> getTechniques() {
		return TECHNIQUES;
	}

	private EarthTechniques() {}
}

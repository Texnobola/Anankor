package net.mcreator.anankor.nature.techniques.light;

import net.mcreator.anankor.AnankorMod;
import net.mcreator.anankor.nature.NatureAbilityDispatcher;
import net.mcreator.anankor.nature.NatureType;
import net.mcreator.anankor.nature.TechniqueCategory;
import net.mcreator.anankor.nature.techniques.FunctionalTechnique;
import net.mcreator.anankor.nature.techniques.INatureTechnique;
import net.mcreator.anankor.nature.techniques.LightTechnique;
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
 * Dedicated registration container and factory for all Light Nature techniques.
 */
public final class LightTechniques {

	public static final Identifier ID_PHOTON_BLINK = Identifier.fromNamespaceAndPath(AnankorMod.MODID,
			"light/photon_blink");
	public static final Identifier ID_PRISMATIC_RAY = Identifier.fromNamespaceAndPath(AnankorMod.MODID,
			"light/prismatic_ray");
	public static final Identifier ID_LUMINESCENT_BURST = Identifier.fromNamespaceAndPath(AnankorMod.MODID,
			"light/luminescent_burst");
	public static final Identifier ID_DAWN_OF_ANANKEIA = Identifier.fromNamespaceAndPath(AnankorMod.MODID,
			"light/dawn_of_anankeia");

	public static final INatureTechnique PHOTON_BLINK = new FunctionalTechnique(
			ID_PHOTON_BLINK,
			NatureType.LIGHT,
			"Photon Blink",
			"Dissolves into pure photons, instantly reappearing forward in line of sight.",
			18,
			35,
			TechniqueCategory.MOBILITY,
			(executor, target) -> {
				Vec3 origin = executor.position();
				Vec3 dest = NatureAbilityDispatcher.findSafeTeleportDestination(executor, 6.0);

				if (executor.level() instanceof ServerLevel sl) {
					// Spawn departure flash
					VfxSpawner.spawnAt(executor, "light_blink", "blink",
							origin.x, origin.y + 0.1, origin.z,
							executor.getYRot(), 0.0f,
							1.3f, 10);
					sl.sendParticles(ParticleTypes.END_ROD, origin.x, origin.y + 1.0, origin.z,
							15, 0.3, 0.6, 0.3, 0.05);
				}

				executor.teleportTo(dest.x, dest.y, dest.z);
				executor.resetFallDistance();
				executor.setDeltaMovement(0, 0, 0);
				executor.level().playSound(null, dest.x, dest.y, dest.z,
						SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.2f, 1.6f);

				if (executor.level() instanceof ServerLevel sl) {
					// Spawn arrival rematerialization flash
					VfxSpawner.spawnAt(executor, "light_blink", "blink",
							dest.x, dest.y + 0.1, dest.z,
							executor.getYRot(), 0.0f,
							1.4f, 10);
					sl.sendParticles(ParticleTypes.END_ROD, dest.x, dest.y + 1.0, dest.z,
							20, 0.4, 0.8, 0.4, 0.05);
				}
			});

	public static final INatureTechnique PRISMATIC_RAY = new FunctionalTechnique(
			ID_PRISMATIC_RAY,
			NatureType.LIGHT,
			"Prismatic Ray",
			"Channels a high-luminescence solar ray that pierces enemies and sears undead.",
			25,
			50,
			TechniqueCategory.STRIKE,
			(executor, target) -> {
				Vec3 look = executor.getLookAngle();
				executor.level().playSound(null, executor.getX(), executor.getY(), executor.getZ(),
						SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS, 1.0f, 1.8f);
				executor.level().playSound(null, executor.getX(), executor.getY(), executor.getZ(),
						SoundEvents.AMETHYST_CLUSTER_HIT, SoundSource.PLAYERS, 1.3f, 1.5f);

				if (executor.level() instanceof ServerLevel sl) {
					// Spawn 3D Prismatic Ray beam extending along look vector
					VfxSpawner.spawnSlash(executor, "light_ray", "ray", 1.3f, 0.5, 15);

					for (int i = 0; i < 22; i++) {
						double dist = 1.0 + i * 0.35;
						sl.sendParticles(ParticleTypes.END_ROD, executor.getX() + look.x * dist,
								executor.getEyeY() + look.y * dist, executor.getZ() + look.z * dist, 2, 0.05, 0.05,
								0.05, 0.02);
					}
					AABB rayBox = executor.getBoundingBox().move(look.scale(3.5)).inflate(2.0);
					for (LivingEntity e : executor.level().getEntitiesOfClass(LivingEntity.class, rayBox)) {
						if (e != executor) {
							float dmg = e.isInvertedHealAndHarm() ? 16.0f : 9.0f;
							e.hurt(executor.damageSources().magic(), dmg);
							e.setRemainingFireTicks(60);
						}
					}
				}
			});

	public static final INatureTechnique LUMINESCENT_BURST = new LightTechnique();

	public static final INatureTechnique DAWN_OF_ANANKEIA = new FunctionalTechnique(
			ID_DAWN_OF_ANANKEIA,
			NatureType.LIGHT,
			"Dawn of Anankeia",
			"Calls down radiant celestial dawn, restoring vigor to allies and purifying darkness.",
			45,
			120,
			TechniqueCategory.EXTENSION,
			(executor, target) -> {
				executor.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 160, 1));
				executor.level().playSound(null, executor.getX(), executor.getY(), executor.getZ(),
						SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.2f, 1.2f);
				executor.level().playSound(null, executor.getX(), executor.getY(), executor.getZ(),
						SoundEvents.CONDUIT_ACTIVATE, SoundSource.PLAYERS, 1.4f, 1.3f);

				if (executor.level() instanceof ServerLevel sl) {
					// Spawn 3D Dawn of Anankeia celestial light pillar & sacred sun mandala (60 ticks = 3.0s)
					VfxSpawner.spawnAt(executor, "light_dawn", "dawn",
							executor.getX(), executor.getY() + 0.05, executor.getZ(),
							executor.getYRot(), 0.0f,
							1.5f, 60);

					sl.sendParticles(ParticleTypes.END_ROD, executor.getX(), executor.getY() + 1.0, executor.getZ(),
							60, 4.0, 2.0, 4.0, 0.1);
					AABB area = executor.getBoundingBox().inflate(8.0);
					for (LivingEntity e : executor.level().getEntitiesOfClass(LivingEntity.class, area)) {
						if (e != executor) {
							if (e.isInvertedHealAndHarm()) {
								e.hurt(executor.damageSources().magic(), 18.0f);
								e.setRemainingFireTicks(120);
							} else {
								e.hurt(executor.damageSources().magic(), 8.0f);
							}
						}
					}
				}
			});

	private static final List<INatureTechnique> TECHNIQUES;

	static {
		List<INatureTechnique> list = new ArrayList<>();
		list.add(PHOTON_BLINK);
		list.add(PRISMATIC_RAY);
		list.add(LUMINESCENT_BURST);
		list.add(DAWN_OF_ANANKEIA);
		TECHNIQUES = Collections.unmodifiableList(list);
	}

	public static List<INatureTechnique> getTechniques() {
		return TECHNIQUES;
	}

	private LightTechniques() {}
}

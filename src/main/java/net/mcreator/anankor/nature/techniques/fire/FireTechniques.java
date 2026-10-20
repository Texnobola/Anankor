package net.mcreator.anankor.nature.techniques.fire;

import net.mcreator.anankor.AnankorMod;
import net.mcreator.anankor.nature.NatureType;
import net.mcreator.anankor.nature.TechniqueCategory;
import net.mcreator.anankor.nature.techniques.FunctionalTechnique;
import net.mcreator.anankor.nature.techniques.INatureTechnique;
import net.mcreator.anankor.network.PlayPlayerAnimationMessage;
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
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Dedicated registration container and factory for all Fire Nature techniques.
 */
public final class FireTechniques {

	public static final Identifier ID_THERMAL_JET = Identifier.fromNamespaceAndPath(AnankorMod.MODID,
			"fire/thermal_jet");
	public static final Identifier ID_IGNITION_BLADE = Identifier.fromNamespaceAndPath(AnankorMod.MODID,
			"fire/ignition_blade");
	public static final Identifier ID_INFERNAL_BULWARK = Identifier.fromNamespaceAndPath(AnankorMod.MODID,
			"fire/infernal_bulwark");
	public static final Identifier ID_CALAMITY_NOVA = Identifier.fromNamespaceAndPath(AnankorMod.MODID,
			"fire/calamity_nova");

	public static final INatureTechnique THERMAL_JET = new FireTechnique();

	public static final INatureTechnique IGNITION_BLADE = new FunctionalTechnique(
			ID_IGNITION_BLADE,
			NatureType.FIRE,
			"Ignition Blade",
			"Slashes an incandescent flame arc, igniting and repelling targets.",
			20,
			45,
			TechniqueCategory.STRIKE,
			(executor, target) -> {
				Vec3 look = executor.getLookAngle();
				executor.level().playSound(null, executor.getX(), executor.getY(), executor.getZ(),
						SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.0f, 0.8f);
				executor.level().playSound(null, executor.getX(), executor.getY(), executor.getZ(),
						SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 0.9f, 1.3f);
				if (executor.level() instanceof ServerLevel sl) {
					// Trigger PAL Player Animation across network
					PacketDistributor.sendToPlayersTrackingEntityAndSelf(executor,
							new PlayPlayerAnimationMessage(executor.getId(), "fire_slash"));

					// Spawn GeckoLib 3D crescent slash wave (0.82s forward thrust)
					VfxSpawner.spawnSlash(executor, "fire_blade", "slash", 1.4f, 1.2, 18);

					for (int i = 0; i < 15; i++) {
						double dist = 1.0 + i * 0.25;
						sl.sendParticles(ParticleTypes.FLAME, executor.getX() + look.x * dist,
								executor.getEyeY() + look.y * dist, executor.getZ() + look.z * dist, 4, 0.2, 0.2,
								0.2, 0.05);
					}
					AABB hitBox = executor.getBoundingBox().move(look.scale(2.5)).inflate(2.0);
					for (LivingEntity e : executor.level().getEntitiesOfClass(LivingEntity.class, hitBox)) {
						if (e != executor) {
							e.hurt(executor.damageSources().playerAttack(
									executor instanceof net.minecraft.world.entity.player.Player p ? p : null),
									8.0f);
							e.setRemainingFireTicks(100);
							e.setDeltaMovement(e.getDeltaMovement().add(look.x * 0.8, 0.35, look.z * 0.8));
						}
					}
				}
			});

	public static final INatureTechnique INFERNAL_BULWARK = new FunctionalTechnique(
			ID_INFERNAL_BULWARK,
			NatureType.FIRE,
			"Infernal Bulwark",
			"Erupts a radiant flame perimeter that grants Fire Resistance and repels foes.",
			24,
			60,
			TechniqueCategory.WARD,
			(executor, target) -> {
				executor.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 160, 0));
				executor.clearFire();
				executor.level().playSound(null, executor.getX(), executor.getY(), executor.getZ(),
						SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 1.0f, 0.9f);
				if (executor.level() instanceof ServerLevel sl) {
					// Trigger PAL Player Animation across network
					PacketDistributor.sendToPlayersTrackingEntityAndSelf(executor,
							new PlayPlayerAnimationMessage(executor.getId(), "fire_guard"));

					// Spawn GeckoLib 3D orbiting obsidian-crested flame shields
					VfxSpawner.spawnAttached(executor, "fire_bulwark", "orbit", 1.0f, 160, 0.2);

					for (int deg = 0; deg < 360; deg += 18) {
						double rad = Math.toRadians(deg);
						sl.sendParticles(ParticleTypes.FLAME, executor.getX() + Math.cos(rad) * 2.2,
								executor.getY() + 0.5, executor.getZ() + Math.sin(rad) * 2.2, 2, 0.05, 0.1, 0.05,
								0.02);
					}
					AABB area = executor.getBoundingBox().inflate(3.5);
					for (LivingEntity e : executor.level().getEntitiesOfClass(LivingEntity.class, area)) {
						if (e != executor) {
							e.setRemainingFireTicks(80);
							Vec3 push = e.position().subtract(executor.position()).normalize().scale(1.2);
							e.setDeltaMovement(push.x, 0.3, push.z);
						}
					}
				}
			});

	public static final INatureTechnique CALAMITY_NOVA = new FunctionalTechnique(
			ID_CALAMITY_NOVA,
			NatureType.FIRE,
			"Calamity Nova",
			"Channels volcanic core flux, unleashing a devastating multi-phase radial cataclysm.",
			45,
			100,
			TechniqueCategory.EXTENSION,
			(executor, target) -> {
				// Phase 1: Awakening & Channeling (0.0s - 2.2s)
				executor.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 3, false, false, false));
				executor.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 75, 2, false, false, false));
				executor.level().playSound(null, executor.getX(), executor.getY(), executor.getZ(),
						SoundEvents.RESPAWN_ANCHOR_CHARGE, SoundSource.PLAYERS, 1.2f, 0.7f);
				executor.level().playSound(null, executor.getX(), executor.getY(), executor.getZ(),
						SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 1.0f, 0.6f);

				if (executor.level() instanceof ServerLevel sl) {
					// Trigger PAL Player Animation across network (8.5s cinematic)
					PacketDistributor.sendToPlayersTrackingEntityAndSelf(executor,
							new PlayPlayerAnimationMessage(executor.getId(), "calamity_nova"));

					// Spawn GeckoLib 3D multi-phase apocalyptic detonation & magma spires (175 ticks / ~8.75s)
					VfxSpawner.spawnBurst(executor, "fire_nova", "detonate", 1.8f, 175);

					// Initial flux gathering aura
					sl.sendParticles(ParticleTypes.FLAME, executor.getX(), executor.getY() + 1.0,
							executor.getZ(), 20, 0.5, 0.5, 0.5, 0.05);

					// Intermediate Buildup pulse 1 (0.8s / 16 ticks)
					AnankorMod.queueServerWork(16, () -> {
						if (executor.isAlive() && executor.level() instanceof ServerLevel s1) {
							s1.sendParticles(ParticleTypes.FLAME, executor.getX(), executor.getY() + 1.2,
									executor.getZ(), 25, 0.8, 0.8, 0.8, 0.08);
							executor.level().playSound(null, executor.getX(), executor.getY(), executor.getZ(),
									SoundEvents.FIRE_AMBIENT, SoundSource.PLAYERS, 1.2f, 1.2f);
						}
					});

					// Intermediate Buildup pulse 2 (1.6s / 32 ticks)
					AnankorMod.queueServerWork(32, () -> {
						if (executor.isAlive() && executor.level() instanceof ServerLevel s2) {
							s2.sendParticles(ParticleTypes.LAVA, executor.getX(), executor.getY() + 1.5,
									executor.getZ(), 20, 0.6, 0.6, 0.6, 0.1);
							executor.level().playSound(null, executor.getX(), executor.getY(), executor.getZ(),
									SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 1.1f, 0.8f);
						}
					});

					// Phase 2: Critical Compression & Tension Pause (2.05s / 41 ticks)
					AnankorMod.queueServerWork(41, () -> {
						if (executor.isAlive() && executor.level() instanceof ServerLevel s3) {
							executor.level().playSound(null, executor.getX(), executor.getY(), executor.getZ(),
									SoundEvents.WARDEN_SONIC_CHARGE, SoundSource.PLAYERS, 1.2f, 1.5f);
							s3.sendParticles(ParticleTypes.LARGE_SMOKE, executor.getX(), executor.getY() + 1.5,
									executor.getZ(), 25, 0.3, 0.3, 0.3, 0.02);
							s3.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, executor.getX(), executor.getY() + 1.5,
									executor.getZ(), 15, 0.2, 0.2, 0.2, 0.05);
						}
					});

					// Phase 3: Cataclysmic Detonation & Downward Slam (2.28s / 46 ticks)
					AnankorMod.queueServerWork(46, () -> {
						if (executor.isAlive() && executor.level() instanceof ServerLevel s4) {
							executor.level().playSound(null, executor.getX(), executor.getY(), executor.getZ(),
									SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.8f, 0.5f);
							executor.level().playSound(null, executor.getX(), executor.getY(), executor.getZ(),
									SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 1.4f, 0.7f);
							executor.level().playSound(null, executor.getX(), executor.getY(), executor.getZ(),
									SoundEvents.DECORATED_POT_BREAK, SoundSource.PLAYERS, 1.5f, 0.6f);

							s4.sendParticles(ParticleTypes.EXPLOSION_EMITTER, executor.getX(),
									executor.getY() + 0.5,
									executor.getZ(), 3, 0.5, 0.5, 0.5, 0);

							for (int i = 0; i < 80; i++) {
								double theta = Math.random() * Math.PI * 2;
								double r = 1.0 + Math.random() * 7.5;
								s4.sendParticles(ParticleTypes.LAVA, executor.getX() + Math.cos(theta) * r,
										executor.getY() + 0.3, executor.getZ() + Math.sin(theta) * r, 2, 0.1, 0.3,
										0.1, 0.05);
							}
							for (int deg = 0; deg < 360; deg += 15) {
								double rad = Math.toRadians(deg);
								s4.sendParticles(ParticleTypes.FLAME, executor.getX() + Math.cos(rad) * 4.5,
										executor.getY() + 0.2, executor.getZ() + Math.sin(rad) * 4.5, 3, 0.2, 0.2,
										0.2, 0.08);
							}

							AABB area = executor.getBoundingBox().inflate(8.5);
							for (LivingEntity e : s4.getEntitiesOfClass(LivingEntity.class, area)) {
								if (e != executor) {
									e.hurt(executor.damageSources().magic(), 18.0f);
									e.setRemainingFireTicks(200);
									Vec3 dir = e.position().subtract(executor.position()).normalize();
									e.setDeltaMovement(dir.x * 1.8, 0.65, dir.z * 1.8);
								}
							}
						}
					});

					// Phase 4 & 5: Molten Cooling & Tectonic Aftermath (5.5s / 110 ticks)
					AnankorMod.queueServerWork(110, () -> {
						if (executor.isAlive() && executor.level() instanceof ServerLevel s5) {
							s5.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, executor.getX(),
									executor.getY() + 0.3, executor.getZ(), 20, 2.5, 0.2, 2.5, 0.02);
						}
					});
				}
			});

	private FireTechniques() {
	}

	public static List<INatureTechnique> getTechniques() {
		List<INatureTechnique> list = new ArrayList<>();
		list.add(THERMAL_JET);       // Slot 0 / Mobility
		list.add(IGNITION_BLADE);     // Slot 1 / Strike
		list.add(INFERNAL_BULWARK);   // Slot 2 / Ward
		list.add(CALAMITY_NOVA);      // Slot 3 / Extension
		return Collections.unmodifiableList(list);
	}
}

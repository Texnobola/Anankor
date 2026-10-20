package net.mcreator.anankor.nature;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Locale;

import net.mcreator.anankor.nature.techniques.*;
import net.mcreator.anankor.nature.techniques.fire.FireTechniques;
import net.mcreator.anankor.nature.techniques.ice.IceTechniques;
import net.mcreator.anankor.network.AnankorModVariables;
import net.mcreator.anankor.init.AnankorModMobEffects;
import net.mcreator.anankor.AnankorMod;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.network.PacketDistributor;
import net.mcreator.anankor.network.PlayPlayerAnimationMessage;
import net.mcreator.anankor.vfx.VfxSpawner;

public class NatureAbilityDispatcher {

	private static final Map<Identifier, INatureTechnique> TECHNIQUES_BY_ID = new HashMap<>();
	private static final Map<NatureType, List<INatureTechnique>> TECHNIQUES_BY_NATURE = new HashMap<>();
	private static final Map<NatureType, Map<TechniqueCategory, INatureTechnique>> DEFAULT_EQUIPPED_SLOTS = new EnumMap<>(NatureType.class);

	public static void registerTechnique(INatureTechnique technique) {
		if (technique == null) return;
		TECHNIQUES_BY_ID.put(technique.getId(), technique);
		NatureType primary = technique.getPrimaryNature();
		if (primary != null && primary != NatureType.NONE) {
			TECHNIQUES_BY_NATURE.computeIfAbsent(primary, k -> new ArrayList<>()).add(technique);
			DEFAULT_EQUIPPED_SLOTS.computeIfAbsent(primary, k -> new EnumMap<>(TechniqueCategory.class))
					.putIfAbsent(technique.getCategory(), technique);
		}
	}

	public static void registerNature(NatureType nature, List<INatureTechnique> techniques) {
		if (techniques == null) return;
		TECHNIQUES_BY_NATURE.put(nature, new ArrayList<>(techniques));
		Map<TechniqueCategory, INatureTechnique> roleMap = DEFAULT_EQUIPPED_SLOTS.computeIfAbsent(nature, k -> new EnumMap<>(TechniqueCategory.class));
		for (INatureTechnique tech : techniques) {
			TECHNIQUES_BY_ID.put(tech.getId(), tech);
			roleMap.putIfAbsent(tech.getCategory(), tech);
		}
	}

	static {
		// ==========================================
		// 0. RAW FLUX (UNALIGNED)
		// ==========================================
		List<INatureTechnique> raw = new ArrayList<>();
		raw.add(new FunctionalTechnique("Flux Propulsion", "Discharges raw kinetic flux behind you to surge forward.",
				15, 30, TechniqueCategory.MOBILITY, (executor, target) -> {
					Vec3 look = executor.getLookAngle();
					executor.setDeltaMovement(
							executor.getDeltaMovement().add(look.x * 1.5, Math.max(0.15, look.y * 0.8), look.z * 1.5));
					executor.syncVelocity = true;
					executor.level().playSound(null, executor.getX(), executor.getY(), executor.getZ(),
							SoundEvents.WIND_CHARGE_BURST.value(), SoundSource.PLAYERS, 0.9f, 1.2f);
					if (executor.level() instanceof ServerLevel sl) {
						sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, executor.getX(), executor.getY() + 0.5,
								executor.getZ(), 15, 0.4, 0.4, 0.4, 0.1);
					}
				}));
		raw.add(new RawFluxTechnique()); // Slot 1: Strike (Flux Surge)
		raw.add(new FunctionalTechnique("Flux Ward", "Condenses raw flux into an ephemeral kinetic shield.", 20, 50,
				TechniqueCategory.WARD, (executor, target) -> {
					executor.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 80, 0));
					executor.level().playSound(null, executor.getX(), executor.getY(), executor.getZ(),
							SoundEvents.SHIELD_BLOCK.value(), SoundSource.PLAYERS, 0.9f, 1.1f);
					if (executor.level() instanceof ServerLevel sl) {
						sl.sendParticles(ParticleTypes.ENCHANTED_HIT, executor.getX(), executor.getY() + 1.0,
								executor.getZ(), 20, 0.5, 0.5, 0.5, 0.1);
					}
				}));
		raw.add(new FunctionalTechnique("Flux Overload",
				"Overcharges unaligned channels, detonating a concussive shockwave.", 40, 90,
				TechniqueCategory.EXTENSION, (executor, target) -> {
					executor.level().playSound(null, executor.getX(), executor.getY(), executor.getZ(),
							SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.0f, 1.0f);
					if (executor.level() instanceof ServerLevel sl) {
						sl.sendParticles(ParticleTypes.EXPLOSION_EMITTER, executor.getX(), executor.getY() + 0.5,
								executor.getZ(), 1, 0, 0, 0, 0);
						AABB area = executor.getBoundingBox().inflate(6.0);
						for (LivingEntity e : executor.level().getEntitiesOfClass(LivingEntity.class, area)) {
							if (e != executor) {
								e.hurt(executor.damageSources().magic(), 10.0f);
								Vec3 diff = e.position().subtract(executor.position()).normalize();
								e.setDeltaMovement(e.getDeltaMovement().add(diff.x * 1.4, 0.5, diff.z * 1.4));
							}
						}
					}
				}));
		registerNature(NatureType.NONE, raw);

		// ==========================================
		// 1. FIRE (Migrated to dedicated FireTechniques module)
		// ==========================================
		registerNature(NatureType.FIRE, FireTechniques.getTechniques());

		// ==========================================
		// 2. WATER
		// ==========================================
		List<INatureTechnique> water = new ArrayList<>();
		water.add(new FunctionalTechnique("Tidal Surge",
				"Propels yourself rapidly forward on a concentrated crest of water.", 15, 35,
				TechniqueCategory.MOBILITY, (executor, target) -> {
					Vec3 look = executor.getLookAngle();
					executor.clearFire();
					executor.setDeltaMovement(
							executor.getDeltaMovement().add(look.x * 1.8, Math.max(0.12, look.y * 0.9), look.z * 1.8));
					executor.syncVelocity = true;
					executor.level().playSound(null, executor.getX(), executor.getY(), executor.getZ(),
							SoundEvents.GENERIC_SPLASH, SoundSource.PLAYERS, 1.2f, 1.2f);
					executor.level().playSound(null, executor.getX(), executor.getY(), executor.getZ(),
							SoundEvents.PLAYER_SPLASH_HIGH_SPEED, SoundSource.PLAYERS, 1.0f, 1.4f);
					if (executor.level() instanceof ServerLevel sl) {
						// Trigger PAL Player Animation across network (0.6s streamlined surf glide)
						PacketDistributor.sendToPlayersTrackingEntityAndSelf(executor,
								new PlayPlayerAnimationMessage(executor.getId(), "water_dash", true, true));

						// Spawn GeckoLib 3D surging wave crest
						VfxSpawner.spawnAttached(executor, "water_surge", "surge", 1.3f, 14, 0.0);

						sl.sendParticles(ParticleTypes.SPLASH, executor.getX(), executor.getY() + 0.3, executor.getZ(),
								35, 0.7, 0.3, 0.7, 0.2);
						sl.sendParticles(ParticleTypes.BUBBLE, executor.getX(), executor.getY() + 0.4, executor.getZ(),
								20, 0.5, 0.3, 0.5, 0.1);
					}
				}));
		water.add(new FunctionalTechnique("Aqua Lance",
				"Condenses pressurized water into a piercing lance that strikes through adversaries.", 22, 50,
				TechniqueCategory.STRIKE, (executor, target) -> {
					Vec3 look = executor.getLookAngle();
					executor.level().playSound(null, executor.getX(), executor.getY(), executor.getZ(),
							SoundEvents.GENERIC_SWIM, SoundSource.PLAYERS, 1.2f, 1.4f);
					executor.level().playSound(null, executor.getX(), executor.getY(), executor.getZ(),
							SoundEvents.TRIDENT_THROW.value(), SoundSource.PLAYERS, 1.0f, 1.5f);
					if (executor.level() instanceof ServerLevel sl) {
						// Trigger PAL Player Animation across network (0.6s spear lunge)
						PacketDistributor.sendToPlayersTrackingEntityAndSelf(executor,
								new PlayPlayerAnimationMessage(executor.getId(), "water_thrust", true, true));

						// Spawn GeckoLib 3D piercing pressurized water lance (forward thrust along -Z)
						VfxSpawner.spawnSlash(executor, "water_lance", "pierce", 1.4f, 1.2, 14);

						for (int i = 0; i < 20; i++) {
							double dist = 1.0 + i * 0.35;
							sl.sendParticles(ParticleTypes.DRIPPING_WATER, executor.getX() + look.x * dist,
									executor.getEyeY() + look.y * dist, executor.getZ() + look.z * dist, 4, 0.1, 0.1,
									0.1, 0.05);
							sl.sendParticles(ParticleTypes.SPLASH, executor.getX() + look.x * dist,
									executor.getEyeY() + look.y * dist, executor.getZ() + look.z * dist, 2, 0.1, 0.1,
									0.1, 0.08);
						}
						AABB beamBox = executor.getBoundingBox().move(look.scale(3.5)).inflate(2.2);
						for (LivingEntity e : executor.level().getEntitiesOfClass(LivingEntity.class, beamBox)) {
							if (e != executor) {
								e.hurt(executor.damageSources().magic(), 10.0f);
								e.clearFire();
								e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 1));
								e.setDeltaMovement(e.getDeltaMovement().add(look.x * 1.2, 0.4, look.z * 1.2));
							}
						}
					}
				}));
		water.add(new WaterTechnique()); // Slot 2: Ward (Torrential Geyser - existing 3D VFX)
		water.add(new FunctionalTechnique("Abyssal Maelstrom",
				"Manifests a spinning vortex pulling entities into the hydraulic center before erupting a geyser.", 45,
				100,
				TechniqueCategory.EXTENSION, (executor, target) -> {
					Vec3 center = executor.position().add(executor.getLookAngle().scale(3.2));
					executor.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 2, false, false, false));
					executor.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 70, 2, false, false, false));

					executor.level().playSound(null, executor.getX(), executor.getY(), executor.getZ(),
							SoundEvents.CONDUIT_ACTIVATE, SoundSource.PLAYERS, 1.2f, 1.1f);
					executor.level().playSound(null, center.x, center.y, center.z,
							SoundEvents.TRIDENT_RIPTIDE_3.value(), SoundSource.PLAYERS, 1.2f, 0.8f);

					if (executor.level() instanceof ServerLevel sl) {
						// Trigger PAL Player Animation across network (5.0s channeling + eruption)
						PacketDistributor.sendToPlayersTrackingEntityAndSelf(executor,
								new PlayPlayerAnimationMessage(executor.getId(), "water_maelstrom", true, true));

						// Spawn GeckoLib 3D spinning whirlpool & rising geyser pillars (105 ticks /
						// ~5.25s)
						VfxSpawner.spawnAt(executor, "water_maelstrom", "vortex",
								center.x, center.y + 0.1, center.z,
								executor.getYRot(), 0.0f,
								1.8f, 105);

						// Phase 1: Swirling vortex currents (0.0s - 1.8s)
						for (int deg = 0; deg < 720; deg += 30) {
							double rad = Math.toRadians(deg);
							double r = (deg / 720.0) * 4.8;
							sl.sendParticles(ParticleTypes.SPLASH, center.x + Math.cos(rad) * r, center.y + 0.3,
									center.z + Math.sin(rad) * r, 3, 0.1, 0.15, 0.1, 0.05);
						}

						// Phase 2: Accelerated vortex suction pulses (1.0s and 2.0s / 20 & 40 ticks)
						AnankorMod.queueServerWork(20, () -> {
							if (executor.isAlive() && executor.level() instanceof ServerLevel s1) {
								s1.playSound(null, center.x, center.y, center.z,
										SoundEvents.PLAYER_SPLASH_HIGH_SPEED, SoundSource.PLAYERS, 1.2f, 0.9f);
								AABB area = new AABB(center.x - 6.0, center.y - 2.0, center.z - 6.0,
										center.x + 6.0, center.y + 3.0, center.z + 6.0);
								for (LivingEntity e : s1.getEntitiesOfClass(LivingEntity.class, area)) {
									if (e != executor) {
										Vec3 pull = center.subtract(e.position()).normalize().scale(0.85);
										e.setDeltaMovement(pull.x, 0.2, pull.z);
										e.hurt(executor.damageSources().magic(), 4.0f);
									}
								}
							}
						});

						AnankorMod.queueServerWork(40, () -> {
							if (executor.isAlive() && executor.level() instanceof ServerLevel s2) {
								s2.playSound(null, center.x, center.y, center.z,
										SoundEvents.GENERIC_SPLASH, SoundSource.PLAYERS, 1.3f, 1.3f);
								AABB area = new AABB(center.x - 6.0, center.y - 2.0, center.z - 6.0,
										center.x + 6.0, center.y + 3.0, center.z + 6.0);
								for (LivingEntity e : s2.getEntitiesOfClass(LivingEntity.class, area)) {
									if (e != executor) {
										Vec3 pull = center.subtract(e.position()).normalize().scale(1.1);
										e.setDeltaMovement(pull.x, 0.25, pull.z);
										e.hurt(executor.damageSources().magic(), 4.0f);
										e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 80, 2));
									}
								}
							}
						});

						// Phase 3: Cataclysmic Hydraulic Geyser Eruption (3.2s / 64 ticks)
						AnankorMod.queueServerWork(64, () -> {
							if (executor.isAlive() && executor.level() instanceof ServerLevel s3) {
								s3.playSound(null, center.x, center.y, center.z,
										SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.5f, 1.4f);
								s3.playSound(null, center.x, center.y, center.z,
										SoundEvents.CONDUIT_DEACTIVATE, SoundSource.PLAYERS, 1.4f, 0.8f);

								// Massive geyser particle pillar & splash burst
								for (int i = 0; i < 60; i++) {
									double h = (i / 60.0) * 7.0;
									s3.sendParticles(ParticleTypes.SPLASH, center.x, center.y + h, center.z,
											8, 0.8, 0.4, 0.8, 0.25);
									s3.sendParticles(ParticleTypes.BUBBLE, center.x, center.y + h, center.z,
											5, 0.6, 0.3, 0.6, 0.15);
								}

								AABB area = new AABB(center.x - 6.5, center.y - 2.0, center.z - 6.5,
										center.x + 6.5, center.y + 4.0, center.z + 6.5);
								for (LivingEntity e : s3.getEntitiesOfClass(LivingEntity.class, area)) {
									if (e != executor) {
										e.hurt(executor.damageSources().magic(), 14.0f);
										e.clearFire();
										// Cataclysmic skyward launch
										e.setDeltaMovement(0.0, 1.4, 0.0);
									}
								}
							}
						});
					}
				}));
		registerNature(NatureType.WATER, water);

		// ==========================================
		// 3. ICE (Migrated to dedicated IceTechniques module)
		// ==========================================
		registerNature(NatureType.ICE, IceTechniques.getTechniques());

		// ==========================================
		// 4. EARTH
		// ==========================================
		List<INatureTechnique> earth = new ArrayList<>();
		earth.add(new FunctionalTechnique("Tectonic Vault",
				"Slams the ground to vault into the air with heavy stone propulsion.", 18, 40,
				TechniqueCategory.MOBILITY, (executor, target) -> {
					Vec3 look = executor.getLookAngle();
					executor.setDeltaMovement(look.x * 1.4, 0.85, look.z * 1.4);
					executor.syncVelocity = true;
					executor.level().playSound(null, executor.getX(), executor.getY(), executor.getZ(),
							SoundEvents.STONE_BREAK, SoundSource.PLAYERS, 1.0f, 0.8f);
					if (executor.level() instanceof ServerLevel sl) {
						sl.sendParticles(ParticleTypes.POOF, executor.getX(), executor.getY(), executor.getZ(),
								20, 0.5, 0.2, 0.5, 0.1);
					}
				}));
		earth.add(new EarthTechnique()); // Slot 1: Strike (Seismic Breaker - existing 3D VFX)
		earth.add(new FunctionalTechnique("Granite Aegis",
				"Draws tectonic bedrock plates around the caster, granting Absorption and fortitude.", 24, 65,
				TechniqueCategory.WARD, (executor, target) -> {
					executor.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 200, 1));
					executor.level().playSound(null, executor.getX(), executor.getY(), executor.getZ(),
							SoundEvents.ANVIL_USE, SoundSource.PLAYERS, 0.8f, 1.2f);
					if (executor.level() instanceof ServerLevel sl) {
						sl.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, executor.getX(), executor.getY() + 0.8,
								executor.getZ(), 10, 0.5, 0.5, 0.5, 0.05);
					}
				}));
		earth.add(new FunctionalTechnique("Cataclysmic Fissure",
				"Rips open a deep seismic fault line, blasting rock shards and crushing armor.", 45, 100,
				TechniqueCategory.EXTENSION, (executor, target) -> {
					Vec3 look = executor.getLookAngle();
					executor.level().playSound(null, executor.getX(), executor.getY(), executor.getZ(),
							SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.2f, 0.5f);
					if (executor.level() instanceof ServerLevel sl) {
						for (int i = 1; i <= 8; i++) {
							double px = executor.getX() + look.x * i;
							double py = executor.getY();
							double pz = executor.getZ() + look.z * i;
							sl.sendParticles(ParticleTypes.EXPLOSION, px, py + 0.5, pz, 1, 0, 0, 0, 0);
						}
						AABB fissureBox = executor.getBoundingBox().move(look.scale(4.0)).inflate(2.5, 1.5, 2.5);
						for (LivingEntity e : executor.level().getEntitiesOfClass(LivingEntity.class, fissureBox)) {
							if (e != executor) {
								e.hurt(executor.damageSources().magic(), 15.0f);
								e.setDeltaMovement(0, 0.7, 0);
							}
						}
					}
				}));
		registerNature(NatureType.EARTH, earth);

		// ==========================================
		// 5. SPACE
		// ==========================================
		List<INatureTechnique> space = new ArrayList<>();
		space.add(new SpaceTechnique()); // Slot 0: Mobility (Spatial Fold - existing 3D VFX)
		space.add(new FunctionalTechnique("Graviton Collapse",
				"Collapses local space coordinates at target, pulling entities into a void crush.", 28, 55,
				TechniqueCategory.STRIKE, (executor, target) -> {
					executor.level().playSound(null, target.x, target.y, target.z,
							SoundEvents.RESPAWN_ANCHOR_DEPLETE.value(), SoundSource.PLAYERS, 1.2f, 1.4f);
					if (executor.level() instanceof ServerLevel sl) {
						sl.sendParticles(ParticleTypes.PORTAL, target.x, target.y + 0.5, target.z, 40, 1.0, 1.0, 1.0,
								0.2);
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
				}));
		space.add(new FunctionalTechnique("Spatial Distortion",
				"Warps local space, deflecting threats and accelerating the caster's flow.", 24, 70,
				TechniqueCategory.WARD, (executor, target) -> {
					executor.addEffect(new MobEffectInstance(MobEffects.SPEED, 140, 1));
					executor.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 140, 0));
					executor.level().playSound(null, executor.getX(), executor.getY(), executor.getZ(),
							SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.9f, 1.6f);
					if (executor.level() instanceof ServerLevel sl) {
						sl.sendParticles(ParticleTypes.REVERSE_PORTAL, executor.getX(), executor.getY() + 1.0,
								executor.getZ(), 25, 0.5, 0.8, 0.5, 0.1);
					}
				}));
		space.add(new FunctionalTechnique("Event Horizon",
				"Anchors a miniature singularity that suspends foes within temporal stasis.", 50, 120,
				TechniqueCategory.EXTENSION, (executor, target) -> {
					executor.level().playSound(null, executor.getX(), executor.getY(), executor.getZ(),
							SoundEvents.END_PORTAL_SPAWN, SoundSource.PLAYERS, 1.0f, 1.5f);
					if (executor.level() instanceof ServerLevel sl) {
						sl.sendParticles(ParticleTypes.PORTAL, executor.getX(), executor.getY() + 1.0, executor.getZ(),
								80, 4.0, 2.0, 4.0, 0.5);
						AABB area = executor.getBoundingBox().inflate(7.0);
						for (LivingEntity e : executor.level().getEntitiesOfClass(LivingEntity.class, area)) {
							if (e != executor) {
								e.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 60, 0));
								e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 120, 3));
								e.hurt(executor.damageSources().magic(), 12.0f);
							}
						}
					}
				}));
		registerNature(NatureType.SPACE, space);

		// ==========================================
		// 6. ELECTRICITY
		// ==========================================
		List<INatureTechnique> electricity = new ArrayList<>();
		electricity.add(new ElectricityTechnique()); // Slot 0: Mobility (Arc Step - existing 3D VFX)
		electricity.add(new FunctionalTechnique("Ball Lightning",
				"Hurls an unstable spherical plasma cluster that erupts on impact.", 24, 45, TechniqueCategory.STRIKE,
				(executor, target) -> {
					Vec3 look = executor.getLookAngle();
					executor.level().playSound(null, executor.getX(), executor.getY(), executor.getZ(),
							SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.PLAYERS, 0.8f, 1.8f);
					if (executor.level() instanceof ServerLevel sl) {
						for (int i = 0; i < 18; i++) {
							double dist = 1.0 + i * 0.35;
							sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, executor.getX() + look.x * dist,
									executor.getEyeY() + look.y * dist, executor.getZ() + look.z * dist, 4, 0.1, 0.1,
									0.1, 0.08);
						}
						AABB hitBox = executor.getBoundingBox().move(look.scale(3.5)).inflate(2.2);
						for (LivingEntity e : executor.level().getEntitiesOfClass(LivingEntity.class, hitBox)) {
							if (e != executor) {
								e.hurt(executor.damageSources().lightningBolt(), 9.0f);
								e.setDeltaMovement(e.getDeltaMovement().add(look.x * 0.7, 0.3, look.z * 0.7));
							}
						}
					}
				}));
		electricity.add(new FunctionalTechnique("Static Field",
				"Charges an electrified aura that accelerates the caster and zaps nearby foes.", 22, 60,
				TechniqueCategory.WARD, (executor, target) -> {
					executor.addEffect(new MobEffectInstance(MobEffects.HASTE, 160, 1));
					executor.level().playSound(null, executor.getX(), executor.getY(), executor.getZ(),
							SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 0.6f, 2.0f);
					if (executor.level() instanceof ServerLevel sl) {
						sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, executor.getX(), executor.getY() + 1.0,
								executor.getZ(), 30, 1.2, 0.8, 1.2, 0.2);
						AABB area = executor.getBoundingBox().inflate(3.5);
						for (LivingEntity e : executor.level().getEntitiesOfClass(LivingEntity.class, area)) {
							if (e != executor) {
								e.hurt(executor.damageSources().magic(), 5.0f);
							}
						}
					}
				}));
		electricity.add(new FunctionalTechnique("Superconductor Overdrive",
				"Injects high-frequency flux across the nervous system, granting hyper-mobility.", 45, 110,
				TechniqueCategory.EXTENSION, (executor, target) -> {
					executor.addEffect(new MobEffectInstance(MobEffects.SPEED, 160, 2));
					executor.addEffect(new MobEffectInstance(MobEffects.JUMP_BOOST, 160, 1));
					executor.level().playSound(null, executor.getX(), executor.getY(), executor.getZ(),
							SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 1.0f, 1.5f);
					if (executor.level() instanceof ServerLevel sl) {
						sl.sendParticles(ParticleTypes.END_ROD, executor.getX(), executor.getY() + 1.0, executor.getZ(),
								20, 0.4, 0.4, 0.4, 0.1);
					}
				}));
		registerNature(NatureType.ELECTRICITY, electricity);

		// ==========================================
		// 7. LIGHT
		// ==========================================
		List<INatureTechnique> light = new ArrayList<>();
		light.add(new FunctionalTechnique("Photon Blink",
				"Dissolves into pure photons, instantly reappearing forward in line of sight.", 18, 35,
				TechniqueCategory.MOBILITY, (executor, target) -> {
					Vec3 dest = findSafeTeleportDestination(executor, 6.0);
					executor.teleportTo(dest.x, dest.y, dest.z);
					executor.resetFallDistance();
					executor.setDeltaMovement(0, 0, 0);
					executor.level().playSound(null, dest.x, dest.y, dest.z,
							SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.2f, 1.6f);
					if (executor.level() instanceof ServerLevel sl) {
						sl.sendParticles(ParticleTypes.END_ROD, dest.x, dest.y + 1.0, dest.z,
								20, 0.4, 0.8, 0.4, 0.05);
					}
				}));
		light.add(new FunctionalTechnique("Prismatic Ray",
				"Channels a high-luminescence solar ray that pierces enemies and sears undead.", 25, 50,
				TechniqueCategory.STRIKE, (executor, target) -> {
					Vec3 look = executor.getLookAngle();
					executor.level().playSound(null, executor.getX(), executor.getY(), executor.getZ(),
							SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS, 1.0f, 1.8f);
					if (executor.level() instanceof ServerLevel sl) {
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
				}));
		light.add(new LightTechnique()); // Slot 2: Ward (Luminescent Burst / Solar Corona - existing 3D VFX)
		light.add(new FunctionalTechnique("Dawn of Anankeia",
				"Calls down radiant celestial dawn, restoring vigor to allies and purifying darkness.", 45, 120,
				TechniqueCategory.EXTENSION, (executor, target) -> {
					executor.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 160, 1));
					executor.level().playSound(null, executor.getX(), executor.getY(), executor.getZ(),
							SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.2f, 1.2f);
					if (executor.level() instanceof ServerLevel sl) {
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
				}));
		registerNature(NatureType.LIGHT, light);

		// ==========================================
		// 8. DARK
		// ==========================================
		List<INatureTechnique> dark = new ArrayList<>();
		dark.add(new FunctionalTechnique("Shadow Step", "Melts into shadows, slipping seamlessly behind line of sight.",
				18, 35, TechniqueCategory.MOBILITY, (executor, target) -> {
					Vec3 dest = findSafeTeleportDestination(executor, 6.5);
					executor.teleportTo(dest.x, dest.y, dest.z);
					executor.resetFallDistance();
					executor.setDeltaMovement(0, 0, 0);
					executor.level().playSound(null, dest.x, dest.y, dest.z,
							SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.9f, 0.6f);
					if (executor.level() instanceof ServerLevel sl) {
						sl.sendParticles(ParticleTypes.SQUID_INK, dest.x, dest.y + 0.8,
								dest.z, 25, 0.5, 0.5, 0.5, 0.05);
					}
				}));
		dark.add(new FunctionalTechnique("Void Tendrils",
				"Commands dark abyssal tendrils to pierce from the ground, afflicting targets with Wither.", 24, 50,
				TechniqueCategory.STRIKE, (executor, target) -> {
					Vec3 look = executor.getLookAngle();
					executor.level().playSound(null, executor.getX(), executor.getY(), executor.getZ(),
							SoundEvents.WITHER_SHOOT, SoundSource.PLAYERS, 0.8f, 0.9f);
					if (executor.level() instanceof ServerLevel sl) {
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
				}));
		dark.add(new DarkTechnique()); // Slot 2: Ward (Abyssal Shroud - existing 3D VFX)
		dark.add(new FunctionalTechnique("Eclipse Singularity",
				"Manifests a dark void domain that leeches vital essence from all trapped foes.", 50, 120,
				TechniqueCategory.EXTENSION, (executor, target) -> {
					executor.level().playSound(null, executor.getX(), executor.getY(), executor.getZ(),
							SoundEvents.WITHER_DEATH, SoundSource.PLAYERS, 0.9f, 0.7f);
					if (executor.level() instanceof ServerLevel sl) {
						sl.sendParticles(ParticleTypes.LARGE_SMOKE, executor.getX(), executor.getY() + 1.0,
								executor.getZ(), 50, 3.5, 1.5, 3.5, 0.05);
						AABB area = executor.getBoundingBox().inflate(7.0);
						float totalHeal = 0;
						for (LivingEntity e : executor.level().getEntitiesOfClass(LivingEntity.class, area)) {
							if (e != executor) {
								e.hurt(executor.damageSources().wither(), 12.0f);
								e.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 0));
								totalHeal += 3.0f;
							}
						}
						if (totalHeal > 0) {
							executor.heal(Math.min(totalHeal, 10.0f));
						}
					}
				}));
		registerNature(NatureType.DARK, dark);
	}

	public static List<INatureTechnique> getTechniques(NatureType nature) {
		return TECHNIQUES_BY_NATURE.getOrDefault(nature, TECHNIQUES_BY_NATURE.get(NatureType.NONE));
	}

	public static INatureTechnique getTechniqueBySlot(NatureType nature, int slotIndex) {
		List<INatureTechnique> list = getTechniques(nature);
		if (list != null && slotIndex >= 0 && slotIndex < list.size()) {
			return list.get(slotIndex);
		}
		TechniqueCategory category = switch (slotIndex) {
			case 0 -> TechniqueCategory.MOBILITY;
			case 1 -> TechniqueCategory.STRIKE;
			case 2 -> TechniqueCategory.WARD;
			case 3 -> TechniqueCategory.EXTENSION;
			default -> TechniqueCategory.STRIKE;
		};
		Map<TechniqueCategory, INatureTechnique> roleMap = DEFAULT_EQUIPPED_SLOTS.get(nature);
		if (roleMap != null && roleMap.containsKey(category)) {
			return roleMap.get(category);
		}
		return list != null && !list.isEmpty() ? list.get(0) : null;
	}

	public static INatureTechnique getTechniqueById(Identifier id) {
		return TECHNIQUES_BY_ID.get(id);
	}

	public static INatureTechnique findTechniqueByName(String name) {
		if (name == null)
			return null;
		String query = name.trim().toLowerCase(Locale.ROOT);
		for (INatureTechnique tech : TECHNIQUES_BY_ID.values()) {
			if (tech.getName().toLowerCase(Locale.ROOT).equals(query) ||
					tech.getName().toLowerCase(Locale.ROOT).replace(" ", "_").equals(query) ||
					tech.getId().getPath().toLowerCase(Locale.ROOT).equals(query) ||
					tech.getId().toString().toLowerCase(Locale.ROOT).equals(query)) {
				return tech;
			}
		}
		for (List<INatureTechnique> list : TECHNIQUES_BY_NATURE.values()) {
			for (INatureTechnique tech : list) {
				if (tech.getName().toLowerCase(Locale.ROOT).equals(query) ||
						tech.getName().toLowerCase(Locale.ROOT).replace(" ", "_").equals(query)) {
					return tech;
				}
			}
		}
		return null;
	}

	public static List<String> getAllTechniqueNames() {
		List<String> names = new ArrayList<>();
		for (List<INatureTechnique> list : TECHNIQUES_BY_NATURE.values()) {
			for (INatureTechnique tech : list) {
				if (!names.contains(tech.getName())) {
					names.add(tech.getName());
				}
			}
		}
		return names;
	}

	public static boolean castSlot(ServerPlayer player, int slotIndex, boolean bypassCosts) {
		if (player == null)
			return false;

		// 1. Burnout check
		if (!bypassCosts && player.hasEffect(AnankorModMobEffects.BURNOUT)) {
			player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
					SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 0.6f, 1.4f);
			player.sendSystemMessage(Component.literal("§c[BURNOUT] Flux channels collapsed! Unable to channel."),
					true);
			return false;
		}

		// 2. Cooldown check
		if (!bypassCosts && TechniqueCooldownManager.isOnCooldown(player, slotIndex)) {
			int remaining = TechniqueCooldownManager.getRemainingTicks(player, slotIndex);
			player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
					SoundEvents.DISPENSER_FAIL, SoundSource.PLAYERS, 0.5f, 1.5f);
			player.sendSystemMessage(
					Component.literal(String.format("§cTechnique on cooldown! (%.1fs remaining)", remaining / 20.0f)),
					true);
			return false;
		}

		AnankorModVariables.PlayerVariables vars = player.getData(AnankorModVariables.PLAYER_VARIABLES);
		NatureType nature = NatureType.fromString(vars.Nature);
		INatureTechnique technique = getTechniqueBySlot(nature, slotIndex);

		if (technique == null) {
			player.sendSystemMessage(Component.literal("§7No technique bound to this slot."), true);
			return false;
		}

		// 3. Flux check
		if (!bypassCosts && !vars.infiniteFlux && vars.flux <= 0) {
			player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
					SoundEvents.VILLAGER_NO, SoundSource.PLAYERS, 0.5f, 1.0f);
			player.sendSystemMessage(Component.literal("§cInsufficient Flux!"), true);
			return false;
		}

		// 4. Flux deduction
		if (!bypassCosts && !vars.infiniteFlux) {
			vars.flux -= technique.getCost();
			vars.markSyncDirty();

			if (vars.flux <= 0) {
				player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
						SoundEvents.GENERIC_BURN, SoundSource.PLAYERS, 0.8f, 0.8f);
				player.sendSystemMessage(Component.literal("§e" + technique.getName() + ": §c" + (int) vars.flux
						+ " §4[OVERDRAFT IMMINENT] §7(" + nature.getDisplayName() + ")"), true);
			} else {
				player.sendSystemMessage(Component.literal("§b" + technique.getName() + " §7| Flux: §f"
						+ (int) vars.flux + " §7(-" + technique.getCost() + ")"), true);
			}
		}

		// 5. Cooldown trigger
		if (!bypassCosts) {
			TechniqueCooldownManager.setCooldown(player, slotIndex, technique.getCooldownTicks());
		}

		// 6. Execution
		vars.selectedTechnique = slotIndex;
		vars.markSyncDirty();

		Vec3 look = player.getLookAngle();
		Vec3 target = player.position().add(look.scale(5.0));
		technique.execute(player, target);
		return true;
	}

	public static void dispatchAbility(NatureType nature, int techniqueIndex, LivingEntity executor, double targetX,
			double targetY, double targetZ) {
		INatureTechnique technique = getTechniqueBySlot(nature, techniqueIndex);
		if (technique != null) {
			technique.execute(executor, new Vec3(targetX, targetY, targetZ));
		}
	}

	public static Vec3 findSafeTeleportDestination(LivingEntity executor, double distance) {
		Level level = executor.level();
		Vec3 eyePos = executor.getEyePosition();
		Vec3 look = executor.getLookAngle();

		// 1. Raycast from eye position along look vector
		Vec3 traceEnd = eyePos.add(look.scale(distance));
		BlockHitResult hit = level.clip(new ClipContext(
				eyePos, traceEnd,
				ClipContext.Block.COLLIDER,
				ClipContext.Fluid.NONE,
				executor));

		Vec3 dest;
		if (hit.getType() == HitResult.Type.BLOCK) {
			Direction hitFace = hit.getDirection();
			if (hitFace == Direction.UP) {
				BlockPos hitPos = hit.getBlockPos();
				VoxelShape shape = level.getBlockState(hitPos).getCollisionShape(level, hitPos);
				double surfaceY = hitPos.getY() + (shape.isEmpty() ? 1.0 : shape.max(Direction.Axis.Y));
				dest = new Vec3(hit.getLocation().x, surfaceY, hit.getLocation().z);
			} else if (hitFace == Direction.DOWN) {
				dest = new Vec3(hit.getLocation().x, hit.getBlockPos().getY() - executor.getBbHeight() - 0.1,
						hit.getLocation().z);
			} else {
				Vec3 normal = new Vec3(hitFace.getStepX(), hitFace.getStepY(), hitFace.getStepZ());
				dest = hit.getLocation().add(normal.scale(0.6));
				if (dest.y < executor.getY()) {
					dest = new Vec3(dest.x, executor.getY(), dest.z);
				}
			}
		} else {
			double targetX = executor.getX() + look.x * distance;
			double targetZ = executor.getZ() + look.z * distance;
			double targetY = executor.getY() + look.y * distance;

			// If looking slightly downward while standing on ground, keep at least current
			// foot level
			if (executor.onGround() && look.y < 0) {
				targetY = executor.getY();
			}
			dest = new Vec3(targetX, targetY, targetZ);
		}

		// 2. Collision avoidance and floor snapping:
		BlockPos feetPos = BlockPos.containing(dest.x, dest.y, dest.z);
		for (int up = 0; up <= 3; up++) {
			BlockPos checkFeet = feetPos.above(up);
			AABB testBox = executor.getBoundingBox().move(dest.x - executor.getX(),
					(double) checkFeet.getY() - executor.getY(), dest.z - executor.getZ());
			if (level.noCollision(executor, testBox)) {
				BlockPos groundCheck = checkFeet.below();
				VoxelShape groundShape = level.getBlockState(groundCheck).getCollisionShape(level, groundCheck);
				if (!groundShape.isEmpty()) {
					double groundTop = groundCheck.getY() + groundShape.max(Direction.Axis.Y);
					return new Vec3(dest.x, Math.max((double) checkFeet.getY(), groundTop), dest.z);
				}
				return new Vec3(dest.x, checkFeet.getY(), dest.z);
			}
		}

		// 3. Fallback for onGround entities: search down 1-3 blocks so they don't hover
		// in air if ground slopes down
		if (executor.onGround()) {
			for (int down = 1; down <= 3; down++) {
				BlockPos checkGround = feetPos.below(down);
				VoxelShape shape = level.getBlockState(checkGround).getCollisionShape(level, checkGround);
				if (!shape.isEmpty()) {
					double groundTop = checkGround.getY() + shape.max(Direction.Axis.Y);
					AABB testBox = executor.getBoundingBox().move(dest.x - executor.getX(), groundTop - executor.getY(),
							dest.z - executor.getZ());
					if (level.noCollision(executor, testBox)) {
						return new Vec3(dest.x, groundTop, dest.z);
					}
				}
			}
		}

		return dest;
	}
}

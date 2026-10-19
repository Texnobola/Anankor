package net.mcreator.anankor.nature;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Locale;

import net.mcreator.anankor.nature.techniques.*;
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
import net.neoforged.neoforge.network.PacketDistributor;
import net.mcreator.anankor.network.PlayPlayerAnimationMessage;
import net.mcreator.anankor.vfx.VfxSpawner;

public class NatureAbilityDispatcher {

	private static final Map<NatureType, List<INatureTechnique>> TECHNIQUES_BY_NATURE = new HashMap<>();

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
		TECHNIQUES_BY_NATURE.put(NatureType.NONE, raw);

		// ==========================================
		// 1. FIRE
		// ==========================================
		List<INatureTechnique> fire = new ArrayList<>();
		fire.add(new FireTechnique()); // Slot 0: Mobility (Thermal Jet)
		fire.add(new FunctionalTechnique("Ignition Blade",
				"Slashes an incandescent flame arc, igniting and repelling targets.", 20, 45, TechniqueCategory.STRIKE,
				(executor, target) -> {
					Vec3 look = executor.getLookAngle();
					executor.level().playSound(null, executor.getX(), executor.getY(), executor.getZ(),
							SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.0f, 0.8f);
					executor.level().playSound(null, executor.getX(), executor.getY(), executor.getZ(),
							SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 0.9f, 1.3f);
					if (executor.level() instanceof ServerLevel sl) {
						// Trigger PAL Player Animation across network
						PacketDistributor.sendToPlayersTrackingEntityAndSelf(executor,
								new PlayPlayerAnimationMessage(executor.getId(), "fire_slash", true, true));

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
				}));
		fire.add(new FunctionalTechnique("Infernal Bulwark",
				"Erupts a radiant flame perimeter that grants Fire Resistance and repels foes.", 24, 60,
				TechniqueCategory.WARD, (executor, target) -> {
					executor.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 160, 0));
					executor.clearFire();
					executor.level().playSound(null, executor.getX(), executor.getY(), executor.getZ(),
							SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 1.0f, 0.9f);
					if (executor.level() instanceof ServerLevel sl) {
						// Trigger PAL Player Animation across network
						PacketDistributor.sendToPlayersTrackingEntityAndSelf(executor,
								new PlayPlayerAnimationMessage(executor.getId(), "fire_guard", true, true));

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
				}));
		fire.add(new FunctionalTechnique("Calamity Nova",
				"Channels volcanic core flux, unleashing a devastating multi-phase radial cataclysm.", 45, 100,
				TechniqueCategory.EXTENSION, (executor, target) -> {
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
								new PlayPlayerAnimationMessage(executor.getId(), "calamity_nova", true, true));

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
								// Apocalyptic explosion audio
								executor.level().playSound(null, executor.getX(), executor.getY(), executor.getZ(),
										SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.8f, 0.5f);
								executor.level().playSound(null, executor.getX(), executor.getY(), executor.getZ(),
										SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 1.4f, 0.7f);
								executor.level().playSound(null, executor.getX(), executor.getY(), executor.getZ(),
										SoundEvents.DECORATED_POT_BREAK, SoundSource.PLAYERS, 1.5f, 0.6f);

								// Massive radial emitter & volcanic lava outburst
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

								// Cataclysmic shockwave damage & outward propulsion
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
				}));
		TECHNIQUES_BY_NATURE.put(NatureType.FIRE, fire);

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
							SoundEvents.GENERIC_SPLASH, SoundSource.PLAYERS, 1.0f, 1.3f);
					if (executor.level() instanceof ServerLevel sl) {
						sl.sendParticles(ParticleTypes.SPLASH, executor.getX(), executor.getY() + 0.5, executor.getZ(),
								30, 0.6, 0.4, 0.6, 0.2);
					}
				}));
		water.add(new FunctionalTechnique("Aqua Lance",
				"Condenses pressurized water into a piercing lance that strikes through adversaries.", 22, 50,
				TechniqueCategory.STRIKE, (executor, target) -> {
					Vec3 look = executor.getLookAngle();
					executor.level().playSound(null, executor.getX(), executor.getY(), executor.getZ(),
							SoundEvents.GENERIC_SWIM, SoundSource.PLAYERS, 1.2f, 1.5f);
					executor.level().playSound(null, executor.getX(), executor.getY(), executor.getZ(),
							SoundEvents.TRIDENT_THROW.value(), SoundSource.PLAYERS, 0.9f, 1.4f);
					if (executor.level() instanceof ServerLevel sl) {
						for (int i = 0; i < 20; i++) {
							double dist = 1.0 + i * 0.35;
							sl.sendParticles(ParticleTypes.DRIPPING_WATER, executor.getX() + look.x * dist,
									executor.getEyeY() + look.y * dist, executor.getZ() + look.z * dist, 5, 0.1, 0.1,
									0.1, 0.05);
						}
						AABB beamBox = executor.getBoundingBox().move(look.scale(3.5)).inflate(2.2);
						for (LivingEntity e : executor.level().getEntitiesOfClass(LivingEntity.class, beamBox)) {
							if (e != executor) {
								e.hurt(executor.damageSources().magic(), 9.0f);
								e.setDeltaMovement(e.getDeltaMovement().add(look.x * 1.1, 0.4, look.z * 1.1));
							}
						}
					}
				}));
		water.add(new WaterTechnique()); // Slot 2: Ward (Torrential Geyser - existing 3D VFX)
		water.add(new FunctionalTechnique("Abyssal Maelstrom",
				"Manifests a spinning vortex pulling entities into the hydraulic center.", 45, 100,
				TechniqueCategory.EXTENSION, (executor, target) -> {
					executor.level().playSound(null, executor.getX(), executor.getY(), executor.getZ(),
							SoundEvents.TRIDENT_RIPTIDE_3.value(), SoundSource.PLAYERS, 1.2f, 0.7f);
					if (executor.level() instanceof ServerLevel sl) {
						Vec3 center = executor.position().add(executor.getLookAngle().scale(3.0));
						for (int deg = 0; deg < 720; deg += 30) {
							double rad = Math.toRadians(deg);
							double r = (deg / 720.0) * 5.0;
							sl.sendParticles(ParticleTypes.SPLASH, center.x + Math.cos(rad) * r, center.y + 0.4,
									center.z + Math.sin(rad) * r, 3, 0.1, 0.2, 0.1, 0.05);
						}
						AABB area = new AABB(center.x - 5.5, center.y - 2.0, center.z - 5.5, center.x + 5.5,
								center.y + 3.0, center.z + 5.5);
						for (LivingEntity e : executor.level().getEntitiesOfClass(LivingEntity.class, area)) {
							if (e != executor) {
								Vec3 pull = center.subtract(e.position()).normalize().scale(0.8);
								e.setDeltaMovement(pull.x, 0.3, pull.z);
								e.hurt(executor.damageSources().magic(), 11.0f);
							}
						}
					}
				}));
		TECHNIQUES_BY_NATURE.put(NatureType.WATER, water);

		// ==========================================
		// 3. ICE
		// ==========================================
		List<INatureTechnique> ice = new ArrayList<>();
		ice.add(new FunctionalTechnique("Frost Skater",
				"Forms smooth frost beneath feet, gliding forward with crisp agility.", 15, 35,
				TechniqueCategory.MOBILITY, (executor, target) -> {
					Vec3 look = executor.getLookAngle();
					executor.setDeltaMovement(executor.getDeltaMovement().add(look.x * 1.7, 0.1, look.z * 1.7));
					executor.syncVelocity = true;
					executor.level().playSound(null, executor.getX(), executor.getY(), executor.getZ(),
							SoundEvents.SNOW_GOLEM_SHOOT, SoundSource.PLAYERS, 1.0f, 1.4f);
					if (executor.level() instanceof ServerLevel sl) {
						sl.sendParticles(ParticleTypes.SNOWFLAKE, executor.getX(), executor.getY() + 0.2,
								executor.getZ(), 20, 0.5, 0.2, 0.5, 0.05);
					}
				}));
		ice.add(new IceTechnique()); // Slot 1: Strike (Glacial Anchor - existing 3D VFX)
		ice.add(new FunctionalTechnique("Permafrost Barrier",
				"Covers the caster in crystalline frost rime, granting Resistance and chilling attackers.", 25, 70,
				TechniqueCategory.WARD, (executor, target) -> {
					executor.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 140, 1));
					executor.level().playSound(null, executor.getX(), executor.getY(), executor.getZ(),
							SoundEvents.GLASS_PLACE, SoundSource.PLAYERS, 1.2f, 0.8f);
					if (executor.level() instanceof ServerLevel sl) {
						sl.sendParticles(ParticleTypes.ITEM_SNOWBALL, executor.getX(), executor.getY() + 1.0,
								executor.getZ(), 25, 0.6, 0.6, 0.6, 0.1);
					}
				}));
		ice.add(new FunctionalTechnique("Absolute Zero",
				"Channels sub-zero crystallization, flash-freezing all surrounding entities.", 50, 120,
				TechniqueCategory.EXTENSION, (executor, target) -> {
					executor.level().playSound(null, executor.getX(), executor.getY(), executor.getZ(),
							SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.4f, 0.6f);
					executor.level().playSound(null, executor.getX(), executor.getY(), executor.getZ(),
							SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.5f, 1.8f);
					if (executor.level() instanceof ServerLevel sl) {
						sl.sendParticles(ParticleTypes.SNOWFLAKE, executor.getX(), executor.getY() + 1.0,
								executor.getZ(), 100, 4.0, 1.5, 4.0, 0.1);
						AABB area = executor.getBoundingBox().inflate(8.0);
						for (LivingEntity e : executor.level().getEntitiesOfClass(LivingEntity.class, area)) {
							if (e != executor) {
								e.hurt(executor.damageSources().freeze(), 12.0f);
								e.setTicksFrozen(200);
								e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 100, 3));
							}
						}
					}
				}));
		TECHNIQUES_BY_NATURE.put(NatureType.ICE, ice);

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
		TECHNIQUES_BY_NATURE.put(NatureType.EARTH, earth);

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
		TECHNIQUES_BY_NATURE.put(NatureType.SPACE, space);

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
		TECHNIQUES_BY_NATURE.put(NatureType.ELECTRICITY, electricity);

		// ==========================================
		// 7. LIGHT
		// ==========================================
		List<INatureTechnique> light = new ArrayList<>();
		light.add(new FunctionalTechnique("Photon Blink",
				"Dissolves into pure photons, instantly reappearing forward in line of sight.", 18, 35,
				TechniqueCategory.MOBILITY, (executor, target) -> {
					Vec3 look = executor.getLookAngle();
					double blinkDist = 6.0;
					Vec3 dest = executor.position().add(look.scale(blinkDist));
					executor.teleportTo(dest.x, dest.y, dest.z);
					executor.level().playSound(null, executor.getX(), executor.getY(), executor.getZ(),
							SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.2f, 1.6f);
					if (executor.level() instanceof ServerLevel sl) {
						sl.sendParticles(ParticleTypes.END_ROD, executor.getX(), executor.getY() + 1.0, executor.getZ(),
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
		TECHNIQUES_BY_NATURE.put(NatureType.LIGHT, light);

		// ==========================================
		// 8. DARK
		// ==========================================
		List<INatureTechnique> dark = new ArrayList<>();
		dark.add(new FunctionalTechnique("Shadow Step", "Melts into shadows, slipping seamlessly behind line of sight.",
				18, 35, TechniqueCategory.MOBILITY, (executor, target) -> {
					Vec3 look = executor.getLookAngle();
					double dist = 6.5;
					Vec3 dest = executor.position().add(look.scale(dist));
					executor.teleportTo(dest.x, dest.y, dest.z);
					executor.level().playSound(null, executor.getX(), executor.getY(), executor.getZ(),
							SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.9f, 0.6f);
					if (executor.level() instanceof ServerLevel sl) {
						sl.sendParticles(ParticleTypes.SQUID_INK, executor.getX(), executor.getY() + 0.8,
								executor.getZ(), 25, 0.5, 0.5, 0.5, 0.05);
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
		TECHNIQUES_BY_NATURE.put(NatureType.DARK, dark);
	}

	public static List<INatureTechnique> getTechniques(NatureType nature) {
		return TECHNIQUES_BY_NATURE.getOrDefault(nature, TECHNIQUES_BY_NATURE.get(NatureType.NONE));
	}

	public static INatureTechnique getTechniqueBySlot(NatureType nature, int slotIndex) {
		List<INatureTechnique> list = getTechniques(nature);
		if (slotIndex >= 0 && slotIndex < list.size()) {
			return list.get(slotIndex);
		}
		return list.isEmpty() ? null : list.get(0);
	}

	public static INatureTechnique findTechniqueByName(String name) {
		if (name == null)
			return null;
		String query = name.trim().toLowerCase(Locale.ROOT);
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
}

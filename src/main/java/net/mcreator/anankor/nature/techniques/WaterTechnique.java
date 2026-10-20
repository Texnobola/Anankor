package net.mcreator.anankor.nature.techniques;

import net.mcreator.anankor.network.PlayPlayerAnimationMessage;
import net.mcreator.anankor.vfx.VfxSpawner;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

import net.mcreator.anankor.nature.TechniqueCategory;

import java.util.List;

public class WaterTechnique implements INatureTechnique {

	@Override
	public String getName() {
		return "Torrential Geyser";
	}

	@Override
	public String getDescription() {
		return "Sweeps both hands skyward, erupting a turbulent high-pressure water geyser that launches enemies into the air.";
	}

	@Override
	public int getCost() {
		return 18;
	}

	@Override
	public int getCooldownTicks() {
		return 60;
	}

	@Override
	public TechniqueCategory getCategory() {
		return TechniqueCategory.WARD;
	}

	@Override
	public void execute(LivingEntity executor, Vec3 targetPosition) {
		Vec3 look = executor.getLookAngle();

		// Extinguish executor
		executor.clearFire();

		// Audio: Roaring water eruption
		executor.level().playSound(null, executor.getX(), executor.getY(), executor.getZ(),
				SoundEvents.GENERIC_SPLASH, SoundSource.PLAYERS, 1.2f, 0.8f);
		executor.level().playSound(null, executor.getX(), executor.getY(), executor.getZ(),
				SoundEvents.PLAYER_SPLASH_HIGH_SPEED, SoundSource.PLAYERS, 1.0f, 1.15f);
		executor.level().playSound(null, executor.getX(), executor.getY(), executor.getZ(),
				SoundEvents.CONDUIT_ACTIVATE, SoundSource.PLAYERS, 0.8f, 1.4f);

		if (executor.level() instanceof ServerLevel level) {
			// Trigger PAL Player Animation across network (seen in first-person & by other
			// players)
			PacketDistributor.sendToPlayersTrackingEntityAndSelf(executor,
					new PlayPlayerAnimationMessage(executor.getId(), "water_geyser", true, true));

			// === GeckoLib VFX: Skyward Geyser Surge ===
			double spawnX = executor.getX() + look.x * 2.2;
			double spawnZ = executor.getZ() + look.z * 2.2;
			VfxSpawner.spawnAt(executor, "water", "geyser_surge",
					spawnX, executor.getY() + 0.05, spawnZ,
					executor.getYRot(), 0.0f,
					1.5f, 19);

			// Dynamic particles: Water column & droplet fountain
			for (int i = 0; i < 40; i++) {
				double height = (i / 40.0) * 4.2;
				double spread = 0.4 + (height * 0.25);
				double px = spawnX + (Math.random() - 0.5) * spread;
				double py = executor.getY() + height;
				double pz = spawnZ + (Math.random() - 0.5) * spread;

				level.sendParticles(ParticleTypes.SPLASH, px, py, pz,
						3, 0.15, 0.3, 0.15, 0.12);
				level.sendParticles(ParticleTypes.BUBBLE, px, py, pz,
						2, 0.1, 0.2, 0.1, 0.08);
			}

			// Cone knockback and skyward blast
			AABB hitBox = executor.getBoundingBox().inflate(3.5).move(look.scale(2.5));
			List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, hitBox,
					e -> e != executor && e.isAlive());
			for (LivingEntity target : targets) {
				target.clearFire();
				target.hurt(level.damageSources().mobAttack(executor), 7.0f);
				target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 80, 1));

				// Launch upward into the air and backward
				Vec3 curVel = target.getDeltaMovement();
				target.setDeltaMovement(curVel.x * 0.5 + look.x * 0.8, Math.max(0.72, curVel.y + 0.65),
						curVel.z * 0.5 + look.z * 0.8);
				target.syncVelocity = true;
			}
		}
	}
}

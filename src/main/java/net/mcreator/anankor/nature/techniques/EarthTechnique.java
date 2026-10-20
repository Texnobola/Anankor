package net.mcreator.anankor.nature.techniques;

import net.mcreator.anankor.network.PlayPlayerAnimationMessage;
import net.mcreator.anankor.vfx.VfxSpawner;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

import net.mcreator.anankor.nature.TechniqueCategory;

public class EarthTechnique implements INatureTechnique {

	@Override
	public String getName() {
		return "Seismic Breaker";
	}

	@Override
	public String getDescription() {
		return "Crashes both fists into the bedrock with tectonic force, erupting a crater of jagged megaliths and launching foes skyward.";
	}

	@Override
	public int getCost() {
		return 25;
	}

	@Override
	public int getCooldownTicks() {
		return 60;
	}

	@Override
	public TechniqueCategory getCategory() {
		return TechniqueCategory.STRIKE;
	}

	@Override
	public void execute(LivingEntity executor, Vec3 targetPosition) {
		// Deep tectonic impact audio
		executor.level().playSound(null, executor.getX(), executor.getY(), executor.getZ(),
				SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.0f, 0.65f);
		executor.level().playSound(null, executor.getX(), executor.getY(), executor.getZ(),
				SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 0.9f, 0.7f);
		executor.level().playSound(null, executor.getX(), executor.getY(), executor.getZ(),
				SoundEvents.STONE_BREAK, SoundSource.PLAYERS, 1.2f, 0.8f);

		if (executor.level() instanceof ServerLevel level) {
			// Trigger PAL Player Animation across network
			PacketDistributor.sendToPlayersTrackingEntityAndSelf(executor,
					new PlayPlayerAnimationMessage(executor.getId(), "earth_smash"));

			// === GeckoLib VFX: Tectonic Core Smash crater & megaliths ===
			VfxSpawner.spawnAt(executor, "earth", "seismic_smash",
					executor.getX(), executor.getY() + 0.05, executor.getZ(),
					executor.getYRot(), 0.0f,
					1.6f, 24);

			// Ground dust, poof, and seismic shockwave particles
			level.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, executor.getX(), executor.getY() + 0.1, executor.getZ(),
					35, 2.5, 0.2, 2.5, 0.06);
			level.sendParticles(ParticleTypes.POOF, executor.getX(), executor.getY() + 0.2, executor.getZ(),
					25, 2.0, 0.3, 2.0, 0.1);

			AABB tremorArea = executor.getBoundingBox().inflate(5.5, 2.5, 5.5);
			List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, tremorArea,
					e -> e != executor && e.isAlive());
			for (LivingEntity target : targets) {
				target.hurt(level.damageSources().mobAttack(executor), 8.5f);

				// Launch violently into air with concussive upward impulse
				Vec3 vel = target.getDeltaMovement();
				target.setDeltaMovement(vel.x * 0.4, 0.82, vel.z * 0.4);
				target.syncVelocity = true;
			}
		}
	}
}

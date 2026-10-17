package net.mcreator.anankor.nature.techniques;

import dev.vfxweaver.api.VFXAPI;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.Identifier;
import net.minecraft.core.particles.ParticleTypes;

import java.util.Map;
import java.util.List;

public class EarthTechnique implements INatureTechnique {

	@Override
	public String getName() {
		return "Seismic Breaker";
	}

	@Override
	public String getDescription() {
		return "Strikes tectonic flux into the bedrock, hurling foes upward with concussive tremors.";
	}

	@Override
	public int getCost() {
		return 25;
	}

	@Override
	public void execute(LivingEntity executor, Vec3 targetPosition) {
		executor.level().playSound(null, executor.getX(), executor.getY(), executor.getZ(),
				SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 0.9f, 0.7f);

		if (executor.level() instanceof ServerLevel level) {
			// Ground dust and smoke ring
			level.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, executor.getX(), executor.getY() + 0.1, executor.getZ(),
					25, 2.0, 0.1, 2.0, 0.05);
			level.sendParticles(ParticleTypes.POOF, executor.getX(), executor.getY() + 0.2, executor.getZ(),
					20, 1.8, 0.2, 1.8, 0.08);

			AABB tremorArea = executor.getBoundingBox().inflate(5.5, 2.0, 5.5);
			List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, tremorArea, e -> e != executor && e.isAlive());
			for (LivingEntity target : targets) {
				target.hurt(level.damageSources().mobAttack(executor), 8.5f);
				// Launch violently into air
				Vec3 vel = target.getDeltaMovement();
				target.setDeltaMovement(vel.x * 0.5, 0.75, vel.z * 0.5);
				target.hurtMarked = true;
			}
		}

		// VFX-Weaver trigger
		if (executor instanceof ServerPlayer player) {
			VFXAPI.sendEffect(player, Identifier.fromNamespaceAndPath("anankor", "seismic_breaker"), player.position(), Map.of(), null);
		}
	}
}

package net.mcreator.anankor.nature.techniques;


import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.Identifier;
import net.minecraft.core.particles.ParticleTypes;

import java.util.Map;
import java.util.List;

public class SpaceTechnique implements INatureTechnique {

	@Override
	public String getName() {
		return "Spatial Fold";
	}

	@Override
	public String getDescription() {
		return "Folds local space coordinates, instantly blinking forward with a vacuum implosion.";
	}

	@Override
	public int getCost() {
		return 25;
	}

	@Override
	public void execute(LivingEntity executor, Vec3 targetPosition) {
		Vec3 origin = executor.position();
		Vec3 look = executor.getLookAngle();
		double blinkDistance = 7.5;

		// Raycast for safe destination
		Vec3 targetTrace = origin.add(look.scale(blinkDistance));
		BlockHitResult hit = executor.level().clip(new ClipContext(
				executor.getEyePosition(),
				targetTrace,
				ClipContext.Block.COLLIDER,
				ClipContext.Fluid.NONE,
				executor));

		Vec3 destination;
		if (hit.getType() == HitResult.Type.BLOCK) {
			destination = hit.getLocation().subtract(look.scale(0.6));
		} else {
			destination = targetTrace;
		}

		// Sounds & origin particles
		executor.level().playSound(null, origin.x, origin.y, origin.z,
				SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.9f, 1.25f);

		if (executor.level() instanceof ServerLevel level) {
			level.sendParticles(ParticleTypes.PORTAL, origin.x, origin.y + 0.5, origin.z, 25, 0.3, 0.5, 0.3, 0.2);
			level.sendParticles(ParticleTypes.REVERSE_PORTAL, destination.x, destination.y + 0.5, destination.z, 25, 0.3, 0.5, 0.3, 0.2);

			// Vacuum effect: pull nearby mobs towards the departure point
			AABB vacuumBox = new AABB(origin.x - 4, origin.y - 2, origin.z - 4, origin.x + 4, origin.y + 2, origin.z + 4);
			List<LivingEntity> nearby = level.getEntitiesOfClass(LivingEntity.class, vacuumBox, e -> e != executor && e.isAlive());
			for (LivingEntity entity : nearby) {
				Vec3 pull = origin.subtract(entity.position()).normalize().scale(0.5);
				entity.setDeltaMovement(entity.getDeltaMovement().add(pull));
				entity.syncVelocity = true;
			}
		}

		// Teleport the entity
		executor.teleportTo(destination.x, destination.y, destination.z);

		// Destination sound
		executor.level().playSound(null, destination.x, destination.y, destination.z,
				SoundEvents.PLAYER_TELEPORT, SoundSource.PLAYERS, 0.8f, 1.4f);
	}
}

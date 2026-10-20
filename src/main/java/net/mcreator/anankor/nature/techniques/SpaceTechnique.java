package net.mcreator.anankor.nature.techniques;

import net.mcreator.anankor.network.PlayPlayerAnimationMessage;
import net.mcreator.anankor.vfx.VfxSpawner;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

import net.mcreator.anankor.nature.TechniqueCategory;

import java.util.List;

public class SpaceTechnique implements INatureTechnique {

	@Override
	public String getName() {
		return "Spatial Fold";
	}

	@Override
	public String getDescription() {
		return "Thrusts arms forward in a quantum phase dash, tearing through local space coordinates with a vacuum implosion.";
	}

	@Override
	public int getCost() {
		return 25;
	}

	@Override
	public int getCooldownTicks() {
		return 50;
	}

	@Override
	public TechniqueCategory getCategory() {
		return TechniqueCategory.MOBILITY;
	}

	@Override
	public void execute(LivingEntity executor, Vec3 targetPosition) {
		Vec3 origin = executor.position();
		Vec3 destination = net.mcreator.anankor.nature.NatureAbilityDispatcher.findSafeTeleportDestination(executor,
				7.5);

		// Sounds & audio feedback
		executor.level().playSound(null, origin.x, origin.y, origin.z,
				SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0f, 1.3f);
		executor.level().playSound(null, origin.x, origin.y, origin.z,
				SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.8f, 1.8f);

		if (executor.level() instanceof ServerLevel level) {
			// Trigger PAL Player Animation across network
			PacketDistributor.sendToPlayersTrackingEntityAndSelf(executor,
					new PlayPlayerAnimationMessage(executor.getId(), "space_dash", true, true));

			level.sendParticles(ParticleTypes.PORTAL, origin.x, origin.y + 0.5, origin.z, 30, 0.4, 0.6, 0.4, 0.25);
			level.sendParticles(ParticleTypes.REVERSE_PORTAL, destination.x, destination.y + 0.5, destination.z, 30,
					0.4, 0.6, 0.4, 0.25);

			// === GeckoLib VFX: Quantum Phase Dash warp tunnel at origin & emergence at
			// destination ===
			VfxSpawner.spawnAt(executor, "space", "phase_dash", origin.x, origin.y + 0.1, origin.z, executor.getYRot(),
					executor.getXRot(), 1.5f, 15);
			VfxSpawner.spawnAt(executor, "space", "energy_pulse", destination.x, destination.y + 0.5, destination.z,
					executor.getYRot(), 0.0f, 1.4f, 12);

			// Vacuum effect: pull nearby mobs towards the departure point
			AABB vacuumBox = new AABB(origin.x - 4, origin.y - 2, origin.z - 4, origin.x + 4, origin.y + 2,
					origin.z + 4);
			List<LivingEntity> nearby = level.getEntitiesOfClass(LivingEntity.class, vacuumBox,
					e -> e != executor && e.isAlive());
			for (LivingEntity entity : nearby) {
				Vec3 pull = origin.subtract(entity.position()).normalize().scale(0.5);
				entity.setDeltaMovement(entity.getDeltaMovement().add(pull));
				entity.syncVelocity = true;
			}
		}

		// Teleport the entity
		executor.teleportTo(destination.x, destination.y, destination.z);
		executor.resetFallDistance();
		executor.setDeltaMovement(0, 0, 0);

		// Destination sound
		executor.level().playSound(null, destination.x, destination.y, destination.z,
				SoundEvents.PLAYER_TELEPORT, SoundSource.PLAYERS, 0.8f, 1.4f);
	}
}

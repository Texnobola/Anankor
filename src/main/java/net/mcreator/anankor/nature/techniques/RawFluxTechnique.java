package net.mcreator.anankor.nature.techniques;

import net.mcreator.anankor.vfx.VfxSpawner;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.particles.ParticleTypes;

import java.util.List;

import net.mcreator.anankor.nature.TechniqueCategory;

public class RawFluxTechnique implements INatureTechnique {

	@Override
	public String getName() {
		return "Flux Surge";
	}

	@Override
	public String getDescription() {
		return "Discharges turbulent unaligned Flux in a concussive radial shockwave.";
	}

	@Override
	public int getCost() {
		return 20;
	}

	@Override
	public int getCooldownTicks() {
		return 30;
	}

	@Override
	public TechniqueCategory getCategory() {
		return TechniqueCategory.STRIKE;
	}

	@Override
	public void execute(LivingEntity executor, Vec3 targetPosition) {
		Vec3 pos = executor.position();

		// 1. Audio
		executor.level().playSound(null, pos.x, pos.y, pos.z,
				SoundEvents.WIND_CHARGE_BURST.value(), SoundSource.PLAYERS, 0.9f, 1.1f);

		// 2. Radial particle burst and area knockback
		if (executor.level() instanceof ServerLevel level) {
			level.sendParticles(ParticleTypes.ELECTRIC_SPARK, pos.x, pos.y + 0.5, pos.z, 30, 1.2, 0.4, 1.2, 0.25);
			level.sendParticles(ParticleTypes.SONIC_BOOM, pos.x, pos.y + 0.5, pos.z, 1, 0, 0, 0, 0);

			// === GeckoLib VFX: Radial shockwave burst ===
			VfxSpawner.spawnBurst(executor, "flux", "shockwave", 1.5f, 12);

			double radius = 4.5;
			AABB shockwaveBox = new AABB(pos.x - radius, pos.y - 1.5, pos.z - radius, pos.x + radius, pos.y + 2.5,
					pos.z + radius);
			List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, shockwaveBox,
					e -> e != executor && e.isAlive());

			for (LivingEntity target : targets) {
				Vec3 diff = target.position().subtract(pos);
				double distance = Math.max(0.1, diff.length());
				Vec3 push = diff.normalize().scale(Math.max(0.4, 1.2 - (distance / radius)));
				target.setDeltaMovement(target.getDeltaMovement().add(push.x, 0.35, push.z));
				target.hurt(level.damageSources().mobAttack(executor), 5.5f);
				target.syncVelocity = true;
			}
		}
	}
}

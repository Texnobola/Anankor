package net.mcreator.anankor.nature.techniques;


import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.Identifier;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

import java.util.Map;
import java.util.List;

public class WaterTechnique implements INatureTechnique {

	@Override
	public String getName() {
		return "Tidal Surge";
	}

	@Override
	public String getDescription() {
		return "Unleashes a pressurized flux torrent, blasting enemies backwards and dousing flames.";
	}

	@Override
	public int getCost() {
		return 18;
	}

	@Override
	public void execute(LivingEntity executor, Vec3 targetPosition) {
		Vec3 look = executor.getLookAngle();

		// Extinguish executor
		executor.clearFire();

		// Audio
		executor.level().playSound(null, executor.getX(), executor.getY(), executor.getZ(),
				SoundEvents.GENERIC_SPLASH, SoundSource.PLAYERS, 1.0f, 0.9f);

		if (executor.level() instanceof ServerLevel level) {
			// Splash & bubble particles in an expanding cone
			for (int i = 0; i < 30; i++) {
				double spreadX = look.x * 2.0 + (Math.random() - 0.5) * 1.5;
				double spreadY = look.y * 2.0 + (Math.random() - 0.5) * 0.8;
				double spreadZ = look.z * 2.0 + (Math.random() - 0.5) * 1.5;
				level.sendParticles(ParticleTypes.SPLASH, executor.getX() + spreadX, executor.getEyeY() + spreadY,
						executor.getZ() + spreadZ,
						3, 0.2, 0.2, 0.2, 0.1);
				level.sendParticles(ParticleTypes.BUBBLE, executor.getX() + spreadX * 0.7,
						executor.getEyeY() + spreadY * 0.7, executor.getZ() + spreadZ * 0.7,
						2, 0.1, 0.1, 0.1, 0.05);
			}

			// Cone knockback and drowning push
			AABB hitBox = executor.getBoundingBox().inflate(3.5).move(look.scale(2.5));
			List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, hitBox,
					e -> e != executor && e.isAlive());
			for (LivingEntity target : targets) {
				target.clearFire();
				target.hurt(level.damageSources().mobAttack(executor), 6.5f);
				target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 80, 0));
				target.knockback(1.4, -look.x, -look.z);
			}
		}
	}
}

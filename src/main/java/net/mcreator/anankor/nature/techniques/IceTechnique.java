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

public class IceTechnique implements INatureTechnique {

	@Override
	public String getName() {
		return "Glacial Anchor";
	}

	@Override
	public String getDescription() {
		return "Supercools atmospheric flux into crystalline frost, anchoring nearby entities in ice.";
	}

	@Override
	public int getCost() {
		return 22;
	}

	@Override
	public void execute(LivingEntity executor, Vec3 targetPosition) {
		executor.level().playSound(null, executor.getX(), executor.getY(), executor.getZ(),
				SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 0.9f, 1.4f);

		if (executor.level() instanceof ServerLevel level) {
			// Snowflake radial burst
			level.sendParticles(ParticleTypes.SNOWFLAKE, executor.getX(), executor.getY() + 0.3, executor.getZ(),
					45, 2.5, 0.5, 2.5, 0.08);

			AABB frostZone = executor.getBoundingBox().inflate(5.0);
			List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, frostZone,
					e -> e != executor && e.isAlive());
			for (LivingEntity target : targets) {
				target.setTicksFrozen(200);
				target.hurt(level.damageSources().freeze(), 5.0f);
				target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 100, 3)); // Slowness IV
			}
		}
	}
}

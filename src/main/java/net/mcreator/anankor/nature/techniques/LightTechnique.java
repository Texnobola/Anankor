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
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

import java.util.Map;
import java.util.List;

public class LightTechnique implements INatureTechnique {

	@Override
	public String getName() {
		return "Luminescent Burst";
	}

	@Override
	public String getDescription() {
		return "Radiates a corona of astral light, searing undead creatures and purging afflictions.";
	}

	@Override
	public int getCost() {
		return 22;
	}

	@Override
	public void execute(LivingEntity executor, Vec3 targetPosition) {
		// Buff caster
		executor.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 120, 0));
		executor.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 200, 0));

		// Sound
		executor.level().playSound(null, executor.getX(), executor.getY(), executor.getZ(),
				SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.85f, 1.3f);

		if (executor.level() instanceof ServerLevel level) {
			// Radiant aura particles
			level.sendParticles(ParticleTypes.END_ROD, executor.getX(), executor.getY() + 1.0, executor.getZ(),
					35, 1.5, 1.5, 1.5, 0.08);
			level.sendParticles(ParticleTypes.GLOW, executor.getX(), executor.getY() + 1.0, executor.getZ(),
					25, 2.0, 1.0, 2.0, 0.05);

			AABB burstArea = executor.getBoundingBox().inflate(6.0);
			List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, burstArea,
					e -> e != executor && e.isAlive());
			for (LivingEntity target : targets) {
				// Extra devastation against undead
				if (target.isInvertedHealAndHarm()) {
					target.hurt(level.damageSources().magic(), 12.0f);
					target.igniteForSeconds(4.0f);
				} else {
					target.hurt(level.damageSources().magic(), 4.5f);
				}
				target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 100, 0));
			}
		}

		// VFX-Weaver trigger
		if (executor instanceof ServerPlayer player) {
			VFXAPI.sendEffect(player, Identifier.fromNamespaceAndPath("anankor", "luminescent_burst"),
					player.position(), Map.of(), null);
		}
	}
}

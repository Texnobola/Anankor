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

		// Sound - Divine invocation & crystal resonant chimes
		executor.level().playSound(null, executor.getX(), executor.getY(), executor.getZ(),
				SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.95f, 1.3f);
		executor.level().playSound(null, executor.getX(), executor.getY(), executor.getZ(),
				SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.2f, 1.4f);
		executor.level().playSound(null, executor.getX(), executor.getY(), executor.getZ(),
				SoundEvents.ILLUSIONER_PREPARE_MIRROR, SoundSource.PLAYERS, 0.8f, 1.2f);

		if (executor.level() instanceof ServerLevel level) {
			// Trigger PAL Player Animation across network
			PacketDistributor.sendToPlayersTrackingEntityAndSelf(executor,
					new PlayPlayerAnimationMessage(executor.getId(), "solar_corona", true, true));

			// Radiant aura particles
			level.sendParticles(ParticleTypes.END_ROD, executor.getX(), executor.getY() + 1.0, executor.getZ(),
					40, 1.8, 1.8, 1.8, 0.08);
			level.sendParticles(ParticleTypes.GLOW, executor.getX(), executor.getY() + 1.0, executor.getZ(),
					30, 2.2, 1.2, 2.2, 0.05);

			// === GeckoLib VFX: Celestial Solar Corona burst ===
			VfxSpawner.spawnBurst(executor, "light", "corona_burst", 2.2f, 17);

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
	}
}

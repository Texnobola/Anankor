package net.mcreator.anankor.nature.techniques;

import net.mcreator.anankor.network.AbyssalShroudEffectMessage;
import net.mcreator.anankor.network.PlayPlayerAnimationMessage;
import net.mcreator.anankor.vfx.VfxSpawner;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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

public class DarkTechnique implements INatureTechnique {

	@Override
	public String getName() {
		return "Abyssal Shroud";
	}

	@Override
	public String getDescription() {
		return "Envelops the vessel in devouring void flux, cloaking into shadows while cursing adversaries.";
	}

	@Override
	public int getCost() {
		return 24;
	}

	@Override
	public int getCooldownTicks() {
		return 80;
	}

	@Override
	public TechniqueCategory getCategory() {
		return TechniqueCategory.WARD;
	}

	@Override
	public void execute(LivingEntity executor, Vec3 targetPosition) {
		// Buff caster with invisibility and stealth speed
		executor.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 140, 0));
		executor.addEffect(new MobEffectInstance(MobEffects.SPEED, 140, 0));

		// Sound - Ominous void whisper, shrieker cry & soul sand collapse
		executor.level().playSound(null, executor.getX(), executor.getY(), executor.getZ(),
				SoundEvents.WITHER_SHOOT, SoundSource.PLAYERS, 0.85f, 0.6f);
		executor.level().playSound(null, executor.getX(), executor.getY(), executor.getZ(),
				SoundEvents.SCULK_SHRIEKER_SHRIEK, SoundSource.PLAYERS, 0.75f, 0.7f);
		executor.level().playSound(null, executor.getX(), executor.getY(), executor.getZ(),
				SoundEvents.SOUL_SAND_BREAK, SoundSource.PLAYERS, 1.0f, 0.5f);

		if (executor.level() instanceof ServerLevel level) {
			// Trigger PAL Player Animation across network
			PacketDistributor.sendToPlayersTrackingEntityAndSelf(executor,
					new PlayPlayerAnimationMessage(executor.getId(), "abyssal_sink", true, true));

			// Shadow smoke and soul particles
			level.sendParticles(ParticleTypes.SQUID_INK, executor.getX(), executor.getY() + 0.8, executor.getZ(),
					35, 1.4, 0.8, 1.4, 0.05);
			level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, executor.getX(), executor.getY() + 0.5, executor.getZ(),
					20, 1.2, 0.5, 1.2, 0.04);
			level.sendParticles(ParticleTypes.SCULK_SOUL, executor.getX(), executor.getY() + 0.3, executor.getZ(),
					15, 1.0, 0.3, 1.0, 0.03);

			// === GeckoLib VFX: Grounded Abyssal Sink rift & tendrils ===
			VfxSpawner.spawnAt(executor, "dark", "abyssal_sink",
					executor.getX(), executor.getY() + 0.05, executor.getZ(),
					executor.getYRot(), 0.0f,
					1.8f, 18);

			// === Post-Effect Shader: Astral Monochrome Wireframe Vision ===
			if (executor instanceof ServerPlayer serverPlayer) {
				PacketDistributor.sendToPlayer(serverPlayer, new AbyssalShroudEffectMessage(140));
			}

			AABB shroudArea = executor.getBoundingBox().inflate(5.5);
			List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, shroudArea,
					e -> e != executor && e.isAlive());
			for (LivingEntity target : targets) {
				target.hurt(level.damageSources().wither(), 5.5f);
				target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 100, 1));
				target.addEffect(new MobEffectInstance(MobEffects.WITHER, 80, 0));
			}
		}
	}
}

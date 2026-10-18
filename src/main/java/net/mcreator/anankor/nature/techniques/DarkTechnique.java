package net.mcreator.anankor.nature.techniques;

import net.mcreator.anankor.vfx.VfxSpawner;
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
	public void execute(LivingEntity executor, Vec3 targetPosition) {
		// Buff caster with invisibility and stealth speed
		executor.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 140, 0));
		executor.addEffect(new MobEffectInstance(MobEffects.SPEED, 140, 0));

		// Sound
		executor.level().playSound(null, executor.getX(), executor.getY(), executor.getZ(),
				SoundEvents.WITHER_SHOOT, SoundSource.PLAYERS, 0.8f, 0.7f);

		if (executor.level() instanceof ServerLevel level) {
			// Shadow smoke and soul particles
			level.sendParticles(ParticleTypes.SQUID_INK, executor.getX(), executor.getY() + 0.8, executor.getZ(),
					30, 1.2, 0.8, 1.2, 0.05);
			level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, executor.getX(), executor.getY() + 0.5, executor.getZ(),
					15, 1.0, 0.5, 1.0, 0.04);

			// === GeckoLib VFX: Devouring void shroud ===
			VfxSpawner.spawnBurst(executor, "dark", "shroud_expand", 1.8f, 18);

			// === Post-Effect Shader: Astral Monochrome Wireframe Vision ===
			if (executor instanceof ServerPlayer serverPlayer) {
				net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(serverPlayer, new net.mcreator.anankor.network.AbyssalShroudEffectMessage(140));
			}

			AABB shroudArea = executor.getBoundingBox().inflate(5.5);
			List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, shroudArea,
					e -> e != executor && e.isAlive());
			for (LivingEntity target : targets) {
				target.hurt(level.damageSources().wither(), 5.5f);
				target.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 100, 0));
				target.addEffect(new MobEffectInstance(MobEffects.WITHER, 80, 0));
			}
		}
	}
}

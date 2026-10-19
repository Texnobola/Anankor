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

import net.mcreator.anankor.nature.TechniqueCategory;

import java.util.List;

public class ElectricityTechnique implements INatureTechnique {

	@Override
	public String getName() {
		return "Arc Step";
	}

	@Override
	public String getDescription() {
		return "Converts the body into ionized flux, zipping forward and electrocuting all in your trajectory.";
	}

	@Override
	public int getCost() {
		return 20;
	}

	@Override
	public int getCooldownTicks() {
		return 40;
	}

	@Override
	public TechniqueCategory getCategory() {
		return TechniqueCategory.MOBILITY;
	}

	@Override
	public void execute(LivingEntity executor, Vec3 targetPosition) {
		Vec3 look = executor.getLookAngle();
		Vec3 start = executor.position();

		// Propel rapidly forward
		Vec3 dashVel = new Vec3(look.x * 2.2, Math.max(0.1, look.y * 1.2), look.z * 2.2);
		executor.setDeltaMovement(dashVel);
		executor.syncVelocity = true;

		// Sound - High-voltage discharge & lightning crackle
		executor.level().playSound(null, executor.getX(), executor.getY(), executor.getZ(),
				SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 0.85f, 1.8f);
		executor.level().playSound(null, executor.getX(), executor.getY(), executor.getZ(),
				SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.PLAYERS, 0.7f, 1.6f);

		if (executor.level() instanceof ServerLevel level) {
			// Trigger PAL Player Animation across network
			PacketDistributor.sendToPlayersTrackingEntityAndSelf(executor,
					new PlayPlayerAnimationMessage(executor.getId(), "lightning_surge", true, true));

			// Line of electric sparks & ionized purple plasma trails
			for (int i = 0; i < 20; i++) {
				double progress = i / 20.0;
				double px = start.x + look.x * progress * 6.0;
				double py = start.y + look.y * progress * 6.0 + 0.8;
				double pz = start.z + look.z * progress * 6.0;
				level.sendParticles(ParticleTypes.ELECTRIC_SPARK, px, py, pz, 4, 0.15, 0.15, 0.15, 0.08);
				level.sendParticles(ParticleTypes.PORTAL, px, py, pz, 2, 0.1, 0.1, 0.1, 0.1);
			}

			// === GeckoLib VFX: Overhauled Anime Jagged Blitz & Railgun Lance dash trail
			// ===
			VfxSpawner.spawnTrail(executor, "electricity", "lightning_dash", 3, 2.0, 1.4f, 13);

			// Strike entities along the trajectory
			AABB strikeBox = executor.getBoundingBox().inflate(2.5).move(look.scale(3.0));
			List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, strikeBox,
					e -> e != executor && e.isAlive());
			for (LivingEntity target : targets) {
				target.hurt(level.damageSources().lightningBolt(), 7.0f);
				target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 2)); // Paralyzing shock
			}
		}
	}
}

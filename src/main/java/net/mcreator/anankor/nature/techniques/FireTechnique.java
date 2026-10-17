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

import java.util.Map;
import java.util.List;

public class FireTechnique implements INatureTechnique {

	@Override
	public String getName() {
		return "Thermal Jet";
	}

	@Override
	public String getDescription() {
		return "Erupts directional thermal thrust, launching forward with a concussive flame cone.";
	}

	@Override
	public int getCost() {
		return 15;
	}

	@Override
	public void execute(LivingEntity executor, Vec3 targetPosition) {
		Vec3 look = executor.getLookAngle();

		// 1. Directional velocity propulsion
		Vec3 currentVelocity = executor.getDeltaMovement();
		executor.setDeltaMovement(currentVelocity.add(look.x * 1.45, Math.max(0.15, look.y * 0.75 + 0.15), look.z * 1.45));
		executor.hurtMarked = true;

		// 2. Audio & Visuals
		executor.level().playSound(null, executor.getX(), executor.getY(), executor.getZ(),
				SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 0.85f, 1.05f);

		if (executor.level() instanceof ServerLevel level) {
			// Spawn directional flame and smoke burst
			level.sendParticles(ParticleTypes.FLAME, executor.getX(), executor.getEyeY() - 0.2, executor.getZ(),
					25, look.x * 0.4, look.y * 0.4, look.z * 0.4, 0.12);
			level.sendParticles(ParticleTypes.LAVA, executor.getX(), executor.getY() + 0.2, executor.getZ(),
					5, 0.2, 0.2, 0.2, 0.05);

			// 3. Concussive cone damage and ignition
			AABB targetBox = executor.getBoundingBox().inflate(3.0).move(look.scale(2.0));
			List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, targetBox, e -> e != executor && e.isAlive());
			for (LivingEntity target : targets) {
				target.hurt(level.damageSources().mobAttack(executor), 7.0f);
				target.igniteForSeconds(3.0f);
				target.knockback(0.9, -look.x, -look.z);
			}
		}

		// 4. VFX-Weaver Cinematic Screen & World Effects
		if (executor instanceof ServerPlayer player) {
			VFXAPI.sendEffect(player, Identifier.fromNamespaceAndPath("anankor", "thermal_jet"), player.position(), Map.of(), null);
		}
	}
}

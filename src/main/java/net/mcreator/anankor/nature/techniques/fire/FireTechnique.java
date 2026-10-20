package net.mcreator.anankor.nature.techniques.fire;

import net.mcreator.anankor.nature.techniques.INatureTechnique;
import net.mcreator.anankor.vfx.VfxSpawner;
import net.mcreator.anankor.network.PlayPlayerAnimationMessage;
import net.neoforged.neoforge.network.PacketDistributor;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.particles.ParticleTypes;

import java.util.List;

import net.mcreator.anankor.AnankorMod;
import net.mcreator.anankor.nature.NatureType;
import net.mcreator.anankor.nature.TechniqueCategory;
import net.minecraft.resources.Identifier;

public class FireTechnique implements INatureTechnique {

	public static final Identifier ID = Identifier.fromNamespaceAndPath(AnankorMod.MODID, "fire/thermal_jet");

	@Override
	public Identifier getId() {
		return ID;
	}

	@Override
	public NatureType getPrimaryNature() {
		return NatureType.FIRE;
	}

	@Override
	public String getName() {
		return "Thermal Jet";
	}

	@Override
	public String getDescription() {
		return "Erupts directional thermal thrust, launching forward with dual-handed rocket flame bursts.";
	}

	@Override
	public int getCost() {
		return 15;
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

		// 1. Directional velocity propulsion
		Vec3 currentVelocity = executor.getDeltaMovement();
		executor.setDeltaMovement(
				currentVelocity.add(look.x * 1.55, Math.max(0.18, look.y * 0.8 + 0.18), look.z * 1.55));
		executor.syncVelocity = true;

		// 2. Audio & Visuals
		executor.level().playSound(null, executor.getX(), executor.getY(), executor.getZ(),
				SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 1.0f, 1.2f);
		executor.level().playSound(null, executor.getX(), executor.getY(), executor.getZ(),
				SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 0.9f, 0.95f);

		if (executor.level() instanceof ServerLevel level) {
			// Trigger PAL Player Animation across network (seen in first-person & by other players)
			PacketDistributor.sendToPlayersTrackingEntityAndSelf(executor,
					new PlayPlayerAnimationMessage(executor.getId(), "fire_jet"));

			// === GeckoLib VFX: Dual Hand Rocket Bursts ===
			VfxSpawner.spawnAt(executor, "fire", "jet_burst",
					executor.getX(), executor.getY() + 0.1, executor.getZ(),
					executor.getYRot(), executor.getXRot(),
					1.25f, 17);

			// Calculate hand exhaust vectors for realistic dual-thruster particles
			Vec3 right = look.cross(new Vec3(0, 1, 0)).normalize();
			Vec3 eye = executor.getEyePosition();
			Vec3 leftHand = eye.subtract(right.scale(0.42)).subtract(look.scale(0.25)).add(0, -0.35, 0);
			Vec3 rightHand = eye.add(right.scale(0.42)).subtract(look.scale(0.25)).add(0, -0.35, 0);
			Vec3 exhaustDir = look.scale(-0.45);

			// Left hand rocket exhaust
			level.sendParticles(ParticleTypes.FLAME, leftHand.x, leftHand.y, leftHand.z,
					16, exhaustDir.x, exhaustDir.y, exhaustDir.z, 0.09);
			level.sendParticles(ParticleTypes.LAVA, leftHand.x, leftHand.y, leftHand.z,
					3, exhaustDir.x * 0.5, exhaustDir.y * 0.5, exhaustDir.z * 0.5, 0.05);

			// Right hand rocket exhaust
			level.sendParticles(ParticleTypes.FLAME, rightHand.x, rightHand.y, rightHand.z,
					16, exhaustDir.x, exhaustDir.y, exhaustDir.z, 0.09);
			level.sendParticles(ParticleTypes.LAVA, rightHand.x, rightHand.y, rightHand.z,
					3, exhaustDir.x * 0.5, exhaustDir.y * 0.5, exhaustDir.z * 0.5, 0.05);

			// Forward concussion wave
			level.sendParticles(ParticleTypes.SMOKE, executor.getX(), executor.getY() + 0.5, executor.getZ(),
					10, -look.x * 0.2, 0.1, -look.z * 0.2, 0.04);

			// 3. Concussive cone damage and ignition
			AABB targetBox = executor.getBoundingBox().inflate(3.0).move(look.scale(2.0));
			List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, targetBox,
					e -> e != executor && e.isAlive());
			for (LivingEntity target : targets) {
				target.hurt(level.damageSources().mobAttack(executor), 7.0f);
				target.igniteForSeconds(3.0f);
				target.knockback(0.9, -look.x, -look.z, level.damageSources().mobAttack(executor), 1.0f);
			}
		}
	}
}

package net.mcreator.anankor.nature.techniques.ice;

import net.mcreator.anankor.nature.techniques.INatureTechnique;
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

import net.mcreator.anankor.AnankorMod;
import net.mcreator.anankor.nature.NatureType;
import net.mcreator.anankor.nature.TechniqueCategory;
import net.minecraft.resources.Identifier;

import java.util.List;

public class IceTechnique implements INatureTechnique {

	public static final Identifier ID = Identifier.fromNamespaceAndPath(AnankorMod.MODID, "ice/glacial_anchor");

	@Override
	public Identifier getId() {
		return ID;
	}

	@Override
	public NatureType getPrimaryNature() {
		return NatureType.ICE;
	}

	@Override
	public String getName() {
		return "Glacial Anchor";
	}

	@Override
	public String getDescription() {
		return "Slams both hands into the earth with gathered frost energy, erupting jagged crystalline glacial spires that freeze and anchor nearby foes.";
	}

	@Override
	public int getCost() {
		return 22;
	}

	@Override
	public int getCooldownTicks() {
		return 60;
	}

	@Override
	public TechniqueCategory getCategory() {
		return TechniqueCategory.STRIKE;
	}

	@Override
	public void execute(LivingEntity executor, Vec3 targetPosition) {
		// Audio: Frost buildup and shattering glacial eruption
		executor.level().playSound(null, executor.getX(), executor.getY(), executor.getZ(),
				SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.2f, 1.6f);
		executor.level().playSound(null, executor.getX(), executor.getY(), executor.getZ(),
				SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.1f, 0.75f);
		executor.level().playSound(null, executor.getX(), executor.getY(), executor.getZ(),
				SoundEvents.AMETHYST_BLOCK_BREAK, SoundSource.PLAYERS, 1.2f, 0.85f);
		executor.level().playSound(null, executor.getX(), executor.getY(), executor.getZ(),
				SoundEvents.POWDER_SNOW_STEP, SoundSource.PLAYERS, 1.0f, 0.6f);

		if (executor.level() instanceof ServerLevel level) {
			// Trigger PAL Player Animation across network (seen in first-person & by other players)
			PacketDistributor.sendToPlayersTrackingEntityAndSelf(executor,
					new PlayPlayerAnimationMessage(executor.getId(), "ice_slam"));

			// === GeckoLib VFX: Ground Slam Glacial Monolith & Spires ===
			VfxSpawner.spawnAt(executor, "ice_anchor", "ground_slam",
					executor.getX(), executor.getY() + 0.05, executor.getZ(),
					executor.getYRot(), 0.0f,
					1.5f, 30);

			// Radial frost crystallization shockwave particles
			level.sendParticles(ParticleTypes.SNOWFLAKE, executor.getX(), executor.getY() + 0.3, executor.getZ(),
					55, 3.0, 0.6, 3.0, 0.1);
			level.sendParticles(ParticleTypes.ITEM_SNOWBALL, executor.getX(), executor.getY() + 0.4, executor.getZ(),
					25, 2.5, 0.5, 2.5, 0.12);

			// Freeze and anchor all nearby enemies
			AABB frostZone = executor.getBoundingBox().inflate(5.5);
			List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, frostZone,
					e -> e != executor && e.isAlive());
			for (LivingEntity target : targets) {
				target.setTicksFrozen(240);
				target.hurt(level.damageSources().freeze(), 7.0f);
				target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 120, 3)); // Slowness IV
				target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 80, 1)); // Weakness II

				// Pin to ground / anchor
				Vec3 curVel = target.getDeltaMovement();
				target.setDeltaMovement(curVel.x * 0.1, Math.min(0.0, curVel.y), curVel.z * 0.1);
				target.syncVelocity = true;
			}
		}
	}
}

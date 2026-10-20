package net.mcreator.anankor.nature.techniques.ice;

import net.mcreator.anankor.AnankorMod;
import net.mcreator.anankor.nature.NatureType;
import net.mcreator.anankor.nature.TechniqueCategory;
import net.mcreator.anankor.nature.techniques.FunctionalTechnique;
import net.mcreator.anankor.nature.techniques.INatureTechnique;
import net.mcreator.anankor.network.PlayPlayerAnimationMessage;
import net.mcreator.anankor.vfx.VfxSpawner;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Dedicated registration container and factory for all Ice Nature techniques.
 */
public final class IceTechniques {

	public static final Identifier ID_FROST_SKATER = Identifier.fromNamespaceAndPath(AnankorMod.MODID,
			"ice/frost_skater");
	public static final Identifier ID_GLACIAL_ANCHOR = Identifier.fromNamespaceAndPath(AnankorMod.MODID,
			"ice/glacial_anchor");
	public static final Identifier ID_PERMAFROST_BARRIER = Identifier.fromNamespaceAndPath(AnankorMod.MODID,
			"ice/permafrost_barrier");
	public static final Identifier ID_ABSOLUTE_ZERO = Identifier.fromNamespaceAndPath(AnankorMod.MODID,
			"ice/absolute_zero");

	public static final INatureTechnique FROST_SKATER = new FunctionalTechnique(
			ID_FROST_SKATER,
			NatureType.ICE,
			"Frost Skater",
			"Forms smooth frost beneath feet, gliding forward with crisp agility.",
			15,
			35,
			TechniqueCategory.MOBILITY,
			(executor, target) -> {
				Vec3 look = executor.getLookAngle();
				executor.setDeltaMovement(executor.getDeltaMovement().add(look.x * 1.7, 0.1, look.z * 1.7));
				executor.syncVelocity = true;
				executor.level().playSound(null, executor.getX(), executor.getY(), executor.getZ(),
						SoundEvents.SNOW_GOLEM_SHOOT, SoundSource.PLAYERS, 1.0f, 1.4f);
				if (executor.level() instanceof ServerLevel sl) {
					sl.sendParticles(ParticleTypes.SNOWFLAKE, executor.getX(), executor.getY() + 0.2,
							executor.getZ(), 20, 0.5, 0.2, 0.5, 0.05);
				}
			});

	public static final INatureTechnique GLACIAL_ANCHOR = new IceTechnique();

	public static final INatureTechnique PERMAFROST_BARRIER = new FunctionalTechnique(
			ID_PERMAFROST_BARRIER,
			NatureType.ICE,
			"Permafrost Barrier",
			"Covers the caster in crystalline frost rime, granting Resistance and chilling attackers.",
			25,
			70,
			TechniqueCategory.WARD,
			(executor, target) -> {
				executor.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 140, 1));
				executor.level().playSound(null, executor.getX(), executor.getY(), executor.getZ(),
						SoundEvents.GLASS_PLACE, SoundSource.PLAYERS, 1.2f, 0.8f);
				if (executor.level() instanceof ServerLevel sl) {
					// Trigger PAL Player Animation across network
					PacketDistributor.sendToPlayersTrackingEntityAndSelf(executor,
							new PlayPlayerAnimationMessage(executor.getId(), "ice_barrier"));
					// GeckoLib 3D Orbiting Aegis VFX attached to caster for full duration (140 ticks = 7s)
					VfxSpawner.spawnAttached(executor, "ice_barrier", "orbit", 1.1f, 140, 0.2);

					sl.sendParticles(ParticleTypes.ITEM_SNOWBALL, executor.getX(), executor.getY() + 1.0,
							executor.getZ(), 25, 0.6, 0.6, 0.6, 0.1);
				}
			});

	public static final INatureTechnique ABSOLUTE_ZERO = new FunctionalTechnique(
			ID_ABSOLUTE_ZERO,
			NatureType.ICE,
			"Absolute Zero",
			"Channels sub-zero crystallization, flash-freezing all surrounding entities.",
			50,
			120,
			TechniqueCategory.EXTENSION,
			(executor, target) -> {
				executor.level().playSound(null, executor.getX(), executor.getY(), executor.getZ(),
						SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.4f, 0.6f);
				executor.level().playSound(null, executor.getX(), executor.getY(), executor.getZ(),
						SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.5f, 1.8f);
				if (executor.level() instanceof ServerLevel sl) {
					// Trigger PAL Player Animation across network
					PacketDistributor.sendToPlayersTrackingEntityAndSelf(executor,
							new PlayPlayerAnimationMessage(executor.getId(), "absolute_zero"));
					// GeckoLib 3D Blooming Glacial Lotus Nova VFX (70 ticks = 3.5s)
					VfxSpawner.spawnAt(executor, "ice_absolute_zero", "absolute_zero",
							executor.getX(), executor.getY() + 0.05, executor.getZ(),
							executor.getYRot(), 0.0f,
							1.8f, 70);

					sl.sendParticles(ParticleTypes.SNOWFLAKE, executor.getX(), executor.getY() + 1.0,
							executor.getZ(), 100, 4.0, 1.5, 4.0, 0.1);
					AABB area = executor.getBoundingBox().inflate(8.0);
					for (LivingEntity e : executor.level().getEntitiesOfClass(LivingEntity.class, area)) {
						if (e != executor) {
							e.hurt(executor.damageSources().freeze(), 12.0f);
							e.setTicksFrozen(200);
							e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 100, 3));
						}
					}
				}
			});

	private IceTechniques() {
	}

	public static List<INatureTechnique> getTechniques() {
		List<INatureTechnique> list = new ArrayList<>();
		list.add(FROST_SKATER);          // Slot 0 / Mobility
		list.add(GLACIAL_ANCHOR);        // Slot 1 / Strike
		list.add(PERMAFROST_BARRIER);    // Slot 2 / Ward
		list.add(ABSOLUTE_ZERO);         // Slot 3 / Extension
		return Collections.unmodifiableList(list);
	}
}

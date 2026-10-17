package net.mcreator.anankor.procedures;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;

import net.mcreator.anankor.network.AnankorModVariables;
import net.mcreator.anankor.nature.NatureType;
import net.mcreator.anankor.init.AnankorModMobEffects;

public class ANTestFluxAbilityProcedure {
	public static void execute(Entity entity) {
		if (entity == null)
			return;

		// 1. Block ability execution if the entity is currently suffering from Burnout
		if (entity instanceof LivingEntity living && living.hasEffect(AnankorModMobEffects.BURNOUT)) {
			if (entity instanceof ServerPlayer player) {
				player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.FIRE_EXTINGUISH,
						SoundSource.PLAYERS, 0.6f, 1.4f);
				player.sendSystemMessage(
						Component.literal("§c[BURNOUT] Your Flux channels are burned out! Unable to channel."), true);
			}
			return;
		}

		AnankorModVariables.PlayerVariables vars = entity.getData(AnankorModVariables.PLAYER_VARIABLES);

		// 2. Cannot initiate an ability if Flux is already depleted (<= 0)
		if (vars.flux <= 0) {
			if (entity instanceof ServerPlayer player) {
				player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.VILLAGER_NO,
						SoundSource.PLAYERS, 0.5f, 1.0f);
				player.sendSystemMessage(Component.literal("§cInsufficient Flux!"), true);
			}
			return;
		}

		// 3. Deduct Flux (permits overdraft into negative values to trigger Burnout
		// scaling)
		double previousFlux = vars.flux;
		double cost = 20.0;
		vars.flux = previousFlux - cost;
		vars.markSyncDirty();

		NatureType nature = NatureType.fromString(vars.Nature);

		if (entity instanceof ServerPlayer player) {
			if (vars.flux <= 0) {
				// Overdraft triggered - warning audio and message
				player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.GENERIC_BURN,
						SoundSource.PLAYERS, 0.8f, 0.8f);
				player.sendSystemMessage(Component.literal(
						"§eFlux: §c" + (int) vars.flux + " §4[OVERDRAFT IMMINENT] §7(" + nature.getDisplayName() + ")"),
						true);
			} else {
				player.sendSystemMessage(
						Component.literal("§bFlux: " + (int) vars.flux + " §7(" + nature.getDisplayName() + ")"), true);
			}
		}
	}
}

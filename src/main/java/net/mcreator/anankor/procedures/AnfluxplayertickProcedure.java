package net.mcreator.anankor.procedures;

import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.Event;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.effect.MobEffectInstance;

import net.mcreator.anankor.network.AnankorModVariables;
import net.mcreator.anankor.init.AnankorModMobEffects;

import javax.annotation.Nullable;

@EventBusSubscriber
public class AnfluxplayertickProcedure {
	@SubscribeEvent
	public static void onPlayerTick(PlayerTickEvent.Post event) {
		execute(event, event.getEntity());
	}

	public static void execute(Entity entity) {
		execute(null, entity);
	}

	private static void execute(@Nullable Event event, Entity entity) {
		if (entity == null)
			return;

		if (entity instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
			net.mcreator.anankor.flora.FluxMeditationHandler.handlePlayerTick(serverPlayer);
		}
		if (entity instanceof LivingEntity _livEnt0 && _livEnt0.hasEffect(AnankorModMobEffects.BURNOUT)) {
			{
				AnankorModVariables.PlayerVariables _vars = entity.getData(AnankorModVariables.PLAYER_VARIABLES);
				_vars.FluxRegenTimer = 0;
				_vars.markSyncDirty();
			}
		} else {
			if (entity.getData(AnankorModVariables.PLAYER_VARIABLES).flux <= 0) {
				double currentFlux = entity.getData(AnankorModVariables.PLAYER_VARIABLES).flux;
				int burnoutDuration;
				if (currentFlux >= -20) {
					burnoutDuration = 1200;
				} else if (currentFlux >= -1100) {
					burnoutDuration = (int) (1200 + ((0 - currentFlux) - 20) * 10);
				} else {
					burnoutDuration = 12000;
				}
				if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide()) {
					_entity.addEffect(new MobEffectInstance(AnankorModMobEffects.BURNOUT, burnoutDuration, 0));
					if (_entity instanceof net.minecraft.server.level.ServerPlayer player) {
						player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
								net.minecraft.sounds.SoundEvents.SHIELD_BREAK.value(),
								net.minecraft.sounds.SoundSource.PLAYERS, 0.9f, 0.7f);
						int totalSeconds = burnoutDuration / 20;
						String formattedTime = totalSeconds >= 60
								? (totalSeconds / 60) + "m " + (totalSeconds % 60) + "s"
								: totalSeconds + "s";
						player.sendSystemMessage(net.minecraft.network.chat.Component
								.literal("§4[BURNOUT] Flux channels collapsed! Recovery: §e" + formattedTime), true);
					}
				}
				{
					AnankorModVariables.PlayerVariables _vars = entity.getData(AnankorModVariables.PLAYER_VARIABLES);
					_vars.flux = 0;
					_vars.FluxRegenTimer = 0;
					_vars.markSyncDirty();
				}
			} else {
				if (entity.getData(AnankorModVariables.PLAYER_VARIABLES).flux < entity
						.getData(AnankorModVariables.PLAYER_VARIABLES).maxflux) {
					{
						AnankorModVariables.PlayerVariables _vars = entity
								.getData(AnankorModVariables.PLAYER_VARIABLES);
						_vars.FluxRegenTimer = entity.getData(AnankorModVariables.PLAYER_VARIABLES).FluxRegenTimer + 1;
						_vars.markSyncDirty();
					}
					if (entity.getData(AnankorModVariables.PLAYER_VARIABLES).FluxRegenTimer >= 20) {
						{
							AnankorModVariables.PlayerVariables _vars = entity
									.getData(AnankorModVariables.PLAYER_VARIABLES);
							_vars.flux = entity.getData(AnankorModVariables.PLAYER_VARIABLES).flux + 1;
							_vars.FluxRegenTimer = 0;
							_vars.markSyncDirty();
						}
					}
				}
			}
			if (entity.getData(AnankorModVariables.PLAYER_VARIABLES).flux >= entity
					.getData(AnankorModVariables.PLAYER_VARIABLES).maxflux) {
				{
					AnankorModVariables.PlayerVariables _vars = entity.getData(AnankorModVariables.PLAYER_VARIABLES);
					_vars.flux = entity.getData(AnankorModVariables.PLAYER_VARIABLES).maxflux;
					_vars.FluxRegenTimer = 0;
					_vars.markSyncDirty();
				}
			}
		}
	}
}
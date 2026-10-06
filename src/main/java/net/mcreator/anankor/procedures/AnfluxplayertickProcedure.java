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
		if (entity instanceof LivingEntity _livEnt0 && _livEnt0.hasEffect(AnankorModMobEffects.BURNOUT)) {
			{
				AnankorModVariables.PlayerVariables _vars = entity.getData(AnankorModVariables.PLAYER_VARIABLES);
				_vars.FluxRegenTimer = 0;
				_vars.markSyncDirty();
			}
		} else {
			if (entity.getData(AnankorModVariables.PLAYER_VARIABLES).flux <= 0) {
				if (entity.getData(AnankorModVariables.PLAYER_VARIABLES).flux >= -20) {
					if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide())
						_entity.addEffect(new MobEffectInstance(AnankorModMobEffects.BURNOUT, 1200, 0));
				} else {
					if (entity.getData(AnankorModVariables.PLAYER_VARIABLES).flux >= -1100) {
						if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide())
							_entity.addEffect(new MobEffectInstance(AnankorModMobEffects.BURNOUT, (int) (1200 + ((0 - entity.getData(AnankorModVariables.PLAYER_VARIABLES).flux) - 20) * 10), 0));
					} else {
						if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide())
							_entity.addEffect(new MobEffectInstance(AnankorModMobEffects.BURNOUT, 12000, 0));
					}
				}
				{
					AnankorModVariables.PlayerVariables _vars = entity.getData(AnankorModVariables.PLAYER_VARIABLES);
					_vars.flux = 0;
					_vars.FluxRegenTimer = 0;
					_vars.markSyncDirty();
				}
			} else {
				if (entity.getData(AnankorModVariables.PLAYER_VARIABLES).flux < entity.getData(AnankorModVariables.PLAYER_VARIABLES).maxflux) {
					{
						AnankorModVariables.PlayerVariables _vars = entity.getData(AnankorModVariables.PLAYER_VARIABLES);
						_vars.FluxRegenTimer = entity.getData(AnankorModVariables.PLAYER_VARIABLES).FluxRegenTimer + 1;
						_vars.markSyncDirty();
					}
					if (entity.getData(AnankorModVariables.PLAYER_VARIABLES).FluxRegenTimer >= 20) {
						{
							AnankorModVariables.PlayerVariables _vars = entity.getData(AnankorModVariables.PLAYER_VARIABLES);
							_vars.flux = entity.getData(AnankorModVariables.PLAYER_VARIABLES).flux + 1;
							_vars.FluxRegenTimer = 0;
							_vars.markSyncDirty();
						}
					}
				}
			}
			if (entity.getData(AnankorModVariables.PLAYER_VARIABLES).flux >= entity.getData(AnankorModVariables.PLAYER_VARIABLES).maxflux) {
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
package net.mcreator.anankor.procedures;

import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;

import net.mcreator.anankor.network.AnankorModVariables;

public class ANTestFluxAbilityProcedure {
	public static void execute(Entity entity) {
		if (entity == null)
			return;
		if (entity.getData(AnankorModVariables.PLAYER_VARIABLES).flux >= 20) {
			{
				AnankorModVariables.PlayerVariables _vars = entity.getData(AnankorModVariables.PLAYER_VARIABLES);
				_vars.flux = entity.getData(AnankorModVariables.PLAYER_VARIABLES).flux - 20;
				_vars.markSyncDirty();
			}
			if (entity instanceof ServerPlayer _player)
				_player.sendSystemMessage(Component.literal(("Flux:" + entity.getData(AnankorModVariables.PLAYER_VARIABLES).flux)), true);
		}
	}
}
package net.mcreator.anankor.procedures;

import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerPlayer;

import net.mcreator.anankor.network.AnankorModVariables;
import net.mcreator.anankor.nature.NatureAbilityDispatcher;

public class ANTestFluxAbilityProcedure {
	public static void execute(Entity entity) {
		if (entity instanceof ServerPlayer player) {
			AnankorModVariables.PlayerVariables vars = player.getData(AnankorModVariables.PLAYER_VARIABLES);
			int selected = (int) vars.selectedTechnique;
			NatureAbilityDispatcher.castSlot(player, selected, false);
		}
	}
}

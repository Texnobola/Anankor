package net.mcreator.anankor.flora;

import net.mcreator.anankor.network.AnankorModVariables;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import net.mcreator.anankor.AnankorMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

public class FluxBerryItem extends Item {

	public FluxBerryItem() {
		super(new Item.Properties()
				.setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(AnankorMod.MODID, "flux_berry")))
				.food(new FoodProperties.Builder()
						.nutrition(2)
						.saturationModifier(0.4f)
						.alwaysEdible()
						.build()));
	}

	@Override
	public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
		ItemStack result = super.finishUsingItem(stack, level, entity);

		if (!level.isClientSide()) {
			AnankorModVariables.PlayerVariables vars = entity.getData(AnankorModVariables.PLAYER_VARIABLES);
			vars.flux = Math.min(vars.maxflux, vars.flux + 20.0);
			vars.markSyncDirty();

			if (entity instanceof ServerPlayer player) {
				player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
						SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.7f, 1.4f);
				player.sendSystemMessage(Component.literal(
						"§b✦ Consumed Flux Berry: +20 Flux §7(" + (int) vars.flux + "/" + (int) vars.maxflux + ")"),
						true);
			}
		}

		return result;
	}
}

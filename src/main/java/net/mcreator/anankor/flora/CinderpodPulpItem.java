package net.mcreator.anankor.flora;

import net.mcreator.anankor.network.AnankorModVariables;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import net.mcreator.anankor.AnankorMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

public class CinderpodPulpItem extends Item {

	public CinderpodPulpItem() {
		super(new Item.Properties()
				.setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(AnankorMod.MODID, "cinderpod_pulp")))
				.food(new FoodProperties.Builder()
						.nutrition(4)
						.saturationModifier(0.6f)
						.alwaysEdible()
						.build()));
	}

	@Override
	public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
		ItemStack result = super.finishUsingItem(stack, level, entity);

		if (!level.isClientSide()) {
			AnankorModVariables.PlayerVariables vars = entity.getData(AnankorModVariables.PLAYER_VARIABLES);
			vars.flux = Math.min(vars.maxflux, vars.flux + 35.0);
			vars.markSyncDirty();

			entity.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 300, 0));

			if (entity instanceof ServerPlayer player) {
				player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
						SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 0.75f, 1.2f);
				player.sendSystemMessage(Component.literal("§6✦ Consumed Cinderpod Pulp: +35 Flux §7(" + (int) vars.flux
						+ "/" + (int) vars.maxflux + ")"), true);
			}
		}

		return result;
	}
}

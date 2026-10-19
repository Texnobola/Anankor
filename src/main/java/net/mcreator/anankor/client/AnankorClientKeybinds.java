package net.mcreator.anankor.client;

import com.mojang.blaze3d.platform.InputConstants;

import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.api.distmarker.Dist;

import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;

import net.mcreator.anankor.network.CastTechniqueSlotMessage;
import net.mcreator.anankor.network.CycleTechniqueMessage;

@EventBusSubscriber(Dist.CLIENT)
public class AnankorClientKeybinds {
	public static final KeyMapping.Category CATEGORY = KeyMapping.Category.GAMEPLAY;

	public static final KeyMapping SLOT_0_MOBILITY = new KeyMapping("key.anankor.slot_0_mobility", InputConstants.KEY_C, CATEGORY);
	public static final KeyMapping SLOT_1_STRIKE = new KeyMapping("key.anankor.slot_1_strike", InputConstants.KEY_G, CATEGORY);
	public static final KeyMapping SLOT_2_WARD = new KeyMapping("key.anankor.slot_2_ward", InputConstants.KEY_X, CATEGORY);
	public static final KeyMapping SLOT_3_EXTENSION = new KeyMapping("key.anankor.slot_3_extension", InputConstants.KEY_Z, CATEGORY);
	public static final KeyMapping OPEN_MENU = new KeyMapping("key.anankor.open_menu", InputConstants.KEY_M, CATEGORY);
	public static final KeyMapping CYCLE_TECHNIQUE = new KeyMapping("key.anankor.cycle_technique", InputConstants.KEY_R, CATEGORY);

	@SubscribeEvent
	public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
		event.register(SLOT_0_MOBILITY);
		event.register(SLOT_1_STRIKE);
		event.register(SLOT_2_WARD);
		event.register(SLOT_3_EXTENSION);
		event.register(OPEN_MENU);
		event.register(CYCLE_TECHNIQUE);
	}

	@SubscribeEvent
	public static void onClientTick(ClientTickEvent.Post event) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.gui.screen() == null && mc.player != null) {
			while (SLOT_0_MOBILITY.consumeClick()) {
				ClientPacketDistributor.sendToServer(new CastTechniqueSlotMessage(0));
			}
			while (SLOT_1_STRIKE.consumeClick()) {
				ClientPacketDistributor.sendToServer(new CastTechniqueSlotMessage(1));
			}
			while (SLOT_2_WARD.consumeClick()) {
				ClientPacketDistributor.sendToServer(new CastTechniqueSlotMessage(2));
			}
			while (SLOT_3_EXTENSION.consumeClick()) {
				ClientPacketDistributor.sendToServer(new CastTechniqueSlotMessage(3));
			}
			while (CYCLE_TECHNIQUE.consumeClick()) {
				ClientPacketDistributor.sendToServer(new CycleTechniqueMessage(1));
			}
			while (OPEN_MENU.consumeClick()) {
				mc.gui.setScreen(new AnankorMenuScreen());
			}
		}
	}
}

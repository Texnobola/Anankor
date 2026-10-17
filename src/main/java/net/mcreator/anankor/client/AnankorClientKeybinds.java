package net.mcreator.anankor.client;

import org.lwjgl.glfw.GLFW;

import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.api.distmarker.Dist;

import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;

import net.mcreator.anankor.network.CycleTechniqueMessage;

@EventBusSubscriber(Dist.CLIENT)
public class AnankorClientKeybinds {
	public static final KeyMapping CYCLE_TECHNIQUE = new KeyMapping("key.anankor.cycle_technique", GLFW.GLFW_KEY_R,
			KeyMapping.Category.GAMEPLAY);
	public static final KeyMapping OPEN_MENU = new KeyMapping("key.anankor.open_menu", GLFW.GLFW_KEY_V,
			KeyMapping.Category.GAMEPLAY);

	@SubscribeEvent
	public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
		event.register(CYCLE_TECHNIQUE);
		event.register(OPEN_MENU);
	}

	@SubscribeEvent
	public static void onClientTick(ClientTickEvent.Post event) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.screen == null && mc.player != null) {
			while (CYCLE_TECHNIQUE.consumeClick()) {
				ClientPacketDistributor.sendToServer(new CycleTechniqueMessage(1));
			}
			while (OPEN_MENU.consumeClick()) {
				mc.setScreen(new AnankorMenuScreen());
			}
		}
	}
}

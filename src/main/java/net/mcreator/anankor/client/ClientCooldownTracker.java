package net.mcreator.anankor.client;

import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.api.distmarker.Dist;

import java.util.Arrays;

/**
 * Tracks client-side cooldown states for the 4 ability slots to drive HUD animations.
 */
@EventBusSubscriber(Dist.CLIENT)
public class ClientCooldownTracker {
	private static final int[] REMAINING_TICKS = new int[4];
	private static final int[] TOTAL_TICKS = new int[4];

	public static void updateCooldown(int slotIndex, int remainingTicks, int totalTicks) {
		if (slotIndex < 0) {
			Arrays.fill(REMAINING_TICKS, 0);
			Arrays.fill(TOTAL_TICKS, 0);
			return;
		}
		if (slotIndex < 4) {
			REMAINING_TICKS[slotIndex] = remainingTicks;
			TOTAL_TICKS[slotIndex] = Math.max(1, totalTicks);
		}
	}

	@SubscribeEvent
	public static void onClientTick(ClientTickEvent.Post event) {
		for (int i = 0; i < 4; i++) {
			if (REMAINING_TICKS[i] > 0) {
				REMAINING_TICKS[i]--;
			}
		}
	}

	public static float getCooldownProgress(int slotIndex) {
		if (slotIndex < 0 || slotIndex >= 4) {
			return 0.0f;
		}
		if (TOTAL_TICKS[slotIndex] <= 0 || REMAINING_TICKS[slotIndex] <= 0) {
			return 0.0f;
		}
		return (float) REMAINING_TICKS[slotIndex] / (float) TOTAL_TICKS[slotIndex];
	}

	public static int getRemainingTicks(int slotIndex) {
		if (slotIndex < 0 || slotIndex >= 4) {
			return 0;
		}
		return REMAINING_TICKS[slotIndex];
	}

	public static boolean isOnCooldown(int slotIndex) {
		return getRemainingTicks(slotIndex) > 0;
	}
}

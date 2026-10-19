package net.mcreator.anankor.nature;

import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;

import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;

import net.mcreator.anankor.network.TechniqueCooldownSyncMessage;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Arrays;

@EventBusSubscriber
public class TechniqueCooldownManager {
	private static final Map<UUID, int[]> COOLDOWNS = new ConcurrentHashMap<>();

	@SubscribeEvent
	public static void onPlayerTick(PlayerTickEvent.Post event) {
		if (event.getEntity() instanceof ServerPlayer player) {
			int[] cd = COOLDOWNS.get(player.getUUID());
			if (cd != null) {
				for (int i = 0; i < cd.length; i++) {
					if (cd[i] > 0) {
						cd[i]--;
					}
				}
			}
		}
	}

	public static boolean isOnCooldown(Player player, int slotIndex) {
		if (slotIndex < 0 || slotIndex >= 4) {
			return false;
		}
		int[] cd = COOLDOWNS.get(player.getUUID());
		return cd != null && cd[slotIndex] > 0;
	}

	public static int getRemainingTicks(Player player, int slotIndex) {
		if (slotIndex < 0 || slotIndex >= 4) {
			return 0;
		}
		int[] cd = COOLDOWNS.get(player.getUUID());
		return cd != null ? cd[slotIndex] : 0;
	}

	public static void setCooldown(ServerPlayer player, int slotIndex, int durationTicks) {
		if (slotIndex < 0 || slotIndex >= 4) {
			return;
		}
		int[] cd = COOLDOWNS.computeIfAbsent(player.getUUID(), k -> new int[4]);
		cd[slotIndex] = durationTicks;
		PacketDistributor.sendToPlayer(player, new TechniqueCooldownSyncMessage(slotIndex, durationTicks, durationTicks));
	}

	public static void resetAllCooldowns(ServerPlayer player) {
		int[] cd = COOLDOWNS.get(player.getUUID());
		if (cd != null) {
			Arrays.fill(cd, 0);
		}
		PacketDistributor.sendToPlayer(player, new TechniqueCooldownSyncMessage(-1, 0, 0));
	}
}

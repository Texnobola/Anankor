package net.mcreator.anankor.flora;

import net.mcreator.anankor.network.AnankorModVariables;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class FluxMeditationHandler {

	private static final Map<UUID, Integer> MEDITATION_TICKS = new ConcurrentHashMap<>();
	private static final int THRESHOLD_TICKS = 60; // 3 seconds required stillness
	private static final int RADIUS = 4;

	public static void handlePlayerTick(ServerPlayer player) {
		UUID uuid = player.getUUID();
		Vec3 delta = player.getDeltaMovement();
		boolean isStationary = delta.horizontalDistanceSqr() < 0.001 && Math.abs(delta.y) < 0.08;

		// 1. Must be crouching and stationary
		if (!player.isCrouching() || !isStationary) {
			if (MEDITATION_TICKS.remove(uuid) != null) {
				// Player stopped meditating
			}
			return;
		}

		// 2. Scan for nearby Flux Flora
		BlockPos playerPos = player.blockPosition();
		ServerLevel level = (ServerLevel) player.level();
		BlockPos foundFlora = null;
		boolean isNetherFlora = false;

		for (BlockPos targetPos : BlockPos.betweenClosed(
				playerPos.offset(-RADIUS, -2, -RADIUS),
				playerPos.offset(RADIUS, 2, RADIUS))) {
			BlockState state = level.getBlockState(targetPos);
			Block block = state.getBlock();

			if (block == AnankorFloraRegistry.FLUX_BUSH.get()) {
				foundFlora = targetPos.immutable();
				isNetherFlora = false;
				break;
			} else if (block == AnankorFloraRegistry.CINDERPOD_BLOOM.get()) {
				foundFlora = targetPos.immutable();
				isNetherFlora = true;
				break;
			}
		}

		if (foundFlora == null) {
			MEDITATION_TICKS.remove(uuid);
			return;
		}

		// 3. Accumulate meditation ticks
		int ticks = MEDITATION_TICKS.getOrDefault(uuid, 0) + 1;
		MEDITATION_TICKS.put(uuid, ticks);

		// 4. Pre-threshold buildup
		if (ticks < THRESHOLD_TICKS) {
			if (ticks % 20 == 0) {
				double remaining = (THRESHOLD_TICKS - ticks) / 20.0;
				player.sendSystemMessage(
						Component.literal(
								"§7✦ Attuning mind to ambient flux... (§e" + String.format("%.1f", remaining) + "s§7)"),
						true);
			}
			level.sendParticles(ParticleTypes.GLOW,
					foundFlora.getX() + 0.5, foundFlora.getY() + 0.5, foundFlora.getZ() + 0.5,
					2, 0.2, 0.2, 0.2, 0.01);
			return;
		}

		// 5. Threshold passed: Harmonic Siphoning Active!
		AnankorModVariables.PlayerVariables vars = player.getData(AnankorModVariables.PLAYER_VARIABLES);

		if (vars.flux < vars.maxflux) {
			// Siphon flux (1.5 flux per second -> ~0.375 every 5 ticks)
			if (ticks % 5 == 0) {
				double gain = isNetherFlora ? 0.45 : 0.35;
				vars.flux = Math.min(vars.maxflux, vars.flux + gain);
				vars.markSyncDirty();
			}
		}

		// Audio & action bar feedback
		if (ticks % 20 == 0) {
			if (isNetherFlora) {
				player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
						SoundEvents.FIRE_AMBIENT, SoundSource.PLAYERS, 0.5f, 1.4f);
				player.sendSystemMessage(
						Component.literal(
								"§6✦ Siphoning Pyretic Flux... §7(" + (int) vars.flux + "/" + (int) vars.maxflux + ")"),
						true);
			} else {
				player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
						SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.5f, 1.6f);
				player.sendSystemMessage(
						Component.literal(
								"§b✦ Siphoning Ambient Flux... §7(" + (int) vars.flux + "/" + (int) vars.maxflux + ")"),
						true);
			}
		}

		// Flowing particle stream from flora to player chest
		Vec3 start = new Vec3(foundFlora.getX() + 0.5, foundFlora.getY() + 0.6, foundFlora.getZ() + 0.5);
		Vec3 end = new Vec3(player.getX(), player.getY() + 0.9, player.getZ());
		Vec3 trajectory = end.subtract(start);

		double step = ((ticks * 2) % 10) / 10.0;
		double px = start.x + trajectory.x * step;
		double py = start.y + trajectory.y * step;
		double pz = start.z + trajectory.z * step;

		if (isNetherFlora) {
			level.sendParticles(ParticleTypes.FLAME, px, py, pz, 1, 0.02, 0.02, 0.02, 0.01);
		} else {
			level.sendParticles(ParticleTypes.ELECTRIC_SPARK, px, py, pz, 1, 0.02, 0.02, 0.02, 0.01);
		}
	}
}

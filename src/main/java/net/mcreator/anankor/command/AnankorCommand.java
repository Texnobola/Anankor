package net.mcreator.anankor.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.chat.Component;

import net.mcreator.anankor.network.AnankorModVariables;
import net.mcreator.anankor.nature.NatureType;
import net.mcreator.anankor.nature.NatureAbilityDispatcher;
import net.mcreator.anankor.nature.TechniqueCooldownManager;
import net.mcreator.anankor.nature.techniques.INatureTechnique;
import net.mcreator.anankor.init.AnankorModMobEffects;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

@EventBusSubscriber
public class AnankorCommand {

	private static final SuggestionProvider<CommandSourceStack> SUGGEST_NATURES = (ctx, builder) ->
			SharedSuggestionProvider.suggest(
					Arrays.stream(NatureType.values()).map(NatureType::getDisplayName),
					builder
			);

	private static final SuggestionProvider<CommandSourceStack> SUGGEST_TECHNIQUES = (ctx, builder) ->
			SharedSuggestionProvider.suggest(
					NatureAbilityDispatcher.getAllTechniqueNames(),
					builder
			);

	@SubscribeEvent
	public static void onRegisterCommands(RegisterCommandsEvent event) {
		CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

		dispatcher.register(
			Commands.literal("anankor")
				.requires(source -> source.permissions().hasPermission(net.minecraft.server.permissions.Permissions.COMMANDS_GAMEMASTER))

				// ==========================================
				// 1. FLUX COMMANDS
				// ==========================================
				.then(Commands.literal("flux")
					.then(Commands.literal("set")
						.then(Commands.argument("amount", DoubleArgumentType.doubleArg(0, 100000))
							.executes(ctx -> executeFluxSet(ctx.getSource(), getSelf(ctx.getSource()), DoubleArgumentType.getDouble(ctx, "amount")))
							.then(Commands.argument("targets", EntityArgument.players())
								.executes(ctx -> executeFluxSet(ctx.getSource(), EntityArgument.getPlayers(ctx, "targets"), DoubleArgumentType.getDouble(ctx, "amount")))
							)
						)
					)
					.then(Commands.literal("add")
						.then(Commands.argument("amount", DoubleArgumentType.doubleArg(-100000, 100000))
							.executes(ctx -> executeFluxAdd(ctx.getSource(), getSelf(ctx.getSource()), DoubleArgumentType.getDouble(ctx, "amount")))
							.then(Commands.argument("targets", EntityArgument.players())
								.executes(ctx -> executeFluxAdd(ctx.getSource(), EntityArgument.getPlayers(ctx, "targets"), DoubleArgumentType.getDouble(ctx, "amount")))
							)
						)
					)
					.then(Commands.literal("infinite")
						.then(Commands.argument("state", BoolArgumentType.bool())
							.executes(ctx -> executeFluxInfinite(ctx.getSource(), getSelf(ctx.getSource()), BoolArgumentType.getBool(ctx, "state")))
							.then(Commands.argument("targets", EntityArgument.players())
								.executes(ctx -> executeFluxInfinite(ctx.getSource(), EntityArgument.getPlayers(ctx, "targets"), BoolArgumentType.getBool(ctx, "state")))
							)
						)
					)
					.then(Commands.literal("reset")
						.executes(ctx -> executeFluxReset(ctx.getSource(), getSelf(ctx.getSource())))
						.then(Commands.argument("targets", EntityArgument.players())
							.executes(ctx -> executeFluxReset(ctx.getSource(), EntityArgument.getPlayers(ctx, "targets")))
						)
					)
				)

				// ==========================================
				// 2. NATURE COMMANDS
				// ==========================================
				.then(Commands.literal("nature")
					.then(Commands.literal("set")
						.then(Commands.argument("nature", StringArgumentType.word())
							.suggests(SUGGEST_NATURES)
							.executes(ctx -> executeNatureSet(ctx.getSource(), getSelf(ctx.getSource()), StringArgumentType.getString(ctx, "nature")))
							.then(Commands.argument("targets", EntityArgument.players())
								.executes(ctx -> executeNatureSet(ctx.getSource(), EntityArgument.getPlayers(ctx, "targets"), StringArgumentType.getString(ctx, "nature")))
							)
						)
					)
				)

				// ==========================================
				// 3. BURNOUT COMMANDS
				// ==========================================
				.then(Commands.literal("burnout")
					.then(Commands.literal("clear")
						.executes(ctx -> executeBurnoutClear(ctx.getSource(), getSelf(ctx.getSource())))
						.then(Commands.argument("targets", EntityArgument.players())
							.executes(ctx -> executeBurnoutClear(ctx.getSource(), EntityArgument.getPlayers(ctx, "targets")))
						)
					)
					.then(Commands.literal("set")
						.then(Commands.argument("seconds", IntegerArgumentType.integer(1, 3600))
							.executes(ctx -> executeBurnoutSet(ctx.getSource(), getSelf(ctx.getSource()), IntegerArgumentType.getInteger(ctx, "seconds")))
							.then(Commands.argument("targets", EntityArgument.players())
								.executes(ctx -> executeBurnoutSet(ctx.getSource(), EntityArgument.getPlayers(ctx, "targets"), IntegerArgumentType.getInteger(ctx, "seconds")))
							)
						)
					)
				)

				// ==========================================
				// 4. COOLDOWN COMMANDS
				// ==========================================
				.then(Commands.literal("cooldown")
					.then(Commands.literal("reset")
						.executes(ctx -> executeCooldownReset(ctx.getSource(), getSelf(ctx.getSource())))
						.then(Commands.argument("targets", EntityArgument.players())
							.executes(ctx -> executeCooldownReset(ctx.getSource(), EntityArgument.getPlayers(ctx, "targets")))
						)
					)
				)

				// ==========================================
				// 5. TEST TECHNIQUE COMMAND
				// ==========================================
				.then(Commands.literal("test")
					.then(Commands.argument("technique", StringArgumentType.greedyString())
						.suggests(SUGGEST_TECHNIQUES)
						.executes(ctx -> executeTestTechnique(ctx.getSource(), getSelf(ctx.getSource()), StringArgumentType.getString(ctx, "technique")))
					)
				)
		);
	}

	private static Collection<ServerPlayer> getSelf(CommandSourceStack source) {
		ServerPlayer player = source.getPlayer();
		return player != null ? Collections.singleton(player) : Collections.emptyList();
	}

	private static int executeFluxSet(CommandSourceStack source, Collection<ServerPlayer> targets, double amount) {
		for (ServerPlayer player : targets) {
			AnankorModVariables.PlayerVariables vars = player.getData(AnankorModVariables.PLAYER_VARIABLES);
			vars.flux = amount;
			vars.markSyncDirty();
			source.sendSuccess(() -> Component.literal("§a[Anankor] Set Flux of §b" + player.getName().getString() + " §ato §e" + (int) amount), true);
		}
		return targets.size();
	}

	private static int executeFluxAdd(CommandSourceStack source, Collection<ServerPlayer> targets, double amount) {
		for (ServerPlayer player : targets) {
			AnankorModVariables.PlayerVariables vars = player.getData(AnankorModVariables.PLAYER_VARIABLES);
			vars.flux = Math.min(vars.maxflux, vars.flux + amount);
			vars.markSyncDirty();
			source.sendSuccess(() -> Component.literal("§a[Anankor] Added §e" + (int) amount + " §aFlux to §b" + player.getName().getString() + " §7(Current: " + (int) vars.flux + ")"), true);
		}
		return targets.size();
	}

	private static int executeFluxInfinite(CommandSourceStack source, Collection<ServerPlayer> targets, boolean state) {
		for (ServerPlayer player : targets) {
			AnankorModVariables.PlayerVariables vars = player.getData(AnankorModVariables.PLAYER_VARIABLES);
			vars.infiniteFlux = state;
			if (state) {
				vars.flux = vars.maxflux;
			}
			vars.markSyncDirty();
			source.sendSuccess(() -> Component.literal("§a[Anankor] Infinite Flux for §b" + player.getName().getString() + " §aset to: §e" + state), true);
		}
		return targets.size();
	}

	private static int executeFluxReset(CommandSourceStack source, Collection<ServerPlayer> targets) {
		for (ServerPlayer player : targets) {
			AnankorModVariables.PlayerVariables vars = player.getData(AnankorModVariables.PLAYER_VARIABLES);
			vars.infiniteFlux = false;
			vars.flux = vars.maxflux;
			vars.FluxRegenTimer = 0;
			vars.markSyncDirty();
			source.sendSuccess(() -> Component.literal("§a[Anankor] Reset Flux of §b" + player.getName().getString() + " §ato normal max (§e" + (int) vars.maxflux + "§a)"), true);
		}
		return targets.size();
	}

	private static int executeNatureSet(CommandSourceStack source, Collection<ServerPlayer> targets, String natureName) {
		NatureType nature = NatureType.fromString(natureName);
		for (ServerPlayer player : targets) {
			AnankorModVariables.PlayerVariables vars = player.getData(AnankorModVariables.PLAYER_VARIABLES);
			vars.Nature = nature.name();
			vars.selectedTechnique = 0;
			vars.markSyncDirty();
			source.sendSuccess(() -> Component.literal("§a[Anankor] Nature of §b" + player.getName().getString() + " §aset to §e" + nature.getDisplayName()), true);
		}
		return targets.size();
	}

	private static int executeBurnoutClear(CommandSourceStack source, Collection<ServerPlayer> targets) {
		for (ServerPlayer player : targets) {
			player.removeEffect(AnankorModMobEffects.BURNOUT);
			AnankorModVariables.PlayerVariables vars = player.getData(AnankorModVariables.PLAYER_VARIABLES);
			if (vars.flux < 0) {
				vars.flux = 0;
			}
			vars.markSyncDirty();
			source.sendSuccess(() -> Component.literal("§a[Anankor] Cleared Burnout for §b" + player.getName().getString()), true);
		}
		return targets.size();
	}

	private static int executeBurnoutSet(CommandSourceStack source, Collection<ServerPlayer> targets, int seconds) {
		for (ServerPlayer player : targets) {
			player.addEffect(new MobEffectInstance(AnankorModMobEffects.BURNOUT, seconds * 20, 0));
			AnankorModVariables.PlayerVariables vars = player.getData(AnankorModVariables.PLAYER_VARIABLES);
			vars.flux = 0;
			vars.FluxRegenTimer = 0;
			vars.markSyncDirty();
			source.sendSuccess(() -> Component.literal("§c[Anankor] Inflicted Burnout for §e" + seconds + "s §con §b" + player.getName().getString()), true);
		}
		return targets.size();
	}

	private static int executeCooldownReset(CommandSourceStack source, Collection<ServerPlayer> targets) {
		for (ServerPlayer player : targets) {
			TechniqueCooldownManager.resetAllCooldowns(player);
			source.sendSuccess(() -> Component.literal("§a[Anankor] Reset all technique cooldowns for §b" + player.getName().getString()), true);
		}
		return targets.size();
	}

	private static int executeTestTechnique(CommandSourceStack source, Collection<ServerPlayer> targets, String techName) {
		INatureTechnique technique = NatureAbilityDispatcher.findTechniqueByName(techName);
		if (technique == null) {
			source.sendFailure(Component.literal("§c[Anankor] Unknown technique: \"" + techName + "\". Use Tab completion to view available abilities."));
			return 0;
		}

		for (ServerPlayer player : targets) {
			Vec3 look = player.getLookAngle();
			Vec3 targetPos = player.position().add(look.scale(5.0));
			technique.execute(player, targetPos);
			source.sendSuccess(() -> Component.literal("§6[Anankor Test] §fExecuted §e" + technique.getName() + " §7(Flux & Cooldown bypassed) on §b" + player.getName().getString()), true);
		}
		return targets.size();
	}
}

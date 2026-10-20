package net.mcreator.anankor.network;

import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.ModList;
import net.neoforged.bus.api.SubscribeEvent;

import net.minecraft.world.entity.player.Player;
import net.minecraft.resources.Identifier;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.chat.Component;
import net.minecraft.network.RegistryFriendlyByteBuf;

import net.mcreator.anankor.AnankorMod;

/**
 * Server-to-client packet notifying clients to play or stop a PAL player animation
 * on a specified player entity.
 */
@EventBusSubscriber
public record PlayPlayerAnimationMessage(int player, String animation)
		implements CustomPacketPayload {

	public static final Type<PlayPlayerAnimationMessage> TYPE = new Type<>(
			Identifier.fromNamespaceAndPath(AnankorMod.MODID, "play_player_animation"));

	public static final StreamCodec<RegistryFriendlyByteBuf, PlayPlayerAnimationMessage> STREAM_CODEC = StreamCodec
			.of((RegistryFriendlyByteBuf buffer, PlayPlayerAnimationMessage message) -> {
				buffer.writeInt(message.player);
				buffer.writeUtf(message.animation == null ? "" : message.animation);
			}, (RegistryFriendlyByteBuf buffer) -> new PlayPlayerAnimationMessage(buffer.readInt(), buffer.readUtf()));

	/**
	 * Backwards-compatibility constructor preserving compatibility for any legacy callers
	 * specifying override or firstPerson flags.
	 */
	public PlayPlayerAnimationMessage(int player, String animation, boolean override, boolean firstPerson) {
		this(player, animation);
	}

	public static PlayPlayerAnimationMessage stop(int player) {
		return new PlayPlayerAnimationMessage(player, "");
	}

	@Override
	public Type<PlayPlayerAnimationMessage> type() {
		return TYPE;
	}

	public static void handleData(final PlayPlayerAnimationMessage message, final IPayloadContext context) {
		if (context.flow() == PacketFlow.CLIENTBOUND) {
			context.enqueueWork(() -> {
				if (context.player() == null || context.player().level() == null) {
					return;
				}
				if (!(context.player().level().getEntity(message.player) instanceof Player targetPlayer)) {
					return;
				}
				if (ModList.get().isLoaded("player_animation_library")) {
					if (message.animation == null || message.animation.isEmpty()) {
						PalClientHelper.stop(targetPlayer);
					} else {
						PalClientHelper.play(targetPlayer, message.animation);
					}
				}
			}).exceptionally(e -> {
				context.connection().disconnect(Component.literal(e.getMessage()));
				return null;
			});
		}
	}

	@SubscribeEvent
	public static void registerMessage(FMLCommonSetupEvent event) {
		AnankorMod.addNetworkMessage(PlayPlayerAnimationMessage.TYPE, PlayPlayerAnimationMessage.STREAM_CODEC,
				PlayPlayerAnimationMessage::handleData);
	}

	/**
	 * Isolated client invocation helper ensuring PAL classes are only resolved
	 * when Player Animation Library is loaded at runtime.
	 */
	private static final class PalClientHelper {
		static void play(Player player, String animation) {
			net.mcreator.anankor.client.AnankorPlayerAnimationHandler.playAnimation(player, animation);
		}

		static void stop(Player player) {
			net.mcreator.anankor.client.AnankorPlayerAnimationHandler.stopAnimation(player);
		}
	}
}
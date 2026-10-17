package net.mcreator.anankor.network;

import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;

import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.Identifier;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.chat.Component;
import net.minecraft.network.RegistryFriendlyByteBuf;

import net.mcreator.anankor.nature.NatureType;
import net.mcreator.anankor.AnankorMod;

@EventBusSubscriber
public record SetNatureMessage(String natureName) implements CustomPacketPayload {
	public static final Type<SetNatureMessage> TYPE = new Type<>(
			Identifier.fromNamespaceAndPath(AnankorMod.MODID, "set_nature"));
	public static final StreamCodec<RegistryFriendlyByteBuf, SetNatureMessage> STREAM_CODEC = StreamCodec.of(
			(RegistryFriendlyByteBuf buffer, SetNatureMessage message) -> buffer.writeUtf(message.natureName),
			(RegistryFriendlyByteBuf buffer) -> new SetNatureMessage(buffer.readUtf()));

	@Override
	public Type<SetNatureMessage> type() {
		return TYPE;
	}

	public static void handleData(final SetNatureMessage message, final IPayloadContext context) {
		if (context.flow() == PacketFlow.SERVERBOUND) {
			context.enqueueWork(() -> {
				if (context.player() instanceof ServerPlayer player) {
					AnankorModVariables.PlayerVariables vars = player.getData(AnankorModVariables.PLAYER_VARIABLES);
					NatureType newNature = NatureType.fromString(message.natureName);
					vars.Nature = newNature.name();
					vars.selectedTechnique = 0;
					vars.markSyncDirty();

					player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
							SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.6f, 1.2f);
					player.sendSystemMessage(
							Component.literal("§6[ANANKOR] Alignment set to: §e" + newNature.getDisplayName()),
							true);
				}
			}).exceptionally(e -> {
				context.connection().disconnect(Component.literal(e.getMessage()));
				return null;
			});
		}
	}

	@SubscribeEvent
	public static void registerMessage(FMLCommonSetupEvent event) {
		AnankorMod.addNetworkMessage(SetNatureMessage.TYPE, SetNatureMessage.STREAM_CODEC,
				SetNatureMessage::handleData);
	}
}

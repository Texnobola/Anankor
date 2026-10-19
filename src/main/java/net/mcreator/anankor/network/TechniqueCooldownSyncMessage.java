package net.mcreator.anankor.network;

import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;

import net.minecraft.resources.Identifier;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;

import net.mcreator.anankor.client.ClientCooldownTracker;
import net.mcreator.anankor.AnankorMod;

@EventBusSubscriber
public record TechniqueCooldownSyncMessage(int slotIndex, int remainingTicks, int totalTicks) implements CustomPacketPayload {
	public static final Type<TechniqueCooldownSyncMessage> TYPE = new Type<>(
			Identifier.fromNamespaceAndPath(AnankorMod.MODID, "technique_cooldown_sync"));

	public static final StreamCodec<RegistryFriendlyByteBuf, TechniqueCooldownSyncMessage> STREAM_CODEC = StreamCodec.of(
			(RegistryFriendlyByteBuf buffer, TechniqueCooldownSyncMessage message) -> {
				buffer.writeInt(message.slotIndex);
				buffer.writeInt(message.remainingTicks);
				buffer.writeInt(message.totalTicks);
			},
			(RegistryFriendlyByteBuf buffer) -> new TechniqueCooldownSyncMessage(
					buffer.readInt(),
					buffer.readInt(),
					buffer.readInt()));

	@SubscribeEvent
	public static void init(FMLCommonSetupEvent event) {
		AnankorMod.addNetworkMessage(TYPE, STREAM_CODEC, TechniqueCooldownSyncMessage::handleData);
	}

	@Override
	public Type<TechniqueCooldownSyncMessage> type() {
		return TYPE;
	}

	public static void handleData(final TechniqueCooldownSyncMessage message, final IPayloadContext context) {
		if (context.flow() == PacketFlow.CLIENTBOUND) {
			context.enqueueWork(() -> {
				ClientCooldownTracker.updateCooldown(message.slotIndex, message.remainingTicks, message.totalTicks);
			});
		}
	}
}

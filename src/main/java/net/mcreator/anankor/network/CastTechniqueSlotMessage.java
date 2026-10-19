package net.mcreator.anankor.network;

import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.Identifier;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;

import net.mcreator.anankor.nature.NatureAbilityDispatcher;
import net.mcreator.anankor.AnankorMod;

@EventBusSubscriber
public record CastTechniqueSlotMessage(int slotIndex) implements CustomPacketPayload {
	public static final Type<CastTechniqueSlotMessage> TYPE = new Type<>(
			Identifier.fromNamespaceAndPath(AnankorMod.MODID, "cast_technique_slot"));

	public static final StreamCodec<RegistryFriendlyByteBuf, CastTechniqueSlotMessage> STREAM_CODEC = StreamCodec.of(
			(RegistryFriendlyByteBuf buffer, CastTechniqueSlotMessage message) -> buffer.writeInt(message.slotIndex),
			(RegistryFriendlyByteBuf buffer) -> new CastTechniqueSlotMessage(buffer.readInt()));

	@SubscribeEvent
	public static void init(FMLCommonSetupEvent event) {
		AnankorMod.addNetworkMessage(TYPE, STREAM_CODEC, CastTechniqueSlotMessage::handleData);
	}

	@Override
	public Type<CastTechniqueSlotMessage> type() {
		return TYPE;
	}

	public static void handleData(final CastTechniqueSlotMessage message, final IPayloadContext context) {
		if (context.flow() == PacketFlow.SERVERBOUND) {
			context.enqueueWork(() -> {
				if (context.player() instanceof ServerPlayer player) {
					NatureAbilityDispatcher.castSlot(player, message.slotIndex, false);
				}
			});
		}
	}
}

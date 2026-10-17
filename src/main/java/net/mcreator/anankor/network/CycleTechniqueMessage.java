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

import net.mcreator.anankor.nature.techniques.INatureTechnique;
import net.mcreator.anankor.nature.NatureType;
import net.mcreator.anankor.nature.NatureAbilityDispatcher;
import net.mcreator.anankor.AnankorMod;

import java.util.List;

@EventBusSubscriber
public record CycleTechniqueMessage(int direction) implements CustomPacketPayload {
	public static final Type<CycleTechniqueMessage> TYPE = new Type<>(
			Identifier.fromNamespaceAndPath(AnankorMod.MODID, "cycle_technique"));
	public static final StreamCodec<RegistryFriendlyByteBuf, CycleTechniqueMessage> STREAM_CODEC = StreamCodec.of(
			(RegistryFriendlyByteBuf buffer, CycleTechniqueMessage message) -> buffer.writeInt(message.direction),
			(RegistryFriendlyByteBuf buffer) -> new CycleTechniqueMessage(buffer.readInt()));

	@Override
	public Type<CycleTechniqueMessage> type() {
		return TYPE;
	}

	public static void handleData(final CycleTechniqueMessage message, final IPayloadContext context) {
		if (context.flow() == PacketFlow.SERVERBOUND) {
			context.enqueueWork(() -> {
				if (context.player() instanceof ServerPlayer player) {
					AnankorModVariables.PlayerVariables vars = player.getData(AnankorModVariables.PLAYER_VARIABLES);
					NatureType nature = NatureType.fromString(vars.Nature);
					List<INatureTechnique> techniques = NatureAbilityDispatcher.getTechniques(nature);
					if (techniques.isEmpty()) {
						techniques = NatureAbilityDispatcher.getTechniques(NatureType.NONE);
					}

					if (!techniques.isEmpty()) {
						int current = (int) vars.selectedTechnique;
						int next = (current + (message.direction >= 0 ? 1 : techniques.size() - 1)) % techniques.size();
						vars.selectedTechnique = next;
						vars.markSyncDirty();

						INatureTechnique tech = techniques.get(next);
						player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
								SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.PLAYERS, 0.4f, 1.3f);
						player.sendSystemMessage(
								Component.literal("§6Active Technique: §f" + tech.getName() + " §7(Cost: "
										+ tech.getCost() + " Flux)"),
								true);
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
		AnankorMod.addNetworkMessage(CycleTechniqueMessage.TYPE, CycleTechniqueMessage.STREAM_CODEC,
				CycleTechniqueMessage::handleData);
	}
}

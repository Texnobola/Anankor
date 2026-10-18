package net.mcreator.anankor.network;

import net.mcreator.anankor.AnankorMod;
import net.mcreator.anankor.client.AbyssalShroudClientHandler;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Network packet sent to the casting player to trigger the Abyssal Shroud post-effect shader.
 */
@EventBusSubscriber
public record AbyssalShroudEffectMessage(int durationTicks) implements CustomPacketPayload {

    public static final Type<AbyssalShroudEffectMessage> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(AnankorMod.MODID, "abyssal_shroud_effect"));

    public static final StreamCodec<RegistryFriendlyByteBuf, AbyssalShroudEffectMessage> STREAM_CODEC =
            StreamCodec.of((RegistryFriendlyByteBuf buffer, AbyssalShroudEffectMessage message) -> {
                buffer.writeInt(message.durationTicks);
            }, (RegistryFriendlyByteBuf buffer) -> new AbyssalShroudEffectMessage(buffer.readInt()));

    @Override
    public Type<AbyssalShroudEffectMessage> type() {
        return TYPE;
    }

    public static void handleData(final AbyssalShroudEffectMessage message, final IPayloadContext context) {
        if (context.flow() == PacketFlow.CLIENTBOUND) {
            context.enqueueWork(() -> {
                AbyssalShroudClientHandler.activate(message.durationTicks);
            }).exceptionally(e -> {
                context.connection().disconnect(Component.literal(e.getMessage()));
                return null;
            });
        }
    }

    @SubscribeEvent
    public static void registerMessage(FMLCommonSetupEvent event) {
        AnankorMod.addNetworkMessage(AbyssalShroudEffectMessage.TYPE, AbyssalShroudEffectMessage.STREAM_CODEC, AbyssalShroudEffectMessage::handleData);
    }
}

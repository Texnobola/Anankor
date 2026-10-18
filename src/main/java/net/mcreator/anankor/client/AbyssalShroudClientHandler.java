package net.mcreator.anankor.client;

import net.mcreator.anankor.AnankorMod;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/**
 * Client-side lifecycle controller for the Abyssal Shroud post-effect shader.
 * Activates the Astral Monochrome wireframe post-effect for the duration of the ability.
 */
@EventBusSubscriber(modid = AnankorMod.MODID, value = Dist.CLIENT)
public class AbyssalShroudClientHandler {

    public static final Identifier SHROUD_POST_EFFECT =
            Identifier.fromNamespaceAndPath(AnankorMod.MODID, "abyssal_shroud");

    private static int ticksRemaining = 0;

    public static void activate(int durationTicks) {
        ticksRemaining = durationTicks;
        applyEffect();
    }

    public static int getTicksRemaining() {
        return ticksRemaining;
    }

    private static void applyEffect() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.gameRenderer != null) {
            try {
                java.lang.reflect.Field failedField = net.minecraft.client.renderer.GameRenderer.class.getDeclaredField("failedPostEffects");
                failedField.setAccessible(true);
                ((java.util.List<?>) failedField.get(mc.gameRenderer)).remove(SHROUD_POST_EFFECT);
            } catch (Throwable ignored) {
            }
            mc.gameRenderer.setSpectatedEntityPostEffect(SHROUD_POST_EFFECT);
        }
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || !mc.player.isAlive()) {
            if (ticksRemaining > 0) {
                ticksRemaining = 0;
                if (mc.gameRenderer != null && SHROUD_POST_EFFECT.equals(mc.gameRenderer.spectatedEntityPostEffect())) {
                    mc.gameRenderer.clearSpectatedEntityPostEffect();
                }
            }
            return;
        }

        if (ticksRemaining > 0) {
            ticksRemaining--;
            if (mc.gameRenderer != null && !SHROUD_POST_EFFECT.equals(mc.gameRenderer.spectatedEntityPostEffect())) {
                applyEffect();
            }
            if (ticksRemaining <= 0) {
                if (mc.gameRenderer != null && SHROUD_POST_EFFECT.equals(mc.gameRenderer.spectatedEntityPostEffect())) {
                    mc.gameRenderer.clearSpectatedEntityPostEffect();
                }
            }
        }
    }
}

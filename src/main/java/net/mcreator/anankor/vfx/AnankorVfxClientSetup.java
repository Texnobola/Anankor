package net.mcreator.anankor.vfx;

import net.mcreator.anankor.AnankorMod;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/**
 * Client-side setup for VFX entity renderers.
 * Automatically registered via EventBusSubscriber on client side.
 */
@EventBusSubscriber(modid = AnankorMod.MODID, value = Dist.CLIENT)
public class AnankorVfxClientSetup {

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(AnankorVfxEntities.ANANKOR_VFX.get(), AnankorVfxRenderer::new);
    }
}

package net.mcreator.anankor.vfx;

import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.RenderPassInfo;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;

/**
 * GeckoLib 5 renderer for AnankorVfxEntity.
 *
 * Features:
 * - No shadow (pure visual effect)
 * - Translucent rendering for magical effects
 * - Dynamic scale from entity data via DataTicket
 * - Suppresses rendering if VFX entity has reached its lifespan or is removed
 */
public class AnankorVfxRenderer extends GeoEntityRenderer<AnankorVfxEntity, EntityRenderState> {

    public static final DataTicket<Float> SCALE_TICKET = DataTicket.create("vfx_scale", Float.class);
    public static final DataTicket<Boolean> FINISHED_TICKET = DataTicket.create("vfx_finished", Boolean.class);

    public AnankorVfxRenderer(EntityRendererProvider.Context context) {
        super(context, new AnankorVfxModel());
        this.shadowRadius = 0.0f;
    }

    @Override
    protected boolean affectedByCulling(AnankorVfxEntity entity) {
        return false;
    }

    @Override
    public RenderType getRenderType(EntityRenderState state, Identifier texture) {
        return RenderTypes.entityTranslucentEmissive(texture);
    }

    @Override
    public void scaleModelForRender(RenderPassInfo<EntityRenderState> info, float widthScale, float heightScale) {
        float scale = info.getOrDefaultGeckolibData(SCALE_TICKET, 1.0f);
        super.scaleModelForRender(info, widthScale * scale, heightScale * scale);
    }

    @Override
    public void extractRenderState(AnankorVfxEntity entity, EntityRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        if (state instanceof GeoRenderState geoState) {
            geoState.addGeckolibData(SCALE_TICKET, entity.getVfxScale());
            boolean isFinished = entity.isRemoved()
                    || (entity.getLifespan() > 0 && entity.tickCount >= entity.getLifespan());
            geoState.addGeckolibData(FINISHED_TICKET, isFinished);
        }
    }

    @Override
    public void submit(EntityRenderState state, PoseStack poseStack, SubmitNodeCollector collector,
            CameraRenderState cameraRenderState) {
        if (state instanceof GeoRenderState geoState
                && Boolean.TRUE.equals(geoState.getGeckolibData(FINISHED_TICKET))) {
            return;
        }
        super.submit(state, poseStack, collector, cameraRenderState);
    }
}

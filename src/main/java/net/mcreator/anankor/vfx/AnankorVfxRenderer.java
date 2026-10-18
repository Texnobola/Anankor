package net.mcreator.anankor.vfx;

import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.RenderPassInfo;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

/**
 * GeckoLib 5 renderer for AnankorVfxEntity.
 *
 * Features:
 *  - No shadow (pure visual effect)
 *  - Translucent rendering for magical effects
 *  - Dynamic scale from entity data
 */
public class AnankorVfxRenderer extends GeoEntityRenderer<AnankorVfxEntity, EntityRenderState> {

    public AnankorVfxRenderer(EntityRendererProvider.Context context) {
        super(context, new AnankorVfxModel());
        this.shadowRadius = 0.0f;
    }

    @Override
    public RenderType getRenderType(EntityRenderState state, Identifier texture) {
        return RenderTypes.entityTranslucent(texture);
    }

    @Override
    public void scaleModelForRender(RenderPassInfo<EntityRenderState> info, float widthScale, float heightScale) {
        super.scaleModelForRender(info, widthScale * this.scaleWidth, heightScale * this.scaleHeight);
    }

    @Override
    public void extractRenderState(AnankorVfxEntity entity, EntityRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        float vfxScale = entity.getVfxScale();
        this.scaleWidth = vfxScale;
        this.scaleHeight = vfxScale;
    }
}

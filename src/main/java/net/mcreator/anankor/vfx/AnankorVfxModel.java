package net.mcreator.anankor.vfx;

import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.model.DefaultedEntityGeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.mcreator.anankor.AnankorMod;
import net.minecraft.resources.Identifier;

/**
 * GeckoLib 5 model for the Anankor VFX entity.
 * Resolves dynamic texture variants using GeoRenderState data tickets.
 */
public class AnankorVfxModel extends DefaultedEntityGeoModel<AnankorVfxEntity> {

    public static final DataTicket<String> TEXTURE_VARIANT_TICKET =
            DataTicket.create("vfx_variant", String.class);

    public AnankorVfxModel() {
        super(Identifier.fromNamespaceAndPath(AnankorMod.MODID, "anankor_vfx"));
    }

    @Override
    public void addAdditionalStateData(AnankorVfxEntity animatable, Object relatedObject, GeoRenderState renderState) {
        super.addAdditionalStateData(animatable, relatedObject, renderState);
        renderState.addGeckolibData(TEXTURE_VARIANT_TICKET, animatable.getTextureVariant());
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        String variant = renderState.getOrDefaultGeckolibData(TEXTURE_VARIANT_TICKET, "flux");
        return Identifier.fromNamespaceAndPath(AnankorMod.MODID, "textures/entity/vfx/vfx_" + variant + ".png");
    }
}

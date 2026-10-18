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
        String variant = animatable.getTextureVariant();
        if (variant == null || variant.isEmpty()) {
            variant = "flux";
        }
        renderState.addGeckolibData(TEXTURE_VARIANT_TICKET, variant);
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        String variant = "flux";
        if (renderState != null && renderState.hasGeckolibData(TEXTURE_VARIANT_TICKET)) {
            String v = renderState.getGeckolibData(TEXTURE_VARIANT_TICKET);
            if (v != null && !v.isEmpty()) {
                variant = v;
            }
        }
        return Identifier.fromNamespaceAndPath(AnankorMod.MODID, "textures/entity/vfx/vfx_" + variant + ".png");
    }
}

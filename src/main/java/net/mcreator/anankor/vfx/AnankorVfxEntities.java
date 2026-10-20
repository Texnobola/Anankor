package net.mcreator.anankor.vfx;

import net.mcreator.anankor.AnankorMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

/**
 * Registers all VFX-related entity types for the Anankor mod.
 */
public class AnankorVfxEntities {

        public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister
                        .create(Registries.ENTITY_TYPE, AnankorMod.MODID);

        public static final ResourceKey<EntityType<?>> VFX_KEY = ResourceKey.create(Registries.ENTITY_TYPE,
                        Identifier.fromNamespaceAndPath(AnankorMod.MODID, "anankor_vfx"));

        /**
         * The generic VFX entity used for all nature technique visual effects.
         * - MISC category (doesn't count towards mob caps)
         * - Non-persistent (never saved to disk)
         * - Tiny collision box (purely visual)
         * - Tracked at 64 blocks, updated every tick
         */
        public static final Supplier<EntityType<AnankorVfxEntity>> ANANKOR_VFX = ENTITY_TYPES.register(
                        "anankor_vfx",
                        () -> EntityType.Builder.<AnankorVfxEntity>of(AnankorVfxEntity::new, MobCategory.MISC)
                                        .sized(1.0f, 1.0f)
                                        .noSave()
                                        .fireImmune()
                                        .clientTrackingRange(64)
                                        .updateInterval(1)
                                        .build(VFX_KEY));

        public static void register(IEventBus modEventBus) {
                ENTITY_TYPES.register(modEventBus);
        }
}

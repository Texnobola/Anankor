package net.mcreator.anankor.nature.techniques;

import net.mcreator.anankor.AnankorMod;
import net.mcreator.anankor.nature.NatureType;
import net.mcreator.anankor.nature.TechniqueCategory;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.Locale;
import java.util.Set;

public interface INatureTechnique {

    /**
     * Stable, unique identifier for this technique (e.g. anankor:fire/ignition_blade).
     */
    default Identifier getId() {
        return Identifier.fromNamespaceAndPath(AnankorMod.MODID,
                getName().toLowerCase(Locale.ROOT).replace(' ', '_'));
    }

    String getName();

    String getDescription();

    int getCost();

    default int getCooldownTicks() {
        return 40; // 2 seconds default
    }

    default TechniqueCategory getCategory() {
        return TechniqueCategory.STRIKE;
    }

    /**
     * The primary Nature this technique belongs to.
     */
    default NatureType getPrimaryNature() {
        return NatureType.NONE;
    }

    /**
     * All Natures required to channel or unlock this technique (supports future hybrids).
     */
    default Set<NatureType> getRequiredNatures() {
        NatureType primary = getPrimaryNature();
        return primary != null && primary != NatureType.NONE ? Set.of(primary) : Set.of();
    }

    /**
     * Whether this technique represents an advanced specialization branch (e.g. Combustion).
     */
    default boolean isSpecialization() {
        return false;
    }

    void execute(LivingEntity executor, Vec3 targetPosition);
}



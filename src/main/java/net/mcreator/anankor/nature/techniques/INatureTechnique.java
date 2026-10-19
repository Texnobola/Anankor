package net.mcreator.anankor.nature.techniques;

import net.mcreator.anankor.nature.TechniqueCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public interface INatureTechnique {
    String getName();

    String getDescription();

    int getCost();

    default int getCooldownTicks() {
        return 40; // 2 seconds default
    }

    default TechniqueCategory getCategory() {
        return TechniqueCategory.STRIKE;
    }

    void execute(LivingEntity executor, Vec3 targetPosition);
}


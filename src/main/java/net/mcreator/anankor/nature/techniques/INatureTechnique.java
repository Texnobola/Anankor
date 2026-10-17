package net.mcreator.anankor.nature.techniques;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public interface INatureTechnique {
    String getName();

    String getDescription();

    int getCost();

    void execute(LivingEntity executor, Vec3 targetPosition);
}

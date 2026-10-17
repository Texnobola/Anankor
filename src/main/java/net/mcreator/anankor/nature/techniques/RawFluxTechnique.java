package net.mcreator.anankor.nature.techniques;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class RawFluxTechnique implements INatureTechnique {

    @Override
    public String getName() {
        return "Raw Flux";
    }

    @Override
    public String getDescription() {
        return "Unleashes raw chaotic energy";
    }

    @Override
    public int getCost() {
        return 20;
    }

    @Override
    public void execute(LivingEntity executor, Vec3 targetPosition) {
        // Implement raw flux technique logic here
        System.out.println("Executing raw flux technique!");
    }
}

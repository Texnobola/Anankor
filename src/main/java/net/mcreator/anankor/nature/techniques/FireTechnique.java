package net.mcreator.anankor.nature.techniques;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class FireTechnique implements INatureTechnique {

    @Override
    public String getName() {
        return "Fire Technique";
    }

    @Override
    public String getDescription() {
        return "A basic fire ability";
    }

    @Override
    public int getCost() {
        return 10;
    }

    @Override
    public void execute(LivingEntity executor, Vec3 targetPosition) {
        // Implement fire technique logic here
        System.out.println("Executing fire technique!");
    }
}

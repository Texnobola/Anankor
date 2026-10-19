package net.mcreator.anankor.nature.techniques;

import net.mcreator.anankor.nature.TechniqueCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.function.BiConsumer;

/**
 * Highly modular technique implementation for expanding the Nature technique
 * roster.
 */
public class FunctionalTechnique implements INatureTechnique {
	private final String name;
	private final String description;
	private final int cost;
	private final int cooldownTicks;
	private final TechniqueCategory category;
	private final BiConsumer<LivingEntity, Vec3> action;

	public FunctionalTechnique(String name, String description, int cost, int cooldownTicks,
			TechniqueCategory category, BiConsumer<LivingEntity, Vec3> action) {
		this.name = name;
		this.description = description;
		this.cost = cost;
		this.cooldownTicks = cooldownTicks;
		this.category = category;
		this.action = action;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public int getCost() {
		return cost;
	}

	@Override
	public int getCooldownTicks() {
		return cooldownTicks;
	}

	@Override
	public TechniqueCategory getCategory() {
		return category;
	}

	@Override
	public void execute(LivingEntity executor, Vec3 targetPosition) {
		if (action != null) {
			action.accept(executor, targetPosition);
		}
	}
}

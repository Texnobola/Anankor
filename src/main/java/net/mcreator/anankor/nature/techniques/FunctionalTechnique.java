package net.mcreator.anankor.nature.techniques;

import net.mcreator.anankor.AnankorMod;
import net.mcreator.anankor.nature.NatureType;
import net.mcreator.anankor.nature.TechniqueCategory;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.function.BiConsumer;

/**
 * Highly modular technique implementation for expanding the Nature technique
 * roster without excessive boilerplate.
 */
public class FunctionalTechnique implements INatureTechnique {
	private final Identifier id;
	private final NatureType primaryNature;
	private final String name;
	private final String description;
	private final int cost;
	private final int cooldownTicks;
	private final TechniqueCategory category;
	private final BiConsumer<LivingEntity, Vec3> action;

	public FunctionalTechnique(String name, String description, int cost, int cooldownTicks,
			TechniqueCategory category, BiConsumer<LivingEntity, Vec3> action) {
		this(null, NatureType.NONE, name, description, cost, cooldownTicks, category, action);
	}

	public FunctionalTechnique(Identifier id, NatureType primaryNature, String name, String description, int cost,
			int cooldownTicks, TechniqueCategory category, BiConsumer<LivingEntity, Vec3> action) {
		this.id = id;
		this.primaryNature = primaryNature != null ? primaryNature : NatureType.NONE;
		this.name = name;
		this.description = description;
		this.cost = cost;
		this.cooldownTicks = cooldownTicks;
		this.category = category;
		this.action = action;
	}

	@Override
	public Identifier getId() {
		return id != null ? id : INatureTechnique.super.getId();
	}

	@Override
	public NatureType getPrimaryNature() {
		return primaryNature;
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

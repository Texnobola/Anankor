/*
 *	MCreator note: This file will be REGENERATED on each build.
 */
package net.mcreator.anankor.init;

import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.core.registries.Registries;

import net.mcreator.anankor.potion.BurnoutMobEffect;
import net.mcreator.anankor.AnankorMod;

public class AnankorModMobEffects {
	public static final DeferredRegister<MobEffect> REGISTRY = DeferredRegister.create(Registries.MOB_EFFECT, AnankorMod.MODID);
	public static final DeferredHolder<MobEffect, MobEffect> BURNOUT = REGISTRY.register("burnout", BurnoutMobEffect::new);
}
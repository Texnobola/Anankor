package net.mcreator.anankor.nature;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.mcreator.anankor.nature.techniques.FireTechnique;
import net.mcreator.anankor.nature.techniques.SpaceTechnique;
import net.mcreator.anankor.nature.techniques.WaterTechnique;
import net.mcreator.anankor.nature.techniques.IceTechnique;
import net.mcreator.anankor.nature.techniques.EarthTechnique;
import net.mcreator.anankor.nature.techniques.ElectricityTechnique;
import net.mcreator.anankor.nature.techniques.LightTechnique;
import net.mcreator.anankor.nature.techniques.DarkTechnique;
import net.mcreator.anankor.nature.techniques.INatureTechnique;
import net.mcreator.anankor.nature.techniques.RawFluxTechnique;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class NatureAbilityDispatcher {

    private static final Map<NatureType, List<INatureTechnique>> TECHNIQUES_BY_NATURE = new HashMap<>();

    static {
        // Raw Flux (Unaligned humans)
        List<INatureTechnique> rawTechniques = new ArrayList<>();
        rawTechniques.add(new RawFluxTechnique());
        TECHNIQUES_BY_NATURE.put(NatureType.NONE, rawTechniques);

        // Fire
        List<INatureTechnique> fireTechniques = new ArrayList<>();
        fireTechniques.add(new FireTechnique());
        TECHNIQUES_BY_NATURE.put(NatureType.FIRE, fireTechniques);

        // Water
        List<INatureTechnique> waterTechniques = new ArrayList<>();
        waterTechniques.add(new WaterTechnique());
        TECHNIQUES_BY_NATURE.put(NatureType.WATER, waterTechniques);

        // Ice
        List<INatureTechnique> iceTechniques = new ArrayList<>();
        iceTechniques.add(new IceTechnique());
        TECHNIQUES_BY_NATURE.put(NatureType.ICE, iceTechniques);

        // Earth
        List<INatureTechnique> earthTechniques = new ArrayList<>();
        earthTechniques.add(new EarthTechnique());
        TECHNIQUES_BY_NATURE.put(NatureType.EARTH, earthTechniques);

        // Space
        List<INatureTechnique> spaceTechniques = new ArrayList<>();
        spaceTechniques.add(new SpaceTechnique());
        TECHNIQUES_BY_NATURE.put(NatureType.SPACE, spaceTechniques);

        // Electricity
        List<INatureTechnique> electricityTechniques = new ArrayList<>();
        electricityTechniques.add(new ElectricityTechnique());
        TECHNIQUES_BY_NATURE.put(NatureType.ELECTRICITY, electricityTechniques);

        // Light
        List<INatureTechnique> lightTechniques = new ArrayList<>();
        lightTechniques.add(new LightTechnique());
        TECHNIQUES_BY_NATURE.put(NatureType.LIGHT, lightTechniques);

        // Dark
        List<INatureTechnique> darkTechniques = new ArrayList<>();
        darkTechniques.add(new DarkTechnique());
        TECHNIQUES_BY_NATURE.put(NatureType.DARK, darkTechniques);
    }

    public static List<INatureTechnique> getTechniques(NatureType nature) {
        return TECHNIQUES_BY_NATURE.getOrDefault(nature, new ArrayList<>());
    }

    public static void dispatchAbility(NatureType nature, int techniqueIndex, LivingEntity executor, double targetX,
            double targetY, double targetZ) {
        List<INatureTechnique> techniques = getTechniques(nature);

        if (techniqueIndex >= 0 && techniqueIndex < techniques.size()) {
            INatureTechnique technique = techniques.get(techniqueIndex);
            technique.execute(executor, new Vec3(targetX, targetY, targetZ));
        } else {
            System.out.println("Invalid technique index: " + techniqueIndex);
        }
    }
}

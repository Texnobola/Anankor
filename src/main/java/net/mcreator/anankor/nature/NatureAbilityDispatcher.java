package net.mcreator.anankor.nature;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.mcreator.anankor.nature.techniques.FireTechnique;
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

        // The remaining 7 canonical Natures (ready to add techniques as you make them)
        TECHNIQUES_BY_NATURE.put(NatureType.WATER, new ArrayList<>());
        TECHNIQUES_BY_NATURE.put(NatureType.ICE, new ArrayList<>());
        TECHNIQUES_BY_NATURE.put(NatureType.EARTH, new ArrayList<>());
        TECHNIQUES_BY_NATURE.put(NatureType.SPACE, new ArrayList<>());
        TECHNIQUES_BY_NATURE.put(NatureType.ELECTRICITY, new ArrayList<>());
        TECHNIQUES_BY_NATURE.put(NatureType.LIGHT, new ArrayList<>());
        TECHNIQUES_BY_NATURE.put(NatureType.DARK, new ArrayList<>());
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

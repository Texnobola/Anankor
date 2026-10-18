package net.mcreator.anankor.client;

import com.zigythebird.playeranim.animation.PlayerAnimationController;
import com.zigythebird.playeranim.api.PlayerAnimationAccess;
import com.zigythebird.playeranim.api.PlayerAnimationFactory;
import com.zigythebird.playeranimcore.enums.PlayState;
import net.mcreator.anankor.AnankorMod;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Avatar;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

import com.zigythebird.playeranimcore.api.firstPerson.FirstPersonConfiguration;
import com.zigythebird.playeranimcore.api.firstPerson.FirstPersonMode;

/**
 * Client-side lifecycle handler and animation trigger manager for
 * ZigyTheBird's PlayerAnimationLibrary (PAL).
 */
@EventBusSubscriber(modid = AnankorMod.MODID, value = Dist.CLIENT)
public class AnankorPlayerAnimationHandler {

    /**
     * The primary animation layer for Anankor techniques and player abilities.
     */
    public static final Identifier ANIMATION_LAYER_ID = Identifier.fromNamespaceAndPath(AnankorMod.MODID,
            "player_animations");

    /**
     * Registers the player animation controller layer with priority 1500 during
     * client setup, configuring first person mode and hand visibility.
     */
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            PlayerAnimationFactory.ANIMATION_DATA_FACTORY.registerFactory(
                    ANIMATION_LAYER_ID,
                    1500, // Gameplay priority
                    avatar -> {
                        PlayerAnimationController controller = new PlayerAnimationController(avatar,
                                (c, state, setter) -> PlayState.STOP);
                        controller.setFirstPersonMode(FirstPersonMode.THIRD_PERSON_MODEL);
                        controller.setFirstPersonConfiguration(new FirstPersonConfiguration()
                                .setShowRightArm(true)
                                .setShowLeftArm(true)
                                .setShowRightItem(false)
                                .setShowLeftItem(false));
                        controller.setFirstPersonFollowsCamera(true);
                        controller.setFirstPersonTransitionLength(2);
                        return controller;
                    });
        });
    }

    /**
     * Plays an animation on the specified player avatar by animation name within
     * the anankor namespace.
     *
     * @param avatar        The player avatar entity
     * @param animationName The name of the animation (e.g. "slash_fire") as
     *                      declared inside the JSON file
     * @return true if the animation was successfully triggered, false otherwise
     */
    public static boolean playAnimation(Avatar avatar, String animationName) {
        if (avatar == null)
            return false;
        PlayerAnimationController controller = (PlayerAnimationController) PlayerAnimationAccess
                .getPlayerAnimationLayer(avatar, ANIMATION_LAYER_ID);
        if (controller != null) {
            return controller.triggerAnimation(Identifier.fromNamespaceAndPath(AnankorMod.MODID, animationName));
        }
        return false;
    }

    /**
     * Plays an animation on the specified player avatar with a full Identifier.
     *
     * @param avatar      The player avatar entity
     * @param animationId Full animation Identifier
     * @return true if the animation was successfully triggered, false otherwise
     */
    public static boolean playAnimation(Avatar avatar, Identifier animationId) {
        if (avatar == null)
            return false;
        PlayerAnimationController controller = (PlayerAnimationController) PlayerAnimationAccess
                .getPlayerAnimationLayer(avatar, ANIMATION_LAYER_ID);
        if (controller != null) {
            return controller.triggerAnimation(animationId);
        }
        return false;
    }

    /**
     * Stops currently playing animations on the player's Anankor layer.
     *
     * @param avatar The player avatar entity
     */
    public static void stopAnimation(Avatar avatar) {
        if (avatar == null)
            return;
        PlayerAnimationController controller = (PlayerAnimationController) PlayerAnimationAccess
                .getPlayerAnimationLayer(avatar, ANIMATION_LAYER_ID);
        if (controller != null) {
            controller.stop();
        }
    }
}

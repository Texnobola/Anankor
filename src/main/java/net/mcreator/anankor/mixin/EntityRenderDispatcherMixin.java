package net.mcreator.anankor.mixin;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.world.entity.player.Player;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.Minecraft;

import net.mcreator.anankor.AnankorModPlayerAnimationAPI;

import com.mojang.blaze3d.vertex.PoseStack;

@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderDispatcherMixin {
	private static Minecraft mc = Minecraft.getInstance();

	@Inject(method = "submit", at = @At("HEAD"), cancellable = true)
	private static void renderShadow(EntityRenderState entityRenderState, CameraRenderState camera, double x, double y, double z, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CallbackInfo ci) {
		if (entityRenderState instanceof AvatarRenderState state) {
			Player player = (Player) state.getRenderData(AnankorModPlayerAnimationAPI.ClientAttachments.PLAYER);
			if (player != null && player.getPersistentData().getBooleanOr("FirstPersonAnimation", false) && mc.options.getCameraType().isFirstPerson() && player == mc.player && (mc.screen == null || mc.screen instanceof ChatScreen)) {
				state.shadowPieces.clear();
			}
		}
	}
}
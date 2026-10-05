package net.mcreator.anankor.mixin;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.player.Player;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.Minecraft;

import net.mcreator.anankor.AnankorModPlayerAnimationAPI;

import com.mojang.math.Axis;
import com.mojang.blaze3d.vertex.PoseStack;

@Mixin(AvatarRenderer.class)
public abstract class PlayerAnimationRendererMixin extends LivingEntityRenderer<AbstractClientPlayer, AvatarRenderState, PlayerModel> {
	private String master = null;
	private Minecraft mc = Minecraft.getInstance();

	public PlayerAnimationRendererMixin(EntityRendererProvider.Context context, boolean slim) {
		super(null, null, 0.5f);
	}

	@Inject(method = "Lnet/minecraft/client/renderer/entity/player/AvatarRenderer;setupRotations(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;FF)V", at = @At("RETURN"))
	private void setupRotations(AvatarRenderState renderState, PoseStack poseStack, float bodyRot, float scale_, CallbackInfo ci) {
		Player player = (Player) renderState.getRenderData(AnankorModPlayerAnimationAPI.ClientAttachments.PLAYER);
		if (player == null)
			return;
		if (master == null) {
			if (!AnankorModPlayerAnimationAPI.animations.isEmpty())
				master = "anankor";
			else
				return;
		}
		if (!master.equals("anankor"))
			return;
		AnankorModPlayerAnimationAPI.PlayerAnimation animation = AnankorModPlayerAnimationAPI.active_animations.get(player);
		if (animation == null)
			return;
		AnankorModPlayerAnimationAPI.PlayerBone bone = animation.bones.get("body");
		boolean firstPerson = player.getPersistentData().getBooleanOr("FirstPersonAnimation", false) && mc.options.getCameraType().isFirstPerson() && player == mc.player && (mc.screen == null || mc.screen instanceof ChatScreen);
		if (bone == null && !firstPerson)
			return;
		if (bone != null) {
			float animationProgress = player.getPersistentData().getFloatOr("PlayerAnimationProgress", 0);
			Vec3 scale = AnankorModPlayerAnimationAPI.PlayerBone.interpolate(bone.scales, animationProgress, player);
			if (scale != null) {
				poseStack.scale((float) scale.x, (float) scale.y, (float) scale.z);
			}
			Vec3 position = AnankorModPlayerAnimationAPI.PlayerBone.interpolate(bone.positions, animationProgress, player);
			if (position != null) {
				if (!firstPerson)
					poseStack.translate((float) -position.x * 0.0625f, (float) (position.y * 0.0625f) + 0.75f, (float) position.z * 0.0625f);
			}
			Vec3 rotation = AnankorModPlayerAnimationAPI.PlayerBone.interpolate(bone.rotations, animationProgress, player);
			if (rotation != null) {
				if (!firstPerson)
					poseStack.mulPose(Axis.ZP.rotationDegrees((float) rotation.z));
				poseStack.mulPose(Axis.YP.rotationDegrees((float) -rotation.y));
				if (!firstPerson)
					poseStack.mulPose(Axis.XP.rotationDegrees((float) -rotation.x));
			}
			if (position != null) {
				if (!firstPerson)
					poseStack.translate(0, -0.75f, 0);
			}
		}
		if (firstPerson && scale_ != 69) {
			poseStack.mulPose(Axis.YP.rotationDegrees(bodyRot - player.getYRot()));
			poseStack.translate(0, 1.5f, 0);
			poseStack.mulPose(Axis.XP.rotationDegrees(-player.getXRot()));
			poseStack.translate(0, -1.5f, 0);
		}
	}
}
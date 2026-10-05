package net.mcreator.anankor.mixin;

import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.Mixin;

import org.objectweb.asm.Opcodes;

import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.layers.PlayerItemInHandLayer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.Minecraft;

import net.mcreator.anankor.AnankorModPlayerAnimationAPI;

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin {
	private String master = null;
	private Minecraft mc = Minecraft.getInstance();

	@ModifyExpressionValue(method = "Lnet/minecraft/client/renderer/entity/LivingEntityRenderer;submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/entity/LivingEntityRenderer;layers:Ljava/util/List;", opcode = Opcodes.GETFIELD))
	private List<Object> filterLayers(List<Object> originalLayers, LivingEntityRenderState entityRenderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
		if (master == null) {
			if (!AnankorModPlayerAnimationAPI.animations.isEmpty())
				master = "anankor";
			else
				return originalLayers;
		}
		if (!master.equals("anankor")) {
			return originalLayers;
		}
		if (entityRenderState instanceof AvatarRenderState renderState && mc.options.getCameraType().isFirstPerson()) {
			Player player = (Player) renderState.getRenderData(AnankorModPlayerAnimationAPI.ClientAttachments.PLAYER);
			if (player == null)
				return originalLayers;
			if (mc.player == player && (mc.screen == null || mc.screen instanceof ChatScreen)) {
				CompoundTag playerData = player.getPersistentData();
				if (playerData.getBooleanOr("FirstPersonAnimation", false)) {
					playerData.putInt("setNullRender", 1);
					return originalLayers.stream().filter(layer -> layer instanceof PlayerItemInHandLayer).toList();
				} else if (playerData.contains("setNullRender")) {
					int ticks = playerData.getIntOr("setNullRender", 0);
					if (ticks <= 0) {
						playerData.remove("setNullRender");
					} else {
						playerData.putInt("setNullRender", ticks - 1);
						return List.of();
					}
				}
			}
		}
		return originalLayers;
	}
}
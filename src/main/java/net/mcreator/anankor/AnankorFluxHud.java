package net.mcreator.anankor.client;

import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.api.distmarker.Dist;

import net.minecraft.world.entity.player.Player;
import net.minecraft.resources.Identifier;
import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Font;
import net.minecraft.client.Minecraft;
import net.minecraft.client.DeltaTracker;

import net.mcreator.anankor.network.AnankorModVariables;
import net.mcreator.anankor.AnankorMod;

/**
 * Client-side Flux HUD for Anankor.
 *
 * Purely visual: it only READS the existing PLAYER_VARIABLES attachment
 * (flux / maxflux), which the server already syncs to the client through
 * AnankorModVariables.PlayerVariablesSyncMessage. No gameplay logic here.
 *
 * Registered as a NeoForge GUI layer (RegisterGuiLayersEvent) on the client
 * dist only, so this class is never loaded on a dedicated server.
 */
@EventBusSubscriber(Dist.CLIENT)
public class AnankorFluxHud {
	public static final Identifier LAYER_ID = Identifier.fromNamespaceAndPath(AnankorMod.MODID, "flux_hud");

	// Layout, in GUI-scaled pixels (NOT monitor pixels). Anchored to the top-left corner.
	private static final int MARGIN_X = 6;
	private static final int MARGIN_Y = 6;
	private static final int PANEL_WIDTH = 110;
	private static final int PANEL_HEIGHT = 26;
	private static final int PADDING = 4;
	private static final int BAR_HEIGHT = 6;

	// Colors (ARGB)
	private static final int COLOR_PANEL_BG = 0x90000000;
	private static final int COLOR_PANEL_BORDER = 0xFF2A1A4A;
	private static final int COLOR_BAR_BG = 0xFF14101F;
	private static final int COLOR_BAR_BORDER = 0xFF000000;
	private static final int COLOR_FILL = 0xFF39C6FF;
	private static final int COLOR_FILL_HIGHLIGHT = 0xFF9BE4FF;
	private static final int COLOR_FILL_LOW = 0xFFFF4A4A;
	private static final int COLOR_FILL_LOW_HIGHLIGHT = 0xFFFF9A9A;
	private static final int COLOR_LABEL = 0xFF7FDBFF;
	private static final int COLOR_VALUE = 0xFFFFFFFF;

	private static final float LOW_FLUX_THRESHOLD = 0.25f;

	// Easing state (visual only): the bar glides toward the real value instead of snapping.
	private static float displayedFraction = -1.0f;
	private static long lastRenderNanos = 0L;

	@SubscribeEvent
	public static void registerGuiLayers(RegisterGuiLayersEvent event) {
		// Rendered after the vanilla hotbar/health/XP/effects layers, but below chat and the tab list.
		event.registerBelow(VanillaGuiLayers.CHAT, LAYER_ID, AnankorFluxHud::render);
	}

	private static void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
		Minecraft mc = Minecraft.getInstance();
		Player player = mc.player;
		// Modded layers are not gated by vanilla's "hide GUI" (F1) flag, so check it ourselves.
		if (player == null || mc.options.hideGui || player.isSpectator())
			return;

		AnankorModVariables.PlayerVariables vars = player.getData(AnankorModVariables.PLAYER_VARIABLES);
		double flux = vars.flux;
		double maxFlux = vars.maxflux;

		float targetFraction = computeFraction(flux, maxFlux);
		float fraction = ease(targetFraction);

		int shownFlux = (int) Math.floor(Math.max(0.0, flux));
		int shownMax = (int) Math.floor(Math.max(0.0, maxFlux));

		int x = MARGIN_X;
		int y = MARGIN_Y;
		Font font = mc.font;

		// Panel background + 1px border
		graphics.fill(x, y, x + PANEL_WIDTH, y + PANEL_HEIGHT, COLOR_PANEL_BORDER);
		graphics.fill(x + 1, y + 1, x + PANEL_WIDTH - 1, y + PANEL_HEIGHT - 1, COLOR_PANEL_BG);

		// Text row: "FLUX" on the left, "current/max" right-aligned
		int textY = y + PADDING;
		graphics.text(font, Component.literal("FLUX").getVisualOrderText(), x + PADDING, textY, COLOR_LABEL, true);
		var valueText = Component.literal(shownFlux + "/" + shownMax).getVisualOrderText();
		graphics.text(font, valueText, x + PANEL_WIDTH - PADDING - font.width(valueText), textY, COLOR_VALUE, true);

		// Bar: 1px black border, dark background, filled portion = floor(innerWidth * fraction)
		int barLeft = x + PADDING;
		int barTop = y + PANEL_HEIGHT - PADDING - BAR_HEIGHT;
		int barRight = x + PANEL_WIDTH - PADDING;
		int barBottom = barTop + BAR_HEIGHT;
		graphics.fill(barLeft, barTop, barRight, barBottom, COLOR_BAR_BORDER);

		int innerLeft = barLeft + 1;
		int innerTop = barTop + 1;
		int innerRight = barRight - 1;
		int innerBottom = barBottom - 1;
		graphics.fill(innerLeft, innerTop, innerRight, innerBottom, COLOR_BAR_BG);

		int innerWidth = innerRight - innerLeft;
		int filledWidth = (int) Math.floor(innerWidth * fraction);
		if (filledWidth > 0) {
			boolean low = targetFraction <= LOW_FLUX_THRESHOLD;
			int fillColor = low ? COLOR_FILL_LOW : COLOR_FILL;
			int highlightColor = low ? COLOR_FILL_LOW_HIGHLIGHT : COLOR_FILL_HIGHLIGHT;
			graphics.fill(innerLeft, innerTop, innerLeft + filledWidth, innerBottom, fillColor);
			graphics.fill(innerLeft, innerTop, innerLeft + filledWidth, innerTop + 1, highlightColor);
		}
	}

	/** flux / maxflux, clamped to [0, 1]. Safe against maxflux <= 0, NaN and infinity. */
	private static float computeFraction(double flux, double maxFlux) {
		if (!(maxFlux > 0.0) || Double.isNaN(flux) || Double.isInfinite(maxFlux))
			return 0.0f;
		double fraction = flux / maxFlux;
		if (Double.isNaN(fraction))
			return 0.0f;
		return (float) Math.max(0.0, Math.min(1.0, fraction));
	}

	/** Frame-rate independent exponential easing toward the target. Visual only. */
	private static float ease(float target) {
		long now = System.nanoTime();
		if (displayedFraction < 0.0f || lastRenderNanos == 0L) {
			displayedFraction = target;
		} else {
			float dtSeconds = Math.min((now - lastRenderNanos) / 1.0E9f, 0.25f);
			float blend = 1.0f - (float) Math.exp(-12.0f * dtSeconds);
			displayedFraction += (target - displayedFraction) * blend;
			if (Math.abs(target - displayedFraction) < 0.001f)
				displayedFraction = target;
		}
		lastRenderNanos = now;
		return displayedFraction;
	}
}

package net.mcreator.anankor.client;

import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.api.distmarker.Dist;

import net.minecraft.world.entity.player.Player;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.resources.Identifier;
import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Font;
import net.minecraft.client.Minecraft;
import net.minecraft.client.DeltaTracker;
import net.minecraft.ChatFormatting;

import net.mcreator.anankor.network.AnankorModVariables;
import net.mcreator.anankor.AnankorMod;

/**
 * Anankor Flux HUD v2 (client-side, visual only).
 *
 * Reads the existing PLAYER_VARIABLES attachment (flux / maxflux), which the server
 * already syncs to the client. No gameplay logic, no new variables, no new packets.
 * Registered as a NeoForge GUI layer (RegisterGuiLayersEvent) on the client dist only.
 *
 * Everything is drawn procedurally with GuiGraphicsExtractor.fill / fillGradient / text,
 * in GUI-scaled coordinates, so it follows the Minecraft GUI Scale setting.
 */
@EventBusSubscriber(Dist.CLIENT)
public class AnankorFluxHud {
	public static final Identifier LAYER_ID = Identifier.fromNamespaceAndPath(AnankorMod.MODID, "flux_hud");

	// ---- Layout (GUI-scaled pixels), anchored to the top-left corner ----
	private static final int MARGIN_X = 6;
	private static final int MARGIN_Y = 6;
	private static final int WIDTH = 128;
	private static final int HEIGHT = 40;
	private static final int CUT = 4; // size of the angular corner cuts (top-left and bottom-right)

	private static final int MARKER_X = 4; // left energy marker, relative to panel
	private static final int MARKER_TOP = 6;
	private static final int MARKER_SEGMENTS = 5;
	private static final int CONTENT_X = 13; // content start, relative to panel
	private static final int CONTENT_RIGHT_PAD = 7;

	private static final int HEADER_Y = 5; // "ANANKOR"
	private static final int LABEL_Y = 13; // "FLUX" and the numbers
	private static final int BAR_Y = 26;
	private static final int BAR_HEIGHT = 9; // 1px border + 7px interior + 1px border
	private static final int BAR_DIVISIONS = 10; // one notch every 10%
	private static final float SMALL_TEXT_SCALE = 0.75f;

	// ---- Palette (kept deliberately small): charcoal / violet / cyan / crimson ----
	private static final int PANEL_BORDER = 0xFF2A1B4A;
	private static final int PANEL_BG = 0xE00A0812;
	private static final int PANEL_TOP_GLOW = 0x3A3A1F7A;
	private static final int PANEL_ACCENT = 0xFF6A45D0;
	private static final int SEPARATOR = 0xFF2C2150;
	private static final int TRACK = 0xFF0B0914;
	private static final int TRACK_SHADOW = 0xFF030208;
	private static final int TRACK_DIVISION = 0xFF1B1433;
	private static final int BAR_BORDER = 0xFF4A3590;
	private static final int MARKER_OFF = 0xFF171226;

	private static final int CYAN_HILITE = 0xFFB8F1FF;
	private static final int CYAN_MAIN = 0xFF2BB5F0;
	private static final int CYAN_DARK = 0xFF1A6FA8;
	private static final int CYAN_DEEP = 0xFF124A78;
	private static final int CYAN_CAP = 0xFFE6FBFF;
	private static final int CYAN_GHOST = 0xFF1D4F70;

	private static final int RED_HILITE = 0xFFFF9AA6;
	private static final int RED_MAIN = 0xFFD42B3F;
	private static final int RED_DARK = 0xFF8E1426;
	private static final int RED_DEEP = 0xFF5E0C1A;
	private static final int RED_CAP = 0xFFFFD6DB;
	private static final int RED_GHOST = 0xFF5A1A28;
	private static final int RED_BORDER = 0xFF5A1422;

	private static final int TEXT_HEADER = 0xFF8B7BB8;
	private static final int TEXT_LABEL = 0xFFD7F6FF;
	private static final int TEXT_CURRENT = 0xFFE8F8FF;
	private static final int TEXT_CURRENT_LOW = 0xFFFF8896;
	private static final int TEXT_MAX = 0xFF7F78A0;

	private static final float LOW_FLUX_THRESHOLD = 0.25f;
	private static final double PULSE_PERIOD_SECONDS = 1.2;
	private static final double GLINT_PERIOD_SECONDS = 2.6;

	// ---- Static text (built once) ----
	private static final FormattedCharSequence HEADER_SEQ = Component.literal("ANANKOR").getVisualOrderText();
	private static final FormattedCharSequence LABEL_SEQ = Component.literal("FLUX").withStyle(ChatFormatting.BOLD).getVisualOrderText();

	// ---- Visual-only animation / cache state ----
	private static float displayedFraction = -1.0f;
	private static float ghostFraction = -1.0f; // slow trailing "drain" shown when Flux drops
	private static long lastRenderNanos = 0L;

	private static int cachedFlux = -1;
	private static int cachedMax = -1;
	private static FormattedCharSequence currentSeq = null;
	private static FormattedCharSequence maxSeq = null;
	private static int currentWidth = 0;
	private static int maxWidth = 0;
	private static int headerWidth = -1;

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

		long now = System.nanoTime();
		float target = computeFraction(flux, maxFlux);
		step(target, now);
		float fraction = displayedFraction;
		float ghost = ghostFraction;

		int shownFlux = (int) Math.floor(Math.max(0.0, flux));
		int shownMax = (int) Math.floor(Math.max(0.0, maxFlux));

		Font font = mc.font;
		refreshTextCache(font, shownFlux, shownMax);

		double seconds = now / 1.0E9;
		boolean low = target <= LOW_FLUX_THRESHOLD;
		float pulse = low ? 0.5f + 0.5f * (float) Math.sin(seconds * (Math.PI * 2.0) / PULSE_PERIOD_SECONDS) : 0.0f;

		// Active energy palette (cyan normally, crimson when dangerously low).
		int hilite = low ? mix(RED_HILITE, RED_CAP, pulse * 0.4f) : CYAN_HILITE;
		int main = low ? mix(RED_MAIN, RED_HILITE, pulse * 0.22f) : CYAN_MAIN;
		int dark = low ? RED_DARK : CYAN_DARK;
		int deep = low ? RED_DEEP : CYAN_DEEP;
		int cap = low ? RED_CAP : CYAN_CAP;
		int ghostColor = low ? RED_GHOST : CYAN_GHOST;
		int frameColor = low ? mix(PANEL_BORDER, RED_BORDER, 0.45f + 0.45f * pulse) : PANEL_BORDER;
		int accentColor = low ? mix(PANEL_ACCENT, RED_MAIN, 0.75f) : PANEL_ACCENT;

		int x = MARGIN_X;
		int y = MARGIN_Y;
		int contentLeft = x + CONTENT_X;
		int contentRight = x + WIDTH - CONTENT_RIGHT_PAD;

		drawFrame(graphics, x, y, frameColor, accentColor);
		drawMarker(graphics, x, y, fraction, main, hilite);
		drawHeader(graphics, font, x, y, contentLeft, contentRight);
		drawLabels(graphics, font, y, contentLeft, contentRight, low);
		drawBar(graphics, contentLeft, y + BAR_Y, contentRight, fraction, ghost, now, seconds, hilite, main, dark, deep, cap, ghostColor);
	}

	// ------------------------------------------------------------------ frame

	private static void drawFrame(GuiGraphicsExtractor g, int x, int y, int borderColor, int accentColor) {
		// Layered border: outer shape in border color, inner shape (inset 1px) in near-black.
		chamferedRect(g, x, y, WIDTH, HEIGHT, CUT, borderColor);
		chamferedRect(g, x + 1, y + 1, WIDTH - 2, HEIGHT - 2, CUT - 1, PANEL_BG);

		// Subtle internal shading: faint violet glow at the top, shadow at the bottom.
		// (Kept clear of the cut corners.)
		g.fillGradient(x + CUT + 1, y + 2, x + WIDTH - 2, y + 11, PANEL_TOP_GLOW, 0x003A1F7A);
		g.fillGradient(x + 2, y + HEIGHT - 10, x + WIDTH - CUT - 1, y + HEIGHT - 2, 0x00000000, 0x66000000);

		// Thin bright accent lines: top edge, a short tick on the left edge, and the bottom-right corner.
		g.fill(x + CUT + 3, y, x + CUT + 3 + 34, y + 1, accentColor);
		g.fill(x, y + CUT + 3, x + 1, y + CUT + 3 + 9, accentColor);
		g.fill(x + WIDTH - CUT - 18, y + HEIGHT - 1, x + WIDTH - CUT, y + HEIGHT, accentColor);
	}

	/** Rectangle with the top-left and bottom-right corners cut diagonally. Uses 2*cut+1 fills. */
	private static void chamferedRect(GuiGraphicsExtractor g, int x, int y, int w, int h, int cut, int color) {
		g.fill(x, y + cut, x + w, y + h - cut, color);
		for (int r = 0; r < cut; r++) {
			g.fill(x + (cut - r), y + r, x + w, y + r + 1, color); // top rows, left corner cut
			g.fill(x, y + h - 1 - r, x + w - (cut - r), y + h - r, color); // bottom rows, right corner cut
		}
	}

	// ----------------------------------------------------------------- marker

	/** Vertical 5-segment energy indicator on the left; fills bottom-up with the Flux fraction. */
	private static void drawMarker(GuiGraphicsExtractor g, int x, int y, float fraction, int main, int hilite) {
		int left = x + MARKER_X;
		for (int i = 0; i < MARKER_SEGMENTS; i++) {
			int top = y + MARKER_TOP + (MARKER_SEGMENTS - 1 - i) * 6;
			float start = i / (float) MARKER_SEGMENTS;
			float end = (i + 1) / (float) MARKER_SEGMENTS;
			if (fraction >= end - 0.0001f) {
				g.fill(left, top, left + 3, top + 5, main);
				g.fill(left, top, left + 3, top + 1, hilite);
			} else if (fraction > start) {
				float t = (fraction - start) * MARKER_SEGMENTS;
				g.fill(left, top, left + 3, top + 5, mix(MARKER_OFF, main, t));
			} else {
				g.fill(left, top, left + 3, top + 5, MARKER_OFF);
			}
		}
		// Thin divider between marker and content.
		g.fill(x + 9, y + MARKER_TOP, x + 10, y + MARKER_TOP + 29, SEPARATOR);
	}

	// ------------------------------------------------------------------- text

	private static void drawHeader(GuiGraphicsExtractor g, Font font, int x, int y, int left, int right) {
		if (headerWidth < 0)
			headerWidth = (int) Math.ceil(font.width(HEADER_SEQ) * SMALL_TEXT_SCALE);

		// Small secondary identifier, drawn at 0.75 scale.
		g.pose().pushMatrix();
		g.pose().translate((float) left, (float) (y + HEADER_Y));
		g.pose().scale(SMALL_TEXT_SCALE, SMALL_TEXT_SCALE);
		g.text(font, HEADER_SEQ, 0, 0, TEXT_HEADER, false);
		g.pose().popMatrix();

		// Thin separator running out to the right edge, closed by a small end tick.
		g.fill(left + headerWidth + 4, y + HEADER_Y + 3, right, y + HEADER_Y + 4, SEPARATOR);
		g.fill(right - 1, y + HEADER_Y + 1, right, y + HEADER_Y + 6, TEXT_HEADER);
	}

	private static void drawLabels(GuiGraphicsExtractor g, Font font, int y, int left, int right, boolean low) {
		int textY = y + LABEL_Y;
		g.text(font, LABEL_SEQ, left, textY, TEXT_LABEL, true);

		// "current / max": current is bright, "/ max" is dimmed so it never overpowers FLUX.
		int maxX = right - maxWidth;
		int currentX = maxX - currentWidth;
		g.text(font, currentSeq, currentX, textY, low ? TEXT_CURRENT_LOW : TEXT_CURRENT, true);
		g.text(font, maxSeq, maxX, textY, TEXT_MAX, true);
	}

	private static void refreshTextCache(Font font, int shownFlux, int shownMax) {
		if (shownFlux == cachedFlux && shownMax == cachedMax && currentSeq != null)
			return;
		cachedFlux = shownFlux;
		cachedMax = shownMax;
		currentSeq = Component.literal(String.valueOf(shownFlux)).getVisualOrderText();
		maxSeq = Component.literal(" / " + shownMax).getVisualOrderText();
		currentWidth = font.width(currentSeq);
		maxWidth = font.width(maxSeq);
	}

	// -------------------------------------------------------------------- bar

	private static void drawBar(GuiGraphicsExtractor g, int left, int top, int right, float fraction, float ghost, long now, double seconds, int hilite, int main, int dark, int deep, int cap,
			int ghostColor) {
		int bottom = top + BAR_HEIGHT;

		// Thin refined border with clipped corners.
		g.fill(left + 1, top, right - 1, bottom, BAR_BORDER);
		g.fill(left, top + 1, right, bottom - 1, BAR_BORDER);

		int innerLeft = left + 1;
		int innerTop = top + 1;
		int innerRight = right - 1;
		int innerBottom = bottom - 1; // interior is 7px tall
		int innerWidth = innerRight - innerLeft;

		// Dark empty track with an inner shadow along the top and faint segment divisions.
		g.fill(innerLeft, innerTop, innerRight, innerBottom, TRACK);
		g.fill(innerLeft, innerTop, innerRight, innerTop + 1, TRACK_SHADOW);
		for (int i = 1; i < BAR_DIVISIONS; i++) {
			int dx = innerLeft + Math.round(innerWidth * i / (float) BAR_DIVISIONS);
			g.fill(dx, innerTop + 1, dx + 1, innerBottom, TRACK_DIVISION);
		}

		int filledWidth = (int) Math.floor(innerWidth * fraction);
		int ghostWidth = (int) Math.floor(innerWidth * ghost);

		// Trailing drain: the previous value fades out slowly after Flux is spent.
		if (ghostWidth > filledWidth)
			g.fill(innerLeft + filledWidth, innerTop + 1, innerLeft + ghostWidth, innerBottom, ghostColor);

		if (filledWidth <= 0)
			return;

		int fillRight = innerLeft + filledWidth;
		int capX = fillRight - 1;

		// Luminous body: bright upper edge, main body, darker lower edge.
		g.fill(innerLeft, innerTop, fillRight, innerTop + 1, hilite);
		g.fill(innerLeft, innerTop + 1, fillRight, innerTop + 4, main);
		g.fill(innerLeft, innerTop + 4, fillRight, innerTop + 6, dark);
		g.fill(innerLeft, innerTop + 6, fillRight, innerBottom, deep);

		// Segment notches cut through the filled region (not over the end-cap).
		int notch = (deep & 0x00FFFFFF) | 0xA0000000;
		for (int i = 1; i < BAR_DIVISIONS; i++) {
			int dx = innerLeft + Math.round(innerWidth * i / (float) BAR_DIVISIONS);
			if (dx < capX)
				g.fill(dx, innerTop + 1, dx + 1, innerBottom, notch);
		}

		// Restrained moving glint travelling through the filled energy.
		if (filledWidth >= 10) {
			float phase = (float) ((seconds % GLINT_PERIOD_SECONDS) / GLINT_PERIOD_SECONDS);
			int gx = innerLeft + (int) (phase * (filledWidth + 20)) - 10;
			int glintSoft = 0x22E0FAFF;
			int glintCore = 0x55E0FAFF;
			fillClipped(g, gx, innerTop + 1, gx + 2, innerTop + 4, innerLeft, capX, glintSoft);
			fillClipped(g, gx + 2, innerTop + 1, gx + 5, innerTop + 4, innerLeft, capX, glintCore);
			fillClipped(g, gx + 5, innerTop + 1, gx + 7, innerTop + 4, innerLeft, capX, glintSoft);
		}

		// Sharp terminal end-cap, with a tiny nub above the bar and a faint 1px lead-in ahead of it.
		g.fill(capX, innerTop, fillRight, innerBottom, cap);
		g.fill(capX, top - 1, fillRight, top, cap);
		if (fillRight < innerRight)
			g.fill(fillRight, innerTop + 1, fillRight + 1, innerBottom - 1, (main & 0x00FFFFFF) | 0x55000000);
	}

	/** fill() clamped horizontally to [clipLeft, clipRight); does nothing if nothing remains. */
	private static void fillClipped(GuiGraphicsExtractor g, int x1, int y1, int x2, int y2, int clipLeft, int clipRight, int color) {
		int l = Math.max(x1, clipLeft);
		int r = Math.min(x2, clipRight);
		if (r > l)
			g.fill(l, y1, r, y2, color);
	}

	// ---------------------------------------------------------------- helpers

	/** flux / maxflux, clamped to [0, 1]. Safe against maxflux <= 0, NaN and infinity. */
	private static float computeFraction(double flux, double maxFlux) {
		if (!(maxFlux > 0.0) || Double.isNaN(flux) || Double.isInfinite(maxFlux))
			return 0.0f;
		double fraction = flux / maxFlux;
		if (Double.isNaN(fraction))
			return 0.0f;
		return (float) Math.max(0.0, Math.min(1.0, fraction));
	}

	/**
	 * Frame-rate independent exponential easing (visual only).
	 * The main bar glides to the real value; the ghost trail follows more slowly when Flux drops.
	 */
	private static void step(float target, long now) {
		if (displayedFraction < 0.0f || ghostFraction < 0.0f || lastRenderNanos == 0L) {
			displayedFraction = target;
			ghostFraction = target;
		} else {
			float dtSeconds = Math.min((now - lastRenderNanos) / 1.0E9f, 0.25f);
			float blend = 1.0f - (float) Math.exp(-12.0f * dtSeconds);
			displayedFraction += (target - displayedFraction) * blend;
			if (Math.abs(target - displayedFraction) < 0.001f)
				displayedFraction = target;

			if (displayedFraction >= ghostFraction) {
				ghostFraction = displayedFraction;
			} else {
				float ghostBlend = 1.0f - (float) Math.exp(-2.5f * dtSeconds);
				ghostFraction += (displayedFraction - ghostFraction) * ghostBlend;
				if (ghostFraction - displayedFraction < 0.001f)
					ghostFraction = displayedFraction;
			}
		}
		lastRenderNanos = now;
	}

	/** Linear ARGB interpolation, t clamped to [0, 1]. */
	private static int mix(int a, int b, float t) {
		float k = Math.max(0.0f, Math.min(1.0f, t));
		int aa = (a >>> 24) & 0xFF, ar = (a >>> 16) & 0xFF, ag = (a >>> 8) & 0xFF, ab = a & 0xFF;
		int ba = (b >>> 24) & 0xFF, br = (b >>> 16) & 0xFF, bg = (b >>> 8) & 0xFF, bb = b & 0xFF;
		int ra = Math.round(aa + (ba - aa) * k);
		int rr = Math.round(ar + (br - ar) * k);
		int rg = Math.round(ag + (bg - ag) * k);
		int rb = Math.round(ab + (bb - ab) * k);
		return (ra << 24) | (rr << 16) | (rg << 8) | rb;
	}
}
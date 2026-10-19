package net.mcreator.anankor.client;

import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.api.distmarker.Dist;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.resources.Identifier;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Font;
import net.minecraft.client.Minecraft;
import net.minecraft.client.DeltaTracker;

import net.mcreator.anankor.network.AnankorModVariables;
import net.mcreator.anankor.nature.NatureType;
import net.mcreator.anankor.nature.TechniqueCategory;
import net.mcreator.anankor.nature.NatureAbilityDispatcher;
import net.mcreator.anankor.nature.techniques.INatureTechnique;
import net.mcreator.anankor.init.AnankorModMobEffects;
import net.mcreator.anankor.AnankorMod;

/**
 * Anankor Elemental Living HUD v3.
 *
 * Symmetrically framed dock anchored directly above the vanilla hotbar (182px
 * wide).
 * Features:
 * - 4 Octagonal Technique Sockets ([C] Mobility, [G] Strike, [X] Ward, [Z]
 * Extension)
 * - Dynamic cooldown darkening sweep masks & remaining timers
 * - Central Crystalline Core Jewel with Nature-aligned breathing glow & faceted
 * highlights
 * - Dual-layer animated Flux gauge with trailing ghost bar, notch dividers, and
 * specular sweep
 * - Fractured crimson Burnout hazard rune and recovery countdown
 * - Infinite Flux indication mode
 */
@EventBusSubscriber(Dist.CLIENT)
public class AnankorFluxHud {
	public static final Identifier LAYER_ID = Identifier.fromNamespaceAndPath(AnankorMod.MODID, "flux_hud");

	// ---- Sizing & Geometry (182px total, identical to hotbar width) ----
	private static final int TOTAL_WIDTH = 182;
	private static final int SLOT_SIZE = 16;
	private static final int SLOT_GAP = 2;
	private static final int SLOT_GROUP_WIDTH = SLOT_SIZE * 2 + SLOT_GAP; // 34px
	private static final int CENTER_WIDTH = 104; // 34 + 5 + 104 + 5 + 34 = 182px
	private static final int HUD_HEIGHT = 18;
	private static final int BAR_DIVISIONS = 5;

	// ---- Static Dark Stone Palette ----
	private static final int STONE_OUTER = 0xFF14101F;
	private static final int STONE_BORDER = 0xFF28203D;
	private static final int STONE_BG = 0xEE0A0812;
	private static final int STONE_RECESS = 0xFF05040A;
	private static final int STONE_DIVIDER = 0xFF1C152B;

	// Low Flux Hazard Palette
	private static final int RED_HILITE = 0xFFFF8594;
	private static final int RED_MAIN = 0xFFE02B3F;
	private static final int RED_DARK = 0xFF8A1422;
	private static final int RED_DEEP = 0xFF4D0914;
	private static final int RED_BORDER = 0xFF6B1824;
	private static final int RED_GHOST = 0xFF521520;

	// ---- Thematic Nature Palettes ----
	public record Palette(int hilite, int main, int dark, int deep, int cap, int ghost, int accent, int border,
			int glow) {
	}

	public static Palette getPaletteForNature(NatureType nature) {
		return switch (nature) {
			case FIRE -> new Palette(0xFFFFD199, 0xFFFF6B22, 0xFFC93208, 0xFF771202, 0xFFFFF2D4, 0xFF551A08, 0xFFFF7B1A,
					0xFF4A1A10, 0x4A7A2508);
			case WATER -> new Palette(0xFFB0F4FF, 0xFF1E9CE6, 0xFF0D5E9E, 0xFF063560, 0xFFE0FAFF, 0xFF0A3355,
					0xFF28B5FF, 0xFF0D2545, 0x4A0B4268);
			case ICE -> new Palette(0xFFE6FBFF, 0xFF7FE5FF, 0xFF3DA3C7, 0xFF1E5B75, 0xFFFFFFFF, 0xFF204859, 0xFF9EEFFF,
					0xFF183747, 0x4A226078);
			case EARTH -> new Palette(0xFFE0F5A8, 0xFF8DC435, 0xFF58821B, 0xFF2E470C, 0xFFF3FFD4, 0xFF354415,
					0xFFA9DC3E, 0xFF253314, 0x4A3A5214);
			case SPACE -> new Palette(0xFFE8B8FF, 0xFFA53DF5, 0xFF6C1DB5, 0xFF3C0C6E, 0xFFF9E6FF, 0xFF3A125E,
					0xFFC15CFF, 0xFF351252, 0x4A521685);
			case ELECTRICITY -> new Palette(0xFFFFFDC2, 0xFFFFDE26, 0xFFBFA010, 0xFF6E5A04, 0xFFFFFFE8, 0xFF524408,
					0xFFFFEA52, 0xFF3D350A, 0x4A6E5C0A);
			case LIGHT -> new Palette(0xFFFFFFFF, 0xFFFFF2A8, 0xFFD4C065, 0xFF7A6B29, 0xFFFFFFFF, 0xFF524922,
					0xFFFFF5B8, 0xFF3D371E, 0x4A756C2B);
			case DARK -> new Palette(0xFF9E84BD, 0xFF56357A, 0xFF321A4F, 0xFF180A29, 0xFFD4BFF2, 0xFF241436, 0xFF7E52AF,
					0xFF231433, 0x4A381657);
			default -> new Palette(0xFFD4CEFF, 0xFF8274CC, 0xFF54469E, 0xFF2E246B, 0xFFEBE8FF, 0xFF282052, 0xFF9E92EB,
					0xFF28203D, 0x4A3A2F6B);
		};
	}

	// Visual animation state
	private static float displayedFraction = -1.0f;
	private static float ghostFraction = -1.0f;
	private static long lastRenderNanos = 0L;

	@SubscribeEvent
	public static void registerGuiLayers(RegisterGuiLayersEvent event) {
		event.registerBelow(VanillaGuiLayers.CHAT, LAYER_ID, AnankorFluxHud::render);
	}

	private static void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
		Minecraft mc = Minecraft.getInstance();
		Player player = mc.player;
		if (player == null || (mc.gui != null && mc.gui.hud != null && mc.gui.hud.isHidden()) || player.isSpectator()) {
			return;
		}

		AnankorModVariables.PlayerVariables vars = player.getData(AnankorModVariables.PLAYER_VARIABLES);
		double flux = vars.flux;
		double maxFlux = vars.maxflux;
		boolean infiniteFlux = vars.infiniteFlux;

		long now = System.nanoTime();
		float target = computeFraction(flux, maxFlux);
		step(target, now);
		float fraction = displayedFraction;
		float ghost = ghostFraction;

		double seconds = now / 1.0E9;
		NatureType nature = NatureType.fromString(vars.Nature);
		Palette pal = getPaletteForNature(nature);

		MobEffectInstance burnoutEffect = player.getEffect(AnankorModMobEffects.BURNOUT);
		boolean isBurnout = burnoutEffect != null;
		int burnoutTicks = isBurnout ? burnoutEffect.getDuration() : 0;

		boolean isLow = !infiniteFlux && !isBurnout && (target <= 0.25f);
		float pulse = isLow ? 0.5f + 0.5f * (float) Math.sin(seconds * 6.0) : 0.0f;

		// Frame color overrides when low or in burnout
		int frameColor = isBurnout ? RED_BORDER
				: (isLow ? mix(pal.border(), RED_BORDER, 0.5f + 0.5f * pulse) : pal.border());
		int accentColor = isBurnout ? RED_MAIN : (isLow ? mix(pal.accent(), RED_MAIN, 0.7f) : pal.accent());

		// Positioning: centered above hotbar & status rows
		int guiW = graphics.guiWidth();
		int guiH = graphics.guiHeight();
		int startX = (guiW - TOTAL_WIDTH) / 2;

		// Adaptive vertical offset based on armor or drowning bubbles to maintain
		// perfect vanilla spacing
		boolean hasArmor = player.getArmorValue() > 0;
		boolean isDrowning = player.getAirSupply() < player.getMaxAirSupply();
		int baseY = (hasArmor || isDrowning) ? guiH - 60 : guiH - 53;

		Font font = mc.font;
		int activeSlot = (int) vars.selectedTechnique;

		// -------------------------------------------------------------
		// 1. LEFT TECHNIQUE SLOTS (Slot 0: Mobility, Slot 1: Strike)
		// -------------------------------------------------------------
		int leftSlot0X = startX;
		int leftSlot1X = startX + SLOT_SIZE + SLOT_GAP;
		drawTechniqueSlot(graphics, font, leftSlot0X, baseY + 1, 0, "M", "C", TechniqueCategory.MOBILITY, nature, pal,
				isBurnout, activeSlot == 0, seconds);
		drawTechniqueSlot(graphics, font, leftSlot1X, baseY + 1, 1, "S", "G", TechniqueCategory.STRIKE, nature, pal,
				isBurnout, activeSlot == 1, seconds);

		// -------------------------------------------------------------
		// 2. CENTRAL FLUX GAUGE & NATURE CORE GEM
		// -------------------------------------------------------------
		int centerBarX = startX + SLOT_GROUP_WIDTH + 5;
		drawCentralPanel(graphics, font, centerBarX, baseY, CENTER_WIDTH, HUD_HEIGHT, fraction, ghost, infiniteFlux,
				(int) Math.max(0, flux), (int) maxFlux, nature, pal, isBurnout, isLow, pulse, seconds);

		// -------------------------------------------------------------
		// 3. RIGHT TECHNIQUE SLOTS (Slot 2: Ward, Slot 3: Extension)
		// -------------------------------------------------------------
		int rightSlot2X = centerBarX + CENTER_WIDTH + 5;
		int rightSlot3X = rightSlot2X + SLOT_SIZE + SLOT_GAP;
		drawTechniqueSlot(graphics, font, rightSlot2X, baseY + 1, 2, "W", "X", TechniqueCategory.WARD, nature, pal,
				isBurnout, activeSlot == 2, seconds);
		drawTechniqueSlot(graphics, font, rightSlot3X, baseY + 1, 3, "E", "Z", TechniqueCategory.EXTENSION, nature, pal,
				isBurnout, activeSlot == 3, seconds);

		// -------------------------------------------------------------
		// 4. BURNOUT HAZARD BANNER (if collapsed)
		// -------------------------------------------------------------
		if (isBurnout) {
			drawBurnoutIndicator(graphics, font, guiW / 2, baseY - 13, burnoutTicks, seconds);
		}
	}

	// =================================================================
	// CENTRAL PANEL & FLUX GAUGE RENDERING
	// =================================================================
	private static void drawCentralPanel(GuiGraphicsExtractor g, Font font, int x, int y, int w, int h,
			float fraction, float ghost, boolean infinite, int flux, int maxFlux,
			NatureType nature, Palette pal, boolean isBurnout, boolean low, float pulse, double seconds) {

		// Outer dark stone chamfered frame
		int borderColor = isBurnout ? RED_BORDER : (low ? mix(pal.border(), RED_BORDER, pulse) : pal.border());
		chamferedRect(g, x, y, w, h, 3, borderColor);
		chamferedRect(g, x + 1, y + 1, w - 2, h - 2, 2, STONE_BG);

		// Top decorative accent glow
		int glow = isBurnout ? 0x4A6B1824 : pal.glow();
		g.fillGradient(x + 3, y + 1, x + w - 3, y + 4, glow, 0x00000000);

		// Central Crystalline Nature Gem
		int gemSize = 10;
		int gemX = x + (w - gemSize) / 2;
		int gemY = y - 4;
		drawNatureGem(g, font, gemX, gemY, gemSize, nature, pal, isBurnout, seconds);

		// Flux Track Dimensions
		int barX = x + 4;
		int barY = y + 7;
		int barW = w - 8;
		int barH = 7;

		// Dark stone recess for the bar
		g.fill(barX, barY, barX + barW, barY + barH, STONE_RECESS);
		g.fill(barX, barY, barX + barW, barY + 1, STONE_BORDER);

		if (isBurnout) {
			// Smoldering ash texture
			g.fill(barX + 1, barY + 1, barX + barW - 1, barY + barH - 1, 0xFF2A0A10);
		} else {
			// Trailing ghost layer (smooth drain)
			if (ghost > fraction && !infinite) {
				int ghostW = (int) Math.round((barW - 2) * Math.min(1.0f, ghost));
				g.fill(barX + 1, barY + 1, barX + 1 + ghostW, barY + barH - 1, low ? RED_GHOST : pal.ghost());
			}

			// Main filled layer
			int fillW = infinite ? (barW - 2) : (int) Math.round((barW - 2) * Math.min(1.0f, fraction));
			if (fillW > 0) {
				int mainCol = low ? mix(RED_MAIN, RED_HILITE, pulse * 0.3f) : pal.main();
				int darkCol = low ? RED_DARK : pal.dark();
				int hiliteCol = low ? RED_HILITE : pal.hilite();

				g.fillGradient(barX + 1, barY + 1, barX + 1 + fillW, barY + barH - 1, mainCol, darkCol);
				// Specular top highlight line
				g.fill(barX + 1, barY + 1, barX + 1 + fillW, barY + 2, hiliteCol);

				// Specular traveling glint sweep across bar
				double glintCycle = (seconds % 2.5) / 2.5;
				int glintPos = (int) (glintCycle * (barW + 12)) - 6;
				if (glintPos >= 0 && glintPos < fillW) {
					int gWidth = Math.min(4, fillW - glintPos);
					g.fill(barX + 1 + glintPos, barY + 1, barX + 1 + glintPos + gWidth, barY + barH - 1, 0x55FFFFFF);
				}
			}
		}

		// Division tick notches every 20%
		for (int i = 1; i < BAR_DIVISIONS; i++) {
			int tickX = barX + (barW * i) / BAR_DIVISIONS;
			g.fill(tickX, barY + 1, tickX + 1, barY + barH - 1, STONE_DIVIDER);
		}

		// Text Readout: Centered small text
		String readout;
		int textColor;
		if (isBurnout) {
			readout = "BURNOUT";
			textColor = 0xFFFF6B7A;
		} else if (infinite) {
			readout = "\u221E FLUX"; // Infinity symbol
			textColor = 0xFFFFF2A8;
		} else {
			readout = flux + " / " + maxFlux;
			textColor = low ? 0xFFFF8594 : 0xFFE0E5FF;
		}

		g.pose().pushMatrix();
		float textScale = 0.65f;
		float textW = font.width(readout) * textScale;
		float textX = x + (w - textW) / 2.0f;
		float textY = barY + 1.0f;
		g.pose().translate(textX, textY);
		g.pose().scale(textScale, textScale);
		g.text(font, readout, 0, 0, textColor, true);
		g.pose().popMatrix();
	}

	// =================================================================
	// CENTRAL CRYSTALLINE NATURE CORE GEM
	// =================================================================
	private static void drawNatureGem(GuiGraphicsExtractor g, Font font, int x, int y, int size,
			NatureType nature, Palette pal, boolean isBurnout, double seconds) {
		int center = size / 2;
		float breath = (float) Math.sin(seconds * 3.0) * 0.5f + 0.5f;

		// Octagonal gem bezel
		int bezel = isBurnout ? RED_BORDER : pal.border();
		chamferedRect(g, x, y, size, size, 2, bezel);

		// Jewel Facet Colors
		int gemCore = isBurnout ? 0xFF3D0C15 : pal.dark();
		int gemGleam = isBurnout ? RED_MAIN : (isBurnout ? RED_HILITE : mix(pal.main(), pal.hilite(), breath * 0.4f));

		g.fill(x + 2, y + 2, x + size - 2, y + size - 2, gemCore);
		// Upper specular highlight
		g.fill(x + 3, y + 2, x + size - 3, y + 3, gemGleam);
		g.fill(x + 2, y + 3, x + 3, y + size - 3, gemGleam);

		// Nature letter rune in the center (e.g. F, W, I, E, S, L, D)
		String rune = isBurnout ? "X" : (nature == NatureType.NONE ? "R" : nature.getDisplayName().substring(0, 1));
		int runeColor = isBurnout ? 0xFFFF7B88 : pal.cap();

		g.pose().pushMatrix();
		float runeScale = 0.60f;
		float rW = font.width(rune) * runeScale;
		float rX = x + (size - rW) / 2.0f;
		float rY = y + 2.5f;
		g.pose().translate(rX, rY);
		g.pose().scale(runeScale, runeScale);
		g.text(font, rune, 0, 0, runeColor, false);
		g.pose().popMatrix();
	}

	// =================================================================
	// 4-CATEGORY OCTAGONAL TECHNIQUE SOCKET
	// =================================================================
	private static void drawTechniqueSlot(GuiGraphicsExtractor g, Font font, int x, int y, int slotIndex,
			String categoryLetter, String keybind, TechniqueCategory category, NatureType nature,
			Palette pal, boolean isBurnout, boolean isSelected, double seconds) {

		// Technique instance for this slot
		INatureTechnique technique = NatureAbilityDispatcher.getTechniqueBySlot(nature, slotIndex);

		// Check cooldown progress
		float cdProgress = ClientCooldownTracker.getCooldownProgress(slotIndex);
		int remainingTicks = ClientCooldownTracker.getRemainingTicks(slotIndex);
		boolean onCd = cdProgress > 0.0f;

		// Outer bezel: Octagonal dark stone
		int bezelCol = isBurnout ? 0xFF3D1620 : (isSelected ? pal.accent() : (onCd ? 0xFF20182C : pal.border()));
		chamferedRect(g, x, y, SLOT_SIZE, SLOT_SIZE, 3, bezelCol);
		chamferedRect(g, x + 1, y + 1, SLOT_SIZE - 2, SLOT_SIZE - 2, 2, STONE_BG);

		// Selected slot gentle breathing outline
		if (isSelected && !onCd && !isBurnout) {
			float selPulse = 0.5f + 0.5f * (float) Math.sin(seconds * 5.0);
			int selCol = mix(pal.accent(), pal.hilite(), selPulse * 0.5f);
			g.fill(x + 3, y + 1, x + SLOT_SIZE - 3, y + 2, selCol);
			g.fill(x + 3, y + SLOT_SIZE - 2, x + SLOT_SIZE - 3, y + SLOT_SIZE - 1, selCol);
		}

		// Category monogram letter in center
		int textColor = isBurnout ? 0xFF5A2530 : (onCd ? 0xFF655A78 : pal.hilite());
		g.pose().pushMatrix();
		float catScale = 0.8f;
		float cw = font.width(categoryLetter) * catScale;
		g.pose().translate(x + (SLOT_SIZE - cw) / 2.0f, y + 3.0f);
		g.pose().scale(catScale, catScale);
		g.text(font, categoryLetter, 0, 0, textColor, true);
		g.pose().popMatrix();

		// Cooldown Darkening Mask Sweep
		if (onCd && !isBurnout) {
			int cdHeight = (int) Math.ceil((SLOT_SIZE - 2) * cdProgress);
			if (cdHeight > 0) {
				g.fill(x + 1, y + 1 + (SLOT_SIZE - 2 - cdHeight), x + SLOT_SIZE - 1, y + SLOT_SIZE - 1, 0xB805030A);
			}

			// Numeric countdown (e.g. "1.5s")
			String cdStr = String.format("%.1f", remainingTicks / 20.0f);
			g.pose().pushMatrix();
			float cdScale = 0.55f;
			float cdw = font.width(cdStr) * cdScale;
			g.pose().translate(x + (SLOT_SIZE - cdw) / 2.0f, y + 5.0f);
			g.pose().scale(cdScale, cdScale);
			g.text(font, cdStr, 0, 0, 0xFFFFE0E5, true);
			g.pose().popMatrix();
		}

		// Small Keybind Label at Bottom-Right Corner (C, G, X, Z)
		g.pose().pushMatrix();
		float keyScale = 0.55f;
		float kw = font.width(keybind) * keyScale;
		g.pose().translate(x + SLOT_SIZE - kw - 1.5f, y + SLOT_SIZE - 6.0f);
		g.pose().scale(keyScale, keyScale);
		g.text(font, keybind, 0, 0, onCd ? 0xFF7A6B88 : 0xFFB4C8FF, true);
		g.pose().popMatrix();
	}

	// =================================================================
	// BURNOUT HAZARD BANNER
	// =================================================================
	private static void drawBurnoutIndicator(GuiGraphicsExtractor g, Font font, int centerX, int y, int durationTicks,
			double seconds) {
		float pulse = (float) Math.sin(seconds * 8.0) * 0.5f + 0.5f;
		int secondsLeft = durationTicks / 20;
		String timeStr = secondsLeft >= 60 ? (secondsLeft / 60) + "m " + (secondsLeft % 60) + "s" : secondsLeft + "s";
		String text = "\u26A0 BURNOUT: " + timeStr;

		float scale = 0.70f;
		float tw = font.width(text) * scale;
		float tx = centerX - tw / 2.0f;

		// Pulsing dark red backing pill
		int pillW = (int) tw + 8;
		int pillX = centerX - pillW / 2;
		chamferedRect(g, pillX, y - 2, pillW, 11, 2, 0xEE1E050B);
		g.fill(pillX + 2, y - 2, pillX + pillW - 2, y - 1, mix(RED_MAIN, RED_HILITE, pulse));

		g.pose().pushMatrix();
		g.pose().translate(tx, y);
		g.pose().scale(scale, scale);
		int col = mix(0xFFFF8594, 0xFFFFFFFF, pulse * 0.4f);
		g.text(font, text, 0, 0, col, true);
		g.pose().popMatrix();
	}

	// =================================================================
	// GEOMETRY & ANIMATION UTILITIES
	// =================================================================
	private static void chamferedRect(GuiGraphicsExtractor g, int x, int y, int w, int h, int cut, int color) {
		g.fill(x, y + cut, x + w, y + h - cut, color);
		for (int r = 0; r < cut; r++) {
			g.fill(x + (cut - r), y + r, x + w - (cut - r), y + r + 1, color);
			g.fill(x + (cut - r), y + h - 1 - r, x + w - (cut - r), y + h - r, color);
		}
	}

	private static float computeFraction(double flux, double maxFlux) {
		if (maxFlux <= 0.0)
			return 0.0f;
		return (float) Math.max(0.0, Math.min(1.0, flux / maxFlux));
	}

	private static void step(float target, long now) {
		if (displayedFraction < 0.0f) {
			displayedFraction = target;
			ghostFraction = target;
			lastRenderNanos = now;
			return;
		}

		float dt = (now - lastRenderNanos) / 1.0E9f;
		lastRenderNanos = now;
		if (dt <= 0.0f)
			return;
		dt = Math.min(dt, 0.1f);

		// Instant or quick follow for primary bar
		float alpha = 1.0f - (float) Math.exp(-dt * 18.0);
		displayedFraction += (target - displayedFraction) * alpha;

		// Slower trailing follow for ghost bar
		if (target < ghostFraction) {
			float ghostAlpha = 1.0f - (float) Math.exp(-dt * 3.5);
			ghostFraction += (target - ghostFraction) * ghostAlpha;
		} else {
			ghostFraction = target;
		}
	}

	private static int mix(int c1, int c2, float t) {
		t = Math.max(0.0f, Math.min(1.0f, t));
		int a1 = (c1 >> 24) & 0xFF, r1 = (c1 >> 16) & 0xFF, g1 = (c1 >> 8) & 0xFF, b1 = c1 & 0xFF;
		int a2 = (c2 >> 24) & 0xFF, r2 = (c2 >> 16) & 0xFF, g2 = (c2 >> 8) & 0xFF, b2 = c2 & 0xFF;
		int a = (int) (a1 + (a2 - a1) * t);
		int r = (int) (r1 + (r2 - r1) * t);
		int g = (int) (g1 + (g2 - g1) * t);
		int b = (int) (b1 + (b2 - b1) * t);
		return (a << 24) | (r << 16) | (g << 8) | b;
	}
}

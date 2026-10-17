package net.mcreator.anankor.client;

import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import net.minecraft.world.entity.player.Player;
import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.Minecraft;

import net.mcreator.anankor.network.SetNatureMessage;
import net.mcreator.anankor.network.AnankorModVariables;
import net.mcreator.anankor.nature.techniques.INatureTechnique;
import net.mcreator.anankor.nature.NatureType;
import net.mcreator.anankor.nature.NatureAbilityDispatcher;
import net.mcreator.anankor.init.AnankorModMobEffects;

import java.util.List;

public class AnankorMenuScreen extends Screen {
	private static final int PANEL_W = 340;
	private static final int PANEL_H = 220;
	private static final int PANEL_BG = 0xF00A0814;
	private static final int PANEL_BORDER = 0xFF2A1B4A;
	private static final int ACCENT_DEFAULT = 0xFF6A45D0;

	public AnankorMenuScreen() {
		super(Component.literal("Anankor Communion"));
	}

	@Override
	protected void init() {
		super.init();
		int left = (this.width - PANEL_W) / 2;
		int top = (this.height - PANEL_H) / 2;

		// Close button
		this.addRenderableWidget(Button.builder(Component.literal("Close"), btn -> this.onClose())
				.bounds(left + PANEL_W - 65, top + 8, 55, 18).build());

		// Nature switcher buttons at the bottom (debug / test progression)
		NatureType[] natures = NatureType.values();
		int btnW = 34;
		int btnH = 16;
		int startX = left + (PANEL_W - (natures.length * (btnW + 2))) / 2;
		int btnY = top + PANEL_H - 26;

		for (int i = 0; i < natures.length; i++) {
			NatureType type = natures[i];
			String shortName = type == NatureType.ELECTRICITY ? "Elec" : type.getDisplayName();
			if (shortName.length() > 5)
				shortName = shortName.substring(0, 4);

			final String natName = type.name();
			this.addRenderableWidget(Button.builder(Component.literal(shortName), btn -> {
				ClientPacketDistributor.sendToServer(new SetNatureMessage(natName));
			}).bounds(startX + i * (btnW + 2), btnY, btnW, btnH).build());
		}
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		// Dark background overlay
		this.extractTransparentBackground(graphics);

		int left = (this.width - PANEL_W) / 2;
		int top = (this.height - PANEL_H) / 2;

		Player player = Minecraft.getInstance().player;
		AnankorModVariables.PlayerVariables vars = player != null ? player.getData(AnankorModVariables.PLAYER_VARIABLES)
				: null;
		NatureType nature = vars != null ? NatureType.fromString(vars.Nature) : NatureType.NONE;

		int accent = nature.getColor();

		// Outer frame
		graphics.fill(left, top, left + PANEL_W, top + PANEL_H, PANEL_BORDER);
		graphics.fill(left + 1, top + 1, left + PANEL_W - 1, top + PANEL_H - 1, PANEL_BG);

		// Accent top line and corners
		graphics.fill(left + 4, top, left + PANEL_W - 4, top + 2, accent);
		graphics.fill(left, top + 4, left + 2, top + 24, accent);
		graphics.fill(left + PANEL_W - 2, top + 4, left + PANEL_W, top + 24, accent);

		// Title & Subtitle
		graphics.text(this.font, Component.literal("ANANKOR — COSMOLOGICAL COMMUNION").getVisualOrderText(), left + 12,
				top + 10, 0xFFE0E0FF, true);
		graphics.text(this.font,
				Component.literal("Active Alignment: §l" + nature.getDisplayName().toUpperCase()).getVisualOrderText(),
				left + 12, top + 24, accent, true);

		// Divider
		graphics.fill(left + 12, top + 36, left + PANEL_W - 12, top + 37, 0xFF2C2150);

		// Left Column: Flux Status
		int colLeft = left + 14;
		int colTop = top + 44;
		graphics.text(this.font, Component.literal("§7[FLUX RESERVE]").getVisualOrderText(), colLeft, colTop,
				0xFFB0B0C0, true);

		if (vars != null) {
			int flux = (int) Math.floor(vars.flux);
			int max = (int) Math.floor(vars.maxflux);
			boolean burned = player.hasEffect(AnankorModMobEffects.BURNOUT);

			graphics.text(this.font, Component.literal("Capacity: §f" + flux + " §7/ §f" + max).getVisualOrderText(),
					colLeft, colTop + 14, 0xFFE8F8FF, true);

			// Bar
			int barW = 120;
			int barH = 8;
			graphics.fill(colLeft, colTop + 27, colLeft + barW, colTop + 27 + barH, 0xFF0B0914);
			float frac = max > 0 ? (float) Math.max(0, Math.min(1.0, (double) flux / max)) : 0f;
			int fillW = (int) (barW * frac);
			graphics.fill(colLeft, colTop + 27, colLeft + fillW, colTop + 27 + barH, burned ? 0xFFD42B3F : accent);

			// Burnout indicator
			if (burned) {
				graphics.text(this.font, Component.literal("§4● BURNOUT ACTIVE").getVisualOrderText(), colLeft,
						colTop + 40, 0xFFFF5555, true);
			} else {
				graphics.text(this.font, Component.literal("§a● CHANNELS OPTIMAL").getVisualOrderText(), colLeft,
						colTop + 40, 0xFF55FF55, true);
			}

			// Active selected technique
			int selectedIndex = (int) vars.selectedTechnique;
			List<INatureTechnique> techniques = NatureAbilityDispatcher.getTechniques(nature);
			if (techniques.isEmpty())
				techniques = NatureAbilityDispatcher.getTechniques(NatureType.NONE);

			String activeTechName = (selectedIndex >= 0 && selectedIndex < techniques.size())
					? techniques.get(selectedIndex).getName()
					: "None";
			graphics.text(this.font, Component.literal("§7Selected: §e" + activeTechName).getVisualOrderText(), colLeft,
					colTop + 56, 0xFFFFFFCC, true);
			graphics.text(this.font, Component.literal("§8Press §6[R] §8to cycle in-game").getVisualOrderText(),
					colLeft, colTop + 68, 0xFF888899, false);
			graphics.text(this.font, Component.literal("§8Press §6[G] §8to cast ability").getVisualOrderText(), colLeft,
					colTop + 78, 0xFF888899, false);
		}

		// Vertical divider
		graphics.fill(left + 146, top + 42, left + 147, top + PANEL_H - 34, 0xFF2C2150);

		// Right Column: Nature Techniques List
		int rightColLeft = left + 154;
		int rightColTop = top + 44;
		graphics.text(this.font, Component.literal("§7[TECHNIQUES AVAILABLE]").getVisualOrderText(), rightColLeft,
				rightColTop, 0xFFB0B0C0, true);

		List<INatureTechnique> techniques = NatureAbilityDispatcher.getTechniques(nature);
		if (techniques.isEmpty()) {
			techniques = NatureAbilityDispatcher.getTechniques(NatureType.NONE);
		}

		if (techniques.isEmpty()) {
			graphics.text(this.font, Component.literal("§8No techniques documented yet.").getVisualOrderText(),
					rightColLeft, rightColTop + 16, 0xFF666677, false);
		} else {
			int yOffset = rightColTop + 16;
			for (int i = 0; i < techniques.size() && i < 3; i++) {
				INatureTechnique t = techniques.get(i);
				graphics.text(this.font,
						Component.literal("§f" + (i + 1) + ". §e" + t.getName() + " §7(§b" + t.getCost() + " Flux§7)")
								.getVisualOrderText(),
						rightColLeft, yOffset, 0xFFFFFFFF, true);
				graphics.text(this.font, Component.literal("   §7" + t.getDescription()).getVisualOrderText(),
						rightColLeft, yOffset + 10, 0xFFAAAAAA, false);
				yOffset += 24;
			}
		}

		// Bottom divider
		graphics.fill(left + 12, top + PANEL_H - 32, left + PANEL_W - 12, top + PANEL_H - 31, 0xFF2C2150);
		graphics.text(this.font, Component.literal("§8Align with Nature (Test):").getVisualOrderText(), left + 14,
				top + PANEL_H - 22, 0xFF777788, false);

		// Render buttons & widgets
		super.extractRenderState(graphics, mouseX, mouseY, partialTick);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}
}

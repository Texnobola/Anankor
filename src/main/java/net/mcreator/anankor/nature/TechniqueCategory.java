package net.mcreator.anankor.nature;

/**
 * The four dedicated technique categories in Anankor's ability system.
 * Each Nature provides one distinct technique in each category.
 */
public enum TechniqueCategory {
	MOBILITY(0, "Mobility", "Movement & Traversal", "C"),
	STRIKE(1, "Strike", "Offensive Attack", "G"),
	WARD(2, "Ward", "Defensive & Utility", "X"),
	EXTENSION(3, "Extension", "Master Art & Domain", "Z");

	private final int slotIndex;
	private final String displayName;
	private final String description;
	private final String defaultKey;

	TechniqueCategory(int slotIndex, String displayName, String description, String defaultKey) {
		this.slotIndex = slotIndex;
		this.displayName = displayName;
		this.description = description;
		this.defaultKey = defaultKey;
	}

	public int getSlotIndex() {
		return slotIndex;
	}

	public String getDisplayName() {
		return displayName;
	}

	public String getDescription() {
		return description;
	}

	public String getDefaultKey() {
		return defaultKey;
	}

	public static TechniqueCategory byIndex(int index) {
		for (TechniqueCategory cat : values()) {
			if (cat.slotIndex == index) {
				return cat;
			}
		}
		return STRIKE;
	}
}

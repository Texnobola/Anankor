package net.mcreator.anankor.nature;

import java.util.Locale;

/**
 * The eight fundamental Natures of Anankeia, plus NONE for unaligned / raw
 * humans.
 */
public enum NatureType {
	NONE("None", 0xFF8B7BB8),
	FIRE("Fire", 0xFFFF5533),
	WATER("Water", 0xFF3399FF),
	ICE("Ice", 0xFF88DDFF),
	EARTH("Earth", 0xFF88AA44),
	SPACE("Space", 0xFFAA44FF),
	ELECTRICITY("Electricity", 0xFFFFDD33),
	LIGHT("Light", 0xFFFFFFAA),
	DARK("Dark", 0xFF442266);

	private final String displayName;
	private final int color;

	NatureType(String displayName, int color) {
		this.displayName = displayName;
		this.color = color;
	}

	public String getDisplayName() {
		return displayName;
	}

	public int getColor() {
		return color;
	}

	/**
	 * Safely parses a string into a NatureType.
	 * Handles null, whitespace, casing, and MCreator's quoted string defaults (e.g.
	 * "\"\"").
	 */
	public static NatureType fromString(String raw) {
		if (raw == null) {
			return NONE;
		}
		String cleaned = raw.replace("\"", "").trim().toUpperCase(Locale.ROOT);
		if (cleaned.isEmpty()) {
			return NONE;
		}
		for (NatureType type : values()) {
			if (type.name().equals(cleaned) || type.displayName.toUpperCase(Locale.ROOT).equals(cleaned)) {
				return type;
			}
		}
		return NONE;
	}

	public boolean isAligned() {
		return this != NONE;
	}
}

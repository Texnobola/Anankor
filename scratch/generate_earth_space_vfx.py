import json
import math
import os
from PIL import Image, ImageDraw, ImageFilter

GEO_DIR = "src/main/resources/assets/anankor/geckolib/models/entity"
ANIM_DIR = "src/main/resources/assets/anankor/geckolib/animations/entity"
TEX_DIR = "src/main/resources/assets/anankor/textures/entity/vfx"

os.makedirs(GEO_DIR, exist_ok=True)
os.makedirs(ANIM_DIR, exist_ok=True)
os.makedirs(TEX_DIR, exist_ok=True)

# ==============================================================================
# Helper functions
# ==============================================================================
def create_texture(width, height, draw_fn, filename):
    img = Image.new("RGBA", (width, height), (0, 0, 0, 0))
    draw_fn(img)
    filepath = os.path.join(TEX_DIR, filename)
    img.save(filepath, "PNG")
    print(f"Created texture: {filepath} ({width}x{height})")

def save_json(filepath, data):
    with open(filepath, "w", encoding="utf-8") as f:
        json.dump(data, f, indent=2)
    print(f"Saved: {filepath}")

# ==============================================================================
# 1. Earth Vault (Tectonic Vault - Mobility)
# ==============================================================================
def make_earth_vault():
    # Model
    geo = {
        "format_version": "1.12.0",
        "minecraft:geometry": [{
            "description": {
                "identifier": "geometry.vfx_earth_vault",
                "texture_width": 256,
                "texture_height": 256,
                "visible_bounds_width": 10,
                "visible_bounds_height": 10,
                "visible_bounds_offset": [0, 2, 0]
            },
            "bones": [
                {
                    "name": "root",
                    "pivot": [0, 0, 0]
                },
                {
                    "name": "ground_fracture",
                    "parent": "root",
                    "pivot": [0, 0, 0],
                    "cubes": [
                        {"origin": [-16, 0.05, -16], "size": [32, 0.2, 32], "uv": [0, 0]},
                        {"origin": [-12, 0.1, -12], "size": [24, 0.2, 24], "uv": [64, 0], "rotation": [0, 45, 0]}
                    ]
                },
                {
                    "name": "basalt_monolith",
                    "parent": "root",
                    "pivot": [0, 0, 0],
                    "cubes": [
                        {"origin": [-6, 0, -6], "size": [12, 36, 12], "uv": [0, 36]},
                        {"origin": [-8, 0, -5], "size": [3, 28, 10], "uv": [52, 36]},
                        {"origin": [5, 0, -5], "size": [3, 30, 10], "uv": [52, 36]},
                        {"origin": [-5, 0, -8], "size": [10, 26, 3], "uv": [80, 36]},
                        {"origin": [-5, 0, 5], "size": [10, 29, 3], "uv": [80, 36]}
                    ]
                },
                {
                    "name": "debris_chunk_1",
                    "parent": "root",
                    "pivot": [8, 10, 8],
                    "cubes": [{"origin": [6, 8, 6], "size": [5, 5, 5], "uv": [110, 36]}]
                },
                {
                    "name": "debris_chunk_2",
                    "parent": "root",
                    "pivot": [-8, 12, 7],
                    "cubes": [{"origin": [-10, 10, 5], "size": [4, 4, 4], "uv": [110, 50]}]
                },
                {
                    "name": "debris_chunk_3",
                    "parent": "root",
                    "pivot": [7, 8, -8],
                    "cubes": [{"origin": [5, 6, -10], "size": [5, 4, 5], "uv": [110, 60]}]
                },
                {
                    "name": "debris_chunk_4",
                    "parent": "root",
                    "pivot": [-8, 14, -8],
                    "cubes": [{"origin": [-10, 12, -10], "size": [4, 5, 4], "uv": [110, 72]}]
                }
            ]
        }]
    }
    save_json(os.path.join(GEO_DIR, "vfx_earth_vault.geo.json"), geo)

    # Animation
    anim = {
        "format_version": "1.8.0",
        "animations": {
            "vault": {
                "animation_length": 0.8,
                "loop": False,
                "bones": {
                    "ground_fracture": {
                        "scale": {
                            "0.0": {"vector": [0.0, 1.0, 0.0]},
                            "0.08": {"vector": [1.2, 1.0, 1.2], "easing": "easeOutQuad"},
                            "0.55": {"vector": [1.0, 1.0, 1.0]},
                            "0.80": {"vector": [0.0, 1.0, 0.0], "easing": "easeInQuad"}
                        }
                    },
                    "basalt_monolith": {
                        "position": {
                            "0.0": {"vector": [0, -36, 0]},
                            "0.12": {"vector": [0, 4, 0], "easing": "easeOutBack"},
                            "0.25": {"vector": [0, 0, 0]},
                            "0.55": {"vector": [0, 0, 0]},
                            "0.80": {"vector": [0, -38, 0], "easing": "easeInQuad"}
                        },
                        "scale": {
                            "0.0": {"vector": [0.4, 0.1, 0.4]},
                            "0.12": {"vector": [1.1, 1.1, 1.1]},
                            "0.25": {"vector": [1.0, 1.0, 1.0]},
                            "0.55": {"vector": [1.0, 1.0, 1.0]},
                            "0.80": {"vector": [0.3, 0.1, 0.3], "easing": "easeInQuad"}
                        }
                    },
                    "debris_chunk_1": {
                        "position": {
                            "0.0": {"vector": [0, -10, 0]},
                            "0.15": {"vector": [12, 18, 12], "easing": "easeOutQuad"},
                            "0.80": {"vector": [18, -4, 18], "easing": "easeInQuad"}
                        },
                        "rotation": {
                            "0.0": {"vector": [0, 0, 0]},
                            "0.80": {"vector": [180, 240, 90]}
                        },
                        "scale": {
                            "0.0": {"vector": [0, 0, 0]},
                            "0.10": {"vector": [1.2, 1.2, 1.2]},
                            "0.70": {"vector": [1.0, 1.0, 1.0]},
                            "0.80": {"vector": [0, 0, 0]}
                        }
                    },
                    "debris_chunk_2": {
                        "position": {
                            "0.0": {"vector": [0, -12, 0]},
                            "0.15": {"vector": [-14, 20, 10], "easing": "easeOutQuad"},
                            "0.80": {"vector": [-20, -4, 14], "easing": "easeInQuad"}
                        },
                        "rotation": {
                            "0.0": {"vector": [0, 0, 0]},
                            "0.80": {"vector": [-160, -200, 120]}
                        },
                        "scale": {
                            "0.0": {"vector": [0, 0, 0]},
                            "0.10": {"vector": [1.2, 1.2, 1.2]},
                            "0.70": {"vector": [1.0, 1.0, 1.0]},
                            "0.80": {"vector": [0, 0, 0]}
                        }
                    },
                    "debris_chunk_3": {
                        "position": {
                            "0.0": {"vector": [0, -10, 0]},
                            "0.15": {"vector": [10, 22, -14], "easing": "easeOutQuad"},
                            "0.80": {"vector": [16, -4, -20], "easing": "easeInQuad"}
                        },
                        "rotation": {
                            "0.0": {"vector": [0, 0, 0]},
                            "0.80": {"vector": [200, 150, -90]}
                        },
                        "scale": {
                            "0.0": {"vector": [0, 0, 0]},
                            "0.10": {"vector": [1.2, 1.2, 1.2]},
                            "0.70": {"vector": [1.0, 1.0, 1.0]},
                            "0.80": {"vector": [0, 0, 0]}
                        }
                    },
                    "debris_chunk_4": {
                        "position": {
                            "0.0": {"vector": [0, -14, 0]},
                            "0.15": {"vector": [-12, 16, -12], "easing": "easeOutQuad"},
                            "0.80": {"vector": [-18, -4, -18], "easing": "easeInQuad"}
                        },
                        "rotation": {
                            "0.0": {"vector": [0, 0, 0]},
                            "0.80": {"vector": [-180, 120, -150]}
                        },
                        "scale": {
                            "0.0": {"vector": [0, 0, 0]},
                            "0.10": {"vector": [1.2, 1.2, 1.2]},
                            "0.70": {"vector": [1.0, 1.0, 1.0]},
                            "0.80": {"vector": [0, 0, 0]}
                        }
                    }
                }
            }
        }
    }
    save_json(os.path.join(ANIM_DIR, "vfx_earth_vault.animation.json"), anim)

    # Texture (256x256)
    def draw_tex(img):
        draw = ImageDraw.Draw(img)
        # Ground fracture UV [0, 0] to [64, 32]
        for y in range(0, 36):
            for x in range(0, 128):
                noise = int(30 * math.sin(x * 0.2) * math.cos(y * 0.2))
                r = min(255, max(0, 90 + noise))
                g = min(255, max(0, 75 + noise))
                b = min(255, max(0, 50 + noise // 2))
                draw.point((x, y), fill=(r, g, b, 230))
        # Draw glowing magma/earth runes on fracture
        for x in range(10, 118, 12):
            draw.line([(x, 5), (x + 6, 25)], fill=(220, 180, 60, 240), width=2)
            draw.line([(x + 6, 25), (x + 12, 10)], fill=(240, 200, 80, 255), width=1)
        
        # Basalt monolith texture [0, 36] to [160, 200]
        for y in range(36, 220):
            for x in range(0, 160):
                c = int(45 + 15 * math.sin(x * 0.15) + 12 * math.cos(y * 0.1))
                r = c + 15
                g = c + 10
                b = c
                # Glowing earth vein
                if abs((x * 2 + y) % 35 - 17) < 2:
                    r, g, b = 210, 165, 45
                draw.point((x, y), fill=(r, g, b, 255))
    create_texture(256, 256, draw_tex, "vfx_earth_vault.png")

# ==============================================================================
# 2. Earth Aegis (Granite Aegis - Ward)
# ==============================================================================
def make_earth_aegis():
    geo = {
        "format_version": "1.12.0",
        "minecraft:geometry": [{
            "description": {
                "identifier": "geometry.vfx_earth_aegis",
                "texture_width": 256,
                "texture_height": 256,
                "visible_bounds_width": 10,
                "visible_bounds_height": 8,
                "visible_bounds_offset": [0, 1, 0]
            },
            "bones": [
                {
                    "name": "root",
                    "pivot": [0, 0, 0]
                },
                {
                    "name": "ground_rune_seal",
                    "parent": "root",
                    "pivot": [0, 0, 0],
                    "cubes": [
                        {"origin": [-18, 0.05, -18], "size": [36, 0.2, 36], "uv": [0, 0]},
                        {"origin": [-14, 0.08, -14], "size": [28, 0.2, 28], "uv": [74, 0], "rotation": [0, 45, 0]}
                    ]
                },
                {
                    "name": "orbit_anchor",
                    "parent": "root",
                    "pivot": [0, 12, 0]
                },
                {
                    "name": "granite_shield_north",
                    "parent": "orbit_anchor",
                    "pivot": [0, 12, 19],
                    "cubes": [
                        {"origin": [-7, 2, 18], "size": [14, 20, 3], "uv": [0, 38]},
                        {"origin": [-5, 6, 21], "size": [10, 12, 2], "uv": [36, 38]}
                    ]
                },
                {
                    "name": "granite_shield_south",
                    "parent": "orbit_anchor",
                    "pivot": [0, 12, -19],
                    "cubes": [
                        {"origin": [-7, 2, -21], "size": [14, 20, 3], "uv": [0, 38]},
                        {"origin": [-5, 6, -23], "size": [10, 12, 2], "uv": [36, 38]}
                    ]
                },
                {
                    "name": "granite_shield_east",
                    "parent": "orbit_anchor",
                    "pivot": [19, 12, 0],
                    "cubes": [
                        {"origin": [18, 2, -7], "size": [3, 20, 14], "uv": [62, 38]},
                        {"origin": [21, 6, -5], "size": [2, 12, 10], "uv": [98, 38]}
                    ]
                },
                {
                    "name": "granite_shield_west",
                    "parent": "orbit_anchor",
                    "pivot": [-19, 12, 0],
                    "cubes": [
                        {"origin": [-21, 2, -7], "size": [3, 20, 14], "uv": [62, 38]},
                        {"origin": [-23, 6, -5], "size": [2, 12, 10], "uv": [98, 38]}
                    ]
                },
                {
                    "name": "crystal_ring",
                    "parent": "root",
                    "pivot": [0, 16, 0]
                },
                {
                    "name": "crystal_ne",
                    "parent": "crystal_ring",
                    "pivot": [14, 16, 14],
                    "cubes": [{"origin": [12, 12, 12], "size": [4, 8, 4], "uv": [130, 38], "rotation": [15, 45, 15]}]
                },
                {
                    "name": "crystal_nw",
                    "parent": "crystal_ring",
                    "pivot": [-14, 16, 14],
                    "cubes": [{"origin": [-16, 12, 12], "size": [4, 8, 4], "uv": [130, 38], "rotation": [15, -45, -15]}]
                },
                {
                    "name": "crystal_se",
                    "parent": "crystal_ring",
                    "pivot": [14, 16, -14],
                    "cubes": [{"origin": [12, 12, -16], "size": [4, 8, 4], "uv": [130, 38], "rotation": [-15, 45, 15]}]
                },
                {
                    "name": "crystal_sw",
                    "parent": "crystal_ring",
                    "pivot": [-14, 16, -14],
                    "cubes": [{"origin": [-16, 12, -16], "size": [4, 8, 4], "uv": [130, 38], "rotation": [-15, -45, -15]}]
                }
            ]
        }]
    }
    save_json(os.path.join(GEO_DIR, "vfx_earth_aegis.geo.json"), geo)

    anim = {
        "format_version": "1.8.0",
        "animations": {
            "orbit": {
                "animation_length": 3.5,
                "loop": True,
                "bones": {
                    "ground_rune_seal": {
                        "rotation": {"vector": [0, "query.anim_time * 25", 0]},
                        "scale": {"vector": ["1.0 + 0.03 * math.sin(query.anim_time * 160)", 1.0, "1.0 + 0.03 * math.sin(query.anim_time * 160)"]}
                    },
                    "orbit_anchor": {
                        "rotation": {"vector": [0, "query.anim_time * 105", 0]},
                        "position": {"vector": [0, "math.sin(query.anim_time * 150) * 1.5", 0]}
                    },
                    "crystal_ring": {
                        "rotation": {"vector": [0, "query.anim_time * -130", 0]},
                        "position": {"vector": [0, "math.cos(query.anim_time * 180) * 2.0", 0]}
                    }
                }
            }
        }
    }
    save_json(os.path.join(ANIM_DIR, "vfx_earth_aegis.animation.json"), anim)

    def draw_tex(img):
        draw = ImageDraw.Draw(img)
        # Ground seal UV [0, 0] to [140, 38]
        for y in range(0, 38):
            for x in range(0, 140):
                c = 60 + int(20 * math.sin(x * 0.1) * math.cos(y * 0.1))
                draw.point((x, y), fill=(c + 15, c + 20, c, 220))
        # Rune accents
        for x in range(8, 130, 16):
            draw.rectangle([x, 10, x + 8, 26], outline=(140, 220, 90, 255), width=2)
            draw.point((x + 4, 18), fill=(210, 255, 120, 255))
        # Granite shields [0, 38] to [130, 120]
        for y in range(38, 120):
            for x in range(0, 130):
                c = 75 + int(15 * math.sin(x * 0.2) + 12 * math.cos(y * 0.15))
                # Carved bevel border
                is_edge = (x % 30 < 2 or y % 30 < 2)
                if is_edge:
                    draw.point((x, y), fill=(130, 180, 80, 255))
                else:
                    draw.point((x, y), fill=(c + 10, c + 15, c + 5, 255))
        # Crystals [130, 38] to [160, 80]
        for y in range(38, 80):
            for x in range(130, 160):
                g = 180 + int(40 * math.sin((x + y) * 0.3))
                draw.point((x, y), fill=(80, g, 110, 240))
    create_texture(256, 256, draw_tex, "vfx_earth_aegis.png")

# ==============================================================================
# 3. Earth Fissure (Cataclysmic Fissure - Extension)
# ==============================================================================
def make_earth_fissure():
    geo = {
        "format_version": "1.12.0",
        "minecraft:geometry": [{
            "description": {
                "identifier": "geometry.vfx_earth_fissure",
                "texture_width": 256,
                "texture_height": 256,
                "visible_bounds_width": 14,
                "visible_bounds_height": 8,
                "visible_bounds_offset": [0, 2, 4]
            },
            "bones": [
                {
                    "name": "root",
                    "pivot": [0, 0, 0]
                },
                {
                    "name": "fault_crack",
                    "parent": "root",
                    "pivot": [0, 0, 0],
                    "cubes": [
                        {"origin": [-8, 0.05, 10], "size": [16, 0.2, 100], "uv": [0, 0]}
                    ]
                },
                {
                    "name": "spike_tier_1",
                    "parent": "root",
                    "pivot": [0, 0, 20],
                    "cubes": [
                        {"origin": [-4, 0, 16], "size": [4, 12, 6], "uv": [36, 0], "rotation": [8, -12, 6]},
                        {"origin": [1, 0, 18], "size": [4, 10, 5], "uv": [36, 0], "rotation": [6, 15, -8]}
                    ]
                },
                {
                    "name": "spike_tier_2",
                    "parent": "root",
                    "pivot": [0, 0, 40],
                    "cubes": [
                        {"origin": [-6, 0, 36], "size": [5, 18, 7], "uv": [58, 0], "rotation": [10, -10, 8]},
                        {"origin": [2, 0, 38], "size": [5, 16, 6], "uv": [58, 0], "rotation": [8, 14, -10]}
                    ]
                },
                {
                    "name": "spike_tier_3",
                    "parent": "root",
                    "pivot": [0, 0, 62],
                    "cubes": [
                        {"origin": [-7, 0, 58], "size": [6, 24, 8], "uv": [84, 0], "rotation": [12, -8, 10]},
                        {"origin": [3, 0, 60], "size": [6, 22, 7], "uv": [84, 0], "rotation": [10, 12, -12]}
                    ]
                },
                {
                    "name": "spike_tier_4",
                    "parent": "root",
                    "pivot": [0, 0, 86],
                    "cubes": [
                        {"origin": [-9, 0, 80], "size": [7, 30, 10], "uv": [114, 0], "rotation": [14, -6, 12]},
                        {"origin": [4, 0, 84], "size": [7, 28, 9], "uv": [114, 0], "rotation": [12, 10, -14]}
                    ]
                },
                {
                    "name": "spike_tier_5",
                    "parent": "root",
                    "pivot": [0, 0, 110],
                    "cubes": [
                        {"origin": [-10, 0, 104], "size": [9, 36, 12], "uv": [150, 0], "rotation": [15, -5, 14]},
                        {"origin": [3, 0, 106], "size": [9, 34, 11], "uv": [150, 0], "rotation": [14, 8, -15]}
                    ]
                }
            ]
        }]
    }
    save_json(os.path.join(GEO_DIR, "vfx_earth_fissure.geo.json"), geo)

    anim = {
        "format_version": "1.8.0",
        "animations": {
            "fissure": {
                "animation_length": 1.4,
                "loop": False,
                "bones": {
                    "fault_crack": {
                        "scale": {
                            "0.0": {"vector": [0, 1, 0]},
                            "0.15": {"vector": [1.2, 1, 1.0], "easing": "easeOutQuad"},
                            "1.05": {"vector": [1.0, 1, 1.0]},
                            "1.40": {"vector": [0, 1, 0], "easing": "easeInQuad"}
                        }
                    },
                    "spike_tier_1": {
                        "position": {
                            "0.0": {"vector": [0, -14, 0]},
                            "0.08": {"vector": [0, 2, 0], "easing": "easeOutBack"},
                            "0.18": {"vector": [0, 0, 0]},
                            "1.05": {"vector": [0, 0, 0]},
                            "1.40": {"vector": [0, -15, 0], "easing": "easeInQuad"}
                        },
                        "scale": {
                            "0.0": {"vector": [0, 0, 0]},
                            "0.08": {"vector": [1.1, 1.1, 1.1]},
                            "1.05": {"vector": [1.0, 1.0, 1.0]},
                            "1.40": {"vector": [0, 0, 0]}
                        }
                    },
                    "spike_tier_2": {
                        "position": {
                            "0.0": {"vector": [0, -20, 0]},
                            "0.16": {"vector": [0, 3, 0], "easing": "easeOutBack"},
                            "0.26": {"vector": [0, 0, 0]},
                            "1.05": {"vector": [0, 0, 0]},
                            "1.40": {"vector": [0, -22, 0], "easing": "easeInQuad"}
                        },
                        "scale": {
                            "0.0": {"vector": [0, 0, 0]},
                            "0.16": {"vector": [1.1, 1.1, 1.1]},
                            "1.05": {"vector": [1.0, 1.0, 1.0]},
                            "1.40": {"vector": [0, 0, 0]}
                        }
                    },
                    "spike_tier_3": {
                        "position": {
                            "0.0": {"vector": [0, -26, 0]},
                            "0.24": {"vector": [0, 4, 0], "easing": "easeOutBack"},
                            "0.34": {"vector": [0, 0, 0]},
                            "1.05": {"vector": [0, 0, 0]},
                            "1.40": {"vector": [0, -28, 0], "easing": "easeInQuad"}
                        },
                        "scale": {
                            "0.0": {"vector": [0, 0, 0]},
                            "0.24": {"vector": [1.1, 1.1, 1.1]},
                            "1.05": {"vector": [1.0, 1.0, 1.0]},
                            "1.40": {"vector": [0, 0, 0]}
                        }
                    },
                    "spike_tier_4": {
                        "position": {
                            "0.0": {"vector": [0, -32, 0]},
                            "0.32": {"vector": [0, 5, 0], "easing": "easeOutBack"},
                            "0.42": {"vector": [0, 0, 0]},
                            "1.05": {"vector": [0, 0, 0]},
                            "1.40": {"vector": [0, -34, 0], "easing": "easeInQuad"}
                        },
                        "scale": {
                            "0.0": {"vector": [0, 0, 0]},
                            "0.32": {"vector": [1.15, 1.15, 1.15]},
                            "1.05": {"vector": [1.0, 1.0, 1.0]},
                            "1.40": {"vector": [0, 0, 0]}
                        }
                    },
                    "spike_tier_5": {
                        "position": {
                            "0.0": {"vector": [0, -38, 0]},
                            "0.40": {"vector": [0, 6, 0], "easing": "easeOutBack"},
                            "0.50": {"vector": [0, 0, 0]},
                            "1.05": {"vector": [0, 0, 0]},
                            "1.40": {"vector": [0, -40, 0], "easing": "easeInQuad"}
                        },
                        "scale": {
                            "0.0": {"vector": [0, 0, 0]},
                            "0.40": {"vector": [1.2, 1.25, 1.2]},
                            "1.05": {"vector": [1.0, 1.0, 1.0]},
                            "1.40": {"vector": [0, 0, 0]}
                        }
                    }
                }
            }
        }
    }
    save_json(os.path.join(ANIM_DIR, "vfx_earth_fissure.animation.json"), anim)

    def draw_tex(img):
        draw = ImageDraw.Draw(img)
        # Ruptured fissure trench texture [0, 0] to [256, 256]
        for y in range(0, 256):
            for x in range(0, 256):
                rock = 50 + int(20 * math.sin(x * 0.15) * math.cos(y * 0.15))
                # Glowing seismic magma core
                is_core = abs(x % 64 - 32) < 4 and y < 180
                if is_core:
                    draw.point((x, y), fill=(240, 170, 40, 255))
                else:
                    draw.point((x, y), fill=(rock + 20, rock + 15, rock + 8, 255))
    create_texture(256, 256, draw_tex, "vfx_earth_fissure.png")

# ==============================================================================
# 4. Space Collapse (Graviton Collapse - Strike)
# ==============================================================================
def make_space_collapse():
    geo = {
        "format_version": "1.12.0",
        "minecraft:geometry": [{
            "description": {
                "identifier": "geometry.vfx_space_collapse",
                "texture_width": 256,
                "texture_height": 256,
                "visible_bounds_width": 10,
                "visible_bounds_height": 10,
                "visible_bounds_offset": [0, 1, 0]
            },
            "bones": [
                {
                    "name": "root",
                    "pivot": [0, 0, 0]
                },
                {
                    "name": "graviton_core",
                    "parent": "root",
                    "pivot": [0, 8, 0],
                    "cubes": [
                        {"origin": [-5, 3, -5], "size": [10, 10, 10], "uv": [0, 0]}
                    ]
                },
                {
                    "name": "inner_accretion_ring",
                    "parent": "root",
                    "pivot": [0, 8, 0],
                    "cubes": [
                        {"origin": [-12, 7.5, -12], "size": [24, 1, 24], "uv": [40, 0], "rotation": [15, 0, 0]}
                    ]
                },
                {
                    "name": "outer_accretion_ring",
                    "parent": "root",
                    "pivot": [0, 8, 0],
                    "cubes": [
                        {"origin": [-18, 7.8, -18], "size": [36, 0.4, 36], "uv": [90, 0], "rotation": [-25, 45, 10]}
                    ]
                },
                {
                    "name": "warp_prism_1",
                    "parent": "root",
                    "pivot": [0, 8, 0],
                    "cubes": [
                        {"origin": [-2, 0, -14], "size": [4, 16, 28], "uv": [0, 40], "rotation": [0, 45, 0]}
                    ]
                },
                {
                    "name": "warp_prism_2",
                    "parent": "root",
                    "pivot": [0, 8, 0],
                    "cubes": [
                        {"origin": [-14, 0, -2], "size": [28, 16, 4], "uv": [0, 40], "rotation": [0, -45, 0]}
                    ]
                },
                {
                    "name": "vacuum_shock_halo",
                    "parent": "root",
                    "pivot": [0, 8, 0],
                    "cubes": [
                        {"origin": [-24, 7.9, -24], "size": [48, 0.2, 48], "uv": [0, 88]}
                    ]
                }
            ]
        }]
    }
    save_json(os.path.join(GEO_DIR, "vfx_space_collapse.geo.json"), geo)

    anim = {
        "format_version": "1.8.0",
        "animations": {
            "collapse": {
                "animation_length": 1.0,
                "loop": False,
                "bones": {
                    "graviton_core": {
                        "scale": {
                            "0.0": {"vector": [0.1, 0.1, 0.1]},
                            "0.25": {"vector": [1.4, 1.4, 1.4], "easing": "easeOutBack"},
                            "0.50": {"vector": [1.1, 1.1, 1.1]},
                            "0.75": {"vector": [1.6, 1.6, 1.6]},
                            "0.88": {"vector": [0.05, 0.05, 0.05], "easing": "easeInBack"},
                            "1.00": {"vector": [0, 0, 0]}
                        },
                        "rotation": {
                            "0.0": {"vector": [0, 0, 0]},
                            "1.0": {"vector": [360, 720, 180]}
                        }
                    },
                    "inner_accretion_ring": {
                        "rotation": {
                            "0.0": {"vector": [15, 0, 0]},
                            "0.5": {"vector": [25, 360, 15]},
                            "1.0": {"vector": [35, 1080, 25]}
                        },
                        "scale": {
                            "0.0": {"vector": [0.2, 0.2, 0.2]},
                            "0.5": {"vector": [1.3, 1.3, 1.3]},
                            "0.75": {"vector": [1.5, 1.5, 1.5]},
                            "0.88": {"vector": [0.05, 0.05, 0.05], "easing": "easeInBack"},
                            "1.0": {"vector": [0, 0, 0]}
                        }
                    },
                    "outer_accretion_ring": {
                        "rotation": {
                            "0.0": {"vector": [-25, 45, 10]},
                            "0.5": {"vector": [-30, -360, 15]},
                            "1.0": {"vector": [-40, -1080, 20]}
                        },
                        "scale": {
                            "0.0": {"vector": [0.1, 0.1, 0.1]},
                            "0.5": {"vector": [1.2, 1.2, 1.2]},
                            "0.75": {"vector": [1.6, 1.6, 1.6]},
                            "0.88": {"vector": [0.02, 0.02, 0.02], "easing": "easeInBack"},
                            "1.0": {"vector": [0, 0, 0]}
                        }
                    },
                    "warp_prism_1": {
                        "rotation": {
                            "0.0": {"vector": [0, 45, 0]},
                            "1.0": {"vector": [180, 540, 90]}
                        },
                        "scale": {
                            "0.0": {"vector": [0.2, 0.2, 0.2]},
                            "0.5": {"vector": [1.2, 1.2, 1.2]},
                            "0.85": {"vector": [0.05, 0.05, 0.05]},
                            "1.0": {"vector": [0, 0, 0]}
                        }
                    },
                    "warp_prism_2": {
                        "rotation": {
                            "0.0": {"vector": [0, -45, 0]},
                            "1.0": {"vector": [-180, -540, -90]}
                        },
                        "scale": {
                            "0.0": {"vector": [0.2, 0.2, 0.2]},
                            "0.5": {"vector": [1.2, 1.2, 1.2]},
                            "0.85": {"vector": [0.05, 0.05, 0.05]},
                            "1.0": {"vector": [0, 0, 0]}
                        }
                    },
                    "vacuum_shock_halo": {
                        "scale": {
                            "0.0": {"vector": [0, 1, 0]},
                            "0.85": {"vector": [0.2, 1, 0.2]},
                            "0.92": {"vector": [2.2, 0.5, 2.2], "easing": "easeOutQuad"},
                            "1.00": {"vector": [0, 0, 0], "easing": "easeInQuad"}
                        }
                    }
                }
            }
        }
    }
    save_json(os.path.join(ANIM_DIR, "vfx_space_collapse.animation.json"), anim)

    def draw_tex(img):
        draw = ImageDraw.Draw(img)
        # Deep cosmic void colors
        for y in range(0, 256):
            for x in range(0, 256):
                dist = math.sqrt((x - 128)**2 + (y - 128)**2)
                r = int(140 + 80 * math.sin(dist * 0.15))
                b = int(220 + 35 * math.cos(dist * 0.1))
                g = int(60 + 40 * math.sin((x + y) * 0.1))
                draw.point((x, y), fill=(r, g, b, 240))
        # Draw neon cosmic runes / grid lines
        for x in range(0, 256, 16):
            draw.line([(x, 0), (x, 256)], fill=(200, 100, 255, 120), width=1)
        for y in range(0, 256, 16):
            draw.line([(0, y), (256, y)], fill=(100, 220, 255, 120), width=1)
    create_texture(256, 256, draw_tex, "vfx_space_collapse.png")

# ==============================================================================
# 5. Space Distortion (Spatial Distortion - Ward)
# ==============================================================================
def make_space_distortion():
    geo = {
        "format_version": "1.12.0",
        "minecraft:geometry": [{
            "description": {
                "identifier": "geometry.vfx_space_distortion",
                "texture_width": 256,
                "texture_height": 256,
                "visible_bounds_width": 10,
                "visible_bounds_height": 8,
                "visible_bounds_offset": [0, 1, 0]
            },
            "bones": [
                {
                    "name": "root",
                    "pivot": [0, 0, 0]
                },
                {
                    "name": "equatorial_warp_ring",
                    "parent": "root",
                    "pivot": [0, 12, 0],
                    "cubes": [
                        {"origin": [-18, 11.5, -18], "size": [36, 1, 36], "uv": [0, 0]}
                    ]
                },
                {
                    "name": "polar_warp_ring",
                    "parent": "root",
                    "pivot": [0, 12, 0],
                    "cubes": [
                        {"origin": [-16, 11.5, -16], "size": [32, 1, 32], "uv": [74, 0], "rotation": [90, 0, 0]}
                    ]
                },
                {
                    "name": "diagonal_warp_ring",
                    "parent": "root",
                    "pivot": [0, 12, 0],
                    "cubes": [
                        {"origin": [-14, 11.5, -14], "size": [28, 1, 28], "uv": [0, 38], "rotation": [45, 0, 45]}
                    ]
                },
                {
                    "name": "astral_facets",
                    "parent": "root",
                    "pivot": [0, 12, 0],
                    "cubes": [
                        {"origin": [-18, 10, -2], "size": [4, 4, 4], "uv": [60, 38], "rotation": [25, 45, 10]},
                        {"origin": [14, 10, -2], "size": [4, 4, 4], "uv": [60, 38], "rotation": [-25, 45, -10]},
                        {"origin": [-2, 10, -18], "size": [4, 4, 4], "uv": [60, 38], "rotation": [10, -45, 25]},
                        {"origin": [-2, 10, 14], "size": [4, 4, 4], "uv": [60, 38], "rotation": [-10, -45, -25]},
                        {"origin": [-2, 24, -2], "size": [4, 4, 4], "uv": [60, 38], "rotation": [45, 10, 30]},
                        {"origin": [-2, -2, -2], "size": [4, 4, 4], "uv": [60, 38], "rotation": [-45, -10, -30]}
                    ]
                }
            ]
        }]
    }
    save_json(os.path.join(GEO_DIR, "vfx_space_distortion.geo.json"), geo)

    anim = {
        "format_version": "1.8.0",
        "animations": {
            "orbit": {
                "animation_length": 4.0,
                "loop": True,
                "bones": {
                    "equatorial_warp_ring": {
                        "rotation": {"vector": [0, "query.anim_time * 90", 0]},
                        "scale": {"vector": ["1.0 + 0.04 * math.sin(query.anim_time * 180)", 1.0, "1.0 + 0.04 * math.sin(query.anim_time * 180)"]}
                    },
                    "polar_warp_ring": {
                        "rotation": {"vector": [90, "query.anim_time * -110", 0]},
                        "scale": {"vector": ["1.0 + 0.04 * math.cos(query.anim_time * 180)", 1.0, "1.0 + 0.04 * math.cos(query.anim_time * 180)"]}
                    },
                    "diagonal_warp_ring": {
                        "rotation": {"vector": [45, "query.anim_time * 130", 45]}
                    },
                    "astral_facets": {
                        "rotation": {"vector": ["query.anim_time * 45", "query.anim_time * -70", "query.anim_time * 30"]},
                        "scale": {"vector": ["1.0 + 0.08 * math.sin(query.anim_time * 150)", "1.0 + 0.08 * math.sin(query.anim_time * 150)", "1.0 + 0.08 * math.sin(query.anim_time * 150)"]}
                    }
                }
            }
        }
    }
    save_json(os.path.join(ANIM_DIR, "vfx_space_distortion.animation.json"), anim)

    def draw_tex(img):
        draw = ImageDraw.Draw(img)
        # Gyroscopic dimensional rings UVs
        for y in range(0, 256):
            for x in range(0, 256):
                wave = int(120 + 80 * math.sin(x * 0.1) * math.cos(y * 0.1))
                r = int(150 + 70 * math.sin(x * 0.15))
                g = int(80 + 50 * math.cos(y * 0.12))
                b = min(255, wave + 60)
                draw.point((x, y), fill=(r, g, b, 230))
        # Add glowing constellation nodes
        for x in range(12, 240, 24):
            for y in range(12, 100, 20):
                draw.ellipse([x - 3, y - 3, x + 3, y + 3], fill=(220, 240, 255, 255))
    create_texture(256, 256, draw_tex, "vfx_space_distortion.png")

# ==============================================================================
# 6. Space Singularity (Event Horizon - Extension)
# ==============================================================================
def make_space_singularity():
    geo = {
        "format_version": "1.12.0",
        "minecraft:geometry": [{
            "description": {
                "identifier": "geometry.vfx_space_singularity",
                "texture_width": 256,
                "texture_height": 256,
                "visible_bounds_width": 16,
                "visible_bounds_height": 12,
                "visible_bounds_offset": [0, 2, 0]
            },
            "bones": [
                {
                    "name": "root",
                    "pivot": [0, 0, 0]
                },
                {
                    "name": "singularity_center",
                    "parent": "root",
                    "pivot": [0, 16, 0]
                },
                {
                    "name": "black_hole_core",
                    "parent": "singularity_center",
                    "pivot": [0, 16, 0],
                    "cubes": [
                        {"origin": [-9, 7, -9], "size": [18, 18, 18], "uv": [0, 0]}
                    ]
                },
                {
                    "name": "photon_ring",
                    "parent": "singularity_center",
                    "pivot": [0, 16, 0],
                    "cubes": [
                        {"origin": [-12, 15.5, -12], "size": [24, 1, 24], "uv": [56, 0]}
                    ]
                },
                {
                    "name": "accretion_disc_inner",
                    "parent": "singularity_center",
                    "pivot": [0, 16, 0],
                    "cubes": [
                        {"origin": [-22, 15.7, -22], "size": [44, 0.6, 44], "uv": [106, 0], "rotation": [12, 0, 0]}
                    ]
                },
                {
                    "name": "accretion_disc_outer",
                    "parent": "singularity_center",
                    "pivot": [0, 16, 0],
                    "cubes": [
                        {"origin": [-34, 15.8, -34], "size": [68, 0.4, 68], "uv": [0, 68], "rotation": [12, 30, 0]}
                    ]
                },
                {
                    "name": "gravitational_lensing_ring",
                    "parent": "singularity_center",
                    "pivot": [0, 16, 0],
                    "cubes": [
                        {"origin": [-28, 15.5, -28], "size": [56, 1, 56], "uv": [0, 138], "rotation": [-35, 60, 20]}
                    ]
                },
                {
                    "name": "polar_jet_north",
                    "parent": "singularity_center",
                    "pivot": [0, 25, 0],
                    "cubes": [
                        {"origin": [-3, 25, -3], "size": [6, 32, 6], "uv": [114, 68]}
                    ]
                },
                {
                    "name": "polar_jet_south",
                    "parent": "singularity_center",
                    "pivot": [0, 7, 0],
                    "cubes": [
                        {"origin": [-3, -25, -3], "size": [6, 32, 6], "uv": [114, 68]}
                    ]
                }
            ]
        }]
    }
    save_json(os.path.join(GEO_DIR, "vfx_space_singularity.geo.json"), geo)

    anim = {
        "format_version": "1.8.0",
        "animations": {
            "singularity": {
                "animation_length": 3.0,
                "loop": True,
                "bones": {
                    "black_hole_core": {
                        "scale": {"vector": ["1.0 + 0.04 * math.sin(query.anim_time * 180)", "1.0 + 0.04 * math.sin(query.anim_time * 180)", "1.0 + 0.04 * math.sin(query.anim_time * 180)"]},
                        "rotation": {"vector": ["query.anim_time * 40", "query.anim_time * 80", 0]}
                    },
                    "photon_ring": {
                        "rotation": {"vector": [0, "query.anim_time * -200", 0]},
                        "scale": {"vector": ["1.0 + 0.06 * math.sin(query.anim_time * 240)", 1.0, "1.0 + 0.06 * math.sin(query.anim_time * 240)"]}
                    },
                    "accretion_disc_inner": {
                        "rotation": {"vector": [12, "query.anim_time * 240", 0]}
                    },
                    "accretion_disc_outer": {
                        "rotation": {"vector": [12, "query.anim_time * 150", 0]}
                    },
                    "gravitational_lensing_ring": {
                        "rotation": {"vector": [-35, "query.anim_time * -100", 20]},
                        "scale": {"vector": ["1.0 + 0.05 * math.sin(query.anim_time * 160)", 1.0, "1.0 + 0.05 * math.sin(query.anim_time * 160)"]}
                    },
                    "polar_jet_north": {
                        "scale": {"vector": ["1.0 + 0.1 * math.sin(query.anim_time * 260)", "1.0 + 0.2 * math.sin(query.anim_time * 300)", "1.0 + 0.1 * math.sin(query.anim_time * 260)"]}
                    },
                    "polar_jet_south": {
                        "scale": {"vector": ["1.0 + 0.1 * math.sin(query.anim_time * 260)", "1.0 + 0.2 * math.sin(query.anim_time * 300)", "1.0 + 0.1 * math.sin(query.anim_time * 260)"]}
                    }
                }
            }
        }
    }
    save_json(os.path.join(ANIM_DIR, "vfx_space_singularity.animation.json"), anim)

    def draw_tex(img):
        draw = ImageDraw.Draw(img)
        # Black hole core: pitch black with cosmic event horizon edge
        for y in range(0, 36):
            for x in range(0, 56):
                draw.point((x, y), fill=(10, 5, 20, 255))
        # Photon ring: intense white-violet glow
        for y in range(0, 36):
            for x in range(56, 106):
                draw.point((x, y), fill=(245, 220, 255, 255))
        # Accretion discs: cosmic gradient from fiery magenta to cyan
        for y in range(0, 256):
            for x in range(106, 256):
                r = int(180 + 70 * math.sin(x * 0.15))
                g = int(90 + 70 * math.sin(y * 0.12))
                b = int(240 + 15 * math.cos((x + y) * 0.1))
                draw.point((x, y), fill=(r, g, b, 245))
        # Polar jets: pure beam glow
        for y in range(68, 140):
            for x in range(114, 150):
                draw.point((x, y), fill=(180, 230, 255, 240))
    create_texture(256, 256, draw_tex, "vfx_space_singularity.png")

if __name__ == "__main__":
    print("Generating Earth VFX...")
    make_earth_vault()
    make_earth_aegis()
    make_earth_fissure()
    print("Generating Space VFX...")
    make_space_collapse()
    make_space_distortion()
    make_space_singularity()
    print("Done!")

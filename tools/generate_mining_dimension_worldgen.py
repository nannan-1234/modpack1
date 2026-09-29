#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Generate the mining dimension datapack files under kubejs/data/miningdim.

Why a generator:
- The dimension is a custom flat world with 5 rock strata.
- Ore features are data-driven JSON. Keeping the feature/count manifest here
  makes it easy to rebalance later without editing dozens of placed_feature
  files by hand.

Run from anywhere:
    python tools/generate_mining_dimension_worldgen.py

The script only writes files under kubejs/data/miningdim and never touches
existing Minecraft/mod data.
"""

from __future__ import annotations

import json
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
DATA_DIR = ROOT / "data" / "miningdim"
NS = "miningdim"

# Ores are placed in the whole combined stone+deepslate band (Y 100-197).
# Existing configured features already map stone -> ore and deepslate ->
# deepslate_ore, so the same feature is valid in both strata.
OVERWORLD_ORES = [
    # (placed slug suffix, referenced configured feature, count per chunk)
    ("coal", "minecraft:ore_coal", 12),
    ("iron", "minecraft:ore_iron", 10),
    ("copper", "minecraft:ore_copper_small", 8),
    ("gold", "minecraft:ore_gold", 4),
    ("redstone", "minecraft:ore_redstone", 5),
    ("lapis", "minecraft:ore_lapis", 4),
    ("diamond", "minecraft:ore_diamond_small", 3),
    ("emerald", "minecraft:ore_emerald", 2),
    ("zinc", "create:zinc_ore", 6),
    ("osmium_upper", "mekanism:ore_osmium_upper", 4),
    ("osmium_small", "mekanism:ore_osmium_small", 2),
    ("tin_small", "mekanism:ore_tin_small", 5),
    ("lead", "mekanism:ore_lead_normal", 3),
    ("uranium", "mekanism:ore_uranium_small", 2),
    ("fluorite", "mekanism:ore_fluorite_normal", 4),
    ("ie_aluminum", "immersiveengineering:bauxite", 5),
    ("ie_lead", "immersiveengineering:lead", 3),
    ("ie_silver", "immersiveengineering:silver", 3),
    ("ie_nickel", "immersiveengineering:nickel", 3),
    ("ie_uranium", "immersiveengineering:uranium", 3),
    ("thermal_apatite", "thermal:apatite_ore", 4),
    ("thermal_cinnabar", "thermal:cinnabar_ore", 3),
    ("thermal_lead", "thermal:lead_ore", 3),
    ("thermal_nickel", "thermal:nickel_ore", 3),
    ("thermal_niter", "thermal:niter_ore", 4),
    ("thermal_silver", "thermal:silver_ore", 3),
    ("thermal_sulfur", "thermal:sulfur_ore", 4),
    ("thermal_tin", "thermal:tin_ore", 4),
    ("uraninite", "powah:uraninite_ore", 2),
    ("uraninite_poor", "powah:uraninite_ore_poor", 2),
    ("uraninite_dense", "powah:uraninite_ore_dense", 1),
    ("gobber_ore", "gobber2:gobber2_ore_configed", 2),
    ("draconium", "draconicevolution:overworld_draconium_ore", 1),
    ("iceandfire_silver", "iceandfire:silver_ore", 3),
    ("iceandfire_sapphire", "iceandfire:sapphire_ore", 2),
    ("bismuthinite", "tinkers_advanced:bismuthinite_ore", 3),
    ("black_zircon", "origincore:black_zircon_ore", 3),
    ("proud_soul_useful", "slashblade_useful_addon:proud_soul_ore", 3),
    ("deep_proud_soul_useful", "slashblade_useful_addon:deep_proud_soul_ore", 2),
    ("proud_soul_legend", "legendblade:proud_soul_ore", 3),
    ("deep_proud_soul_legend", "legendblade:deep_proud_soul_ore", 2),
]

NETHER_ORES = [
    ("nether_quartz", "minecraft:ore_quartz", 12),
    ("nether_gold", "minecraft:ore_nether_gold", 8),
    ("ancient_debris_small", "minecraft:ore_ancient_debris_small", 3),
    ("ancient_debris_large", "minecraft:ore_ancient_debris_large", 2),
    ("gobber_nether", "gobber2:gobber2_ore_nether_configed", 4),
    ("draconium_nether", "draconicevolution:nether_draconium_ore", 1),
    ("black_zircon_nether", "origincore:nether_black_zircon_ore", 4),
    ("cobalt_small", "tconstruct:cobalt_ore_small", 3),
    ("cobalt_large", "tconstruct:cobalt_ore_large", 2),
    ("stibnite", "tinkers_advanced:stibnite_ore", 4),
]

END_ORES = [
    ("draconium_end", "draconicevolution:end_draconium_ore", 2),
    ("gobber_end", "gobber2:gobber2_ore_end_configed", 4),
    ("black_zircon_end", "origincore:end_black_zircon_ore", 4),
    ("iridium_lean", "tinkers_advanced:iridium_lean_ore", 3),
]

# (slug, referenced feature, count, min_y, max_y)
ALL_ORES = [
    *[(*row, 100, 197) for row in OVERWORLD_ORES],
    *[(*row, 0, 99) for row in NETHER_ORES],
    *[(*row, -64, -1) for row in END_ORES],
]


def placed_feature_json(feature_id: str, count: int, min_y: int, max_y: int) -> dict:
    return {
        "feature": feature_id,
        "placement": [
            {"type": "minecraft:count", "count": count},
            {"type": "minecraft:in_square"},
            {
                "type": "minecraft:height_range",
                "height": {
                    "type": "minecraft:trapezoid",
                    "min_inclusive": {"absolute": min_y},
                    "max_inclusive": {"absolute": max_y},
                },
            },
            {"type": "minecraft:biome"},
        ],
    }


def biome_json(ore_placed_ids: list[str]) -> dict:
    # GenerationStep.Decoration has 11 slots. Underground ores are index 6.
    feature_rows: list[list[str]] = [[] for _ in range(11)]
    feature_rows[6] = ore_placed_ids
    return {
        "temperature": 0.8,
        "downfall": 0.0,
        "has_precipitation": False,
        "effects": {
            "sky_color": 10526657,
            "fog_color": 12638463,
            "water_color": 4159204,
            "water_fog_color": 329011,
        },
        "carvers": {},
        "features": feature_rows,
        "spawners": {
            "ambient": [],
            "axolotls": [],
            "creature": [],
            "misc": [],
            "monster": [],
            "underground_water_creature": [],
            "water_ambient": [],
            "water_creature": [],
        },
        "spawn_costs": {},
    }


def dimension_type_json() -> dict:
    return {
        "ambient_light": 0.1,
        "bed_works": True,
        "coordinate_scale": 1.0,
        "effects": "minecraft:overworld",
        "fixed_time": 6000,
        "has_ceiling": False,
        "has_raids": False,
        "has_skylight": True,
        "height": 384,
        "infiniburn": "#minecraft:infiniburn_overworld",
        "logical_height": 384,
        "min_y": -64,
        "monster_spawn_block_light_limit": 0,
        "monster_spawn_light_level": {
            "type": "minecraft:uniform",
            "value": {"min_inclusive": 0, "max_inclusive": 7},
        },
        "natural": False,
        "piglin_safe": False,
        "respawn_anchor_works": False,
        "ultrawarm": False,
    }


def dimension_json() -> dict:
    return {
        "type": f"{NS}:mining",
        "generator": {
            "type": "minecraft:flat",
            "settings": {
                "biome": f"{NS}:mining",
                "lakes": False,
                "features": True,
                "layers": [
                    {"block": "minecraft:end_stone", "height": 64},   # Y -64 .. -1
                    {"block": "minecraft:netherrack", "height": 100}, # Y 0 .. 99
                    {"block": "minecraft:deepslate", "height": 50},   # Y 100 .. 149
                    {"block": "minecraft:stone", "height": 48},       # Y 150 .. 197
                    {"block": "minecraft:dirt", "height": 3},         # Y 198 .. 200
                ],
                "structure_overrides": [],
            },
        },
    }


def write_json(path: Path, data: dict | list) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(
        json.dumps(data, ensure_ascii=False, indent=2) + "\n",
        encoding="utf-8",
    )


def main() -> None:
    written: list[Path] = []

    for slug, feature_id, count, min_y, max_y in ALL_ORES:
        path = DATA_DIR / "worldgen" / "placed_feature" / f"ore_{slug}.json"
        write_json(path, placed_feature_json(feature_id, count, min_y, max_y))
        written.append(path)

    ore_placed_ids = [f"{NS}:ore_{slug}" for slug, *_ in ALL_ORES]
    biome_path = DATA_DIR / "worldgen" / "biome" / "mining.json"
    write_json(biome_path, biome_json(ore_placed_ids))
    written.append(biome_path)

    type_path = DATA_DIR / "dimension_type" / "mining.json"
    write_json(type_path, dimension_type_json())
    written.append(type_path)

    dim_path = DATA_DIR / "dimension" / "mining.json"
    write_json(dim_path, dimension_json())
    written.append(dim_path)

    print(f"Generated {len(written)} files under {DATA_DIR}")


if __name__ == "__main__":
    main()

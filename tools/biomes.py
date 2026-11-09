#!/usr/bin/env python3
"""Generate the biome JSON files for the ported 1.5.2 biomes.

Biomes of 1.5.2 were Java objects; today they are data. This script turns the old properties (climate,
colours, mob spawn lists) into data/<namespace>/worldgen/biome/*.json, so the values stay in one table.
Grass and foliage colours are looked up in the original 1.5.2 colour maps, which tools/reference.py
downloads with the 1.5.2 client jar.

Usage:  python3 tools/biomes.py [--cache DIR]      (needs Pillow; run tools/reference.py once before)
"""
import argparse, io, json, os, struct, zipfile

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
DEFAULT_WATER = 0x3F76E4  # tint that reproduces the look of the old untinted water texture


def f32(value):
    return struct.unpack("f", struct.pack("f", value))[0]


def sky_color(temperature):
    """BiomeGenBase.getSkyColorByTemp, with java.awt.Color.HSBtoRGB in float precision."""
    t = f32(temperature / 3.0)
    t = max(-1.0, min(1.0, t))
    hue, sat, bri = f32(0.62222224 - f32(t * 0.05)), f32(0.5 + f32(t * 0.1)), 1.0
    if sat == 0:
        r = g = b = int(f32(bri * 255.0 + 0.5))
    else:
        h = f32((hue - int(hue // 1)) * 6.0)
        f = f32(h - int(h // 1))
        p = f32(bri * f32(1.0 - sat))
        q = f32(bri * f32(1.0 - f32(sat * f)))
        t2 = f32(bri * f32(1.0 - f32(sat * f32(1.0 - f))))
        r, g, b = [(bri, t2, p), (q, bri, p), (p, bri, t2), (p, q, bri), (t2, p, bri), (bri, p, q)][int(h)]
        r, g, b = (int(f32(c * 255.0 + 0.5)) for c in (r, g, b))
    return r << 16 | g << 8 | b


class ColorMaps:
    def __init__(self, client_jar):
        from PIL import Image
        with zipfile.ZipFile(client_jar) as z:
            self.grass = Image.open(io.BytesIO(z.read("misc/grasscolor.png"))).convert("RGB")
            self.foliage = Image.open(io.BytesIO(z.read("misc/foliagecolor.png"))).convert("RGB")

    @staticmethod
    def _lookup(image, temperature, rainfall):
        temperature = max(0.0, min(1.0, f32(temperature)))
        rainfall = max(0.0, min(1.0, f32(rainfall))) * temperature
        r, g, b = image.getpixel((int((1.0 - temperature) * 255.0), int((1.0 - rainfall) * 255.0)))
        return r << 16 | g << 8 | b

    def grass_color(self, temperature, rainfall):
        return self._lookup(self.grass, temperature, rainfall)

    def foliage_color(self, temperature, rainfall):
        return self._lookup(self.foliage, temperature, rainfall)


def hexcolor(rgb):
    return "#%06x" % rgb


def multiply(base, multiplier):
    return sum(((base >> s & 255) * (multiplier >> s & 255) // 255) << s for s in (16, 8, 0))


def spawn(entity, weight, low, high):
    count = low if low == high else {"type": "minecraft:uniform", "max_inclusive": high, "min_inclusive": low}
    return {"type": "minecraft:" + entity, "count": count, "weight": weight}


# BiomeGenBase defaults. 1.5.2 made 5% of all zombies villagers when they spawned; that is a separate
# spawn entry today, so monster weights are scaled by ten to keep the same ratios.
CREATURES = [spawn("sheep", 12, 4, 4), spawn("pig", 10, 4, 4), spawn("chicken", 10, 4, 4), spawn("cow", 8, 4, 4)]
MONSTERS = [spawn("spider", 100, 4, 4), spawn("zombie", 95, 4, 4), spawn("zombie_villager", 5, 1, 1),
            spawn("skeleton", 100, 4, 4), spawn("creeper", 100, 4, 4), spawn("slime", 100, 4, 4),
            spawn("enderman", 10, 1, 4)]
WATER = [spawn("squid", 10, 4, 4)]
AMBIENT = [spawn("bat", 10, 8, 8)]


def biome(temperature=0.5, rainfall=0.5, rain=True, creatures=CREATURES, monsters=MONSTERS, water=WATER,
          water_multiplier=0xFFFFFF, grass=None, foliage=None):
    return dict(temperature=temperature, rainfall=rainfall, rain=rain, creatures=creatures, monsters=monsters,
                water=water, water_multiplier=water_multiplier, grass=grass, foliage=foliage)


def swamp_tint(color, maps):
    return ((color & 0xFEFEFE) + 5115470) // 2


VANILLA = {
    "ocean": biome(creatures=[]),
    "plains": biome(0.8, 0.4),
    "desert": biome(2.0, 0.0, rain=False, creatures=[]),
    "extreme_hills": biome(0.2, 0.3),
    "forest": biome(0.7, 0.8, creatures=CREATURES + [spawn("wolf", 5, 4, 4)]),
    "taiga": biome(0.05, 0.8, creatures=CREATURES + [spawn("wolf", 8, 4, 4)]),
    "swampland": biome(0.8, 0.9, monsters=MONSTERS + [spawn("slime", 10, 1, 1)], water_multiplier=14745518,
                       grass=swamp_tint, foliage=swamp_tint),
    "river": biome(creatures=[]),
    "frozen_ocean": biome(0.0, 0.5, creatures=[]),
    "frozen_river": biome(0.0, 0.5, creatures=[]),
    "ice_plains": biome(0.0, 0.5),
    "ice_mountains": biome(0.0, 0.5),
    "mushroom_island": biome(0.9, 1.0, creatures=[spawn("mooshroom", 8, 4, 8)], monsters=[], water=[]),
    "mushroom_island_shore": biome(0.9, 1.0, creatures=[spawn("mooshroom", 8, 4, 8)], monsters=[], water=[]),
    "beach": biome(0.8, 0.4, creatures=[]),
    "desert_hills": biome(2.0, 0.0, rain=False, creatures=[]),
    "forest_hills": biome(0.7, 0.8, creatures=CREATURES + [spawn("wolf", 5, 4, 4)]),
    "taiga_hills": biome(0.05, 0.8, creatures=CREATURES + [spawn("wolf", 8, 4, 4)]),
    "extreme_hills_edge": biome(0.2, 0.3),
    "jungle": biome(1.2, 0.9, creatures=CREATURES + [spawn("chicken", 10, 4, 4)], monsters=MONSTERS + [spawn("ocelot", 20, 1, 1)]),
    "jungle_hills": biome(1.2, 0.9, creatures=CREATURES + [spawn("chicken", 10, 4, 4)], monsters=MONSTERS + [spawn("ocelot", 20, 1, 1)]),
}

WOLF_5 = CREATURES + [spawn("wolf", 5, 4, 4)]
WOLF_8 = CREATURES + [spawn("wolf", 8, 4, 4)]
JUNGLE_CREATURES = CREATURES + [spawn("chicken", 10, 4, 4)]
JUNGLE_MONSTERS = MONSTERS + [spawn("ocelot", 20, 1, 1)]
FOREST_T, FOREST_R = 0.7, 0.8


def fixed(temperature, rainfall, colormap):
    """ExtrabiomesXL biomes that looked their grass/foliage colour up at a fixed climate point."""
    return lambda color, maps: getattr(maps, colormap)(temperature, rainfall)


def tinted(temperature, rainfall):
    return dict(grass=fixed(temperature, rainfall, "grass_color"), foliage=fixed(temperature, rainfall, "foliage_color"))


# Temperatures like "forest - 0.1" are float arithmetic in the original; f32 keeps the same values.
EXTRABIOMES = {
    "alpine": biome(0.0, 0.1, creatures=WOLF_8),
    "autumn_woods": biome(FOREST_T, FOREST_R, creatures=WOLF_5, **tinted(1.0, 0.1)),
    "birch_forest": biome(0.4, 0.7, creatures=WOLF_5),
    "extreme_jungle": biome(1.2, 0.9, creatures=JUNGLE_CREATURES, monsters=JUNGLE_MONSTERS),
    "forested_hills": biome(f32(f32(FOREST_T) - f32(0.1)), FOREST_R, creatures=WOLF_5, **tinted(0.8, 1.0)),
    "forested_island": biome(f32(f32(FOREST_T) + f32(0.1)), FOREST_R, creatures=WOLF_5, **tinted(0.4, 0.7)),
    "glacier": biome(0.0, 0.0, creatures=[]),
    "green_hills": biome(f32(f32(FOREST_T) - f32(0.1)), f32(f32(FOREST_R) + f32(0.1)), **tinted(0.7, 0.8)),
    "green_swamp": biome(f32(f32(0.8) - f32(0.1)), 0.9, monsters=MONSTERS + [spawn("slime", 10, 1, 1)]),
    "ice_wasteland": biome(0.0, 0.1, creatures=[]),
    "marsh": biome(0.8, 0.9),
    "meadow": biome(**tinted(1.0, 1.0)),
    "mini_jungle": biome(1.2, 0.9, creatures=JUNGLE_CREATURES, monsters=JUNGLE_MONSTERS, water_multiplier=2404380),
    "mountain_desert": biome(2.0, 0.0, rain=False, creatures=[]),
    "mountain_ridge": biome(2.0, 0.0, rain=False, creatures=[]),
    "mountain_taiga": biome(0.0, 0.8, creatures=WOLF_8),
    "pine_forest": biome(FOREST_T, FOREST_R, creatures=WOLF_5, **tinted(0.4, 0.6)),
    "rainforest": biome(1.1, 1.4),
    "redwood_forest": biome(1.1, 1.4),
    "redwood_lush": biome(1.1, 1.4),
    "savanna": biome(2.0, 0.0),
    "shrubland": biome(0.4, 0.6),
    "snowy_forest": biome(0.05, 0.8, creatures=WOLF_5, **tinted(0.0, 0.2)),
    "snowy_rainforest": biome(0.05, 1.3, **tinted(0.0, 0.1)),
    "temperate_rainforest": biome(0.6, 0.9, creatures=WOLF_5),
    "tundra": biome(0.0, 0.0),
    "wasteland": biome(2.0, 0.0, rain=False, creatures=[], water_multiplier=15761408),
    "woodlands": biome(FOREST_T, FOREST_R, creatures=WOLF_5, **tinted(1.0, 0.2)),
}

MODS = {
    "worldgen152": ("mods/worldgen152/src/main/resources/data/worldgen152/worldgen/biome", VANILLA),
    "extrabiomesxl": ("mods/ebxl/src/main/resources/data/extrabiomesxl/worldgen/biome", EXTRABIOMES),
}


def to_json(spec, maps):
    t, r = spec["temperature"], spec["rainfall"]
    grass, foliage = maps.grass_color(t, r), maps.foliage_color(t, r)
    if spec["grass"]:
        grass = spec["grass"](grass, maps)
    if spec["foliage"]:
        foliage = spec["foliage"](foliage, maps)
    spawns = {"ambient": AMBIENT}
    for category, entries in (("creature", spec["creatures"]), ("monster", spec["monsters"]), ("water_creature", spec["water"])):
        if entries:
            spawns[category] = entries
    return {
        "attributes": {
            "minecraft:gameplay/natural_mob_spawns": {
                "argument": {"spawn_costs": {}, "spawns_by_category": spawns},
                "modifier": "overlay",
            },
            "minecraft:visual/sky_color": hexcolor(sky_color(t)),
        },
        "carvers": [],
        "downfall": r,
        "effects": {
            "foliage_color": hexcolor(foliage),
            "grass_color": hexcolor(grass),
            "water_color": hexcolor(multiply(DEFAULT_WATER, spec["water_multiplier"])),
        },
        "features": [],
        "has_precipitation": spec["rain"],
        "temperature": t,
    }


def main():
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("--cache", default=os.path.join(ROOT, ".cache"))
    args = ap.parse_args()
    jar = os.path.join(args.cache, "tools", "minecraft-1.5.2-client.jar")
    if not os.path.exists(jar):
        raise SystemExit(f"{jar} is missing: run tools/reference.py first")
    maps = ColorMaps(jar)
    for mod, (directory, table) in MODS.items():
        out = os.path.join(ROOT, directory)
        os.makedirs(out, exist_ok=True)
        for name, spec in table.items():
            with open(os.path.join(out, name + ".json"), "w") as f:
                json.dump(to_json(spec, maps), f, indent=2, sort_keys=True)
                f.write("\n")
        print(f"{mod}: {len(table)} biomes -> {directory}")


if __name__ == "__main__":
    main()

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


def swamp_tint(color):
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

MODS = {
    "worldgen152": ("mods/worldgen152/src/main/resources/data/worldgen152/worldgen/biome", VANILLA),
}


def to_json(spec, maps):
    t, r = spec["temperature"], spec["rainfall"]
    grass, foliage = maps.grass_color(t, r), maps.foliage_color(t, r)
    if spec["grass"]:
        grass = spec["grass"](grass)
    if spec["foliage"]:
        foliage = spec["foliage"](foliage)
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

#!/usr/bin/env python3
"""Writes the data and asset files of the Twilight Forest port (run from anywhere)."""
import json
import os

ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)), '..', 'src', 'main', 'resources')
NS = 'twilightforest'


def write(path, data):
    full = os.path.join(ROOT, path)
    os.makedirs(os.path.dirname(full), exist_ok=True)
    with open(full, 'w') as f:
        json.dump(data, f, indent=2)
        f.write('\n')


def rgb(r, g, b):
    return '#%02x%02x%02x' % (int(r * 255), int(g * 255), int(b * 255))


# WorldProviderTwilightForest: sky 0.168/0.180/0.387, fog from cos(1.5707965) * 2 + 0.5 = 0.5, celestial angle 0.225
# (81 degrees, skylightSubtracted 2), stars at full brightness, no sunrise colours.
bright = 0.5
FOG = rgb(0.7529412 * (bright * 0.94 + 0.06), 1.0 * (bright * 0.94 + 0.06), 0.8470588 * (bright * 0.91 + 0.09))
SKY = rgb(0.16796875, 0.1796875, 0.38671875)

write(f'data/{NS}/dimension_type/twilight_forest.json', {
    'coordinate_scale': 1.0,
    'has_ceiling': False,
    'has_ender_dragon_fight': False,
    'has_skylight': True,
    'has_fixed_time': True,
    'height': 256,
    'logical_height': 256,
    'min_y': 0,
    'infiniburn': '#minecraft:infiniburn_overworld',
    'monster_spawn_block_light_limit': 0,
    'monster_spawn_light_level': {'type': 'minecraft:uniform', 'min_inclusive': 0, 'max_inclusive': 7},
    'ambient_light': 0.0,
    'attributes': {
        'minecraft:gameplay/bed_rule': {'can_set_spawn': 'always', 'can_sleep': 'always'},
        'minecraft:gameplay/respawn_anchor_works': False,
        'minecraft:gameplay/sky_light_level': 13.0,
        'minecraft:visual/sky_color': SKY,
        'minecraft:visual/fog_color': FOG,
        'minecraft:visual/cloud_color': '#ccffffff',
        'minecraft:visual/cloud_height': 128.0,
        'minecraft:visual/sun_angle': 81.0,
        'minecraft:visual/moon_angle': 261.0,
        'minecraft:visual/star_angle': 81.0,
        'minecraft:visual/star_brightness': 1.0,
        'minecraft:visual/sky_light_factor': 0.82,
    },
})

write(f'data/{NS}/dimension/twilight_forest.json', {
    'type': f'{NS}:twilight_forest',
    'generator': {'type': f'{NS}:twilight_forest', 'biome_source': {'type': f'{NS}:twilight_forest'}},
})

# TFBiomeBase and its subclasses: temperature, rainfall, water colour (waterColorMultiplier)
BIOMES = {
    'twilight_lake': (0.66, 1.0, None), 'twilight_forest': (0.5, 0.5, None), 'dense_twilight_forest': (0.7, 0.8, None),
    'highlands': (0.5, 0.3, None), 'mushrooms': (0.5, 0.8, None), 'twilight_swamp': (0.8, 0.9, 14745518),
    'twilight_stream': (0.5, 1.0, None), 'snowy_forest': (0.125, 0.9, None), 'glacier': (0.0, 0.1, None),
    'twilight_clearing': (0.8, 0.4, None), 'clearing_border': (0.5, 0.5, None), 'lake_border': (0.5, 0.5, None),
    'deep_mushrooms': (0.8, 1.0, None), 'major_feature': (0.5, 0.5, None), 'minor_feature': (0.5, 0.5, None),
    'dark_forest': (0.7, 0.8, None), 'enchanted_forest': (0.5, 0.5, None), 'fire_swamp': (1.0, 0.4, 7089196),
}
EMPTY_SPAWNS = {k: [] for k in ['ambient', 'axolotls', 'creature', 'misc', 'monster', 'underground_water_creature',
                                'water_ambient', 'water_creature']}
for name, (temp, rain, water) in BIOMES.items():
    write(f'data/{NS}/worldgen/biome/{name}.json', {
        # the biome lists of TFBiomeBase come with Teil 24c
        'attributes': {
            'minecraft:gameplay/natural_mob_spawns': {'argument': {'spawn_costs': {}, 'spawns_by_category': EMPTY_SPAWNS},
                                                       'modifier': 'overlay'},
        },
        'carvers': [],
        'downfall': rain,
        'effects': {'water_color': '#%06x' % water if water is not None else '#3f76e4'},
        'features': [],
        'has_precipitation': True,
        'temperature': temp,
    })

# blocks
CUBES = {'hedge': 'hedge', 'darkwood_leaves': 'darkwood_leaves'}
for name, texture in CUBES.items():
    write(f'assets/{NS}/blockstates/{name}.json', {'variants': {'': {'model': f'{NS}:block/{name}'}}})
    write(f'assets/{NS}/models/block/{name}.json', {'parent': 'minecraft:block/cube_all', 'textures': {'all': f'{NS}:block/{texture}'}})
    write(f'assets/{NS}/items/{name}.json', {'model': {'type': 'minecraft:model', 'model': f'{NS}:block/{name}'}})

# BlockTFPortal: the nether portal texture on a slab of 12/16, drawn on the top face and the four sides
write(f'assets/{NS}/blockstates/twilight_portal.json', {'variants': {'': {'model': f'{NS}:block/twilight_portal'}}})
write(f'assets/{NS}/models/block/twilight_portal.json', {
    'textures': {'particle': 'minecraft:block/nether_portal', 'portal': 'minecraft:block/nether_portal'},
    'elements': [{
        'from': [0, 0, 0], 'to': [16, 12, 16],
        'faces': {
            'up': {'texture': '#portal', 'cullface': 'up'},
            'north': {'uv': [0, 4, 16, 16], 'texture': '#portal', 'cullface': 'north'},
            'south': {'uv': [0, 4, 16, 16], 'texture': '#portal', 'cullface': 'south'},
            'west': {'uv': [0, 4, 16, 16], 'texture': '#portal', 'cullface': 'west'},
            'east': {'uv': [0, 4, 16, 16], 'texture': '#portal', 'cullface': 'east'},
        },
    }],
})

LANG_EN = {
    f'block.{NS}.twilight_portal': 'Twilight Portal',
    f'block.{NS}.hedge': 'Hedge',
    f'block.{NS}.darkwood_leaves': 'Darkwood Leaves',
    f'biome.{NS}.twilight_lake': 'Twilight Lake', f'biome.{NS}.twilight_forest': 'Twilight Forest',
    f'biome.{NS}.dense_twilight_forest': 'Dense Twilight Forest', f'biome.{NS}.highlands': 'Highlands',
    f'biome.{NS}.mushrooms': 'Mushrooms', f'biome.{NS}.twilight_swamp': 'Twilight Swamp',
    f'biome.{NS}.twilight_stream': 'Twilight Stream', f'biome.{NS}.snowy_forest': 'Snowy Forest',
    f'biome.{NS}.glacier': 'Glacier', f'biome.{NS}.twilight_clearing': 'Twilight Clearing',
    f'biome.{NS}.clearing_border': 'Clearing Border', f'biome.{NS}.lake_border': 'Lake Border',
    f'biome.{NS}.deep_mushrooms': 'Lots of Mushrooms', f'biome.{NS}.major_feature': 'Major Feature',
    f'biome.{NS}.minor_feature': 'Minor Feature', f'biome.{NS}.dark_forest': 'Dark Forest',
    f'biome.{NS}.enchanted_forest': 'Enchanted Forest', f'biome.{NS}.fire_swamp': 'Fire Swamp',
}
LANG_DE = dict(LANG_EN)
LANG_DE.update({
    f'block.{NS}.twilight_portal': 'Zwielicht-Portal', f'block.{NS}.hedge': 'Hecke', f'block.{NS}.darkwood_leaves': 'Dunkelholzlaub',
    f'biome.{NS}.twilight_lake': 'Zwielichtsee', f'biome.{NS}.twilight_forest': 'Zwielichtwald',
    f'biome.{NS}.dense_twilight_forest': 'Dichter Zwielichtwald', f'biome.{NS}.highlands': 'Hochland',
    f'biome.{NS}.mushrooms': 'Pilzwald', f'biome.{NS}.twilight_swamp': 'Zwielichtsumpf', f'biome.{NS}.twilight_stream': 'Zwielichtbach',
    f'biome.{NS}.snowy_forest': 'Verschneiter Wald', f'biome.{NS}.glacier': 'Gletscher', f'biome.{NS}.twilight_clearing': 'Zwielichtlichtung',
    f'biome.{NS}.clearing_border': 'Lichtungsrand', f'biome.{NS}.lake_border': 'Seeufer', f'biome.{NS}.deep_mushrooms': 'Viele Pilze',
    f'biome.{NS}.major_feature': 'Großes Bauwerk', f'biome.{NS}.minor_feature': 'Kleines Bauwerk', f'biome.{NS}.dark_forest': 'Dunkelwald',
    f'biome.{NS}.enchanted_forest': 'Verzauberter Wald', f'biome.{NS}.fire_swamp': 'Feuersumpf',
})
write(f'assets/{NS}/lang/en_us.json', LANG_EN)
write(f'assets/{NS}/lang/de_de.json', LANG_DE)

with open(os.path.join(ROOT, '..', '..', '..', 'original-assets.txt'), 'w') as f:
    f.write('# path in twilightforest-1.18.1.zip -> path in the ported jar (written by tools/gen.py)\n')
    for name, texture in CUBES.items():
        f.write(f'mods/twilightforest/textures/blocks/{texture}.png -> assets/{NS}/textures/block/{texture}.png\n')
print('ok')

#!/usr/bin/env python3
"""Generates assets/data JSON, the language file and original-assets.txt of the AsgardShield core port
(mods/asgardshield). The textures of the addons ship in the core archive (as in 1.5.2), so they are imported here too."""
import os, sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from asgen import Gen, CORE, HS

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
g = Gen(ROOT, CORE)

# ---------------------------------------------------------------- textures: everything of mods/mod_AsgardShield
SHIELD_TEX = {'Wood': 'wooden', 'Stone': 'stone', 'Iron': 'iron', 'Diamond': 'diamond', 'Nether': 'nether_quartz', 'Ender': 'ender',
              'Skull': 'skull', 'Patchwork': 'patchwork', 'Livingmetal': 'livingmetal', 'Biomass': 'biomass'}
SWORD_TEX = {'Wood': 'wooden', 'Stone': 'stone', 'Iron': 'iron', 'Gold': 'golden', 'Diamond': 'diamond', 'Nether': 'nether_quartz',
             'Ender': 'ender', 'Skull': 'skull', 'Patchwork': 'patchwork', 'Livingmetal': 'livingmetal', 'Biomass': 'biomass'}
SRC = 'mods/mod_AsgardShield/textures'
DST = f'assets/{CORE}/textures'
assets = []
for old, new in SHIELD_TEX.items():
    for gild, prefix in (('', ''), ('Gilded', 'gilded_')):
        for suffix in ('', '_overlay'):
            assets.append((f'{SRC}/items/item.ASShield{old}{gild}{suffix}.png', f'{DST}/item/{prefix}{new}_shield{suffix}.png'))
for old, new in SWORD_TEX.items():
    assets.append((f'{SRC}/items/item.ASGiantSword{old}.png', f'{DST}/item/{new}_giant_sword.png'))
assets.append((f'{SRC}/gui/GuardGauge.png', f'{DST}/gui/guard_gauge.png'))
assets.append((f'{SRC}/gui/VCGauge.png', f'{DST}/gui/vanguard_gauge.png'))

# ---------------------------------------------------------------- items and recipes
SHIELDS = [('wooden_shield', 'Wooden Shield', 'wood', '#minecraft:planks'),
           ('stone_shield', 'Stone Shield', 'stone', 'minecraft:cobblestone'),
           ('iron_shield', 'Iron Shield', 'iron', 'minecraft:iron_ingot'),
           ('diamond_shield', 'Diamond Shield', 'diamond', 'minecraft:diamond')]
for name, title, color, material in SHIELDS:
    g.shield(name, title, color, name)
    g.shield('gilded_' + name, 'Gilded ' + title, color, 'gilded_' + name)
    g.shield_recipe(name, material)
    # repair: the tool material, and leather (ItemArmor of the CLOTH material)
    g.tag(f'{CORE}:repairs/shield/{color}', material, 'minecraft:leather')
SWORDS = [('wooden_giant_sword', 'Wooden Giant Sword', 'wood', '#minecraft:planks'),
          ('stone_giant_sword', 'Stone Giant Sword', 'stone', 'minecraft:cobblestone'),
          ('iron_giant_sword', 'Iron Giant Sword', 'iron', 'minecraft:iron_ingot'),
          ('golden_giant_sword', 'Golden Giant Sword', 'gold', 'minecraft:gold_ingot'),
          ('diamond_giant_sword', 'Diamond Giant Sword', 'diamond', 'minecraft:diamond')]
for name, title, material_key, material in SWORDS:
    g.sword(name, title, name)
    g.sword_recipe(name, material, 'minecraft:stick')
    g.tag(f'{CORE}:repairs/giant_sword/{material_key}', material)
g.recipes['gild_shield'] = {'type': f'{CORE}:gild_shield'}

# ASAddonConfig.HarkenScytheMod: Harken Scythe weapons that do not count as swords when switching to the shield
HARKEN = [f'{m}_{w}' for w in ('scythe', 'glaive') for m in ('wooden', 'stone', 'iron', 'diamond', 'golden', 'livingmetal', 'biomass')]
HARKEN += ['soul_reaper', 'necronomicon']
g.tag(f'{CORE}:harkenscythe_weapons', *[{'id': f'{HS}:{h}', 'required': False} for h in HARKEN])

# ---------------------------------------------------------------- language
PERKS = {1: ('Arrow Catch', 'Fire Damage'), 2: ('Fire Resistant', 'Explosive Damage'), 3: ('Explosive Resistant', 'Water Damage'),
         4: ('Projectile Reflect', 'Reflected Damage'), 5: ('Fire Catch', 'Fatigue'), 6: ('Teleport Displace', 'Ender Damage'),
         7: ('Fear', 'Brittle Bones'), 8: ('Foulness', 'Foulness'), 9: ('Soul Matrix', 'Soul Eater'), 10: ('Blood Matrix', 'Blood Drinker')}
g.lang[f'tooltip.{CORE}.perk'] = 'Special Perk: %s'
g.lang[f'tooltip.{CORE}.weakness'] = 'Weakness: %s'
for i, (perk, weakness) in PERKS.items():
    g.lang[f'tooltip.{CORE}.perk.{i}'] = perk
    g.lang[f'tooltip.{CORE}.weakness.{i}'] = weakness
g.lang[f'jei.{CORE}.shield'] = (
    'Hold right-click to guard: the shield takes the hits instead of you and loses durability. Guarding fills the Guard '
    'Gauge above the food bar (gilded shields more slowly); when it is full the guard breaks until the gauge has emptied '
    'a good way. Holding the shield without guarding lowers the damage you take. With a shield in the hotbar the Vanguard '
    'Count above the hotbar fills up; when it is full, sneak while guarding to strike everything around you. Right-click '
    'with a sword switches to the shield of the hotbar, letting go switches back. Every shield has a special perk and a '
    'weakness. Dye shields like leather armour; six gold ingots around a shield gild it.')
g.lang[f'jei.{CORE}.giant_sword'] = (
    'A heavy sword that guards like a shield (hold right-click): it takes the hits instead of you and fills the Guard '
    'Gauge, but has no perks. Holding it without guarding lowers the damage you take.')
g.finish()

with open(ROOT + '/original-assets.txt', 'w') as f:
    f.write('# path in AsgardShield 2.0.4 (asgardshield.zip) -> path in the ported jar\n')
    f.write('# the archive also holds the textures of the three addons (mods/asgardshield-addons uses them from here)\n')
    for s, d in assets:
        f.write(f'{s} -> {d}\n')
print('assets', len(assets))

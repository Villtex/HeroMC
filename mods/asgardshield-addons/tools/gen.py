#!/usr/bin/env python3
"""Generates assets/data JSON and the language file of the AsgardShield addons port (mods/asgardshield-addons).
The textures are imported by the core (mods/asgardshield/original-assets.txt), the models point there."""
import os, sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
sys.path.insert(0, os.path.join(ROOT, '..', 'asgardshield', 'tools'))
from asgen import Gen, HS

g = Gen(ROOT, 'asaddons')
HS_LOADED = [{'type': 'neoforge:mod_loaded', 'modid': HS}]
LEATHER = 'minecraft:leather'


def opt(item):
    return {'id': item, 'required': False}


SHIELDS = [  # name, title, colour key, texture, repair material
    ('nether_quartz_shield', 'Nether Quartz Shield', 'nether', 'minecraft:quartz'),
    ('ender_shield', 'Ender Shield', 'ender', 'minecraft:obsidian'),
    ('skull_shield', 'Skull Shield', 'skull', 'minecraft:bone'),
    ('patchwork_shield', 'Patchwork Shield', 'patchwork', 'minecraft:rotten_flesh'),
    ('livingmetal_shield', 'Livingmetal Shield', 'livingmetal', opt(f'{HS}:livingmetal_ingot')),
    ('biomass_shield', 'Biomass Shield', 'biomass', opt(f'{HS}:biomass')),
]
for name, title, color, repair in SHIELDS:
    g.shield(name, title, color, name)
    g.shield('gilded_' + name, 'Gilded ' + title, color, 'gilded_' + name)
    g.tag(f'asaddons:repairs/shield/{color}', repair, LEATHER)
SWORDS = [
    ('nether_quartz_giant_sword', 'Nether Quartz Giant Sword', 'nether', 'minecraft:quartz'),
    ('ender_giant_sword', 'Ender Giant Sword', 'ender', 'minecraft:obsidian'),
    ('skull_giant_sword', 'Skull Giant Sword', 'skull', 'minecraft:bone'),
    ('patchwork_giant_sword', 'Patchwork Giant Sword', 'patchwork', 'minecraft:rotten_flesh'),
    ('livingmetal_giant_sword', 'Livingmetal Giant Sword', 'livingmetal', opt(f'{HS}:livingmetal_ingot')),
    ('biomass_giant_sword', 'Biomass Giant Sword', 'biomass', opt(f'{HS}:biomass')),
]
for name, title, color, repair in SWORDS:
    g.sword(name, title, name)
    g.tag(f'asaddons:repairs/giant_sword/{color}', repair)

# ASAddon_NetherEnder
g.shield_recipe('nether_quartz_shield', 'minecraft:quartz')
g.shield_recipe('ender_shield', 'minecraft:obsidian', top='minecraft:ender_eye')
g.sword_recipe('nether_quartz_giant_sword', 'minecraft:quartz', 'minecraft:blaze_rod')
g.sword_recipe('ender_giant_sword', 'minecraft:obsidian', 'minecraft:blaze_rod', middle='minecraft:ender_eye')
# ASAddon_Hallowed (Item.skull without damage value: the skeleton skull)
g.shield_recipe('skull_shield', 'minecraft:bone', top='minecraft:skeleton_skull')
g.shaped('patchwork_shield', ['XXX', 'XXX', ' X '], {'X': 'minecraft:rotten_flesh'}, g.n('patchwork_shield'))
g.sword_recipe('skull_giant_sword', 'minecraft:bone', 'minecraft:stick', middle='minecraft:skeleton_skull')
g.shaped('patchwork_giant_sword', [' XX', 'XXX', 'XX '], {'X': 'minecraft:rotten_flesh'}, g.n('patchwork_giant_sword'))
# ASAddon_HarkenScythe: only with Harken Scythe
g.shield_recipe('livingmetal_shield', f'{HS}:livingmetal_ingot', conditions=HS_LOADED)
g.shield_recipe('biomass_shield', f'{HS}:biomass', conditions=HS_LOADED)
g.sword_recipe('livingmetal_giant_sword', f'{HS}:livingmetal_ingot', 'minecraft:stick', conditions=HS_LOADED)
g.sword_recipe('biomass_giant_sword', f'{HS}:biomass', 'minecraft:stick', conditions=HS_LOADED)
g.finish()

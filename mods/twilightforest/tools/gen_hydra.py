# Teil 24f: the hydra (run from gen.py after gen_mobs.py and gen_items.py; uses write, n, NS, ASSETS, names, tag,
# SOUNDS_JSON, EXTRA_SOUNDS, count, drop, mob_loot)

# ---------------------------------------------------------------- entities, egg (EntityTFHydra, mob 189)
HYDRA_EGG = (1321280, 2719851)
for e, en, de in (('hydra', 'Hydra', 'Hydra'), ('hydra_head', 'Hydra Head', 'Hydrakopf'), ('hydra_mortar', 'Hydra Mortar', 'Hydra-Mörser')):
    names(f'entity.{NS}.{e}', en, de)
write(f'assets/{NS}/models/item/hydra_spawn_egg.json', {'parent': 'minecraft:item/generated', 'textures': {'layer0': 'minecraft:item/egg'}})
write(f'assets/{NS}/items/hydra_spawn_egg.json', {'model': {'type': 'minecraft:model', 'model': n('item/hydra_spawn_egg'),
                                                           'tints': [{'type': 'minecraft:constant', 'value': (0xFF000000 | HYDRA_EGG[0]) - (1 << 32)}]}})
names(f'item.{NS}.hydra_spawn_egg', 'Hydra Spawn Egg', 'Spawn-Ei (Hydra)')
tag('entity_type', 'c:bosses', n('hydra'))

# textures (model/hydra4.png, model/hydramortar.png)
ASSETS.append(('mods/twilightforest/textures/model/hydra4.png', f'assets/{NS}/textures/entity/hydra.png'))
ASSETS.append(('mods/twilightforest/textures/model/hydramortar.png', f'assets/{NS}/textures/entity/hydra_mortar.png'))

# the flame breath (EntityTFLargeFlameFX: the vanilla flame texture, index 48)
write(f'assets/{NS}/particles/large_flame.json', {'textures': ['minecraft:flame']})

# ---------------------------------------------------------------- sounds (TFSounds: mob/tf/hydra/*)
HYDRA_SOUNDS = {
    'hydra.growl': (['growl1', 'growl2', 'growl3'], 'Hydra growls', 'Hydra knurrt'),
    'hydra.hurt': (['hurt5', 'hurt2', 'hurt6', 'hurt4'], 'Hydra hurts', 'Hydra verletzt'),
    'hydra.death': (['death'], 'Hydra dies', 'Hydra stirbt'),
    'hydra.roar': (['roar3', 'roar4'], 'Hydra roars', 'Hydra brüllt'),
    'hydra.warn': (['warn1'], 'Hydra hisses', 'Hydra zischt'),
}
for event, (files, en, de) in HYDRA_SOUNDS.items():
    EXTRA_SOUNDS[event] = {'sounds': [n(f'hydra/{f}') for f in files], 'subtitle': f'subtitles.{NS}.{event}'}
    for f in files:
        ASSETS.append((f'mods/twilightforest/sounds/hydra/{f}.ogg', f'assets/{NS}/sounds/hydra/{f}.ogg'))
    names(f'subtitles.{NS}.{event}', en, de)
# sounds.json was written by gen_mobs.py already: write it again with the hydra's events
SOUNDS_JSON.update(EXTRA_SOUNDS)
write(f'assets/{NS}/sounds.json', SOUNDS_JSON)

# ---------------------------------------------------------------- loot (EntityTFHydra.dropFewItems)
# rand(3 + looting) + 5 times dropItem(hydraChop, 5): stacks of five; the looting part as 0..5 chops per level
mob_loot('hydra', [
    {'rolls': count(5, 7), 'entries': [{'type': 'minecraft:item', 'name': n('hydra_chop'),
                                        'modifier': [{'type': 'minecraft:set_count', 'count': 5}]}]},
    {'rolls': 1, 'entries': [{'type': 'minecraft:item', 'name': n('hydra_chop'), 'modifier': [
        {'type': 'minecraft:set_count', 'count': 0},
        {'type': 'minecraft:enchanted_count_increase', 'count': {'type': 'minecraft:uniform', 'min': 0.0, 'max': 5.0},
         'enchantment': 'minecraft:looting'}]}]},
    # rand(4 + looting) + 7 fiery blood
    drop(n('fiery_blood'), 7, 10),
    {'rolls': 1, 'entries': [{'type': 'minecraft:item', 'name': n('hydra_trophy')}]},
])

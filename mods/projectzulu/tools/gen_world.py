# Teil 11, batch World (ZuluWorld). Runs inside tools/gen.py (write, n, NS, lang, assets).
# The limited mob spawner of Project Zulu|Dungeon (dungeon/BlockLimitedMobSpawner.java). Its unlocalized name was
# "mobSpawner" without own icons, so it looked like the vanilla spawner: the models use the vanilla spawner model.
# No loot table: the block drops nothing (only experience, in LimitedMobSpawnerBlock).

def _pzw_merge_tag(path, values):
    full = os.path.join(RES, path)
    obj = {'replace': False, 'values': []}
    if os.path.isfile(full):
        with open(full) as f:
            obj = json.load(f)
    for v in values:
        if v not in obj['values']:
            obj['values'].append(v)
    write(path, obj)

write(f'assets/{NS}/blockstates/limited_mob_spawner.json', {'variants': {'': {'model': 'minecraft:block/spawner'}}})
write(f'assets/{NS}/items/limited_mob_spawner.json', {'model': {'type': 'minecraft:model', 'model': 'minecraft:block/spawner'}})
lang[f'block.{NS}.limited_mob_spawner'] = 'Limited Mob Spawner'
# Material.rock: only a pickaxe mines it at full speed
_pzw_merge_tag('data/minecraft/tags/block/mineable/pickaxe.json', [n('limited_mob_spawner')])

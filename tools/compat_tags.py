#!/usr/bin/env python3
"""Teil 26e: common tags (c:, minecraft:) for all ported mods, so that mods of today see Hexxit's ores, metals,
chests, tools and bosses, and Hexxit recipes accept theirs.

The original pack exchanged these through the Forge OreDictionary (ingotCopper, oreTin, ...); the c: tags are its
successor. The script reads the item and block ids from the built jars (build/pack/mods, run ./gradlew pack first),
sorts them into tags by the rules below and merges them into the tag files of the mod owning each id
(mods/<mod>/src/main/resources/data/<ns>/tags/...): existing entries stay, new ones are appended, "replace" is false.

  python3 tools/compat_tags.py          write
  python3 tools/compat_tags.py --check  only list what would change (exit 1 if anything)
"""
import glob, json, os, re, sys, zipfile
from collections import defaultdict

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))

# ---------------------------------------------------------------------------------------------------- id lists
def ids():
    items, blocks, fluids = set(), set(), set()
    for jar in glob.glob(os.path.join(ROOT, 'build/pack/mods/*.jar')):
        with zipfile.ZipFile(jar) as z:
            for n in z.namelist():
                m = re.match(r'assets/([^/]+)/(items|blockstates)/([^/]+)\.json$', n)
                if m:
                    (items if m.group(2) == 'items' else blocks).add(f'{m.group(1)}:{m.group(3)}')
    if not items:
        sys.exit('no jars in build/pack/mods: run CI=true ./gradlew pack first')
    return items, blocks


def mod_dirs():
    out = {}
    for props in glob.glob(os.path.join(ROOT, 'mods/*/gradle.properties')):
        for line in open(props):
            if line.startswith('mod_id='):
                out[line.split('=', 1)[1].strip()] = os.path.dirname(props)
    out['harkenscythe_core'] = os.path.join(ROOT, 'mods/harkenscythe')
    return out


ITEMS, BLOCKS = ids()
TAGS = defaultdict(list)  # (kind, tag) -> [entries]


def add(kind, tag, *entries):
    for e in entries:
        if e not in TAGS[(kind, tag)]:
            TAGS[(kind, tag)].append(e)


def both(tag, entry):
    """Block item: block and item tag."""
    if entry in BLOCKS:
        add('block', tag, entry)
    if entry in ITEMS:
        add('item', tag, entry)


def sub(kind, parent, name, entry):
    """c:<parent>/<name> with the entry, and c:<parent> with #c:<parent>/<name>."""
    if kind == 'both':
        both(f'c:{parent}/{name}', entry)
        if entry in BLOCKS:
            add('block', f'c:{parent}', f'#c:{parent}/{name}')
        if entry in ITEMS:
            add('item', f'c:{parent}', f'#c:{parent}/{name}')
    else:
        add(kind, f'c:{parent}/{name}', entry)
        add(kind, f'c:{parent}', f'#c:{parent}/{name}')


# ---------------------------------------------------------------------------------------------------- metals
# material in the id -> name in the c: tag
METAL = {'alubrass': 'aluminum_brass', 'knight_metal': 'knightmetal'}
for i in sorted(ITEMS):
    ns, path = i.split(':')
    if ns == 'minecraft':
        continue
    m = re.fullmatch(r'([a-z]+(?:_brass)?)_(ingot|nugget)', path)
    if m and not path.startswith(('fancy_', 'obsidian_ingot_brick')):
        sub('item', 'ingots' if m.group(2) == 'ingot' else 'nuggets', METAL.get(m.group(1), m.group(1)), i)
    m = re.fullmatch(r'raw_([a-z]+)', path)
    if m and m.group(1) in ('aluminum', 'ironwood'):
        sub('item', 'raw_materials', m.group(1), i)
add('item', 'c:ingots/knightmetal', 'twilightforest:knight_metal')
add('item', 'c:ingots', '#c:ingots/knightmetal')
INGOT_METALS = {t.split('/')[1] for (k, t) in TAGS if k == 'item' and t.startswith('c:ingots/')}
for b in sorted(BLOCKS):
    ns, path = b.split(':')
    m = re.fullmatch(r'([a-z]+)_block', path)
    if m and METAL.get(m.group(1), m.group(1)) in INGOT_METALS:
        sub('both', 'storage_blocks', METAL.get(m.group(1), m.group(1)), b)

# ores: (id, material, ground)
ORES = [
    ('tconstruct:copper_ore', 'copper', 'stone'), ('tconstruct:tin_ore', 'tin', 'stone'),
    ('tconstruct:aluminum_ore', 'aluminum', 'stone'), ('tconstruct:cobalt_ore', 'cobalt', 'netherrack'),
    ('tconstruct:ardite_ore', 'ardite', 'netherrack'),
    ('tconstruct:copper_gravel_ore', 'copper', None), ('tconstruct:tin_gravel_ore', 'tin', None),
    ('tconstruct:aluminum_gravel_ore', 'aluminum', None), ('tconstruct:cobalt_gravel_ore', 'cobalt', None),
    ('tconstruct:iron_gravel_ore', 'iron', None), ('tconstruct:gold_gravel_ore', 'gold', None),
    ('meteors:meteor_ore', 'meteorite', 'stone'),
    ('quantumcraft:quantum_ore', 'quantum', 'stone'), ('quantumcraft:quantum_ore_glowing', 'quantum', 'stone'),
]
for ore, mat, ground in ORES:
    sub('both', 'ores', mat, ore)
    if ground:
        both(f'c:ores_in_ground/{ground}', ore)
    # Tinkers' metal ores drop themselves (one ore per block), quantum ore drops dust like redstone
    both('c:ore_rates/singular' if ore.startswith(('tconstruct:', 'meteors:')) else 'c:ore_rates/dense', ore)

# gems and dusts
for entry, kind, name in [('meteors:red_meteor_gem', 'gems', 'red_meteor'), ('meteors:frezarite_crystal', 'gems', 'frezarite'),
                          ('meteors:kreknorite_chip', 'gems', 'kreknorite'),
                          ('quantumcraft:quantum_dust', 'dusts', 'quantum'), ('legendgear:stardust', 'dusts', 'stardust'),
                          ('legendgear:emerald_shard', 'nuggets', 'emerald')]:
    if entry in ITEMS:
        sub('item', kind, name, entry)
sub('both', 'storage_blocks', 'red_meteor', 'meteors:red_meteor_gem_block')
sub('both', 'storage_blocks', 'livingmetal', 'harkenscythe_core:livingmetal_block')

# ---------------------------------------------------------------------------------------------------- chests
for b in sorted(BLOCKS):
    ns, path = b.split(':')
    if ns == 'betterstorage' and (path.startswith('reinforced_') and path.endswith('_chest') or path == 'locker'):
        both('c:chests', b)
both('c:chests/wooden', 'secretroomsmod:secret_chest')
both('c:chests/trapped', 'secretroomsmod:secret_trapped_chest')
for t in ('c:chests/wooden', 'c:chests/trapped'):
    if ('block', t) in TAGS:
        add('block', 'c:chests', '#' + t)
    if ('item', t) in TAGS:
        add('item', 'c:chests', '#' + t)

# ---------------------------------------------------------------------------------------------------- tools
TINKERS = {
    'pickaxe': ['pickaxe', 'mining'], 'hammer': ['pickaxe', 'mining'], 'shovel': ['shovel'], 'excavator': ['shovel'],
    'hatchet': ['axe'], 'lumberaxe': ['axe'], 'mattock': ['axe', 'shovel'], 'battleaxe': ['axe', 'melee'],
    'broadsword': ['sword'], 'longsword': ['sword'], 'rapier': ['sword'], 'cutlass': ['sword'], 'dagger': ['sword'],
    'frypan': ['melee'], 'battlesign': ['melee'], 'scythe': ['hoe', 'melee'], 'shortbow': ['bow'], 'chisel': [],
}
OWN = {'soulshards:shadow_blade': ['sword'], 'legendgear:slime_sword': ['sword']}
NOT_TOOLS = {'meteors:meteor_shield'}  # a block


def roles(i):
    if i in NOT_TOOLS:
        return []
    ns, path = i.split(':')
    if ns == 'minecraft':
        return []
    if ns == 'tconstruct':
        return TINKERS.get(path, [])
    if i in OWN:
        return OWN[i]
    for suffix, r in (('_giant_sword', ['sword']), ('_sword', ['sword']), ('_bigsword', ['sword']), ('_dagger', ['sword']),
                      ('_pickaxe', ['pickaxe', 'mining']), ('_axe', ['axe']), ('_hatchet', ['axe']),
                      ('_shovel', ['shovel']), ('_hoe', ['hoe']), ('_scythe', ['melee']), ('_glaive', ['melee']),
                      ('_spear', ['melee']), ('_kama', ['shears']), ('_bow', ['bow']), ('_shield', ['shield'])):
        if path.endswith(suffix) and not path.endswith('_spawn_egg'):
            return r
    return []


ROLE_TAGS = {'sword': ['minecraft:swords', 'c:tools/melee_weapon'], 'axe': ['minecraft:axes'],
             'pickaxe': ['minecraft:pickaxes'], 'shovel': ['minecraft:shovels'], 'hoe': ['minecraft:hoes'],
             'mining': ['c:tools/mining_tool'], 'melee': ['c:tools/melee_weapon'], 'shears': ['c:tools/shear'],
             'bow': ['c:tools/bow', 'c:tools/ranged_weapon'], 'shield': ['c:tools/shield']}
for i in sorted(ITEMS):
    for r in roles(i):
        for t in ROLE_TAGS[r]:
            add('item', t, i)
for t in ('c:tools/melee_weapon', 'c:tools/mining_tool', 'c:tools/shear', 'c:tools/bow', 'c:tools/ranged_weapon', 'c:tools/shield'):
    if ('item', t) in TAGS:
        add('item', 'c:tools', '#' + t)

# ---------------------------------------------------------------------------------------------------- fluids, mobs
for m in ('iron', 'gold', 'copper', 'tin', 'aluminum', 'cobalt', 'ardite', 'bronze', 'alubrass', 'manyullyn', 'alumite',
          'obsidian', 'steel', 'glass', 'emerald', 'nickel', 'lead', 'silver', 'platinum', 'invar', 'electrum'):
    name = METAL.get(m, m)
    add('fluid', f'c:molten_{name}', f'tconstruct:molten_{m}', f'tconstruct:flowing_molten_{m}')
add('fluid', 'c:blood', 'tconstruct:blood', 'tconstruct:flowing_blood')
add('entity_type', 'c:bosses', 'twilightforest:naga', 'twilightforest:lich', 'twilightforest:hydra',
    'twilightforest:minoshroom', 'twilightforest:ur_ghast')

# ---------------------------------------------------------------------------------------------------- write
KIND_DIR = {'item': 'item', 'block': 'block', 'fluid': 'fluid', 'entity_type': 'entity_type'}


# ---------------------------------------------------------------------------------------------------- recipes
# Where the original took an ore dictionary word (ingotTin, blockSteel, oreAluminum, ...), the recipe takes the c: tag.
# Metals that vanilla has itself (iron, gold, copper) keep their items: their tags would mix vanilla's nuggets and
# ingots into Hexxit's recipes and clash with vanilla's own recipes.
RECIPE_KINDS = ('ingots', 'nuggets', 'storage_blocks', 'ores', 'raw_materials', 'dusts')
VANILLA_METALS = {'iron', 'gold', 'copper'}
RECIPE_MODS = ('tconstruct', 'artifice', 'natura', 'betterstorage', 'backpacks', 'bibliocraft', 'hexxitgear', 'xreliquary',
               'chococraft', 'enchantingplus', 'legendgear', 'secretrooms', 'qcraft')


def recipe_tags():
    tag_of = {}
    for (kind, tag), entries in TAGS.items():
        if kind != 'item' or tag.count('/') != 1:
            continue
        group, name = tag[2:].split('/')
        if group in RECIPE_KINDS and name not in VANILLA_METALS:
            for e in entries:
                if not e.startswith('#') and 'gravel_ore' not in e:
                    tag_of.setdefault(e, '#' + tag)
    return tag_of


def rewrite_recipe(node, tag_of):
    """Replaces item ids by tags in ingredient positions (keys, ingredient lists, single ingredients); not in a
    result and not inside a list of alternatives (a tag cannot stand there)."""
    count = 0
    if isinstance(node, dict):
        for k, v in node.items():
            if k in ('result', 'results', 'output'):
                continue
            if isinstance(v, str) and k in ('ingredient', 'base', 'addition', 'template', 'input'):
                if v in tag_of:
                    node[k] = tag_of[v]
                    count += 1
            elif k == 'key' and isinstance(v, dict):
                for kk, vv in v.items():
                    if isinstance(vv, str) and vv in tag_of:
                        v[kk] = tag_of[vv]
                        count += 1
            elif k == 'ingredients' and isinstance(v, list):
                for idx, vv in enumerate(v):
                    if isinstance(vv, str) and vv in tag_of:
                        v[idx] = tag_of[vv]
                        count += 1
            elif isinstance(v, (dict, list)):
                count += rewrite_recipe(v, tag_of)
    elif isinstance(node, list):
        for v in node:
            count += rewrite_recipe(v, tag_of)
    return count


def recipes(check, changed):
    tag_of = recipe_tags()
    for mod in RECIPE_MODS:
        for f in sorted(glob.glob(os.path.join(ROOT, 'mods', mod, 'src/main/resources/data/*/recipe/**/*.json'), recursive=True)):
            data = json.load(open(f))
            n = rewrite_recipe(data, tag_of)
            if n:
                changed.append(f'{os.path.relpath(f, ROOT)}: {n} ingredient(s) -> tag')
                if not check:
                    with open(f, 'w') as out:
                        json.dump(data, out, indent=2)
                        out.write('\n')


def main():
    check = '--check' in sys.argv
    dirs = mod_dirs()
    changed = []
    # group entries by owning mod: every concrete id goes to its own mod's file, references to each mod that has a
    # member of the referenced tag
    per_file = defaultdict(list)
    concrete_mods = defaultdict(set)  # (kind, tag) -> mod dirs with concrete entries
    for (kind, tag), entries in TAGS.items():
        for e in entries:
            if not e.startswith('#'):
                d = dirs.get(e.split(':')[0])
                if d:
                    per_file[(d, kind, tag)].append(e)
                    concrete_mods[(kind, tag)].add(d)
    for (kind, tag), entries in TAGS.items():
        for e in entries:
            if e.startswith('#'):
                for d in sorted(concrete_mods.get((kind, e[1:]), ())):
                    per_file[(d, kind, tag)].append(e)
    for (d, kind, tag), entries in sorted(per_file.items()):
        ns, path = tag.split(':')
        f = os.path.join(d, 'src/main/resources/data', ns, 'tags', KIND_DIR[kind], path + '.json')
        old = json.load(open(f)) if os.path.exists(f) else {}
        values = list(old.get('values', []))
        new = [e for e in entries if e not in values and {'id': e, 'required': False} not in values]
        if not new:
            continue
        changed.append(f'{os.path.relpath(f, ROOT)}: +{len(new)}')
        if not check:
            os.makedirs(os.path.dirname(f), exist_ok=True)
            data = {'replace': old.get('replace', False), 'values': values + new}
            data.update({k: v for k, v in old.items() if k not in data})
            with open(f, 'w') as out:
                json.dump(data, out, indent=2)
                out.write('\n')
    recipes(check, changed)
    print('\n'.join(changed) or 'nothing to change')
    if check and changed:
        sys.exit(1)


if __name__ == '__main__':
    main()

#!/usr/bin/env python3
"""Cross-checks the generated resources against the Java registrations: every item has an item definition and a name,
every block a blockstate and a name, every model and texture reference resolves (textures via original-assets.txt)."""
import glob, json, os, re

M = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RES = M + '/src/main/resources'
NS = 'harkenscythe_core'
A = f'{RES}/assets/{NS}'
J = M + '/src/main/java/harkenscythe'
targets = {line.split('->')[1].strip() for line in open(M + '/original-assets.txt') if '->' in line}
items = set(re.findall(r'(?:scythe|glaive|keeper|amulet|armor|spectral|item)\("([a-z_]+)"', open(J + '/HSItems.java').read()))
items |= {s + '_skull' for s in re.findall(r'^\s+[A-Z_]+\("([a-z_]+)"', open(J + '/item/SkullType.java').read(), re.M)}
blocks = set(re.findall(r'(?:registerBlock|cloth)\("([a-z_]+)"', open(J + '/HSBlocks.java').read()))
block_items = set(re.findall(r'blockItem\(HSBlocks\.([A-Z_]+)', open(J + '/HSItems.java').read()))
items |= {b.lower() for b in block_items}
lang = json.load(open(A + '/lang/en_us.json'))
err = []
for i in items:
    if not os.path.exists(f'{A}/items/{i}.json'):
        err.append('no item definition ' + i)
    if f'item.{NS}.{i}' not in lang and f'block.{NS}.{i}' not in lang:
        err.append('no name ' + i)
for b in blocks:
    if not os.path.exists(f'{A}/blockstates/{b}.json'):
        err.append('no blockstate ' + b)
    if f'block.{NS}.{b}' not in lang:
        err.append('no block name ' + b)
seen = set()


def check_model(ref):
    ns, p = ref.split(':') if ':' in ref else ('minecraft', ref)
    if ns != NS or ref in seen:
        return
    seen.add(ref)
    f = f'{A}/models/{p}.json'
    if not os.path.exists(f):
        err.append('missing model ' + ref)
        return
    m = json.load(open(f))
    if 'parent' in m:
        check_model(m['parent'])
    for t in m.get('textures', {}).values():
        if isinstance(t, str) and t.startswith(NS + ':') and f'assets/{NS}/textures/{t.split(":")[1]}.png' not in targets:
            err.append(f'missing texture {t} in {ref}')


for f in glob.glob(A + '/blockstates/*.json') + glob.glob(A + '/items/*.json'):
    for ref in re.findall(r'"model": "([a-z_:/0-9]+)"', open(f).read()):
        check_model(ref)
for f in glob.glob(A + '/equipment/*.json'):
    for t in re.findall(r'"texture": "[a-z_]+:([a-z_0-9]+)"', open(f).read()):
        for layer in ('humanoid', 'humanoid_leggings'):
            if f'"{layer}"' in open(f).read() and f'assets/{NS}/textures/entity/equipment/{layer}/{t}.png' not in targets:
                err.append(f'missing equipment texture {layer}/{t}')
enchantments = {os.path.basename(f)[:-5] for f in glob.glob(f'{RES}/data/{NS}/enchantment/*.json')}
for f in glob.glob(RES + '/data/**/*.json', recursive=True):
    for ref in re.findall(r'"' + NS + r':([a-z_0-9]+)"(?!:)', open(f).read()):
        if ref not in items and ref not in blocks and ref not in enchantments and ref != 'altar' and '/recipe/' in f:
            err.append(f'unknown {NS}:{ref} in {f[len(RES):]}')
# altar recipes: one recipe per number and altar; the augment rows cover every roll and rank exactly once
orders = {}
for f in glob.glob(f'{RES}/data/{NS}/recipe/altar/*/*.json'):
    r = json.load(open(f))
    key = (r['altar'], r['order'])
    if key in orders:
        err.append(f'altar order {key} twice: {orders[key]} {f}')
    orders[key] = f
    if r.get('augments'):
        grid = [[0] * 101 for _ in range(101)]
        for a in r['augments']:
            if a['enchantment'].split(':')[1] not in enchantments:
                err.append(f'unknown augment {a["enchantment"]} in {f}')
            for roll in range(a.get('min_roll', 1), a.get('max_roll', 100) + 1):
                for rank in range(a.get('min_rank', 1), a.get('max_rank', 100) + 1):
                    grid[roll][rank] += 1
        if any(grid[x][y] != 1 for x in range(1, 101) for y in range(1, 101)):
            err.append('augment rows do not cover the dice exactly once in ' + f)
if len(orders) != 2 * 14:
    err.append(f'{len(orders)} altar recipes, expected 28')
# Teil 9, part 3: every mob has a name, a spawn egg (item definition and name) and is in exactly the tags it belongs to
mobs = re.findall(r'(?:monster|pet|humanoid)\("([a-z_]+)"', open(J + '/mob/HSMobs.java').read())
tags_of = {t: json.load(open(f'{RES}/data/{NS}/tags/entity_type/{t}.json'))['values'] for t in ('spectral_pets', 'harbingers', 'soulless')}
for m in mobs:
    if f'entity.{NS}.{m}' not in lang:
        err.append('no entity name ' + m)
    if not os.path.exists(f'{A}/items/{m}_spawn_egg.json') or f'item.{NS}.{m}_spawn_egg' not in lang:
        err.append('no spawn egg ' + m)
    expected = 'harbingers' if m == 'harbinger' else 'soulless' if m in ('ectoplasm', 'hemoglobin', 'spectral_miner_evil') else 'spectral_pets'
    for t, values in tags_of.items():
        if (f'{NS}:{m}' in values) != (t == expected):
            err.append(f'{m} wrongly in/out of tag {t}')
if len(mobs) != 26:
    err.append(f'{len(mobs)} mobs, expected 26')
for t in ('harbinger', 'spectral_miner_evil'):
    if not os.path.exists(f'{RES}/data/{NS}/loot_table/entities/{t}.json'):
        err.append('no loot table ' + t)
java = ''.join(open(p).read() for p in glob.glob(J + '/**/*.java', recursive=True))
for t in re.findall(r'id\("(textures/[a-z_/]+\.png)', java):
    if f'assets/{NS}/{t}' not in targets:
        err.append('java texture not imported: ' + t)
print('\n'.join(sorted(set(err))) or 'OK', len(items), 'items', len(blocks), 'blocks')

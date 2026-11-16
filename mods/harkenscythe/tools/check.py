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
for f in glob.glob(RES + '/data/**/*.json', recursive=True):
    for ref in re.findall(r'"' + NS + r':([a-z_0-9]+)"(?!:)', open(f).read()):
        if ref not in items and ref not in blocks and '/recipe/' in f:
            err.append(f'unknown {NS}:{ref} in {f[len(RES):]}')
java = ''.join(open(p).read() for p in glob.glob(J + '/**/*.java', recursive=True))
for t in re.findall(r'id\("(textures/[a-z_/]+\.png)', java):
    if f'assets/{NS}/{t}' not in targets:
        err.append('java texture not imported: ' + t)
print('\n'.join(sorted(set(err))) or 'OK', len(items), 'items', len(blocks), 'blocks')

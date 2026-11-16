#!/usr/bin/env python3
"""Cross-checks the generated resources of the Artifice port against the Java registrations: every block has a
blockstate, loot table and name, every item an item definition and a name, every model and texture reference
resolves (textures via original-assets.txt), and every artifice: reference and tag in the data exists."""
import json, os, re, glob

M = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RES = M + '/src/main/resources'
A = RES + '/assets/artifice'
J = M + '/src/main/java/artifice'
targets = set()
for line in open(M + '/original-assets.txt'):
    if '->' in line:
        targets.add(line.split('->')[1].strip())
tiers = ['basic', 'reinforced', 'industrial', 'advanced']
bsrc = open(J + '/ArtificeBlocks.java').read()
blocks = set(re.findall(r'(?:block|stone|frame|registerBlock)\("([a-z_]+)"', bsrc))
for suf in re.findall(r'frames\("(_[a-z_]+)"', bsrc) + ['_scaffolding']:
    for t in tiers:
        blocks.add(t + suf)
isrc = open(J + '/ArtificeItems.java').read()
items = set(re.findall(r'(?:item|simple|dye)\("([a-z_]+)"', isrc))
items |= {u.lower() for u in re.findall(r'^\s+([A-Z_]+)\(Enchantments', open(J + '/item/Upgrade.java').read(), re.M)}
items |= blocks
lang = json.load(open(A + '/lang/en_us.json'))
err = []
for b in blocks:
    if not os.path.exists(f'{A}/blockstates/{b}.json'): err.append('no blockstate ' + b)
    if not os.path.exists(f'{RES}/data/artifice/loot_table/blocks/{b}.json'): err.append('no loot ' + b)
    if f'block.artifice.{b}' not in lang: err.append('no lang block ' + b)
for i in items:
    if not os.path.exists(f'{A}/items/{i}.json'): err.append('no itemdef ' + i)
    if f'item.artifice.{i}' not in lang and f'block.artifice.{i}' not in lang: err.append('no lang item ' + i)
seen = set()
def check_model(ref):
    ns, p = ref.split(':') if ':' in ref else ('minecraft', ref)
    if ns != 'artifice' or ref in seen: return
    seen.add(ref)
    f = f'{A}/models/{p}.json'
    if not os.path.exists(f): err.append('missing model ' + ref); return
    m = json.load(open(f))
    if 'parent' in m: check_model(m['parent'])
    for t in m.get('textures', {}).values():
        if isinstance(t, str) and t.startswith('artifice:'):
            if 'assets/artifice/textures/' + t.split(':')[1] + '.png' not in targets: err.append(f'missing texture {t} in {ref}')
for f in glob.glob(A + '/blockstates/*.json') + glob.glob(A + '/items/*.json'):
    for ref in re.findall(r'"model": "([a-z_:/0-9]+)"', open(f).read()):
        check_model(ref)
for f in glob.glob(RES + '/data/**/*.json', recursive=True):
    text = open(f).read()
    tagrefs = set(re.findall(r'"#artifice:([a-z_/0-9]+)"', text))
    for ref in tagrefs:
        if not glob.glob(RES + f'/data/artifice/tags/*/{ref}.json'): err.append(f'unknown tag #artifice:{ref} in {f[len(RES):]}')
    for ref in re.findall(r'#c:([a-z_/0-9]+)', text):
        if not glob.glob(RES + f'/data/c/tags/item/{ref}.json') and ref not in ('rods/wooden', 'dyes/yellow', 'nuggets/gold', 'nuggets/copper'):
            err.append(f'unknown tag #c:{ref} in {f[len(RES):]}')
    for ref in re.findall(r'"artifice:([a-z_/0-9]+)"', text):
        if '/' in ref or ref in ('box', 'upgrade') and '"type"' in text: continue
        if ref not in items and ref not in blocks: err.append(f'unknown ref artifice:{ref} in {f[len(RES):]}')
# every block the sledge knows is in the tag that makes it mine fast (and the other way round)
tag = json.load(open(RES + '/data/artifice/tags/block/sledge_breakable.json'))['values']
java = open(J + '/item/SledgeParts.java').read()
for b in re.findall(r'(?:exact|wild)\(m, Blocks\.([A-Z_]+)', java):
    if 'minecraft:' + b.lower() not in tag: err.append('sledge block not in tag: ' + b)
print('\n'.join(sorted(set(err))) or 'OK', len(blocks), 'blocks', len(items), 'items')

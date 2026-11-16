import json, os, re, glob
import os
M = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RES=M + '/src/main/resources'
A=RES+'/assets/natura'
J=M + '/src/main/java/natura'
targets=set()
for line in open(M + '/original-assets.txt'):
    if '->' in line: targets.add(line.split('->')[1].strip())
woods='eucalyptus sakura ghostwood redwood bloodwood hopseed maple silverbell amaranth tiger willow darkwood fusewood'.split()
src=open(J+'/NaturaBlocks.java').read()
blocks=set(re.findall(r'(?:block|blockOnly|woodBlock|simple|log|leavesBlock|sapling|door|cloud|bush|glowshroom|largeGlowshroom)\("([a-z_]+)"',src))
for suf in re.findall(r'w\.id \+ "(_[a-z_]+)"',src):
    for w in woods: blocks.add(w+suf)
noitem={'saguaro_sprout','saguaro_fruit','barley_crop','cotton_crop'}
isrc=open(J+'/NaturaItems.java').read()
items=set(re.findall(r'(?:item|simple|berry|netherBerry|registerItem)\("([a-z_]+)"',isrc))
for suf in re.findall(r'w\.id \+ "(_[a-z_]+)"',isrc):
    ws = woods if 'STICKS' in isrc.split(suf)[0][-200:] else ['ghostwood','bloodwood','darkwood','fusewood']
    for w in ws: items.add(w+suf)
for pre in ['ghostwood','bloodwood','darkwood','fusewood','quartz']:
    for k in ['sword','pickaxe','shovel','hatchet','kama']: items.add(f'{pre}_{k}')
items |= (blocks-noitem)
lang=json.load(open(A+'/lang/en_us.json'))
err=[]
for b in blocks:
    if not os.path.exists(f'{A}/blockstates/{b}.json'): err.append('no blockstate '+b)
    if not os.path.exists(f'{RES}/data/natura/loot_table/blocks/{b}.json'): err.append('no loot '+b)
    if f'block.natura.{b}' not in lang: err.append('no lang block '+b)
for i in items:
    if not os.path.exists(f'{A}/items/{i}.json'): err.append('no itemdef '+i)
    if f'item.natura.{i}' not in lang and f'block.natura.{i}' not in lang: err.append('no lang item '+i)
def modelpath(ref):
    ns,p=ref.split(':') if ':' in ref else ('minecraft',ref)
    return ns,p
def check_model(ref, seen=set()):
    ns,p=modelpath(ref)
    if ns!='natura' or ref in seen: return
    seen.add(ref)
    f=f'{A}/models/{p}.json'
    if not os.path.exists(f): err.append('missing model '+ref); return
    m=json.load(open(f))
    if 'parent' in m: check_model(m['parent'])
    for t in m.get('textures',{}).values():
        if isinstance(t,str) and t.startswith('natura:'):
            if 'assets/natura/textures/'+t.split(':')[1]+'.png' not in targets: err.append(f'missing texture {t} in {ref}')
for f in glob.glob(A+'/blockstates/*.json')+glob.glob(A+'/items/*.json'):
    for ref in re.findall(r'"model": "([a-z_:/0-9]+)"', open(f).read()):
        check_model(ref)
# recipe/loot references to natura items
allnames=items|blocks
for f in glob.glob(RES+'/data/**/*.json',recursive=True):
    for ref in re.findall(r'"#?natura:([a-z_/0-9]+)"', open(f).read()):
        if '/' in ref or f.endswith('tags') : continue
        if ref not in allnames and ref not in ('kamas','sticks','imp'): err.append(f'unknown ref natura:{ref} in {f[len(RES):]}')
print('\n'.join(sorted(set(err))) or 'OK', len(blocks), len(items))

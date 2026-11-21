#!/usr/bin/env python3
"""1.5.2 ModelBase constructor -> 26.3 createBodyLayer() part definitions.

Usage:  python3 tools/modelbase_to_layer.py <ModelX.java> <root1,root2,...> [parent_var]
  ModelX.java  decompiled 1.5.2 model (.reference/<mod>/src/...), constructor `public ModelX() { ... }`
  roots        top-level ModelRenderer fields that render() draws (children come in through addChild)
  parent_var   PartDefinition variable to attach the roots to (default: root)

Copies boxes, texture offsets (also setTextureOffset("NAME.box") named boxes), mirror (only where it was set before
addBox), rotation points, rotations and the addChild hierarchy exactly. Prints the Java lines to stdout, warnings
(parts no root reaches, parts with two parents) to stderr. setRotationAngles/setLivingAnimations are ported by hand.
"""
import re, sys
src = open(sys.argv[1]).read()
roots = sys.argv[2].split(',')
parent_var = sys.argv[3] if len(sys.argv) > 3 else 'root'
m = re.search(r'public Model\w*\([^)]*\)\s*\{(.*?)\n   \}', src, re.S)
body = m.group(1)
stmts = [s.strip() for s in body.split(';') if s.strip()]
# ModelBiped of 1.5.2: the parts a subclass does not make itself keep their defaults (ModelBiped(0, 0, 64, 32))
if re.search(r'extends ModelBiped\b', src):
    defaults = {
        'bipedHead': ['this.bipedHead = new ModelRenderer(this, 0, 0)', 'this.bipedHead.addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, 0.0F)',
                      'this.bipedHead.setRotationPoint(0.0F, 0.0F, 0.0F)'],
        'bipedHeadwear': ['this.bipedHeadwear = new ModelRenderer(this, 32, 0)', 'this.bipedHeadwear.addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, 0.5F)',
                          'this.bipedHeadwear.setRotationPoint(0.0F, 0.0F, 0.0F)'],
        'bipedBody': ['this.bipedBody = new ModelRenderer(this, 16, 16)', 'this.bipedBody.addBox(-4.0F, 0.0F, -2.0F, 8, 12, 4, 0.0F)',
                      'this.bipedBody.setRotationPoint(0.0F, 0.0F, 0.0F)'],
        'bipedRightArm': ['this.bipedRightArm = new ModelRenderer(this, 40, 16)', 'this.bipedRightArm.addBox(-3.0F, -2.0F, -2.0F, 4, 12, 4, 0.0F)',
                          'this.bipedRightArm.setRotationPoint(-5.0F, 2.0F, 0.0F)'],
        'bipedLeftArm': ['this.bipedLeftArm = new ModelRenderer(this, 40, 16)', 'this.bipedLeftArm.mirror = true',
                         'this.bipedLeftArm.addBox(-1.0F, -2.0F, -2.0F, 4, 12, 4, 0.0F)', 'this.bipedLeftArm.setRotationPoint(5.0F, 2.0F, 0.0F)'],
        'bipedRightLeg': ['this.bipedRightLeg = new ModelRenderer(this, 0, 16)', 'this.bipedRightLeg.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4, 0.0F)',
                          'this.bipedRightLeg.setRotationPoint(-1.9F, 12.0F, 0.0F)'],
        'bipedLeftLeg': ['this.bipedLeftLeg = new ModelRenderer(this, 0, 16)', 'this.bipedLeftLeg.mirror = true',
                         'this.bipedLeftLeg.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4, 0.0F)', 'this.bipedLeftLeg.setRotationPoint(1.9F, 12.0F, 0.0F)'],
    }
    pre = []
    for part, lines in defaults.items():
        if not any(re.match(r'this\.%s = new ModelRenderer' % part, st) for st in stmts):
            pre += lines
    stmts = pre + stmts
# ModelRenderer fields initialised in the class body
stmts = ['this.%s = new ModelRenderer(this%s)' % (f.group(1), ', ' + f.group(2) if f.group(2) else '')
         for f in re.finditer(r'ModelRenderer (\w+) = new ModelRenderer\(this(?:, ([^)]*))?\);', src)] + stmts


def args(s):
    out, depth, cur = [], 0, ''
    for ch in s:
        if ch == '(':
            depth += 1
        if ch == ')':
            depth -= 1
        if ch == ',' and depth == 0:
            out.append(cur.strip())
            cur = ''
        else:
            cur += ch
    out.append(cur.strip())
    return out


def num(e):
    e = e.replace('this.', '').strip()
    if re.fullmatch(r'-?\d+', e):
        return e + '.0F'
    return e


texoff = {}
R = {}
order = []
for s in stmts:
    s = re.sub(r'\s+', ' ', s)
    if s.startswith('float ') or s.startswith('this.textureWidth') or s.startswith('this.textureHeight') or s.startswith('super('):
        continue
    mm = re.fullmatch(r'this\.setTextureOffset\("([^"]+)", (\d+), (\d+)\)', s)
    if mm:
        texoff[mm.group(1)] = (mm.group(2), mm.group(3))
        continue
    mm = re.fullmatch(r'this\.(\w+) = new ModelRenderer\(this(?:, (.*))?\)', s)
    if mm:
        n = mm.group(1)
        a = args(mm.group(2)) if mm.group(2) else []
        r = dict(name=n, boxname=None, uv=('0', '0'), boxes=[], rp=('0.0F', '0.0F', '0.0F'), rot=('0.0F', '0.0F', '0.0F'),
                 mirror=False, children=[])
        if len(a) == 1:
            r['boxname'] = a[0].strip('"')
        elif len(a) == 2:
            r['uv'] = (a[0], a[1])
        R[n] = r
        order.append(n)
        continue
    mm = re.fullmatch(r'this\.(\w+)\.setRotationPoint\((.*)\)', s)
    if mm:
        R[mm.group(1)]['rp'] = tuple(num(x) for x in args(mm.group(2)))
        continue
    mm = re.fullmatch(r'this\.setRotation\(this\.(\w+), (.*)\)', s)
    if mm:
        R[mm.group(1)]['rot'] = tuple(num(x) for x in args(mm.group(2)))
        continue
    mm = re.fullmatch(r'this\.(\w+)\.mirror = (true|false)', s)
    if mm:
        if mm.group(1) in R:  # mirror on the inherited part before it is replaced: no effect
            R[mm.group(1)]['mirror'] = mm.group(2) == 'true'
        continue
    mm = re.fullmatch(r'this\.(\w+)\.setTextureSize\((\d+), (\d+)\)', s)
    if mm:
        continue
    mm = re.fullmatch(r'this\.(\w+)\.setTextureOffset\((\d+), (\d+)\)', s)
    if mm:
        R[mm.group(1)]['uv'] = (mm.group(2), mm.group(3))
        continue
    mm = re.fullmatch(r'this\.(\w+)\.addChild\(this\.(\w+)\)', s)
    if mm:
        R[mm.group(1)]['children'].append(mm.group(2))
        continue
    mm = re.fullmatch(r'this\.(\w+)\.addBox\((.*)\)', s)
    if mm:
        r = R[mm.group(1)]
        a = args(mm.group(2))
        if a[0].startswith('"'):
            key = r['boxname'] + '.' + a[0].strip('"')
            uv = texoff[key]
            a = a[1:]
        else:
            uv = r['uv']
        r['boxes'].append((uv, r['mirror'], [num(x) for x in a]))
        continue
    mm = re.fullmatch(r'this\.(\w+)\.rotateAngle([XYZ]) = (.*)', s)
    if mm:
        rot = list(R[mm.group(1)]['rot'])
        rot['XYZ'.index(mm.group(2))] = num(mm.group(3))
        R[mm.group(1)]['rot'] = tuple(rot)
        continue
    mm = re.fullmatch(r'this\.(\w+)\.setTextureOffset\((\d+), (\d+)\)\.addBox\((.*)\)', s)
    if mm:
        r = R[mm.group(1)]
        r['uv'] = (mm.group(2), mm.group(3))
        r['boxes'].append((r['uv'], r['mirror'], [num(x) for x in args(mm.group(4))]))
        continue
    if re.fullmatch(r'(this\.)?(field_\w+|textureWidth|textureHeight|isJumping)\b.*|float \w+ = .*', s):
        continue
    print('warning: skipped ' + s, file=sys.stderr)

reach = set()


def walk(n):
    if n in reach:
        print('// DUPLICATE parent for', n, file=sys.stderr)
    reach.add(n)
    for c in R[n]['children']:
        walk(c)


for r in roots:
    walk(r)
unreached = [n for n in order if n not in reach]
if unreached:
    print('// UNREACHED:', unreached, file=sys.stderr)
out = []


def cubes(r):
    s = 'CubeListBuilder.create()'
    mir = False
    for uv, m, a in r['boxes']:
        if m != mir:
            s += '.mirror()' if m else '.mirror(false)'
            mir = m
        dims = a[:6]
        extra = '' if len(a) == 6 else ', new CubeDeformation(%s)' % a[6]
        s += '\n                .texOffs(%s, %s).addBox(%s%s)' % (uv[0], uv[1], ', '.join(dims), extra)
    return s


def pose(r):
    if set(r['rot']) <= {'0.0F', '-0.0F'}:
        return 'PartPose.offset(%s)' % ', '.join(r['rp'])
    return 'PartPose.offsetAndRotation(%s, %s)' % (', '.join(r['rp']), ', '.join(r['rot']))


def emit(n, parent):
    r = R[n]
    decl = 'PartDefinition %s = ' % n if r['children'] else ''
    out.append('        %s%s.addOrReplaceChild("%s", %s,\n                %s);' % (decl, parent, n, cubes(r), pose(r)))
    for c in r['children']:
        emit(c, n)


for rt in roots:
    emit(rt, parent_var)
print('\n'.join(out))

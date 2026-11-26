#!/usr/bin/env python3
"""Compare the blocks of two 26.3 worlds (region files of the dev server), e.g. before and after a change to the
world generation of Hexxit Integrated:

  python3 tools/compare_worlds.py <world A> <world B> [--dim minecraft:overworld] [--area X1 Z1 X2 Z2] [--show N]

Only chunks that are fully generated ("minecraft:full") in both worlds count; --area limits them to chunk coordinates
X1..X2 / Z1..Z2. Prints the number of blocks compared and that differ, the pairs of block states that differ most, and
the chunks with the most differences. Generation is not fully deterministic at chunk borders even with the same code
(features of neighbouring chunks overlap in a different order), so compare against two runs of the old code as well.
Save the worlds first (`save-all flush`).
"""
import argparse, collections, os, struct, sys, zlib


def read_nbt(data):
    pos = 0

    def take(fmt):
        nonlocal pos
        value = struct.unpack_from(fmt, data, pos)
        pos += struct.calcsize(fmt)
        return value[0]

    def string():
        nonlocal pos
        n = take('>H')
        value = data[pos:pos + n].decode('utf-8', 'replace')
        pos += n
        return value

    def payload(tag):
        nonlocal pos
        if tag == 1: return take('>b')
        if tag == 2: return take('>h')
        if tag == 3: return take('>i')
        if tag == 4: return take('>q')
        if tag == 5: return take('>f')
        if tag == 6: return take('>d')
        if tag == 7:
            n = take('>i'); value = data[pos:pos + n]; pos += n; return value
        if tag == 8: return string()
        if tag == 9:
            kind = take('>b'); n = take('>i'); return [payload(kind) for _ in range(n)]
        if tag == 10:
            value = {}
            while True:
                kind = take('>b')
                if kind == 0: return value
                name = string()
                value[name] = payload(kind)
        if tag == 11:
            n = take('>i'); value = struct.unpack_from(f'>{n}i', data, pos); pos += 4 * n; return value
        if tag == 12:
            n = take('>i'); value = struct.unpack_from(f'>{n}q', data, pos); pos += 8 * n; return value
        raise ValueError(f'NBT tag {tag}')

    kind = take('>b')
    string()
    return payload(kind)


def region_dir(world, dim):
    ns, path = dim.split(':')
    # 26.3: every dimension under dimensions/<namespace>/<path>; older layouts as fallback
    candidates = [os.path.join(world, 'dimensions', ns, path, 'region')]
    if dim == 'minecraft:overworld':
        candidates.append(os.path.join(world, 'region'))
    if dim == 'minecraft:the_nether':
        candidates.append(os.path.join(world, 'DIM-1', 'region'))
    for c in candidates:
        if os.path.isdir(c):
            return c
    return candidates[0]


def chunks(world, dim):
    """(chunkX, chunkZ) -> chunk NBT of every fully generated chunk."""
    out = {}
    folder = region_dir(world, dim)
    for name in os.listdir(folder):
        if not name.endswith('.mca'):
            continue
        with open(os.path.join(folder, name), 'rb') as f:
            data = f.read()
        if len(data) < 8192:
            continue
        for i in range(1024):
            offset = struct.unpack_from('>I', data, i * 4)[0]
            if offset == 0:
                continue
            start = (offset >> 8) * 4096
            length, compression = struct.unpack_from('>IB', data, start)
            raw = data[start + 5:start + 4 + length]
            if compression != 2:
                raise SystemExit(f'compression {compression} not supported (use region-file-compression=deflate)')
            nbt = read_nbt(zlib.decompress(raw))
            if nbt.get('Status') == 'minecraft:full':
                out[(nbt['xPos'], nbt['zPos'])] = nbt
    return out


def state_name(entry):
    if isinstance(entry, str):
        # 26.3 writes a state without properties as its plain name
        return entry
    if '' in entry:
        # element of a list with mixed tag types, wrapped as {"": value}
        return state_name(entry[''])
    # 26.3: {"id": ..., "properties": {...}}; before: {"Name": ..., "Properties": {...}}
    name = entry.get('id', entry.get('Name'))
    props = entry.get('properties', entry.get('Properties'))
    if not props:
        return name
    return name + '[' + ','.join(f'{k}={v}' for k, v in sorted(props.items())) + ']'


def section_states(section):
    """4096 block state names (index y * 256 + z * 16 + x) of a section, or None for all air."""
    states = section.get('block_states')
    if not states:
        return None
    palette = [state_name(p) for p in states['palette']]
    data = states.get('data')
    if not data:
        return [palette[0]] * 4096
    bits = max(4, (len(palette) - 1).bit_length())
    per_long = 64 // bits
    mask = (1 << bits) - 1
    out = []
    for value in data:
        value &= (1 << 64) - 1
        for _ in range(per_long):
            out.append(palette[value & mask])
            value >>= bits
            if len(out) == 4096:
                return out
    return out


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('a')
    ap.add_argument('b')
    ap.add_argument('--dim', default='minecraft:overworld')
    ap.add_argument('--area', type=int, nargs=4)
    ap.add_argument('--show', type=int, default=15)
    args = ap.parse_args()
    a = chunks(args.a, args.dim)
    b = chunks(args.b, args.dim)
    common = sorted(set(a) & set(b))
    if args.area:
        x1, z1, x2, z2 = args.area
        common = [c for c in common if x1 <= c[0] <= x2 and z1 <= c[1] <= z2]
    pairs = collections.Counter()
    per_chunk = collections.Counter()
    total = 0
    for pos in common:
        sa = {s['Y']: s for s in a[pos].get('sections', [])}
        sb = {s['Y']: s for s in b[pos].get('sections', [])}
        for y in sorted(set(sa) | set(sb)):
            ca = section_states(sa[y]) if y in sa else None
            cb = section_states(sb[y]) if y in sb else None
            ca = ca or ['minecraft:air'] * 4096
            cb = cb or ['minecraft:air'] * 4096
            total += 4096
            if ca == cb:
                continue
            for i in range(4096):
                if ca[i] != cb[i]:
                    pairs[(ca[i], cb[i])] += 1
                    per_chunk[pos] += 1
    differ = sum(pairs.values())
    print(f'chunks compared: {len(common)} (A {len(a)}, B {len(b)} full)')
    print(f'blocks compared: {total}, differ: {differ} ({100 * differ / max(total, 1):.4f} %)')
    if differ:
        print('\nmost frequent differences (A -> B):')
        for (sa_, sb_), n in pairs.most_common(args.show):
            print(f'  {n:7d}  {sa_}  ->  {sb_}')
        print('\nchunks with most differences:')
        for pos, n in per_chunk.most_common(args.show):
            print(f'  {n:7d}  chunk {pos[0]} {pos[1]}')


if __name__ == '__main__':
    sys.exit(main())

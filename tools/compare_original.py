#!/usr/bin/env python3
"""Compare a world of the original Minecraft 1.5.2 server with what the port's core predicts for its seed.

  python3 tools/compare_original.py <world dir> <seed> [vanilla] [--interior N] [--max N] [--show N]

Reads the region files (Anvil, block ids with the "Add" nibbles), takes every chunk the original has
populated and whose neighbours it has populated too (a chunk next to an unpopulated one is still missing
what that neighbour will put into it), asks tools/expect.sh for the same chunks and counts the blocks that
differ, per chunk and per pair of block ids. Also compares the world spawn in level.dat.
"vanilla" leaves the biomes of mods out of the prediction (for a world of the vanilla server).
"""
import base64, collections, gzip, os, struct, subprocess, sys, zlib

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))


def read_nbt(data):
    pos = 0

    def take(fmt):
        nonlocal pos
        size = struct.calcsize(fmt)
        value = struct.unpack_from(fmt, data, pos)
        pos += size
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
        raise ValueError(f'NBT tag {tag}')

    kind = take('>b')
    string()
    return payload(kind)


def region_chunks(path):
    with open(path, 'rb') as f:
        data = f.read()
    for i in range(1024):
        offset = struct.unpack_from('>I', data, i * 4)[0]
        if offset == 0:
            continue
        start = (offset >> 8) * 4096
        length, compression = struct.unpack_from('>IB', data, start)
        raw = data[start + 5:start + 4 + length]
        yield read_nbt(zlib.decompress(raw) if compression == 2 else gzip.decompress(raw))['Level']


def cells(level):
    """id << 4 | meta for every block of the chunk, index (x * 16 + z) * 256 + y."""
    out = [0] * (16 * 16 * 256)
    for section in level['Sections']:
        base = section['Y'] * 16
        blocks, meta, add = section['Blocks'], section['Data'], section.get('Add')
        for i in range(4096):       # index y * 256 + z * 16 + x
            bid = blocks[i]
            if add:
                bid |= (add[i >> 1] >> (4 * (i & 1)) & 15) << 8
            if bid:
                x, z, y = i & 15, i >> 4 & 15, i >> 8
                out[(x * 16 + z) * 256 + base + y] = bid << 4 | meta[i >> 1] >> (4 * (i & 1)) & 15
    return out


def main():
    args = sys.argv[1:]
    limit = int(args[args.index('--max') + 1]) if '--max' in args else 100000
    show = int(args[args.index('--show') + 1]) if '--show' in args else 15
    world, seed = args[0], args[1]
    vanilla = ['vanilla'] if 'vanilla' in args else []
    original, populated = {}, set()
    region_dir = os.path.join(world, 'region')
    for name in sorted(os.listdir(region_dir)):
        if name.endswith('.mca'):
            for level in region_chunks(os.path.join(region_dir, name)):
                key = (level['xPos'], level['zPos'])
                original[key] = level
                if level.get('TerrainPopulated'):
                    populated.add(key)
    # Population of a chunk writes into its neighbours and reads what earlier ones left there. At the rim of what
    # the original generated, chunks were populated without neighbours the port populates first, and that carries
    # on for a few chunks. Compared are chunks with populated chunks all around, --interior chunks deep (default 3).
    interior = int(args[args.index('--interior') + 1]) if '--interior' in args else 3
    reach = range(-interior, interior + 1)
    final = sorted(k for k in populated if all((k[0] + dx, k[1] + dz) in populated for dx in reach for dz in reach))[:limit]
    print(f'{len(original)} chunks in the world, {len(populated)} populated, {len(final)} compared ({interior} chunks inside the populated area)')
    spawn = read_nbt(gzip.open(os.path.join(world, 'level.dat')).read())['Data']
    predicted = subprocess.run([os.path.join(ROOT, 'tools/expect.sh'), seed, *vanilla, 'spawn'], capture_output=True, text=True).stdout.split()
    print(f"spawn: original {spawn['SpawnX']} {spawn['SpawnZ']}, predicted {' '.join(predicted[1:])}")
    if not final:
        return
    coords = [str(v) for k in final for v in k]
    out = subprocess.run([os.path.join(ROOT, 'tools/expect.sh'), seed, *vanilla, 'raw', *coords], capture_output=True, text=True)
    if out.returncode:
        raise SystemExit(out.stderr[-2000:])
    total = same = moved = 0
    # blocks the running game moves after generation: flowing water and lava (and what they harden to), falling
    # sand and gravel, snow that falls or melts
    live = {0, 1, 4, 8, 9, 10, 11, 12, 13, 49, 78}
    liquid = {8, 9, 10, 11, 49}
    pairs, per_chunk, examples = collections.Counter(), {}, []
    biome_same = biome_total = 0
    for line in out.stdout.splitlines():
        if line.startswith('G '):
            _, cx, cz, blob = line.split(' ')
            have_biomes = original[(int(cx), int(cz))].get('Biomes')
            if have_biomes:
                want_biomes = base64.b64decode(blob)
                biome_total += 256
                biome_same += sum(1 for i in range(256) if have_biomes[i] == want_biomes[i])
        if not line.startswith('R '):
            continue
        _, cx, cz, blob = line.split(' ')
        key = (int(cx), int(cz))
        raw = base64.b64decode(blob)
        want = struct.unpack(f'>{len(raw) // 2}H', raw)
        have = cells(original[key])
        bad = 0
        for i in range(len(have)):
            a, b = have[i], want[i]
            if a == b:
                continue
            # the same liquid, still or flowing, is not a difference worth counting
            if {a >> 4, b >> 4} <= {8, 9} or {a >> 4, b >> 4} <= {10, 11}:
                continue
            # leaves: bit 8 is "check for decay", set by the running game
            if a >> 4 == 18 and b >> 4 == 18 and (a ^ b) & 7 == 0:
                continue
            ia, ib = a >> 4, b >> 4
            if ia in live and ib in live and (ia in liquid or ib in liquid or {ia, ib} <= {0, 12, 13, 78} or {ia, ib} == {1, 4}):
                moved += 1
                continue
            bad += 1
            pairs[(a >> 4, b >> 4) if a >> 4 != b >> 4 else (f'{a >> 4}:{a & 15}', f'{b >> 4}:{b & 15}')] += 1
            if len(examples) < show:
                examples.append(f'  {key[0] * 16 + (i >> 12)} {i & 255} {key[1] * 16 + (i >> 8 & 15)}: original {a >> 4}:{a & 15}, port {b >> 4}:{b & 15}')
        total += len(have)
        same += len(have) - bad
        per_chunk[key] = bad
    clean = sum(1 for v in per_chunk.values() if v == 0)
    print(f'blocks: {total} compared, {total - same} differ ({100.0 * (total - same) / total:.4f} %), plus {moved} that the running '
          f'game moved (flowing liquid, fallen sand and gravel, snow); chunks without a difference: {clean} of {len(per_chunk)}')
    print(f'biomes: {biome_same} of {biome_total} columns equal')
    print('most frequent differences (original -> port):')
    for (a, b), n in pairs.most_common(show):
        print(f'  {a} -> {b}: {n}')
    print('chunks with most differences:', sorted(((v, k) for k, v in per_chunk.items() if v), reverse=True)[:show])
    print('first differences:')
    print('\n'.join(examples))


if __name__ == '__main__':
    main()

#!/usr/bin/env python3
"""Move the spawn point of a 1.5.2 world, so that the original server generates the area around it on its next start.

  python3 tools/original_spawn.py <world dir> <x> <z>

The 1.5.2 server keeps the 25x25 chunks around the spawn loaded; without players this is the only way to make it
generate a chosen place (a village, a stronghold) for tools/compare_original.py.
"""
import gzip, os, struct, sys

path = os.path.join(sys.argv[1], 'level.dat')
data = bytearray(gzip.open(path).read())
for name, value in (('SpawnX', int(sys.argv[2])), ('SpawnZ', int(sys.argv[3]))):
    tag = b'\x03' + struct.pack('>H', len(name)) + name.encode()
    at = data.index(tag) + len(tag)
    data[at:at + 4] = struct.pack('>i', value)
with gzip.open(path, 'wb') as f:
    f.write(bytes(data))
print('spawn moved to', sys.argv[2], sys.argv[3])

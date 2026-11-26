#!/usr/bin/env python3
"""Time how long the running dev server needs to generate an area of new chunks (world generation speed).

  python3 tools/bench_chunks.py CX CZ SIZE [--dim minecraft:overworld] [--port 25575] [--password hexxit]

Forceloads chunks CX..CX+SIZE-1 / CZ..CZ+SIZE-1 (in pieces of 16x16) over RCON and polls `execute if loaded` for each
chunk until all are fully loaded, then prints the seconds and ms per chunk. Use an area nobody has generated yet (a
fresh world, or new coordinates each run) and compare runs on the same seed and area. The chunks stay forceloaded.
For a profile at the same time: `jcmd <pid> JFR.start settings=profile filename=x.jfr` before, `JFR.stop` after,
then tools/jfr_top.py.
"""
import argparse, socket, sys, time

sys.path.insert(0, __import__('os').path.dirname(__file__))
from rcon import packet  # noqa: E402


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('cx', type=int)
    ap.add_argument('cz', type=int)
    ap.add_argument('size', type=int)
    ap.add_argument('--dim', default='minecraft:overworld')
    ap.add_argument('--port', type=int, default=25575)
    ap.add_argument('--password', default='hexxit')
    args = ap.parse_args()
    sock = socket.create_connection(('127.0.0.1', args.port), timeout=600)
    if packet(sock, 1, 3, args.password)[0] == -1:
        raise SystemExit('rcon: wrong password')
    rid = [100]

    def cmd(c):
        rid[0] += 1
        return packet(sock, rid[0], 2, f'execute in {args.dim} run {c}' if c.startswith('forceload') else
                      c.replace('execute ', f'execute in {args.dim} ', 1))[1]

    cx, cz, size = args.cx, args.cz, args.size
    start = time.time()
    for x0 in range(cx, cx + size, 16):
        for z0 in range(cz, cz + size, 16):
            x1 = min(x0 + 16, cx + size) - 1
            z1 = min(z0 + 16, cz + size) - 1
            cmd(f'forceload add {x0 * 16} {z0 * 16} {x1 * 16} {z1 * 16}')
    todo = {(x, z) for x in range(cx, cx + size) for z in range(cz, cz + size)}
    last = time.time()
    while todo:
        todo = {(x, z) for (x, z) in todo if 'passed' not in cmd(f'execute if loaded {x * 16} 64 {z * 16}').lower()}
        if time.time() - last > 15:
            print(f'{time.time() - start:7.1f} s  {len(todo)} chunks left', flush=True)
            last = time.time()
        if todo:
            time.sleep(0.5)
    total = time.time() - start
    print(f'DONE {size}x{size} chunks in {total:.1f} s ({total * 1000 / size / size:.0f} ms/chunk)')


if __name__ == '__main__':
    main()

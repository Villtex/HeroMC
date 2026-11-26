#!/usr/bin/env python3
"""Summarise the CPU samples of a Java Flight Recorder file (e.g. of the dev server during tools/bench_chunks.py).

  python3 tools/jfr_top.py FILE.jfr [--top 40] [--filter hexxit]     self and inclusive share per method
  python3 tools/jfr_top.py FILE.jfr --callers Long2ObjectOpenHashMap.get [--depth 2]   who calls a method

Recording: `jcmd` lists the server JVM (net.neoforged.devlaunch.Main); `jcmd <pid> JFR.start name=x settings=profile
filename=x.jfr`, later `jcmd <pid> JFR.stop name=x`. Note: work run through CompletableFuture.supplyAsync (the noise
and surface step of the chunk generator) shows without the frames of whoever started it, so a scratch chunk built by a
mod counts as vanilla terrain there.
"""
import argparse, collections, json, subprocess


def samples(path):
    out = subprocess.run(['jfr', 'print', '--json', '--stack-depth', '96', '--events', 'jdk.ExecutionSample', path],
                         capture_output=True, text=True, check=True).stdout
    for e in json.loads(out)['recording']['events']:
        frames = (e['values'].get('stackTrace') or {}).get('frames') or []
        if frames:
            yield [f'{f["method"]["type"]["name"]}.{f["method"]["name"]}' for f in frames]


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('file')
    ap.add_argument('--top', type=int, default=40)
    ap.add_argument('--filter', default='')
    ap.add_argument('--callers')
    ap.add_argument('--depth', type=int, default=2)
    args = ap.parse_args()
    self_c, incl, callers = collections.Counter(), collections.Counter(), collections.Counter()
    total = 0
    for names in samples(args.file):
        total += 1
        self_c[names[0]] += 1
        for name in set(names):
            if args.filter in name:
                incl[name] += 1
        if args.callers:
            for i, name in enumerate(names):
                if args.callers in name:
                    callers[' <- '.join(names[i:i + 1 + args.depth])] += 1
                    break
    print(f'samples: {total}')
    if args.callers:
        for name, c in callers.most_common(args.top):
            print(f'{100 * c / total:6.2f}%  {name}')
        return
    print('\n== self ==')
    for name, c in self_c.most_common(args.top):
        print(f'{100 * c / total:6.2f}%  {name}')
    print('\n== inclusive ==')
    for name, c in incl.most_common(args.top):
        print(f'{100 * c / total:6.2f}%  {name}')


if __name__ == '__main__':
    main()

#!/usr/bin/env python3
"""Zip with every mod of the pack, ready to drop into a mods folder.

    python3 tools/make_zip.py [ziel.zip]      (default: build/HeroMC-mods.zip)

Builds the pack (CI=true ./gradlew pack) unless --no-build, then packs:
  mods/         the pack's jars (build/pack/mods), JEI and the performance mods (C2ME, Lithium, ScalableLux)
  client-mods/  client only (Sodium, build/pack/client-mods)
"""
import argparse
import os
import re
import subprocess
import sys
import zipfile
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
import extra_mods  # noqa: E402

ROOT = Path(__file__).resolve().parent.parent


def jei_jar():
    version = re.search(r'^jei_version=(\S+)', (ROOT / 'gradle.properties').read_text(), re.M).group(1)
    cache = Path.home() / '.gradle' / 'caches' / 'modules-2' / 'files-2.1' / 'mezz.jei' / 'jei-26.3-neoforge' / version
    jars = [p for p in cache.glob(f'*/jei-26.3-neoforge-{version}.jar')]
    if not jars:
        sys.exit(f'JEI {version} not in the Gradle cache: run ./gradlew pack first')
    return jars[0]


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('target', nargs='?', default=str(ROOT / 'build' / 'HeroMC-mods.zip'))
    ap.add_argument('--no-build', action='store_true')
    args = ap.parse_args()
    if not args.no_build:
        subprocess.run(['./gradlew', 'pack', '-q'], cwd=ROOT, check=True, env={**os.environ, 'CI': 'true'})
    mods = sorted((ROOT / 'build' / 'pack' / 'mods').glob('*.jar'))
    mods += [jei_jar()] + extra_mods.download(extra_mods.PERF_MODS)
    client = sorted((ROOT / 'build' / 'pack' / 'client-mods').glob('*.jar'))
    target = Path(args.target)
    target.parent.mkdir(parents=True, exist_ok=True)
    with zipfile.ZipFile(target, 'w', zipfile.ZIP_DEFLATED) as z:
        for jar in mods:
            z.write(jar, f'mods/{jar.name}')
        for jar in client:
            z.write(jar, f'client-mods/{jar.name}')
    print(f'{target}: {len(mods)} mods, {len(client)} client-only')


if __name__ == '__main__':
    main()

#!/usr/bin/env python3
"""Fetch the sources of Tinkers' Construct 3.12.1 and Mantle (Forge 1.20.1) for Teil 27 (mod tconstruct3).

    python3 tools/reference_tc3.py [--out DIR]      (default: .reference/tc3, not committed)

Shallow git clones of the release tags, one folder each (no history):
  tconstruct   SlimeKnights/TinkersConstruct  v3.12.1.231   the original being ported (code, data, assets)
  mantle       SlimeKnights/Mantle            v1.11.117     the library version Tinkers 3.12.1 builds on
  mantle-1.21  SlimeKnights/Mantle            branch 1.21   NeoForge 1.21.1 port of Mantle, a help for the rewrite

The build of mods/mantle and mods/tconstruct3 takes textures, models, sounds and books from the same folders
(clones them itself when missing, see buildSrc ImportTagAssets). Code and textures are MIT (README of Tinkers).
Uses git (the GitHub archive URLs may be blocked behind proxies; git over https works).
"""
import argparse
import os
import subprocess
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
SOURCES = [
    ("tconstruct", "https://github.com/SlimeKnights/TinkersConstruct.git", "v3.12.1.231"),
    ("mantle", "https://github.com/SlimeKnights/Mantle.git", "v1.11.117"),
    ("mantle-1.21", "https://github.com/SlimeKnights/Mantle.git", "1.21"),
]


def fetch(out, name, url, ref):
    dst = os.path.join(out, name)
    marker = os.path.join(dst, ".hexxit-ref")
    if os.path.exists(marker) and open(marker).read().strip() == ref:
        print(f"{name}: {ref} already there")
        return
    if os.path.exists(dst):
        subprocess.run(["rm", "-rf", dst], check=True)
    os.makedirs(out, exist_ok=True)
    print(f"{name}: cloning {url} @ {ref}")
    for attempt in range(4):
        r = subprocess.run(["git", "-c", "advice.detachedHead=false", "clone", "-q", "--depth", "1",
                            "--branch", ref, url, dst])
        if r.returncode == 0:
            break
        subprocess.run(["rm", "-rf", dst])
    else:
        sys.exit(f"clone failed: {url} @ {ref}")
    with open(marker, "w") as f:
        f.write(ref + "\n")


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--out", default=os.path.join(ROOT, ".reference", "tc3"))
    args = ap.parse_args()
    for name, url, ref in SOURCES:
        fetch(args.out, name, url, ref)


if __name__ == "__main__":
    main()

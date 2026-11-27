#!/usr/bin/env python3
"""Load condition "only in Hexxit Legacy" for the data of Tinkers' Construct 1.4.3 (Teil 27a).

Hexxit Integrated gets Tinkers' Construct 3.12.1 (mod tconstruct3) instead; both are installed. Recipes and
entity loot of 1.4.3 carry {"type": "worldgen152:world_type", "legacy": true}, so they load only in Legacy worlds.
Not conditioned: block loot (blocks placed in older Integrated worlds still drop themselves), tags (no conditions
in tags), villager trades (registry entries the profession needs; switched off in code), and the Integrated
features of 26b/26c (biome modifiers, placed features; they go away in Teil 27k).

gen.py calls with_condition() for every file it writes; run this script to apply it to the committed files
(idempotent):  python3 tools/legacy_condition.py
"""
import json
import os

CONDITION = {'type': 'worldgen152:world_type', 'legacy': True}
GATED = ('data/tconstruct/recipe/', 'data/tconstruct/loot_table/entities/', 'data/tconstruct/advancement/',
         'data/tconstruct/neoforge/loot_modifiers/')


def gated(path):
    return path.replace('\\', '/').startswith(GATED)


def with_condition(path, obj):
    """obj with the Legacy condition first (dict order: conditions lead), when path is gated data."""
    if not gated(path) or not isinstance(obj, dict):
        return obj
    conds = [c for c in obj.get('neoforge:conditions', []) if c != CONDITION]
    rest = {k: v for k, v in obj.items() if k != 'neoforge:conditions'}
    return {'neoforge:conditions': [CONDITION] + conds, **rest}


def main():
    res = os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))), 'src/main/resources')
    changed = 0
    for top, _, files in os.walk(os.path.join(res, 'data')):
        for name in files:
            full = os.path.join(top, name)
            rel = os.path.relpath(full, res)
            if not name.endswith('.json') or not gated(rel):
                continue
            with open(full) as f:
                obj = json.load(f)
            new = with_condition(rel, obj)
            if new != obj or list(new) != list(obj):
                with open(full, 'w') as f:
                    json.dump(new, f, indent=2, ensure_ascii=False)
                    f.write('\n')
                changed += 1
    print(f'{changed} files got the Hexxit Legacy condition')


if __name__ == '__main__':
    main()

#!/usr/bin/env python3
"""Fremde NeoForge-26.3-Mods für den Kompatibilitätstest (Teil 26f) holen.

Lädt die unten festgelegten Versionen von Modrinth nach run/mods-extra/ (nicht im Repo).
Zum Testen: python3 tools/extra_mods.py --install   (kopiert sie nach run/server/mods)
Wieder entfernen: python3 tools/extra_mods.py --uninstall
Nur die Leistungs-Mods (C2ME, Lithium, ScalableLux): --set perf; beide Gruppen: --set all.
"""
import argparse
import hashlib
import json
import shutil
import sys
import urllib.parse
import urllib.request
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
EXTRA = ROOT / 'run' / 'mods-extra'
SERVER_MODS = ROOT / 'run' / 'server' / 'mods'

# (Modrinth-Slug, Versionsnummer, wofür)
MODS = [
    ('biomes-o-plenty', '26.3.0.0.17', 'Biome über TerraBlender'),
    ('glitchcore', '26.3.0.0.5', 'Bibliothek für Biomes O\' Plenty'),
    ('towns-and-towers', '1.13.13', 'Strukturen (Dörfer, Außenposten, Schiffe)'),
    ('cristel-lib', 'neoforge-26.3-3.1.13', 'Bibliothek für Towns and Towers'),
    ('assorted-ores', '2.0.0+neoforge', 'Erze und Metalle über c:-Tags (Zinn, Silber, Blei, ...)'),
    ('assorted-lib', '5.0.3+neoforge', 'Bibliothek für Assorted Ores'),
    ('pipez', 'neoforge-1.2.33+26.3', 'Rohre für Items und Flüssigkeiten'),
    ('immersive-ores', '26.3-0.4', 'Werkzeuge, Waffen, Rüstungen (und Erze)'),
    ('jade', '26.3.5+neoforge', 'Anzeige, was man ansieht'),
]
# Leistungs-Mods: Chunks parallel erzeugen und laden, Spiel-Logik und Licht schneller
PERF_MODS = [
    ('c2me-neoforge', '0.4.2-alpha.0.110+26.3', 'Chunk-Erzeugung/-Laden parallel (C2ME)'),
    ('lithium', 'mc26.3-0.26.4-neoforge', 'Spiel-Logik schneller'),
    ('scalablelux', '0.3.0-alpha.0.17+26.3', 'Lichtberechnung parallel (Starlight-Nachfolger)'),
]
SETS = {'compat': MODS, 'perf': PERF_MODS, 'all': MODS + PERF_MODS}
API = 'https://api.modrinth.com/v2'


def get(url):
    req = urllib.request.Request(url, headers={'User-Agent': 'heromc-compat-test'})
    with urllib.request.urlopen(req) as r:
        return r.read()


def pick(slug, version):
    q = urllib.parse.urlencode({'loaders': '["neoforge"]', 'game_versions': '["26.3"]'})
    versions = json.loads(get(f'{API}/project/{slug}/version?{q}'))
    if version:
        versions = [v for v in versions if v['version_number'] == version]
    if not versions:
        sys.exit(f'{slug} {version or ""}: keine NeoForge-26.3-Version gefunden')
    v = versions[0]
    return v['version_number'], next(f for f in v['files'] if f['primary'])


def download(mods):
    EXTRA.mkdir(parents=True, exist_ok=True)
    jars = []
    for slug, version, why in mods:
        number, f = pick(slug, version)
        target = EXTRA / f['filename']
        jars.append(target)
        if target.exists() and hashlib.sha1(target.read_bytes()).hexdigest() == f['hashes']['sha1']:
            print(f'ok      {slug} {number}')
            continue
        data = get(f['url'])
        if hashlib.sha1(data).hexdigest() != f['hashes']['sha1']:
            sys.exit(f'{slug}: Prüfsumme stimmt nicht')
        target.write_bytes(data)
        print(f'geladen {slug} {number} ({why})')
    return jars


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('--install', action='store_true', help='auch nach run/server/mods kopieren')
    ap.add_argument('--uninstall', action='store_true', help='die Jars aus run/server/mods entfernen')
    ap.add_argument('--set', choices=sorted(SETS), default='compat',
                    help='compat = Teil-26f-Auswahl (Standard), perf = C2ME/Lithium/ScalableLux, all = beide')
    a = ap.parse_args()
    if a.uninstall:
        for jar in EXTRA.glob('*.jar'):
            (SERVER_MODS / jar.name).unlink(missing_ok=True)
        print('entfernt')
        return
    jars = download(SETS[a.set])
    if a.install:
        SERVER_MODS.mkdir(parents=True, exist_ok=True)
        for jar in jars:
            shutil.copy2(jar, SERVER_MODS / jar.name)
        print(f'nach {SERVER_MODS} kopiert')


if __name__ == '__main__':
    main()

---
name: tinkers3-263
description: Beim Arbeiten an Tinkers' Construct 3.12.1 und Mantle (mods/tconstruct3, mods/mantle, Teile 27a–27k) benutzen – Mod-ID tconstruct3 neben 1.4.3 (tconstruct), Quellen vom Tag holen, Daten-Konverter convert_data.py und sein Bericht, Daten ordnerweise einschalten (data-enabled.txt), Welttyp-Regel (1.4.3 nur Hexxit Legacy, 3.12.1 nur Integrated) in Daten und Code über worldgen152:world_type und HexxitWorldType, Assets aus dem Tag mit umbenanntem Namensraum.
---

# Tinkers' Construct 3.12.1 + Mantle auf 26.3 (Teil 27)

## Zwei Versionen, ein Spiel

| | 1.4.3 (Teil 25) | 3.12.1 (Teil 27) |
|---|---|---|
| Mod-ID | `tconstruct` (`mods/tconstruct`) | `tconstruct3` (`mods/tconstruct3`), Bibliothek `mantle` (`mods/mantle`) |
| gilt in | Hexxit Legacy | Hexxit Integrated (jeder andere Welttyp) |
| Paket | `tconstruct` | `slimeknights.tconstruct`, `slimeknights.mantle` (Originalnamen behalten) |

Beide sind immer registriert. **Daten:** Ladebedingung `{"type":"worldgen152:world_type","legacy":true|false}` als
erstes Element von `"neoforge:conditions"`. **Code:** `hexxit.worldgen152.HexxitWorldType`:
- `isLegacy(MinecraftServer)`, `isLegacy(Level)` (Client-Level: was der Server geschickt hat)
- `clientLegacy()` (Paket `worldgen152:world_type` beim Betreten; vorher und nach dem Verlassen `true`)
- `onClientChange(Consumer<Boolean>)` für Client-Caches (Kreativ-Tabs, JEI), Teil 27k
Ereignis-Handler, die ohne Daten wirken, prüfen zuerst den Welttyp und tun in der falschen Welt nichts.

1.4.3-Seite: `mods/tconstruct/tools/legacy_condition.py` setzt `legacy:true` an Rezepte, Entity-Beute,
Fortschritte, Beute-Modifier (`gen.py` ruft `with_condition` in `write()`). Block-Beute, Tags, Dorfbewohner-Handel,
Biom-Modifier/Features (Integrated-Features aus 26b/26c) bleiben ohne Bedingung; die Features fallen erst in 27k weg.
Achtung: `gen.py` ist hinter den Handänderungen von 26b/26e zurück – **nicht neu laufen lassen** (löscht c:-Tags,
Integrated-Features); stattdessen `legacy_condition.py` nach Handänderungen ausführen.

## Quellen und Assets

```
python3 tools/reference_tc3.py     # .reference/tc3/{tconstruct,mantle,mantle-1.21} (git, flach, nicht im Repo)
```
- `tconstruct` = Tag `v3.12.1.231`, `mantle` = `v1.11.117` (maßgeblich), `mantle-1.21` = Zweig `1.21` (NeoForge 1.21.1,
  Datenkomponenten; nur als Hilfe beim Umbau).
- Assets holt der Build (`buildSrc/.../ImportTagAssets.groovy`, Einstellungen `tag_assets_*` in `gradle.properties`):
  `src/{main,generated}/resources/assets` → `build/generated/tag-assets`, dabei `assets/tconstruct/` → `assets/tconstruct3/`,
  `tconstruct:` → `tconstruct3:` in Textdateien, in Sprachdateien die Schlüssel (`item.tconstruct.x` → `item.tconstruct3.x`).
  Im Code also immer `TConstruct.MOD_ID`/`getResource`, nie `"tconstruct"` als Text.

## Daten: Konverter

```
python3 mods/tconstruct3/tools/convert_data.py
```
schreibt `mods/tconstruct3/src/converted/data/**` und `mods/mantle/src/converted/data/**` (eingecheckt, komplett neu
geschrieben) und den Bericht `mods/tconstruct3/tools/convert_report.md`. Regeln stehen oben im Skript; das Wichtigste:
- Ordner 26.3 (`recipe`, `loot_table`, `advancement`, `structure`, `tags/item` …), `data/forge/tags` → `data/c/tags`,
  `forge:`-Tags → `c:` (Tabelle `TAG_RENAME`), `forge:`-Typen → `neoforge:`/`minecraft:` (`FORGE_IDS`, auch Attribute).
- Zutaten: `{"item"}`→`"id"`, `{"tag"}`→`"#tag"`, eigene Typen → `{"neoforge:ingredient_type": "tconstruct3:…", …}`
  (nur unter den Schlüsseln `ING_KEYS`). Flüssigkeits-/Mengen-Objekte (`fluid`, `amount`, `amount_needed`) bleiben in
  Mantle-Form → die Mantle-Lader (27b) lesen sie so.
- Ausgaben `{"item","count","nbt"}` → `{"id","count","components"}`; Tag-Ausgaben `{"tag",…}` bleiben (Mantle `ItemOutput`).
- NBT → Komponenten über `nbt_to_components` (vorläufige Namen `tconstruct3:materials`, `:display`, `:tank`,
  `:material`; **27d legt die echten fest und passt die Tabelle an**), Unbekanntes → `minecraft:custom_data` + Bericht.
- Beute/Fortschritte/Prädikate im 26.3-Format (`condition`/`modifier`/`type`, `minecraft:entity_type` …); eigene
  Item-Prädikate (`tconstruct:tool_stack`) → `"predicates": {"tconstruct3:tool_stack": …}` (als Item-Sub-Prädikat
  registrieren, 27d/27e).
- Welttyp-Bedingung `legacy:false` an allem in `gated()`: Rezepte, Nicht-Block-Beute, Fortschritte, Beute-Modifier,
  Biom-Modifier, Struktur-Sets, `tinkering/**`, `mantle/**`. `forge:conditional` → eine Datei je Alternative (`_alt1` …).
- Strukturvorlagen `.nbt` werden gelesen, Namen umbenannt, neu geschrieben.
Regel ändern → Skript laufen lassen → `git diff --stat` und Bericht ansehen. Nicht von Hand in `src/converted` ändern.

## Daten einschalten

`mods/<mod>/data-enabled.txt`: Pfad-Präfixe (z. B. `data/tconstruct3/tinkering/materials/`), nur diese kommen ins Jar
(Build-Task `convertedData`). Ein Teil schaltet ein, was sein Code liest **und** wofür alle Registry-Einträge
existieren – sonst bricht der Serverstart in Integrated ab (unbekannte Items in Rezepten/Tags). Tags erst einschalten,
wenn alle genannten Items/Blöcke registriert sind (Tags kennen keine Bedingung).

## Prüfen

- Legacy/Integrated am Testserver: Skill `integrated-263` (`level-type=hexxit\:legacy`, Welt löschen).
- Rezept da/nicht da: Crafter + RCON (Skill `server-tests-263`), z. B. 1.4.3 `tconstruct:alubrass_ingot` aus 9
  `tconstruct:alubrass_nugget`: in Legacy ein Barren, in Integrated nichts.
- Log: `grep -c "worldgen152:world_type\|Couldn't parse" run/server/logs/latest.log` – keine Fehler der Bedingung.

---
name: compat-263
description: Beim Anbinden eines portierten Hexxit-Mods an fremde 26.3-Mods benutzen (Teil 26e/26f und jeder neue Port) – Item-/Fluid-Capabilities für Kisten, Maschinen und Tanks (RegisterCapabilitiesEvent, Transfer-API mit Transaktionen), c:-/minecraft:-Tags für Erze, Metalle, Kisten, Werkzeuge und Bosse, Rezepte über Tags statt OreDictionary-Wörtern, Daten nur in Hexxit Legacy (Ladebedingung worldgen152:world_type), Welttyp im Code erkennen, Prüfung mit /hexxitcompat.
---

# Schnittstellen für fremde Mods (26.3)

## Capabilities (NeoForge 26.3 Transfer-API)

- Typen: `Capabilities.Item.BLOCK` / `Capabilities.Fluid.BLOCK` liefern `ResourceHandler<ItemResource|FluidResource>`
  (Paket `net.neoforged.neoforge.transfer`), Änderungen nur in einer `TransactionContext`
  (`try (var tx = Transaction.openRoot()) { h.insert(res, n, tx); tx.commit(); }`).
- **Items:** Block-Entity als `Container` (bzw. `WorldlyContainer` für ISidedInventory) schreiben, dann reicht
  `VanillaContainerWrapper.of(container)` / `new WorldlyContainerWrapper(worldly, side)`. Vorlage: `ItemCapabilities`
  in `mods/{tconstruct,bibliocraft,betterstorage,secretrooms,harkenscythe}` – registriert für **alle** Block-Entity-Typen
  des Namensraums, Anmeldung im Mod-Konstruktor:
  `modBus.addListener(RegisterCapabilitiesEvent.class, ItemCapabilities::register);`
  Neuer Mod mit Inventar-Blöcken: Datei kopieren, Paket und Hauptklasse anpassen. Sperren (Schloss) über
  `canPlaceItem`/`getContainerSize` des Containers, nicht im Wrapper.
- **Flüssigkeiten ohne Transaktionen** (alte fill/drain-Logik mit Simulieren): `tconstruct.smeltery.LiquidHandler` als
  Vorlage – `SnapshotJournal` mit vorgemerkten Mengen, die erst in `onRootCommit` ausgeführt werden; beim Vormerken gegen
  „schon vorgemerkt + neu“ simulieren.
- Eimer: Klasse genau `BucketItem` → NeoForge meldet die Fluid-Item-Capability selbst an.
- Pack-Konfiguration beachten: BetterStorage-Kisten-Stapel bieten nichts (`enableCrateInventoryInterface=false`).

## Tags

- `python3 tools/compat_tags.py` (nach `CI=true ./gradlew pack`, liest die Ids aus `build/pack/mods`) schreibt in die
  Tag-Dateien des jeweiligen Mods: `c:ingots|nuggets|storage_blocks|raw_materials|ores|ores_in_ground|ore_rates|gems|dusts`,
  `c:chests`, `minecraft:swords|axes|pickaxes|shovels|hoes`, `c:tools/*`, `c:molten_*`, `c:bosses`; danach die Rezepte.
  `--check` zeigt nur. **Nach jedem `tools/gen.py` eines Mods erneut laufen lassen** (die Generatoren schreiben die
  Tag-/Rezeptdateien neu). Neue Metalle, Erze, Werkzeugnamen: Tabellen oben im Skript ergänzen.
- Namen wie NeoForge `Tags` (`net.neoforged.neoforge.common.Tags`): `c:tools/melee_weapon`, `c:tools/mining_tool`,
  `c:tools/shear`, `c:tools/bow`, `c:tools/shield`; Kunstnamen für Legierungen: `aluminum_brass`, `knightmetal`.
- Abbaustufe: `minecraft:needs_{stone,iron,diamond}_tool` nach dem `setBlockHarvestLevel` des Originals (nur wo das
  Material ein Werkzeug verlangt).
- Entrinden/Pfade/Umgraben: keine ItemAbilities mehr in 26.3, sondern Komponente `BLOCK_TRANSFORMER` – kommt von
  `Item.Properties.axe/shovel/hoe`. Eigene Werkzeug-Klassen ohne diese Properties bekommen sie nicht.

## Rezepte über Tags

- OreDictionary-Wort des Originals (`ingotTin`, `blockSteel`) → `#c:ingots/tin`, `#c:storage_blocks/steel` in Schlüssel,
  `ingredients` und `ingredient`; nie im Ergebnis, nicht in Listen von Alternativen. Eisen/Gold/Kupfer als Items lassen
  (sonst Konflikte mit Vanilla-Nugget-/Schmelzrezepten). Ein Rezept mit einem Tag, das evtl. niemand füllt, braucht
  `neoforge:not` + `neoforge:tag_empty` (Skill `server-tests-263`).
- Tinkers-Schmelzerei: `SmelteryRecipes.meltTag(...)` – neue Metalle dort eintragen; Datapacks fügen Items über die
  `c:`-Tags hinzu.

## Nur in Hexxit Legacy

- Daten: `"neoforge:conditions": [{"type": "worldgen152:world_type", "legacy": true}]`. Ersetzt eine Datei eine
  Vanilla-Id, verschwindet bei falscher Bedingung auch das Vanilla-Rezept: dann das Vanilla-Rezept unter eigener Id mit
  `{"type":"neoforge:not","value":{...legacy...}}` dazulegen (Vorlage `projectzulu/tools/gen_potions.py`).
- Code ohne Abhängigkeit zu worldgen152: `server.overworld().getChunkSource().getGenerator().getClass().getName()
  .equals("hexxit.worldgen152.OldChunkGenerator")` ab `ServerStartedEvent`; der Client erfährt es per Payload
  (Vorlage `combat152`, `ModePayload`).
- Registry-Daten (`data/minecraft/enchantment` …) laden vor den Dimensionen: dort keine Welttyp-Bedingung, Unterschied
  im Code abbilden.

## Prüfen am Server

`/hexxitcompat item <x y z> "<item>"` (+5, −2, abgebrochenes −1), `fluid <x y z> "<fluid>"`, `melt "<item>"`,
`tag "<item>" "<tag>"`, `recipe "<id>"`, `mine <x y z> "<werkzeug>"` (richtiges Werkzeug, Tempo, Drops) per `tools/rcon.py` (Ids in Anführungszeichen). Ausgabe auch im Log
(`hexxitcompat`). Kampf-Modus steht beim Start im Log: `Melee combat: 1.5.2 (Hexxit Legacy)`.

## Mit echten fremden Mods testen (Teil 26f)

- `python3 tools/extra_mods.py --install` lädt die Testauswahl (BOP, Towns and Towers, Assorted Ores, Pipez, Immersive
  Ores, Jade; feste Versionen) nach `run/mods-extra/` und kopiert sie nach `run/server/mods`; `--uninstall` nimmt sie
  wieder heraus. Neue Mods/Versionen: Modrinth-API `project/<slug>/version?loaders=["neoforge"]&game_versions=["26.3"]`.
- Pipez-Rohr ohne Schraubenschlüssel auf „Herausziehen“ stellen:
  `setblock x y z pipez:item_pipe[west=true,east=true,has_data=true]`, dann
  `data merge block x y z {ExtractingSides:[0b,0b,0b,0b,1b,0b]}` (Reihenfolge Unten, Oben, Nord, Süd, West, Ost).
  `fluid_pipe` genauso. Ohne `has_data=true` gibt es keine Block-Entity.
- Legacy-Prüfung (`verify_blocks.py`) **ohne** fremde Mods: Deren Erz-Features laufen in Legacy weiter
  (`OldChunkGenerator.legacySettings` filtert nur `minecraft:`) und zählen als Abweichungen.
- Der ERROR `NeoForgeDevDistCleaner … LevelRenderer` beim Laden fremder Mixins gibt es nur in der Dev-Umgebung.

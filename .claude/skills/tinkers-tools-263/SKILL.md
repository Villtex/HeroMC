---
name: tinkers-tools-263
description: Beim Weiterbauen von Tinkers' Construct (mods/tconstruct, Teile 25b–25f) benutzen – wie Werkzeuge, Teile, Materialien, Muster und Modifikatoren im Port abgebildet sind (Komponente tconstruct:tool statt InfiTool-NBT, ein Item je Teil und Material, alte IDs), wo neue Fähigkeiten, Modifikator-Effekte, Gussformen und Werkzeug-Modelle eingehängt werden und wie man es ohne Spieler prüft.
---

# Tinkers' Construct im Port (Stand Teil 25f, Port vollständig)

Code: `mods/tconstruct/src/main/java/tconstruct/`, Daten/Assets aus `mods/tconstruct/tools/gen.py`
(`python3 mods/tconstruct/tools/gen.py` überschreibt `src/main/resources/assets|data` und `original-assets.txt`).
Original: `python3 tools/reference.py --only tconstruct,z-hexxit-configs`, Paket `mods.tinker.tconstruct`.
Offenes je Unterteil: `mods/tconstruct/TODO.md`.

## Abbildung 1.5.2 → 26.3

| 1.5.2 | Port |
|---|---|
| `TConstructRegistry.addToolMaterial` (id 0–17) | `tools/TMaterial` (Enum, `id` = alte Material-ID, auch Bogen/Pfeil-Werte) |
| `ToolPart` / `ToolShard` mit Damage = Material | ein Item je Teil und Material: `tconstruct:<material>_<teil>` (`PartType`, `ToolPartItem`), `TItems.part(type, mat)` |
| `Bowstring`, `Fletching` | `bowstring`, `<feather|leaf|slime|blue_slime>_fletching` (`CustomPartItem`, `CustomMaterials`) |
| `woodPattern` / `metalPattern` Damage n | `<name>_pattern` / `<name>_cast` (`PatternItem.meta` = altes Damage, `TItems.PATTERN_NAMES`) |
| `materials` Damage 0–33 | eigene Items (`TItems.MATERIAL_NAMES`), 19 = `minecraft:iron_nugget` |
| `ToolCore`-Unterklassen | ein `ToolCoreItem` je `ToolType` (Werte, Teile, Kategorien, Effektiv-Tags) |
| NBT `InfiTool` | Komponente `tconstruct:tool` = `ToolData`; bearbeiten über `InfiTool.of(stack)` … `t.save(stack)` |
| `tags.getInteger("Redstone")`, `getIntArray`, `getFloat` | `t.getInt/getArray/getFloat/getBool/has(...)` mit denselben Schlüsseln wie 1.5.2 |
| `Tooltip#` / `ModifierTip#`, `Effect1..6` | `t.tips` (String-Paare), `t.effects` |
| `ToolBuilder`, `PatternBuilder`, `ToolMod` | `tools/ToolBuilder`, `items/PatternBuilder`, `modifiers/Modifiers` (alle in Original-Reihenfolge) |
| `AbilityHelper.damageTool` | `ToolCoreItem.damageTool(stack, n, entity)`; kaputt = `t.broken`, keine Vanilla-Haltbarkeit |
| Tische (`ToolStationBlock` Meta 0–13, Forge, CraftingStation, CraftingSlab) | `TBlocks` (ein Block je Holz/Metall), BEs in `blocks/`, Menüs in `menu/`, Screens in `client/` |

Alte IDs: `TItems.OLD_IDS` (Item-ID aus `TinkersWorkshop.txt` + 256, Damage) und `TBlocks.registerOldIds`
(`OldBlockStates` 1326/1341/1346/1347). Neue Items mit alter ID dort eintragen.

## Werte und Regeln

- Pack-Konfiguration ist fest eingebaut: Metallteile **nicht** aus Holzmustern (`TMaterial.nonMetal()`), Modifikatoren
  heben die Abbaustufe, Schablonentische craftbar.
- Abbau: `ToolCoreItem.getDestroySpeed/isCorrectToolForDrops` mit den Block-Tags `tconstruct:effective/<name>` (aus
  gen.py) und der Stufe aus `needs_*_tool` (+ `tconstruct:needs_cobalt_tool` = 4, `needs_manyullyn_tool` = 5 für 25d).
- Schaden: `Attack` als `ATTACK_DAMAGE`-Modifikator (`getDefaultAttributeModifiers`, Wert − 1), damit combat152 die
  1.5.2-Kampfregeln anwendet; `damageModifier` (Hackmesser, Rapier, Sense) in `tools/TEvents`.
- Blocken: `ToolType.blocks()` → `Combat152.BLOCKING`.

## Neues einhängen

- Fähigkeit beim Abbau: `tools/ToolAbilities.onBreak` (`BreakBlockEvent`, Priorität HIGH, vor TreeCapitator) nach
  `tool.type` verzweigen. Weitere Blöcke eines Bereichs mit `areaBreak(player, pos)` abbauen (ruft
  `player.gameMode.destroyBlock` mit gesetztem `AREA`-Flag: dann laufen nur die Modifikatoren, keine Rekursion; Drops,
  Abnutzung über `mineBlock`, Schutz bleiben Vanilla). `true` zurückgeben = normalen Abbau abbrechen.
- Modifikator vor dem Abbau (`TActiveOmniMod.beforeBlockBreak`): `ToolAbilities.beforeBlockBreak`; beim Treffer:
  `ToolCombat.afterHit` (aus `hurtEnemy`), Zusatzschaden `ToolCombat.modDamage` (über `TEvents`, LivingIncomingDamage).
  Ganzer 1.5.2-Treffer ohne Spielerschlag (Flächen, Geschosse): `ToolCombat.attack(stack, angreifer, ziel, tool, basis)`.
- Rechtsklick/Laden/Werfen: `ToolCoreItem.use/releaseUsing/finishUsingItem/getUseAnimation/getUseDuration` je `type`.
  Gehtempo beim Benutzen = Komponente `USE_EFFECTS` (`TItems.useSpeed`, 1.5.2: 0,2 × Faktor des Werkzeugs).
- Meißel-Stufen: `recipe/Detailing.add(() -> eingabeBlock, () -> ausgabeZustand)`; gilt für Welt und Rezept.
- Geschosse: `entity/TinkerArrow`, `entity/ThrownDagger` (beide `AbstractArrow`, Stack per `SynchedEntityData` für den
  Renderer), Zielen mit 1.5.2-Streuung `TinkerArrow.aim`.
- Werkzeug-Modell: gen.py `TOOLS` (Suffixe aus `getIconSuffix`, kaputter Teil, Effekt-Suffix) erzeugt je Werkzeug ein
  `minecraft:composite` aus `minecraft:select` mit Eigenschaft `tconstruct:tool_part` (`part` = handle/head/accessory/
  extra/effect1..6/arrow; Wert = Material-/Effekt-ID, `_broken`, `_1.._3` beim gespannten Bogen,
  `client/ToolPartProperty`). Nur vorhandene Texturen werden Fälle (gen.py braucht `.reference/tconstruct`).
- JEI: `compat/TConstructJeiPlugin` + `TinkerCategory` (Zeile aus Eingabe-Listen und Ausgaben, fokus-verknüpft).

## Schmelzerei (25c)

- Flüssigkeiten: `smeltery/TFluids` (Index = alte Metadaten, `TFluids.fluid(i)`, `TFluids.of(fluid)`), Aussehen in
  `client/TSmelteryClient` (`RegisterFluidModelsEvent`, Texturen `block/liquid_<tex>` + `_flow` mit `.mcmeta` aus den `.txt`).
- Neue Rezepte in `smeltery/SmelteryRecipes.build()` (Reihenfolge = Original): `melt/meltBlock/meltTag` (Tag `c:...` = Erz-Wörterbuch,
  Render-Block + Größe für die Ansicht im Inneren), `alloy`, `table`/`basin` bzw. `new Casting(...)` mit Prädikat für die Gussform.
  JEI liest die Tabellen direkt (`compat/SmelteryJei`).
- Blöcke tauschen Flüssigkeit über `smeltery.LiquidContainer` (fill/drain mit Simulieren wie `ITankContainer`), keine NeoForge-Capability.
- Wandblöcke: alles mit `ServantBlockEntity` (Unterklassen von `SearedBrickBlock`, `SearedTankBlock`); eigener Boden nur `SearedBrickBlock`
  ohne Kern/Abfluss. Name-Kollision beachten: Flüssigkeit und Block teilen sich das Register (`liquid_seared_stone`).
- Datenteil in `tools/gen_smeltery.py` (läuft in `gen.py`, liest Namen aus `en_US.xml`/`de_DE.xml` des Originals).
- Testen per RCON: Struktur mit `setblock` bauen, Tank `data merge block … {Liquid:{id:"minecraft:lava",amount:4000}}`, Kern
  `{Items:[…]}`, `tick sprint 3000`, `data get block … Liquids`; Hahn mit Redstone-Block an/aus (schaltet jedes Mal um).

## Welt (25d)

- Kern ohne Spielklassen: `tconstruct/gen` (`TCWorldGen`: Erze, Büsche, Nether, Oberflächenerze am SAND-Event, Beute;
  `SlimeIslandGen` inkl. `slimeTree` für Insel und Setzling; Dorf-Bauteile). In `tools/expect.sh`/`Expect.java` eingetragen.
- Spielseite `TWorld` (Büsche je Erzsorte mit `age` 0–3, Beeren, Schleimblöcke, Flüssigkeit `liquid_blue_slime`, Beruf
  `tconstruct:tinker`, alte IDs 1317/1318/1345/1350/2384–2387), Blöcke in `tconstruct.world`, Mob `entity/BlueSlime`
  (Unterklasse von `Slime`, Größe/Leben/Split überschrieben), Daten aus `tools/gen_world.py`.
- Prüfen: Chunks mit Inseln findet ein Raster (`expect.sh 1234 <48×48 Chunks>` und nach IDs 2385/1345/1319 filtern),
  Dörfer mit Werkzeugschmiede `expect.sh 1234 structures 220`, Nether-Erze `expect.sh 1234 nether …`.

## Spieler (25e)

- `TPlayer` (Attachment `tconstruct:player` = `player/PlayerData`: `armor` 7, `knapsack` 27, Flags; immer über `TPlayer.data(player)`
  holen, das hängt die Neuberechnung der Leben an), Ereignisse in `player/PlayerEvents` (Tod, Respawn, Drops, Spinnen-Reiter),
  Items in `player/PlayerItems`, Daten aus `tools/gen_player.py`.
- Handbuch: `client/manual` (`ManualScreen` liest `assets/tconstruct/manuals/*.xml`, Seitentypen in `ManualPages`, Bilder/Rezepte in
  `ManualIcons` per Registry-Name – neue Blöcke von 25f bekommen dort automatisch ihr Bild, wenn der Name passt).
- FakePlayer stirbt nicht (`die` löst keine Drops aus): im Testbefehl `LivingDropsEvent` selbst posten.

## Deko, Fallen, Maschinen (25f)

- Registrierung `TDeco` (Blöcke, BE-Typen, Menüs, alte IDs, Meißel-Stufen), Daten `tools/gen_deco.py`. Tag-Dateien dort nur über
  `tag_add(...)` ergänzen: ein `write` überschreibt die Tags der früheren gen-Skripte (mineable, slabs).
- Beute-JSON im 26.3-Format: `modifier`/`type` und `condition` (nicht `functions`/`conditions`), sonst wird es still ignoriert.
- Tarnblock (Zugbrücke, Landmine): BE erbt `machine/CamoBlockEntity` (Slot `camoSlot()`, Licht über Zustand `TDeco.LIGHT`,
  ModelData `TDeco.CAMO`), Modell `client/deco/CamoModel` (FULL oder BOX = auf die Form zugeschnitten), Tönung `CamoTint`.
- Meißel aus Items (Barren, Edelsteine): `Detailing.addItem(() -> item, () -> zustand)`, nur im Raster; JEI zeigt beides.
- Verbundenes Glas: `ConnectedGlassBlock` (6 Nachbar-Properties) + Mehrteil-Blockstate aus `gen_deco.connected_glass`.

## Prüfen ohne Spieler

Temporärer Befehl (nie committen), Block-Entities direkt füllen: `PartBuilderBlockEntity.setItem/removeItem` (Logik
wie `PartCrafterLogic`), `ToolStationBlockEntity.setItem(1..3)` → Ergebnis in Slot 0, `InfiTool.of(...)` ausgeben;
Abbau mit `stack.getDestroySpeed(state)` / `isCorrectToolForDrops`. Vorlage siehe Teil 25a in `docs/PLAN-legacy.md`.
- FakePlayer: `isSwinging()` bleibt true (er tickt nicht) → für Moos & Co. einen Mob als Träger nehmen. Mit
  `fp.gameMode.destroyBlock(pos)` laufen die Abbau-Fähigkeiten echt (Blickrichtung mit `fp.snapTo(..., yaw, pitch)`).
- Neue Entities (Drops, Geschosse) findet `getEntitiesOfClass` im selben Befehl oft nicht: danach per RCON zählen
  (`execute if entity @e[type=tconstruct:arrow]`, `execute as @e[type=item] run data get entity @s Item`).

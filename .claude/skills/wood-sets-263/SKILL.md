---
name: wood-sets-263
description: Beim Portieren eines Hexxit-Mods mit eigenen Holzarten oder Pflanzen auf 26.3 benutzen (Planken, Stämme, Blätter, Setzlinge, Treppen, Stufen, Zäune, Tore, Türen, Falltüren, Knöpfe, Druckplatten, Werkbänke, Bücherregale, Brennwert, Brennbarkeit, Rezeptkonflikte mit Vanilla). Greift bei Natura (Vorlage), Project Zulu (Palme), Twilight Forest, Tinkers' Construct.
---

# Holzsets und Pflanzen in 26.3

Vorlage: `mods/natura` (13 Hölzer). Java in `natura/NaturaBlocks.java` (`perWood`, `woodBlock`), die JSONs hat ein
Python-Generator erzeugt (Muster unten), nicht von Hand.

## Blöcke (Java)

- Holzklassen von Vanilla direkt: `RotatedPillarBlock`, `StairBlock(planks.defaultBlockState(), p)`, `SlabBlock`,
  `FenceBlock`, `FenceGateBlock(WoodType.OAK, p)`, `PressurePlateBlock(BlockSetType.OAK, p)`,
  `ButtonBlock(BlockSetType.OAK, 30, p)`, `TrapDoorBlock(BlockSetType.OAK, p)`, `DoorBlock(BlockSetType.OAK, p)`.
  Tür-Item: `new DoubleHighBlockItem(block.get(), p)` mit `p.useBlockDescriptionPrefix()`.
- Eigene Werkbank: `CraftingTableBlock` + `getMenuProvider` → Unterklasse von `CraftingMenu`, die nur `stillValid`
  auf den eigenen Block prüft (`natura.block.NaturaWorkbenchBlock`). Kein MenuType, Client zeigt den Vanilla-Bildschirm.
- Blätter: Unterklasse von `TintedParticleLeavesBlock(0.01F, p)`; eigener Zerfall in `randomTick` (siehe `NaturaLeavesBlock`).
- Setzlinge: `VegetationBlock` + `BonemealableBlock`, `STAGE` = Bit 8 von 1.5.2, Wachstum über einen statischen Haken,
  den die Weltgenerierung verdrahtet (`natura.block.NaturaTree.setGrower`).
- `Material.wood` → `.instrument(BASS).sound(SoundType.WOOD)`, brennbar → `.ignitedByLava()`. `setResistance(r)` von 1.5.2
  = `r * 0.6` heute; `setLightValue(f)` = `(int) (15 * f)`.

## Brennwert und Brennbarkeit

- Ofen-Brennwert ist eine Item-Komponente: `p.cookingFuel(ContextIntProviders.COOKING_TIME_WOOD_BLOCKS)` (300),
  `COOKING_TIME_WOOD_ITEMS_LARGE` (200), `COOKING_TIME_WOOD_ITEMS_EXTRA_SMALL` (100). Für Block-Items über
  `ITEMS.registerSimpleBlockItem(block, itemProps)`. Es gibt keine `furnace_fuels`-Data-Map mehr.
- Feuer: `FireBlock.setFlammable` ist privat. Stattdessen Data-Map `data/neoforge/data_maps/block/flammable.json`:
  `{"values": {"mod:x": {"ignite_odds": 5, "burn_odds": 20}}}` (= `setBurnProperties(id, 5, 20)` von 1.5.2).

## Modelle und Blockstates aus Vanilla erzeugen

Vanilla-Daten entpacken: `unzip <mod>/build/moddev/artifacts/minecraft-patched-26.3.0.102.jar 'assets/minecraft/*' 'data/minecraft/*'`.
Für Treppe, Zaun, Tor, Tür, Falltür, Knopf, Druckplatte: `blockstates/oak_<art>.json` laden, `minecraft:block/oak_` →
`<mod>:block/<holz>_` ersetzen, jedes dann genannte Modell aus `models/block/oak_*.json` kopieren und darin
`minecraft:block/oak_planks` (bzw. `oak_door_`, `oak_trapdoor`) ersetzen; Parent ohne Namespace mit `minecraft:` versehen.
Item-Definitionen: Zaun → `block/<holz>_fence_inventory` (Parent `minecraft:block/fence_inventory`), Knopf →
`button_inventory`, Falltür → `<holz>_trapdoor_bottom`, Tür → `item/generated`. Loot: Vanilla `oak_slab`/`oak_door`/`bookshelf` als Muster.
Bögen: Item-Definition `minecraft:condition`/`using_item` + `range_dispatch` `use_duration` (`scale` 1.0, Schwellen in Ticks).

## Rezeptkonflikte mit Vanilla

Vanilla nimmt `#minecraft:planks` für Werkbank, Stöcke, Bücherregal, Schüssel und Holzwerkzeuge. Hatte das Original eigene
Rezepte mit demselben Muster (Natura: `addShapedRecipeFirst`), gewinnt heute sonst Vanilla (Rezepte sind nach Id sortiert).
Lösung: die Vanilla-Rezepte in `data/minecraft/recipe/` überschreiben, Zutat
`{"neoforge:ingredient_type": "neoforge:difference", "base": "#minecraft:planks", "subtracted": "#<mod>:planks"}`.
Zutatenlisten (`["a", "b"]`) und Ergebnis-Komponenten (`"components": {"minecraft:enchantments": {"minecraft:protection": 1}}`) gehen direkt.

## Tags (Block + Item)

`logs` (+ `logs_that_burn` nur brennbare), `planks`, `wooden_slabs`, `wooden_stairs`, `wooden_fences`, `fence_gates`,
`wooden_pressure_plates`, `wooden_buttons`, `wooden_trapdoors`, `wooden_doors`, `leaves`, `saplings`, `mineable/axe`
(Blätter `mineable/hoe`), `enchantment_power_provider` (Bücherregale), `c:player_workstations/crafting_tables`,
`c:rods/wooden` (Stöcke). Werkzeuge in `minecraft:swords/axes/pickaxes/shovels` (Verzauberbarkeit kommt darüber),
Bögen in `minecraft:enchantable/bow` + `enchantable/durability`. TreeCapitator: Skill `treecapitator-trees`.

## Prüfen

Ohne Server: Querverweise skripten (jede Blockstate-/Item-Modell-Referenz existiert, jede Textur steht in
`original-assets.txt`, jeder Block hat Blockstate, Loot, Sprache). `CI=true ./gradlew :<mod>:build` lädt dabei die
Texturen aus dem Original und bricht bei fehlenden ab. Datenpack-Fehler zeigt erst der Serverstart.

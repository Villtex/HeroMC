# Tinkers' Construct 3.12.1 – offen (Teil 27)

Stand nach 27e (Modifikator-System). Ausführlich: Skill `tinkers3-263`, Bericht des Konverters `tools/convert_report.md`.

## Wo der Code liegt

- `src/main/java`: portiert und im Build (27d: `library/{json,utils,module,materials,tools,modifiers-Kern,events}`,
  `common` ohne Multiblock, `tools/stats`, `tools/TinkerToolParts`, Platzhalter `tools/TinkerTools`,
  `shared/TinkerEffects`, `shared/TinkerCommons` mit nur dem, was die Bibliothek braucht).
- `todo/java`: Originalquellen nach `tools/forge_to_neo.py`, noch nicht portiert (nicht im Build). Paketweise nach
  `src/main/java` holen. Für große Zwischenstände, die noch nicht bauen: Ordner `wip/java` und
  `./gradlew :tconstruct3:compileJava -Pwip` (Build-Schalter in `build.gradle`).
- Gekennzeichnete Lücken im Code: `grep -rn "TODO 27[e-k]" src/main/java`.

## Für die nächsten Teile

- **Aus 27e offen:** Modifikator-Daten, die `data-enabled.txt` mit `!` ausschließt (je Teil wieder freischalten,
  Grund steht dabei), `tinkering/tags/modifiers/`, Modifikator-Rezepte (Rezeptdaten aus, brauchen 27g/27h),
  `ToolInventoryCapability.menuFactory = ToolContainerMenu::new` (27h), EFLN-Explosion (27j, `ProjectileExplosionModule`),
  Flasche/Bottle der Trank-Flüssigkeiten in `TagPredicate.legacyTag` (27g), `LayoutIcon` baut beim Laden einen
  `ItemStack` (vor dem Einschalten der Tisch-Layouts in 27g auf `ItemOutput` umstellen), BlockItem-Prüfung von
  `BlockItemProviderModule` (entfiel mit `ItemOutput`), `TinkerModifiers`/`TinkerTables` aus dem Original ergänzen.
- **27f (Rezepte):** `MaterialCastingLookup`, `MaterialRecipeCache`, `MeltingFuelLookup`, `ModifierRecipeLookup`
  (liegen in `todo`, brauchen die Rezeptklassen), dann Material-Prädikate `castable`/`composite`, Fluid-Prädikat
  `fuel`, `SlotTypeModifierPredicate`, `RandomMaterial.RandomVariant` (Varianten aus Material-Rezepten),
  `MaterialRecipe.INGOTS_PER_REPAIR` in `RepairStats`/`SkullStats`, `TinkerRecipeTypes.init` in `TinkerModule`,
  `ModifierSets` ↔ `ModifierSetWorktableRecipe`. Rarität und Materialname der Teile (`MaterialItem.getRarity`,
  Komponente `minecraft:rarity`), Weiterleitungen beim Laden (`MaterialItem.verifyTag`, `ToolStack.verifyTag`).
- **27g:** Tische und `station_layouts` einschalten (3 Layouts filtern auf das Item-Tag `tconstruct3:modifiable`, das
  erst mit 27h gefüllt ist); `InventorySlotSyncPacket` (todo), `TinkerCommons` vervollständigen (Platzhalter mit
  den Bedingungen und Prädikat-Ladern), `FluidDeferredRegisterExtension` (braucht `SlimeFluidType`).
  `minecraft:copy_nbt` in 19 Block-Beute-Tabellen durch `copy_components` ersetzen (Konverter-Regel).
- **27h (Werkzeuge):** `ModifiableItem` (todo; Item-API von 26.3: `use` → `InteractionResult`, Attribut-Komponente,
  Enchantments, Haltbarkeit über `minecraft:max_damage`, das `ToolStack.setStats` schon setzt), Rüstung/Fernkampf
  (`library/tools/item/{armor,ranged}`), `IndestructibleItemEntity`, `ArmorUtil`, Material-Blöcke
  (`library/tools/part/block`), `TinkerTools`/`TinkerModifiers` an Stelle der Platzhalter (Registrierungen der
  Definitionsmodule stehen schon in `TinkerTools.registerSerializers`). In Legacy-Welten fehlen alle Definitionen:
  „Missing tool definition“ dann nicht als ERROR loggen (oder Prüfung über `HexxitWorldType`).
  `MobEquipment` baut beim Laden `ItemStack`s → vor dem Einschalten von `mob_equipment` auf ersten Gebrauch umstellen.
- **27i:** Gussformen `PartCastItem`, `CastItemObject`, `ItemDeferredRegisterExtension.registerCast` (todo),
  `smeltery`-Pakete im `TinkerNetwork`, Platten-Attrappen im Teile-Tab. Komponente `tconstruct3:tank` (Konverter
  vorläufig) festlegen.
- **27j:** `trim_material` (16) auf das 26.3-Format; `EnderSlimeTeleportEvent` (todo, braucht `EnderSlimeEntity`);
  Geoden-Klänge (`Sounds` mit `GeodeItemObject.BudSize` sind da); `minecraft:geode`/`ore`/`tree`-Features prüfen.
- **27k:** Integrated-Features von 1.4.3 abschalten; JEI/Kreativ-Tabs über `HexxitWorldType.onClientChange` neu
  aufbauen (der Teile-Tab `tconstruct3:tool_parts` ist in Legacy noch sichtbar).

## In 27d gestrichen (in 3.12.1 veraltet oder doppelt)

- `json/condition` (`TagNotEmptyCondition`, `TagIntersectionPresentCondition`, `TagDifferencePresentCondition`):
  zum Entfernen markiert, in den Daten unbenutzt (Mantle-Bedingungen statt dessen).
- `json/loot/TagPreferenceLootEntry`: zum Entfernen markiert (`mantle:tag_preference`).
- `json/loot/HasLootContextSetCondition`: gleich wie `mantle:has_context_set`; `TinkerCommons` registriert Mantles
  Codec zusätzlich unter `tconstruct3:has_context_set` (1× in den Daten).
- `common/recipe/LoadableFinishedRecipe` (Datagen).
- `Config`: „Feuer-/Explosionsschutz auf alle Slots“ (Enchantment-Slots sind in 26.3 Daten, ggf. 27h als JSON).

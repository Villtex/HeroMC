---
name: tinkers3-263
description: Beim Arbeiten an Tinkers' Construct 3.12.1 und Mantle (mods/tconstruct3, mods/mantle, Teile 27a–27k) benutzen – Mod-ID tconstruct3 neben 1.4.3 (tconstruct), Quellen vom Tag holen, Daten-Konverter convert_data.py und sein Bericht, Daten ordnerweise einschalten (data-enabled.txt), Welttyp-Regel (1.4.3 nur Hexxit Legacy, 3.12.1 nur Integrated) in Daten und Code über worldgen152:world_type und HexxitWorldType, Assets aus dem Tag mit umbenanntem Namensraum. Ab 27d: Werkzeugdaten als Datenkomponenten (ToolStack, ToolDataComponents), Tinkers-IDs als ResourceId mit .location(), Ersatz für LazyOptional/Tier/Capabilities, Platzhalter TinkerTools/TinkerCommons, große Ports stufenweise mit -Pwip und die Lombok-Falle bei Fehlern im Klassenkopf. Auch für Client-Code von Mantle/Tinkers auf 26.3: GUI-Umbau (tools/gui_to_extract.py), Bildschirme, Handbücher, Modell-Lader und Blockstate-Modelle, Flüssigkeits-Modelle, Block-Renderer mit Submit-Pipeline.
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
- NBT → Komponenten über `nbt_to_components` (`tic_*` → Werkzeug-Komponenten von 27d, `Material` → `tconstruct3:material`,
  `tic_display`/`tic_broken` → `{}`; `tank` noch vorläufig, 27i), Unbekanntes → `minecraft:custom_data` + Bericht.
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

## Tinkers-Bibliothek (seit 27d, `mods/tconstruct3/src/main/java/slimeknights/tconstruct/library`)

- **Werkzeugdaten = Komponenten** (`library/tools/nbt/ToolDataComponents`): `tconstruct3:materials|upgrades|modifiers|
  stats|multipliers|persistent_data|volatile_data|broken|display|material`, Werte in der NBT-Form von 1.20.1. `ToolStack`
  hält den `ItemStack` (`ToolStack.from(stack)` schreibt direkt hinein, `copyFrom`/`createTool` auf einer Kopie);
  `getPersistentData()` ist eine Kopie mit Änderungs-Hörer → nach `put…` steht es im Stack. Listen, die man aus
  den Daten holt und **in place** ändert, werden nicht zurückgeschrieben: wieder `put` aufrufen. Nie `stack.getTag()`
  – Schnellzugriffe ohne ToolStack in `ModifierUtil` (`getModifierLevel`, `checkVolatileFlag` …).
- **IDs:** `MaterialId`, `ModifierId`, `ToolStatId`, `MaterialStatsId`, `Pattern` sind `ResourceId` (hüllt einen
  `Identifier`, der in 26.3 final ist): `.location()` für Registries/Pakete/`Util.makeTranslationKey`;
  `fix_by_errors.py` setzt es von selbst. `Modifier`/`MaterialStatType` sind kein `IdAwareObject` mehr.
- **Ersatz für Weggefallenes:** `library.utils.LazyOptional` (forge_to_neo stellt um), `library.utils.Tier` +
  `TierSortingRegistry` (Ernte-Stufen über `incorrect_for_*_tool`-Tags), Entity-Daten als Attachments
  (`TinkerDataCapability.get(entity)` statt `getCapability(CAPABILITY)`, Register `ATTACHMENT_TYPES`),
  Werkzeug-Capabilities `ToolCapabilityProvider` mit `ItemCapability` (Provider + `registerCapability` +
  `registerItems` in `RegisterCapabilitiesEvent`), Enchantments in Hooks als `ResourceKey<Enchantment>`
  (`EnchantmentModifierHook.toMap/setEnchantments`), Attribute als `Holder<Attribute>`, Schaden über
  `ToolAttackUtil.hurt(entity, source, dmg)` (26.3 `hurt` ist void), `ModifierUtil.SHIELD_BLOCK`,
  `ModifierSets.isInSet`, `ArmorModuleBuilder.ARMOR_TYPES` (die vier Rüstungsteile; `ArmorType.values()` hat BODY).
- **Platzhalter:** `tools/TinkerTools`, `shared/TinkerCommons`, `shared/TinkerEffects` enthalten nur, was die
  Bibliothek braucht (Verweise per `RegistryObject.create`, Lader-Registrierungen). Der jeweilige Teil ersetzt sie
  durch das volle Original und behält die 26.3-Teile daraus.
- **Testen ohne Werkzeug-Items:** temporärer Befehl mit `ToolDefinition.create(id)` (vor dem Datenladen) und
  `ToolStack.createTool(Items.STICK, definition, MaterialNBT.of(...))`, Stack in eine Kiste, `save-all`, Neustart.

## Große Ports in Stufen (seit 27d)

- Unfertiges nach `mods/<mod>/wip/java`: nur `./gradlew :<mod>:compileJava -Pwip` kompiliert es, der normale Build
  bleibt grün und Zwischenstände lassen sich pushen. Fertig → nach `src/main/java` verschieben.
- **Lombok läuft gar nicht, solange es Fehler im Klassenkopf gibt** (`cannot inherit from final`, fehlende Klassen in
  `extends`/`implements`/Importen): dann fehlen alle generierten Konstruktoren/Getter und es gibt Hunderte
  Folgefehler. Erst diese Fehler beheben (`-XprintRounds` über `-PjavacRounds` zeigt, ob Runden laufen).

## Port-Werkzeuge Forge 1.20.1 → NeoForge 26.3 (seit 27b, auch für Tinkers-Code)

1. Original-Dateien kopieren, `python3 tools/forge_to_neo.py <dir>` (Pakete, `ResourceLocation`→`Identifier`,
   `RegistryObject`→`DeferredHolder`, `ForgeRegistries.X`→`BuiltInRegistries.X`, `ToolAction`→`ItemAbility`,
   `isClientSide`→`isClientSide()`, Forge-Fluid-API → `slimeknights.mantle.fluid.compat`, `NetworkEvent`/
   `NetworkDirection` → Mantle-Shims, `CraftingHelper` → `slimeknights.mantle.util.CraftingHelper`,
   `SimpleJsonResourceReloadListener` → `slimeknights.mantle.data.listener.JsonReloadListener`). Neue Regeln dort ergänzen.
2. Kompilieren: `CI=true ./gradlew :<mod>:compileJava -q > log 2>&1` (alle Fehler, `-Xmaxerrs`), dann
   `python3 tools/fix_by_errors.py log` (NBT-Getter → `getIntOr(k, 0)` usw., `ResourceKey.location()` →
   `identifier()`, `registry.get(id)` → `getValue`, `getHolder` → `get`). Wiederholen, Rest von Hand.
3. Große Pakete stufenweise: noch nicht portierte Quellen in einen nicht kompilierten Ordner (`mods/<mod>/todo/`),
   paketweise nach `src/main/java` holen. So bleibt jeder Zwischenstand baubar.
4. Lombok/JSR-305: `mod_uses_lombok=true`, Mixins: `mod_mixins=<name>.mixins.json` in `gradle.properties`.

Mantle-Ersatz für weggefallene 26.3-APIs (Tinkers benutzt diese):
- **Netzwerk:** `NetworkWrapper` (Pakete im Mod-Konstruktor registrieren), Handler bekommt `NetworkEvent.Context`.
- **Rezepte:** `ICommonRecipe` (`getId`, `getResultItem(RegistryAccess)`, `assemble(C, RegistryAccess)`,
  `getIngredients`, `getGroup`), Eingaben `IRecipeContainer`/`ISingleStackContainer`/`IEmptyContainer` sind
  `RecipeInput`. `LoadableRecipeSerializer.of(loadable[, type])` → `RecipeSerializer` (Record); Rezept-ID beim Laden
  in `ContextKey.ID` (Mixin `PendingRegistrationMixin` → `RecipeIdContext`). Rezepte am Client: `RecipeHelper.recipeMap(level)`
  (nur Typen, die der Server per `OnDatapackSyncEvent#sendRecipes` schickt).
- **Lader:** `JsonReloadListener` (Gson-Form, verwirft Dateien mit falschen `neoforge:conditions`), Registry-Zugriff
  beim JSON-Lesen über `ContextKey.registryOps(context)`; `CodecLoadable` nutzt das.
- **Flüssigkeiten:** `fluid.compat.IFluidHandler`/`FluidTank`; Block/Item-Lookup `FluidHandlers.getBlockHandler`,
  `getItemHandler`; anderen Mods anbieten mit `FluidHandlers.asResourceHandler(tank)` in `RegisterCapabilitiesEvent`.
- **Daten:** Item-NBT → Komponenten (`MantleData`, `LegacyNbt.toComponents` für alte `"nbt"`), `MobType` als
  `slimeknights.mantle.data.predicate.entity.MobType` (über Entity-Tags), Enchantments nur als `ResourceKey`.
- **Registrierung:** `slimeknights.mantle.registration.RegistryObject<T>` ersetzt Forges `RegistryObject` (auf
  `DeferredHolder`; `forge_to_neo.py` stellt den Import um). Mantles Register führen jeden Konstruktor in
  `RegistrationContext` aus, die Mixins `ItemPropertiesMixin`/`BlockPropertiesMixin` setzen damit die Registry-ID
  (26.3 braucht sie vor dem Bauen). Eigene `DeferredRegister` von Tinkers ohne Mantle brauchen `setId` selbst.
- **Keine ItemStacks während eines Reloads** (Item-Komponenten sind erst danach gebunden, „Components not bound yet“):
  Lader mit `ItemOutput` parsen beim ersten Gebrauch (`FluidContainerTransferManager.ensureLoaded`).
- **Item-Fähigkeiten:** `IFluidContainerItem` (statt `initCapabilities`), `ConstantFluidContainerWrapper` ist ein
  `ItemAccessResourceHandler`; Blöcke: `inventory.compat.ItemHandlers`/`fluid.compat.FluidHandlers`.
- **Daten statt Code in 26.3:** Brennwerte `neoforge:furnace_fuels`, Entrinden `neoforge:transformable`,
  Entflammbarkeit `neoforge:flammable`; Schilder hängen an den Vanilla-Typen (`BlockEntityTypeAddBlocksEvent`).
- Tooltips: `TooltipItem`/`BlockTooltipItem` rufen die 1.20.1-Form `appendHoverText(stack, level, List, flag)` auf,
  Unterklassen überschreiben weiter diese.

## Client 26.3 (seit 27c, Mantle-Client; gilt für Tinkers 27f–27k)

- **GUI:** nach `forge_to_neo.py` noch `python3 tools/gui_to_extract.py <dir>` (blit nach Argumentzahl, extract-Methoden,
  Maus/Tasten-Records, 2D-Pose). Danach von Hand: `setColor`/`setShaderColor` → Farbe als letztes `blit`-Argument
  (`ARGB.colorFromFloat`), `RenderSystem` weg, Tooltips sind `setTooltipForNextFrame` in **Bildschirm**koordinaten
  (`pose().transformPosition`). **Textfarbe ohne Alpha wird nicht gezeichnet** (`0x404040`, Farben aus Daten):
  `TextDataRenderer.opaque(c)`. Bildschirmgröße/Slots ändern: Access Transformer von Mantle gilt auch für `tconstruct3`.
- **Module-Bildschirme:** `ModuleScreen.extractBackground` ist leer (kein Abdunkeln je Modul), Slot-Hover über
  `MultiModuleScreen.isHoveringSlot`.
- **Bücher:** Items `{"type":"forge:nbt","item","nbt"}` → `IngredientData.nbtStackFactory` (in 27d für `Material` auf die
  Tinkers-Komponente setzen). Rezeptseiten brauchen Rezepte am Client: Typen mit `OnDatapackSyncEvent#sendRecipes`.
- **Block-Renderer:** `BlockEntityRenderer<T,S>` mit Render-State; Items mit `RenderingHelper.resolveItem` im Extract,
  `renderItem(pose, collector, state, renderItem, light)` im Submit; Flüssigkeiten mit `FluidRenderer.renderCuboids/
  renderScaledCuboid(pose, SubmitNodeCollector, …)` (intern `submitCustomGeometry`, Typ `MantleRenderTypes.FLUID`).
- **Flüssigkeits-Texturen/Tönung** kommen aus `FluidModel` (`FluidRenderer.getModel/getStillSprite/getColor`), nicht aus
  `IClientFluidTypeExtensions`. Mantle registriert Modelle für alle Fluid-Typen mit `mantle/fluid_texture`-JSON.
- **Modelle:** eigene Lader = `UnbakedModelLoader` (NeoForge `ModelEvent.RegisterLoaders`), Vanilla-Teil lesen mit
  `ColoredBlockModel.readVanilla(json, ctx)`, Geometrie als `ExtendedUnbakedGeometry`. Alles, was Welt/Blockentity braucht,
  ist ein Blockstate-Modell (`RegisterBlockStateModels`, `"type"` im Blockstate); der Asset-Import setzt `"type"` für die
  Lader in `ImportTagAssets.BLOCKSTATE_LOADERS` – neue Tinkers-Lader mit Weltbezug (Tank, fluid_texture) dort eintragen.
  Texturwechsel zur Laufzeit: Flächen beim Laden einzeln backen, dann `QuadRetexture.addSwapped` (Vorbild
  `RetexturedModel`, `ConnectedBlockStateModel`). Item-Quads aus Sprites: `MantleItemLayerModel.getQuadsForSprite`.
- **Item-Modelle:** 26.3 braucht `assets/<ns>/items/<id>.json` je Item; Varianten über `minecraft:select`/`condition`,
  dynamische Modelle als eigenes `ItemModel.Unbaked` (`RegisterItemModelsEvent`). Die 1.20.1-Assets haben keine.
- **Fallen** (alle in 27c gefunden): `ItemStack` in statischen Initialisierern von Client-Klassen („Components not bound
  yet“); leere `Ingredient` verboten → `IngredientLoadable.empty()` (`mantle:empty`); eigene Brigadier-Argumente
  registrieren (`ArgumentTypeDeferredRegister`), sonst trennt der Server beim Beitritt („Invalid player data“);
  Entities nie `move()` auf dem Client. Client-Probleme zeigen sich nur im echten Client (Skill `headless-client`).

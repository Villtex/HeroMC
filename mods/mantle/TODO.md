# Mantle 1.11.117 auf 26.3 – Stand nach Teil 27c

Stand: Kern (27b) und Client (27c) portiert und bauen (`mods/mantle/src/main`). Was noch nicht portiert ist, liegt in
`mods/mantle/todo-27k/` (Bauwerk-Vorschau der Bücher, Extra-Herzen), gestrichene Klassen in `mods/mantle/todo/dropped/`. Beides wird nicht kompiliert. Vorgehen je Paket: nach `src/main/java` verschieben, kompilieren,
`python3 tools/fix_by_errors.py <log>` laufen lassen, Rest von Hand (Skill `tinkers3-263`, Abschnitt „Port-Werkzeuge“).

## Fertig (src/main)
- `util` (ohne Client), `data` (Loadables, Lader, Prädikate, Registries, Datamaps), `network` (NetworkWrapper auf
  NeoForge-Payloads), `config`, `recipe` (ICommonRecipe/Container auf RecipeInput, Serializer-Fabriken, Rezept-ID per
  Mixin, RecipeHelper, Bedingungen `mantle:tag_filled|tag_empty|tag_combination_filled`, `mantle:crafting_shaped_retextured`,
  ItemOutput/FluidOutput/SizedIngredient/EntityIngredient/FluidIngredient), `fluid/compat` (IFluidHandler-Brücke),
  `MantleData` (Komponenten), `client/SafeClientAccess`, `TooltipKey`.
- **Zutaten-Typen** `mantle:potion`, `mantle:potion_display`, `mantle:fluid_container` als `ICustomIngredient`
  (JSON im 1.20.1-Format, so wie der Konverter es liefert).
- **Flüssigkeits-Transfer** (`FluidTransferHelper`, `fluid/transfer`, Lader `mantle/fluid_transfer` über
  `AddServerReloadListenersEvent`, Paket), `datagen/MantleTags` (nur die Tag-Schlüssel). Daten `data/mantle/mantle/fluid_transfer/` eingeschaltet.
- **Beute**: Bedingungen (`block_tag`, `has_context_set`, …), Funktionen (`set_fluid`, `fill_retextured_block`),
  Eintrag `tag_preference`, Beute-Modifier `add_entry`/`replace_item` (Funktionen als Holder-Liste), `LootTableInjector`
  als Welt-Registry (lädt vor den Beute-Tabellen, `LootPool#entries` per Reflection).
- **registration**: `RegistryObject<T>` (Ersatz für Forge, auf `DeferredHolder`), `RegistrationContext` + Mixins
  `ItemPropertiesMixin`/`BlockPropertiesMixin` (Registry-ID vor dem Bauen, sonst „Item id not set“; Block-Items mit
  `block.`-Beschreibung), alle Deferred-Register, Objekte (`ItemObject`, `EnumObject`, `WoodBlockObject`, `FluidObject` …).
- **block/item/inventory**: Schilder auf den Vanilla-Blockentity-Typen (`BlockEntityTypeAddBlocksEvent`),
  `inventory/compat` (IItemHandler-Brücke auf die 26.3-Transfer-API), `ConstantFluidContainerWrapper` als
  `ItemAccessResourceHandler`, `IFluidContainerItem` (Ersatz für `initCapabilities`), Tooltip-Items mit Brücke zur
  1.20.1-Methode `appendHoverText(stack, level, List, flag)`, Inventar-Blöcke droppen über `preRemoveSideEffects`.

## Gestrichen (todo/dropped/, Tinkers 3.12.1 braucht es nicht)
- `registration/adapter` (bekommt fertig gebaute Objekte; 26.3 braucht die ID vor dem Bauen).
- `InvertedFluid` und `FluidDeferredRegister#invertedFlowing` (aufwärts fließende Flüssigkeit, Flüssigkeits-Interna von
  26.3 komplett anders). `InvertedFluidType` bleibt (nur die Client-Textur, 27c).
- `ArmorTooltipItem` (26.3 hat kein `ArmorItem`), `HarvestTiersCommand` (keine Werkzeug-Stufen), `MissingMappingsEvent`/
  `ObjectHolder`-Teile von `RegistrationHelper`/`MantleRegistrations`.

## 27b erledigt
- `command` (ohne `command/client`, das ist 27c) und `MantleEvents` sind portiert, die Registrierungen aus dem alten
  `Mantle.java` (Prädikat-Lader, `TagPreference.init`, `MantleCommand.init`, Konfiguration) stehen in `Mantle`.

## Offen für spätere Teile (Folgen der 26.3-Änderungen)
- **Keine ItemStacks beim Reload**: 26.3 bindet Item-Komponenten erst nach allen Reload-Listenern („Components not
  bound yet“). Lader, die `ItemOutput`/`ItemStack` lesen, parsen deshalb erst beim ersten Gebrauch (Vorbild:
  `FluidContainerTransferManager.ensureLoaded`). Gilt für alle Tinkers-Lader (27d–27h).
- **Fluid-Transfer im Spiel prüfen** (Client oder FakePlayer): Lader parst erst beim ersten Gebrauch, am Server ohne
  Spieler noch nicht ausgelöst.
- **Brennwerte** (27g/27j): `Burnable*Item` speichern die Zeit nur noch; 26.3 kennt Brennstoff nur über die Datenkarte
  `neoforge:furnace_fuels`. Für die Holz-Sets von Tinkers (Greenheart, Skyroot, Bloodshroom: Holz 300, Knopf 100,
  Schild 200 …) eine Datenkarte schreiben.
- **Entrinden** (27j): `StrippableLogBlock` entrindet nicht mehr selbst; Einträge in der Datenkarte `neoforge:transformable`.
- **Entflammbarkeit** (27j): prüfen, ob `registerWood` sie setzt; in 26.3 Datenkarte `neoforge:flammable`.
- **Blumentöpfe** (27j): `FlowerPotBlock#addPlant` fehlt in 26.3; prüfen, ob der Supplier-Konstruktor den Topf füllt.
- **Spawn-Eier**: Farben gibt es nicht mehr (Texturen), `registerWithEgg` ignoriert sie.
- **Item-NBT von SingleItemHandler** jetzt `write(ValueOutput)`/`read(ValueInput)` (Tinkers 27g anpassen).
- **Lebensmittel-Effekte** kommen aus der Komponente `minecraft:consumable` (`ContainerFoodItem.getEffects`), Tinkers'
  `TinkerFood` (27g) muss Effekte als `ApplyStatusEffectsConsumeEffect` setzen.

## 27c erledigt (Client)
- Bildschirm-Hilfen, Handbuch-Engine (Bücher, Seiten-Typen, Buch-Pakete und -Items, `/mantle book open`),
  Flüssigkeits-Tooltips/-Texturen, Render-Helfer (Submit-Pipeline), Modell-Lader, HUD (Nebenhand-Angriff, Gauge).
- Einzelheiten und Entscheidungen: `docs/PLAN.md`, Teil 27c – Stand; Muster im Skill `tinkers3-263` („Client 26.3“).

## Offen aus 27c (für 27k bzw. die Tinkers-Teile)
- **Bauwerk-Vorschau in Büchern** (`ContentStructure`, `StructureElement`, `client/book/structure`, in `todo-27k/`):
  braucht einen eigenen Picture-in-Picture-Renderer (`RegisterPictureInPictureRenderersEvent`) mit einer Template-Welt;
  bis dahin zeigt `mantle:structure` Titel und Text. Tinkers nutzt es auf 2 Seiten (Schmelzerei, Gießerei).
- **Extra-Herzen** (`ExtraHeartRenderHandler`, in `todo-27k/`): nur in Integrated zeichnen (Legacy hat die Herzen von
  Tinkers 1.4.3), Vanilla-Herz-Sprites `hud/heart/*` statt `icons.png`. Bis dahin zeichnet Vanilla.
- **Item-Modelle**: 26.3 braucht `assets/<ns>/items/<id>.json`. Für Tische mit Holztextur (`mantle:retextured` am
  Item), `mantle:nbt_key` (Varianten über `minecraft:select` auf einer Komponente) und alle Tinkers-Items legen 27f–27h
  die Item-Definitionen an; die Modell-Lader backen bis dahin das Standard-Aussehen.
- **Buch-Export** (`/mantle book export_images|export_html`) gestrichen (Entwickler-Werkzeug, offscreen Framebuffer).
- **Mantle-Shader** (Nebel-Fix, Fullbright) gestrichen; `MantleRenderTypes` nimmt Vanilla-Typen.
- **Effekt-Tönung von Item-Symbolen** in Buch-Auswahlseiten (gesperrte Abschnitte) nur als Farbfläche (kein globaler
  Shader-Farbwert in 26.3).
- **Im Client prüfen, sobald Tinkers-Blöcke existieren (27g/27i):** verbundenes Glas, Tische mit Holz, Flüssigkeiten in
  Tanks, Gauge-Tooltip, Nebenhand-Anzeige.

## Hinweise für Tinkers (27d–27h)
- Konverter: `"neoforge:ingredient_type": "mantle:tag"/"mantle:set"/... sind Mantle-**Prädikate** (Schlüssel
  `ingredient`/`tools` in Modifikator-Daten), keine Zutaten – in `convert_data.py` ausnehmen.
- Rezept-ID: `ContextKey.ID` kommt aus `RecipeIdContext` (Mixin `PendingRegistrationMixin`), über das Netz schickt
  `LoadableRecipeSerializer` die ID von `ICommonRecipe#getId` mit.
- `TypeAwareRecipeSerializer` und `LoggingRecipeSerializer` gibt es nicht mehr (RecipeSerializer ist ein Record);
  `LoadableRecipeSerializer.of(loadable, type)` gibt `RecipeSerializer<T>`, der Typ steht in `LoadableRecipeSerializer.TYPE`.
- Datenkomponenten statt NBT: `ItemStackLoadable`/`FluidStackLoadable` lesen `id`/`components` (alt: `item`/`nbt`
  über `LegacyNbt`); `RetexturedHelper` über `mantle:block_texture`.

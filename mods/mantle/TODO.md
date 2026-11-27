# Mantle 1.11.117 auf 26.3 – offen (Teil 27b, Zwischenstand)

Stand: Kern teilweise portiert und baut (`mods/mantle/src/main`). Noch nicht portierte Quellen liegen bereits
umbenannt (`tools/forge_to_neo.py` ist gelaufen) in `mods/mantle/todo/` (27b) und `mods/mantle/todo-27c/` (Client, 27c).
Sie werden nicht kompiliert. Vorgehen je Paket: nach `src/main/java` verschieben, kompilieren,
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

## Offen in 27b (todo/)
1. **command** (`todo/slimeknights/mantle/command`, `fix_by_errors.py` ist schon gelaufen, ~50 Fehler übrig):
   `hasPermission(int)` → 26.3-Rechte, Rezept-Manager/`getRecipeIds`, `LootDataType`/`getLootData`, Registry-Tags
   (`getTagNames`/`getTag` → `listTagIds`/`get`), `ClickEvent.OpenFile`, Pack-Version, Verzauberungen als Holder,
   `ProjectileUtil.getEntityHitResult`, Reichweite `Attributes.ENTITY_INTERACTION_RANGE`. Tinkers braucht davon
   `MantleCommand`, `GeneratePackHelper`, `RemoveRecipesCommand`, `argument/TagSource(Argument)`.
2. **MantleEvents** (Seelengebunden über `MantleData.SOULBOUND_SLOT`, Lectern-Buch → 27c), dann die restlichen
   Registrierungen aus `todo/slimeknights/mantle/Mantle.java` (Prädikat-Lader `BlockPredicate.LOADER.register(...)` usw.,
   `TagPreference.init`, `MantleCommand.init`).

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

## Für 27c (todo-27c/)
- Buch-Pakete (`network/packet/*Book*`, `*Page*`) in `MantleNetwork.registerPackets` eintragen; `fluid/texture`,
  `fluid/tooltip`, `TextureFluidType`, `InvertedFluidType`, `command/client`.

## Hinweise für Tinkers (27d–27h)
- Konverter: `"neoforge:ingredient_type": "mantle:tag"/"mantle:set"/... sind Mantle-**Prädikate** (Schlüssel
  `ingredient`/`tools` in Modifikator-Daten), keine Zutaten – in `convert_data.py` ausnehmen.
- Rezept-ID: `ContextKey.ID` kommt aus `RecipeIdContext` (Mixin `PendingRegistrationMixin`), über das Netz schickt
  `LoadableRecipeSerializer` die ID von `ICommonRecipe#getId` mit.
- `TypeAwareRecipeSerializer` und `LoggingRecipeSerializer` gibt es nicht mehr (RecipeSerializer ist ein Record);
  `LoadableRecipeSerializer.of(loadable, type)` gibt `RecipeSerializer<T>`, der Typ steht in `LoadableRecipeSerializer.TYPE`.
- Datenkomponenten statt NBT: `ItemStackLoadable`/`FluidStackLoadable` lesen `id`/`components` (alt: `item`/`nbt`
  über `LegacyNbt`); `RetexturedHelper` über `mantle:block_texture`.

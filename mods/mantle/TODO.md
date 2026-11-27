# Mantle 1.11.117 auf 26.3 – offen (Teil 27b, Zwischenstand)

Stand: Kern teilweise portiert und baut (`mods/mantle/src/main`). Noch nicht portierte Quellen liegen bereits
umbenannt (`tools/forge_to_neo.py` ist gelaufen) in `mods/mantle/todo/` (27b) und `mods/mantle/todo-27c/` (Client, 27c).
Sie werden nicht kompiliert. Vorgehen je Paket: nach `src/main/java` verschieben, kompilieren,
`python3 tools/fix_by_errors.py <log>` laufen lassen, Rest von Hand (Skill `tinkers3-263`, Abschnitt „Port-Werkzeuge“).

## Fertig (src/main)
- `util` (ohne Client), `data` (Loadables, Lader, Prädikate, Registries, Datamaps), `network` (NetworkWrapper auf
  NeoForge-Payloads), `config`, `recipe` (ICommonRecipe/Container auf RecipeInput, Serializer-Fabriken, Rezept-ID per
  Mixin, RecipeHelper mit Client-Rezepten, Bedingungen `mantle:tag_filled|tag_empty|tag_combination_filled`,
  `mantle:crafting_shaped_retextured`, ItemOutput/FluidOutput/SizedIngredient/EntityIngredient/FluidIngredient),
  `fluid/compat` (IFluidHandler-Brücke), `registration/MantleData` (Komponenten), `client/SafeClientAccess`, `TooltipKey`.

## Offen in 27b (todo/)
1. **recipe/ingredient** `ItemIngredient`, `PotionIngredient`, `PotionDisplayIngredient`, `FluidContainerIngredient`,
   `recipe/helper/LoadableIngredientSerializer`: als `ICustomIngredient` + `IngredientType` (MapCodec aus
   `LoadableMapCodec`, StreamCodec aus `LoadableStreamCodec`), registriert in `NeoForgeRegistries.Keys.INGREDIENT_TYPES`
   als `mantle:potion`, `mantle:potion_display`, `mantle:fluid_container`. Vorlage: Mantle 1.21 `recipe/ingredient/item`.
2. **fluid** `FluidTransferHelper`, `UnplaceableFluid`, `InvertedFluid`, `fluid/transfer/*` (Behälter-Transfer, Lader
   `mantle/fluid_transfer`, Paket `FluidContainerTransferPacket` in `MantleNetwork` eintragen): über
   `fluid.compat.FluidHandlers`; `AddReloadListenerEvent` → `AddServerReloadListenersEvent`; `PotionUtils` →
   `PotionContents`. Danach `ItemPredicate.MAY_HAVE_TRANSFER` wieder auf `FluidContainerTransferManager` stellen
   (PORT-TODO dort).
3. **loot** (Basis schon aus Mantle 1.21 kopiert, Pakete umgehängt): an 26.3 anpassen – Bedingungen in Funktionen als
   `Optional<Holder<LootItemCondition>>`, `LootContextParam(Set)` → `ContextKey`/`ContextKeySet`,
   `getReferencedContextParams` → `validate(ValidationContext)`, `LootPoolSingletonContainer` →
   `LootPoolEntry`/`SingleEntryContainerBase`, Builder (Datagen) streichen. `MantleLoot.init(bus)` im Konstruktor.
4. **registration** (deferred, object, adapter, Builder): `RegistryObject` → `DeferredHolder`, `IForgeRegistry` →
   `Registry`, `ForgeFlowingFluid` → `BaseFlowingFluid`; Holz-/Bau-Objekte gegen 26.3 (`WoodType`, `BlockSetType`).
5. **block**, **block/entity**, **block/fluid**, **inventory**, **item**: Block-Entities mit `ValueInput/ValueOutput`,
   Menüs (`IContainerFactory`), Schilder ggf. streichen (26.3-Schilder anders), Bücher-Items → 27c.
6. **command** (ohne `command/client`, das ist 27c), **MantleEvents** (Seelengebunden über `MantleData.SOULBOUND_SLOT`,
   Lectern-Buch → 27c), dann alle Registrierungen in `Mantle` (Vorlage: `todo/slimeknights/mantle/Mantle.java`):
   Prädikat-Lader (`BlockPredicate.LOADER.register(...)` usw.), `TagPreference.init`, `LootTableInjector`.

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

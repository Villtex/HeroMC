# Harken Scythe: Schnittstellen für die Altäre (Teil 9, Teilauftrag 2)

Noch nicht portiert: `BlockHSSoulAltar`, `BlockHSBloodAltar`, `TileEntityHSSoulAltar/BloodAltar`, `ContainerHS*Altar`,
`GuiHS*Altar`, `RecipeHSSoulAltar/BloodAltar`, `GuiHandler`, die Altar-Pakete von `CommonPacketHandlerHarkenScythe`.
Ids: Blöcke `harkenscythe_core:altar_of_souls`, `altar_of_blood` (snake_case von „Altar of Souls/Blood“).

## Was der Kern schon bereitstellt

- Items (`harkenscythe.HSItems`): `BOOK_OF_SHADOWS`, `BOOK_OF_CARNAGE` (Altar-Rezepte, 1.5.2: Obsidian/Netherrack
  „XXX“ unten, „*X*“ mit Enderperlen, Buch oben), Keeper/Vessel (`ESSENCE_KEEPER`, `SOULKEEPER`, `BLOODKEEPER`,
  `ESSENCE_VESSEL`, `SOUL_VESSEL`, `BLOOD_VESSEL`), `BIOMASS_SEED` → `GERMINATED_BIOMASS_SEED` (Blutaltar),
  `LIVINGMETAL_INGOT`, `SOULWEAVE_CLOTH`, `BLOODWEAVE_CLOTH`, `ECTOPLASM`, Talismane, Amulette.
- Füllstand der Keeper: **Haltbarkeit** wie in 1.5.2 (gespeichert = max − damage), Hilfen in `harkenscythe.item.Essence`
  (`stored`, `has`, `consume`, `fill`, `drain`). Kapazität: Soulkeeper 20, Bloodkeeper 40, Soul Vessel 40, Blood Vessel 80.
- Tiegel: `HSBlocks.SOUL_CRUCIBLE` / `BLOOD_CRUCIBLE`, Füllstand im Blockstate `CrucibleBlock.LEVEL` (0–10, Seelen ×5,
  Blut ×10). Der Seelenaltar sucht Tiegel im Umkreis 2 (x/z) und −1..+1 (y) (`BlockHSSoulAltar.randomDisplayTick` und
  die Altar-Rezepte), der Blutaltar entsprechend `BLOOD_CRUCIBLE`.
- Augments als Verzauberungen: `HarkenScythe.SOUL_ATTUNED` … `SANGUINARY` (`ResourceKey<Enchantment>`), Stufe lesen mit
  `harkenscythe.event.HSEvents.level(level, stack, key)`. Unterstützte Items: Tags `harkenscythe_core:enchantable/*`
  (`weapon`, `shield`, `armor`, `attunable`) bzw. Vanilla-Tags (`enchantable/bow`, `enchantable/durability`).
  Kosten 100 (nicht am Zaubertisch), die Altäre müssen sie direkt anwenden (`ItemStack.enchant(holder, level)`).
- Attunement gibt es schon als Werkbank-Rezepte (`data/harkenscythe_core/recipe/attunement/`).
- Projekt-Skill-Kandidat: `menu-gui-263` für Container/Screen/Pakete; Altar-Texturen (`textures/gui/guiSoulAlter.png`,
  `guiBloodAlter.png`, `blocks/tile.HS*Altar_*`) noch nicht in `original-assets.txt` – in `tools/gen.py` ergänzen.
- `RenderHSSoulAltar/BloodAltar` (schwebendes Buch) fehlen ebenfalls.

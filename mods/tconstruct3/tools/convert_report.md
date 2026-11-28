# Daten-Konverter Tinkers 3.12.1 / Mantle – Bericht

Erzeugt von `convert_data.py` (nicht von Hand bearbeiten). Was hier steht, hat der Konverter nicht sicher
umsetzen können oder nur vorläufig umgesetzt; die Teile 27b–27k arbeiten die Punkte ab.

## Dateien je Art

- advancement: 428
- biome_modifier: 8
- command: 1
- damage_type: 32
- function: 8
- loot_modifier: 21
- loot_table: 344
- mantle: 56
- recipe: 3061
- structure: 25
- tags: 853
- tinkering: 845
- trim_material: 16
- worldgen: 31

## c: tags neither NeoForge nor Tinkers define (fine when other mods fill them) (462)

- `c:acetaldehyde`
- `c:armors/boots/bronze`
- `c:armors/boots/constantan`
- `c:armors/boots/copper`
- `c:armors/boots/electrum`
- `c:armors/boots/invar`
- `c:armors/boots/lead`
- `c:armors/boots/nickel`
- `c:armors/boots/osmium`
- `c:armors/boots/refined_glowstone`
- `c:armors/boots/refined_obsidian`
- `c:armors/boots/silver`
- `c:armors/boots/steel`
- `c:armors/boots/tin`
- `c:armors/chestplates/bronze`
- `c:armors/chestplates/constantan`
- `c:armors/chestplates/copper`
- `c:armors/chestplates/electrum`
- `c:armors/chestplates/invar`
- `c:armors/chestplates/lead`
- `c:armors/chestplates/nickel`
- `c:armors/chestplates/osmium`
- `c:armors/chestplates/refined_glowstone`
- `c:armors/chestplates/refined_obsidian`
- `c:armors/chestplates/silver`
- `c:armors/chestplates/steel`
- `c:armors/chestplates/tin`
- `c:armors/helmets/bronze`
- `c:armors/helmets/constantan`
- `c:armors/helmets/copper`
- `c:armors/helmets/electrum`
- `c:armors/helmets/invar`
- `c:armors/helmets/lead`
- `c:armors/helmets/nickel`
- `c:armors/helmets/osmium`
- `c:armors/helmets/refined_glowstone`
- `c:armors/helmets/refined_obsidian`
- `c:armors/helmets/silver`
- `c:armors/helmets/steel`
- `c:armors/helmets/tin`
- `c:armors/leggings/bronze`
- `c:armors/leggings/constantan`
- `c:armors/leggings/copper`
- `c:armors/leggings/electrum`
- `c:armors/leggings/invar`
- `c:armors/leggings/lead`
- `c:armors/leggings/nickel`
- `c:armors/leggings/osmium`
- `c:armors/leggings/refined_glowstone`
- `c:armors/leggings/refined_obsidian`
- `c:armors/leggings/silver`
- `c:armors/leggings/steel`
- `c:armors/leggings/tin`
- `c:biodiesel`
- `c:coins`
- `c:coins/aluminum`
- `c:coins/bronze`
- `c:coins/constantan`
- `c:coins/copper`
- `c:coins/electrum`
- … und 402 weitere

## custom ingredient types (port in 27b-27f) (204)

- 87× `tconstruct3:material`
- 49× `mantle:tag`
- 20× `tconstruct3:no_container`
- 10× `tconstruct3:material_value`
- 9× `tconstruct3:instrument`
- 7× `mantle:inverted`
- 6× `tconstruct3:has_modifier`
- 4× `mantle:or`
- 3× `mantle:set`
- 2× `mantle:fluid_container`
- 2× `mantle:potion_display`
- 2× `tconstruct3:tool_hook`
- `mantle:and`
- `tconstruct3:block_tag`
- `tconstruct3:has_persistent_key`

## custom item predicates as item sub predicates (register in 27d/27e) (126)

- 126× `tconstruct3:tool_stack`

## dropped files (1)

- `forge/loot_modifiers/global_loot_modifiers.json`

## forge:conditional recipes split into one file per alternative (66)

- 62× `2 alternatives`
- 3× `3 alternatives`
- `4 alternatives`

## item NBT tank converted to provisional component tconstruct3:tank (27i decides) (12)

- 9× `tank  (tconstruct/advancements/foundry/blaze.json)`
- 3× `tank  (tconstruct/advancements/foundry/manyullyn_lanterns.json)`

## loot functions on NBT (rewrite as copy_components/set_components when the block entity is ported) (19)

- `minecraft:copy_nbt  (tconstruct/loot_tables/blocks/cast_chest.json)`
- `minecraft:copy_nbt  (tconstruct/loot_tables/blocks/end_fluid_cannon.json)`
- `minecraft:copy_nbt  (tconstruct/loot_tables/blocks/fake_storage_block.json)`
- `minecraft:copy_nbt  (tconstruct/loot_tables/blocks/scorched_anvil.json)`
- `minecraft:copy_nbt  (tconstruct/loot_tables/blocks/scorched_fluid_cannon.json)`
- `minecraft:copy_nbt  (tconstruct/loot_tables/blocks/scorched_fuel_gauge.json)`
- `minecraft:copy_nbt  (tconstruct/loot_tables/blocks/scorched_fuel_tank.json)`
- `minecraft:copy_nbt  (tconstruct/loot_tables/blocks/scorched_ingot_gauge.json)`
- `minecraft:copy_nbt  (tconstruct/loot_tables/blocks/scorched_ingot_tank.json)`
- `minecraft:copy_nbt  (tconstruct/loot_tables/blocks/scorched_lantern.json)`
- `minecraft:copy_nbt  (tconstruct/loot_tables/blocks/seared_casting_tank.json)`
- `minecraft:copy_nbt  (tconstruct/loot_tables/blocks/seared_fluid_cannon.json)`
- `minecraft:copy_nbt  (tconstruct/loot_tables/blocks/seared_fuel_gauge.json)`
- `minecraft:copy_nbt  (tconstruct/loot_tables/blocks/seared_fuel_tank.json)`
- `minecraft:copy_nbt  (tconstruct/loot_tables/blocks/seared_ingot_gauge.json)`
- `minecraft:copy_nbt  (tconstruct/loot_tables/blocks/seared_ingot_tank.json)`
- `minecraft:copy_nbt  (tconstruct/loot_tables/blocks/seared_lantern.json)`
- `minecraft:copy_nbt  (tconstruct/loot_tables/blocks/tinkers_anvil.json)`
- `minecraft:copy_nbt  (tconstruct/loot_tables/blocks/tinkers_chest.json)`

## recipe types of other mods (kept, need that mod) (2)

- 2× `ceramics:kiln`

## sized/mixed ingredient objects kept in Tinkers form (114)

- 75× `amount,tag`
- 22× `amount_needed,item`
- 15× `ingredient`
- 2× `amount_needed,tag`

## trim materials (26.3: no ingredient/item_model_index; the item gets a provides_trim_material component) (16)

- `trim_material/amethyst_bronze.json`
- `trim_material/cinderslime.json`
- `trim_material/cobalt.json`
- `trim_material/earthslime.json`
- `trim_material/enderslime.json`
- `trim_material/hepatizon.json`
- `trim_material/ichor.json`
- `trim_material/knightmetal.json`
- `trim_material/knightslime.json`
- `trim_material/manyullyn.json`
- `trim_material/pig_iron.json`
- `trim_material/queens_slime.json`
- `trim_material/rose_gold.json`
- `trim_material/skyslime.json`
- `trim_material/slimesteel.json`
- `trim_material/steel.json`

## vanilla worldgen types (check the format against 26.3 in Teil 27j) (8)

- 4× `configured_feature: minecraft:geode`
- 2× `configured_feature: minecraft:ore`
- 2× `configured_feature: minecraft:tree`

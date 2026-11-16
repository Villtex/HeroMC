---
name: mobs-263
description: Beim Portieren von Mobs, Spawn-Eiern, Booten, Mob-Drops oder Mob-Spawns eines Hexxit-Mods auf 26.3 benutzen (EntityRegistry.registerModEntity/addSpawn, RenderLiving mit ModelBase, eigene Boote, SpawnEgg-Items, dropFewItems). Beispiele im Repo: mods/natura (NaturaEntities, natura.entity, natura.client.entity), mods/ebxl (Scarecrow).
---

# Mobs, Spawn-Eier und Boote auf 26.3

Vorbild: `mods/natura/src/main/java/natura/NaturaEntities.java`, `natura/entity/*`, `natura/client/entity/*`,
Abschnitt „ENTITIES“ in `mods/natura/tools/gen.py`.

## Registrieren

- `DeferredRegister.createEntities(MOD_ID)`, `ENTITIES.registerEntityType(name, Klass::new, MobCategory, b -> b.sized(w, h)...)`.
  `isImmuneToFire` → `.fireImmune()`, Monster → `.notInPeaceful()`. `registerModEntity(..., trackingRange, ...)`:
  `clientTrackingRange(range / 16)`, `updateInterval(...)`.
- Attribute über `EntityAttributeCreationEvent` (`event.put(TYPE.get(), Klass.createAttributes().build())`).
- Vanilla-Typen heißen in 26.3 `EntityTypes.SKELETON` (nicht `EntityType.`).
- Werte aus 1.5.2: `getMaxHealth` → `MAX_HEALTH`, `getAttackStrength` → `ATTACK_DAMAGE`, `experienceValue` → `this.xpReward`,
  Sprunghöhe → `JUMP_STRENGTH`, eigenes `fall()` mit `dist - 5` → `SAFE_FALL_DISTANCE 5`, Suchradius → `FOLLOW_RANGE`.
  Alte AI-Geschwindigkeiten sind absolut: in 26.3 Goal-Faktor = alter Wert / moveSpeed.
- `setDead`-Logik (z. B. Junge beim Tod): `remove(RemovalReason)` überschreiben, nur bei `RemovalReason.KILLED`.
- `initCreature` → `finalizeSpawn`. Bei `Spider` die Zufallseffekte umgehen: Gruppendaten einer anderen Klasse
  übergeben, den 1-%-Skelettreiter danach mit `ejectPassengers()` entfernen (siehe `HeatscarSpider`).
- Creeper mit eigener Zündschnur: eigenen Zähler in `tick()` vor `super.tick()`, `getSwelling(float)` überschreiben.

## Spawns

- `EntityRegistry.addSpawn` → Biome-Modifier `data/<ns>/neoforge/biome_modifier/*.json`:
  `{"type":"neoforge:add_spawns","biomes":"#minecraft:is_nether","spawners":[{"type":"ns:mob","count":4,"weight":10}]}`;
  Packgröße min≠max als `{"type":"minecraft:uniform","min_inclusive":a,"max_inclusive":b}` (26.3-Format, nicht minCount/maxCount).
- `getCanSpawnHere` → `RegisterSpawnPlacementsEvent` mit `Operation.REPLACE` und `SpawnPlacementTypes.ON_GROUND`.
  EntityMob → `Monster::checkMonsterSpawnRules`, ohne Lichtprüfung → `Monster::checkAnyLightMonsterSpawnRules`,
  nur im Nether → `level.getLevel().dimension() == Level.NETHER`. Tiere ohne Gras-Bedingung brauchen ein eigenes Prädikat.

## Drops

- `dropFewItems` → Loot-Tabelle `data/<ns>/loot_table/entities/<id>.json`, Typ `minecraft:entity`. 26.3-Format:
  Funktionen unter `"modifier"`, Diskriminator `"type"`. `rand(n)+a` → `set_count` uniform a..a+n-1;
  Looting `rand(1 + level)` → `enchanted_count_increase` uniform 0..1. Vanilla-Pools (Platte vom Creeper) aus
  `build/vanilla-data/data/minecraft/loot_table/entities/*.json` übernehmen.
- Abhängig vom Zustand (geladener Creeper): `dropCustomDeathLoot(ServerLevel, DamageSource, boolean)` in Java.

## Client

- Modell: `EntityModel<LivingEntityRenderState>`, Teile per `CubeListBuilder`, `LayerDefinition.create(mesh, 64, 32)`
  (ModelBase-Standardgröße). `setRotationAngles(f, f1, f2, f3, f4)` → `state.walkAnimationPos`, `walkAnimationSpeed`,
  `state.yRot` (Kopf-Yaw), `state.xRot` (Pitch), in Grad → `* Mth.DEG_TO_RAD`.
- Renderer: `MobRenderer`; Vanilla-Renderer ableiten und `getTextureLocation`/`scale` überschreiben
  (`SpiderRenderer<T>`, `CreeperRenderer`). Registrieren über `EntityRenderersEvent.RegisterLayerDefinitions`/`RegisterRenderers`.

## Spawn-Eier

- `new Item.Properties().spawnEgg(TYPE.get())`, Klasse `SpawnEggItem` (Entitätstypen sind vor Items registriert).
- 26.3 hat keine einfärbbare Ei-Vorlage: Item-Modell `minecraft:item/egg` mit `tints` `minecraft:constant` (Primärfarbe).

## Boote

- Eigener `EntityType<Boat>` je Holz: `(type, level) -> new Boat(type, level, itemSupplier)`, Item-Supplier lazy über
  `DeferredItem.createItem(Identifier)`; Builder wie Vanilla `noLootTable().sized(1.375F, 0.5625F).eyeHeight(0.5625F)`.
  Item: `new BoatItem(TYPE.get(), p)`, `stacksTo(1)`, brennbares Holz `cookingFuel(ContextIntProviders.COOKING_TIME_BOATS)`.
- Auf Lava fahren: `canBoatInFluid(FluidState)` und `canBoatInFluid(FluidType)` überschreiben (NeoForge, `BloodwoodBoat`).
- Alte 64x32-Boottexturen: eigenes Modell nach `ModelBoat` von 1.5.2, Renderer von `AbstractBoatRenderer` ableiten
  (übernimmt Drehung um 90° und Höhe 0.375 der 26.3-Boote), siehe `NaturaBoatModel`/`NaturaBoatRenderer`.

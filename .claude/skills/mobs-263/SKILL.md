---
name: mobs-263
description: Beim Portieren von Mobs, Spawn-Eiern, Booten, Mob-Drops oder Mob-Spawns eines Hexxit-Mods auf 26.3 benutzen (EntityRegistry.registerModEntity/addSpawn, RenderLiving mit ModelBase, eigene Boote, SpawnEgg-Items, dropFewItems). Greift auch bei vielen Mobs auf einmal (Project Zulu, ChocoCraft, Infernal Mobs, Better Dungeons, Twilight Forest). Beispiele im Repo: mods/natura (NaturaEntities, natura.entity, natura.client.entity), mods/ebxl (Scarecrow), mods/projectzulu (40 Kreaturen: Vögel, Reittiere, Boss, Mehrteiler).
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

## Stolperfallen (aus Teil 9, Harken Scythe)

- `registerGoals` läuft im Konstruktor von `Mob` vor den eigenen Feldern: Ziele, die eigene Felder brauchen, im
  eigenen Konstruktor nach `super(...)` anlegen.
- Zähmbare Mobs: `TamableAnimal`; Lebenspunkte abhängig vom Zähmen über `applyTamingSideEffects`.
- Adulte Variantensounds (Huhn, Kuh, Schwein, Wolf, Katze) gibt es nur je Variante: per
  `BuiltInRegistries.SOUND_EVENT.getValue(Identifier.withDefaultNamespace("entity.cow.ambient"))` holen.
- Ausrüstung aus dem Konstruktor geht bei `/summon` verloren: zusätzlich in `finalizeSpawn` setzen.
- Slime-Unterklassen: `setCubeMobHealth`, `getParticleType` überschreiben, eigene Teilung in `remove`.
- Durchscheinende Modelle („spectral“): `RenderTypes.entityTranslucent(texture)` im Renderer. Vorbild
  `mods/harkenscythe/.../client/mob`.

## Viele Mobs (Vorbild Project Zulu, `mods/projectzulu`)

- **Modell-Geometrie nicht abtippen:** `python3 tools/modelbase_to_layer.py <ModelX.java> <wurzel1,wurzel2>` gibt die
  `addOrReplaceChild`-Zeilen samt Hierarchie, texOffs und `mirror` aus. Nur Animationen von Hand portieren.
- **Animations-Easing am Entity** (EntityModelRotation, `eXXX.rotateX(step, min, max)` in `setLivingAnimations`):
  pro Renderer `EasingCache` (WeakHashMap Entity → Werte) + `Eased.step(...)` in `extractRenderState`, Ergebnis in
  einen eigenen RenderState (`ArmadilloRenderer`). 26.3 setzt Posen jeden Frame zurück: Konstanten jedes Mal setzen.
- **Zornig nach Treffer** (angerLevel): `NeutralMob` in 26.3 = `get/setPersistentAngerEndTime(long)`,
  `EntityReference<LivingEntity>`-Ziel, `updatePersistentAnger(level, true)` in `customServerAiStep`,
  `add/readPersistentAngerSaveData` im NBT (`ArmadilloEntity`). `PanicGoal` nur, wenn das Original wirklich floh.
- **getAttackStrength nach Schwierigkeit:** Normal-Wert als `ATTACK_DAMAGE`, Vanilla skaliert selbst.
  Große Mobs: eigene Nahkampf-Reichweite `(2*Breite)²` (`ZuluBigAnimal`), sonst treffen sie nie.
- **Fliegende Mobs ohne Pathfinder** (EntityAerial): Motion-Delta zur Zielposition vor der KI, `travel()` ohne
  Schwerkraft mit Luftreibung 0.91 (`AerialAnimal`, `AerialGoals`).
- **Reiten mit Sattel und Lenk-Item** (1.5.2-Schweine-KI): `getControllingPassenger`, `tickRidden`,
  `getRiddenInput`, `getRiddenSpeed` (`ZuluHorseEntity`). Vorher prüfen, ob `isRideable()` im Original überhaupt true war.
- **Mehrteilige Mobs** (Tausendfüßer, Wurm): NeoForge-`PartEntity`, Teile folgen der Kopf-Spur (`CentipedeEntity`).
- **Boss-Leiste:** `ServerBossEvent` (`MummyPharaohEntity`).
- **Farbvarianten** als eigene Entity-Typen mit einer Klasse; Renderer wählt die Textur je Typ (Finken, Pferde).
- **Pack-Konfiguration als Quelle:** Spawns je Biom, Eifarben und gewichtete Drops (min..max Würfe) aus der
  Forge-.cfg per gen.py erzeugen (`mods/projectzulu/tools/gen.py`, `parse_cfg`). Biome anderer, noch nicht portierter
  Mods überspringen. Peaceful-Despawn des Originals → `notInPeaceful()`; Zähmbare stattdessen `discard()` nur ungezähmt.
- **Parallel portieren:** je Batch eine eigene Registrierungsklasse (eigene `DeferredRegister`, gleicher Namespace)
  und ein eigener Renderer-Registrar, Agents in eigenen Worktrees; gemeinsame Maps (Texturen/Sounds) nur anhängen.
  Merge-Konflikte bleiben so auf die Map-Dateien beschränkt (Konfliktmarker löschen, beide Seiten behalten).
- **Untote:** Tag `data/minecraft/tags/entity_type/undead.json` (Smite und Tränke hängen daran).
- **Ogg-Namen mit Großbuchstaben** (`MummyShortRoar.ogg`) im Ziel klein schreiben, sonst ungültiger Ressourcenpfad.

## Prüfen ohne Spieler

- `/summon` jedes Mobs + `data get entity … Health` + `loot spawn x y z kill @e[type=…,limit=1]` (zieht die
  Loot-Tabelle wie ein Tod).
- Natürliche Spawns: Kategorie CREATURE spawnt beim Generieren neuer Chunks auch ohne Spieler →
  `locate biome …`, `forceload add` um den Fund (±96), 15 s warten, `execute if entity @e[type=…,x=…,dx=…]` zählen.
  Monster/Ambient brauchen einen Spieler in der Nähe.

## Bewaffnete Biped-Mobs mit eigener KI (Vorbild Better Dungeons, `betterdungeons.mob`)

- **Mehrere 1.5.2-Konstruktoren** (`Mob(world)`, `(world, lvl)`, `(world, lvl, type)`): als Methoden `initAsWorld/initAsLevel/
  initAsLevelType` (`HumanEntity`); Konstruktor (nur Server) und `finalizeSpawn` rufen `initAsWorld`, Spawner rufen ihre
  Variante über eine API (`BDDungeonMob.initDungeonMob`). Vor jedem Neu-Init die Klassen-Goals entfernen (`resetClass`).
- **`configRaze`-Muster** (Werte in einer überschriebenen Methode, die der Basis-Konstruktor aufruft): nur Felder der
  Basisklasse setzen; Felder der Unterklasse mit Initialisierer werden erst danach gesetzt. Goals im eigenen Konstruktor
  anlegen, `registerGoals` leer lassen.
- **Linke Hand** (`leftHandItem`) = `OFFHAND`, Drop-Chance 0; `equipmentDropChances` 0.001 → `setDropChance`.
- **Schaden wie 1.5.2:** `doHurtTarget` selbst rechnen (Rassenstärke + 1.5.2-Waffenwert − 1; Äxte/Spitzhacken waren in
  1.5.2 viel schwächer, Tabelle `BDMobUtil.oldWeaponDamage`), Stärke/Schwäche als `3 << amp` / `2 << amp`,
  danach `causeExtraKnockback` + `EnchantmentHelper.doPostAttackEffects`.
- **Mutex-Bits → Goal-Flags:** 1 = MOVE, 2 = LOOK, 4 = JUMP (Granaten-KI mit 4 läuft neben dem Nahkampf), 8 = keine.
  Alle Goals `requiresUpdateEveryTick() = true`. Alte absolute KI-Geschwindigkeiten: `speed(abs) = abs / MOVEMENT_SPEED-Basis`.
  `getSpeedModifier`-Overrides (Blocken 0.7) → `setSpeed` überschreiben.
- **Target-Tasks von 1.5.2** (Sicht + 60 Ticks Gedächtnis, eigene Reichweite statt FOLLOW_RANGE) als eigene Basisklasse
  (`HumanTargetGoals.Target`); `EntityAIHurtByTarget` setzt das Ziel nur, `continueExecuting` endet sofort.
- **Fremden Mobs Goals geben** (Schwein/Kuh als Reittier): `Mob.goalSelector` per Reflection (`BDMobUtil.goals`).
- **Level-abhängige Drops:** eigene Loot-Bedingung (`Registries.LOOT_CONDITION_TYPE`, `MapCodec`), hier
  `betterdungeons:mob_level`; gemeinsame Pools als eigene Tabelle per `{"type":"minecraft:loot_table","value":…}`
  einbinden, „1/20 alles“ als Pool mit `random_chance`. Loot-Bedingungen in 26.3 unter `"condition"` (ein Objekt,
  mehrere mit `minecraft:all_of`). Generator `mods/betterdungeons/tools/gen_humans.py`.
- **Client:** eigenes `HumanoidModel` mit komplett eigenem `setupAnim` (`resetPose()` + `ModelBiped.setRotationAngles` von
  1.5.2: Beine bei x ±2, gehaltenes Item senkt den Arm), Varianten über Geometrie-/Animations-Enum (`HumanModel`);
  Rüstung je Geometrie als eigene `ArmorModelSet` (Inflation 1.0/0.5). Vanilla-Texturen von 1.5.2 fehlen: 26.3-Texturen
  mit gleichem Layout nehmen (Skelett/Enderman 64×32, Zombie/Steve 64×64 → Layer 64×64), Zombie-Pigman → Vanilla-Layer
  `ModelLayers.ZOMBIFIED_PIGLIN`. Drehen: `poseStack.rotateDegrees(Axis, deg)` / `rotate(Quaternionf)`.
- **Söldner-Steuerung:** GUI nur auf dem Client öffnen (statischer Hook `BDHumans.openControlScreen`, vom Client gesetzt),
  Befehl per `playToServer`-Payload, Server prüft Entfernung und Wertebereich.

# Project Zulu Teil 10 – Portierungsnotizen (Kreaturen)

Original: `.reference/projectzulu/src/projectzulu/common/mobs/` (entity, entityai, entitydefaults, models, renders)
und `.reference/projectzulu/src/projectzulu/common/core/entitydeclaration/`. Pack-Konfiguration (Standardwerte!):
`.reference/z-hexxit-configs/pack-files/config/Project Zulu/ProjectZuluMobBiomeConfig.cfg`.

Fertiges Musterexemplar (genau so arbeiten): **Armadillo** –
`src/main/java/projectzulu/entity/ArmadilloEntity.java`,
`client/entity/ArmadilloModel.java` (RenderState als innere Klasse `State`),
`client/entity/ArmadilloRenderer.java` (EasingCache), Einträge in `ZuluEntities.java`, `ZuluSounds.java`,
`client/entity/ZuluEntityRenderers.java`.

## Konventionen

- Paket `projectzulu.entity` (Server/Common), `projectzulu.client.entity` (Modelle/Renderer).
- Entity-Klassen heißen `<Name>Entity`, Modelle `<Name>Model`, Renderer `<Name>Renderer`.
- Registrierung: `ZuluEntities.entity("snake_case", …)` + Spawn-Ei `egg(...)` + `attributes(...)`-Eintrag +
  `spawnPlacements(...)`-Eintrag. Renderer/Layer in `ZuluEntityRenderers` (`layer(...)`, Baby-Layer über `baby(...)`).
- Vanilla-Basisklassen nutzen: friedliche Tiere `Animal`, nur bei Schlag zurückschlagend → zusätzlich `NeutralMob`
  (26.3-API: `getPersistentAngerEndTime`/`setPersistentAngerEndTime(long)`, `EntityReference<LivingEntity>`-Target,
  `updatePersistentAnger(level, true)` in `customServerAiStep`; Vorbild Armadillo). Monster → `Monster`.
  Zähmbare (Fox, …) → `TamableAnimal`.
- 1.5.2-KI → Vanilla-Goals: AttackOnCollide→`MeleeAttackGoal`, Panic→`PanicGoal`, Wander(x,120)→
  `RandomStrollGoal(this, f, 120)`, Tempt→`TemptGoal`, Mate→`BreedGoal`, FollowParent→`FollowParentGoal`,
  HurtByTarget→`HurtByTargetGoal`, NearestAttackableTarget(Player, wenn attacking)→NeutralMob-Muster.
  Goal-Geschwindigkeit = alter Wert / moveSpeed (oft 1.0).
- `getAttackStrength`-Tabellen (easy/normal/hard): **Normal-Wert** als `ATTACK_DAMAGE` eintragen – Vanilla skaliert
  Schaden an Spielern selbst (easy d/2+1, hard d*1.5); Kommentar mit Originaltabelle.
- `experienceValue`/`getExperiencePoints`: `this.xpReward` setzen, Feinheiten als Kommentar.
- Größen: `boundingBox.setBounds(...)` im 1.5.2-Konstruktor war wirkungslos (setPosition überschreibt).
  Maßgeblich sind `setSize(w,h)`-Aufrufe; fehlt einer, gilt der Default der Oberklasse (EntityLiving 0.6×1.8).
- NBT: `addAdditionalSaveData(ValueOutput)` / `readAdditionalSaveData(ValueInput)`.
- Despawn: Pack-Konfig `<Mob> Should Despawn`. `Animal` despawnt nie → bei true `removeWhenFarAway` überschreiben
  (`return !this.isTame()` o. ä.); Monster despawnen von selbst → bei false nichts tun (kommt nicht vor).
  `despawnInPeaceful=true` gilt im Pack und traf im Original JEDES EntityGenericAnimal (außer gezähmten):
  **alle** Kreaturen bekommen `notInPeaceful()` im Builder-Lambda — AUSSER zähmbare (Fox, Pferde, …): die statt
  dessen in `customServerAiStep` bei Peaceful und `!isTame()` per `discard()` entfernen, damit Gezähmte bleiben.
- Sounds: `sounds.<name>` im Original → `ZuluSounds.<NAME>` (`sound("snake_case")`). Ogg-Dateien NICHT kopieren,
  nur Event registrieren; sounds.json und Assets macht tools/gen.py. Vanilla-Sounds (z. B. Zombie) direkt verwenden.
- Modelle: ModelBase → `createBodyLayer()` wie ArmadilloModel; Rotationspunkte/Boxen/texOffs exakt übernehmen,
  Bonestruktur (addChild) beibehalten. 64×32 ist Standardgröße. `setRotationAngles(f,f1,…)`:
  f→`state.walkAnimationPos`, f1→`state.walkAnimationSpeed`, f2→`state.ageInTicks`, f3→`state.yRot` (Grad!),
  f4→`state.xRot` (Grad!); Grad→`* Mth.DEG_TO_RAD`.
- `EntityModelRotation`-Easing (Felder `eXXX` am Entity + setLivingAnimations): pro Renderer `EasingCache` +
  `Eased`-Werte im `extractRenderState` steppen, Ergebnis in den RenderState (Vorbild ArmadilloRenderer).
- Zustände, die das Modell braucht (animTime/Angriff, inCover, …): SynchedEntityData-Booleans/Ints am Entity,
  im RenderState spiegeln. Angriffs-`animTime` (maxAnimTime 20, zählt pro Tick runter): im `doHurtTarget`-Override
  (`doHurtTarget(ServerLevel, Entity)`) auf 20 setzen, als EntityData-Int syncen, in `aiStep()` dekrementieren.
  Wo das Modell nur einen Beiß-/Schlag-Schwung braucht, reicht auch Vanilla-`attackAnim` (`state.attackTime`).
- Renderer: `AgeableMobRenderer<T, State, Model>` (auch für Mobs ohne Kinder funktioniert `MobRenderer`; nur bei
  Ageable nötig). Textur `textures/entity/<name>.png` unter Namespace `projectzulu` (gen.py mappt die Originale).
- API unsicher? Erst im Repo nach Vorbildern suchen (mods/natura, mods/harkenscythe, mods/ebxl), dann
  `mcp__minecraft-dev__get_minecraft_source` (version "26.3", mapping "mojmap") für Vanilla-Klassen.
- Nach jedem Batch: `CI=true ./gradlew :projectzulu:compileJava -q` muss grün sein.

## Kanonische Registrierungsnamen

armadillo, sandworm, lizard, lizard_spit, mummy_pharaoh, mummy, vulture, tree_ent, mammoth, fox, boar, mimic,
alligator, frog, penguin, beaver, black_bear, brown_bear, polar_bear, ostrich, rhino, rabbit, red_finch,
green_finch, blue_finch, gorilla, giraffe, elephant, horse_beige, horse_black, horse_brown, horse_dark_black,
horse_dark_brown, horse_grey, horse_white, eagle, horn_bill, pelican, minotaur, haunted_armor, centipede.
(gen.py verlässt sich auf genau diese IDs.)

## Nicht portieren

- Zähm-GUI (Paper-Interaktion, openGui), Pakete (PacketManager), Temperatur-System, horserandom (abgeschaltet),
  EntityZulu/EntityMaster/EntityFollower/EntityHerd (nicht in der Pack-Konfig, spawnen nie).
- `dropFewItems`/Custom-Drops: machen Loot-Tabellen aus gen.py – im Entity nichts implementieren,
  AUSSER zustandsabhängige Drops (`dropCustomDeathLoot`).
- `getCanSpawnHere`-Feinheiten gehören in `ZuluEntities.spawnPlacements` (REPLACE), Biome/Gewichte macht gen.py.

## Batches C, D, E (parallel in eigenen Worktrees)

Jeder Batch registriert NUR in seiner eigenen Klasse (Stubs sind angelegt und verdrahtet):
- C Großtiere: `ZuluBigAnimals.java` + `client/entity/BigAnimalRenderers.java`
- D Monster: `ZuluMonsters.java` + `client/entity/MonsterRenderers.java`
- E Pferde: `ZuluHorses.java` + `client/entity/HorseRenderers.java`

Dort eigene `ENTITIES`/`ITEMS`/`SOUNDS`-Register (gleicher Namespace). Gemeinsame Helfer wiederverwenden, nicht kopieren:
`ZuluEntities.daylitGroundPlacement(event, type)`, `ZuluEntityRenderers.layer(name)`/`baby(supplier)` (paketweit),
`entity/ZuluLandAnimal` (Batch-B-Basis: Flucht/Zorn, Alarm an Artgenossen), `client/entity/LandAnimalRenderer`,
`Eased`/`EasingCache`. Bestehende Klassen anderer Batches NICHT ändern; fehlt etwas, eigene Unterklasse/Kopie im
eigenen Batch und im Bericht erwähnen. texture-map.txt und sound-map.txt nur am Ende anhängen.

# Teil 11 – Blöcke, Items, Tränke, Weltgenerierung

Original: `.reference/projectzulu/src/projectzulu/common/` (`blocks/`, `potion/`, `world/`, `dungeon/`, `core/`,
`ProjectZulu_Blocks.java`, `ProjectZulu_World.java`, `ProjectZulu_Dungeon.java`). Pack-Konfiguration:
`ProjectZuluConfig.cfg` (Block-/Item-IDs, Tränke an, Brauständer ersetzen, Grabstein bei Tod, Blumentopf ersetzen,
Temperatur aus) und `ProjectZuluStructureBiomeConfig.cfg` (Strukturen je Biom).

Gemeinsames Grundgerüst (fertig, nicht umbauen):
- `gen/ZuluIds.java`: alle 1.5.2-Block-IDs (951–971) und Item-IDs (+256) aus der Pack-Konfig.
- `gen/ZuluWorldGen.java`: Generator-Hülle, in worldgen152 als `ProjectZulu|Core` angemeldet, in `tools/expect.sh`
  und `tools/java/Expect.java` eingetragen.
- `ZuluItems`: alle ItemGenerics-Metas als eigene Items (raw_fiber, pulp, salt, shiny_bauble, plant_stalk,
  prickly_powder, powder_slush, glowing_goo, small_/large_unhealthy_heart + die Drop-Items aus Teil 10).
- `ZuluCreativeTabs`: eigener Tab „Project Zulu“ (Icon: Item `projectzulu:campfire`), zeigt automatisch jedes Item
  des Namespaces; Extra-Stacks (Tränke) über `ZuluCreativeTabs.addExtra(...)`.
- `tools/gen.py` führt am Ende alle `tools/gen_*.py` aus (gleicher Namespace: `write`, `n`, `NS`, `lang`, `assets`,
  `simple_item(item, tex, title)`). Jedes Paket schreibt NUR in seine eigene `tools/gen_<paket>.py` (Modelle,
  Blockstates, Lang, Rezepte, Loot-Tabellen, Tags, Texturen über `assets.append((quelle, ziel))`).
  `gen.py` löscht `src/main/resources/{assets,data}` und erzeugt alles neu: nie Dateien von Hand dorthin legen.

## Pakete (parallel, je eigener Worktree)

Jedes Paket registriert nur in seiner Klasse `Zulu<Paket>.java` (+ `client/<Paket>Client.java`, beide angelegt und
verdrahtet) und eigenen neuen Klassen in Unterpaketen (`projectzulu.block`, `projectzulu.item`, `projectzulu.potion`,
`projectzulu.world`, …). Bestehende Dateien anderer Pakete nicht ändern. Rezepte des Originals
(`blocks/ItemBlockRecipeManager.java`) jeweils für die eigenen Items/Blöcke als JSON. Jeder Block mit 1.5.2-ID meldet
im eigenen Register `OldBlockStates.register/registerMeta(ZuluIds.X, …)` an (Metadaten wie im Original).
Kein `runServer` in den Paketen (Ports kollidieren); Prüfung: `python3 mods/projectzulu/tools/gen.py` und
`CI=true ./gradlew :projectzulu:compileJava -q` grün. Den Gesamttest macht die Hauptsession nach dem Zusammenführen.

- **Plants** (`ZuluPlants`, `gen_plants.py`): Aloe Vera + Samen, Watered Dirt, Water Droplet, Tumbleweed,
  Palme komplett (Stamm, Planken, Stufe, Doppelstufe, Treppe, Laub, Setzling mit Wachstum über `OldLiveGen`),
  Kokosnuss-Block + Kokos-Items (Item, Samen, Schale, Milchfragment, Kokosmilch; `CoconutCraftingHandler`),
  Treibsand, Night Bloom, Creeper Blossom (+ `EntityCreeperBlossomPrimed` und Renderer), Universal Flower Pot
  („Replace Default Flower Pot“=true), `RawFiberCraftingHandler`, `ZuluFuelManager`, TreeCapitator-Palme.
  Skills: `wood-sets-263`, `treecapitator-trees`, `worldgen152` (Setzling).
- **Gear** (`ZuluGear`, `gen_gear.py`): 10 Rüstungssets (`ItemZuluArmor`, Temperatur aus), Jasper (`BlockJasper`,
  Sounds `mummy_roar`/`summon_whispers`), Spikes (+ Render), Campfire (+ Render, Gamerule `doesCampfireBurn`),
  Mob-Köpfe (Block, BlockEntity, Renderer, alle Kopf-Modelle aus `blocks/heads`, Drops der Kreaturen), Grabstein
  (BlockEntity mit Text, GUI, Renderer, „Drop Tombstone On Death“=true mit Inventar), Ankh-Wirkung,
  Truhenbeute aus `ItemBlockRecipeManager` (ChestGenHooks) als `gen/ZuluLoot.java` ohne Spielklassen.
  Skills: `client-render-263`, `menu-gui-263`.
- **World** (`ZuluWorld`, `gen/*`, `gen_world.py`): `ZuluWorldGen` bitgenau nach `ProjectZuluGenerator`:
  `StructureManager` (Pyramide, Labyrinth, Friedhof, Oase je `ProjectZuluStructureBiomeConfig.cfg`),
  `WorldGenReplaceTop`, `WorldGenOnSurface`; `OldBlockInfo` aller generierten Zulu-Blöcke; LimitedMobSpawner
  (Block, BlockEntity, Spawn-Logik, Renderer; Editor-GUI nur wenn nötig), Strukturen-Mobs (Pharao, Mimic,
  Minotaurus, Spukrüstung über `setSummoned`/`setHovering`/`setRandomArmor`), Structure Placer.
  Darf `mods/worldgen152` erweitern (eigene Tile-/Entity-Arten für den Chunk-Writer). Skill: `worldgen152`.
- **Potions** (`ZuluPotions`, `gen_potions.py`): 6 Effekte (bubbling/„Shiny!“, incendiary, slowfall, cleansing,
  curse, thorn) mit Wirkung, Tränke (normal/lang/stark, Wurf, Verweil) und Brau-Rezepte aus den 1.5.2-Bitmasken
  (`potionRequirements`, `ItemGenerics`-Effektstrings) übersetzt, „Alter Vanilla Potion Requirements“ und
  „Replace Vanilla Brewing Stand“ (= Zutaten der Ausrüstung brauen) sinngemäß, Null-Potion-Handler, Tränke im Tab.

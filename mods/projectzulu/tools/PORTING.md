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

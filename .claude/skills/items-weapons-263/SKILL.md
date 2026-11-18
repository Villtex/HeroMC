---
name: items-weapons-263
description: Beim Portieren von Waffen, Stäben, Bögen, Geschossen und Spezial-Items eines Hexxit-Mods (1.5.2) auf 26.3 benutzen – ItemSword-Werte, Blocken, Lade-Angriffe, eigene Geschoss-Entity mit Verhalten je Typ, magische Pfeile/Köcher am Vanilla-Bogen, Taschen mit Inventar, Items mit Damage-Varianten und alten IDs, Kapseln (Bereich kopieren/einfügen), Einmal-Spawner mit Spieler-Nähe. Beispiel im Repo: mods/betterdungeons (BDItems, item/*, projectile/*, block/BDSpawnerBlockEntity). Greift auch bei LegendGear, Twilight Forest, Xeno's Reliquary, Harken Scythe.
---

# Waffen, Geschosse und Spezial-Items (1.5.2 → 26.3)

Vorbild: `mods/betterdungeons/src/main/java/betterdungeons/` (`BDItems`, `item/*`, `projectile/*`, `BDEvents`, `BDFx`),
Daten aus `mods/betterdungeons/tools/gen.py`.

## Werte aus 1.5.2

- `ItemSword`: Schaden 4 + Material-Bonus (Holz/Gold 0, Stein 1, Eisen 2, Diamant „EMERALD“ 3). Modifikator = Schaden − 1
  (Grundschaden des Spielers): `p.sword(ToolMaterial.IRON, schaden - 1 - material.attackDamageBonus(), -2.4F)`, dann
  `.durability(n)` (überschreibt; `getMaxDamage()`-Overrides des Originals gewinnen gegen `setMaxDamage`!),
  `.repairable(tag)`, `.component(DataComponents.WEAPON, new Weapon(nutzungenProTreffer))`. Verzauberbar über
  `#minecraft:enchantable/melee_weapon` + `durability`.
- `getDamageVsEntity` bei Nicht-Schwertern (Stäbe): `ItemAttributeModifiers` mit `ATTACK_DAMAGE` = Wert − 1.
- `landMovementFactor -= x` solange gehalten: `MOVEMENT_SPEED` `ADD_VALUE -x` in MAINHAND (Attribut-Liste komplett neu setzen).
- `EnumArmorMaterial`: eigenes `ArmorMaterial` (Verteidigung je Typ von 1.5.2, Enchantability des Konstruktor-Materials,
  nicht von `getArmorMaterial()`!), Haltbarkeit danach mit `.durability(n)`. Equipment-JSON siehe Skill `client-render-263`.
- Items ohne `setMaxStackSize` stapeln bis 64 (auch Schilde/Banner von Better Dungeons) – so lassen.

## Blocken und „solange benutzt“

- `EnumAction.block` (Schild, Banner, Schwert mit eigenem Effekt beim Blocken): Komponente `BLOCKS_ATTACKS` mit
  `new DamageReduction(180F, Optional.empty(), -0.5F, 0.5F)` ergibt den 1.5.2-Wert (1 + Schaden) / 2, Item-Schaden aus
  (`ItemDamageFunction(1e9F, 0, 0)`). Damit liefern `use`/Animation/Dauer schon die Vanilla-Defaults (`BDItems.blocking`).
- Normale Schwerter (`p.sword(...)`, ohne eigenes `use`) blocken schon über `mods/combat152` (1.5.2-Kampfregeln fürs
  ganze Pack): dort nichts nachbauen. `BLOCKS_ATTACKS` selbst nur bei Items mit eigenem Effekt beim Blocken.
- `onUpdate` mit `isUsingItem && aktuelles Item` → `onUseTick` (beide Seiten, Effekte nur Server). Effekte mit 1 Tick
  Dauer mindestens 2 Ticks geben.
- `inventoryTick` läuft nur auf dem Server (`ServerLevel`): Partikel dort über `ServerLevel.sendParticles(..., 0, vx, vy, vz, 1.0)`
  (Zähler 0 = Geschwindigkeit wie 1.5.2), Helfer `BDFx.particle`.
- Rüstungs-Ticks (`onArmorTickUpdate`): `PlayerTickEvent.Post` auf beiden Seiten; Bewegung des Spielers zählt auf dem
  Client. Sprungtaste am Server: `ServerPlayer.getLastClientInput().jump()` (`BDFx.isJumping`).
- `onLeftClickEntity`: dieselbe Methode in `IItemExtension` (NeoForge), einfach überschreiben (beide Seiten, true bricht ab).

## Lade-Angriffe (Speer, großes Schwert)

`releaseUsing` mit gecachtem Schaden: transienten `ATTACK_DAMAGE`-Modifikator (Zielwert − aktueller Wert) setzen,
`player.attack(target)`, Modifikator entfernen (`BDItemUtil.attackWithDamage`). `player.swing(hand, SwingAnimation.DEFAULT, true)`.

## Eigene Geschoss-Entity mit Typen (EntityBaseBall-Muster)

- Eine Entity (`extends Projectile`), Typ und Stufe als `SynchedEntityData`-Bytes, Verhalten je Typ als Objekt, das
  erst beim ersten Zugriff gewählt wird (auf dem Client kommt der Typ nach dem Spawn-Paket: neu wählen, solange Dummy).
- Eigener `tick()` wie 1.5.2 (Strahl `ClipContext.Block.OUTLINE`, Entities im Kasten + `inflate(0.3).clip`), Logik auf
  beiden Seiten, Seiteneffekte nur Server. Größe je Typ über `getDimensions` + `refreshDimensions()`.
- **`e.isAlive()` beim Treffer prüfen**: sterbende Mobs bleiben 20 Ticks pickable und fangen sonst Geschosse ab.
- Besitzer: `setOwner`; `getAddEntityPacket` von `Projectile` schickt ihn mit (Seil-Renderer braucht ihn am Client).
- Nicht speichern: `shouldBeSaved()` false (1.5.2 tötete sie beim Laden). Reiten (Netz, Blase, Haken): `startRiding(x, true, true)`.
- Fabrikmethoden für die Mob-KI dokumentieren (`BDProjectile.spawn(shooter, ProjectileType.X, lvl)`, `spawnAimed`).
- Pfeil-Varianten: `extends AbstractArrow`, Effekt in `doPostHurtEffects`, Rückstoß in `doKnockback`, Pickup `Items.ARROW`;
  `setArrowHeading` von 1.5.2 hatte Streuung 0.0075 (heute 0.0172) → eigene `heading`.
- Renderer: Billboard `poseStack.rotate(camera.orientation)` + `submitCustomGeometry(..., RenderTypes.entityTranslucent(tex), ...)`
  mit UV aus dem 256er-Sprite-Blatt; Seile mit `RenderTypes.lines()` (Vorbild `FishingHookRenderer`).

## Bogen, magische Pfeile, Köcher

`BowItem.releaseUsing` bricht vor `ArrowLooseEvent` ab, wenn `getProjectile` leer ist: `LivingGetProjectileEvent` liefert
eine Ersatz-Munition (Kopie), `ArrowLooseEvent` (abbrechen = kein Bogenschaden, wie 1.5.2) schießt dann selbst
(`BDEvents`). Pfeile nicht in `#minecraft:arrows` aufnehmen, wenn das Original sie nur im eigenen Code nutzte.

## Taschen / Köcher

Inhalt in `DataComponents.CONTAINER`; Menü = `new ChestMenu(MenuType.GENERIC_9x1, id, inv, container, 1)` (Client nutzt
den Vanilla-Screen, Inventory Tweaks erkennt ihn), `SimpleContainer.setChanged` schreibt zurück, eigenen Slot in
`clicked` sperren, `removed` wirft Unerlaubtes ab (`player.drop(stack, false, Prediction.SERVER_ONLY)`). `BagItem`.

## Damage-Varianten und alte IDs

Jede Damage-Variante ein eigenes Item; `OldGameIds.registerItem(id + 256, damage, "mod:item")` (ohne Damage: `-1`-Form),
damit Kisten der Weltgenerierung sie füllen. Item-Damage als Zustand (Cooldown, Entity-ID) → `CUSTOM_DATA` oder
echte Haltbarkeit; Item-Modell per `minecraft:has_component`-Bedingung umschalten, Färbung `minecraft:constant`-Tints.

## Kapseln (Bereich ins Item)

`StructureTemplate.fillFromWorld(level, ecke, größe, true, List.of())` → `save(new CompoundTag())` in `CUSTOM_DATA`;
leeren: erst `Clearable.clearContent()` der Block-Entities, dann Luft mit `UPDATE_CLIENTS | UPDATE_KNOWN_SHAPE |
UPDATE_SUPPRESS_DROPS`; einfügen: `template.load(level.holderLookup(Registries.BLOCK), tag)` + `placeInWorld(...)` (`CapsuleData`).

## Einmal-Spawner mit Spieler-Nähe

BlockEntity-Ticker (`createTickerHelper`), `getNearestPlayer(..., 16, false)` ohne Kreativ, Countdown, Mob erzeugen,
`levelEvent(LevelEvent.PARTICLES_MOBBLOCK_SPAWN, pos, 0)`, Block zu Luft. Testbar ohne Spieler über eine öffentliche
`tickWithPlayer`-Methode (`BDSpawnerBlockEntity`) aus einem temporären Testbefehl.

## Prüfen

- FakePlayer als Schütze: Testmob nach `kill` erst 1,5 s später rufen (sterbende Mobs fangen Treffer), Zombies brauchen
  `time set midnight` (Sonnenbrand verfälscht Lebenspunkte), Zombie-Rüstung 2 senkt Schaden (8 → 7,87).
- Werte aller Items mit einem Testbefehl ausgeben (`getMaxDamage`, `forEachModifier`, `ENCHANTABLE`) und gegen 1.5.2 lesen.

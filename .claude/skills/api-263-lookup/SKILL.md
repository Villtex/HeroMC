---
name: api-263-lookup
description: Beim Schreiben von 26.3-Code für einen Port benutzen, wenn eine Klasse/Methode/ein Feld nicht gefunden wird oder das Datenformat (Loot, Advancements, Prädikate) unklar ist; auch bei Texturnamen mit Großbuchstaben und ICraftingHandlern (ItemCraftedEvent) – Signaturen und Vanilla-Daten von 26.3 nachschlagen statt raten, plus die Umbenennungen, auf die Teil 19 (LegendGear, Falling Meteors) gestoßen ist. Auch für WAV-Sounds aus 1.5.2-Archiven.
---

# 26.3 nachschlagen statt raten

## Werkzeuge (lokal, ohne Netz)

```
J=/root/.gradle/caches/neoformruntime/intermediate_results/applyDevTransforms_*_output.jar   # Minecraft, Mojang-Namen
N=/root/.gradle/caches/modules-2/files-2.1/net.neoforged/neoforge/26.3.0.102/*/neoforge-26.3.0.102-universal.jar
javap -cp $J:$N net.minecraft.world.item.Item | grep use       # Signaturen
unzip -l $J | grep '/ItemOwner.class'                           # Paket einer Klasse finden
# Quelltext einer Klasse: Klasse(n) aus $J entpacken, dann
java -jar .cache/tools/vineflower.jar -log=ERROR <in-dir> <out-dir>
unzip -p $(ls .../neoforge-26.3.0.102-sources.jar) net/neoforged/neoforge/event/.../X.java   # NeoForge-Events
```
Vanilla-Daten (Loot, Advancements, Rezepte) als JSON: `/root/.config/minecraft-dev-mcp/decompiled/26.3/mojmap/data/minecraft/`
(vom minecraft-dev-MCP angelegt; sonst `mcp__minecraft-dev__get_minecraft_source`). Vor dem Schreiben eigener JSON-Dateien
eine passende Vanilla-Datei ansehen. Erst ein Compile-Lauf (`CI=true ./gradlew :<mod>:compileJava -q 2>&1 | grep -A3 error:`)
zeigt meist alle Abweichungen auf einmal – dann gesammelt korrigieren.

## Datenformate

- Loot: Bedingungen unter `"condition"` (ein Objekt; mehrere über `{"type":"minecraft:all_of","terms":[...]}`), Funktionen
  unter `"modifier"`, Diskriminator überall `"type"` (siehe auch `mobs-263`). Blockzustand: `minecraft:match_block` mit
  `"blocks"` und `"state"`. Zahlen in `enchanted_count_increase` brauchen `{"type":"minecraft:uniform",...}`. Falsche
  Tabellen brechen den Serverstart ab (Registry loading errors) – das ist der Test.
- Advancement-Kriterien: `conditions.entity` ist **ein** Prädikat-Objekt
  `{"type":"minecraft:entity_properties","entity":"this","predicate":{"minecraft:entity_type":"ns:id"}}`.
  Nur per Code vergebene Erfolge: Kriterium `{"trigger":"minecraft:impossible"}`, im Code
  `player.getAdvancements().award(holder, "<kriterium>")`.

## Umbenennungen (1.21 → 26.3), die schon vorkamen

| alt / erwartet | 26.3 |
|---|---|
| `PushReaction.DESTROY` / `BLOCK` | `POPPED` / `IMMOVEABLE` |
| `Blocks.WHITE_WOOL` … | `Blocks.WOOL.white()`, `Blocks.WOOL.asList()` |
| `entity.hurtMarked = true` | `entity.needsSync = true` |
| `entity.invulnerableTime = n` | `setInvulnerableTime(n)` |
| `EntityType.LIGHTNING_BOLT` | `EntityTypes.LIGHTNING_BOLT` |
| `player.drop(stack, false)` | `drop(stack, false, Prediction.SERVER_ONLY)` |
| `swing(hand)` | `swing(hand, SwingAnimation.DEFAULT, true)` (`item.component.SwingAnimation`) |
| `EnderMan` | `Enderman` |
| `FarmBlock` | `FarmlandBlock` |
| `simpleCodec`/`codec()` an `BaseEntityBlock` | entfällt |
| `dimensionType().bedWorks()` | z. B. `level.dimension() == Level.OVERWORLD` (canRespawnHere von 1.5.2) |
| `ModConfig.Type.SERVER` | gibt es nicht; `STARTUP` (feste Pack-Werte) |
| `ItemOwner` | `net.minecraft.world.entity.ItemOwner` |
| `AbstractArrow.getBaseDamage()` | privat, ohne Getter (Reflection auf `baseDamage`) |
| `TriState` | `net.minecraft.util.TriState` |

Private Synch-Daten (z. B. `Creeper.DATA_IS_POWERED`): zur Laufzeit gelten Mojang-Namen, Reflection auf das Feld geht.

## Ressourcenpfade nur klein

`Identifier` erlaubt im Pfad nur `[a-z0-9/._-]`: 1.5.2-Texturnamen wie `itemHeadband.png` beim Import klein bzw. in
snake_case schreiben (Ziel in `original-assets.txt` und alle Verweise in Modellen). Teil 19 hatte das übersehen; die
`gen.py` von LegendGear und Meteors schreiben eigene Ids jetzt über `_lower` klein.

## ICraftingHandler (onCrafting) von 1.5.2

`PlayerEvent.ItemCraftedEvent` (NeoForge-Bus) kommt wie damals **vor** dem Verbrauch des Rasters (`ResultSlot.checkTakeAchievements`,
auch beim Shift-Klick einmal je Fertigung): Raster ändern (ganzen Stapel verbrauchen = Stapel auf 1 setzen), Belohnung
nur auf dem Server geben. Der Crafter-Block löst es nicht aus. Ergebnis mit Daten aus den Zutaten lieber als
`CustomRecipe` (`assemble`), sonst stimmt es beim Shift-Klick nicht (Vorlage `xreliquary`: `XREvents`, `SatchelRecipe`).

## WAV-Sounds aus 1.5.2

26.3 spielt nur Ogg. `buildSrc` (`ImportOriginalAssets`) wandelt beim Import jede `*.wav`, deren Ziel auf `.ogg` endet, mit
`ffmpeg -c:a libvorbis` um: in `original-assets.txt` einfach `meteor/sound/x.wav -> assets/<ns>/sounds/x.ogg` schreiben.

## Eigene Flüssigkeiten (FluidType): Entities frieren ein

NeoForge 26.3 bewegt ein Lebewesen in einer eigenen Flüssigkeit **gar nicht** (weder Schwerkraft noch Laufen), solange
`FluidType.move` `false` liefert und die Flüssigkeit nicht `isWaterLike(true)` ist (`LivingEntity.travelInFluid`).
- 1.5.2 `Material.water` → `FluidType.Properties...isWaterLike(true)` (Schwimmen wie in Wasser).
- Eigene `MaterialLiquid` (weder Wasser noch Lava) → `FluidType` ableiten und `move` wie Luft überschreiben
  (Vorlage `tconstruct.smeltery.TFluids.MoltenType`).
- Prüfen am Testserver: Mob über einem Becken beschwören, `data get entity … Pos` mehrmals, Y muss sinken.
- Gegenstände (`ItemEntity`) schwimmen in 26.3 an der Wasseroberfläche (1.5.2: sie sanken). Logik, die den Block
  unter einem Gegenstand im Wasser braucht (Twilight-Portal), muss eine Höhe tiefer schauen, wenn dort Wasser ist.

## Maustasten zählen ab 1

26.3 (SDL3) nummeriert Maustasten **ab 1**: `InputConstants.MOUSE_BUTTON_LEFT = 1`, `MIDDLE = 2`, `RIGHT = 3`.
`event.button() == 0` trifft nie; immer die Konstanten benutzen (Teil 26-Nachlauf: Enchanting Plus, MapWriter,
Tinkers-Schmelze reagierten deshalb nicht auf Klicks). Slot-Klicks an das Menü (`handleInventoryMouseClick`, PICKUP
mit 0/1) sind davon nicht betroffen.

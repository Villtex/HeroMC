---
name: server-tests-263
description: Verhalten eines portierten Mods ohne Spieler am Testserver (./gradlew runServer) prüfen – Rezepte über Crafter + RCON, Kampf/Ernte/Rituale über einen temporären FakePlayer-Befehl, ticken lassen in forceloaded Chunks. Greift bei Schritt 5 „Prüfen“ jedes Teils, wenn kein Client da ist.
---

# Verhalten ohne Spieler prüfen (26.3)

Server: `./gradlew runServer` (RCON in `run/server/server.properties`: `enable-rcon=true`, Passwort `hexxit`,
`pause-when-empty-seconds=-1`), Befehle mit `python3 tools/rcon.py '<befehl>'`, beenden mit `tools/rcon.py stop`.
Log nicht selbst lesen: `grep -E "ERROR|Exception" run/server/logs/latest.log` oder Subagent `log-reader`.

## Ticken ohne Spieler

- Entities und Zufallsticks laufen nur in geladenen Chunks: `forceload add <x1> <z1> <x2> <z2>` zuerst.
- `/summon <ns>:<mob> x y z {…}` lädt aus NBT: Ausrüstung aus dem Konstruktor zusätzlich in `finalizeSpawn` setzen,
  sonst fehlt sie nach `/summon`.
- Zustand lesen: `data get entity @e[type=<ns>:<mob>,limit=1] Health`, `data get block x y z`.
- Schaden ohne Spieler: `/damage <ziel> <n> minecraft:mob_attack by <angreifer>` (Angreifer z. B. Zombie mit
  `equipment:{mainhand:{id:…}}`) – löst `LivingIncomingDamageEvent`/Drops mit Waffen-Verzauberungen aus.

## Rezepte über einen Crafter

```
setblock 0 64 0 minecraft:crafter[orientation=up_east]
data merge block 0 64 0 {Items:[{Slot:0b,id:"…",count:1},…]}   # Slots 0–8 zeilenweise
setblock 0 63 0 minecraft:redstone_block                         # Puls → Ergebnis fällt oben heraus
data get entity @e[type=item,sort=nearest,limit=1,x=0,y=65,z=0] Item
```
Prüft auch Rest-Items (`getCraftingRemainder`), Komponenten-Zutaten (`neoforge:components`) und `CustomRecipe`.
Vor jedem Test Item-Entities löschen: `kill @e[type=item]`.

## Temporärer Testbefehl mit FakePlayer

Für Rechtsklick, Lade-Angriffe, Menüs, Rituale: einen Befehl (`RegisterCommandsEvent`) im Mod anlegen, der
`FakePlayerFactory.get(level, profile)` nutzt, und **nie committen** (vor dem Commit `git diff` prüfen).
- Der FakePlayer tickt nicht: keine Equipment-Attribute (Waffenschaden), keine `inventoryTick`-Zähler →
  Item-Logik direkt aufrufen (`item.use`, `releaseUsing`, `hurtEnemy`) und Attribute ggf. von Hand setzen.
- Menü-Logik: Menü mit dem FakePlayer erzeugen und `clickMenuButton(player, id)` aufrufen.
- Zufallsverteilungen (Altar-Würfel, Drops) mit 10.000 Wiederholungen im Befehl auszählen und mit dem Original
  vergleichen.

## Fremde, unfertige Teilprojekte

Startet der Server wegen eines unfertigen anderen Teils nicht: dessen `mods/<x>/gradle.properties` vorübergehend
wegschieben (dann ist es kein Teilprojekt mehr), `run/server/mods/<x>*` löschen, testen, Datei zurücklegen.

## Stolperfallen

- Vor `runServer` mit `pgrep -af fml.modFolders` prüfen, ob noch ein Server (z. B. eines abgebrochenen Agents) läuft.
  Er schreibt sonst in die frisch gelöschte Welt, der neue Start bricht mit „Overworld settings missing“ ab.
  Beenden mit `python3 tools/rcon.py --port <rcon> stop`.
- `loot spawn … mine` prüft `requiresCorrectToolForDrops` nicht: kein Test für die Werkzeugstufe.

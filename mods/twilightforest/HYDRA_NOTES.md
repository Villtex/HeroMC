# Hydra (Teil 24f) – Notizen für TODO.md

Stand: Entities, Client, Daten fertig und übersetzt; **nichts davon im Spiel geprüft** (kein Server-/Client-Lauf),
alles nur durch Lesen des Codes gegen 1.5.2 abgeglichen.

## Aufbau

- `entity/boss/hydra/Hydra` (EntityTFHydra, `Mob` + `Enemy`), `HydraHeadContainer` (Zustandsmaschine der Köpfe),
  `HydraHead` (EntityTFHydraHead: echte Entity `twilightforest:hydra_head`, gespeichert, per Teilname „head0..6“ wieder
  gefunden), `HydraPart` (NeoForge-`PartEntity`: Körper, Schwanz und je Kopf 5 Halsglieder = 37 Teile),
  `HydraMortar` (`twilightforest:hydra_mortar`, eigener Flug wie EntityThrowable von 1.5.2).
- Client: `client/entity/hydra/*` – Körper-Renderer, Kopf-Renderer zeichnet wie RenderTFHydraHead auch die fünf Hälse,
  Mörser-Renderer, eigenes Partikel `twilightforest:large_flame` (EntityTFLargeFlameFX, Flammenatem).
- Registrierung nur in `TFHydra` (Entities, Ei, 5 Sounds, Partikel, Attribute), Daten in `tools/gen_hydra.py`
  (schreibt `sounds.json` nach gen_mobs.py noch einmal mit den Hydra-Sounds).

## Einzuhängen (Hauptsession)

- `TFHydra.TAB_ITEMS` (Spawn-Ei) in den Kreativ-Tab, `TFHydra.registerOld()` aufrufen.
- Boss-Spawner: In 26.3 heißt `restrictTo` jetzt `Mob.setHomeTo(BlockPos, int)` (`setHomeTo(spawnerPos, 50)`).
  Die Hydra nutzt die Heimat nicht (sie läuft nie), wie in 1.5.2.
- **Loot-Tabelle verweist auf `twilightforest:hydra_trophy`**, das es noch nicht gibt: bis das Item registriert ist,
  lädt `loot_table/entities/hydra.json` nicht (je nach Stand bricht das den Serverstart ab, vgl. api-263-lookup).
- Erfolg `twilightHunter` beim Tod (TFAchievementPage) fehlt noch (gibt es im Port schon Fortschritte?).

## Bewusste Abweichungen von 1.5.2

- Brüllen beim Nachwachsen (Zustand 16): 1.5.2 startete den Sound jeden Tick 50-mal neu (`didRoar` wurde nie gesetzt),
  hier einmal je Brüllen.
- XP (511): 1.5.2 fragte bei deathTime 200 `recentlyHit` ab, das 100 Ticks nach dem letzten Treffer abgelaufen war –
  die Hydra gab nie XP. Hier gibt es sie bei deathTime 200, wenn ein Spieler sie getötet hat.
- Beute: 1.5.2 warf `rand(3 + looting) + 5`-mal **Stapel zu 5** Hydra-Koteletts (dropItem(id, 5)). Umgesetzt als
  5–7 Würfe zu 5 Stück plus 0..5 Stück je Looting-Stufe (gleicher Mittelwert, nicht in Fünferschritten).
- Körper/Schwanz schieben nur Nicht-Spieler weg: in 1.5.2 kam addVelocity auf dem Server nie beim Spieler an
  (kein Geschwindigkeitspaket ohne Treffer). Die 16×12-Box der Hydra selbst schiebt nichts (`pushEntities` leer).
- Blöcke zerstören: zusätzlich zu Obsidian/Endstein/Grundgestein bleiben alle unzerstörbaren Blöcke (Härte < 0) stehen;
  Spielregel mobGriefing wird wie in 1.5.2 nicht gefragt. „Fester Boden“ (Material.isSolid) = `BlockState.isSolid()`.
- Mörser-Explosion mit `ExplosionInteraction.MOB` (beachtet mobGriefing; 1.5.2 zerstörte immer).
- Schaden: 1.5.2 rechnete in ganzen Zahlen; hier wird der float-Schaden erst gerundet, dann /8 gerundet (Panzer).
  Schwache Treffer (< 4) machen dadurch wie früher 0 Schaden.
- `/kill` wirkt auf Hydra und Köpfe (sonst wäre die Hydra unverwundbar gegen Befehle); ein Kopf stirbt dabei allein.
- Die Beine (EntityDragonPart „leg“) wurden in 1.5.2 nie platziert (lagen bei 0,0,0) und sind weggelassen.
- Halsglieder: kein eigener Schatten (1.5.2 zeichnete unter jedem Glied einen); Gieren der Glieder interpoliert
  (1.5.2 hinkte einen Tick hinterher, ohne Interpolation).
- Größe der Köpfe (4×4 aktiv, sonst 1×1) aus dem synchronisierten Kopfzustand statt aus dem (auf dem Client nie
  aktualisierten) Container-Zustand.
- Sounds der Köpfe (Feueratem, Brüllen, Warnung) nur vom Server (1.5.2 spielte sie zusätzlich lokal auf dem Client).
- Mörser-Aufblitzen: Weiß-Overlay wie beim TNT statt zweitem untexturiertem, additiv geblendetem Durchgang.
- Boss-Leiste über `ServerBossEvent` (rosa, wie die alte BossStatus-Leiste), sichtbar für alle, die die Hydra verfolgen
  (1.5.2: nur solange sie gezeichnet wurde).
- Gegner-Suche wie 1.5.2 mit nächstem Spieler in 48 (auch Kreativ); Nebenziele: alle LivingEntity in 20 außer
  Zuschauern, Hydra und Köpfen.

## Im Client zu prüfen

- Modelle (Körper, Kopf mit Kiefer/Krause, Hals, Mörser): Lage, Drehrichtung von Kopf-Gieren/-Nicken, Kiefer öffnet
  nach unten, Hälse verbinden Körper und Kopf ohne Lücken, Halsglieder drehen in die richtige Richtung.
- Sterbender Kopf kippt 90° zur Seite und verschwindet nach 20 Ticks, Explosionen wandern den Hals entlang.
- Rotes Aufleuchten beim Treffer (Körper, Köpfe, Hälse) und 200 Ticks lang beim Tod der Hydra.
- Flammenatem (Partikel `large_flame`, 5-fach groß), Mörser schwillt/blinkt und brennt, Boss-Leiste.
- Treffen der Teile mit Nahkampf und Pfeilen (PartEntity-IDs: Server vergibt sie fortlaufend nach der Hydra,
  der Client setzt id + 1 … id + 37 – wie beim Enderdrachen).

## Ohne Spieler zu prüfen (Server)

- `/summon twilightforest:hydra` → 7 Köpfe `hydra_head` erscheinen, 3 aktiv; Speichern/Laden findet die Köpfe wieder
  (`NumHeads`, `SpawnHeads`).
- Kopf töten (> 120 Schaden) → stirbt 70 Ticks, wächst nach 100 Ticks mit einem zweiten nach.
- `loot spawn … kill @e[type=twilightforest:hydra,limit=1]` (erst, wenn `hydra_trophy` existiert).

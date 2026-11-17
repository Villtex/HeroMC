# Harken Scythe: Mobs (Teil 9, Teilauftrag 3) – Stand

Portiert in `harkenscythe.mob` (Server) und `harkenscythe.client.mob` (Modelle/Renderer); Registrierung, Spawn-Eier,
Attribute, Spawn-Platzierung und die Hooks von `entity.SpectralPets` in `mob.HSMobs` (im Mod-Konstruktor
`HSMobs.register`). Daten über `tools/gen.py` (Abschnitt „Teil 9, part 3“), geprüft von `tools/check.py`.

- `SpectralPet` (TamableAnimal): gemeinsames Verhalten aller `EntityHSSpectral*` (Lebensdauer −1 HP/200 Ticks,
  Zähmen mit Creepball, Heilen mit Blutkeeper/-gefäß, Sitzen, halber Schaden von Mobs, Biss 1/2, Minion „Evil“).
  Ziele werden im Konstruktor über `setupGoals()` angelegt (nicht in `registerGoals`, das vor den Feldern läuft).
- Einfache Arten: `SimpleSpectral` (Huhn, Kuh, Dorfbewohner, Silberfisch); bewaffnete: `SpectralHumanoid`
  (Zombie, Pigman, Wither-Skelett: Schwert; Skelett: Bogen; Hexe: Trank; Bergarbeiter: Bogen/Schwert/Fackel/Rüstung).
- `Harbinger`, `EvilSpectralMiner` (Monster), `EssenceSlime` (Slime: Ektoplasma/Hämoglobin).
- Biome-Modifier `spawn_harbinger` (13 Biome, Gewicht 1 → 10), Loot-Tabellen `entities/harbinger`,
  `entities/spectral_miner_evil`; Sattel, Waffen, Rüstung, getragene Blöcke in Java (`dropCustomDeathLoot`).

## Abweichungen (bewusst)

- Waffen/Tränke geben keinen Zusatzschaden (wie im Original: `getAttackStrength` wurde nie benutzt).
- Gespeichert wird zusätzlich: Halsband (Wolf), Katzenfell (Ozelot), Wert der Schleime (1.5.2 verlor sie beim Speichern).
- Harbinger: das Teleportieren zum Ziel nutzte `entityToAttack` (nie gesetzt bei KI-Mobs) und fehlt daher.
- Schweinesteuerung über die 26.3-Reitlogik (ohne Boost); Hexe wirft Wurftränke über `ThrownSplashPotion`.
- Sound „damage.fall“ des Bergarbeiters gab es in 1.5.2 nicht: stumm. Variantensounds (Huhn, Kuh, …) per Registry-ID.

## Offen

- Client-Prüfung offen: Renderer starten fehlerfrei bis zum Titelbildschirm, Aussehen/Animationen in der Welt nicht
  gesehen. Nicht gezeichnet: Schweinesattel (die 1.5.2-Textur `/mob/saddle.png` fehlt, 26.3 hat ein anderes Layout),
  Block in der Hand des Enderman, Trank in der Hand der Hexe.
- Zähmen/Interaktionen mit Spieler (Creepball, Waffen geben, Keeper auf verlorener Seele) nicht per RCON prüfbar.

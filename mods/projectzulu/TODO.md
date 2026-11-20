# Project Zulu – offene Punkte

## Paket World (Teil 11)

- **Structure Placer** (`ItemStructurePlacer`, Item-ID 5097 + 256, Damage 0 Oase, 1 Pyramide, 2 Labyrinth, 3 Friedhof):
  nicht portiert. Plan: Item mit 4 Varianten, Bogen-Animation, 16 Ticks; beim Loslassen
  `StructureManager.tryToGenerateSpecificStructure` an der Spielerposition (y − 1, Oase/Labyrinth/Friedhof y). Dafür
  muss `OldLiveGen` außer Blöcken auch Tile-Daten (Truhen, Spawner, `OldTile.Mod`) und Entities schreiben (neue
  Methode neben `generate`), Reichweite Pyramide ±26/−13/+27, Labyrinth ±22 bis +31 in z, −12/+16.
  `world.rand` des Originals: `level.getRandom()`.
- **Limited Mob Spawner, Editor**: `GuiLimitedMobSpawner` (NBT-Baum, Spawn-Listen, Debug-Modus, Pakete) fehlt. Ersatz:
  ein Spawn-Ei setzt den Mob (Interface `Spawner`); Werte sonst per `/data`. `SpawnSound` einer SpawnData (Datei aus
  `config/Project Zulu/CustomResources`) wird nicht abgespielt.
- **ProjectZulu|Dungeon-Befehle** `/playsound`, `/streamsound`, `/spawnentity`, `/placeblock`: nicht portiert.
- **Schweben der Strukturmobs**: `MimicEntity`/`HauntedArmorEntity.setHovering` wird nicht gespeichert; da der
  Chunk-Writer Entities als NBT in den Chunk legt, ist das Flag beim Laden weg (die Mobs stehen ohnehin auf dem Boden
  ihrer Zelle). Braucht ein gespeichertes Feld in den Entity-Klassen (Paket Monster).

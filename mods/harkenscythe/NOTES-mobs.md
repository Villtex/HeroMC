# Harken Scythe: Schnittstellen für die Mobs (Teil 9, Teilauftrag 3)

Noch nicht portiert: alle `EntityHSSpectral*`, `EntityHSSpectralMinerEvil`, `EntityHSHarbinger`, `EntityHSSlime*`
(Ektoplasma/Hämoglobin), `EntityAIHS*`, ihre Modelle und Renderer, Spawn-Eier, der Harbinger-Spawn
(`addSpawn(EntityHSHarbinger, 1, 0, 1, monster, …)`, dieselben 13 Biome wie die verlorene Seele: Biome-Modifier
wie `data/harkenscythe_core/neoforge/biome_modifier/spawn_soul_lost.json`, Gewicht 1 → 10).

## Schnittstelle `harkenscythe.entity.SpectralPets`

Teilauftrag 3 setzt im Mod-Konstruktor `SpectralPets.hooks = new SpectralPets.Hooks() { ... }` (alle Methoden haben
Standard-Implementierungen, die nichts tun):

| Hook | Aufrufer im Kern | Original |
|---|---|---|
| `create(fallenSoulName, level, x, y, z, random, needsMaster)` | Necronomicon (`NecronomiconItem`) | `EntityHSSoul.createSpectralPet` (Namen: Chicken, Cow, Sheep, Bat, Pig, Wolf, Cat_Black/Red/Siamese, Ocelot, Squid, Villager, Creeper, Zombie, PigZombie, Skeleton, Skeleton_Wither, Witch, Spider, CaveSpider, Silverfish, Enderman, Blaze, Ghast; siehe `entity.FallenSoul`) |
| `createMiner(level, x, y, z, random)` | Necronomicon auf verlorener Seele | `EntityHSSoulLost.createSpectralPet` (EntityHSSpectralMiner) |
| `spawnEvilMiner(level, x, y, z)` | Keeper auf verlorener Seele (`LostSoulEntity.mobInteract`) | `EntityHSSoulLost.interact` (EntityHSSpectralMinerEvil); nur bei `true` verschwindet die Seele |
| `spawnEssenceSlime(level, x, y, z, size, worth, blood)` | abgebauter Tiegel (`CrucibleBlock`) | `BlockHS*Crucible.breakBlock` (EntityHSSlimeSoul/Blood, `setResourceWorthTotal`) |
| `harbingerSoulReap(harbinger, victim)` | `HSEvents.livingDrops`, wenn der Angreifer im Tag `harbingers` ist | `EventHookContainerClassHarkenScythe.harbingerSoulReap` |
| `keepsSoulLight(entity)` | `SoulLightBlock.randomTick` | Spektraler Bergarbeiter mit Fackel in der Hand |

Für `harbingerSoulReap` gibt es `HSEvents.spawnSoul(level, HSEntities.SOUL_GRIEVING.get(), victim, 1)` und
`HSEvents.soulReaping(...)`; `SoulEntity.getFallenSoulName()` liefert den Namen für `create`.

## Entity-Tags (leer, von Teilauftrag 3 zu füllen)

- `harkenscythe_core:spectral_pets`: alle Spektral-Tiere inkl. Spektral-Bergarbeiter (`isSpectralPet`: kein Blut, keine Seele).
- `harkenscythe_core:harbingers`: der Harbinger (darf Seelen verletzen; seine Kills gehen an `harbingerSoulReap`;
  getötet hinterlässt er eine „culled“ Seele, Wert 10).
- `harkenscythe_core:soulless`: Ektoplasma-/Hämoglobin-Schleim und der böse Spektral-Bergarbeiter (keine Seele).

Seelen-Entitäten (fertig): `soul_common`, `soul_grieving`, `soul_culled`, `soul_wrathful` (`SoulEntity`, nur vom
Harbinger verletzbar) und `soul_lost` (`LostSoulEntity`). Texturen der Mobs unter `models/mob/` im Archiv, noch
nicht in `original-assets.txt`.

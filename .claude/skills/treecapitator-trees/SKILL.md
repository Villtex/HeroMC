---
name: treecapitator-trees
description: Beim Portieren eines Hexxit-Mods mit eigenen Bäumen, Riesenpilzen oder Äxten benutzen, damit TreeCapitator (mods/treecapitator) sie fällt bzw. als Axt erkennt. Greift bei Natura (Teil 7), Project Zulu (Palme, Teil 11), Harken Scythe (Livingmetal-/Biomass-Axt, Teil 9), Falling Meteors (Frezarite-/Meteor-Axt, Teil 19), Twilight Forest, Tinkers' Construct.
---

# Bäume und Äxte bei TreeCapitator anmelden

TreeCapitator 1.5.2.r14 kannte die Bäume der anderen Mods aus seiner Konfiguration (`TreeCapitator.cfg`, Abschnitt
`tree_and_mod_configs.<mod>` mit `logConfigKeys`/`leafConfigKeys` und `axeIDList`). Im Port sind das Daten.

## Bäume: Daten-Registry `treecapitator:tree`

Eine Datei je Baum im Jar des **neuen** Mods (oder in `mods/treecapitator`, wenn der Mod schon existiert):

```
mods/<mod>/src/main/resources/data/<modid>/treecapitator/tree/<schlüssel>.json
```

```json
{ "logs": ["natura:eucalyptus_log", "natura:eucalyptus_wood"],
  "leaves": ["natura:eucalyptus_leaves"],
  "maxHorLeafBreakDist": 6, "requireLeafDecayCheck": false }
```

- `<schlüssel>` = Baumname aus der Pack-Konfiguration (`.reference/z-hexxit-configs/pack-files/config/TreeCapitator.cfg`,
  Abschnitt `tree_and_mod_configs.<mod>`, z. B. `eucalyptus`, `sakura`, `projectzulu_palm`). Gleicher Schlüssel in
  mehreren Namespaces = ein Baum (so hängt ExtrabiomesXL Herbstlaub an `vanilla_oak`, siehe
  `mods/treecapitator/src/main/resources/data/extrabiomesxl/treecapitator/tree/`).
- Einträge: `ns:block`, `ns:block[eigenschaft=wert]` (nur diese Eigenschaften müssen passen, Ersatz für Metadaten)
  oder `#ns:tag`. Unbekannte Blöcke werden ignoriert, die Datei darf also schon vor dem Mod existieren.
- Alte Metadaten-Varianten: Achsen-Bits (4, 8, 12) weglassen, die heutigen Blöcke haben `axis`. Steckt die Holzart
  in einer Eigenschaft, `[type=...]` schreiben.
- Felder optional, sonst der Standard aus `treecapitator-server.toml` (Pack: `allowSmartTreeDetection` true,
  `breakSpeedModifier` 0.256, `maxHorLeafBreakDist` 4, `maxHorLogBreakDist` 16, `maxLeafIDDist` 1,
  `maxVerLogBreakDist` -1, `minLeavesToID` 3, `onlyDestroyUpwards` true, `requireLeafDecayCheck` true).
  Nur übernehmen, was die Pack-Konfiguration beim Baum setzt.
- `requireLeafDecayCheck`: Blätter zählen nur, wenn sie nicht `persistent` sind und höchstens 4 Blöcke von einem
  gefällten Stamm entfernt (Bit 8 von 1.5.2). Eigene Blätter ohne `persistent`-Eigenschaft gelten als natürlich.
- Die Registry wird zum Client synchronisiert (er braucht sie für die Abbaugeschwindigkeit). Sie lädt nur beim
  Serverstart, nicht bei `/reload`.

## Äxte und Scheren: Item-Tags

- `#treecapitator:axes` enthält `#minecraft:axes`. Eine Axt, die im Mod schon `#minecraft:axes` trägt, braucht nichts.
  Sonst (Sense, Paxel, Tinkers-Werkzeug) in `data/treecapitator/tags/item/axes.json` des neuen Mods eintragen
  (`"replace": false` ist Standard, Tags werden zusammengeführt).
- `#treecapitator:shears` analog (Pack: nur Schere; `shearLeaves`/`shearVines` sind aus).

## Prüfen

Am Testserver (`./gradlew runServer`, RCON wie in `tools/rcon.py`) einen Baum setzen (`place feature`, oder mit
`fill` Stamm und Blätter bauen) und mit einem FakePlayer fällen. Vorlage für einen **temporären** Befehl (nicht
committen): `FakePlayerFactory.get(level, new GameProfile(uuid, "[Test]"))`, Axt in die Hand,
`fp.gameMode.destroyBlock(pos)`, vorher/nachher Stämme (`BlockTags.LOGS`) und Blätter (`BlockTags.LEAVES`) im Umkreis
zählen. Erwartung: alle Stämme ab dem gefällten aufwärts weg, Blätter der Krone weg, Axt-Schaden = Zahl der Stämme.
Ein Stamm ohne mindestens 3 Blätter am oberen Ende ist kein Baum (nur ein Block bricht).

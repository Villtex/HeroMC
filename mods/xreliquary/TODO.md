# Xeno's Reliquary – offen und bewusste Abweichungen

## Client-Prüfung offen
Nur im Code geprüft: Kugel- und Wurf-Billboards (`BillboardRenderer`, Eiskugel als Schneeball-Textur), Item-Tönungen
(Pistole nach geladener Kugel über Custom Model Data, Kugeln/Magazine, schimmernde Seren und Umhang über die eigene
Tint-Quelle `xreliquary:time`), Kelch/Münze mit zweitem Symbol, Partikel, Altar-Leuchten, JEI-Seiten.

## Nicht portiert
- Die vier Blinzel-Bilder von Salamander- und Gespensterauge (Overlay 0–3 alle 80 Ticks): die Texturen sind im Jar,
  das Item zeigt nur das Grundbild.

## Bewusste Abweichungen
- Kugeln, Magazine, Pistolenteile und kondensierte Tränke hatten Damage-Werte; jeder ist ein eigenes Item (alte IDs mit
  Damage in `OldGameIds`). Die neun Kugel-Entities sind eine Entity `xreliquary:shot` mit Typ, alle Wurfdinge eine
  Entity `xreliquary:thrown`; beide werden nicht gespeichert.
- Ladung von Stab, Eisstab und Prüfstein ist wie im Original die Item-Haltbarkeit (0 = leer); kein Reparieren im
  Raster/Amboss (`setNoCombineRepair`, keine Reparatur-Zutat).
- Elixiere über die Komponente `minecraft:consumable` (0,8 s, Wirkungen, Allheilmittel entfernt die acht Leiden), das
  leere Fläschchen kommt über `usingConvertsTo`.
- Münze des Glücks: Gegenstände landen an den Füßen des Spielers (das Original setzte sie 0,9 unter die Füße).
- Leerentränen/-tasche merken sich das Item als Registry-Id (`item`, `itemQuantity`, `capacity`) und vergleichen Item +
  Komponenten (1.5.2: Id + Damage). Die Kapazität der Tasche endet bei 32000 (das Original lief dort in eine
  Endlosschleife).
- Alkahest und Glühwasser über `ItemCraftedEvent` wie die ICraftingHandler; der Crafter-Block löst das Event nicht aus
  (keine Auszahlung, kein Fläschchen).
- Schaden der Kugeln, Granate, Glühwasser und Erschütterung zählt als Angriff des Spielers (`playerAttack`), wie
  `causePlayerDamage`.
- Eisstab-Kugel bewegt sich wie im Original zweimal pro Tick.

# Tinkers' Construct – offen nach Teil 25b

Stand nach 25a (Werkzeug-Kern) und 25b (Werkzeuge im Einsatz, Darstellung). Was die nächsten Unterteile brauchen oder nachholen müssen.

## Nach 25b – kleine Abweichungen und Offenes

- Bei normalen Schlägen (Vanilla-Angriff + combat152) kommen Zusatzschaden der Modifikatoren, Sprint-Ausfall des
  Langschwerts und Schadensfaktor nach Krit und Blocken des Ziels (im Original davor); Rückstoß-Modifikator ohne die
  0,1 nach oben. Sense und geworfener Dolch nutzen die ganze 1.5.2-Formel (`ToolCombat.attack`), dort ohne
  Verzauberungsschaden (Tinkers-Werkzeuge sind nicht verzauberbar außer über Lapis/Seide).
- Meißel-Stufen der späteren Teile: `Detailing.add(...)` für Obsidian/Sandstein/Barren → Multi-Ziegel (multiBrick 0–13,
  multiBrickFancy 0–15, 25f), Geschwindigkeitsblöcke (speedBlock 0–6, 25f), versengte Blöcke (smeltery 2/4/6/8–11, 25c);
  die Gegenstands-Zutaten (Eisenbarren, Gold, Lapis, Diamant, Redstone, Knochen, Schleim, Fleischblock, Endstein,
  Aluminium) brauchen dann eigene Einträge, weil `Detailing` heute nur Blöcke kennt.
- Kein JEI für die Meißel-Rezepte (Code-Rezept).
- Trankwerfer: das Geräusch "launcher_clank" fehlt im Archiv, ersetzt durch das Armbrust-Laden.
- Pfeile, die einen Mob treffen, landen wie im Original in einem leeren Ausrüstungs-Slot (sichtbar in der Hand).
- Enthaupten (Köpfe) folgt mit 25e.

## 25c – Schmelzerei

- Gussformen (`*_cast`) sind registriert, werden aber erst mit dem Gießtisch nutzbar. Metallteile (Eisen, Cobalt …)
  gibt es nur durch Gießen ("Craft metals with Wood Patterns" = false).
- Werkzeugschmieden: Blöcke sind da, das Rezept braucht versengte Ziegel (`smeltery` 2) und die Metallblöcke
  (`metalBlock`) → mit 25c/25f eintragen (TContent: `bbb / msm / m m`, b = Seared Bricks, s = Werkzeugstation,
  m = Metallblock; Iron/Gold/Diamond/Emerald + "blockCobalt" usw.).
- Leere Gussformen (`blank_cast` aus Duraluminium, `gold_blank_cast` aus Gold) und Schmelzen der Muster.
- Metallblöcke, Erze, Rezepte Barren ↔ Block (`metalBlock` 3,5,6,4,7,0,1,2,8,9).
- Eisenklumpen ist `minecraft:iron_nugget` (alte ID 6035:19 zeigt darauf). Kupfer/Zinn usw. bleiben eigene Items.

## 25d / 25e / 25f

- `TItems.CONSECRATED_SOIL` (geweihte Erde, craftedSoil 4) ist noch `null`: der Bann-Modifikator wird erst mit 25f
  anbringbar.
- Musterkiste mit freien Mustern ("Add Patterns to Pattern Chests" = false im Pack) – nichts zu tun.
- `tinkerHouseChest`/`tinkerHousePatterns` (Dorf, 25d) nutzen die alten IDs, die schon registriert sind.
- Handbuch-Rezepte (`manualBook`) in 25e.

## Client-Prüfung offen

- 25b: Werkzeugmodelle aus Schichten (alle Materialien, Effekte, kaputt), Bogen-Spannzustände und Pfeil im Bogen,
  Pfeil-/Dolch-Renderer (Lage, Drehung), Bratpfanne (Modell mit Griff, GUI-Anzeigen), Gehtempo beim Benutzen.
- Alle GUIs (Werkzeugstation mit Knöpfen und Werte-Panel, Schmiede, Teilbauer mit Musterkiste, Schablonentisch,
  Musterkiste, Werkbank-Station), Tischmodelle, Stufen, Item-Symbole, JEI-Kategorien.
- Das Verschieben der Eingabe-Slots je Werkzeug (Reflection auf `Slot.x/y`, nur Client).

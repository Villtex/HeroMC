# Tinkers' Construct – offen nach Teil 25c

Stand nach 25a (Werkzeug-Kern) und 25b (Werkzeuge im Einsatz, Darstellung). Was die nächsten Unterteile brauchen oder nachholen müssen.

## Nach 25b – kleine Abweichungen und Offenes

- Bei normalen Schlägen (Vanilla-Angriff + combat152) kommen Zusatzschaden der Modifikatoren, Sprint-Ausfall des
  Langschwerts und Schadensfaktor nach Krit und Blocken des Ziels (im Original davor); Rückstoß-Modifikator ohne die
  0,1 nach oben. Sense und geworfener Dolch nutzen die ganze 1.5.2-Formel (`ToolCombat.attack`), dort ohne
  Verzauberungsschaden (Tinkers-Werkzeuge sind nicht verzauberbar außer über Lapis/Seide).
- Meißel-Stufen der späteren Teile: `Detailing.add(...)` für Obsidian/Sandstein/Barren → Multi-Ziegel (multiBrick 0–13,
  multiBrickFancy 0–15, 25f), Geschwindigkeitsblöcke (speedBlock 0–6, 25f); die versengten Blöcke sind mit 25c eingetragen;
  die Gegenstands-Zutaten (Eisenbarren, Gold, Lapis, Diamant, Redstone, Knochen, Schleim, Fleischblock, Endstein,
  Aluminium) brauchen dann eigene Einträge, weil `Detailing` heute nur Blöcke kennt.
- Kein JEI für die Meißel-Rezepte (Code-Rezept).
- Trankwerfer: das Geräusch "launcher_clank" fehlt im Archiv, ersetzt durch das Armbrust-Laden.
- Pfeile, die einen Mob treffen, landen wie im Original in einem leeren Ausrüstungs-Slot (sichtbar in der Hand).
- Enthaupten (Köpfe) folgt mit 25e.

## Nach 25c – Schmelzerei

- Erledigt: Gussformen, Metallteile durch Gießen, Werkzeugschmieden-Rezepte, leere Gussformen, Metallblöcke, Erze, Grout.
- Klares Glas (25f): Gießbecken/-tisch geben heute Vanilla-Glas und -Glasscheibe aus; mit 25f auf `glass`/`glassPane` umstellen,
  Schmelzen von klarem Glas, Scheiben und gefärbtem klarem Glas (500/350°, 1000/250 mB) und die Tank-Rezepte mit klarem Glas ergänzen.
- Brownstone (25f): Gießbecken Kies + 16 mB Zinn → `speedBlock` 0 fehlt.
- Erzbeeren (25d): Schmelzen (4 Beeren → 16 mB bei 100°, Eisen/Gold/Kupfer/Zinn/Aluminium) und Ofen-Rezepte fehlen.
- Blauer Schleimschlamm-Rezept (4 blaue Schleimbälle = strangeFood, 25e) fehlt; Block und Ofenrezept sind da.
- Die Flüssigkeiten haben keine Fluid-Capability (Transfer-API von NeoForge); die Blöcke sprechen untereinander über
  `smeltery.LiquidContainer`. Bei Bedarf (Rohre anderer Mods) nachrüsten.
- Fluss-Blöcke alter Welten (1313/1314) werden geschmolzenes Eisen (die Art stand in einer TileEntity).

## 25d / 25e / 25f

- Musterkiste mit freien Mustern ("Add Patterns to Pattern Chests" = false im Pack) – nichts zu tun.
- `tinkerHouseChest`/`tinkerHousePatterns` (Dorf, 25d) nutzen die alten IDs, die schon registriert sind.
- Handbuch-Rezepte (`manualBook`) in 25e.

## Client-Prüfung offen

- 25c: Flüssigkeiten (Quelle/Fließen, animiert), Schmelzerei-GUI (Scrollen ab 3 Lagen, Hitzebalken, Tooltips), Renderer von
  Tank, Gießtisch/-becken (Flüssigkeit + Gegenstände), Hahnstrahl, Kanal, Trockengestell, Inhalt der Schmelzerei; Blockmodelle
  (Tisch, Becken, Hahn, Kanal, Gestell), JEI-Kategorien mit Flüssigkeiten.

- 25b: Werkzeugmodelle aus Schichten (alle Materialien, Effekte, kaputt), Bogen-Spannzustände und Pfeil im Bogen,
  Pfeil-/Dolch-Renderer (Lage, Drehung), Bratpfanne (Modell mit Griff, GUI-Anzeigen), Gehtempo beim Benutzen.
- Alle GUIs (Werkzeugstation mit Knöpfen und Werte-Panel, Schmiede, Teilbauer mit Musterkiste, Schablonentisch,
  Musterkiste, Werkbank-Station), Tischmodelle, Stufen, Item-Symbole, JEI-Kategorien.
- Das Verschieben der Eingabe-Slots je Werkzeug (Reflection auf `Slot.x/y`, nur Client).

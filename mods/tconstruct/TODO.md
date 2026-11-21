# Tinkers' Construct – offen nach Teil 25d

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
- Erledigt mit 25d: Erzbeeren schmelzen (16 mB bei 100°) und im Ofen (Klumpen), Rezept für blauen Schleimschlamm.
- Die Flüssigkeiten haben keine Fluid-Capability (Transfer-API von NeoForge); die Blöcke sprechen untereinander über
  `smeltery.LiquidContainer`. Bei Bedarf (Rohre anderer Mods) nachrüsten.
- Fluss-Blöcke alter Welten (1313/1314) werden geschmolzenes Eisen (die Art stand in einer TileEntity).

## Nach 25d – Welt

- Schleiminseln: Form, Erosion und Bäume zog das Original aus `new Random()` ohne Seed (nicht seed-fest); hier eine eigene
  `Random` je Chunk aus Welt-Seed und Chunk (`SlimeIslandGen.islandRandom`). Rarität, Becken und Gras nutzen wie im Original
  die gemeinsame Zufallsfolge.
- Blauer Schleim: keine natürlichen Spawns (das Original las „Activate Blue Slime Spawns“ nie und meldete keinen Spawn an);
  er kommt nur aus flüssigem blauem Schleim (Zufallsticks statt der Fluid-Ticks des Originals). Reiter aus `initCreature`
  (Skelett/Creeper, Königsschleim mit Gefolge) gibt es nur beim Spawn-Ei des Originals (`TitleIcon`, 25e).
- Herzbehälter: nur das Kleine rote Herz (6027:1) als Item für die Beute; leerer Behälter und Herzbehälter mit Wirkung in 25e.
- Tüftler (Beruf 78943): Handel 14 Kolben → Aluminium-Beerenbusch, 23 Kolben → Eisen-Beerenbusch (was `addToListWithCheck`
  vom Original übrig ließ), beide gleich auf Stufe 1/2 statt eines je Stufe. Ohne Arbeitsblock; Strukturdorfbewohner kommen
  mit Stufe 2.
- Erzbeeren-Büsche: Tooltips der Items fehlen; Schleimblätter zerfallen ohne Holz oder erstarrten Schleim im Umkreis 4
  (vereinfacht wie bei Natura).
- Musterkiste mit freien Mustern ("Add Patterns to Pattern Chests" = false im Pack) – nichts zu tun.
- Handbuch-Rezepte (`manualBook`) in 25e.

## Client-Prüfung offen

- 25d: Busch-Modelle je Stufe (reif, animierte Essenzbeere), Schleimgras/-blätter/-setzling/-gras, flüssiger Schleim
  (animiert), blauer Schleim und Königsschleim (Textur, Bossleiste), Tüftler-Textur, Handel im Menü.

- 25c: Flüssigkeiten (Quelle/Fließen, animiert), Schmelzerei-GUI (Scrollen ab 3 Lagen, Hitzebalken, Tooltips), Renderer von
  Tank, Gießtisch/-becken (Flüssigkeit + Gegenstände), Hahnstrahl, Kanal, Trockengestell, Inhalt der Schmelzerei; Blockmodelle
  (Tisch, Becken, Hahn, Kanal, Gestell), JEI-Kategorien mit Flüssigkeiten.

- 25b: Werkzeugmodelle aus Schichten (alle Materialien, Effekte, kaputt), Bogen-Spannzustände und Pfeil im Bogen,
  Pfeil-/Dolch-Renderer (Lage, Drehung), Bratpfanne (Modell mit Griff, GUI-Anzeigen), Gehtempo beim Benutzen.
- Alle GUIs (Werkzeugstation mit Knöpfen und Werte-Panel, Schmiede, Teilbauer mit Musterkiste, Schablonentisch,
  Musterkiste, Werkbank-Station), Tischmodelle, Stufen, Item-Symbole, JEI-Kategorien.
- Das Verschieben der Eingabe-Slots je Werkzeug (Reflection auf `Slot.x/y`, nur Client).

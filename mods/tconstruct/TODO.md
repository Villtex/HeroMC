# Tinkers' Construct – offen nach Teil 25f (Port abgeschlossen)

**Teil 27a:** 1.4.3 gilt nur noch in Hexxit Legacy (Integrated bekommt Tinkers 3.12.1, `mods/tconstruct3`). Rezepte und
Entity-Beute tragen `worldgen152:world_type` `legacy:true` (`tools/legacy_condition.py`). `tools/gen.py` ist hinter den
Handänderungen aus 26b/26e zurück (c:-Tags, Integrated-Features, Rezepte über Tags): nicht neu laufen lassen, ohne es
vorher nachzuziehen.

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
- Erledigt mit 25e: Enthaupten (Köpfe). Erledigt mit 25f: Meißel-Stufen für Ziegel/Brownstone (auch aus Items), JEI-Kategorie Meißel.

## Nach 25c – Schmelzerei

- Erledigt: Gussformen, Metallteile durch Gießen, Werkzeugschmieden-Rezepte, leere Gussformen, Metallblöcke, Erze, Grout.
- Erledigt mit 25f: klares Glas gießen/schmelzen, Tanks mit klarem Glas, Brownstone aus Kies + Zinn. Beute der Doppelstufen
  und Tanks auf das 26.3-Format (`modifier`/`condition`) korrigiert.
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
- Erledigt mit 25e: leerer Kanister und Herzkanister mit Wirkung.
- Tüftler (Beruf 78943): Handel 14 Kolben → Aluminium-Beerenbusch, 23 Kolben → Eisen-Beerenbusch (was `addToListWithCheck`
  vom Original übrig ließ), beide gleich auf Stufe 1/2 statt eines je Stufe. Ohne Arbeitsblock; Strukturdorfbewohner kommen
  mit Stufe 2.
- Erzbeeren-Büsche: Tooltips der Items fehlen; Schleimblätter zerfallen ohne Holz oder erstarrten Schleim im Umkreis 4
  (vereinfacht wie bei Natura).
- Musterkiste mit freien Mustern ("Add Patterns to Pattern Chests" = false im Pack) – nichts zu tun.
- Erledigt mit 25e: Handbuch-Rezepte.

## Nach 25e – Spieler

- Herz-Anzeige: Vanilla 26.3 zeichnet Leben über 20 in weiteren Reihen; die farbigen Herzen (`newhearts.png`) des Originals in
  derselben Reihe sind nicht nachgebaut (die Vanilla-Ebene PLAYER_HEALTH enthält auch Rüstung/Hunger/Luft und lässt sich nicht
  einzeln ersetzen). Die Textur ist schon im Jar.
- TFoodStats: das Original setzte nach jedem Respawn eigene FoodStats ein (Heilung ab Hunger 12 + 2 × Schwierigkeit statt 18); hier
  bleibt Vanilla-Hunger.
- Goldener Kopf Damage 1 (leuchtend, Regeneration IV …) hatte kein Rezept und keinen Kreativ-Eintrag: weggelassen.
- Rezepte Goldene Karotte bzw. Goldener Kopf aus Gold-Erzbeeren um einen Apfel haben dasselbe Raster wie der Goldene Apfel, der in 1.5.2
  immer gewann: nur der Goldene Apfel ist eingetragen.
- Knochenmehl am Schleimsetzling wirkte im Original nur im Kreativmodus (Metadaten-Prüfung); nicht nachgebaut.
- Erledigt mit 25f: Essenz-Extraktor.
- Erledigt mit 25f (Namen passen, Brownstone = `rough_brownstone`): Handbuch-Bilder von 25f-Blöcken (Klarglas, Brownstone, Punji, Barrikade, Zugbrücke, Feueranzünder) bleiben leer, bis es sie gibt
  (`client/manual/ManualIcons` sucht sie über den Namen: `clear_glass`, `brownstone`, `punji_sticks`, `oak_barricade`, `drawbridge`,
  `igniter` – in 25f diese Namen verwenden oder dort anpassen).

## Nach 25f – Deko, Fallen, Redstone

- Nicht portiert, weil im Original ohne Rezept und unfertig (Debug-Ausgaben, halbes Kristall-System `Crystallinity`):
  Lichtaggregator (1327), Lichtkristall (1335) mit Entity `Crystal`, Antilicht (1342). `dimension/*` war nie registriert.
- Rezept Kürbislaterne aus Kürbis + Steinfackel: im Original kaputt (Schlüssel `s` statt `w`, ergab Laterne aus einem Kürbis) –
  weggelassen.
- „logWood“ für die Eichenbarrikade: hier die übrigen Vanilla-Stämme (Tag `tconstruct:other_logs`), keine Stämme anderer Mods.
- Landmine: die Auslöseart stand im Tile Entity; alte Welten (1334) werden immer die Holz-Landmine (alles).
  Vier eigene Blöcke statt eines Items mit Damage. Die Raketen-/Feuerball-Kombination: ohne lebenden Auslöser fliegt
  der Feuerball ohne Besitzer.
- Zugbrücke: `drawbridgeState`-Tabelle vereinfacht: zurückgenommen wird, was als Item gleich dem im Slot ist, außer
  den „nie“-Blöcken (Grundgestein, Kolbenkopf, Spawner, Pflanzen, Portale, Kuchen); Wasser/Lava gibt es nicht als Item.
- Verbundenes Glas: Mehrteil-Modell aus der Original-Tabelle (`gen_deco.py` liest `GlassBlockConnected.java`); Ausrichtung
  der Randtexturen (v. a. Unterseite) nur im Client prüfbar.

## Client-Prüfung offen

- 25f: verbundene Glastexturen (alle 6 Seiten), Scheiben, Tarnung von Zugbrücke und Landmine (Modell, Tönung, Licht),
  Landmine- und Zugbrücken-GUI (Knöpfe), Buch auf dem Essenz-Extraktor, Barrikaden-/Punji-Modelle, Steinfackel, Tooltips.

- 25e: Tabs über Inventar/Rüstung/Rucksack, beide GUIs (Leerplatz-Bilder, Spielerfigur), Taste O, Handbuch (Layout, Schrift,
  Bilder, Umblättern), Item-Modelle, Ei-Farbe, Essenzkristall-Animation.

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

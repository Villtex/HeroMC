# Karten (Teil 24f) – Abweichungen und offene Prüfungen

Klassen: `TFMaps` (Register, Biomfarben, Kartendaten mit genauer Mitte), `item/TFFilledMapItem`, `MagicMapItem`,
`MazeMapItem`, `EmptyMagicMapItem`, `EmptyMazeMapItem`, Client `client/TFMapsClient`. Daten in `tools/gen_items.py`.

## Bewusste Abweichungen

- **Speicher:** Kartendaten liegen in Vanilla-`MapItemSavedData` (`map_<n>`, Komponente `minecraft:map_id`), nicht in
  eigenen `magicmap_<n>`/`mazemap_<n>`. Alle Karten teilen sich also einen Zähler mit Vanilla-Karten.
- **Mitte:** wie 1.5.2 (Zauberkarte: auf 2048 gerundet, Labyrinthkarte: auf 128 gerundet + 10). Die Daten werden
  über den Codec angelegt, weil `createFresh` die Mitte auf sein eigenes Raster schieben würde.
- **Zauberkarte erkennen:** Die Pixel enthalten Biom-ID + 1 statt Kartenfarbe. Der Client erkennt eine Zauberkarte an
  der unsichtbaren Dekoration `twilightforest:magic_map` (auch auf dem Stapel in `minecraft:map_decorations`, damit
  sie im Rahmen nach einem Neustart da ist) und malt eine eigene Textur (Render-State-Modifier von NeoForge).
- **Biomfarben:** 1.5.2-Farben der TF-Biome (TFBiomeBase) und der Vanilla-Biome 0–22 (BiomeGenBase). Außerhalb des
  Zwielichtwalds wird das Biom über seinen worldgen152-Schlüssel (oder ein paar Vanilla-Schlüssel) auf die alte ID
  abgebildet; Biome anderer Mods ohne bekannte Farbe sind grau, Biome ganz ohne alte ID bleiben leer. Außerhalb
  des Zwielichtwalds wird je 4 × 4-Zelle in Meereshöhe gezählt (Biome von heute sind 3D, keine Spalten).
- **Bauwerks-Symbole:** Dekorationstypen `twilightforest:feature/<name>` (je TFFeature), gezeichnet von
  `TFMapsClient` aus `textures/gui/mapicons.png` (wie TFMagicMapRenderer). Sie stehen auch auf dem Stapel, damit sie
  einen Neustart überleben und im Rahmen erscheinen (1.5.2 zeigte im Rahmen die Vanilla-Darstellung ohne Symbole).
  Ein Symbol am äußersten Rand (relativ −64) sitzt dort, wo 1.5.2 es hatte; `checkExistingFeatures` entfällt
  (konnte nie etwas ändern, `generateFeatureFor` liefert nie „nichts“).
- **Höhenschicht der Labyrinthkarte** (`TFMazeMapData.yCenter`) ist die Komponente `twilightforest:maze_map_y` am
  Stapel; Kopien durch Klonen behalten sie.
- **Erzfarben:** Erze über die `c:ores/*`-Tags (Tiefenschiefer-Erze zählen wie normale); „Name enthält ore“ wird zu
  `c:ores` oder Registry-Name `*_ore`/`ore*` (Netherquarz-Erz wird dadurch rot).
- **Stapelgröße:** Gefüllte Karten stapeln bis 64 wie Vanilla (1.5.2: 1), weil das Klonen (Vanilla-Rezepttyp
  `crafting_transmute` mit bis zu 8 leeren Karten) sonst Stapel über dem Maximum erzeugen würde.
- **Klonen** ist das Daten-Rezept `<karte>_cloning` (`crafting_transmute`), kein eigener Rezepttyp.
- **Spieler-Symbol** außerhalb der Höhenschicht: nur wenn der Spieler auf der Karte steht, wird es zum weißen Punkt;
  außerhalb der Karte bleibt die Vanilla-Verfolgung.
- Leere Karten werden wie in 1.5.2 auch im Kreativmodus verbraucht; Fortschritt „done“ der drei Karten-Fortschritte
  wird beim Benutzen vergeben (zusätzlich: Rezept hergestellt oder Karte im Inventar).
- In der Hand: auch die Nebenhand zeichnet (wie Vanilla-Karten heute).

## Im Client prüfen

- Zauberkarte in der Hand und im Rahmen: Biomfarben, Schachbrett der leeren Pixel, Symbole (Lage, Größe, unter dem
  Spieler-Symbol), unsichtbare Markierung zeichnet nichts.
- Labyrinth-/Erzkarte: Farben der Schicht, Erze, weißer Punkt außerhalb der Schicht.
- Namen „Magische Karte #n“, Seltenheitsfarben.

## Geprüft (Server, FakePlayer-Testbefehl, nicht eingecheckt)

- Leere Karten benutzen: Mitte, Maßstab, Höhe, Name „#n“; 32 Ticks in der Hand malen Biom-IDs + 1 (TF und
  Oberwelt), Bauwerks-Symbole an den Rasterpunkten (z. B. Hügel bei 0,0, Labyrinth bei 0,32), Labyrinth- und Erzkarte
  Farben der Schicht. Crafter: alle vier Rezepte, Klonen behält Name/Höhe, falsche leere Karte klont nicht.

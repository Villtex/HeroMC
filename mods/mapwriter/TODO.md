# MapWriter – offen

- **Untergrundkarte** (`UndergroundMapRenderer`, Kategorie `undergroundMap`) fehlt. Im Pack ist sie aus
  (`enabled=0`), deshalb kennt der Port nur kleine Karte, große Karte und Vollbild.
- **Nicht übernommen:** `textureSize`/`linearTextureScaling` wirken nicht mehr (die Karte wird je Bild aus den
  Regionen gezeichnet statt in eine große GL-Textur gemischt), `teleportEnabled` ist standardmäßig aus,
  `chunksPerTick` steuert weiter, wie viele Chunks je Tick eingelesen werden.
- **Blockfarben:** statt `BlockColours`/`BlockColourGen` mit Block-IDs und der `blockcolour`/`blocktype`-Tabelle
  der Pack-Konfiguration nimmt der Port den Mittelwert der Textur je Blockzustand und die Tönung aus dem Spiel
  (Gras, Laub, Wasser). Übernommen sind nur die zwei Einträge des Packs (Löwenzahn, Mohn).
- **Dateiformat:** eine PNG je 512×512-Region unter `mapwriter/<welt>/dim_<dimension>/r.<x>.<z>.png` statt der
  `.mwregion`-Dateien des Originals; Marker in `markers.txt`, Dimensionen in `dimensions.txt`.
- **Offen:** Spur (`playerTrailEnabled`) ist im Pack aus und nur im Code geprüft; die Karte eines Servers wird
  unter `mp_<adresse>` abgelegt, ohne Port im Namen.
- Client-Prüfung erledigt (Minikarte rund mit Rand, Vollbildkarte, Marker anlegen und zeichnen, Zoom,
  Koordinaten, Biomanzeige, gespeicherte Regionsbilder).

# SecretRoomsMod – offen und bewusste Abweichungen

## Client-Prüfung offen
Nur im Code geprüft: das Tarn-Modell (`client/CamoModel`: Modell des kopierten Blocks, Einwegglas mit Glas auf den
anderen Seiten, Türen/Falltür mit auf die Platte zugeschnittenen Flächen des kopierten Blocks), die Tönung des kopierten
Blocks (Gras, Laub), `/srm-show` (Neuzeichnen im Umkreis 20), die Einweg-Taste (Backslash) mit Chatmeldung.

## Bewusste Abweichungen
- Es wird nur der Blockzustand kopiert, nicht die Block-Entity-Daten (copyTE). Kopiert werden volle Würfel mit Modell
  ohne Block-Entity (1.5.2 nahm auch Halbstufen und andere Standard-Blöcke).
- Bei mehreren gleichwertigen Nachbarn nimmt die Wahl den ersten (unten, oben, West, Ost, Nord, Süd); im Original
  zufällig, weil `BlockHolder.equals(Object)` immer falsch war.
- Die Tarnung wird im ersten Tick nach dem Setzen gewählt (`onLoad`), damit sie auch bei `/setblock` und Vorlagen greift.
- Tarnblöcke sind immer lichtundurchlässig (1.5.2: Lichtdurchlässigkeit des kopierten Blocks).
- Der Knopf ist zwei Blöcke (Stein, Holz) statt Damage 0/1; beide bleiben 30 Ticks gedrückt wie im Original.
- Die Treppe hat immer die untere Form (das Original kannte keine umgedrehte Treppe).
- Mit `/srm-show` zeigen Fackelhebel und feste Luft weiterhin nichts Eigenes (Original: TorchLever- bzw.
  SolidAir-Textur).
- Wasserflasche in den Feste-Luft-Rezepten als Komponenten-Zutat (Trank „Wasser“).

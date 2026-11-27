# Tinkers' Construct 3.12.1 – offen (Teil 27)

Stand nach 27a (Grundlage). Ausführlich: Skill `tinkers3-263`, Bericht des Konverters `tools/convert_report.md`.

## Für die nächsten Teile

- **27b (Mantle-Kern):** Lader für `tinkering/**` und `mantle/**` müssen `neoforge:conditions` auswerten (erstes Element
  immer `worldgen152:world_type`, `legacy:false`); Tinkers-eigene Bedingungen `tconstruct3:config`, `mantle:tag_filled`,
  `mantle:tag_combination_filled` als NeoForge-Bedingungen registrieren (sie stehen jetzt in `neoforge:conditions`).
  Mantle-Lader lesen Zutaten im 26.3-Format (Strings/`#tag`/`neoforge:ingredient_type`), Ausgaben `{"id",…}` oder
  `{"tag",…}`, Flüssigkeiten in Mantle-Form (`fluid`/`tag` + `amount`).
- **27d:** Komponenten-Namen festlegen und `nbt_to_components` in `convert_data.py` anpassen (vorläufig
  `tconstruct3:materials`, `:display`, `:tank`, `:material`); Item-Sub-Prädikat `tconstruct3:tool_stack` registrieren.
- **27g/27i:** `minecraft:copy_nbt` in 19 Block-Beute-Tabellen (Tanks, Anzeigen, Truhen, Amboss) durch
  `copy_components` ersetzen, sobald die Block-Entities Komponenten haben (Regel im Konverter ergänzen).
- **27j:** `trim_material` (16) auf das 26.3-Format (ohne `ingredient`/`item_model_index`, Komponente
  `provides_trim_material` am Item); `minecraft:geode`/`ore`/`tree`-Features gegen 26.3 prüfen.
- **27k:** Integrated-Features von 1.4.3 (Biom-Modifier, Dorfteile) abschalten; JEI/Kreativ-Tabs über
  `HexxitWorldType.onClientChange` neu aufbauen.
- `data-enabled.txt` ist leer: keine konvertierten Daten im Jar, bis der Code dafür da ist.

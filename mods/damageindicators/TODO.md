# Damage Indicators – offen

- **Nur die Haut „Default“.** Das Original brachte drei Häute mit (Default, WoWLike, Minimal) und konnte weitere
  aus `DITextures/<name>/skin.cfg` laden; die Auswahl lief über eine eigene GUI. Portiert ist die Haut des Packs
  (`Portrait_Skin=/DITextures/Default/`), ihre Maße stehen fest in `client/Portrait.java`.
- **Keine Einstell-GUIs.** `RepositionGui`, `AdvancedGui`, `SkinGui` und die Konfiguration je Mob-Klasse
  (`DIAdvancedCompatibility`, Namens-Überschreibung, Maßstab je Mob, „IgnoreThisMob“) fehlen; alles läuft über
  `damageindicatorsmod-client.toml`. Die Taste `.` schaltet nur noch die Schadenszahlen an und aus.
- **Bewusste Abweichungen:**
  - Die Zahlen zeichnet die HUD-Ebene, projiziert auf den Bildschirm, nicht eine Partikel-Entity mit eigener
    Schriftart-Textur (`CustomFontRender` baute zur Laufzeit ein Bild aus der Schrift des Texturenpakets).
  - Die Größe wächst wie im Original 8 % je Bild bis zum Dreifachen und schrumpft dann um 4 %, gerechnet mit
    etwa drei Bildern je Tick.
  - `Enable_Depth_Test=false` des Packs heißt hier: Zahlen für Mobs in Sichtlinie liegen immer vorn.
  - Die Vorschau im Bild ist auf den Kasten zugeschnitten (`g.entity` kennt die Pose nicht) und etwas kleiner
    als im Original, damit auch lange Mobs (Kuh, Chocobo) ganz hineinpassen.
- **Mob-Effekte kommen vom Server** (`DINetwork`, alle zwei Ticks, 30 Blöcke), weil 26.3 dem Client nur die
  eigenen Effekte schickt. Der Bereich steht fest; das Original hatte ihn in der Server-Konfiguration.
- Client-Prüfung erledigt (Bild, Lebensbalken, Effekt-Kasten mit Countdown, Schadenszahl); offen bleibt das
  Verhalten mit Mehrteiler-Mobs (Enderdrache) und sehr großen Mobs aus anderen Mods.

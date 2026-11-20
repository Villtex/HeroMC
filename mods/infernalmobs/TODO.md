# Infernal Mobs – offen

- **Beuteliste ergänzen**, sobald die Mods portiert sind (in `IMConfig.DEFAULT_ITEMS`):
  - Teil 19, Falling Meteors: alte IDs 6058, 6059, 6060, 6062, 6064, 6067, 6068, 6069, 6071, 6072, 6073, 6074, 6075,
    6076, 6079, 6081 (= Config-ID + 256, siehe `Meteors.cfg` des Packs).
  - Teil 20, Xeno's Reliquary: alte ID 6408 (`xreliquary.cfg`).
- **Client-Prüfung offen:** HUD (Name, Lebensleiste, Modifikator-Zeilen) und die Zauber-Partikel um Rare-Mobs nur
  im Code geprüft, nicht im laufenden Client.

## Bewusste Abweichungen vom Original

- Leben: statt das Leben per Reflection über das Maximum zu setzen, ein `max_health`-Modifikator
  (`infernalmobs:health`, Obergrenze des Attributs 1024). Regen/Lifesteal/1UP heilen bis zu diesem Maximum.
  Ein vom Speicherstand geladener Rare-Mob behält sein gespeichertes Leben (das Original setzte es beim
  Nachladen wieder auf voll, sobald ein Client danach fragte).
- Pakete 1/4 (Modifikatoren und Leben an den Client) durch ein synchronisiertes Data Attachment ersetzt, das Leben
  kommt vom Mob selbst. Pakete 2/3 (Blastoff/Gravity) sind serverseitige Bewegung mit `needsSync`.
- Konfiguration: Entity-Namen-Abschnitte als Listen von Entity-IDs (`forbiddenEntities`, `entitiesAlwaysInfernal`),
  `useSimpleEntityClassnames` entfällt. Beuteliste mit Item-IDs statt Zahlen-IDs; unbekannte Items werden einmal
  geloggt und übersprungen. `disableGUIoverlay` ist eine Client-Einstellung (`InfernalMobs-client.toml`), der Rest
  eine synchronisierte Server-Einstellung (`InfernalMobs.toml`).
- Alchemist wirft die Tränke der 26.3-Hexe (Wurftrank Schaden, Langsamkeit, Gift, Schwäche) statt der alten
  Schadenswerte 32732/32698/32660/32696 (bei Langsamkeit/Schwäche war dort Stufe II gesetzt).
- Anzeigename: übersetzter Entity-Name statt `EntityList.getEntityString` (den das Original wegen `split(".")` nie kürzte).
- Partikel: fünf `entity_effect`-Partikel je Tick statt eines je 10 ms gerenderten Frames.
- LivingHurtEvent → `LivingIncomingDamageEvent`; Treffer, die 1.5.2 wegen der Unverwundbarkeitszeit verworfen hätte,
  lösen keine Modifikatoren aus, ein verschachtelter Gegentreffer auf einen Mob, dessen Modifikatoren gerade laufen
  (Vengeance gegen Vengeance), wird verworfen.

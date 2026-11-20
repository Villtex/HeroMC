# Falling Meteors – offen (work in progress)

Zwischenstand eines abgebrochenen Subagenten, in der Hauptsession weitergeführt. `gradle.properties` liegt als
`gradle.properties.wip` daneben, damit das Teilprojekt noch nicht gebaut wird, bis es kompiliert und Ressourcen hat.

- Client fehlt: `meteors.client.MeteorsClient` (Renderer Meteor, Alien Creeper, Comet Kitty, Schildstrahl, Partikel,
  Detektor-Nadeln aus `MeteorNetwork`).
- Ressourcen fehlen: Modelle, Sprachdateien, Rezepte, Loot, Verzauberungen, `original-assets.txt` (Sounds sind .wav).
- Weltgenerierung `gen/MeteorsWorldGen` noch nicht in `tools/expect.sh` / `verify_blocks.py`.
- Infernal Mobs: Meteors-Items in `IMConfig.DEFAULT_ITEMS`.

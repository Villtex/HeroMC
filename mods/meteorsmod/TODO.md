# Falling Meteors – offen und bewusste Abweichungen

Port begonnen von einem Subagenten (Kern, Blöcke, Items, Entities, Krater, Ereignisse), in der Hauptsession
fertiggestellt (Netzwerk, Client, Ressourcen, Prüfung, Fallgeschwindigkeit des Meteoriten).

## Client-Prüfung offen
Meteor-Modell (ModelMeteor als 1/8/14 Würfel), Alien Creeper (Creeper-Modell mit meteorcreeper.png, immer geladen),
Kometenkatze (Katzenmodell mit cometKitty.png), Detektor-Nadeln (32 Bilder des Streifens über die Eigenschaft
`meteors:detector`), Partikel, Rüstungen am Spieler.

## Nicht portiert (Client)
Schildstrahl (TileEntityMeteorShieldRayRenderer), Raumhelm der Kometenkatze, Meteor-Timer-Modell (als halbe Stufe),
Partikelbilder aus particles.png (getönte Vanilla-Puffs).

## Bewusste Abweichungen
- Konfiguration als STARTUP-Config mit den Pack-Werten (`MeteorsConfig`).
- Pakete MetSettings und MetChunk entfallen (nur für Tooltips); der Rest ist eine Sync-Nutzlast (`MeteorNetwork`).
- Cold Touch und Magnetization: Kosten des Originals (min 40 über max 31), also nie am Zaubertisch, wie damals.
- Blockdrops nach Hitze als Loot-Tabellen (Glück als uniform_bonus_count genähert).
- Sounds .wav → .ogg beim Asset-Import (ffmpeg).

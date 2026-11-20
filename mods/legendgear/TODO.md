# LegendGear – offen und bewusste Abweichungen

## Client-Prüfung offen
Alles Sichtbare ist nur im Code geprüft: Partikel (Runen, Funken, Flammenwirbel), Item-Modelle (Spiegel hell/aktiv,
Enterhaken abgeschossen), Krug-Modell, Sockel, Sternenstrahl-Fackel, Sternschnuppe, Stirnband/Wirbelwindstiefel am
Spieler, Rezept-Anzeige der Spezialrezepte in JEI.

## Nicht portiert (Client)
- Schwert im Sockel (TileEntityPedestalRender), Himmelsstrahl (TileEntitySkybeamRender), Sternenstein-Hülle
  (TileEntityStarstoneRender): die Blöcke funktionieren, die Zusatzgrafik fehlt.
- Enderbomben-Ring, Sternenstrahl-Stern (RenderGrindStar), Hakenkette (RenderHookshot): Entities ohne bzw. als Item.
- Regenbogen-Erfahrungskugeln (PrismaticXP), Ziffern auf Köcher/Bombentasche (stattdessen Balken + Tooltip),
  Portal-Verzerrung beim Spiegel und beim Ender-Rückruf (timeInPortal), mystischer Samen leuchtend bei Gewitter,
  blinkende Bombe.

## Bewusste Abweichungen
- Konfiguration fest auf die Werte der Pack-`LegendGear.cfg` (`LGConfig`, `LGWorldGen`). BombableBlocks des Packs:
  Pflasterstein, TNT und 2461 (die alte Standard-ID des Krugs; im Pack ist der Krug 1179) – der Krug ist also wie im Pack
  nicht sprengbar.
- Köcher/Bombentasche zählen in der Komponente `legendgear:ammo` statt im Item-Schaden; der Spiegel merkt sich „draußen“
  in Custom Data + Custom Model Data.
- Monster-Beute: „experienceValue >= 5“ als `Enemy`; Ender-Medaillon lädt beim Zorn eines Endermans
  (`EndermanAngerEvent`, nur wenn er den Spieler noch nicht jagt) statt beim Anstarr-Geräusch.
- Wirbelwindstiefel: doppelte Laufgeschwindigkeit als Attribut-Modifikator; die doppelte Luftsteuerung fehlt.
- Sprung-Erkennung (JumpNoticeHandler) serverseitig aus der letzten Client-Eingabe, keine eigenen Pakete.
- Medaillon-Schwert: Auslösen beim Start eines Schlags (`isSwinging`-Flanke) statt swingProgressInt == 1.
- Pyro-Amulett/Phönixfeder: Unverwundbarkeit über `setInvulnerableTime`. Aero-Amulett liest den Pfeilschaden per
  Reflection (`baseDamage`).
- Fangei und Ikarusflügel waren im Original nie registriert und fehlen hier auch.

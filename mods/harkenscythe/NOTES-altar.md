# Harken Scythe: die Altäre (Teil 9, Teilauftrag 2)

Portiert in `harkenscythe.altar` (Server) und `harkenscythe.client.Altar*` (Client), JEI in `harkenscythe.compat.AltarCategory`.

- Blöcke `harkenscythe_core:altar_of_souls` / `altar_of_blood` (`AltarBlock`, 12/16 hoch, Härte 5, Widerstand 6 = 1.5.2 10,
  Metall-Klang, Spitzhacke), Block-Entity `harkenscythe_core:altar` (`AltarBlockEntity`: ein Slot, Buch-Animation des
  Zaubertischs). Werkbank: Obsidian/Netherrack „XXX“, „*X*“ mit Enderperlen, Book of Shadows/Carnage oben.
- Tiegel: `AltarBlock.essence/drain` und `animateTick` laufen über dieselbe Schleife wie 1.5.2 (Ring 2 um den Altar,
  Höhe −1..+1, Luft auf halbem Weg eine Ebene über dem Tiegel). Kosten zählen in ganzen Tiegelstufen (Seelen /5, Blut /10).
- Menü `harkenscythe_core:altar` (`AltarMenu`): Der Server rechnet Essenz, Rezept und Kosten aus und schickt sie als
  Daten-Slots; der Knopf ist `clickMenuButton(0)` → `AltarBlock.craft`. Das HSPacket von 1.5.2 entfällt.
- Rezepttyp `harkenscythe_core:altar` (`AltarRecipe`), Daten in `data/harkenscythe_core/recipe/altar/<soul|blood>/`, erzeugt
  von `tools/gen.py`. `order` = alte Rezeptnummer (1–6 Umwandlung, 20–27 Augment), die höchste passende gewinnt.
  Augment-Zeilen: Wurf und Rang je 1–100 wie `soulAltarItemStackAugmentBuilder`; `tools/check.py` prüft, dass jede
  Kombination genau einmal abgedeckt ist. Schild/Riesenschwert nur mit `asgardshield_core` (Bedingung `mod_loaded`).
- Die Rezepte gehen per `OnDatapackSyncEvent.sendRecipes` an den Client (`AltarRecipesClient`) für JEI.

Abweichungen: Essenzanzeige live statt Momentaufnahme beim Öffnen; Augmente nur auf unverzauberte Gegenstände, auch
das Attunement (1.5.2 zeigte den Knopf bei Talisman/Amulett ohne dieses Attunement, wirkte aber je nach Server/Client
uneinheitlich); Gegenstand im Slot geht beim Schließen ins Inventar statt vor den Spieler. Orange Wolle fehlt wie in
1.5.2 (`woolColorCheck`).

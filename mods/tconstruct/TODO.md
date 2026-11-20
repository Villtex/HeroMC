# Tinkers' Construct – offen nach Teil 25a

Stand nach 25a (Werkzeug-Kern). Was die nächsten Unterteile brauchen oder nachholen müssen.

## 25b – Werkzeuge im Einsatz und Darstellung

- Modifikator-Effekte (`TActiveOmniMod`, `ActiveToolMod`): Moos-Reparatur, Auto-Smelt, Glück-Zufallsstufen
  (`ModLapis.afterBlockBreak`), Redstone nur Tempo (schon), Lohe/Feurig (`Fiery`, `Lava` setzen Feuer), Nekrotisch
  (Leben), Rückstoß (`Knockback`), Bann/Gliederfüßer (`ModSmite`, `ModAntiSpider` Schaden), Enthaupten (Köpfe, 25e),
  Elektro entfällt (kein IC2). Die Werte stehen schon im Werkzeug (`InfiTool`-Schlüssel wie in 1.5.2).
- Fähigkeiten: Hammer/Exkavator 3×3, Holzfälleraxt (TreeCapitator), Sense, Streitaxt (Rechtsklick), Langschwert
  (Ausfall + Sprint-Ladeangriff `chargeAttack`), Rapier (Rüstungsdurchschlag `pierceArmor`, Rücksprung), Dolch werfen,
  Hackmesser/Sense/Bratpfanne eigene `onLeftClickEntity`, Bratpfanne abstellen (`EquipBlock`/`FrypanLogic`),
  Meißel (`Detailing`), Kurzbogen + Pfeile, Trankwerfer (`PotionLauncher`, Item noch nicht registriert).
- Blocken: die Waffen mit `EnumAction.block` haben schon `Combat152.BLOCKING`; die Geh-Geschwindigkeit ×2,5 beim
  Blocken (`Weapon.onUpdate`) fehlt noch.
- Schadens-Multiplikator (Hackmesser/Rapier/Sense) läuft in `TEvents` nach dem Blocken des Ziels (combat152 halbiert
  in derselben Ereignis-Stufe); im Original kam er davor. Kleine Abweichung, ggf. in 25b genauer.
- Darstellung: Werkzeug-Modelle aus Schichten je Material (Griff, Kopf, Zubehör, Extra, Effekt1..6, kaputter Kopf),
  Bogen-Spannzustände. Heute zeigt jedes Werkzeug ein festes Symbol (Holzgriff, Eisenkopf). Texturen liegen schon
  unter `textures/item/<werkzeug>/` nur für die Symbole; für alle Materialien in `tools/gen.py` ergänzen.

## 25c – Schmelzerei

- Gussformen (`*_cast`) sind registriert, werden aber erst mit dem Gießtisch nutzbar. Metallteile (Eisen, Cobalt …)
  gibt es nur durch Gießen ("Craft metals with Wood Patterns" = false).
- Werkzeugschmieden: Blöcke sind da, das Rezept braucht versengte Ziegel (`smeltery` 2) und die Metallblöcke
  (`metalBlock`) → mit 25c/25f eintragen (TContent: `bbb / msm / m m`, b = Seared Bricks, s = Werkzeugstation,
  m = Metallblock; Iron/Gold/Diamond/Emerald + "blockCobalt" usw.).
- Leere Gussformen (`blank_cast` aus Duraluminium, `gold_blank_cast` aus Gold) und Schmelzen der Muster.
- Metallblöcke, Erze, Rezepte Barren ↔ Block (`metalBlock` 3,5,6,4,7,0,1,2,8,9).
- Eisenklumpen ist `minecraft:iron_nugget` (alte ID 6035:19 zeigt darauf). Kupfer/Zinn usw. bleiben eigene Items.

## 25d / 25e / 25f

- `TItems.CONSECRATED_SOIL` (geweihte Erde, craftedSoil 4) ist noch `null`: der Bann-Modifikator wird erst mit 25f
  anbringbar.
- Musterkiste mit freien Mustern ("Add Patterns to Pattern Chests" = false im Pack) – nichts zu tun.
- `tinkerHouseChest`/`tinkerHousePatterns` (Dorf, 25d) nutzen die alten IDs, die schon registriert sind.
- Handbuch-Rezepte (`manualBook`) in 25e.

## Client-Prüfung offen

- Alle GUIs (Werkzeugstation mit Knöpfen und Werte-Panel, Schmiede, Teilbauer mit Musterkiste, Schablonentisch,
  Musterkiste, Werkbank-Station), Tischmodelle, Stufen, Item-Symbole, JEI-Kategorien.
- Das Verschieben der Eingabe-Slots je Werkzeug (Reflection auf `Slot.x/y`, nur Client).

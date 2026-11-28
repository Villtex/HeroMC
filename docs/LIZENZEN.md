# Lizenzen der portierten Mods

Stand: 28.11.2026. Ziel: klären, was HeroMC veröffentlichen darf. Keine Rechtsberatung.

Quellen: Lizenzdateien in den Original-Jars des Packs (sicher) sowie Repositories, Foren und CurseForge (meist
neuer als die Versionen von 1.5.2, deshalb oft nur „wahrscheinlich“). „Unklar“ heißt: keine Lizenz gefunden. Rechtlich
gilt dann „alle Rechte vorbehalten“.

Ein Hinweis „inoffizieller Port“ ersetzt keine Lizenz. Die Ports sind nach dem dekompilierten Original geschrieben und
damit abgeleitete Werke. Die Texturen und Sounds im Jar sind die Originale.

## A – frei: veröffentlichen mit Auflagen

| Mod | Lizenz Code | Lizenz Assets | Auflagen | Sicherheit | Quelle |
|---|---|---|---|---|---|
| Hexxit Gear | GPL v3 | wie Code | Port unter GPL v3, Quellcode offen | sicher | `LICENSE` im Jar |
| Backpacks | MMPL 1.0.1 | wie Code | Port unter MMPL, Quellcode kostenlos verfügbar | sicher | `LICENSE.txt` im Jar |
| bspkrsCore, TreeCapitator | CC BY-NC-SA 3.0 | wie Code | Namensnennung, nicht kommerziell, gleiche Lizenz | sicher | `LICENSE` im Jar |
| Inventory Tweaks | MIT | wie Code | Lizenztext und Copyright beilegen | sicher | GitHub Inventory-Tweaks/inventory-tweaks |
| Tinkers' Construct 3.12.1, Mantle | MIT | MIT | Lizenztext beilegen | sicher | Tag v3.12.1.231 |
| Tinkers' Construct 1.4.3 | CC0 | CC BY 3.0 | Namensnennung für die Texturen; Fremd-Texturen siehe unten | wahrscheinlich | README im Zweig 1.7.10 |
| Natura | CC0 | CC BY 3.0 | wie Tinkers | wahrscheinlich | README von progwml6/Natura |
| ExtrabiomesXL | CC BY-SA 3.0 | CC BY-SA 3.0 | Namensnennung, gleiche Lizenz | wahrscheinlich | README des Repositorys |
| The Twilight Forest | LGPL 2.1 | CC BY-NC-SA 4.0 (aus zweiter Hand) | Code unter LGPL, Grafiken nicht kommerziell | wahrscheinlich | LICENSE von 2017 |
| Project Zulu | Apache 2.0 | wie Code | Lizenz und Hinweise beilegen | wahrscheinlich | CurseForge |
| Artifice | frei mit Namensnennung | wie Code | Änderungen offen, keine Werbe-Links | wahrscheinlich | README von 2015 |
| Secret Rooms | LGPL 3.0 | wie Code | Code unter LGPL | wahrscheinlich | Repository von 2019 |
| BetterStorage | MIT | wie Code | Lizenztext beilegen | wahrscheinlich | Repository von 2014 |
| Back Tools (iChun) | GPL v3 | – | Port unter GPL v3 | wahrscheinlich | Repository von 2019 |
| Xeno's Reliquary | GPL | ein Sound CC BY | GPL, Namensnennung | wahrscheinlich | README eines Forks |
| More Bows | gemeinfrei (Angabe des Forks) | CC0 | keine | wahrscheinlich | NeRdTheNed/More-bows |
| Chest Transporter | GPL v3 (Repository von 2018) | – | 1.5.2 war vermutlich ARR | wahrscheinlich | cubex2/chesttransporter |

„Wahrscheinlich“ heißt: Die Lizenz stammt aus einer späteren Version. Ob sie für den Stand von 1.5.2 gilt, ist offen.
Am sichersten fragt man auch hier kurz beim Autor nach.

## B – Erlaubnis nötig

| Mod | Lizenz | Warum | Autor |
|---|---|---|---|
| Obsidian Pressure Plates | CC BY-NC-ND 3.0 (Jar) | „ND“: keine Bearbeitungen | Myrathi |
| Battle Towers, Infernal Mobs, Ruins | eigene Lizenz | Modpacks erlaubt, eigenständige Weitergabe nicht | AtomicStryker |
| ChocoCraft | ARR (CurseForge) | alle Rechte vorbehalten | Torojima |
| BiblioCraft | ARR (CurseForge) | alle Rechte vorbehalten | Nuchaz |
| CoFHCore | später „Don't Be a Jerk“, Grafiken ARR | Version 1.5.2 ohne Lizenz | KingLemmingCoFH |
| Falling Meteors | ARR (Seite eines Forks) | alle Rechte vorbehalten | AlexDGr8r |

## C – unklar, behandeln wie ARR

RottenFleshToLeather (thesbros), Asgard Shield und Addons, Harken Scythe (Jade Knightblazer), ArmorBar (wohl
CrudeDragos), BuffBar, Soul Shards (ShadwDrgn), offLawn (Rhodox, Slinky), Wild Caves 3 (Alexmania), Enchanting Plus
(später LGPL 3 auf CurseForge), Custom LAN Ports (CptHunter), qCraft (dan200, TeacherGaming), MapWriter 2.0.5 (das
spätere Repository unter MIT stammt von anderen Leuten), Damage Indicators (rich1051414), Dimensional Doors
(StevenRS11, SenseiKiwi; der spätere Port steht unter GPL v3), LegendGear (NMcCoy), Better Dungeons (Chocolatin).

## Weitere Rechte

- **`worldgen152`, `combat152`:** Sie bilden Code von Minecraft 1.5.2 (Mojang) bitgenau nach. Die EULA verbietet,
  Minecraft-Code zu verteilen. Eine Nachbildung nach dekompiliertem Code ist eine Grauzone und das größte Risiko
  außerhalb der Mods.
- **Fremd-Texturen:** Tinkers 1.4.3 nennt Fremd-Texturen in `textures/*/Credits.txt`: Thaumium-Teile von NekoGloop,
  Creeper aus dem JohnSmith-Texturpaket, Crystal Guardians von Axolotine.
- **Name „Hexxit“:** Das Pack stammt vom Technic-Team (KakerMix). Eine Marke wurde nicht gefunden. Den Namen nur als
  Bezug verwenden („inspiriert von Hexxit“), nicht als Produktnamen, oder Technic fragen.
- **Mojang:** Keine Minecraft-Dateien mitliefern, nichts verkaufen, nicht nach offiziellem Produkt aussehen.

## Weg zum Veröffentlichen

1. Jedes Teilprojekt `mods/<mod>/` bekommt die Lizenz seines Originals (`LICENSE` und `license` in
   `neoforge.mods.toml`). Share-Alike und GPL lassen keine Sammellizenz fürs ganze Repo zu.
2. Assets nicht mitliefern: Der Spieler holt sich das Original-Pack von Technic, ein Installer zieht die Texturen
   lokal heraus. Der Build macht das schon über `original-assets.txt`. Ausnahme: Assets unter freier Lizenz
   (Tinkers 3, Tinkers 1.4.3, Natura).
3. Für Gruppe B anfragen und Antworten schriftlich ablegen. Wer nicht antwortet: Mod privat lassen oder als eigenes
   Paket nur für Besitzer des Originals.
4. Gruppe C: Autoren suchen (Foren, GitHub, Discord) und fragen. Bis dahin nicht veröffentlichen.
5. Credits-Datei mit allen Originalautoren und der Hinweis „inoffiziell, nicht verbunden mit Mojang oder Technic“.

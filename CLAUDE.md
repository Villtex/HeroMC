# HeroMC – Hinweise für Claude

Projektüberblick und Bauen/Starten stehen in `README.md`.

## Tokens sparen

- Große Logs, Crash-Reports und Gradle-Ausgaben nie selbst komplett lesen. Dafür den Subagent `log-reader` (Haiku) benutzen und nur dessen Zusammenfassung weiterverwenden. Die Fehler selbst analysieren und beheben bleibt Aufgabe der Hauptsession.
- Subagents sind ausdrücklich erlaubt, ohne Rückfrage: immer dann einsetzen, wenn es sich lohnt, zum Beispiel für breite Suchen, Logs, unabhängige Teilaufgaben, die parallel laufen können, oder einen zweiten Blick auf einen kniffligen Port. Modell und Aufwand wählt Claude pro Aufgabe selbst, nur nie den Aufwand `max`. Faustregel: Haiku, niedrig für Lesen und Zusammenfassen; Sonnet 5.5, niedrig bis mittel für mechanische Arbeit; Opus 5.5, mittel bis hoch für normale Port-Arbeit; Fable 5.2, hoch bis extra hoch für bitgenaue Weltgenerierung und festgefahrene Fehler. Bei Subagents, die Code erkunden, die graphify-Regel unten in den Prompt übernehmen.
- Testläufe (Server-Start, `./gradlew runServer`) nur starten, wenn die Änderung dafür fertig ist, nicht nach jeder Kleinigkeit.
- Neue, unabhängige Aufgabe = neue Session. Lange Sessions werden mit jedem Schritt teurer.

## Arbeitsweise beim Portieren

- Die Arbeit ist in Teile aufgeteilt: `docs/PLAN.md`. Sagt der Nutzer „starte Teil N“, den Abschnitt „Regeln für jeden Teil“ und den Abschnitt von Teil N lesen und genau diesen Teil abarbeiten. Am Ende den Status in `docs/PLAN.md` und `README.md` eintragen, committen, pushen.

## Branches

- Hauptbranch ist `ccr-83d04117-2w49l8` (es gibt kein `main`). Jede Session arbeitet auf ihrem eigenen Branch und startet vom aktuellen Hauptbranch (vorher `git fetch` und darauf aufsetzen).
- Am Ende jeder Session, wenn Build und Prüfungen grün sind, mergt die Session ihre Arbeit selbst in den Hauptbranch und löscht danach ihren Branch auf GitHub. Diese Erlaubnis gilt dauerhaft, der Nutzer hat sie ausdrücklich gegeben.
  1. `git fetch origin`, dann den aktuellen Hauptbranch in den eigenen Branch mergen. Konflikte auflösen, danach erneut `CI=true ./gradlew clean pack`.
  2. Per Fast-Forward pushen: `git push origin HEAD:ccr-83d04117-2w49l8`. Kein Force-Push. Wird der Push abgelehnt, weil inzwischen jemand anderes gepusht hat, zurück zu Schritt 1.
  3. Ein offener PR des Branches schließt GitHub dann selbst als gemergt; sonst den PR schließen.
  4. Den eigenen Branch auf GitHub löschen: `git push origin --delete <branch>`.
- Schlägt Build oder Prüfung fehl, nicht mergen. Den Branch stehen lassen, einen Entwurfs-PR öffnen und dem Nutzer sagen, was offen ist.
- Keine Branches auf anderen Branches stapeln. Neue Teile immer vom Hauptbranch aus beginnen.

- Ziel: Hexxit 1.0.11 (MC 1.5.2) 1:1 auf NeoForge 26.3. Zurückgestellt: Twilight Forest, Tinkers' Construct. NEI entfällt, stattdessen JEI-Kompatibilität für alle portierten Mods.
- JDK 25 liegt in der Umgebung "HeroMC" unter `/opt/jdks/jdk25` (Setup-Skript der Umgebung). Bauen mit `CI=true ./gradlew ...`.
- Original-Quellen als Vorlage: `python3 tools/reference.py --only <mod>` (bzw. `--only minecraft` für Vanilla 1.5.2) dekompiliert nach `.reference/<mod>/src`. Nicht committen.
- 26.3-APIs nachschlagen: `javap -p` gegen `mods/*/build/moddev/artifacts/minecraft-patched-26.3*.jar` (Skill `minecraft-modding`, Datei `references/version-bump-method.md`).
- NeoForge-Doku über den Context7-Connector: `/neoforged/documentation`, `/neoforged/moddevgradle`, Javadoc `/websites/nekoyue_github_io_forgejavadocs-ng_javadoc_1_21_x-neoforge`. Die Doku endet vor 26.3: Signaturen immer mit `javap` gegenprüfen.
- MCP-Server `minecraft-dev` (`.mcp.json`, Paket `@mcdxai/minecraft-dev-mcp`): 26.3-Quellen lesen und durchsuchen statt `javap`. Einmal pro Umgebung `decompile_minecraft_version` mit `jarPath` = das patched-Jar oben, `version` `26.3-neoforge-26.3.0.102`, `mapping` `mojmap`; danach `get_minecraft_source` / `search_minecraft_code` mit derselben Version. Ohne MCP geht dasselbe über `minecraft-dev-cli <tool> --key value`.
- `run/server` ist nicht eingecheckt. Vor dem ersten Start `eula.txt` (`eula=true`) und `server.properties` mit `level-seed=1234`, `enable-rcon=true`, `rcon.password=hexxit`, `rcon.port=25575`, `online-mode=false`, `pause-when-empty-seconds=-1` anlegen (sonst pausiert der leere Server nach 60 Sekunden und lädt keine Chunks mehr).
- Terrain-Prüfung: `tools/expect.sh 1234 find 260 > expect.txt`, Server mit `level-seed=1234` starten, `python3 tools/verify_blocks.py expect.txt`. Muss 0 Abweichungen ergeben.
- Verhalten am Server per `python3 tools/rcon.py "<befehl>"` prüfen (RCON-Daten in `run/server/server.properties`).

## Skills anlegen

Wenn eine Arbeit Wissen hervorbringt, das künftige Sessions wieder brauchen, wird daraus selbstständig ein Projekt-Skill unter `.claude/skills/<name>/SKILL.md`, ohne Nachfrage. Gleich mitcommitten.

- **Anlegen, wenn:** dasselbe Vorgehen in mindestens zwei späteren Teilen gebraucht wird (zum Beispiel Mod-Generator in `worldgen152` einhängen, JEI-Plugin, Mob mit neuem Render-System, Vorlagen-Import) oder eine Lösung lange gesucht werden musste und wieder auftreten kann.
- **Nicht anlegen für:** Einmaliges, den Stand eines einzelnen Teils (gehört in `docs/PLAN.md`), oder was schon in `CLAUDE.md`, `docs/PLAN.md` oder im Skill `minecraft-modding` steht.
- **Erst prüfen, ob ein vorhandener Skill passt.** Dann diesen erweitern statt einen neuen anzulegen.
- **Form:** kurz, höchstens etwa 150 Zeilen, konkrete Schritte, Dateipfade und Befehle. Im Frontmatter `name` und eine `description`, die sagt, wann der Skill greift.
- **Bestehende Projekt-Skills** in `README.md` unter „Aufbau“ kurz auflisten, damit der Nutzer sie sieht.

## graphify

This project has a knowledge graph at graphify-out/ with god nodes, community structure, and cross-file relationships.

Rules:
- When graphify-out/graph.json exists, run `graphify query "<question>"` before your first grep, search or source-file read for a codebase question, and put this rule in every subagent prompt that explores code. Read raw files only for lines the graph or an earlier read already located. Use `graphify path "<A>" "<B>"` for relationships and `graphify explain "<concept>"` for focused concepts. These return a scoped subgraph, usually much smaller than GRAPH_REPORT.md or raw grep output.
- If graphify-out/wiki/index.md exists, use it for broad navigation instead of raw source browsing.
- Read graphify-out/GRAPH_REPORT.md only for broad architecture review or when query/path/explain do not surface enough context.
- After modifying code, run `graphify update .` to keep the graph current (AST-only, no API cost).
- A committed graphify-out/graph.json is rewritten whole on every build, so a large diff to it in a pull request is expected. Do not flag it as churn or suggest skipping the graph rebuild or its hooks.

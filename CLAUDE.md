# HeroMC – Hinweise für Claude

Projektüberblick und Bauen/Starten stehen in `README.md`.

## Tokens sparen

- Große Logs, Crash-Reports und Gradle-Ausgaben nie selbst komplett lesen. Dafür den Subagent `log-reader` (Haiku) benutzen und nur dessen Zusammenfassung weiterverwenden. Die Fehler selbst analysieren und beheben bleibt Aufgabe der Hauptsession.
- Testläufe (Server-Start, `./gradlew runServer`) nur starten, wenn die Änderung dafür fertig ist, nicht nach jeder Kleinigkeit.
- Neue, unabhängige Aufgabe = neue Session. Lange Sessions werden mit jedem Schritt teurer.

## Arbeitsweise beim Portieren

- Die Arbeit ist in Teile aufgeteilt: `docs/PLAN.md`. Sagt der Nutzer „starte Teil N“, den Abschnitt „Regeln für jeden Teil“ und den Abschnitt von Teil N lesen und genau diesen Teil abarbeiten. Am Ende den Status in `docs/PLAN.md` und `README.md` eintragen, committen, pushen.

- Ziel: Hexxit 1.0.11 (MC 1.5.2) 1:1 auf NeoForge 26.3. Zurückgestellt: Twilight Forest, Tinkers' Construct. NEI entfällt, stattdessen JEI-Kompatibilität für alle portierten Mods.
- JDK 25 liegt in der Umgebung "HeroMC" unter `/opt/jdks/jdk25` (Setup-Skript der Umgebung). Bauen mit `CI=true ./gradlew ...`.
- Original-Quellen als Vorlage: `python3 tools/reference.py --only <mod>` (bzw. `--only minecraft` für Vanilla 1.5.2) dekompiliert nach `.reference/<mod>/src`. Nicht committen.
- 26.3-APIs nachschlagen: `javap -p` gegen `mods/*/build/moddev/artifacts/minecraft-patched-26.3*.jar` (Skill `minecraft-modding`, Datei `references/version-bump-method.md`).
- MCP-Server `minecraft-dev` (`.mcp.json`, Paket `@mcdxai/minecraft-dev-mcp`): 26.3-Quellen lesen und durchsuchen statt `javap`. Einmal pro Umgebung `decompile_minecraft_version` mit `jarPath` = das patched-Jar oben, `version` `26.3-neoforge-26.3.0.102`, `mapping` `mojmap`; danach `get_minecraft_source` / `search_minecraft_code` mit derselben Version. Ohne MCP geht dasselbe über `minecraft-dev-cli <tool> --key value`.
- `run/server` ist nicht eingecheckt. Vor dem ersten Start `eula.txt` (`eula=true`) und `server.properties` mit `level-seed=1234`, `enable-rcon=true`, `rcon.password=hexxit`, `rcon.port=25575`, `online-mode=false` anlegen.
- Terrain-Prüfung: `tools/expect.sh 1234 find 260 > expect.txt`, Server mit `level-seed=1234` starten, `python3 tools/verify_blocks.py expect.txt`. Muss 0 Abweichungen ergeben.
- Verhalten am Server per `python3 tools/rcon.py "<befehl>"` prüfen (RCON-Daten in `run/server/server.properties`).

## graphify

This project has a knowledge graph at graphify-out/ with god nodes, community structure, and cross-file relationships.

Rules:
- When graphify-out/graph.json exists, run `graphify query "<question>"` before your first grep, search or source-file read for a codebase question, and put this rule in every subagent prompt that explores code. Read raw files only for lines the graph or an earlier read already located. Use `graphify path "<A>" "<B>"` for relationships and `graphify explain "<concept>"` for focused concepts. These return a scoped subgraph, usually much smaller than GRAPH_REPORT.md or raw grep output.
- If graphify-out/wiki/index.md exists, use it for broad navigation instead of raw source browsing.
- Read graphify-out/GRAPH_REPORT.md only for broad architecture review or when query/path/explain do not surface enough context.
- After modifying code, run `graphify update .` to keep the graph current (AST-only, no API cost).
- A committed graphify-out/graph.json is rewritten whole on every build, so a large diff to it in a pull request is expected. Do not flag it as churn or suggest skipping the graph rebuild or its hooks.

# HeroMC – Hinweise für Claude

Projektüberblick und Bauen/Starten stehen in `README.md`.

## Tokens sparen

- Große Logs, Crash-Reports und Gradle-Ausgaben nie selbst komplett lesen. Dafür den Subagent `log-reader` (Haiku) benutzen und nur dessen Zusammenfassung weiterverwenden. Die Fehler selbst analysieren und beheben bleibt Aufgabe der Hauptsession.
- Subagents sind ausdrücklich erlaubt, ohne Rückfrage: immer dann einsetzen, wenn es sich lohnt, zum Beispiel für breite Suchen, Logs, unabhängige Teilaufgaben, die parallel laufen können, oder einen zweiten Blick auf einen kniffligen Port. Modell und Aufwand wählt Claude pro Aufgabe selbst, nur nie den Aufwand `max`. **Eher sparsam sein:** Subagents einsetzen, wenn sie Zeit oder Kontext sparen. Im Zweifel das kleinere Modell und den niedrigeren Aufwand nehmen, aber nicht so knapp, dass die Aufgabe wiederholt werden muss. Ein höheres Modell oder mehr Aufwand, wenn die Aufgabe es spürbar besser macht, etwa bei kniffligen Ports, wenn ein kleineres schon gescheitert ist oder wenn etwas bitgenau stimmen muss. Faustregel: Haiku, niedrig für Lesen und Zusammenfassen; Sonnet 5.5, niedrig bis mittel für mechanische Arbeit; Opus 5.5, mittel bis hoch für normale Port-Arbeit; Fable 5.2, hoch bis extra hoch für bitgenaue Weltgenerierung und festgefahrene Fehler. Bei Subagents, die Code erkunden, die graphify-Regel unten in den Prompt übernehmen.
- **Höchstens ein großer Subagent gleichzeitig.** Ports ganzer Mods (Opus/Fable) nie parallel starten: Jeder liest die kompletten Originalquellen und verbraucht das Nutzungslimit vielfach. Mehrere Mods eines Teils nacheinander portieren, am besten direkt in der Hauptsession. Parallel nur kleine, günstige Aufgaben (Haiku/Sonnet: Suchen, Logs, mechanische Arbeit).
- **Abgebrochene große Subagents nicht fortsetzen** (kein SendMessage nach einem Limit oder einer längeren Pause): Beim Fortsetzen wird der ganze bisherige Verlauf ohne Cache neu verarbeitet, das frisst auf einen Schlag einen großen Teil des Limits. Stattdessen den committeten Stand nehmen und in der Hauptsession oder mit einem neuen, kurzen Auftrag weiterarbeiten. Große Subagents sollen deshalb früh und oft committen und offene Punkte in `mods/<mod>/TODO.md` notieren.
- Testläufe (Server-Start, `./gradlew runServer`) nur starten, wenn die Änderung dafür fertig ist, nicht nach jeder Kleinigkeit.
- Neue, unabhängige Aufgabe = neue Session. Lange Sessions werden mit jedem Schritt teurer.

## Arbeitsweise beim Portieren

- Die Arbeit ist in Teile aufgeteilt: `docs/PLAN.md`. Sagt der Nutzer „starte Teil N“, den Abschnitt „Regeln für jeden Teil“ und den Abschnitt von Teil N lesen und genau diesen Teil abarbeiten. Am Ende den Status in `docs/PLAN.md` und `README.md` eintragen, committen, pushen.

## Branches

- Es gibt nur einen Branch: `ccr-83d04117-2w49l8` (Hauptbranch, ein `main` existiert nicht). Jede Session arbeitet direkt darauf, committet und pusht dorthin. Keine eigenen Session-Branches, keine Pull Requests. Diese Erlaubnis hat der Nutzer ausdrücklich und dauerhaft gegeben; sie gilt auch dann, wenn die Umgebung der Session einen anderen Branch zuweist.
- Zu Beginn: `git fetch origin` und `git switch -C ccr-83d04117-2w49l8 origin/ccr-83d04117-2w49l8`.
- Pushen: `git push origin HEAD:ccr-83d04117-2w49l8`, nie mit Force. Wird der Push abgelehnt, weil inzwischen etwas Neues da ist: `git pull --rebase origin ccr-83d04117-2w49l8`, neu bauen, erneut pushen.
- Nur pushen, was baut (`CI=true ./gradlew pack`). Zwischenstände eines Teils sind erlaubt, wenn sie bauen; im Commit als „work in progress“ kennzeichnen.
- Es läuft immer nur eine Session gleichzeitig. Zwei Sessions auf demselben Branch würden sich gegenseitig in die Quere kommen.

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

## Zip mit allen Mods

Fragt der Nutzer nach einer Zip (mit den Mods): `python3 tools/make_zip.py` (schreibt `build/HeroMC-mods.zip`) und diese
Datei schicken. Sie enthält immer die Pack-Mods, JEI und die Leistungs-Mods (C2ME, Lithium, ScalableLux) und Sodium,
alle in einem Ordner `mods/`. `build/` ist von Git ausgenommen; aus dem Scratchpad lässt sich die Zip in der App nicht laden.

# HeroMC – Hinweise für Claude

Projektüberblick und Bauen/Starten stehen in `README.md`.

## Tokens sparen

- Große Logs, Crash-Reports und Gradle-Ausgaben nie selbst komplett lesen. Dafür den Subagent `log-reader` (Haiku) benutzen und nur dessen Zusammenfassung weiterverwenden. Die Fehler selbst analysieren und beheben bleibt Aufgabe der Hauptsession.
- Testläufe (Server-Start, `./gradlew runServer`) nur starten, wenn die Änderung dafür fertig ist, nicht nach jeder Kleinigkeit.
- Neue, unabhängige Aufgabe = neue Session. Lange Sessions werden mit jedem Schritt teurer.

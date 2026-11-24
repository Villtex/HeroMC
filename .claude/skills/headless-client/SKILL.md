---
name: headless-client
description: Starting the test client (./gradlew runClient) in the cloud container without a screen, e.g. to look at rendering bugs on screenshots. Applies when a client check is really needed; often reading the code is enough.
---

# Starting the test client without a screen

Only do this when the code alone does not settle the question: starting takes several minutes, and the
software renderer (llvmpipe) is slow.

## What does not work and why

- Plain `Xvfb`: NeoForge's early window and Minecraft (SDL3, `GlBackend`) fail with
  "Couldn't find matching GLX visual".
- Reason: Minecraft requests an sRGB framebuffer (`SDL_GL_SetAttribute(22, 1)`), and the Xvfb GLX of
  Ubuntu 24.04 offers no sRGB config (`glxinfo` -> column "sr" never `y`).
- Vulkan is not available (no `VK_KHR_surface`).

## Recipe

```bash
apt-get install -y xdotool mesa-utils libgl1-mesa-dri libglx-mesa0 libegl-mesa0
Xvfb :99 -screen 0 1280x720x24 +extension GLX +render -noreset &
# turn off the early window (fails on Xvfb); run/ is not in git
sed -i 's/earlyWindowControl = true/earlyWindowControl = false/' run/client/config/fml.toml
DISPLAY=:99 LIBGL_ALWAYS_SOFTWARE=1 SDL_VIDEO_FORCE_EGL=1 nohup ./gradlew runClient > <scratchpad>/client.log 2>&1 &
```

- `SDL_VIDEO_FORCE_EGL=1` is the crucial part: over EGL, Mesa provides sRGB and the context comes up.
- `run/client/config/fml.toml` only exists after the first start; if it is missing, start once, end it, then edit.
- Check readiness in the log (`Created: ... atlas` lines, no `Failed to create backend`), do not read the log in full.
- Screenshot: `DISPLAY=:99 import -window root -resize 1280x720 <scratchpad>/shot.png`, then look at it with Read.
- Input: `xdotool` (`mousemove`, `click 1`, `type`, `key`) on `DISPLAY=:99`.
- Shutdown: `pkill -f net.neoforged; pkill Xvfb`.

## In the game (tested: Twilight portal, Enchanting Plus table)

- The window is 854×480 in the 1280×720 screen; a screenshot scaled to 960×540 maps to the screen by ×1.333.
- Title → Singleplayer (≈637,323) → Game Mode twice = Creative (commands on) → Create New World (≈480,567); ~90 s.
- Commands: `xdotool key slash; xdotool type --delay 15 "<cmd>"; xdotool key Return`. Only near the player
  (otherwise "That position is not loaded"); results come in the log as `[CHAT]` lines.
- Look at a block with `tp @s x y z <yaw> <pitch>` (clicks do not turn the camera), right click `xdotool click 3`,
  pick hotbar slot with `xdotool key 3`.
- Drag in a GUI: `mousedown 1`, several `mousemove`, `mouseup 1`.
- A temporary `LOGGER.info` in the screen code shows what arrives (never commit it).

## Dem Testserver beitreten (Teil 16)

- Server starten (`run/server/server.properties` mit RCON, `online-mode=false`), dann im Root-`build.gradle` beim
  `client`-Run vorübergehend `programArguments.addAll('--quickPlayMultiplayer', 'localhost')` (nicht committen).
- Erster Start zeigt den Willkommens-/Barrierefreiheits-Bildschirm: „Continue“ klicken (bei 1280×720 etwa 638,567).
- „not white-listed“: `rcon whitelist add Dev`, dann im Server-Menü den LAN-Eintrag doppelklicken.
- Szene per RCON bauen, Spieler mit `tp Dev x y z <yaw> <pitch>` ausrichten (`facing` traf nicht zuverlässig),
  Ausschnitt: `import -window root -crop 854x480+213+120 shot.png`. Die Maus dreht die Kamera nicht (kein Fokus),
  Klicks (`xdotool click 3` = Rechtsklick auf das Fadenkreuz) und `key Escape` gehen.
- `./gradlew --stop` (auch von Subagents) beendet laufenden Server und Client mit.

## Erfahrungen aus Teil 22

- Beitritt ohne Klicken im Menü: im Root-`build.gradle` beim `client`-Run
  `programArguments.addAll('--quickPlayMultiplayer', 'localhost', '--username', 'Dev')` (nicht committen,
  nach dem Test `git checkout build.gradle`). Danach einmal „Continue“ bei 638,567 klicken.
- **Rechtsklick (`xdotool click 3`) wird erst nach einem vorangegangenen `click 1` zugestellt** (Fokus). Also
  erst links in die GUI klicken, dann rechts. Tastatureingaben gehen sonst ins Spiel statt in den Dialog.
- Szene bauen: `tp Dev <x> <y> <z> <yaw> <pitch>`, Mob mit `{NoAI:1b,NoGravity:1b}` auf feste Koordinaten
  (`NoAI` allein verhindert das Fallen nicht). Ins Fadenkreuz zielen heißt Neigung ausrechnen, nicht raten.
- Nachts stürzt der Client an Mod-Entities ab, die auf dem Client `move()` rufen: `gamerule doDaylightCycle false`
  und `time set noon` setzen, dann bleibt die Szene stabil und hell genug für Screenshots.
- Ausschnitt vergrößern: `import` kennt `-scale` nicht, deshalb `import -window root shot.png` und danach
  `convert shot.png -crop BxH+X+Y -scale 250% zoom.png`.

## Einzelspielerwelt direkt laden (Teil 26e)

- Welt des Testservers kopieren: `cp -r run/server/world run/client/saves/t`, im Root-`build.gradle` beim `client`-Run
  vorübergehend `programArguments.addAll('--quickPlaySingleplayer', 't', '--username', 'Dev')`, danach `git checkout build.gradle`.
- Fenster bei 1280×720: Spielbild 854×480 bei +213+120. „Continue“ 638,567; im Spiel `key Escape` → Pausenmenü,
  „World Options…“ etwa 800,425. Laden dauert mit llvmpipe einige Minuten (Ladebalken abwarten).

## Sodium und Iris im Testclient

- `runClient` lädt Sodium (eigenes Source-Set `clientRun` im Root-`build.gradle`, nur der Client-Run; der Server bleibt
  ohne). Vanilla-Renderer zum Vergleich: `./gradlew runClient -Psodium_iris=false`. Versionen in `gradle.properties`.
- Darstellungsfehler erst gegen Vanilla gegenprüfen: dieselbe Szene, dieselbe `tp`-Position, Ausschnitte mit
  `convert a.png b.png -append cmp.png` vergleichen. Animierte Dinge (Glühwürmchen pulsiert, Wasser) mit einer
  Bildserie (`import -window root -crop WxH+X+Y` in einer Schleife) statt einem Einzelbild.
- Iris mit Shaderpack: Pack nach `run/client/shaderpacks/`, in `run/client/config/iris.properties`
  `shaderPack=<datei>.zip`. Stand 26.3.0.102: Iris 1.11.7 stürzt in `FrontendRenderPass.validateDraw` ab, auch ohne
  Hexxit-Mods. Gegenprobe ohne eigene Mods: vorübergehender Run mit `loadedMods = []` und eigenem `gameDirectory`.
- Bei `tutorialStep:none` in `run/client/options.txt` verdeckt kein Tutorial-Hinweis die rechte Bildhälfte.

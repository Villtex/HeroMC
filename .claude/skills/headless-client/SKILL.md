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

As of the first test: the client got as far as loading textures. Creating a world and moving in-game have not been tried yet.
Add to this skill once that is done.

## Dem Testserver beitreten (Teil 16)

- Server starten (`run/server/server.properties` mit RCON, `online-mode=false`), dann im Root-`build.gradle` beim
  `client`-Run vorübergehend `programArguments.addAll('--quickPlayMultiplayer', 'localhost')` (nicht committen).
- Erster Start zeigt den Willkommens-/Barrierefreiheits-Bildschirm: „Continue“ klicken (bei 1280×720 etwa 638,567).
- „not white-listed“: `rcon whitelist add Dev`, dann im Server-Menü den LAN-Eintrag doppelklicken.
- Szene per RCON bauen, Spieler mit `tp Dev x y z <yaw> <pitch>` ausrichten (`facing` traf nicht zuverlässig),
  Ausschnitt: `import -window root -crop 854x480+213+120 shot.png`. Die Maus dreht die Kamera nicht (kein Fokus),
  Klicks (`xdotool click 3` = Rechtsklick auf das Fadenkreuz) und `key Escape` gehen.
- `./gradlew --stop` (auch von Subagents) beendet laufenden Server und Client mit.

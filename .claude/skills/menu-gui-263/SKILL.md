---
name: menu-gui-263
description: Beim Portieren einer eigenen GUI mit Container eines Hexxit-Mods auf 26.3 benutzen (GuiContainer + Container + IGuiHandler, Pakete vom Client an den Server, Server-Konfiguration, die der Client für eine Vorschau braucht). Greift bei Maschinen, Werkbänken, Tischen, Taschen. Beispiel im Repo: mods/enchantingplus (eplus).
---

# Menü, Screen und Pakete in 26.3 (NeoForge 26.3.0.102)

Vorlage: `mods/enchantingplus/src/main/java/eplus/` (`EnchantTableMenu`, `client/EnchantTableScreen`,
`network/*`, `EplusConfig`). Signaturen mit `javap` gegen
`~/.gradle/caches/neoformruntime/intermediate_results/applyDevTransforms_*_output.jar` und das NeoForge-Jar
`neoforge-26.3.0.102-universal.jar` prüfen; FML-Klassen (`ModConfig`) liegen in `fancymodloader/loader-*.jar`.

## Container → `AbstractContainerMenu`

- `DeferredRegister.create(Registries.MENU, MOD_ID)`, Typ mit Zusatzdaten:
  `IMenuTypeExtension.create((id, inv, buf) -> new MyMenu(id, inv, buf.readBlockPos()))`.
- Öffnen (Server): `serverPlayer.openMenu(new SimpleMenuProvider((id, inv, p) -> new MyMenu(id, inv, pos), title), buf -> buf.writeBlockPos(pos))`.
  Ersetzt `player.openGui(...)` und den `IGuiHandler`.
- Eigenes Inventar: `new SimpleContainer(n) { setChanged() { super.setChanged(); menu.slotsChanged(this); } }` = `onCraftMatrixChanged`.
  Läuft auf beiden Seiten (der Client bekommt Slot-Pakete → `setItem` → `setChanged`).
- `stillValid(ContainerLevelAccess.create(level, pos), player, block)`; `removed()` → `clearContainer(player, inv)`
  (1.5.2 warf Gegenstände mit `dropPlayerItem` aus, 26.3 gibt sie ins Inventar zurück).
- `quickMoveStack` statt `transferStackInSlot`; wird in einer Schleife aufgerufen, solange dasselbe Item zurückkommt.
- Kleine Knöpfe ohne Daten: Client `minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id)`,
  Server `clickMenuButton(player, id)` (nur bei `player instanceof ServerPlayer` handeln).

## GuiContainer → `AbstractContainerScreen`

- Größe im Konstruktor: `super(menu, inv, title, xSize, ySize)` (`imageWidth/Height` sind final).
- `drawGuiContainerBackgroundLayer` → `extractBackground(g, mx, my, a)` (erst `super`, dann
  `g.blit(RenderPipelines.GUI_TEXTURED, TEX, leftPos, topPos, u, v, w, h, 256, 256)`).
- Kein Titel/„Inventory“: `extractLabels` leer überschreiben.
- `drawScreen` nach `super` → `extractRenderState` nach `super.extractRenderState(...)`.
- `updateScreen` → `containerTick()`; `initGui` → `init()` mit `addRenderableWidget(Button.builder(...).bounds(...).build())`.
- `drawRect` → `g.fill(x1, y1, x2, y2, argb)`; `fontRenderer.drawString` → `g.text(font, s, x, y, argb, shadow)`
  (Farben mit Alpha wie im Original übernehmen).
- `Mouse.isButtonDown(0)` → eigenes Flag aus `mouseClicked/mouseReleased(MouseButtonEvent)`;
  `Mouse.getEventDWheel` → `mouseScrolled(x, y, scrollX, scrollY)` (unten = negativ).
- Shift: `this.minecraft.hasShiftDown()`. Übersetzung: `I18n.get(key)`, Existenz: `Language.getInstance().has(key)`.
- Tooltip an der Maus: `g.setTooltipForNextFrame(font, List<FormattedCharSequence>, x, y)` (nur einer je Frame).
  Mehrere feste Kästen wie `drawHoveringText` von 1.5.2: `TooltipRenderUtil.extractTooltipBackground(g, x, y, w, h, null)`
  + `g.text(...)`, siehe `EnchantTableScreen.drawHoveringText`.
- Registrieren: Mod-Bus `RegisterMenuScreensEvent` → `event.register(MENU.get(), MyScreen::new)` (nur im Client laden).
- Knopf in eine Vanilla-GUI: `ScreenEvent.Init.Post` → `event.addListener(button)`; zeichnen: `ScreenEvent.Render.Post`.

## Pakete → Custom Payloads

- `record X(...) implements CustomPacketPayload` mit `Type` und `StreamCodec` (`StreamCodec.composite`, `ByteBufCodecs.map(HashMap::new, Enchantment.STREAM_CODEC, VAR_INT, max)`, `StreamCodec.unit(INSTANCE)`).
- Mod-Bus `RegisterPayloadHandlersEvent`: `event.registrar("1").playToServer(TYPE, CODEC, handler)`.
  Server→Client ohne Handler registrieren (`playToClient(TYPE, CODEC)`) und den Handler im Client über
  `RegisterClientPayloadHandlersEvent.register(TYPE, handler)` setzen, damit der Server keine Client-Klassen lädt.
- Senden: `ClientPacketDistributor.sendToServer(p)`, `PacketDistributor.sendToPlayer(serverPlayer, p)`.
- **Server prüft alles neu** (Menü offen und gültig, Werte im erlaubten Bereich, Kosten neu rechnen); das Original
  vertraute dem Client oft.

## Konfiguration und Spielerdaten

- Server-Werte, die der Client für Vorschauen braucht: `ModConfig.Type.SYNCED` (in 26.3 gibt es kein `SERVER` mehr),
  wird beim Login an den Client geschickt. Vor dem Laden wirft `.get()`; mit `spec.isLoaded()` auf Standardwerte ausweichen.
- `player.getEntityData()` → Attachment: `AttachmentType.builder(() -> def).serialize(Codec.BOOL.fieldOf("value")).copyOnDeath()`,
  Register `NeoForgeRegistries.Keys.ATTACHMENT_TYPES`.
- Item-NBT des Mods → eigene Data Component (`persistent` + `networkSynchronized`, wenn die GUI sie liest).

## Vanilla-Block ersetzen (Mod hat Block-ID überschrieben)

- Rechtsklick: `PlayerInteractEvent.RightClickBlock` (Hauptehand, nicht bei Schleichen mit Item) auf beiden Seiten
  abbrechen, Server öffnet das Menü (siehe Skill `client-render-263`).
- Lichtwert: steht pro `BlockState` im finalen Feld `BlockStateBase.lightEmission` → per Reflection setzen
  (`EnchantingPlus.applyLight`), auf Server (`ServerAboutToStartEvent`) und Client (`ClientPlayerNetworkEvent.LoggingIn`).
- `randomDisplayTick`-Partikel: kein Hook am Vanilla-Block → im `ClientTickEvent.Post` wie `ClientLevel.animateTick`
  667× in Radius 16 und 32 Blöcke ziehen und bei Treffer die Partikel des Originals erzeugen.

## Truhen-GUIs und Inventory Tweaks

Inventory Tweaks (`mods/inventorytweaks`) zeigt Sortier-Knöpfe und sortiert per Middle-Click in Truhen-Menüs.
Menüs, die von `ChestMenu`, `ShulkerBoxMenu` oder `DispenserMenu` erben, erkennt es selbst. Eigene Truhen-Screens
(BetterStorage, Backpacks, BiblioCraft) im `InterModEnqueueEvent` anmelden, ohne Abhängigkeit:
`InterModComms.sendTo("inventorytweaks", "chest", () -> "voller.Klassenname.DesScreens;9")` (Zahl = Zeilenbreite).

## Eigener Rezepttyp für eine Maschine (Vorbild: Harken-Scythe-Altäre)

- Typ + Serializer registrieren, Rezepte als Daten-JSON (`gen.py`). `placementInfo()` → `PlacementInfo.create(ingredient)`,
  sonst warnt der Server „can't be placed“.
- Rezepte zum Client: `OnDatapackSyncEvent` → `event.sendRecipes(TYPE)`; auf dem Client `RecipesReceivedEvent`
  (Priorität HIGHEST, vor JEI) zwischenspeichern. JEI-Kategorie mit `IRecipeHolderType`.
- Vom Server berechnete Werte (Essenz, Kosten, Rezeptnummer) über `ContainerData` an den Screen, Knopf über
  `clickMenuButton` – keine eigenen Pakete nötig. `mods/harkenscythe/.../altar`.

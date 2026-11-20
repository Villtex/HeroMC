---
name: client-render-263
description: Beim Portieren von Client-Darstellung eines Hexxit-Mods auf 26.3 benutzen. Greift bei HUD-Anzeigen (Tick-Handler mit TickType.RENDER, Overlays), Gegenständen oder Modellen am Spielermodell (Rücken, Schild, Rucksack), Rüstungen (ItemArmor mit eigener Textur oder eigenem Helm-Modell, getArmorModel), durchscheinenden oder halbtransparenten Blöcken (ISimpleBlockRenderingHandler mit Alpha), und bei Rechtsklick-Logik, die vor dem Block greifen muss (PlayerInteractEvent), sowie bei Blöcken, die das Modell eines anderen Blocks zeigen (Tarnblöcke, Mikroblöcke). Beispiele im Repo: armorbar, buffbar, backtools, obsidiplates, chesttransporter, hexxitgear.
---

# Client-Darstellung in 26.3 (NeoForge 26.3.0.102)

Ergänzt den Skill `minecraft-modding` („GUI / render cookbook“). Quellen nachlesen: MCP `minecraft-dev`
(`get_minecraft_source`, Version `26.3-neoforge-26.3.0.102`, mojmap) und das NeoForge-Quell-Jar
`~/.gradle/caches/modules-2/files-2.1/net.neoforged/neoforge/26.3.0.102/*/neoforge-26.3.0.102-sources.jar`
(entpacken, dann `grep`).

## Nur-Client-Mod sauber laden

- Mod-Konstruktor `(IEventBus modBus, ModContainer container)`; Client-Klassen nur hinter
  `if (FMLEnvironment.getDist() == Dist.CLIENT) client.X.register(modBus);` ansprechen. Der Server lädt den Mod dann ohne Fehler.
- Einstellungen der alten `.cfg`: `ModConfigSpec` + `container.registerConfig(ModConfig.Type.CLIENT, SPEC)`. Standardwerte
  aus `z-hexxit-configs`. Achtung: manche Originale lasen ihre Konfig nie (BuffBar: statische Felder vor `preInit`) – im Plan notieren.

## HUD (statt `ITickHandler` mit `TickType.RENDER`)

```java
modBus.addListener(RegisterGuiLayersEvent.class, e -> e.registerAbove(VanillaGuiLayers.HOTBAR, id, MyHud::render));
static void render(GuiGraphicsExtractor g, DeltaTracker delta) { ... }   // Minecraft.getInstance().gui.screen() != null -> nichts zeichnen
```

- `g.guiWidth()/guiHeight()` = `ScaledResolution`. 1.5.2-`drawTexturedModalRect(x,y,u,v,w,h)` mit 256er-Textur:
  `g.blit(RenderPipelines.GUI_TEXTURED, tex, x, y, u, v, w, h, texW, texH[, argb])` (echte Texturgröße angeben).
- Effektsymbole (früher `/gui/inventory.png` ab y=198): `g.blitSprite(RenderPipelines.GUI_TEXTURED, Hud.getMobEffectSprite(effect), x, y, 18, 18)`.
- Text: `g.text(font, s, x, y, 0xFF000000 | rgb, false)` – Farbe braucht Alpha, `drawString` von 1.5.2 hatte keinen Schatten.
- Gegenstand: `g.fakeItem(stack, x, y)`. Skalieren: `g.pose()` ist `Matrix3x2fStack` (`pushMatrix/translate/scale/popMatrix`).
- Was „alle N Frames“ aktualisierte, auf `ClientTickEvent.Post` (NeoForge-Bus) legen.

## Mob im HUD zeigen (Portrait, Vorschaukasten)

Vorlage: `mods/damageindicators/client/Portrait.java`.
- `InventoryScreen.renderEntityInInventoryFollowsAngle(g, x0, y0, x1, y1, size, 0.0625F, xAngle, yAngle, entity)`.
  `size` = GUI-Pixel je Block (Vanilla: Spieler 1,8 Blöcke mit `size` 30 in einem Kasten von 70). `xAngle`/`yAngle`
  werden mit 20 multipliziert und ergeben `bodyRot = 180 + xAngle*20`; `1.5F` gibt eine Dreiviertelansicht.
- **`g.entity(...)` folgt `g.pose()` nicht** (die Render-State merkt sich nur die rohen Koordinaten und den
  Scissor-Stack). Steht das HUD unter `pose().translate/scale`, den Kasten selbst in Bildschirmkoordinaten
  umrechnen (`x0 = (1-s)*loc + s*(loc+offset)`, Breite `w*s`) und `size` mit skalieren.
- **Nichts schneidet zu:** vorher `g.enableScissor(x0,y0,x1,y1)`, nachher `g.disableScissor()`, sonst ragt der Mob
  aus dem Rahmen. Lange Mobs (Kuh) sind breiter als ihre Hitbox: `size` aus `max(bbHeight, bbWidth*1.4)` und nur
  etwa 45 % der Kastenhöhe, sonst stößt das Modell an.

## Fremde Effekte, Leben und Bewegung auf dem Client

- 26.3 schickt dem Client **nur die eigenen Mob-Effekte**: `getActiveEffects()` eines anderen Lebewesens ist dort
  leer. Wer sie anzeigen will (Lebensanzeigen, Portraits), braucht eine eigene Payload vom Server
  (Vorlage `mods/damageindicators/DINetwork.java`: alle zwei Ticks je Spieler, nur bei Änderung). Leben,
  Maximalleben und Namen synchronisiert das Spiel von selbst.
- Eine vom Server verfolgte Entity darf auf dem Client **nicht `move()` aufrufen**, sonst
  `IllegalStateException: Attempted to move entity on a logical side not permitted to simulate movement`.
  Schwerkraft und `move` in `tick()` mit `if (!level().isClientSide())` klammern; der Client bekommt die Position
  aus den Paketen und darf `getDeltaMovement()` nur lesen (gefunden an `legendgear` FallingStarEntity).

## Etwas am Spielermodell zeichnen (Rücken, Schultern)

1. Zustand pro Spieler im Client-Tick führen (`mc.level.players()`, Map nach `getId()`, beim Weltwechsel leeren).
2. In den Render-State bringen:
   ```java
   static final ContextKey<T> KEY = new ContextKey<>(Identifier.fromNamespaceAndPath(MOD, "name"));
   modBus.addListener(RegisterRenderStateModifiersEvent.class, e -> e.registerAvatarEntityModifier(new AvatarRenderStateModifier() {
       public <A extends Avatar & ClientAvatarEntity> void accept(A avatar, AvatarRenderState s) { s.setRenderData(KEY, ...); }
   }));
   ```
   Item-Modell im Extract auflösen: `new ItemStackRenderState()` +
   `Minecraft.getInstance().getItemModelResolver().updateForLiving(state, stack, ItemDisplayContext.NONE, avatar)`.
   (`ClientAvatarEntity` liegt in `net.minecraft.client.entity`.)
3. Ebene anhängen: `EntityRenderersEvent.AddLayers` → für jedes `event.getSkins()` `event.getPlayerRenderer(skin).addLayer(new MyLayer(r))`;
   `MyLayer extends RenderLayer<AvatarRenderState, PlayerModel>`, `submit(PoseStack, SubmitNodeCollector, light, state, yRot, xRot)`.
4. Im Layer: `getParentModel().body.translateAndRotate(poseStack)` (folgt Schleichen/Schwimmen). Modellraum: y nach unten,
   +z = Rücken, Hals bei y=0, Füße 24 px tiefer, Spielerskala 0,9375. 1.5.2-Weltkoordinaten (y hoch, Rücken −z):
   `poseStack.rotateDegrees(Axis.XP, 180)`. Drehen nur mit `poseStack.rotateDegrees(Axis.?, deg)` (`mulPose(Quaternion)` gibt es nicht mehr).
   `ItemDisplayContext.NONE`-Modell: die Verschiebung von 0..1 zur Mitte macht Vanilla schon selbst (`ItemTransform.apply`), **nicht** noch einmal `translate(-0.5,-0.5,-0.5)` (sonst sitzt es einen halben Block daneben, z. B. vor dem Kopf); seine Vorderseite zeigt nach +z.
   `item.submit(poseStack, collector, light, OverlayTexture.NO_OVERLAY, state.outlineColor)`. Body in try/catch, einmal loggen.
   Umhang sichtbar: `state.showCape && state.skin.cape() != null`; unsichtbar: `state.isInvisible`.

## Rüstung: Texturen und eigene Helm-Modelle (`ItemArmor`, `getArmorTexture`, `getArmorModel`)

Beispiel: `mods/hexxitgear` (`HexxitGear.diamond(...)`, `client/HelmetModels`, `client/HexxitGearClient`).
- Werte: `new ArmorMaterial(durability, defense, enchantability, equipSound, toughness, knockback, repairTag, assetKey)`,
  Item mit `p.humanoidArmor(material, ArmorType.HELMET)`. `EnumArmorMaterial.DIAMOND` = `ArmorMaterials.DIAMOND` (33, 3/8/6/3, 10).
  Asset-Schlüssel: `ResourceKey.create(EquipmentAssets.ROOT_ID, id)`; je Textur ein eigenes Material (gleiche Werte).
- `assets/<mod>/equipment/<name>.json`: `{"layers":{"humanoid":[{"texture":"<mod>:<name>"}],"humanoid_leggings":[...]}}`.
  1.5.2 `xxx.png` (Schicht 1: Helm, Brust, Stiefel) → `textures/entity/equipment/humanoid/<name>.png`, `xxx2.png`
  (Hose) → `textures/entity/equipment/humanoid_leggings/<name>.png`. Ohne `humanoid_baby` tragen Baby-Mobs sie unsichtbar.
- Verzauberbar nur über Tags: `#minecraft:enchantable/head_armor` (chest/leg/foot), `enchantable/equippable`,
  `enchantable/durability`. Nicht in `#minecraft:head_armor` usw. eintragen, sonst werden sie über `trimmable_armor` verzierbar.
- Eigenes Modell: `RegisterClientExtensionsEvent` → `IClientItemExtensions.getHumanoidArmorModel(stack, layerType, original)`
  gibt ein `HumanoidModel` zurück (alle sieben Teile leer, Würfel als Kinder von `head`; `LayerDefinition.create(mesh, texW, texH).bakeRoot()`,
  einmal lazy bauen). `setupAnim` ruft der Renderer selbst. `ModelRenderer.render(f5 + d)` des Originals vergrößert Würfel **und**
  Drehpunkt um `(f5+d)/f5`: `PartPose.offset(px*s, py*s, pz*s).withScale(s)`. `mirror = true` → `CubeListBuilder.mirror()`.

## Halbdurchsichtige Blöcke (Alpha im Renderer)

Die Render-Schicht folgt der Textur. Erzwingen und Alpha setzen im Modell-JSON:
```json
"textures": { "top": { "sprite": "minecraft:block/obsidian", "force_translucent": true } },
"elements": [ { "faces": { "up": { "texture": "#top", "neoforge_data": { "color": "1CFFFFFF" } } } } ]
```
`color` ist ARGB (hex, Alpha zuerst). Item-Modell getrennt, wenn das Inventar einen anderen Alpha hatte.

## Rechtsklick vor dem Block (Original: `PlayerInteractEvent` mit `RIGHT_CLICK_BLOCK`)

`Item.useOn` kommt erst nach `useWithoutItem` des Blocks (Truhe öffnet sich). Stattdessen auf dem NeoForge-Bus
`PlayerInteractEvent.RightClickBlock`, auf **beiden** Seiten abbrechen (`setCancellationResult(InteractionResult.SUCCESS); setCanceled(true)`),
Logik nur `!level.isClientSide()`. Entitäten: `PlayerInteractEvent.EntityInteract`. Ein Block wie ein Item setzen:
`((BlockItem) Items.CHEST).place(new BlockPlaceContext(level, player, hand, stack, event.getHitVec()))`, Position = `context.getClickedPos()`.

## Stolperfallen

- Rüstungsmodell mit Animation je Träger (Rucksackdeckel, Vorlage `mods/betterstorage/client/BackpackArmorModel`):
  `registerEntityModifier(new TypeToken<LivingEntityRenderer<LivingEntity, LivingEntityRenderState, ?>>(){}, …)` setzt
  Render-Data, die `HumanoidModel.setupAnim` liest. `render(0.05F)` des Originals: `PartPose.ZERO.withScale(...)` am Kind-Teil.
- Deckel-/Türwinkel nie an einer geteilten `ModelPart` setzen und dann `submitModelPart` aufrufen (Darstellung kommt
  verzögert, alle Blöcke zeigen den letzten Winkel): eigenes `Model<Float>` mit `setupAnim`, `collector.submitModel(model, angle, …)`.

- `MobEffectInstance` klemmt Stufen auf 0..255: negative Stufen (Sprungkraft −2) → Attribut-Modifikator (`Attributes.JUMP_STRENGTH`).
- `PushReaction.DESTROY` heißt `POPPED`. `BaseEntityBlock` braucht keinen `codec()` mehr.
- `JAVA_TOOL_OPTIONS` nicht leeren: Er trägt den Proxy, sonst schlägt `createMinecraftArtifacts` fehl.

## Rüstungstextur je Färbefarbe

Hatte das Original eine eigene Texturdatei je Farbe (`getArmorTexture` mit Metadaten/NBT), in 26.3
`IClientItemExtensions.getArmorTexture` überschreiben (Vorbild Soulweave/Bloodweave in `mods/harkenscythe`).
`original-assets.txt` nimmt pro Quelldatei nur ein Ziel: mehrfach gebrauchte Texturen im Modell referenzieren.

## Block mit TileEntitySpecialRenderer und IItemRenderer (Vorlage `mods/bibliocraft`)

- Block: `RenderShape.MODEL` mit Modell nur aus Partikeltextur (`{"textures":{"particle":...}}`), alles andere zeichnet
  der `BlockEntityRenderer`. Rotation der TileEntity („angle“) als Block-Property; Formen je Winkel vorberechnen.
- ModelBase → `tools/modelbase_to_layer.py`, Layer über `EntityRenderersEvent.RegisterLayerDefinitions`. 1.5.2-TESRs
  zeichneten **ohne** Spiegelung: Modell-y = Welt-y; nach `translate(0.5,0.5,0.5)` (Blockmitte wie `glTranslated(i+0.5…)`)
  `collector.submitModelPart(part, pose, RenderTypes.entityCutout(tex), state.lightCoords, OverlayTexture.NO_OVERLAY, null)`.
  `render(0.03125F)` → zusätzlich `pose.scale(0.5F,…)`. `glRotatef(a, 0, 1, 0)` = `pose.rotateDegrees(Axis.YP, a)`.
- Alles im `extractRenderState` aus dem BlockEntity in den State kopieren; Items dort mit
  `context.itemModelResolver().updateForTopItem(new ItemStackRenderState(), stack, ItemDisplayContext.GROUND, level, null, seed)`
  auflösen, im `submit` `itemState.submit(pose, collector, light, OverlayTexture.NO_OVERLAY, 0)` (Vorbild Vanilla `ShelfRenderer`).
- Text in der Welt (`fontRenderer.drawString` im TESR): `collector.submitText(pose, x, y, FormattedCharSequence, shadow,
  Font.DisplayMode.POLYGON_OFFSET, light, 0xFF000000|rgb, 0, 0)`, Font aus `context.font()`.
- „Spieler schaut auf diesen Block“ (DrawBlockHighlightEvent): im Extract `Minecraft.getInstance().hitResult` prüfen.
- IItemRenderer → Item-JSON `{"model":{"type":"minecraft:special","base":"<ns>:item/<kind>","model":{"type":"<ns>:block","block":"<ns>:<id>"}}}`,
  eigener `SpecialModelRenderer.Unbaked` (MapCodec mit Block-Feld) über `RegisterSpecialModelRendererEvent`; ModelParts
  im `bake` aus `ctx.entityModelSet().bakeLayer(...)`. Display-Transformationen kommen aus dem Basis-Modell.
  Fabriken im Mod-Konstruktor nach Block-**Id** ablegen (die Blöcke gibt es dann noch nicht).
- OBJ mit Gruppen, die zur Laufzeit wechseln: eigener Leser + `submitCustomGeometry` (`bibliocraft/decor/client/ObjModel`).
- Licht nach dem Inhalt (getLightValue mit TileEntity): `AuxiliaryLightManager` von NeoForge (`bibliocraft/shelf`).

## Block mit dem Modell eines anderen Blocks (Tarnblöcke, Vorlage `mods/secretrooms`)

- Block-Entity hält den kopierten `BlockState`, `getModelData()` → `ModelData.of(PROP, state)`, bei Änderung
  `requestModelDataUpdate()` + `sendBlockUpdated`; Wahl des Zustands erst in `onLoad()` (Server), denn in `onPlace`
  gibt es die Block-Entity noch nicht.
- Modell: `ModelEvent.ModifyBakingResult` → `getBakingResult().blockStateModels()` der eigenen Zustände durch eine
  `DynamicBlockStateModel`-Hülle ersetzen; in `collectParts(level, pos, state, …)` `level.getModelData(pos).get(PROP)`
  lesen und `Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(copy).collectParts(...)` aufrufen.
  Quads tragen ihre Render-Schicht selbst (`materialInfo().layer()`).
- Teilflächen (Türplatte mit fremder Textur): `BakedQuad(pos0..3, packedUV0..3, dir, materialInfo, bakedNormals,
  bakedColors)` neu bauen, UV mit `UVPair.unpackU/V/pack` interpolieren (`secretrooms/client/CamoModel.cut`).
- Tönung: `RegisterColorHandlersEvent.BlockTintSources` mit einer Liste von `BlockTintSource` je Tint-Index, die
  `Minecraft.getInstance().getBlockColors().getTintSource(copy, i).colorInWorld(copy, level, pos)` zurückgeben.
- Neu zeichnen ohne Blockänderung: `clientLevel.setSectionRangeDirty(...)`.

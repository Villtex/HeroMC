package bibliocraft.display.client;

import bibliocraft.BiblioCraft;
import bibliocraft.client.BiblioRender;
import bibliocraft.common.BCConfig;
import bibliocraft.common.BiblioBlock;
import bibliocraft.display.ArmorRules;
import bibliocraft.display.ArmorStandBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.List;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.object.skull.SkullModelBase;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.entity.layers.EquipmentLayerRenderer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.level.block.AbstractSkullBlock;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.jspecify.annotations.Nullable;

/**
 * TileEntityArmorStandRenderer: the stand, the armor on it and with the reading glasses the name and enchantments of
 * the piece under the cursor (HighlightHandler: upper half helmet above, chestplate below; lower half leggings above,
 * boots below).
 *
 * <p>The armor is drawn with the vanilla equipment layers (textures of the equipment asset, dye, trims, glint, and a
 * custom armor model of IClientItemExtensions in place of getArmorModel) on ModelDummy, a ModelBiped without
 * expansion, at the positions and scales of the original. The original looked the textures up by item name in
 * RenderPlayer's armor list; the name-based head textures (creeper, zombie, Steve, wither skeleton, skeleton) are the
 * vanilla skull models by head type now (the zombie head pointed at a texture missing from the jar).
 */
public class ArmorStandRenderer implements BlockEntityRenderer<ArmorStandBlockEntity, ArmorStandRenderer.State> {
    public static final Identifier TEXTURE = BiblioCraft.id("textures/model/armorstand.png");
    /** glTranslated y and glScalef of the four pieces (ModelDummy at scale 1/16). */
    private static final float[] PIECE_Y = {1.55F, 1.6F, 1.8F, 1.7F};
    private static final float[] PIECE_SCALE = {1.02F, 1.12F, 1.05F, 1.1F};
    /** renderText y/z adjustment for armortextnum 1..4. */
    private static final double[][] TEXT_ADJUST = {{-208.0, -15.0}, {-85.0, 10.0}, {20.0, 0.0}, {100.0, 0.0}};
    private static final HumanoidRenderState DUMMY_STATE = new HumanoidRenderState();
    /** Set from EntityRenderersEvent.AddLayers on every resource reload (the asset manager is not reachable otherwise). */
    static volatile @Nullable EquipmentLayerRenderer equipmentRenderer;

    private final ModelPart stand;
    private final Dummy[] dummies;
    private final java.util.function.Function<SkullBlock.Type, @Nullable SkullModelBase> skulls;
    private final Font font;

    public static class State extends BlockEntityRenderState {
        int angle;
        final ItemStack[] armor = {ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY};
        SkullBlock.@Nullable Type skull;
        /** showArmorText 1..4 (stand slot + 1), 0 = none. */
        int textSlot;
        @Nullable String text;
        List<String> enchantments = List.of();
    }

    /** ModelDummy: a ModelBiped drawn without animation (render without setRotationAngles). */
    static final class Dummy extends HumanoidModel<HumanoidRenderState> {
        Dummy(ModelPart root) {
            super(root);
        }

        @Override
        public void setupAnim(HumanoidRenderState state) {
            this.resetPose();
        }
    }

    public ArmorStandRenderer(BlockEntityRendererProvider.Context context) {
        this.stand = context.bakeLayer(DisplayModels.ARMOR_STAND);
        this.dummies = new Dummy[]{new Dummy(context.bakeLayer(DisplayModels.DUMMY_HEAD)), new Dummy(context.bakeLayer(DisplayModels.DUMMY_CHEST)),
                new Dummy(context.bakeLayer(DisplayModels.DUMMY_LEGS)), new Dummy(context.bakeLayer(DisplayModels.DUMMY_FEET))};
        EntityModelSet modelSet = context.entityModelSet();
        this.skulls = net.minecraft.util.Util.memoize(type -> SkullBlockRenderer.createModel(modelSet, type));
        this.font = context.font();
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(ArmorStandBlockEntity be, State state, float partialTicks, Vec3 cameraPosition,
            ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(be, state, partialTicks, cameraPosition, breakProgress);
        state.angle = BiblioBlock.angle(be.getBlockState());
        for (int i = 0; i < 4; i++) {
            state.armor[i] = be.getArmor(i).copy();
        }
        state.skull = state.armor[0].getItem() instanceof BlockItem bi && bi.getBlock() instanceof AbstractSkullBlock skull ? skull.getType() : null;
        state.textSlot = 0;
        BiblioRender.Look upper = BiblioRender.look(be.getBlockPos().above());
        if (upper != null) {
            state.textSlot = upper.hitY() > 0.5 ? 1 : upper.hitY() < 0.5 ? 2 : 0;
        }
        BiblioRender.Look lower = BiblioRender.look(be.getBlockPos());
        if (lower != null) {
            state.textSlot = lower.hitY() > 0.5 ? 3 : lower.hitY() < 0.5 ? 4 : 0;
        }
        state.text = null;
        state.enchantments = List.of();
        if (state.textSlot > 0 && !state.armor[state.textSlot - 1].isEmpty()) {
            ItemStack piece = state.armor[state.textSlot - 1];
            state.text = piece.getHoverName().getString();
            state.enchantments = DisplayText.enchantments(piece);
        }
    }

    /** The stand frame of the original: from y 1.5 turned by the angle and up-side down (ModelBase y down). */
    private static void standFrame(PoseStack poseStack, int angle, float y) {
        poseStack.translate(0.5, y, 0.5);
        poseStack.rotateDegrees(Axis.YP, WeaponCaseRenderer.degrees(angle));
        poseStack.rotateDegrees(Axis.XP, 180.0F);
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        int light = state.lightCoords;
        if (state.skull != null) {
            SkullModelBase model = this.skulls.apply(state.skull);
            if (model != null) {
                poseStack.pushPose();
                standFrame(poseStack, state.angle, PIECE_Y[0]);
                poseStack.scale(1.02F, 1.02F, 1.02F);
                RenderType type = SkullBlockRenderer.getSkullRenderType(state.skull, null);
                SkullBlockRenderer.submitSkull(0.0F, poseStack, collector, light, model, type, 0, null);
                poseStack.popPose();
            }
        }
        EquipmentLayerRenderer equipment = equipmentRenderer;
        if (equipment != null) {
            for (int i = 0; i < 4; i++) {
                this.submitArmor(equipment, i, state.armor[i], state.angle, poseStack, collector, light);
            }
        }
        poseStack.pushPose();
        standFrame(poseStack, state.angle, 1.5F);
        collector.submitModelPart(this.stand, poseStack, RenderTypes.entityCutout(TEXTURE), light, OverlayTexture.NO_OVERLAY, null);
        if (state.text != null) {
            this.renderText(poseStack, collector, state.text, state.enchantments, TEXT_ADJUST[state.textSlot - 1], light);
        }
        poseStack.popPose();
    }

    /** One piece of ItemArmor (slot 0 helmet .. 3 boots); getArmorModel models get the offsets of the original. */
    private void submitArmor(EquipmentLayerRenderer equipment, int slot, ItemStack stack, int angle, PoseStack poseStack, SubmitNodeCollector collector,
            int light) {
        if (ArmorRules.armorType(stack) != slot) {
            return;
        }
        Equippable equippable = stack.get(DataComponents.EQUIPPABLE);
        if (equippable == null || equippable.assetId().isEmpty()) {
            return;
        }
        ResourceKey<EquipmentAsset> asset = equippable.assetId().get();
        EquipmentClientInfo.LayerType layer = slot == 2 ? EquipmentClientInfo.LayerType.HUMANOID_LEGGINGS : EquipmentClientInfo.LayerType.HUMANOID;
        Dummy dummy = this.dummies[slot];
        boolean custom = IClientItemExtensions.of(stack).getHumanoidArmorModel(stack, layer, dummy) != dummy;
        poseStack.pushPose();
        standFrame(poseStack, angle, PIECE_Y[slot]);
        if (custom) {
            // helmModel.render(..., modelScale = 1/15): no extra scale but for the boots, legs 0.1 lower, boots 0.2 higher
            if (slot == 3) {
                poseStack.scale(1.1F, 1.1F, 1.1F);
                poseStack.translate(0.0F, -0.2F, 0.0F);
            } else if (slot == 2) {
                poseStack.translate(0.0F, 0.1F, 0.0F);
            }
            poseStack.scale(16.0F / 15.0F, 16.0F / 15.0F, 16.0F / 15.0F);
        } else {
            poseStack.scale(PIECE_SCALE[slot], PIECE_SCALE[slot], PIECE_SCALE[slot]);
        }
        equipment.renderLayers(layer, asset, dummy, DUMMY_STATE, stack, poseStack, collector, light, 0);
        poseStack.popPose();
    }

    /** renderText: name in Config.color, the enchantments in Config.color2 below it. */
    private void renderText(PoseStack poseStack, SubmitNodeCollector collector, String text, List<String> ench, double[] adjust, int light) {
        poseStack.pushPose();
        poseStack.scale(0.005F, 0.005F, 0.005F);
        poseStack.translate(0.0, 100.0 + adjust[0], -45.0 + adjust[1]);
        DisplayText.centered(poseStack, collector, this.font, text, 0, BCConfig.textColor(), light);
        for (int i = 0; i < ench.size(); i++) {
            DisplayText.centered(poseStack, collector, this.font, ench.get(i), 10 * (i + 1), DisplayText.ENCHANT_COLOR, light);
        }
        poseStack.popPose();
    }

    /** getRenderBoundingBox: both halves. */
    @Override
    public AABB getRenderBoundingBox(ArmorStandBlockEntity be) {
        return be.renderBox();
    }

    /** ItemArmorStandRenderer: the stand alone at 2/3 size (scale 1/24), standing on the bottom of the item cube. */
    static void submitItem(ModelPart stand, PoseStack poseStack, SubmitNodeCollector collector, int light) {
        poseStack.pushPose();
        poseStack.translate(0.5, 1.0, 0.5);
        poseStack.rotateDegrees(Axis.XP, 180.0F);
        poseStack.scale(2.0F / 3.0F, 2.0F / 3.0F, 2.0F / 3.0F);
        collector.submitModelPart(stand, poseStack, RenderTypes.entityCutout(TEXTURE), light, OverlayTexture.NO_OVERLAY, null);
        poseStack.popPose();
    }
}

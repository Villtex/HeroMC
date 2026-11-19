package betterstorage.client;

import betterstorage.BetterStorage;
import betterstorage.block.ArmorStandBlock;
import betterstorage.block.ArmorStandBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.client.renderer.entity.layers.EquipmentLayerRenderer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.jspecify.annotations.Nullable;

/**
 * TileEntityArmorStandRenderer: ModelArmorStand, and the armor on it as an invisible player wearing it (the vanilla
 * player armor models and equipment layers, a custom armor model of IClientItemExtensions like getArmorModel).
 */
public class ArmorStandRenderer implements BlockEntityRenderer<ArmorStandBlockEntity, ArmorStandRenderer.State> {
    public static final Identifier TEXTURE = BetterStorage.id("textures/model/armor_stand.png");
    private static final HumanoidRenderState DUMMY_STATE = new HumanoidRenderState();
    /** Set from EntityRenderersEvent.AddLayers on every resource reload. */
    static volatile @Nullable EquipmentLayerRenderer equipmentRenderer;
    private final BSModels.ArmorStandModel stand;
    private final ArmorModelSet<Dummy> armor;

    public static class State extends BlockEntityRenderState {
        int rotation;
        final ItemStack[] armor = {ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY};
    }

    /** The player model drawn without animation. */
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
        this.stand = new BSModels.ArmorStandModel(context.bakeLayer(BSModels.ARMOR_STAND));
        this.armor = ArmorModelSet.bake(ModelLayers.PLAYER_ARMOR, context.entityModelSet(), Dummy::new);
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(ArmorStandBlockEntity be, State state, float partialTicks, Vec3 camera,
            ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(be, state, partialTicks, camera, breakProgress);
        state.rotation = be.rotation * 360 / 16;
        for (int i = 0; i < 4; i++) {
            state.armor[i] = be.armor.get(i).copy();
        }
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        poseStack.pushPose();
        submitStand(poseStack, collector, this.stand, state.rotation, state.lightCoords);
        poseStack.popPose();
        EquipmentLayerRenderer equipment = equipmentRenderer;
        if (equipment == null) {
            return;
        }
        for (int i = 0; i < 4; i++) {
            this.submitArmor(equipment, ArmorStandBlock.SLOTS[i], state.armor[i], state.rotation, poseStack, collector, state.lightCoords);
        }
    }

    /** glTranslated(x, y + 2, z + 1), glScalef(1, -1, -1), glTranslated(0.5, 0.5, 0.5). */
    static void submitStand(PoseStack poseStack, SubmitNodeCollector collector, BSModels.ArmorStandModel stand, float rotation, int light) {
        poseStack.translate(0.0F, 2.0F, 1.0F);
        poseStack.scale(1.0F, -1.0F, -1.0F);
        poseStack.translate(0.5F, 0.5F, 0.5F);
        collector.submitModel(stand, rotation, poseStack, stand.renderType(TEXTURE), light, OverlayTexture.NO_OVERLAY, 0);
    }

    /** renderEntityWithPosYaw(playerDummy, x + 0.5, y + 1.6875, z + 0.5): feet 1.62 below that, RenderPlayer's transformation. */
    private void submitArmor(EquipmentLayerRenderer equipment, EquipmentSlot slot, ItemStack stack, int rotation, PoseStack poseStack,
            SubmitNodeCollector collector, int light) {
        Equippable equippable = stack.get(DataComponents.EQUIPPABLE);
        if (stack.isEmpty() || equippable == null || equippable.slot() != slot || equippable.assetId().isEmpty()) {
            return;
        }
        ResourceKey<EquipmentAsset> asset = equippable.assetId().get();
        EquipmentClientInfo.LayerType layer = slot == EquipmentSlot.LEGS ? EquipmentClientInfo.LayerType.HUMANOID_LEGGINGS
                : EquipmentClientInfo.LayerType.HUMANOID;
        Dummy dummy = this.armor.get(slot);
        Model<? super HumanoidRenderState> model = cast(IClientItemExtensions.of(stack).getGenericArmorModel(stack, layer, dummy));
        poseStack.pushPose();
        poseStack.translate(0.5F, 1.6875F - 1.62F, 0.5F);
        poseStack.rotateDegrees(Axis.YP, 180.0F - rotation);
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        poseStack.scale(0.9375F, 0.9375F, 0.9375F);
        poseStack.translate(0.0F, -1.501F, 0.0F);
        equipment.renderLayers(layer, asset, model, DUMMY_STATE, stack, poseStack, collector, light, 0);
        poseStack.popPose();
    }

    @SuppressWarnings("unchecked")
    private static Model<? super HumanoidRenderState> cast(Model<?> model) {
        return (Model<? super HumanoidRenderState>) model;
    }

    @Override
    public AABB getRenderBoundingBox(ArmorStandBlockEntity be) {
        return be.renderBox();
    }
}

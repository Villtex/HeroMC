package betterstorage.client;

import betterstorage.BetterStorage;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.rendertype.RenderTypes;

/**
 * The ModelBase classes of the original as layer definitions: ModelChest and ModelLargeChest of 1.5.2 (the reinforced
 * chest textures have their layout), ModelArmorStand, ModelBackpack (block) and ModelBackpackArmor (worn, in
 * {@link BackpackArmorModel}). All are drawn like the 1.5.2 renderers did, up-side down (ModelBase y points down).
 */
public final class BSModels {
    public static final ModelLayerLocation CHEST = new ModelLayerLocation(BetterStorage.id("reinforced_chest"), "main");
    public static final ModelLayerLocation LARGE_CHEST = new ModelLayerLocation(BetterStorage.id("reinforced_chest"), "large");
    public static final ModelLayerLocation ARMOR_STAND = new ModelLayerLocation(BetterStorage.id("armor_stand"), "main");
    public static final ModelLayerLocation BACKPACK = new ModelLayerLocation(BetterStorage.id("backpack"), "main");
    public static final ModelLayerLocation BACKPACK_ARMOR = new ModelLayerLocation(BetterStorage.id("backpack"), "armor");

    private BSModels() {
    }

    /** ModelChest (64 x 64) or ModelLargeChest (128 x 64, 30 wide, knob at 16). */
    public static LayerDefinition chest(boolean large) {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        int w = large ? 30 : 14;
        root.addOrReplaceChild("lid", CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, -5.0F, -14.0F, w, 5.0F, 14.0F),
                PartPose.offset(1.0F, 7.0F, 15.0F));
        root.addOrReplaceChild("knob", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, -2.0F, -15.0F, 2.0F, 4.0F, 1.0F),
                PartPose.offset(large ? 16.0F : 8.0F, 7.0F, 15.0F));
        root.addOrReplaceChild("below", CubeListBuilder.create().texOffs(0, 19).addBox(0.0F, 0.0F, 0.0F, w, 10.0F, 14.0F),
                PartPose.offset(1.0F, 6.0F, 1.0F));
        return LayerDefinition.create(mesh, large ? 128 : 64, 64);
    }

    /** ModelArmorStand (64 x 32); setRotation turns all but the bottom plate. */
    public static LayerDefinition armorStand() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("bottom", CubeListBuilder.create().texOffs(4, 0).addBox(-6.0F, 0.0F, -6.0F, 12.0F, 1.0F, 12.0F),
                PartPose.offset(0.0F, 23.0F, 0.0F));
        root.addOrReplaceChild("middle", CubeListBuilder.create().texOffs(0, 0).addBox(-0.5F, 0.5F, -0.5F, 1.0F, 31.0F, 1.0F),
                PartPose.offset(0.0F, -8.0F, 0.0F));
        root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(4, 9).addBox(-2.0F, 0.0F, -1.0F, 4.0F, 1.0F, 2.0F),
                PartPose.offset(0.0F, -8.0F, 0.0F));
        root.addOrReplaceChild("shoulder", CubeListBuilder.create().texOffs(4, 13).addBox(-6.0F, 0.0F, -1.0F, 12.0F, 1.0F, 2.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        root.addOrReplaceChild("legs", CubeListBuilder.create().texOffs(4, 16).addBox(-3.0F, 0.0F, -1.0F, 6.0F, 1.0F, 2.0F),
                PartPose.offset(0.0F, 8.0F, 0.0F));
        return LayerDefinition.create(mesh, 64, 32);
    }

    /** ModelBackpack (32 x 32): the lid ("top") turns around its back edge. */
    public static LayerDefinition backpack() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("main", CubeListBuilder.create().texOffs(0, 8).addBox(-5.0F, 0.0F, -3.0F, 10.0F, 9.0F, 5.0F),
                PartPose.offset(0.0F, 15.0F, 0.0F));
        root.addOrReplaceChild("top", CubeListBuilder.create().texOffs(0, 0).addBox(-5.0F, -3.0F, 0.0F, 10.0F, 3.0F, 5.0F),
                PartPose.offset(0.0F, 15.0F, -3.0F));
        root.addOrReplaceChild("front", CubeListBuilder.create().texOffs(0, 22).addBox(-4.0F, 0.0F, 0.0F, 8.0F, 6.0F, 2.0F),
                PartPose.offset(0.0F, 17.0F, 2.0F));
        root.addOrReplaceChild("right", CubeListBuilder.create().texOffs(20, 22).addBox(0.0F, 0.0F, -1.0F, 1.0F, 8.0F, 1.0F),
                PartPose.offset(-4.0F, 13.0F, -3.0F));
        root.addOrReplaceChild("left", CubeListBuilder.create().texOffs(20, 22).addBox(0.0F, 0.0F, -1.0F, 1.0F, 8.0F, 1.0F),
                PartPose.offset(3.0F, 13.0F, -3.0F));
        return LayerDefinition.create(mesh, 32, 32);
    }

    /** ModelChest with the lid angle of the original (1 - (1 - a)^3, a quarter turn). */
    public static final class ChestModel extends Model<Float> {
        private final ModelPart lid;
        private final ModelPart knob;

        public ChestModel(ModelPart root) {
            super(root, RenderTypes::entityCutout);
            this.lid = root.getChild("lid");
            this.knob = root.getChild("knob");
        }

        @Override
        public void setupAnim(Float open) {
            super.setupAnim(open);
            float angle = 1.0F - open;
            angle = 1.0F - angle * angle * angle;
            this.lid.xRot = -(float) (angle * Math.PI / 2.0);
            this.knob.xRot = this.lid.xRot;
        }
    }

    /** ModelBackpack with the lid angle of TileEntityBackpackRenderer (1 - (1 - a)^2, an eighth turn). */
    public static final class BackpackModel extends Model<Float> {
        private final ModelPart top;

        public BackpackModel(ModelPart root) {
            super(root, RenderTypes::entityCutout);
            this.top = root.getChild("top");
        }

        @Override
        public void setupAnim(Float open) {
            super.setupAnim(open);
            float angle = 1.0F - open;
            angle = 1.0F - angle * angle;
            this.top.xRot = (float) (angle * Math.PI / 4.0);
        }
    }

    /** ModelArmorStand turned by the stand's rotation (degrees). */
    public static final class ArmorStandModel extends Model<Float> {
        private final ModelPart[] turning;

        public ArmorStandModel(ModelPart root) {
            super(root, RenderTypes::entityCutout);
            this.turning = new ModelPart[]{root.getChild("middle"), root.getChild("head"), root.getChild("shoulder"), root.getChild("legs")};
        }

        @Override
        public void setupAnim(Float degrees) {
            super.setupAnim(degrees);
            for (ModelPart part : this.turning) {
                part.yRot = (float) (degrees / 360.0F * Math.PI * 2.0);
            }
        }
    }
}

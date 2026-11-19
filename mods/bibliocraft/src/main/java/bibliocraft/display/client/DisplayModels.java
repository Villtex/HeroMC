package bibliocraft.display.client;

import bibliocraft.BiblioCraft;
import java.util.Set;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * ModelWeaponCase (128 x 64), ModelLabel (64 x 32), ModelArmorStand (64 x 64) and ModelDummy (a ModelBiped without
 * expansion, 64 x 32, one layer per armor slot), boxes copied with tools/modelbase_to_layer.py.
 */
public final class DisplayModels {
    public static final ModelLayerLocation WEAPON_CASE = new ModelLayerLocation(BiblioCraft.id("weapon_case"), "main");
    public static final ModelLayerLocation LABEL = new ModelLayerLocation(BiblioCraft.id("label"), "main");
    public static final ModelLayerLocation ARMOR_STAND = new ModelLayerLocation(BiblioCraft.id("armor_stand"), "main");
    /** ModelDummy.renderHead / renderChest / renderLegs / renderFeet. */
    public static final ModelLayerLocation DUMMY_HEAD = new ModelLayerLocation(BiblioCraft.id("armor_stand"), "dummy_head");
    public static final ModelLayerLocation DUMMY_CHEST = new ModelLayerLocation(BiblioCraft.id("armor_stand"), "dummy_chest");
    public static final ModelLayerLocation DUMMY_LEGS = new ModelLayerLocation(BiblioCraft.id("armor_stand"), "dummy_legs");
    public static final ModelLayerLocation DUMMY_FEET = new ModelLayerLocation(BiblioCraft.id("armor_stand"), "dummy_feet");

    private DisplayModels() {
    }

    /** renderShell: Bottom and the four boards; renderInner: the inner* parts; renderLid: lid (turned about z). */
    public static LayerDefinition weaponCase() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("Bottom", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-6.0F, 0.0F, -8.0F, 14.0F, 2.0F, 16.0F),
                PartPose.offset(-1.0F, 22.0F, 0.0F));
        root.addOrReplaceChild("backboard", CubeListBuilder.create()
                .texOffs(13, 26).addBox(6.0F, 0.0F, -8.0F, 1.0F, 4.0F, 16.0F),
                PartPose.offset(0.0F, 18.0F, 0.0F));
        root.addOrReplaceChild("frontboard", CubeListBuilder.create()
                .texOffs(13, 26).addBox(-7.0F, 0.0F, -8.0F, 1.0F, 4.0F, 16.0F),
                PartPose.offset(0.0F, 18.0F, 0.0F));
        root.addOrReplaceChild("leftboard", CubeListBuilder.create()
                .texOffs(17, 19).addBox(-6.0F, 0.0F, 7.0F, 12.0F, 4.0F, 1.0F),
                PartPose.offset(0.0F, 18.0F, 0.0F));
        root.addOrReplaceChild("rightboard", CubeListBuilder.create()
                .texOffs(17, 19).addBox(-6.0F, 0.0F, -8.0F, 12.0F, 4.0F, 1.0F),
                PartPose.offset(0.0F, 18.0F, 0.0F));
        root.addOrReplaceChild("innerLeft", CubeListBuilder.create()
                .texOffs(74, 59).addBox(0.0F, 0.0F, 0.0F, 12.0F, 3.0F, 1.0F),
                PartPose.offset(-6.0F, 19.0F, 6.0F));
        root.addOrReplaceChild("innerRight", CubeListBuilder.create()
                .texOffs(74, 59).addBox(0.0F, 0.0F, 0.0F, 12.0F, 3.0F, 1.0F),
                PartPose.offset(-6.0F, 19.0F, -7.0F));
        root.addOrReplaceChild("innerBack", CubeListBuilder.create()
                .texOffs(101, 48).addBox(0.0F, 0.0F, 0.0F, 1.0F, 3.0F, 12.0F),
                PartPose.offset(5.0F, 19.0F, -6.0F));
        root.addOrReplaceChild("innerFront", CubeListBuilder.create()
                .texOffs(101, 48).addBox(0.0F, 0.0F, 0.0F, 1.0F, 3.0F, 12.0F),
                PartPose.offset(-6.0F, 19.0F, -6.0F));
        root.addOrReplaceChild("innterBottom", CubeListBuilder.create()
                .texOffs(56, 45).addBox(0.0F, 0.0F, 0.0F, 10.0F, 1.0F, 12.0F),
                PartPose.offset(-5.0F, 21.0F, -6.0F));
        root.addOrReplaceChild("lid", CubeListBuilder.create().mirror()
                .texOffs(97, 0).addBox(-14.0F, -2.0F, 7.0F, 14.0F, 2.0F, 1.0F)
                .texOffs(97, 0).addBox(-14.0F, -2.0F, -8.0F, 14.0F, 2.0F, 1.0F)
                .texOffs(97, 3).addBox(-1.0F, -2.0F, -7.0F, 1.0F, 2.0F, 14.0F)
                .texOffs(97, 3).addBox(-14.0F, -2.0F, -7.0F, 1.0F, 2.0F, 14.0F)
                .texOffs(58, 34).addBox(-13.0F, -2.0F, 6.0F, 12.0F, 1.0F, 1.0F)
                .texOffs(58, 34).addBox(-13.0F, -2.0F, -7.0F, 12.0F, 1.0F, 1.0F)
                .texOffs(58, 20).addBox(-13.0F, -2.0F, -6.0F, 1.0F, 1.0F, 12.0F)
                .texOffs(58, 20).addBox(-2.0F, -2.0F, -6.0F, 1.0F, 1.0F, 12.0F)
                .texOffs(85, 20).addBox(-12.0F, -2.0F, -6.0F, 10.0F, 1.0F, 12.0F)
                .texOffs(68, 15).addBox(-15.0F, -1.0F, -1.0F, 1.0F, 2.0F, 2.0F),
                PartPose.offset(7.0F, 18.0F, 0.0F));
        return LayerDefinition.create(mesh, 128, 64);
    }

    /** renderLabel; its unit is half a pixel in the world (scale 1/32). */
    public static LayerDefinition label() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("back", CubeListBuilder.create()
                .texOffs(7, 5).addBox(-7.0F, -4.0F, 0.0F, 14.0F, 8.0F, 1.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        root.addOrReplaceChild("topEdge", CubeListBuilder.create()
                .texOffs(4, 1).addBox(-8.0F, -5.0F, -1.0F, 16.0F, 1.0F, 2.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        root.addOrReplaceChild("bottomEdge", CubeListBuilder.create()
                .texOffs(4, 16).addBox(-8.0F, 4.0F, -1.0F, 16.0F, 1.0F, 2.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        root.addOrReplaceChild("leftEdge", CubeListBuilder.create()
                .texOffs(0, 5).addBox(-8.0F, -4.0F, -1.0F, 1.0F, 8.0F, 2.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        root.addOrReplaceChild("rightEdge", CubeListBuilder.create()
                .texOffs(38, 5).addBox(7.0F, -4.0F, -1.0F, 1.0F, 8.0F, 2.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        return LayerDefinition.create(mesh, 64, 32);
    }

    /** renderStand (the parts call setTextureSize(64, 32) only after addBox, so their UVs use the model's 64 x 64). */
    public static LayerDefinition armorStand() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("Spine", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-2.0F, -22.0F, -2.0F, 4.0F, 7.0F, 4.0F),
                PartPose.offset(0.0F, 24.0F, 0.0F));
        root.addOrReplaceChild("shoulder", CubeListBuilder.create()
                .texOffs(17, 15).addBox(-7.0F, 0.0F, -1.0F, 14.0F, 2.0F, 4.0F),
                PartPose.offset(0.0F, 0.0F, -1.0F));
        root.addOrReplaceChild("spineSkinny", CubeListBuilder.create()
                .texOffs(0, 32).addBox(-8.0F, 0.0F, -8.0F, 16.0F, 2.0F, 16.0F),
                PartPose.offset(0.0F, 22.0F, 0.0F));
        root.addOrReplaceChild("leg2", CubeListBuilder.create()
                .texOffs(34, 22).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 7.0F, 2.0F),
                PartPose.offsetAndRotation(-6.0F, 1.0F, 0.0F, 0.0F, 0.0F, (float) (-Math.PI / 4)));
        root.addOrReplaceChild("leg3", CubeListBuilder.create()
                .texOffs(25, 22).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 7.0F, 2.0F),
                PartPose.offsetAndRotation(6.0F, 1.0F, 0.0F, 0.0F, 0.0F, (float) (Math.PI / 4)));
        root.addOrReplaceChild("Spinetop", CubeListBuilder.create()
                .texOffs(25, 0).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 4.0F, 4.0F),
                PartPose.offset(0.0F, -8.0F, 0.0F));
        root.addOrReplaceChild("SpineMid", CubeListBuilder.create()
                .texOffs(29, 8).addBox(-1.0F, 1.0F, -1.0F, 2.0F, 4.0F, 2.0F),
                PartPose.offset(0.0F, -5.0F, 0.0F));
        root.addOrReplaceChild("SpineBottom", CubeListBuilder.create()
                .texOffs(4, 13).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 13.0F, 2.0F),
                PartPose.offset(0.0F, 9.0F, 0.0F));
        return LayerDefinition.create(mesh, 64, 64);
    }

    /** ModelBiped(0) with only the parts ModelDummy draws for that slot (head + headwear, body + arms, legs). */
    public static LayerDefinition dummy(int slot) {
        MeshDefinition mesh = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F);
        switch (slot) {
            case 0 -> mesh.getRoot().retainPartsAndChildren(Set.of("head"));
            case 1 -> mesh.getRoot().retainExactParts(Set.of("body", "left_arm", "right_arm"));
            default -> mesh.getRoot().retainExactParts(Set.of("left_leg", "right_leg"));
        }
        return LayerDefinition.create(mesh, 64, 32);
    }
}

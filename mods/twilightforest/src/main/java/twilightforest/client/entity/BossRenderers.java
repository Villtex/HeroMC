package twilightforest.client.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.client.renderer.entity.ZombieRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.entity.state.ZombieRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import org.jspecify.annotations.Nullable;
import twilightforest.TFBosses;
import twilightforest.TwilightForest;
import twilightforest.entity.boss.Lich;
import twilightforest.entity.boss.LichMinion;
import twilightforest.entity.boss.Minoshroom;
import twilightforest.entity.boss.Naga;
import twilightforest.entity.boss.NagaSegment;
import twilightforest.entity.boss.UrGhast;

/** Teil 24f: models and renderers of the naga, the lich and its minions, the minoshroom and the ur-ghast. */
public final class BossRenderers {
    public static final ModelLayerLocation NAGA_HEAD = layer("naga_head");
    public static final ModelLayerLocation NAGA_SEGMENT = layer("naga_segment");
    public static final ModelLayerLocation LICH = layer("lich");
    public static final ModelLayerLocation MINOSHROOM = layer("minoshroom");
    public static final ModelLayerLocation UR_GHAST = layer("ur_ghast");

    private BossRenderers() {
    }

    private static ModelLayerLocation layer(String name) {
        return new ModelLayerLocation(TwilightForest.id(name), "main");
    }

    public static void register(IEventBus modBus) {
        modBus.addListener(BossRenderers::layers);
        modBus.addListener(BossRenderers::renderers);
    }

    private static void layers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(NAGA_HEAD, () -> box("head", -12.0F));
        event.registerLayerDefinition(NAGA_SEGMENT, () -> box("body", -16.0F));
        event.registerLayerDefinition(LICH, LichModel::createBodyLayer);
        event.registerLayerDefinition(MINOSHROOM, MinoshroomModel::createBodyLayer);
        event.registerLayerDefinition(UR_GHAST, UrGhastModel::createBodyLayer);
    }

    private static void renderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(TFBosses.NAGA.get(), NagaRenderer::new);
        event.registerEntityRenderer(TFBosses.LICH.get(), LichRenderer::new);
        event.registerEntityRenderer(TFBosses.LICH_MINION.get(), MinionRenderer::new);
        event.registerEntityRenderer(TFBosses.MINOSHROOM.get(), MinoshroomRenderer::new);
        // RenderTFTowerGhast(ModelTFTowerBoss, 0.625, 24): (24 + 1) / 2 with the never synced attack counter
        event.registerEntityRenderer(TFBosses.UR_GHAST.get(), c -> new TFMobRenderer<UrGhast, TFRenderStates.Variant>(c,
                new UrGhastModel(c.bakeLayer(UR_GHAST)), null, 0.625F, TFRenderStates.Variant::new,
                s -> TFMobRenderer.texture(UR_GHAST_TEXTURES[Math.floorMod(s.type, 3)]), (e, s) -> s.type = e.aggroStatus()) {
            @Override
            public boolean shouldRender(UrGhast entity, Frustum frustum, double x, double y, double z, float partialTick) {
                return true;
            }
        }.scaled(12.5F));
        event.registerEntityRenderer(TFBosses.LICH_BOLT.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(TFBosses.LICH_BOMB.get(), ThrownItemRenderer::new);
    }

    private static final String[] UR_GHAST_TEXTURES = {"towerboss", "towerboss_openeyes", "towerboss_fire"};

    /** ModelTFNaga: one 16³ box (head from y -12, body segment from y -16), drawn twice the size. */
    private static LayerDefinition box(String name, float y) {
        MeshDefinition mesh = new MeshDefinition();
        mesh.getRoot().addOrReplaceChild(name, CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, y, -8.0F, 16.0F, 16.0F, 16.0F), PartPose.ZERO);
        return LayerDefinition.create(mesh, 64, 32);
    }

    static class BoxModel extends EntityModel<EntityRenderState> {
        BoxModel(ModelPart root) {
            super(root);
        }
    }

    /** Scales about the model origin (1.5 above the feet), as ModelRenderer.render(f5 × factor) did. */
    static void scaleAboutModelOrigin(PoseStack poseStack, float scale) {
        poseStack.translate(0.0F, -1.501F, 0.0F);
        poseStack.scale(scale, scale, scale);
        poseStack.translate(0.0F, 1.501F, 0.0F);
    }

    // ---------------------------------------------------------------- naga

    public static class NagaState extends LivingEntityRenderState {
        int segments;
        final double[] x = new double[Naga.MAX_SEGMENTS];
        final double[] y = new double[Naga.MAX_SEGMENTS];
        final double[] z = new double[Naga.MAX_SEGMENTS];
        final float[] yaw = new float[Naga.MAX_SEGMENTS];
        final float[] pitch = new float[Naga.MAX_SEGMENTS];
    }

    /** RenderTFNaga: the head twice the size; the segments (RenderTFSpikeBlock) turned 180 - yaw and by their pitch. */
    static class NagaRenderer extends MobRenderer<Naga, NagaState, EntityModel<EntityRenderState>> {
        private static final Identifier HEAD = TFMobRenderer.texture("nagahead");
        private static final Identifier SEGMENT = TFMobRenderer.texture("nagasegment");
        private final BoxModel segment;

        NagaRenderer(EntityRendererProvider.Context context) {
            super(context, new BoxModel(context.bakeLayer(NAGA_HEAD)), 0.625F);
            this.segment = new BoxModel(context.bakeLayer(NAGA_SEGMENT));
        }

        @Override
        public NagaState createRenderState() {
            return new NagaState();
        }

        @Override
        public void extractRenderState(Naga entity, NagaState state, float partialTick) {
            super.extractRenderState(entity, state, partialTick);
            double nx = Mth.lerp(partialTick, entity.xo, entity.getX());
            double ny = Mth.lerp(partialTick, entity.yo, entity.getY());
            double nz = Mth.lerp(partialTick, entity.zo, entity.getZ());
            state.segments = 0;
            for (NagaSegment seg : entity.getSegments()) {
                if (!seg.isActive()) {
                    continue;
                }
                int i = state.segments++;
                state.x[i] = Mth.lerp(partialTick, seg.xo, seg.getX()) - nx;
                state.y[i] = Mth.lerp(partialTick, seg.yo, seg.getY()) - ny;
                state.z[i] = Mth.lerp(partialTick, seg.zo, seg.getZ()) - nz;
                state.yaw[i] = Mth.rotLerp(partialTick, seg.yRotO, seg.getYRot());
                state.pitch[i] = Mth.lerp(partialTick, seg.xRotO, seg.getXRot());
            }
        }

        @Override
        protected void scale(NagaState state, PoseStack poseStack) {
            scaleAboutModelOrigin(poseStack, 2.0F);
        }

        @Override
        public void submit(NagaState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
            super.submit(state, poseStack, collector, camera);
            if (state.isInvisible) {
                return;
            }
            for (int i = 0; i < state.segments; i++) {
                poseStack.pushPose();
                poseStack.translate(state.x[i], state.y[i], state.z[i]);
                poseStack.rotateDegrees(Axis.YP, 180.0F - Mth.wrapDegrees(state.yaw[i]));
                poseStack.rotateDegrees(Axis.XP, state.pitch[i]);
                poseStack.scale(-2.0F, -2.0F, 2.0F);
                collector.submitModel(this.segment, state, poseStack, SEGMENT, state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
                poseStack.popPose();
            }
        }

        @Override
        public boolean shouldRender(Naga entity, Frustum frustum, double x, double y, double z, float partialTick) {
            return true;
        }

        @Override
        public Identifier getTextureLocation(NagaState state) {
            return HEAD;
        }
    }

    // ---------------------------------------------------------------- lich

    public static class LichState extends HumanoidRenderState {
        boolean clone;
        int shields;
        float age;
    }

    /**
     * ModelTFLich (64 × 64): a thin biped with collar and cloak, drawn 1.125 times the size; the shield belt with one
     * shield per strength turns around it. A shadow clone is the bare biped, grey and see-through.
     */
    public static class LichModel extends HumanoidModel<LichState> {
        private final ModelPart collar;
        private final ModelPart cloak;
        private final ModelPart shieldBelt;
        private final ModelPart[] shields = new ModelPart[Lich.INITIAL_SHIELD_STRENGTH];

        public LichModel(ModelPart root) {
            super(root);
            this.collar = root.getChild("collar");
            this.cloak = root.getChild("cloak");
            this.shieldBelt = root.getChild("shield_belt");
            for (int i = 0; i < this.shields.length; i++) {
                this.shields[i] = this.shieldBelt.getChild("shield" + i);
            }
        }

        public static LayerDefinition createBodyLayer() {
            MeshDefinition mesh = new MeshDefinition();
            PartDefinition root = mesh.getRoot();
            root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(8, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 24.0F, 4.0F), PartPose.offset(0.0F, -4.0F, 0.0F));
            root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(0, 16).addBox(-2.0F, -2.0F, -1.0F, 2.0F, 12.0F, 2.0F),
                    PartPose.offset(-5.0F, -2.0F, 0.0F));
            root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(0, 16).mirror().addBox(-2.0F, -2.0F, -1.0F, 2.0F, 12.0F, 2.0F),
                    PartPose.offset(5.0F, -2.0F, 0.0F));
            PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F),
                    PartPose.offset(0.0F, -4.0F, 0.0F));
            head.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(32, 0).addBox(-4.0F, -12.0F, -4.0F, 8.0F, 8.0F, 8.0F,
                    new net.minecraft.client.model.geom.builders.CubeDeformation(0.5F)), PartPose.ZERO);
            root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 16).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 12.0F, 2.0F),
                    PartPose.offset(-2.0F, 9.5F, 0.0F));
            root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(0, 16).mirror().addBox(-1.0F, 0.0F, -1.0F, 2.0F, 12.0F, 2.0F),
                    PartPose.offset(2.0F, 9.5F, 0.0F));
            root.addOrReplaceChild("collar", CubeListBuilder.create().texOffs(32, 16).addBox(-6.0F, 0.0F, 0.0F, 12.0F, 12.0F, 1.0F),
                    PartPose.offsetAndRotation(0.0F, -3.0F, -1.0F, 2.164208F, 0.0F, 0.0F));
            root.addOrReplaceChild("cloak", CubeListBuilder.create().texOffs(0, 44).addBox(-6.0F, 0.0F, 0.0F, 12.0F, 19.0F, 1.0F),
                    PartPose.offset(0.0F, -4.0F, 2.5F));
            PartDefinition belt = root.addOrReplaceChild("shield_belt", CubeListBuilder.create(), PartPose.ZERO);
            for (int i = 0; i < Lich.INITIAL_SHIELD_STRENGTH; i++) {
                belt.addOrReplaceChild("shield" + i, CubeListBuilder.create().texOffs(26, 40).addBox(0.5F, -6.0F, -6.0F, 1.0F, 12.0F, 12.0F), PartPose.ZERO);
            }
            return LayerDefinition.create(mesh, 64, 64);
        }

        @Override
        public void setupAnim(LichState s) {
            this.resetPose();
            OldBiped.angles(this, s);
            float og = Mth.sin(s.swingAnimation * Mth.PI);
            float other = Mth.sin((1.0F - (1.0F - s.swingAnimation) * (1.0F - s.swingAnimation)) * Mth.PI);
            float f2 = s.ageInTicks;
            this.rightArm.zRot = 0.0F;
            this.leftArm.zRot = 0.5F;
            this.rightArm.yRot = -(0.1F - og * 0.6F);
            this.leftArm.yRot = 0.1F - og * 0.6F;
            this.rightArm.xRot = -1.570796F;
            this.leftArm.xRot = -3.141593F;
            this.rightArm.xRot -= og * 1.2F - other * 0.4F;
            this.leftArm.xRot -= og * 1.2F - other * 0.4F;
            this.rightArm.zRot += Mth.cos(f2 * 0.26F) * 0.15F + 0.05F;
            this.leftArm.zRot -= Mth.cos(f2 * 0.26F) * 0.15F + 0.05F;
            this.rightArm.xRot += Mth.sin(f2 * 0.167F) * 0.15F;
            this.leftArm.xRot -= Mth.sin(f2 * 0.167F) * 0.15F;
            this.head.y = -4.0F;
            this.rightLeg.y = 9.5F;
            this.leftLeg.y = 9.5F;
            this.rightArm.y = -2.0F;
            this.leftArm.y = -2.0F;
            this.body.y = -4.0F;
            this.collar.visible = !s.clone;
            this.cloak.visible = !s.clone;
            boolean belt = !s.clone && s.shields > 0;
            this.shieldBelt.visible = belt;
            if (belt) {
                for (int i = 0; i < this.shields.length; i++) {
                    ModelPart shield = this.shields[i];
                    shield.visible = i < s.shields;
                    float rotateY = i * (360.0F / s.shields) * Mth.DEG_TO_RAD;
                    shield.x = Mth.cos(rotateY) * 11.0F;
                    shield.z = -Mth.sin(rotateY) * 11.0F;
                    shield.yRot = rotateY;
                }
                this.shieldBelt.yRot = s.age / 5.0F;
                this.shieldBelt.xRot = Mth.sin(s.age / 5.0F) / 4.0F;
                this.shieldBelt.zRot = Mth.cos(s.age / 5.0F) / 4.0F;
            }
        }
    }

    /** RenderTFLich: a clone is drawn grey (0.33) at 0.8 opacity. */
    static class LichRenderer extends HumanoidMobRenderer<Lich, LichState, LichModel> {
        private static final Identifier TEXTURE = TFMobRenderer.texture("twilightlich64");

        LichRenderer(EntityRendererProvider.Context context) {
            super(context, new LichModel(context.bakeLayer(LICH)), 1.0F);
        }

        @Override
        public LichState createRenderState() {
            return new LichState();
        }

        @Override
        public void extractRenderState(Lich entity, LichState state, float partialTick) {
            super.extractRenderState(entity, state, partialTick);
            state.clone = entity.isShadowClone();
            state.shields = entity.getShieldStrength();
            state.age = entity.tickCount + partialTick;
        }

        @Override
        protected void scale(LichState state, PoseStack poseStack) {
            scaleAboutModelOrigin(poseStack, 1.125F);
        }

        @Override
        protected @Nullable RenderType getRenderType(LichState state, boolean visible, boolean translucent, boolean glowing) {
            return state.clone ? RenderTypes.entityTranslucent(TEXTURE) : super.getRenderType(state, visible, translucent, glowing);
        }

        @Override
        protected int getModelTint(LichState state) {
            return state.clone ? 0xCC545454 : -1;
        }

        @Override
        public Identifier getTextureLocation(LichState state) {
            return TEXTURE;
        }
    }

    // ---------------------------------------------------------------- lich minion

    public static class MinionState extends ZombieRenderState {
        boolean strong;
    }

    /** ModelTFLichMinion: a zombie tinted green (0.5, 1, 0.5), brighter (0.25, 2, 0.25) while it has strength. */
    static class MinionRenderer extends ZombieRenderer {
        MinionRenderer(EntityRendererProvider.Context context) {
            super(context);
        }

        @Override
        public ZombieRenderState createRenderState() {
            return new MinionState();
        }

        @Override
        public void extractRenderState(net.minecraft.world.entity.monster.zombie.Zombie entity, ZombieRenderState state, float partialTick) {
            super.extractRenderState(entity, state, partialTick);
            if (state instanceof MinionState minion && entity instanceof LichMinion) {
                minion.strong = entity.hasEffect(MobEffects.STRENGTH);
            }
        }

        @Override
        protected int getModelTint(ZombieRenderState state) {
            return state instanceof MinionState minion && minion.strong ? 0xFF40FF40 : 0xFF80FF80;
        }
    }

    // ---------------------------------------------------------------- minoshroom

    public static class MinoshroomState extends HumanoidRenderState {
        final BlockModelRenderState mushroom = new BlockModelRenderState();
    }

    /** ModelTFMinoshroom (128 × 32): a cow body with four legs and udders, the minotaur's chest, arms and head in front. */
    public static class MinoshroomModel extends HumanoidModel<MinoshroomState> {
        private final ModelPart cowBody;
        private final ModelPart[] legs = new ModelPart[4];

        public MinoshroomModel(ModelPart root) {
            super(root);
            this.cowBody = root.getChild("cow_body");
            for (int i = 0; i < 4; i++) {
                this.legs[i] = root.getChild("leg" + (i + 1));
            }
        }

        public static LayerDefinition createBodyLayer() {
            MeshDefinition mesh = new MeshDefinition();
            PartDefinition root = mesh.getRoot();
            PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(96, 16).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F),
                    PartPose.offset(0.0F, -6.0F, -9.0F));
            head.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);
            PartDefinition horn1a = head.addOrReplaceChild("horn1a", CubeListBuilder.create().texOffs(24, 0).addBox(0.0F, -1.0F, -1.0F, 3.0F, 2.0F, 2.0F),
                    PartPose.offsetAndRotation(4.0F, -6.0F, 0.0F, 0.0F, (float) (Math.PI / 12), 0.0F));
            horn1a.addOrReplaceChild("horn1b", CubeListBuilder.create().texOffs(24, 2).addBox(-1.0F, -4.0F, -1.0F, 2.0F, 4.0F, 2.0F),
                    PartPose.offsetAndRotation(2.75F, 0.0F, 0.0F, (float) (Math.PI / 6), (float) (-Math.PI / 6), 0.0F));
            PartDefinition horn2a = head.addOrReplaceChild("horn2a", CubeListBuilder.create().texOffs(24, 0).addBox(-3.0F, -1.0F, -1.0F, 3.0F, 2.0F, 2.0F),
                    PartPose.offsetAndRotation(-4.0F, -6.0F, 0.0F, 0.0F, (float) (-Math.PI / 12), 0.0F));
            horn2a.addOrReplaceChild("horn2b", CubeListBuilder.create().texOffs(24, 2).addBox(-1.0F, -4.0F, -1.0F, 2.0F, 4.0F, 2.0F),
                    PartPose.offsetAndRotation(-2.75F, 0.0F, 0.0F, (float) (Math.PI / 6), (float) (Math.PI / 6), 0.0F));
            head.addOrReplaceChild("snout", CubeListBuilder.create().texOffs(105, 28).addBox(-2.0F, -1.0F, -1.0F, 4.0F, 3.0F, 1.0F),
                    PartPose.offset(0.0F, -2.0F, -4.0F));
            root.addOrReplaceChild("cow_body", CubeListBuilder.create().texOffs(18, 4).addBox(-6.0F, -10.0F, -7.0F, 12.0F, 18.0F, 10.0F),
                    PartPose.offsetAndRotation(0.0F, 5.0F, 2.0F, 1.570796F, 0.0F, 0.0F));
            root.addOrReplaceChild("leg1", CubeListBuilder.create().texOffs(0, 16).addBox(-3.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F), PartPose.offset(-3.0F, 12.0F, 7.0F));
            root.addOrReplaceChild("leg2", CubeListBuilder.create().texOffs(0, 16).addBox(-1.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F), PartPose.offset(3.0F, 12.0F, 7.0F));
            root.addOrReplaceChild("leg3", CubeListBuilder.create().texOffs(0, 16).addBox(-3.0F, 0.0F, -3.0F, 4.0F, 12.0F, 4.0F), PartPose.offset(-3.0F, 12.0F, -5.0F));
            root.addOrReplaceChild("leg4", CubeListBuilder.create().texOffs(0, 16).addBox(-1.0F, 0.0F, -3.0F, 4.0F, 12.0F, 4.0F), PartPose.offset(3.0F, 12.0F, -5.0F));
            root.addOrReplaceChild("udders", CubeListBuilder.create().texOffs(52, 0).addBox(-2.0F, -3.0F, 0.0F, 4.0F, 6.0F, 2.0F),
                    PartPose.offsetAndRotation(0.0F, 14.0F, 6.0F, 1.570796F, 0.0F, 0.0F));
            root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(64, 0).addBox(-4.0F, 0.0F, -2.5F, 8.0F, 12.0F, 5.0F), PartPose.offset(0.0F, -6.0F, -9.0F));
            root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(90, 0).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F),
                    PartPose.offset(5.0F, -4.0F, -9.0F));
            root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(90, 0).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F),
                    PartPose.offset(-5.0F, -4.0F, -9.0F));
            root.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.ZERO);
            root.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.ZERO);
            return LayerDefinition.create(mesh, 128, 32);
        }

        /** setRotationAngles: the biped arms with the held item, the four cow legs walking. */
        @Override
        public void setupAnim(MinoshroomState s) {
            this.resetPose();
            float f = s.walkAnimationPos;
            float f1 = s.walkAnimationSpeed;
            float f2 = s.ageInTicks;
            this.head.yRot = s.yRot * Mth.DEG_TO_RAD;
            this.head.xRot = s.xRot * Mth.DEG_TO_RAD;
            this.rightArm.xRot = Mth.cos(f * 0.6662F + Mth.PI) * 2.0F * f1 * 0.5F;
            this.leftArm.xRot = Mth.cos(f * 0.6662F) * 2.0F * f1 * 0.5F;
            if (!s.rightHandItemStack.isEmpty()) {
                this.rightArm.xRot = this.rightArm.xRot * 0.5F - Mth.PI / 10.0F;
            }
            this.rightArm.zRot += Mth.cos(f2 * 0.09F) * 0.05F + 0.05F;
            this.leftArm.zRot -= Mth.cos(f2 * 0.09F) * 0.05F + 0.05F;
            this.rightArm.xRot += Mth.sin(f2 * 0.067F) * 0.05F;
            this.leftArm.xRot -= Mth.sin(f2 * 0.067F) * 0.05F;
            this.cowBody.xRot = Mth.PI / 2.0F;
            this.legs[0].xRot = Mth.cos(f * 0.6662F) * 1.4F * f1;
            this.legs[1].xRot = Mth.cos(f * 0.6662F + Mth.PI) * 1.4F * f1;
            this.legs[2].xRot = Mth.cos(f * 0.6662F + Mth.PI) * 1.4F * f1;
            this.legs[3].xRot = Mth.cos(f * 0.6662F) * 1.4F * f1;
        }
    }

    /** RenderTFMinoshroom: two red mushrooms on the back and one on the head, as on a mooshroom. */
    static class MinoshroomRenderer extends HumanoidMobRenderer<Minoshroom, MinoshroomState, MinoshroomModel> {
        private static final Identifier TEXTURE = TFMobRenderer.texture("minoshroomtaur");
        private static final BlockDisplayContext MUSHROOMS = BlockDisplayContext.create();
        private final BlockModelResolver blockModels;

        MinoshroomRenderer(EntityRendererProvider.Context context) {
            super(context, new MinoshroomModel(context.bakeLayer(MINOSHROOM)), 0.625F);
            this.blockModels = context.getBlockModelResolver();
            this.addLayer(new RenderLayer<>(this) {
                @Override
                public void submit(PoseStack poseStack, SubmitNodeCollector collector, int light, MinoshroomState state, float yRot, float xRot) {
                    if (state.isInvisible || state.mushroom.isEmpty()) {
                        return;
                    }
                    int overlay = LivingEntityRenderer.getOverlayCoords(state, 0.0F);
                    poseStack.pushPose();
                    poseStack.translate(0.2F, 0.375F - 0.75F, 0.5F);
                    poseStack.rotateDegrees(Axis.YP, -48.0F);
                    poseStack.scale(-1.0F, -1.0F, 1.0F);
                    poseStack.translate(-0.5F, -0.5F, -0.5F);
                    state.mushroom.submit(poseStack, collector, light, overlay, state.outlineColor);
                    poseStack.popPose();
                    poseStack.pushPose();
                    poseStack.translate(0.2F, 0.375F - 0.75F, 0.5F);
                    poseStack.rotateDegrees(Axis.YP, 42.0F);
                    poseStack.translate(0.1F, 0.0F, -0.6F);
                    poseStack.rotateDegrees(Axis.YP, -48.0F);
                    poseStack.scale(-1.0F, -1.0F, 1.0F);
                    poseStack.translate(-0.5F, -0.5F, -0.5F);
                    state.mushroom.submit(poseStack, collector, light, overlay, state.outlineColor);
                    poseStack.popPose();
                    poseStack.pushPose();
                    this.getParentModel().getHead().translateAndRotate(poseStack);
                    poseStack.translate(0.0F, -0.7F, -0.2F);
                    poseStack.rotateDegrees(Axis.YP, -78.0F);
                    poseStack.scale(-1.0F, -1.0F, 1.0F);
                    poseStack.translate(-0.5F, -0.5F, -0.5F);
                    state.mushroom.submit(poseStack, collector, light, overlay, state.outlineColor);
                    poseStack.popPose();
                }
            });
        }

        @Override
        public MinoshroomState createRenderState() {
            return new MinoshroomState();
        }

        @Override
        public void extractRenderState(Minoshroom entity, MinoshroomState state, float partialTick) {
            super.extractRenderState(entity, state, partialTick);
            this.blockModels.update(state.mushroom, Blocks.RED_MUSHROOM.defaultBlockState(), MUSHROOMS);
        }

        @Override
        public Identifier getTextureLocation(MinoshroomState state) {
            return TEXTURE;
        }
    }

    // ---------------------------------------------------------------- ur-ghast

    /**
     * ModelTFTowerBoss: the ghast body with nine short tentacles at fixed places, each of three segments that sway.
     */
    public static class UrGhastModel extends EntityModel<LivingEntityRenderState> {
        private final ModelPart body;
        private final ModelPart[] tentacles = new ModelPart[9];
        private final ModelPart[][] sub = new ModelPart[9][3];

        public UrGhastModel(ModelPart root) {
            super(root);
            this.body = root.getChild("body");
            for (int i = 0; i < 9; i++) {
                this.tentacles[i] = this.body.getChild("tentacle" + i);
                ModelPart parent = this.tentacles[i];
                for (int j = 0; j < 3; j++) {
                    this.sub[i][j] = parent.getChild("sub" + j);
                    parent = this.sub[i][j];
                }
            }
        }

        public static LayerDefinition createBodyLayer() {
            MeshDefinition mesh = new MeshDefinition();
            PartDefinition root = mesh.getRoot();
            int yOffset = -16;
            PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, -8.0F, -8.0F, 16.0F, 16.0F, 16.0F),
                    PartPose.offset(0.0F, 24 + yOffset, 0.0F));
            float[][] points = {{4.5F, 23 + yOffset, 4.5F, 0}, {-4.5F, 23 + yOffset, 4.5F, 0}, {0.0F, 23 + yOffset, 0.0F, 0}, {5.5F, 23 + yOffset, -4.5F, 0},
                    {-5.5F, 23 + yOffset, -4.5F, 0}, {-7.5F, 3.5F, -1.0F, (float) (Math.PI / 4)}, {-7.5F, -1.5F, 3.5F, (float) (Math.PI / 3)},
                    {7.5F, 3.5F, -1.0F, (float) (-Math.PI / 4)}, {7.5F, -1.5F, 3.5F, (float) (-Math.PI / 3)}};
            for (int num = 0; num < 9; num++) {
                PartDefinition tentacle = body.addOrReplaceChild("tentacle" + num, CubeListBuilder.create().texOffs(num % 3, 0)
                        .addBox(-1.5F, 0.0F, -1.5F, 3.0F, 5.0F, 3.0F), PartPose.offsetAndRotation(points[num][0], points[num][1], points[num][2], 0.0F, 0.0F,
                        points[num][3]));
                PartDefinition parent = tentacle;
                for (int i = 0; i < 3; i++) {
                    parent = parent.addOrReplaceChild("sub" + i, CubeListBuilder.create().texOffs(num % 4, i * 5 - 1)
                            .addBox(-1.5F, -0.5F, -1.5F, 3.0F, 4.0F, 3.0F), PartPose.offset(0.0F, 4.0F, 0.0F));
                }
            }
            return LayerDefinition.create(mesh, 64, 32);
        }

        @Override
        public void setupAnim(LivingEntityRenderState state) {
            super.setupAnim(state);
            for (int i = 0; i < 9; i++) {
                this.tentacles[i].xRot = 0.2F * Mth.sin(state.ageInTicks * 0.3F + i) + 0.4F;
                float time = (state.ageInTicks + i * 9) / 2.0F;
                this.sub[i][0].xRot = 0.2F + Mth.cos(time * 0.3335F) * 0.15F;
                this.sub[i][1].xRot = 0.1F + Mth.cos(time * 0.4445F) * 0.2F;
                this.sub[i][2].xRot = 0.1F + Mth.cos(time * 0.5555F) * 0.25F;
                this.tentacles[i].yRot = 0.4F * Mth.sin(time * 0.3F);
            }
            this.body.xRot = state.xRot * Mth.DEG_TO_RAD;
            this.body.yRot = state.yRot * Mth.DEG_TO_RAD;
        }
    }
}

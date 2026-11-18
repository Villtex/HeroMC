package betterdungeons.client.mob;

import betterdungeons.BetterDungeons;
import betterdungeons.mob.BDCreatures;
import betterdungeons.mob.GolemBoss;
import betterdungeons.mob.IceBull;
import betterdungeons.mob.MonkingBoss;
import betterdungeons.mob.SpiderBoss;
import betterdungeons.mob.SummonedBait;
import betterdungeons.mob.SummonedUndead;
import betterdungeons.mob.TurtleBoss;
import betterdungeons.mob.TurtlePart;
import betterdungeons.mob.Wyvern;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.SlimeRenderer;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

import java.util.Random;

/**
 * ClientProxy.registerRenderInformation for the bosses and summoned mobs of {@link BDCreatures}: RenderBull,
 * RenderBipedCustom(ModelGiant), RenderGolemBoss, RenderSpiderBoss, RenderTurtle + RenderGeneric(ModelTurtlePart),
 * RenderLiving(ModelDragonQuadruped), RenderBiped(ModelSkeletonSummoned) and RenderBiped(ModelBiped) for the bait; the
 * slime boss uses the vanilla slime renderer (it was an EntitySlime with /mob/slime.png).
 */
public final class BDCreatureRenderers {
    public static final ModelLayerLocation BULL = layer("ice_bull");
    public static final ModelLayerLocation MONKING = layer("monking");
    public static final ModelLayerLocation GOLEM_BOSS = layer("golem_boss");
    public static final ModelLayerLocation SPIDER_BOSS = layer("spider_boss");
    public static final ModelLayerLocation TURTLE = layer("turtle_boss");
    public static final ModelLayerLocation TURTLE_PART = layer("turtle_boss_part");
    public static final ModelLayerLocation WYVERN = layer("wyvern");
    public static final ModelLayerLocation SUMMONED_UNDEAD = layer("summoned_undead");
    public static final ModelLayerLocation SUMMONED_BAIT = layer("summoned_bait");

    private BDCreatureRenderers() {
    }

    private static ModelLayerLocation layer(String name) {
        return new ModelLayerLocation(Identifier.fromNamespaceAndPath(BetterDungeons.MOD_ID, name), "main");
    }

    static Identifier tex(String name) {
        return Identifier.fromNamespaceAndPath(BetterDungeons.MOD_ID, "textures/entity/" + name + ".png");
    }

    public static void register(IEventBus modBus) {
        modBus.addListener(BDCreatureRenderers::layers);
        modBus.addListener(BDCreatureRenderers::renderers);
    }

    private static void layers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(BULL, BullModel::createBodyLayer);
        event.registerLayerDefinition(MONKING, MonkingModel::createBodyLayer);
        event.registerLayerDefinition(GOLEM_BOSS, GolemBossModel::createBodyLayer);
        event.registerLayerDefinition(SPIDER_BOSS, SpiderBossModel::createBodyLayer);
        event.registerLayerDefinition(TURTLE, TurtleModel::createShellLayer);
        event.registerLayerDefinition(TURTLE_PART, TurtleModel::createPartLayer);
        event.registerLayerDefinition(WYVERN, WyvernModel::createBodyLayer);
        event.registerLayerDefinition(SUMMONED_UNDEAD, BDCreatureRenderers::skeletonLayer);
        // ModelBiped (64x32 layout) on the summoner's 64x64 skin
        event.registerLayerDefinition(SUMMONED_BAIT, () -> LayerDefinition.create(HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F), 64, 64));
    }

    private static void renderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(BDCreatures.ICE_BULL.get(), BullRenderer::new);
        event.registerEntityRenderer(BDCreatures.MONKING.get(), MonkingRenderer::new);
        event.registerEntityRenderer(BDCreatures.GOLEM_BOSS.get(), GolemBossRenderer::new);
        event.registerEntityRenderer(BDCreatures.SPIDER_BOSS.get(), SpiderBossRenderer::new);
        event.registerEntityRenderer(BDCreatures.SLIME_BOSS.get(), SlimeRenderer::new);
        event.registerEntityRenderer(BDCreatures.TURTLE_BOSS.get(), TurtleRenderer::new);
        event.registerEntityRenderer(BDCreatures.WYVERN.get(), WyvernRenderer::new);
        event.registerEntityRenderer(BDCreatures.SUMMONED_UNDEAD.get(), UndeadRenderer::new);
        event.registerEntityRenderer(BDCreatures.SUMMONED_BAIT.get(), BaitRenderer::new);
    }

    /** ModelSkeletonSummoned: the skeleton's thin limbs on ModelBiped (64x32). */
    private static LayerDefinition skeletonLayer() {
        MeshDefinition mesh = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F);
        PartDefinition r = mesh.getRoot();
        r.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(40, 16).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 12.0F, 2.0F), PartPose.offset(-5.0F, 2.0F, 0.0F));
        r.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(40, 16).mirror().addBox(-1.0F, -2.0F, -1.0F, 2.0F, 12.0F, 2.0F),
                PartPose.offset(5.0F, 2.0F, 0.0F));
        r.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 16).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 12.0F, 2.0F), PartPose.offset(-2.0F, 12.0F, 0.0F));
        r.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(0, 16).mirror().addBox(-1.0F, 0.0F, -1.0F, 2.0F, 12.0F, 2.0F),
                PartPose.offset(2.0F, 12.0F, 0.0F));
        return LayerDefinition.create(mesh, 64, 32);
    }

    // ------------------------------------------------------------------------------------------------ ice bull

    /** RenderBull: shadow 1.2, moved 0.36 down and stretched 1.5 x 1.2 x 1.2. */
    static class BullRenderer extends MobRenderer<IceBull, LivingEntityRenderState, BullModel> {
        private static final Identifier TEXTURE = tex("ice_bull");

        BullRenderer(EntityRendererProvider.Context c) {
            super(c, new BullModel(c.bakeLayer(BULL)), 1.2F);
        }

        @Override
        public LivingEntityRenderState createRenderState() {
            return new LivingEntityRenderState();
        }

        @Override
        protected void scale(LivingEntityRenderState state, PoseStack poseStack) {
            poseStack.translate(0.0F, 0.36F, 0.0F);
            poseStack.scale(1.5F, 1.2F, 1.2F);
        }

        @Override
        public Identifier getTextureLocation(LivingEntityRenderState state) {
            return TEXTURE;
        }
    }

    // ------------------------------------------------------------------------------------------------ monking, golem boss

    static void extractGiant(MonkingBoss e, MonkingModel.State s) {
        s.attacking = e.isAttacking();
        s.attackAnim = e.getAttackAnimTime();
        s.attackAnimTime = e.totalAttackAnimTime;
        s.attackType = e.getAttackType();
        s.entityId = e.getId();
        s.tickCount = e.tickCount;
    }

    /** RenderBipedCustom(ModelGiant), shadow 1. */
    static class MonkingRenderer extends MobRenderer<MonkingBoss, MonkingModel.State, MonkingModel> {
        private static final Identifier TEXTURE = tex("monking");

        MonkingRenderer(EntityRendererProvider.Context c) {
            super(c, new MonkingModel(c.bakeLayer(MONKING)), 1.0F);
        }

        @Override
        public MonkingModel.State createRenderState() {
            return new MonkingModel.State();
        }

        @Override
        public void extractRenderState(MonkingBoss entity, MonkingModel.State state, float partialTick) {
            super.extractRenderState(entity, state, partialTick);
            extractGiant(entity, state);
        }

        @Override
        public Identifier getTextureLocation(MonkingModel.State state) {
            return TEXTURE;
        }
    }

    /**
     * RenderGolemBoss: tinted red the more it is hurt (glColor3f(1, h / 2 + 0.5, h / 2 + 0.5)), and while
     * powersource is set five random light blue bolts around it (seeded with id + age, so they hold for a tick).
     */
    static class GolemBossRenderer extends MobRenderer<GolemBoss, MonkingModel.State, GolemBossModel> {
        private static final Identifier TEXTURE = tex("golem_boss");

        GolemBossRenderer(EntityRendererProvider.Context c) {
            super(c, new GolemBossModel(c.bakeLayer(GOLEM_BOSS)), 1.0F);
        }

        @Override
        public MonkingModel.State createRenderState() {
            return new MonkingModel.State();
        }

        @Override
        public void extractRenderState(GolemBoss entity, MonkingModel.State state, float partialTick) {
            super.extractRenderState(entity, state, partialTick);
            extractGiant(entity, state);
            state.renderHeal = entity.renderHeal();
            state.powersource = entity.powersource;
            state.boundingBoxWidth = entity.getBbWidth();
        }

        @Override
        protected int getModelTint(MonkingModel.State state) {
            float gb = state.renderHeal / 2.0F + 0.5F;
            return ARGB.colorFromFloat(1.0F, 1.0F, gb, gb);
        }

        @Override
        public void submit(MonkingModel.State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
            if (state.powersource) {
                this.electricField(state, poseStack, collector);
            }
            super.submit(state, poseStack, collector, camera);
        }

        private void electricField(MonkingModel.State state, PoseStack poseStack, SubmitNodeCollector collector) {
            Random rnd = new Random(state.entityId + state.tickCount);
            int color = ARGB.colorFromFloat(0.6F, 0.5F, 0.64F, 1.0F);
            collector.submitCustomGeometry(poseStack, RenderTypes.lines(), (pose, buffer) -> {
                for (int a = 0; a < 5; a++) {
                    int steps = rnd.nextInt(26) + 5;
                    int startX = rnd.nextInt();
                    int startY = rnd.nextInt();
                    int startZ = rnd.nextInt();
                    float lx = 0.0F;
                    float ly = 0.0F;
                    float lz = 0.0F;
                    for (int i = 0; i <= steps; i++) {
                        double dist = state.boundingBoxWidth;
                        double boltSize = 80.0;
                        float x = (float) (Math.sin((i + startX) / boltSize * Math.PI * 2.0) * dist + (rnd.nextDouble() - 0.5) * 0.5);
                        float z = (float) (Math.cos((i + startZ) / boltSize * Math.PI * 2.0) * dist + (rnd.nextDouble() - 0.5) * 0.5);
                        float y = (float) (Math.sin((state.tickCount + i + startY) / boltSize * Math.PI) + rnd.nextDouble() + 1.8);
                        if (i > 0) {
                            float nx = x - lx;
                            float ny = y - ly;
                            float nz = z - lz;
                            float len = Mth.sqrt(nx * nx + ny * ny + nz * nz);
                            if (len > 0.0F) {
                                nx /= len;
                                ny /= len;
                                nz /= len;
                            }
                            buffer.addVertex(pose, lx, ly, lz).setColor(color).setNormal(pose, nx, ny, nz).setLineWidth(3.0F);
                            buffer.addVertex(pose, x, y, z).setColor(color).setNormal(pose, nx, ny, nz).setLineWidth(3.0F);
                        }
                        lx = x;
                        ly = y;
                        lz = z;
                    }
                }
            });
        }

        @Override
        public Identifier getTextureLocation(MonkingModel.State state) {
            return TEXTURE;
        }
    }

    // ------------------------------------------------------------------------------------------------ spider boss

    /** RenderSpiderBoss: 1.5 times the size, falls 180 degrees when dying, glowing eyes (/mob/spider_eyes.png). */
    static class SpiderBossRenderer extends MobRenderer<SpiderBoss, SpiderBossModel.State, SpiderBossModel> {
        private static final Identifier TEXTURE = tex("spider_boss");
        private static final RenderType EYES = RenderTypes.eyes(Identifier.withDefaultNamespace("textures/entity/spider/spider_eyes.png"));

        SpiderBossRenderer(EntityRendererProvider.Context c) {
            super(c, new SpiderBossModel(c.bakeLayer(SPIDER_BOSS)), 1.0F);
            this.addLayer(new EyesLayer<>(this) {
                @Override
                public RenderType renderType() {
                    return EYES;
                }
            });
        }

        @Override
        public SpiderBossModel.State createRenderState() {
            return new SpiderBossModel.State();
        }

        @Override
        public void extractRenderState(SpiderBoss entity, SpiderBossModel.State state, float partialTick) {
            super.extractRenderState(entity, state, partialTick);
            state.swing = entity.getSwingAnimation(partialTick);
        }

        @Override
        protected void scale(SpiderBossModel.State state, PoseStack poseStack) {
            poseStack.scale(1.5F, 1.5F, 1.5F);
        }

        @Override
        protected float getFlipDegrees() {
            return 180.0F;
        }

        @Override
        public Identifier getTextureLocation(SpiderBossModel.State state) {
            return TEXTURE;
        }
    }

    // ------------------------------------------------------------------------------------------------ turtle

    public static class TurtleState extends EntityRenderState {
        public float yaw;
        public final TurtleModel.PartState[] parts = new TurtleModel.PartState[5];

        public TurtleState() {
            for (int i = 0; i < 5; i++) {
                this.parts[i] = new TurtleModel.PartState();
            }
        }
    }

    /**
     * RenderTurtle (a plain Render, no hurt tint or death tilt; shadow 1): the shell 1.5 above the feet, turned
     * 180 - yaw and flipped; RenderGeneric for each part that is still there, 1.7 above the part's feet, turned by the
     * part's own yaw.
     */
    static class TurtleRenderer extends EntityRenderer<TurtleBoss, TurtleState> {
        private static final Identifier TEXTURE = tex("turtle_boss");
        private final TurtleModel.Shell shell;
        private final TurtleModel.Part part;

        TurtleRenderer(EntityRendererProvider.Context c) {
            super(c);
            this.shell = new TurtleModel.Shell(c.bakeLayer(TURTLE));
            this.part = new TurtleModel.Part(c.bakeLayer(TURTLE_PART));
            this.shadowRadius = 1.0F;
        }

        @Override
        public TurtleState createRenderState() {
            return new TurtleState();
        }

        @Override
        public void extractRenderState(TurtleBoss entity, TurtleState state, float partialTick) {
            super.extractRenderState(entity, state, partialTick);
            state.yaw = entity.getYRot(partialTick);
            TurtlePart[] parts = entity.getAllParts();
            for (int i = 0; i < parts.length; i++) {
                TurtlePart p = parts[i];
                TurtleModel.PartState ps = state.parts[i];
                ps.head = p.isHead();
                ps.visible = !p.isGone();
                ps.dx = Mth.lerp(partialTick, p.xo, p.getX()) - state.x;
                ps.dy = Mth.lerp(partialTick, p.yo, p.getY()) - state.y;
                ps.dz = Mth.lerp(partialTick, p.zo, p.getZ()) - state.z;
                ps.yaw = Mth.rotLerp(partialTick, p.yRotO, p.getYRot());
                ps.lightCoords = state.lightCoords;
                ps.outlineColor = state.outlineColor;
            }
        }

        @Override
        public void submit(TurtleState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
            poseStack.pushPose();
            poseStack.translate(0.0F, 1.5F, 0.0F);
            poseStack.rotateDegrees(Axis.YP, 180.0F - state.yaw);
            poseStack.scale(-1.0F, -1.0F, 1.0F);
            collector.submitModel(this.shell, state, poseStack, TEXTURE, state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
            poseStack.popPose();
            for (TurtleModel.PartState ps : state.parts) {
                if (!ps.visible) {
                    continue;
                }
                poseStack.pushPose();
                poseStack.translate(ps.dx, ps.dy + 1.7, ps.dz);
                poseStack.rotateDegrees(Axis.YP, 180.0F - ps.yaw);
                poseStack.scale(-1.0F, -1.0F, 1.0F);
                collector.submitModel(this.part, ps, poseStack, TEXTURE, ps.lightCoords, OverlayTexture.NO_OVERLAY, ps.outlineColor);
                poseStack.popPose();
            }
            super.submit(state, poseStack, collector, camera);
        }

        /** The parts stand up to 2 blocks out of the shell. */
        @Override
        protected AABB getBoundingBoxForCulling(TurtleBoss entity, float partialTicks) {
            return super.getBoundingBoxForCulling(entity, partialTicks).inflate(2.5);
        }
    }

    // ------------------------------------------------------------------------------------------------ wyvern

    /** RenderLiving(ModelDragonQuadruped), shadow 1; its texture bdimg/dragonbd.png was missing from the mod. */
    static class WyvernRenderer extends MobRenderer<Wyvern, WyvernModel.State, WyvernModel> {
        private static final Identifier TEXTURE = tex("dragonbd");

        WyvernRenderer(EntityRendererProvider.Context c) {
            super(c, new WyvernModel(c.bakeLayer(WYVERN)), 1.0F);
        }

        @Override
        public WyvernModel.State createRenderState() {
            return new WyvernModel.State();
        }

        @Override
        public void extractRenderState(Wyvern entity, WyvernModel.State state, float partialTick) {
            super.extractRenderState(entity, state, partialTick);
            state.onGround = entity.onGround();
        }

        @Override
        public Identifier getTextureLocation(WyvernModel.State state) {
            return TEXTURE;
        }
    }

    // ------------------------------------------------------------------------------------------------ summoned

    public static class UndeadState extends HumanoidRenderState {
        public int lvl;
    }

    /** ModelSkeletonSummoned: ModelBiped's pose, then both arms held forward like a zombie's (with the swing). */
    static class UndeadModel extends HumanoidModel<UndeadState> {
        UndeadModel(ModelPart root) {
            super(root);
        }

        @Override
        public void setupAnim(UndeadState s) {
            super.setupAnim(s);
            float f6 = Mth.sin(s.swingAnimation * Mth.PI);
            float f7 = Mth.sin((1.0F - (1.0F - s.swingAnimation) * (1.0F - s.swingAnimation)) * Mth.PI);
            this.rightArm.zRot = 0.0F;
            this.leftArm.zRot = 0.0F;
            this.rightArm.yRot = -(0.1F - f6 * 0.6F);
            this.leftArm.yRot = 0.1F - f6 * 0.6F;
            this.rightArm.xRot = -Mth.HALF_PI;
            this.leftArm.xRot = -Mth.HALF_PI;
            this.rightArm.xRot -= f6 * 1.2F - f7 * 0.4F;
            this.leftArm.xRot -= f6 * 1.2F - f7 * 0.4F;
            this.rightArm.zRot += Mth.cos(s.ageInTicks * 0.09F) * 0.05F + 0.05F;
            this.leftArm.zRot -= Mth.cos(s.ageInTicks * 0.09F) * 0.05F + 0.05F;
            this.rightArm.xRot += Mth.sin(s.ageInTicks * 0.067F) * 0.05F;
            this.leftArm.xRot -= Mth.sin(s.ageInTicks * 0.067F) * 0.05F;
        }
    }

    /** RenderBiped(ModelSkeletonSummoned, 0.5): the skeleton texture, the wither skeleton's at level 3. */
    static class UndeadRenderer extends HumanoidMobRenderer<SummonedUndead, UndeadState, UndeadModel> {
        private static final Identifier SKELETON = Identifier.withDefaultNamespace("textures/entity/skeleton/skeleton.png");
        private static final Identifier WITHER = Identifier.withDefaultNamespace("textures/entity/skeleton/wither_skeleton.png");

        UndeadRenderer(EntityRendererProvider.Context c) {
            super(c, new UndeadModel(c.bakeLayer(SUMMONED_UNDEAD)), 0.5F);
        }

        @Override
        public UndeadState createRenderState() {
            return new UndeadState();
        }

        @Override
        public void extractRenderState(SummonedUndead entity, UndeadState state, float partialTick) {
            super.extractRenderState(entity, state, partialTick);
            state.lvl = entity.getLvl();
        }

        @Override
        public Identifier getTextureLocation(UndeadState state) {
            return state.lvl == 3 ? WITHER : SKELETON;
        }
    }

    public static class BaitState extends HumanoidRenderState {
        public Identifier skin = net.minecraft.client.resources.DefaultPlayerSkin.getDefaultTexture();
    }

    /** RenderBiped(ModelBiped, 0.5) with the summoner's texture (its skin), its armour and held item. */
    static class BaitRenderer extends HumanoidMobRenderer<SummonedBait, BaitState, HumanoidModel<BaitState>> {
        BaitRenderer(EntityRendererProvider.Context c) {
            super(c, new HumanoidModel<>(c.bakeLayer(SUMMONED_BAIT)), 0.5F);
            this.addLayer(new HumanoidArmorLayer<>(this, ArmorModelSet.bake(ModelLayers.PLAYER_ARMOR, c.getModelSet(), HumanoidModel::new),
                    c.getEquipmentRenderer()));
        }

        @Override
        public BaitState createRenderState() {
            return new BaitState();
        }

        @Override
        public void extractRenderState(SummonedBait entity, BaitState state, float partialTick) {
            super.extractRenderState(entity, state, partialTick);
            LivingEntity summoner = entity.getSummoner();
            state.skin = summoner instanceof AbstractClientPlayer player ? player.getSkin().body().texturePath() : net.minecraft.client.resources.DefaultPlayerSkin.getDefaultTexture();
        }

        @Override
        public Identifier getTextureLocation(BaitState state) {
            return state.skin;
        }
    }
}

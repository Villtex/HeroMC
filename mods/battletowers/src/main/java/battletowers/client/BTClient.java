package battletowers.client;

import battletowers.BattleTowers;
import battletowers.entity.BattleTowerGolem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/** The renderers: AS_RenderGolem (a biped at twice the size, awake or dormant texture) and AS_RenderFireball(0.5). */
public final class BTClient {
    static final ModelLayerLocation GOLEM = new ModelLayerLocation(Identifier.fromNamespaceAndPath(BattleTowers.MOD_ID, "golem"), "main");
    private static final Identifier AWAKE = Identifier.fromNamespaceAndPath(BattleTowers.MOD_ID, "textures/entity/golem.png");
    private static final Identifier DORMANT = Identifier.fromNamespaceAndPath(BattleTowers.MOD_ID, "textures/entity/golem_dormant.png");

    private BTClient() {
    }

    public static void register(IEventBus modBus) {
        modBus.addListener(EntityRenderersEvent.RegisterLayerDefinitions.class, event ->
                event.registerLayerDefinition(GOLEM, () -> LayerDefinition.create(HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F), 64, 32)));
        modBus.addListener(EntityRenderersEvent.RegisterRenderers.class, event -> {
            event.registerEntityRenderer(BattleTowers.GOLEM.get(), GolemRenderer::new);
            // the fire charge icon as a camera-facing quad at half size
            event.registerEntityRenderer(BattleTowers.GOLEM_FIREBALL.get(), c -> new ThrownItemRenderer<>(c, 0.5F, true));
        });
    }

    public static class GolemState extends HumanoidRenderState {
        boolean dormant = true;
    }

    /** RenderBiped(ModelBiped, 1.0) scaled by 2 (rescaleGolem). */
    static class GolemRenderer extends HumanoidMobRenderer<BattleTowerGolem, GolemState, HumanoidModel<GolemState>> {
        GolemRenderer(EntityRendererProvider.Context c) {
            super(c, new HumanoidModel<>(c.bakeLayer(GOLEM)), 1.0F);
        }

        @Override
        public GolemState createRenderState() {
            return new GolemState();
        }

        @Override
        public void extractRenderState(BattleTowerGolem entity, GolemState state, float partialTick) {
            super.extractRenderState(entity, state, partialTick);
            state.dormant = entity.isDormant();
        }

        @Override
        protected void scale(GolemState state, PoseStack poseStack) {
            poseStack.scale(2.0F, 2.0F, 2.0F);
        }

        @Override
        public Identifier getTextureLocation(GolemState state) {
            return state.dormant ? DORMANT : AWAKE;
        }
    }
}

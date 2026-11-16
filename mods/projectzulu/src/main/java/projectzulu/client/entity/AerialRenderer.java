package projectzulu.client.entity;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;
import projectzulu.ProjectZulu;
import projectzulu.entity.AerialAnimal;

import java.util.function.Supplier;

/** RenderGenericLiving (shadow 0.5) for the birds, filling the {@link BirdRenderState}. */
public class AerialRenderer<T extends AerialAnimal, S extends BirdRenderState> extends MobRenderer<T, S, EntityModel<S>> {
    /** Wraps the game time so the float keeps whole ticks (1.5.2 cast the long world time to float). */
    private static final long TIME_WRAP = 1L << 20;

    private final Identifier texture;
    private final Supplier<S> stateFactory;

    public AerialRenderer(EntityRendererProvider.Context context, EntityModel<S> model, Supplier<S> stateFactory, String texture) {
        super(context, model, 0.5F);
        this.stateFactory = stateFactory;
        this.texture = Identifier.fromNamespaceAndPath(ProjectZulu.MOD_ID, "textures/entity/" + texture + ".png");
    }

    @Override
    public S createRenderState() {
        return this.stateFactory.get();
    }

    @Override
    public void extractRenderState(T entity, S state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.worldTime = (float) (entity.level().getGameTime() % TIME_WRAP);
        state.entityState = entity.getEntityState();
        state.grounded = entity.isEntityGrounded();
        state.onGround = entity.onGround();
    }

    @Override
    public Identifier getTextureLocation(S state) {
        return this.texture;
    }
}

package projectzulu.client.entity;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Mob;
import projectzulu.ProjectZulu;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * RenderGenericLiving (shadow 0.5) for the Batch D monsters: one model, a texture chosen from the render state and a
 * hook that copies the entity state the model needs into the render state.
 */
public class MonsterRenderer<T extends Mob, S extends LivingEntityRenderState> extends MobRenderer<T, S, EntityModel<S>> {
    private final Supplier<S> stateFactory;
    private final BiConsumer<T, S> extractor;
    private final Function<S, Identifier> texture;

    public MonsterRenderer(EntityRendererProvider.Context context, EntityModel<S> model, Supplier<S> stateFactory,
            BiConsumer<T, S> extractor, Function<S, Identifier> texture) {
        super(context, model, 0.5F);
        this.stateFactory = stateFactory;
        this.extractor = extractor;
        this.texture = texture;
    }

    public MonsterRenderer(EntityRendererProvider.Context context, EntityModel<S> model, Supplier<S> stateFactory,
            BiConsumer<T, S> extractor, String texture) {
        this(context, model, stateFactory, extractor, s -> tex(texture));
    }

    static Identifier tex(String name) {
        return Identifier.fromNamespaceAndPath(ProjectZulu.MOD_ID, "textures/entity/" + name + ".png");
    }

    @Override
    public S createRenderState() {
        return this.stateFactory.get();
    }

    @Override
    public void extractRenderState(T entity, S state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        this.extractor.accept(entity, state);
    }

    @Override
    public Identifier getTextureLocation(S state) {
        return this.texture.apply(state);
    }
}

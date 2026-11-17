package projectzulu.client.entity;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.AgeableMobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.animal.Animal;
import projectzulu.ProjectZulu;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

/**
 * RenderGenericLiving / RenderTameable (shadow 0.5) for the land animals of Batch B: adult and baby model, one
 * texture, and an optional hook that copies entity state the model needs (sitting, on ground) into the render state.
 */
public class LandAnimalRenderer<T extends Animal, S extends LivingEntityRenderState> extends AgeableMobRenderer<T, S, EntityModel<S>> {
    private final Identifier texture;
    private final Supplier<S> stateFactory;
    private final BiConsumer<T, S> extractor;

    public LandAnimalRenderer(EntityRendererProvider.Context context, EntityModel<S> adult, EntityModel<S> baby, Supplier<S> stateFactory,
            String texture, BiConsumer<T, S> extractor) {
        super(context, adult, baby, 0.5F);
        this.stateFactory = stateFactory;
        this.extractor = extractor;
        this.texture = Identifier.fromNamespaceAndPath(ProjectZulu.MOD_ID, "textures/entity/" + texture + ".png");
    }

    public LandAnimalRenderer(EntityRendererProvider.Context context, EntityModel<S> adult, EntityModel<S> baby, Supplier<S> stateFactory,
            String texture) {
        this(context, adult, baby, stateFactory, texture, (entity, state) -> {
        });
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
        return this.texture;
    }
}

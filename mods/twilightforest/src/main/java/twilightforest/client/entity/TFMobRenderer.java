package twilightforest.client.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Mob;
import org.jspecify.annotations.Nullable;
import twilightforest.TwilightForest;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * RenderLiving of the Twilight mobs: one model (a baby model when the mob is young), a texture per state, an optional
 * scale (preRenderCallback) and a hook that copies what the model needs into the render state.
 */
public class TFMobRenderer<T extends Mob, S extends LivingEntityRenderState> extends MobRenderer<T, S, EntityModel<? super S>> {
    private final EntityModel<? super S> adult;
    private final @Nullable EntityModel<? super S> baby;
    private final Supplier<S> stateFactory;
    private final Function<S, Identifier> texture;
    private final BiConsumer<T, S> extractor;
    private float scale = 1.0F;

    public TFMobRenderer(EntityRendererProvider.Context context, EntityModel<? super S> adult, @Nullable EntityModel<? super S> baby, float shadow,
            Supplier<S> stateFactory, Function<S, Identifier> texture, BiConsumer<T, S> extractor) {
        super(context, adult, shadow);
        this.adult = adult;
        this.baby = baby;
        this.stateFactory = stateFactory;
        this.texture = texture;
        this.extractor = extractor;
    }

    public static Identifier texture(String name) {
        return TwilightForest.id("textures/entity/" + name + ".png");
    }

    /** A renderer with one texture and no extra state. */
    public static <T extends Mob> TFMobRenderer<T, LivingEntityRenderState> simple(EntityRendererProvider.Context context,
            EntityModel<? super LivingEntityRenderState> model, float shadow, String texture) {
        Identifier location = texture(texture);
        return new TFMobRenderer<>(context, model, null, shadow, LivingEntityRenderState::new, s -> location, (e, s) -> {
        });
    }

    public TFMobRenderer<T, S> scaled(float scale) {
        this.scale = scale;
        return this;
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
    public void submit(S state, PoseStack poseStack, net.minecraft.client.renderer.SubmitNodeCollector collector,
            net.minecraft.client.renderer.state.level.CameraRenderState camera) {
        this.model = state.isBaby && this.baby != null ? this.baby : this.adult;
        super.submit(state, poseStack, collector, camera);
    }

    @Override
    protected void scale(S state, PoseStack poseStack) {
        if (this.scale != 1.0F) {
            poseStack.scale(this.scale, this.scale, this.scale);
        }
    }

    @Override
    public Identifier getTextureLocation(S state) {
        return this.texture.apply(state);
    }
}

package projectzulu.client.entity;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.AgeableMobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import projectzulu.ProjectZulu;
import projectzulu.entity.MammothEntity;
import projectzulu.entity.ZuluBigAnimal;

import java.util.function.Function;

/**
 * RenderGenericLiving (shadow 0.5) for the Batch C big animals. The render state carries what the models need
 * (animTime, entity state) and what picks the texture (snow coat, saddle).
 */
public class BigAnimalRenderer<T extends ZuluBigAnimal> extends AgeableMobRenderer<T, BigAnimalRenderer.State, EntityModel<BigAnimalRenderer.State>> {
    /** BiomeGenBase.taiga and taigaHills of 1.5.2 (snowy), as ported by worldgen152. */
    private static final ResourceKey<Biome> TAIGA = ResourceKey.create(Registries.BIOME, Identifier.fromNamespaceAndPath("worldgen152", "taiga"));
    private static final ResourceKey<Biome> TAIGA_HILLS = ResourceKey.create(Registries.BIOME,
            Identifier.fromNamespaceAndPath("worldgen152", "taiga_hills"));

    private final Function<State, Identifier> texture;

    public static class State extends LivingEntityRenderState {
        public int animTime;
        public int entityState;
        public boolean snowy;
        public boolean saddled;
    }

    public BigAnimalRenderer(EntityRendererProvider.Context context, EntityModel<State> adult, EntityModel<State> baby,
            Function<State, Identifier> texture) {
        super(context, adult, baby, 0.5F);
        this.texture = texture;
    }

    public BigAnimalRenderer(EntityRendererProvider.Context context, EntityModel<State> adult, EntityModel<State> baby, String texture) {
        this(context, adult, baby, state -> texture(texture));
    }

    static Identifier texture(String name) {
        return Identifier.fromNamespaceAndPath(ProjectZulu.MOD_ID, "textures/entity/" + name + ".png");
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(T entity, State state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.animTime = entity.getAnimTime();
        state.entityState = entity.getEntityState();
        // getTexture of the mammoth and the tree ent: biome at the truncated position (int) posX, (int) posZ
        BlockPos pos = new BlockPos((int) entity.getX(), (int) entity.getY(), (int) entity.getZ());
        var biome = entity.level().getBiome(pos);
        state.snowy = biome.is(TAIGA) || biome.is(TAIGA_HILLS);
        state.saddled = entity instanceof MammothEntity mammoth && mammoth.isSaddled();
    }

    @Override
    public Identifier getTextureLocation(State state) {
        return this.texture.apply(state);
    }
}

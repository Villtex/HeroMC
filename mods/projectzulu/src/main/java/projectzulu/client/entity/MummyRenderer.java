package projectzulu.client.entity;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Mob;

/**
 * RenderLiving with ModelMummy for the mummy, RenderMummyPharaoh for the pharaoh (the same biped, plus the held ankh
 * in the right hand, which the item-in-hand layer draws). Shadow 0.5.
 */
public class MummyRenderer<T extends Mob> extends HumanoidMobRenderer<T, HumanoidRenderState, MummyModel> {
    private final Identifier texture;

    public MummyRenderer(EntityRendererProvider.Context context, String texture) {
        super(context, new MummyModel(context.bakeLayer(MonsterRenderers.MUMMY_LAYER)), 0.5F);
        this.texture = MonsterRenderer.tex(texture);
    }

    @Override
    public HumanoidRenderState createRenderState() {
        return new HumanoidRenderState();
    }

    @Override
    public Identifier getTextureLocation(HumanoidRenderState state) {
        return this.texture;
    }
}

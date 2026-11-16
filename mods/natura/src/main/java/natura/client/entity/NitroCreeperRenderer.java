package natura.client.entity;

import natura.Natura;
import net.minecraft.client.renderer.entity.CreeperRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.CreeperRenderState;
import net.minecraft.resources.Identifier;

/** RenderCreeper with creeperunstable.png. */
public class NitroCreeperRenderer extends CreeperRenderer {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(Natura.MOD_ID, "textures/entity/nitro_creeper.png");

    public NitroCreeperRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public Identifier getTextureLocation(CreeperRenderState state) {
        return TEXTURE;
    }
}

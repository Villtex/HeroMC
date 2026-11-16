package natura.client.entity;

import natura.Natura;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.AbstractBoatRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.BoatRenderState;
import net.minecraft.resources.Identifier;

/**
 * BloodBoatRender, EucalyptusBoatRender, RedwoodBoatRender, WhiteBoatRender: the 1.5.2 boat model with the boat
 * texture of the wood, placed and turned like the boats of 26.3 (no water mask: water shows inside, as in 1.5.2).
 */
public class NaturaBoatRenderer extends AbstractBoatRenderer {
    private final EntityModel<BoatRenderState> model;

    public NaturaBoatRenderer(EntityRendererProvider.Context context, String wood) {
        super(context, Identifier.fromNamespaceAndPath(Natura.MOD_ID, "textures/entity/boat/" + wood + ".png"));
        this.model = new NaturaBoatModel(context.bakeLayer(NaturaEntityRenderers.BOAT_LAYER));
        this.shadowRadius = 0.5F;
    }

    @Override
    protected EntityModel<BoatRenderState> model() {
        return this.model;
    }
}

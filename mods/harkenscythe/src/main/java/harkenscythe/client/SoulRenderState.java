package harkenscythe.client;

import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.resources.Identifier;

public class SoulRenderState extends EntityRenderState {
    /** innerRotation + partial tick. */
    public float time;
    public Identifier texture;
}

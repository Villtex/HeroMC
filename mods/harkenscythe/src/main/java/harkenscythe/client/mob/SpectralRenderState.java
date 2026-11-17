package harkenscythe.client.mob;

import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.DyeColor;

/** What the renderers of the Harken Scythe mobs take from the entity (one state for all of them). */
public class SpectralRenderState extends HumanoidRenderState {
    public Identifier texture;
    public boolean sitting;
    public boolean tame;
    public boolean resting;
    public boolean sheared;
    public boolean powered;
    public boolean screaming;
    public boolean charging;
    public int skin;
    /** chicken wing flap, sheep head height, wolf tail, creeper swelling, ... */
    public float flap;
    public float headEatPos;
    public float headEatAngle;
    public float tailAngle;
    public float shakeHead;
    public float shakeMane;
    public float shakeBody;
    public float shakeTail;
    public float wetShade = 1.0F;
    public float swell;
    public float xBodyRot;
    public float zBodyRot;
    public float tentacleAngle;
    public DyeColor collar = DyeColor.RED;
}

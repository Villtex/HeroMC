package betterdungeons.client.human;

import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.resources.Identifier;

/** What RenderHuman and ModelHuman read from EntityHuman. */
public class HumanRenderState extends HumanoidRenderState {
    public boolean sitting;
    public boolean aiming;
    public boolean defending;
    public boolean twoHanded;
    public boolean gunner;
    public boolean bannerman;
    public boolean captain;
    public boolean speaking;
    /** isSneaking of the human (always for goblins and monkeys). */
    public boolean sneakPose;
    public boolean riding;
    public boolean hasMainHandItem;
    public float sizeModifier = 1.0F;
    public boolean showCape;
    public int capeSprite;
    public int speechSprite;
    /** EntityPirateBoss.isInvisible: no shield, feather or cape either. */
    public boolean bossInvisible;
    public boolean swingInProgress;
    public Identifier texture;
    public final ItemStackRenderState feather = new ItemStackRenderState();
}

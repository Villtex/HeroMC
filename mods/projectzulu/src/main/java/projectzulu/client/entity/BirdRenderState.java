package projectzulu.client.entity;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import projectzulu.entity.AerialAnimal;

/** What the bird models of Project Zulu read from the entity: world time, AI state, grounded and onGround. */
public class BirdRenderState extends LivingEntityRenderState {
    /** worldObj.getWorldTime() of the original (whole ticks, the wings flap in 20 steps per second as before). */
    public float worldTime;
    public AerialAnimal.EntityState entityState = AerialAnimal.EntityState.IDLE;
    public boolean grounded;
    public boolean onGround;
}

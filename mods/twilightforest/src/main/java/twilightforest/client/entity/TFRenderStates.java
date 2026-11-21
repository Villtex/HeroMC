package twilightforest.client.entity;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.entity.state.SheepRenderState;

/** The render states of the Twilight mobs: what the models need beyond LivingEntityRenderState. */
public final class TFRenderStates {
    private TFRenderStates() {
    }

    /** Birds: the wing angle (RenderTFBird.handleRotationFloat), landed or flying, and the colour of the tiny bird. */
    public static class Bird extends LivingEntityRenderState {
        public float wing;
        public boolean landed = true;
        public int type;
    }

    /** Bunny and other mobs with a texture variant. */
    public static class Variant extends LivingEntityRenderState {
        public int type;
    }

    /** The questing ram: the colours it has eaten. */
    public static class QuestRam extends LivingEntityRenderState {
        public int colorFlags;
    }

    /** The bighorn uses the sheep state. */
    public static class Bighorn extends SheepRenderState {
    }

    /** Mobs whose model reads the vertical motion (kobold jaw) or other flags. */
    public static class Flags extends LivingEntityRenderState {
        public boolean jumping;
        public boolean flag;
        public float value;
    }
}

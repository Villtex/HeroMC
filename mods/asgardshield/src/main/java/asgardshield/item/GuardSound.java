package asgardshield.item;

import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

/**
 * The guard sound of a shield or giant sword (asgardShieldSound / zedSwordSound, ASShieldPerks.playGuardSoundShield):
 * "mob.zombie.wood", "dig.stone", "mob.zombie.metal", "mob.zombie.step" and "mob.skeleton.hurt" of 1.5.2.
 */
public enum GuardSound {
    WOOD(SoundEvents.ZOMBIE_ATTACK_WOODEN_DOOR, 0.1F, 1.0F),
    STONE(SoundEvents.STONE_BREAK, 1.0F, 0.5F),
    METAL(SoundEvents.ZOMBIE_ATTACK_IRON_DOOR, 0.1F, 1.0F),
    CRYSTAL(SoundEvents.ZOMBIE_ATTACK_IRON_DOOR, 0.1F, 3.0F),
    FLESH(SoundEvents.ZOMBIE_STEP, 1.0F, 1.0F),
    BONE(SoundEvents.SKELETON_HURT, 0.1F, 0.8F);

    public final SoundEvent sound;
    public final float volume;
    public final float pitch;

    GuardSound(SoundEvent sound, float volume, float pitch) {
        this.sound = sound;
        this.volume = volume;
        this.pitch = pitch;
    }

    GuardSound(Holder<SoundEvent> sound, float volume, float pitch) {
        this(sound.value(), volume, pitch);
    }
}

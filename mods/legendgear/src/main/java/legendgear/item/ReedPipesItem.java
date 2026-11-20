package legendgear.item;

import legendgear.LGFx;
import legendgear.LGSounds;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.level.Level;

/**
 * ItemSimplePipes (reed pipes): held, they play the note the player's pitch picks from a pentatonic scale (sneaking a
 * second scale), a new attack on every change and a sustain sound every 5 pitch units.
 */
public class ReedPipesItem extends Item {
    static final int[] NOTES = {0, 3, 7, 9, 12};
    static final int[] ALT_NOTES = {-1, 2, 5, 8, 11};

    public ReedPipesItem(Properties properties) {
        super(properties);
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.NONE;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000;
    }

    static float notePitch(int note) {
        return (float) Math.pow(2.0, (note - 12) / 12.0);
    }

    static int noteFromSpan(double span, int[] notes) {
        int which = (int) (span * (notes.length - 1) + 0.5);
        return notes[Math.max(0, Math.min(notes.length - 1, which))];
    }

    private static int note(LivingEntity player) {
        double span = (180.0F - (player.getXRot() + 90.0F)) / 180.0F;
        return noteFromSpan(span, player.isShiftKeyDown() ? ALT_NOTES : NOTES) + 1;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!player.isUsingItem()) {
            player.startUsingItem(hand);
            int note = note(player);
            LGFx.sound(player, LGSounds.FLUTEATTACK.get(), 1.0F, notePitch(note));
            player.getPersistentData().putInt("flutenote", note);
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public boolean releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        entity.getPersistentData().putFloat("noteTime", 0.0F);
        return false;
    }

    @Override
    public void onUseTick(Level level, LivingEntity player, ItemStack stack, int count) {
        var data = player.getPersistentData();
        float noteTime = data.getFloatOr("noteTime", 0.0F);
        int note = note(player);
        float pitch = notePitch(note);
        if (note != data.getIntOr("flutenote", 0)) {
            data.putInt("flutenote", note);
            noteTime = 0.0F;
            LGFx.sound(player, LGSounds.FLUTEATTACK.get(), 1.0F, pitch);
        } else {
            noteTime += pitch;
        }
        if (noteTime >= 5.0F) {
            noteTime -= 5.0F;
            LGFx.sound(player, LGSounds.FLUTESUSTAIN.get(), 1.0F, pitch);
        }
        data.putFloat("noteTime", noteTime);
    }
}

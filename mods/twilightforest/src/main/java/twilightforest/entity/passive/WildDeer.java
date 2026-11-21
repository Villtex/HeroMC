package twilightforest.entity.passive;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;
import twilightforest.TFEntities;

/**
 * EntityTFDeer: a cow that makes no sound when idle or walking and cannot be milked, 0.7 x 2.3. Drops 0..2 (+ looting)
 * leather and 1..3 (+ looting) venison, cooked when it burns (loot table).
 */
public class WildDeer extends Cow {
    public WildDeer(EntityType<? extends Cow> type, Level level) {
        super(type, level);
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return null;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (player.getItemInHand(hand).is(Items.BUCKET)) {
            return InteractionResult.PASS;
        }
        return super.mobInteract(player, hand);
    }

    @Override
    public @Nullable Cow getBreedOffspring(ServerLevel level, AgeableMob partner) {
        return TFEntities.WILD_DEER.get().create(level, EntitySpawnReason.BREEDING);
    }
}

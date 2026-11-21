package tconstruct.world;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** OreBerries: the essence berry gives 3 to 6 experience on right-click (and is used up outside creative mode). */
public class OreberryItem extends Item {
    private final boolean essence;

    public OreberryItem(boolean essence, Properties properties) {
        super(properties);
        this.essence = essence;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!this.essence) {
            return InteractionResult.PASS;
        }
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide()) {
            level.addFreshEntity(new ExperienceOrb(level, player.getX(), player.getY() + 1.0, player.getZ(), level.getRandom().nextInt(4) + 3));
        }
        stack.consume(1, player);
        return InteractionResult.SUCCESS;
    }
}

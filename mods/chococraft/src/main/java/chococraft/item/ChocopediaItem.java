package chococraft.item;

import chococraft.ChocoCraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

/** The Chocopedia: right-clicked into the air it opens the book (onChocopediaRightClick); on a chocobo, its page. */
public class ChocopediaItem extends Item {
    public ChocopediaItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide()) {
            ChocoCraft.openChocopedia(null);
        }
        return InteractionResult.SUCCESS;
    }
}

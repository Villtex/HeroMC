package legendgear.item;

import legendgear.LGConfig;
import legendgear.LGFx;
import legendgear.LGItems;
import legendgear.LGSounds;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

/** EmeraldShard (damage 0 shard, 1 piece): a use with 8 in the stack merges them into one piece or one emerald. */
public class EmeraldShardItem extends Item {
    private final boolean piece;

    public EmeraldShardItem(Properties properties, boolean piece) {
        super(properties);
        this.piece = piece;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.getCount() < LGConfig.EMERALD_EXCHANGE_RATE) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        stack.shrink(LGConfig.EMERALD_EXCHANGE_RATE);
        ItemStack merged = this.piece ? new ItemStack(Items.EMERALD) : new ItemStack(LGItems.EMERALD_PIECE.get());
        LGFx.sound(player, this.piece ? LGSounds.MONEYBIG.get() : LGSounds.MONEYMID.get(), 0.5F, 1.0F);
        if (!player.getInventory().add(merged)) {
            player.drop(merged, false, net.minecraft.util.Prediction.SERVER_ONLY);
        }
        return InteractionResult.SUCCESS;
    }
}

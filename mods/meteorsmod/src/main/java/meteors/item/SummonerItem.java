package meteors.item;

import meteors.MeteorType;
import meteors.entity.MeteorSummonerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * ItemSummoner of one damage value: thrown like a snowball ("random.bow" sound); the random one picks the meteor type
 * when thrown (HandlerMeteor.getMeteorType), the others summon their own type ({@code type} = damage - 1).
 */
public class SummonerItem extends Item {
    /** -1 for the random summoner, else the meteor type id. */
    public final int type;

    public SummonerItem(Properties properties, int type) {
        super(properties);
        this.type = type;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARROW_SHOOT, SoundSource.NEUTRAL, 0.5F,
                0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F));
        if (level instanceof ServerLevel serverLevel) {
            boolean random = this.type < 0;
            int metType = random ? MeteorType.random(serverLevel.getRandom()).id() : this.type;
            Projectile.spawnProjectileFromRotation((l, owner, s) -> new MeteorSummonerEntity(l, owner, s, metType, random),
                    serverLevel, stack, player, 0.0F, 1.5F, 1.0F);
        }
        stack.consume(1, player);
        return InteractionResult.SUCCESS;
    }
}

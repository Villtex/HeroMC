package shadwdrgn.soulshards;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Consumer;

/** ItemShard: shows its mob, tier and kills; goes into a soul cage, or eats a spawner of its mob for 200 souls. */
public class SoulShardItem extends Item {
    public SoulShardItem(Properties properties) {
        super(properties);
    }

    public static SoulShards.Soul soul(ItemStack stack) {
        return stack.getOrDefault(SoulShards.SOUL.get(), SoulShards.Soul.EMPTY);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide()) {
            return InteractionResult.PASS;
        }
        ItemStack stack = context.getItemInHand();
        SoulShards.Soul soul = soul(stack);
        EntityType<?> type = HomeDimensions.type(soul.type());
        if (type == null) {
            return InteractionResult.PASS;
        }
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        if (state.is(SoulShards.SOUL_CAGE.get())) {
            int tier = soul.tier();
            if (tier == 0 || tier < 4 && !HomeDimensions.canSpawnIn(type, level.dimension())) {
                return InteractionResult.PASS;
            }
            if (level.getBlockEntity(pos) instanceof SoulCageBlockEntity cage) {
                // the shard replaces the one in the cage (the old one is lost, as in 1.5.2)
                cage.bind(stack.copy(), type, tier, level.hasNeighborSignal(pos));
                stack.setCount(0);
                return InteractionResult.SUCCESS;
            }
        } else if (state.is(Blocks.SPAWNER) && level.getBlockEntity(pos) instanceof SpawnerBlockEntity spawner) {
            Entity shown = spawner.getSpawner().getOrCreateDisplayEntity(level, pos);
            if (shown != null && shown.getType() == type) {
                stack.set(SoulShards.SOUL.get(), new SoulShards.Soul(soul.type(), Math.min(SoulShards.MAX_SOULS, soul.charge() + SoulShards.SPAWNER_SOULS)));
                level.levelEvent(2001, pos, Block.getId(state));
                level.removeBlock(pos, false);
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        SoulShards.Soul soul = soul(stack);
        EntityType<?> type = HomeDimensions.type(soul.type());
        if (type != null) {
            tooltip.accept(type.getDescription());
        }
        tooltip.accept(Component.translatable("item.soulshards.soul_shard.tier", soul.tier()));
        if (soul.charge() > 0) {
            tooltip.accept(Component.translatable("item.soulshards.soul_shard.killed", soul.charge()));
        }
    }

    /** For the JEI page and commands: the registry name of a mob. */
    public static String id(EntityType<?> type) {
        return BuiltInRegistries.ENTITY_TYPE.getKey(type).toString();
    }
}

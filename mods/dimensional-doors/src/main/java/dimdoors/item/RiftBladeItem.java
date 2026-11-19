package dimdoors.item;

import dimdoors.DimDoors;
import dimdoors.block.DimDoorBlock;
import dimdoors.world.DDData.DimData;
import dimdoors.world.DDData.LinkData;
import dimdoors.world.DDWorld;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * ItemRiftBlade: a golden sword (7 damage, 500 uses) that opens transient doors on rifts, jumps at the creature it
 * is pointed at, and, drawn like a bow for more than 12 ticks, tears a rift with a transient door into a new pocket
 * in front of its holder ("Enable Rift Creation with Rift Blade"). It turns doors too (see DimDoorBlock).
 */
public class RiftBladeItem extends Item {
    public RiftBladeItem(Properties properties) {
        super(properties);
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.BOW;
    }

    @Override
    public boolean releaseUsing(ItemStack stack, Level level, LivingEntity entity, int remaining) {
        if (level.isClientSide() || !(entity instanceof Player player)) {
            return false;
        }
        Vec3 look = player.getViewVector(1.0F);
        // posY of a 1.5.2 player was at its eyes
        int x = Mth.floor(player.getX() + look.x * 2.0);
        int y = Mth.floor(player.getY() + 1.62 + look.y * 2.0 + 2.0);
        int z = Mth.floor(player.getZ() + look.z * 2.0);
        int rotation = DDWorld.facing(player);
        BlockPos pos = new BlockPos(x, y, z);
        int dim = DDWorld.dimId(level, pos);
        if (this.getUseDuration(stack, entity) - remaining > 12 && DDItems.canPlace(level, pos)) {
            DimData data = DDWorld.data().dim(dim);
            if (data == null || data.depth == 0) {
                DDWorld.createPocket(new LinkData(dim, 0, x, y, z, x, y, z, true, rotation), true, false);
            }
            level.playSound(null, player, DimDoors.RIFT_DOOR.get(), SoundSource.PLAYERS, 0.6F, 1.0F);
            DimDoorBlock.placeDoorBlock(level, pos.below(), rotation, DimDoors.TRANSIENT_DOOR.get());
        }
        return false;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide()) {
            BlockPos hit = DDItems.target(level, player, 7.0);
            if (hit != null && level.getBlockState(hit).is(DimDoors.RIFT.get()) && DDWorld.link(level, hit) != null
                    && player.mayUseItemAt(hit, Direction.UP, stack) && player.mayUseItemAt(hit.above(), Direction.UP, stack)
                    && DDItems.canPlace(level, hit) && DDItems.canPlace(level, hit.below())) {
                DimDoorBlock.placeDoorBlock(level, hit.below(), DDWorld.placeFacing(player), DimDoors.TRANSIENT_DOOR.get());
                level.playSound(null, player, DimDoors.RIFT_DOOR.get(), SoundSource.PLAYERS, 0.6F, 1.0F);
                stack.hurtAndBreak(10, player, hand);
            }
            List<LivingEntity> list = level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(8.0), e -> e != player);
            AABB area = new AABB(player.getX() - 8, player.getY() - 8, player.getZ() - 8, player.getX() + 8, player.getY() + 8, player.getZ() + 8);
            for (LivingEntity target : list) {
                if (!target.getBoundingBox().intersects(area)) {
                    continue;
                }
                Vec3 look = player.getViewVector(1.0F).normalize();
                Vec3 to = new Vec3(target.getX() - player.getX(), target.getBoundingBox().minY + target.getBbHeight() / 2.0F - player.getEyeY(),
                        target.getZ() - player.getZ());
                double length = to.length();
                double dot = look.dot(to.normalize());
                if (dot + 0.1 > 1.0 - 0.025 / length && player.hasLineOfSight(target)) {
                    teleportToEntity(target, player);
                    break;
                }
            }
        }
        player.startUsingItem(hand);
        return InteractionResult.CONSUME;
    }

    /** teleportToEntity: to 2.5 blocks before the creature, on the ground there or on the first free block. */
    private static void teleportToEntity(LivingEntity target, Player holder) {
        Vec3 d = new Vec3(holder.getX() - target.getX(), holder.getBoundingBox().minY + holder.getBbHeight() / 2.0F - target.getY() + target.getEyeHeight(),
                holder.getZ() - target.getZ());
        double coefficient = (d.length() - 2.5) / d.length();
        d = d.scale(coefficient);
        double x = holder.getX() - d.x;
        double z = holder.getZ() - d.z;
        double y = holder.level().getHeight(Heightmap.Types.MOTION_BLOCKING, Mth.floor(x), Mth.floor(z));
        if (Math.abs(holder.getY() - d.y - y) > 2.0) {
            int by = Mth.floor(holder.getY() - d.y);
            BlockPos.MutableBlockPos at = new BlockPos.MutableBlockPos(Mth.floor(x), by, Mth.floor(z));
            while (!holder.level().getBlockState(at).isAir()) {
                at.move(Direction.UP);
            }
            y = at.getY();
        }
        holder.teleportTo((net.minecraft.server.level.ServerLevel) holder.level(), x, y, z, Set.of(), holder.getYRot(), holder.getXRot(), false);
        holder.level().playSound(null, holder.getX(), holder.getY(), holder.getZ(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    /** onItemUse: on the top of a block under a rift, a transient door into that rift. */
    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (context.getClickedFace() != Direction.UP || context.getPlayer() == null) {
            return InteractionResult.PASS;
        }
        Level level = context.getLevel();
        Player player = context.getPlayer();
        BlockPos pos = context.getClickedPos().above();
        ItemStack stack = context.getItemInHand();
        if (!player.mayUseItemAt(pos, Direction.UP, stack) || !player.mayUseItemAt(pos.above(), Direction.UP, stack)) {
            return InteractionResult.PASS;
        }
        if (!DDItems.canPlace(level, pos) || DDWorld.link(level, pos.above()) == null) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide()) {
            DimDoorBlock.placeDoorBlock(level, pos, DDWorld.placeFacing(player), DimDoors.TRANSIENT_DOOR.get());
            level.playSound(null, player, DimDoors.RIFT_SOUND.get(), SoundSource.PLAYERS, 0.6F, 1.0F);
            stack.hurtAndBreak(10, player, context.getHand());
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        tooltip.accept(Component.literal("Opens a temporary doors,"));
        tooltip.accept(Component.literal("special teleport attack,"));
        tooltip.accept(Component.literal("and rotates existing doors"));
    }
}

package betterdungeons.item;

import betterdungeons.BDFx;
import betterdungeons.api.BDCommandableMob;
import betterdungeons.api.BDEntityIds;
import betterdungeons.api.BDSummonedMob;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** The smaller items of chocolate.mods.BD.items and friendlyMobs.items. */
public final class MiscItems {
    private MiscItems() {
    }

    /** ItemGoldenFeather: a fall of 3 blocks or more ends while it is in the inventory; costs one use (385). */
    public static class GoldenFeather extends Item {
        public GoldenFeather(Properties properties) {
            super(properties);
        }

        @Override
        public void inventoryTick(ItemStack stack, ServerLevel level, Entity owner, @Nullable EquipmentSlot slot) {
            if (owner.fallDistance >= 3.0 && owner instanceof LivingEntity living) {
                stack.hurtAndBreak(1, level, living, item -> { });
                owner.resetFallDistance();
                for (int i = 0; i < 3; i++) {
                    BDFx.particle(level, ParticleTypes.CLOUD, owner.getX(), owner.getY() - 0.4, owner.getZ(), (BDItemUtil.RAND.nextFloat() - 0.5F) / 2.0F,
                            -0.5, (BDItemUtil.RAND.nextFloat() - 0.5F) / 2.0F);
                }
            }
        }
    }

    /**
     * ItemScrollOfEscape: in the overworld, hold use for more than 50 ticks and let go: back to the bed (or the world
     * spawn). One scroll is used up out of creative mode.
     */
    public static class ScrollOfEscape extends Item {
        public ScrollOfEscape(Properties properties) {
            super(properties);
        }

        @Override
        public InteractionResult use(Level level, Player player, InteractionHand hand) {
            if (level.dimension() == Level.OVERWORLD) {
                player.startUsingItem(hand);
                return InteractionResult.CONSUME;
            }
            return InteractionResult.PASS;
        }

        @Override
        public int getUseDuration(ItemStack stack, LivingEntity user) {
            return 72000;
        }

        @Override
        public ItemUseAnimation getUseAnimation(ItemStack stack) {
            return ItemUseAnimation.NONE;
        }

        @Override
        public boolean releaseUsing(ItemStack stack, Level level, LivingEntity entity, int remainingTime) {
            if (this.getUseDuration(stack, entity) - remainingTime > 50 && entity instanceof Player player) {
                if (!player.hasInfiniteMaterials()) {
                    stack.shrink(1);
                }
                if (player instanceof ServerPlayer server) {
                    TeleportTransition target = server.findRespawnPositionAndUseSpawnBlock(false, TeleportTransition.DO_NOTHING);
                    Vec3 pos = target.position();
                    if (target.newLevel() == server.level()) {
                        server.teleportTo(pos.x, pos.y + 0.5, pos.z);
                    }
                }
                return true;
            }
            return false;
        }
    }

    /** ItemCursedBone: summons an undead (level 2) three blocks ahead; eight uses. */
    public static class CursedBone extends Item {
        public CursedBone(Properties properties) {
            super(properties);
        }

        @Override
        public InteractionResult use(Level level, Player player, InteractionHand hand) {
            ItemStack stack = player.getItemInHand(hand);
            EntityType<?> type = BDEntityIds.type("SummonedUndead");
            if (level instanceof ServerLevel server && type != null) {
                Entity e = type.create(server, EntitySpawnReason.MOB_SUMMONED);
                if (e != null) {
                    if (e instanceof BDSummonedMob summoned) {
                        summoned.initSummoned(player, 0, 2);
                    }
                    Vec3 dest = player.position().add(player.getLookAngle().scale(3.0));
                    e.setPos(dest.x, dest.y + 1.0, dest.z);
                    server.addFreshEntity(e);
                    stack.hurtAndBreak(1, player, hand);
                }
            }
            return InteractionResult.SUCCESS;
        }
    }

    /**
     * ItemPickaxeMagic ("Super Tool! (Creative only)"): mines everything but bedrock (15 on stone and metal, else 8),
     * 10000 damage; use on a block breaks the 3 x 3 face around it with drops, one use per block.
     */
    public static class MagicPickaxe extends Item {
        public MagicPickaxe(Properties properties) {
            super(properties);
        }

        @Override
        public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
            return !state.is(Blocks.BEDROCK);
        }

        @Override
        public InteractionResult useOn(UseOnContext context) {
            Level level = context.getLevel();
            BlockPos pos = context.getClickedPos();
            Direction side = context.getClickedFace();
            Player player = context.getPlayer();
            for (int t = -1; t < 2; t++) {
                for (int v = -1; v < 2; v++) {
                    BlockPos p = switch (side.getAxis()) {
                        case Y -> pos.offset(t, 0, v);
                        case Z -> pos.offset(t, v, 0);
                        case X -> pos.offset(0, t, v);
                    };
                    this.destroyAndDrop(level, p, context.getItemInHand(), player);
                }
            }
            return InteractionResult.SUCCESS;
        }

        private void destroyAndDrop(Level level, BlockPos pos, ItemStack stack, @Nullable Player player) {
            BlockState state = level.getBlockState(pos);
            if (state.isAir() || state.is(Blocks.BEDROCK)) {
                return;
            }
            if (level instanceof ServerLevel server) {
                for (int c = 0; c < 8; c++) {
                    server.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, state), pos.getX() + (BDItemUtil.RAND.nextFloat() - 0.5),
                            pos.getY() + BDItemUtil.RAND.nextFloat() - 0.5F, pos.getZ() + BDItemUtil.RAND.nextFloat() - 0.5, 0, 0.0, 0.0, 0.0, 1.0);
                }
                if (player != null) {
                    stack.hurtAndBreak(1, player, InteractionHand.MAIN_HAND);
                }
                Block.dropResources(state, level, pos, level.getBlockEntity(pos), player, stack);
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
            }
        }
    }

    /**
     * ItemController (mercenary controller): left click a mercenary to assign it (BDEvents), right click a block to send
     * it there, a living entity to attack it. Sneak + use opened GuiMobControl in 1.5.2 (part of the mob port).
     */
    public static class Controller extends Item {
        public Controller(Properties properties) {
            super(properties);
        }

        static @Nullable Entity assigned(Level level, ItemStack stack) {
            CustomData data = stack.get(DataComponents.CUSTOM_DATA);
            int id = data == null ? 0 : data.copyTag().getIntOr("EntityID", 0);
            return id == 0 ? null : level.getEntity(id);
        }

        /** onLeftClickEntity / itemInteractionForEntity: remember the mob. */
        public static boolean assign(ItemStack stack, Player player, Entity entity, boolean leftClick) {
            if (!(entity instanceof BDCommandableMob mob) || leftClick && !mob.isMercenary()) {
                return false;
            }
            CompoundTag tag = new CompoundTag();
            tag.putInt("EntityID", entity.getId());
            tag.putString("name", entity.getName().getString());
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
            stack.set(DataComponents.ITEM_NAME, entity.getName());
            stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
            if (leftClick && !player.level().isClientSide()) {
                player.sendSystemMessage(Component.translatable("item.betterdungeons.controller.assigned", entity.getName()));
            }
            return true;
        }

        @Override
        public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
            return assign(stack, player, entity, true);
        }

        @Override
        public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
            return assign(player.getItemInHand(hand), player, target, false) ? InteractionResult.SUCCESS : InteractionResult.PASS;
        }

        @Override
        public InteractionResult use(Level level, Player player, InteractionHand hand) {
            ItemStack stack = player.getItemInHand(hand);
            if (!stack.has(DataComponents.CUSTOM_DATA) || level.isClientSide()) {
                return InteractionResult.PASS;
            }
            Entity e = assigned(level, stack);
            if (e instanceof BDCommandableMob human) {
                if (!player.isShiftKeyDown()) {
                    HitResult hit = lookAt(player, 50.0);
                    if (hit instanceof BlockHitResult block && hit.getType() == HitResult.Type.BLOCK) {
                        human.setStandingPosition(block.getBlockPos(), player.getYRot());
                    } else if (hit instanceof net.minecraft.world.phys.EntityHitResult entityHit && entityHit.getEntity() instanceof LivingEntity target) {
                        human.setCommandedTarget(target);
                    }
                }
            } else {
                stack.remove(DataComponents.CUSTOM_DATA);
                stack.remove(DataComponents.ENCHANTMENT_GLINT_OVERRIDE);
                stack.set(DataComponents.ITEM_NAME, stack.getItem().components().get(DataComponents.ITEM_NAME));
            }
            return InteractionResult.SUCCESS;
        }
    }

    /**
     * ItemPathMarker: use on blocks to note up to 16 points (each at most 16 blocks from the last), sneak + use shows
     * them; left click a mercenary to give it the path (two points or more).
     */
    public static class PathMarker extends Item {
        public PathMarker(Properties properties) {
            super(properties);
        }

        @Override
        public InteractionResult use(Level level, Player player, InteractionHand hand) {
            ItemStack stack = player.getItemInHand(hand);
            CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
            if (player.isShiftKeyDown() && stack.has(DataComponents.CUSTOM_DATA)) {
                if (level instanceof ServerLevel server) {
                    int points = tag.getIntOr("pos", 0);
                    for (int p = 0; p < points; p++) {
                        server.sendParticles(ParticleTypes.HAPPY_VILLAGER, tag.getIntOr("x" + p, 0) + 0.5, tag.getIntOr("y" + p, 0) + 1.5,
                                tag.getIntOr("z" + p, 0) + 0.5, 5, 0.2, 0.2, 0.2, 0.0);
                    }
                }
                return InteractionResult.SUCCESS;
            }
            HitResult hit = lookAt(player, 50.0);
            if (hit instanceof BlockHitResult block && hit.getType() == HitResult.Type.BLOCK && !level.isClientSide()) {
                int point = tag.getIntOr("pos", 0);
                if (point >= 16) {
                    player.sendSystemMessage(Component.translatable("item.betterdungeons.path_marker.full"));
                    return InteractionResult.SUCCESS;
                }
                BlockPos pos = block.getBlockPos();
                if (point > 0) {
                    BlockPos last = new BlockPos(tag.getIntOr("x" + (point - 1), 0), tag.getIntOr("y" + (point - 1), 0), tag.getIntOr("z" + (point - 1), 0));
                    if (Math.sqrt(last.distSqr(pos)) > 16.0) {
                        player.sendSystemMessage(Component.translatable("item.betterdungeons.path_marker.too_far"));
                        return InteractionResult.SUCCESS;
                    }
                }
                tag.putInt("x" + point, pos.getX());
                tag.putInt("y" + point, pos.getY());
                tag.putInt("z" + point, pos.getZ());
                tag.putInt("pos", point + 1);
                stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
                stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
                ((ServerLevel) level).sendParticles(ParticleTypes.HAPPY_VILLAGER, pos.getX() + 0.5, pos.getY() + 1.5, pos.getZ() + 0.5, 5, 0.2, 0.2, 0.2, 0.0);
            }
            return InteractionResult.SUCCESS;
        }

        @Override
        public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
            return givePath(stack, player, entity);
        }

        /** onLeftClickEntity: a mercenary walks the noted path. */
        public static boolean givePath(ItemStack stack, Player player, Entity entity) {
            if (!(entity instanceof BDCommandableMob human) || !human.isMercenary() || !stack.has(DataComponents.CUSTOM_DATA)) {
                return false;
            }
            CompoundTag tag = stack.get(DataComponents.CUSTOM_DATA).copyTag();
            int points = tag.getIntOr("pos", 0);
            if (points < 2) {
                return false;
            }
            List<Vec3> path = new ArrayList<>();
            for (int p = 0; p < points; p++) {
                path.add(new Vec3(tag.getIntOr("x" + p, 0) + 0.5, tag.getIntOr("y" + p, 0), tag.getIntOr("z" + p, 0) + 0.5));
            }
            human.setPath(path);
            if (!player.level().isClientSide()) {
                player.sendSystemMessage(Component.translatable("item.betterdungeons.path_marker.new_path", entity.getName()));
            }
            return true;
        }
    }

    /** HelperPlayer.getMovingObjectPositionFromPlayer: the entity on the line of sight, else the block. */
    static HitResult lookAt(Player player, double dist) {
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        BlockHitResult block = player.level().clip(new ClipContext(eye, eye.add(look.scale(dist)), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
        double reach = block.getType() == HitResult.Type.MISS ? dist : block.getLocation().distanceTo(eye) - 1.0;
        Vec3 end = eye.add(look.scale(reach));
        AABB box = player.getBoundingBox().expandTowards(look.scale(reach)).inflate(1.0);
        for (Entity e : player.level().getEntities(player, box)) {
            if (e.isPickable()) {
                Optional<Vec3> hit = e.getBoundingBox().inflate(e.getBbWidth(), e.getBbHeight(), e.getBbWidth()).clip(eye, end);
                if (hit.isPresent()) {
                    return new net.minecraft.world.phys.EntityHitResult(e, hit.get());
                }
            }
        }
        return block;
    }
}

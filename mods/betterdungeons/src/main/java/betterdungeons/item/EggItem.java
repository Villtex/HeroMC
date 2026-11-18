package betterdungeons.item;

import betterdungeons.api.BDEntityIds;
import betterdungeons.api.RegisterDungeonMobs;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

/**
 * ItemEggBD: one item per mob (damage 100..116) and boss (500..514) egg. Use spawns the mob where the player looks
 * (80 blocks; on an entity: one block above it). The mob is found by its 1.5.2 name in {@link BDEntityIds}: as long as
 * the mob port has not registered that type the egg does nothing (it says so). Left click a living entity to assign
 * it, then left click another to make the first attack it (ItemEggBD.onLeftClickEntity, all eggs).
 */
public class EggItem extends Item {
    private final String mob;

    public EggItem(Properties properties, String mob) {
        super(properties);
        this.mob = mob;
    }

    /** The 1.5.2 entity name ("ArmoredSkeleton"). */
    public String mob() {
        return this.mob;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        HitResult hit = pick(player, level);
        if (hit.getType() == HitResult.Type.MISS || !(level instanceof ServerLevel server)) {
            return InteractionResult.SUCCESS;
        }
        BlockPos at;
        if (hit instanceof net.minecraft.world.phys.EntityHitResult entityHit) {
            Entity e = entityHit.getEntity();
            at = BlockPos.containing(e.getX(), e.getY() + 1, e.getZ());
        } else {
            BlockHitResult block = (BlockHitResult) hit;
            at = block.getBlockPos().relative(block.getDirection());
        }
        Entity e = this.spawn(server, at);
        if (e == null) {
            player.sendSystemMessage(Component.translatable("item.betterdungeons.egg.missing", Component.translatable(this.getDescriptionId())));
        }
        return InteractionResult.SUCCESS;
    }

    /** onItemUse: the mob at (x, y + 1, z); the goblin boss comes riding an orc (4, 3). */
    private @Nullable Entity spawn(ServerLevel level, BlockPos pos) {
        EntityType<?> type = BDEntityIds.type(this.mob);
        if (type == null) {
            return null;
        }
        Entity e = type.create(level, EntitySpawnReason.SPAWN_ITEM_USE);
        if (e == null) {
            return null;
        }
        e.setPos(pos.getX(), pos.getY() + 1, pos.getZ());
        if (e instanceof Mob mob) {
            mob.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), EntitySpawnReason.SPAWN_ITEM_USE, null);
        }
        if (this.mob.equals("goblinBoss")) {
            Entity orc = RegisterDungeonMobs.create(level, "OrcArmored", 4, 3);
            if (orc != null) {
                orc.setPos(pos.getX(), pos.getY() + 1, pos.getZ());
                level.addFreshEntity(orc);
                level.addFreshEntity(e);
                e.startRiding(orc, true, true);
                return e;
            }
        }
        level.addFreshEntity(e);
        return e;
    }

    /** ItemEggBD.playerPick: blocks up to 80, entities along the first 40 blocks. */
    private static HitResult pick(Player player, Level level) {
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        Vec3 end = eye.add(look.scale(80.0));
        BlockHitResult block = level.clip(new ClipContext(eye, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
        AABB box = player.getBoundingBox().expandTowards(look.scale(40.0)).inflate(1.0);
        for (Entity e : level.getEntities(player, box)) {
            if (e.isPickable()) {
                Optional<Vec3> hit = e.getBoundingBox().inflate(0.3).clip(eye, end);
                if (hit.isPresent()) {
                    return new net.minecraft.world.phys.EntityHitResult(e, hit.get());
                }
            }
        }
        return block;
    }

    /** onLeftClickEntity: true cancels the attack. */
    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data != null) {
            Entity assigned = player.level().getEntity(data.copyTag().getIntOr("EntityID", 0));
            if (assigned instanceof LivingEntity) {
                if (assigned instanceof Mob mob && entity instanceof LivingEntity target) {
                    mob.setTarget(target);
                }
            } else {
                if (!player.level().isClientSide()) {
                    player.sendSystemMessage(Component.translatable("item.betterdungeons.egg.lost"));
                }
                stack.remove(DataComponents.CUSTOM_DATA);
            }
            return true;
        }
        if (entity instanceof LivingEntity) {
            CompoundTag tag = new CompoundTag();
            tag.putInt("EntityID", entity.getId());
            tag.putString("name", entity.getName().getString());
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
            if (!player.level().isClientSide()) {
                player.sendSystemMessage(Component.translatable("item.betterdungeons.egg.assigned", entity.getName()));
                player.sendSystemMessage(Component.translatable("item.betterdungeons.egg.fight"));
            }
            return true;
        }
        return false;
    }
}

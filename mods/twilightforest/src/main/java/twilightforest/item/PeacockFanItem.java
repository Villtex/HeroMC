package twilightforest.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * ItemTFPeacockFan (1024 uses): on the ground it blows what is in the 4-block box 3 blocks ahead away (force 2) and
 * picks plants there 1 in 3 (one use); in the air it boosts the player (motion × 3 sideways, 1.5 up, no fall damage)
 * and gives jump boost for 45 ticks.
 */
public class PeacockFanItem extends Item {
    public PeacockFanItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level instanceof ServerLevel server) {
            if (!player.onGround()) {
                player.addEffect(new MobEffectInstance(MobEffects.JUMP_BOOST, 45, 0));
            } else {
                AABB box = TFItemUtil.effectBox(player, 3.0, 2.0);
                fanBlocks(server, player, box);
                fanEntities(server, player, box);
                TFItemUtil.damage(stack, 1, player);
            }
        } else {
            if (!player.onGround() && !player.hasEffect(MobEffects.JUMP_BOOST)) {
                Vec3 m = player.getDeltaMovement();
                player.setDeltaMovement(m.x * 3.0, 1.5, m.z * 3.0);
                player.fallDistance = 0.0;
            } else {
                AABB box = TFItemUtil.effectBox(player, 3.0, 2.0);
                Vec3 look = player.getLookAngle();
                for (int i = 0; i < 30; i++) {
                    level.addParticle(ParticleTypes.CLOUD, box.minX + level.getRandom().nextFloat() * (box.maxX - box.minX),
                            box.minY + level.getRandom().nextFloat() * (box.maxY - box.minY),
                            box.minZ + level.getRandom().nextFloat() * (box.maxZ - box.minZ), look.x, look.y, look.z);
                }
            }
            level.playLocalSound(player.getX() + 0.5, player.getY() + 0.5, player.getZ() + 0.5, SoundEvents.PLAYER_BREATH, player.getSoundSource(),
                    1.0F + level.getRandom().nextFloat(), level.getRandom().nextFloat() * 0.7F + 0.3F, false);
        }
        player.startUsingItem(hand);
        return InteractionResult.CONSUME;
    }

    private static void fanEntities(ServerLevel level, Player player, AABB box) {
        Vec3 move = player.getLookAngle().scale(2.0);
        for (Entity entity : level.getEntities((Entity) null, box, e -> true)) {
            if (entity.isPushable() || entity instanceof ItemEntity) {
                entity.setDeltaMovement(move);
                entity.needsSync = true;
            }
        }
    }

    /** BlockFlower (flowers, grass, saplings, mushrooms, crops): picked 1 in 3. */
    private static void fanBlocks(ServerLevel level, Player player, AABB box) {
        for (int x = Mth.floor(box.minX); x <= Mth.floor(box.maxX); x++) {
            for (int y = Mth.floor(box.minY); y <= Mth.floor(box.maxY); y++) {
                for (int z = Mth.floor(box.minZ); z <= Mth.floor(box.maxZ); z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    BlockState state = level.getBlockState(pos);
                    if (state.getBlock() instanceof VegetationBlock && level.getRandom().nextInt(3) == 0) {
                        level.destroyBlock(pos, true, player);
                    }
                }
            }
        }
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 20;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.BLOCK;
    }
}

package legendgear.item;

import legendgear.LGBlocks;
import legendgear.LGParticles;
import legendgear.LGSounds;
import legendgear.block.MysticShrubBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/**
 * ItemMagicPowder: sprinkled on a block it changes it into its neighbour in the old metadata order (pumpkin and melon,
 * the mushrooms, the flowers, cobblestone and mossy, the stone bricks, sandstone, saplings and wool through their
 * kinds) and lets a cut mystic shrub grow back. On a mob it rejuvenates (babies grow up, adults become babies), dyes
 * sheep at random, charges creepers and gives zombies a pumpkin or melon to wear.
 */
public class MagicPowderItem extends Item {
    private static final List<Block> STONE_BRICKS = List.of(Blocks.STONE_BRICKS, Blocks.MOSSY_STONE_BRICKS, Blocks.CRACKED_STONE_BRICKS,
            Blocks.CHISELED_STONE_BRICKS);
    private static final List<Block> SANDSTONE = List.of(Blocks.SANDSTONE, Blocks.CHISELED_SANDSTONE, Blocks.SMOOTH_SANDSTONE);
    private static final List<Block> SAPLINGS = List.of(Blocks.OAK_SAPLING, Blocks.SPRUCE_SAPLING, Blocks.BIRCH_SAPLING, Blocks.JUNGLE_SAPLING);
    private static final List<Block> WOOL = Blocks.WOOL.asList();

    public MagicPowderItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    private static Block next(List<Block> cycle, Block block) {
        int i = cycle.indexOf(block);
        return i < 0 ? null : cycle.get((i + 1) % cycle.size());
    }

    private static BlockState transform(BlockState state) {
        Block b = state.getBlock();
        if (b == LGBlocks.MYSTIC_SHRUB.get()) {
            return state.getValue(MysticShrubBlock.STAGE) == 2 ? state.setValue(MysticShrubBlock.STAGE, 0) : null;
        }
        if (b == Blocks.PUMPKIN || b == Blocks.CARVED_PUMPKIN) {
            return Blocks.MELON.defaultBlockState();
        }
        if (b == Blocks.MELON) {
            return Blocks.PUMPKIN.defaultBlockState();
        }
        if (b == Blocks.BROWN_MUSHROOM) {
            return Blocks.RED_MUSHROOM.defaultBlockState();
        }
        if (b == Blocks.RED_MUSHROOM) {
            return Blocks.BROWN_MUSHROOM.defaultBlockState();
        }
        if (b == Blocks.POPPY) {
            return Blocks.DANDELION.defaultBlockState();
        }
        if (b == Blocks.DANDELION) {
            return Blocks.POPPY.defaultBlockState();
        }
        if (b == Blocks.COBBLESTONE) {
            return Blocks.MOSSY_COBBLESTONE.defaultBlockState();
        }
        if (b == Blocks.MOSSY_COBBLESTONE) {
            return Blocks.COBBLESTONE.defaultBlockState();
        }
        for (List<Block> cycle : List.of(STONE_BRICKS, SANDSTONE, SAPLINGS, WOOL)) {
            Block n = next(cycle, b);
            if (n != null) {
                return n.defaultBlockState();
            }
        }
        return null;
    }

    static void sparkle(Level level, double x, double y, double z) {
        RandomSource r = level.getRandom();
        LGParticlesHelper.explode(level, x, y, z);
        for (int i = 0; i < 20; i++) {
            LGParticles.sparkle(level, x + r.nextGaussian() * 0.5, y + r.nextGaussian() * 0.5, z + r.nextGaussian() * 0.5, 0.0, -0.02, 0.0, 1.0F);
        }
    }

    private static void sounds(Level level, double x, double y, double z) {
        level.playSound(null, x, y, z, LGSounds.SPRINKLE.get(), SoundSource.NEUTRAL, 0.2F, 1.0F);
        level.playSound(null, x, y, z, LGSounds.TRANSFORM.get(), SoundSource.NEUTRAL, 1.0F, 0.7F + level.getRandom().nextFloat() * 0.5F);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState changed = transform(level.getBlockState(pos));
        if (changed == null) {
            return InteractionResult.PASS;
        }
        if (level instanceof ServerLevel server) {
            level.setBlock(pos, changed, 2);
            sparkle(server, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
            sounds(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
            if (context.getPlayer() == null || !context.getPlayer().getAbilities().instabuild) {
                context.getItemInHand().shrink(1);
            }
        }
        return InteractionResult.SUCCESS;
    }

    /** handleSprinkleOnEntity (EntityInteractEvent). */
    public static boolean sprinkle(Player player, ItemStack item, Entity target) {
        boolean success = false;
        Level level = target.level();
        if (target instanceof AgeableMob mob) {
            mob.setAge(mob.isBaby() ? 0 : -6000);
            success = true;
        }
        if (target instanceof Sheep sheep) {
            sheep.setColor(DyeColor.byId((int) (Math.random() * 16.0)));
            success = true;
        }
        if (target instanceof Creeper creeper) {
            CreeperAccess.power(creeper);
            success = true;
        }
        if (target instanceof Zombie zombie) {
            var data = zombie.getPersistentData();
            if (zombie.getItemBySlot(EquipmentSlot.HEAD).isEmpty()) {
                zombie.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Blocks.CARVED_PUMPKIN));
                data.putBoolean("pumpkined", true);
                success = true;
            } else if (data.getBooleanOr("pumpkined", false) && !data.getBooleanOr("meloned", false)) {
                zombie.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Blocks.MELON));
                data.putBoolean("meloned", true);
                data.putBoolean("pumpkined", false);
                success = true;
            } else if (data.getBooleanOr("meloned", false) && !data.getBooleanOr("pumpkined", false)) {
                zombie.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Blocks.CARVED_PUMPKIN));
                data.putBoolean("meloned", false);
                data.putBoolean("pumpkined", true);
                success = true;
            }
        }
        if (success) {
            sparkle(level, target.getX(), target.getY() + target.getBbHeight() / 2.0F, target.getZ());
            sounds(level, target.getX(), target.getY(), target.getZ());
            if (!player.getAbilities().instabuild) {
                item.shrink(1);
            }
        }
        return success;
    }

    /** largeexplode at the spot. */
    static final class LGParticlesHelper {
        static void explode(Level level, double x, double y, double z) {
            legendgear.LGFx.particle(level, ParticleTypes.EXPLOSION, x, y, z, 0.0, 0.0, 0.0);
        }
    }
}

package harkenscythe.item;

import harkenscythe.HSBlocks;
import harkenscythe.HSItems;
import harkenscythe.block.BiomassPlantBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * ItemHSScythe: charged, it strikes everything within sqrt(15) blocks and reaps the souls of what it kills (the Soul
 * Reaper one more). Right-clicked on sugar cane, wheat or a biomass plant it harvests it into the player's hands:
 * up to three canes, two wheat (ripe) and a seed, two biomass (ripe) and a seed; three durability per crop.
 */
public class ScytheItem extends ReapingWeaponItem {
    public ScytheItem(ToolMaterial material, Properties properties) {
        super(material, 0.1F, properties);
    }

    public static Item.Properties properties(Item.Properties p, ToolMaterial material, boolean soulReaper) {
        weaponProperties(p, material);
        return soulReaper ? p.rarity(Rarity.EPIC) : p;
    }

    public boolean isSoulReaper() {
        return this == HSItems.SOUL_REAPER.get();
    }

    @Override
    protected double reachSqr() {
        return 15.0;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        ItemStack stack = context.getItemInHand();
        if (player == null || !player.mayUseItemAt(pos, context.getClickedFace(), stack)) {
            return InteractionResult.PASS;
        }
        BlockState state = level.getBlockState(pos);
        boolean reed = state.is(Blocks.SUGAR_CANE);
        boolean wheat = state.is(Blocks.WHEAT);
        boolean biomass = state.is(HSBlocks.BIOMASS_PLANT.get());
        if (!(reed && context.getClickedFace() != Direction.DOWN) && !wheat && !biomass) {
            return InteractionResult.PASS;
        }
        int yield = 1;
        if (level.getBlockState(pos.above()).is(state.getBlock())) {
            yield = 2;
        }
        if (level.getBlockState(pos.above(2)).is(state.getBlock())) {
            yield = 3;
        }
        if (!level.isClientSide()) {
            if (reed) {
                player.drop(new ItemStack(Items.SUGAR_CANE, yield), false, net.minecraft.util.Prediction.SERVER_ONLY);
            }
            if (wheat) {
                if (state.getValue(CropBlock.AGE) == 7) {
                    player.drop(new ItemStack(Items.WHEAT, 2), false, net.minecraft.util.Prediction.SERVER_ONLY);
                }
                player.drop(new ItemStack(Items.WHEAT_SEEDS), false, net.minecraft.util.Prediction.SERVER_ONLY);
            }
            if (biomass) {
                if (state.getValue(BiomassPlantBlock.AGE) == 3) {
                    player.drop(new ItemStack(HSItems.BIOMASS.get(), 2), false, net.minecraft.util.Prediction.SERVER_ONLY);
                }
                player.drop(new ItemStack(HSItems.BIOMASS_SEED.get()), false, net.minecraft.util.Prediction.SERVER_ONLY);
            }
        } else {
            for (int i = 0; i < 10; i++) {
                Vec3 speed = new Vec3((level.getRandom().nextFloat() - 0.5) * 0.1, Math.random() * 0.1 + 0.1, 0.0)
                        .xRot(-player.getXRot() * (float) (Math.PI / 180.0)).yRot(-player.getYRot() * (float) (Math.PI / 180.0));
                level.addParticle(new ItemParticleOption(ParticleTypes.ITEM, Items.WHEAT), pos.getX() + 0.5, pos.getY() + 0.5,
                        pos.getZ() + 0.5, speed.x, speed.y + 0.05, speed.z);
            }
        }
        player.swing(context.getHand(), net.minecraft.world.item.component.SwingAnimation.DEFAULT, true);
        SoundType sound = SoundType.GRASS;
        level.playSound(player, pos, sound.getStepSound(), SoundSource.BLOCKS, (sound.getVolume() + 1.0F) / 2.0F, sound.getPitch() * 0.8F);
        if (!level.isClientSide()) {
            level.removeBlock(pos, false);
            stack.hurtAndBreak(yield * 3, player, context.getHand());
        }
        return InteractionResult.SUCCESS;
    }
}

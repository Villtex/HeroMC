package harkenscythe.item;

import harkenscythe.block.CreepBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * ItemHSGlaive: charged, it strikes everything within five blocks in a narrow cone ahead, and each hit spills blood
 * (more with a better glaive: {@link #bloodPoolChance}). Right-clicked on top of creep it tills it.
 */
public class GlaiveItem extends ReapingWeaponItem {
    private final int bloodPoolChance;

    public GlaiveItem(ToolMaterial material, int bloodPoolChance, Properties properties) {
        super(material, 0.2F, properties);
        this.bloodPoolChance = bloodPoolChance;
    }

    public static Item.Properties properties(Item.Properties p, ToolMaterial material) {
        return weaponProperties(p, material);
    }

    public int bloodPoolChance() {
        return this.bloodPoolChance;
    }

    @Override
    protected double reachSqr() {
        return 25.0;
    }

    @Override
    protected boolean inReach(Player player, LivingEntity target) {
        Vec3 look = player.getViewVector(1.0F).normalize();
        Vec3 to = new Vec3(target.getX() - player.getX(), target.getBoundingBox().minY + target.getBbHeight() / 2.0F - player.getEyeY(),
                target.getZ() - player.getZ());
        double distance = to.length();
        return look.dot(to.normalize()) > 0.9 - 0.025 / distance;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (context.getClickedFace() != Direction.UP) {
            return InteractionResult.PASS;
        }
        Player player = context.getPlayer();
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        ItemStack stack = context.getItemInHand();
        if (player == null || !player.mayUseItemAt(pos, Direction.UP, stack)) {
            return InteractionResult.PASS;
        }
        BlockState state = level.getBlockState(pos);
        if (!CreepBlock.isCreep(state) || state.getValue(CreepBlock.SOIL) != 0) {
            return InteractionResult.PASS;
        }
        SoundType sound = SoundType.GRAVEL;
        level.playSound(player, pos, sound.getStepSound(), SoundSource.BLOCKS, (sound.getVolume() + 1.0F) / 2.0F, sound.getPitch() * 0.8F);
        if (!level.isClientSide()) {
            level.setBlock(pos, state.setValue(CreepBlock.SOIL, 1), Block.UPDATE_CLIENTS);
            stack.hurtAndBreak(1, player, context.getHand());
        }
        return InteractionResult.SUCCESS;
    }
}

package twilightforest.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import twilightforest.TFItems;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * ItemTFFieryPick: what the pick mines drops smelted (with the smelting experience, fractions by chance), what it hits
 * catches fire for 15 seconds unless fire immune, and it harvests obsidian. 1.5.2 spawned the smelted item and
 * suppressed the normal drop; here the drops of the block are replaced (BlockDropsEvent).
 */
public class FieryPickItem extends Item {
    public FieryPickItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
        return state.is(Blocks.OBSIDIAN) || super.isCorrectToolForDrops(stack, state);
    }

    @Override
    public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (!target.fireImmune() && target.level() instanceof ServerLevel level) {
            target.igniteForSeconds(15.0F);
            for (int i = 0; i < 20; i++) {
                double vx = level.getRandom().nextGaussian() * 0.02;
                double vy = level.getRandom().nextGaussian() * 0.02;
                double vz = level.getRandom().nextGaussian() * 0.02;
                level.sendParticles(ParticleTypes.FLAME,
                        target.getX() + level.getRandom().nextFloat() * target.getBbWidth() * 2.0F - target.getBbWidth() - vx * 10.0,
                        target.getY() + level.getRandom().nextFloat() * target.getBbHeight() - vy * 10.0,
                        target.getZ() + level.getRandom().nextFloat() * target.getBbWidth() * 2.0F - target.getBbWidth() - vz * 10.0,
                        0, vx, vy, vz, 1.0);
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        tooltip.accept(Component.translatable("item.twilightforest.fiery_pickaxe.tooltip").withStyle(ChatFormatting.GRAY));
    }

    /** onBlockDestroyed: every drop with a furnace recipe is replaced by its result, the experience is dropped as orbs. */
    public static void onBlockDrops(BlockDropsEvent event) {
        if (!event.getTool().is(TFItems.FIERY_PICKAXE.get()) || event.getBreaker() == null) {
            return;
        }
        ServerLevel level = event.getLevel();
        double x = event.getPos().getX() + 0.5;
        double y = event.getPos().getY() + 0.5;
        double z = event.getPos().getZ() + 0.5;
        for (ItemEntity drop : event.getDrops()) {
            ItemStack input = drop.getItem();
            Optional<RecipeHolder<SmeltingRecipe>> recipe = level.recipeAccess().getRecipeFor(RecipeType.SMELTING, new SingleRecipeInput(input), level);
            if (recipe.isEmpty()) {
                continue;
            }
            ItemStack result = recipe.get().value().assemble(new SingleRecipeInput(input));
            result.setCount(result.getCount() * input.getCount());
            drop.setItem(result);
            float floatXP = recipe.get().value().experience() * input.getCount();
            int xp = Mth.floor(floatXP);
            if (floatXP > xp && level.getRandom().nextFloat() < floatXP - xp) {
                xp++;
            }
            if (xp > 0) {
                ExperienceOrb.award(level, new net.minecraft.world.phys.Vec3(x, y, z), xp);
            }
            for (int i = 0; i < 5; i++) {
                double rx = level.getRandom().nextGaussian() * 0.02;
                double ry = level.getRandom().nextGaussian() * 0.02;
                double rz = level.getRandom().nextGaussian() * 0.02;
                level.sendParticles(ParticleTypes.FLAME, x + rx * 20.0, y + ry * 20.0, z + rz * 20.0, 0, -rx, -ry, -rz, 1.0);
            }
        }
    }
}

package tconstruct.items;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownSplashPotion;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ChargedProjectiles;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.function.Consumer;

/**
 * PotionLauncher (a test item of 1.5.2, creative only): held for 1.5 s it loads the first potion of the inventory
 * (with a clank; the sound "launcher_clank" is not in the archive, the crossbow loading sound stands in); the next swing launches it as a splash potion, twice as fast and flatter (-10 degrees) as a thrown one.
 * The loaded potion is kept as the charged projectile of the item.
 */
public class PotionLauncherItem extends Item {
    public PotionLauncherItem(Properties properties) {
        super(properties);
    }

    private static boolean loaded(ItemStack stack) {
        ChargedProjectiles loaded = stack.get(DataComponents.CHARGED_PROJECTILES);
        return loaded != null && !loaded.isEmpty();
    }

    /** getInventorySlotContainItem(Item.potion): the first potion (drinkable or splash) of the main inventory. */
    private static int potionSlot(Player player) {
        List<ItemStack> items = player.getInventory().getNonEquipmentItems();
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).is(Items.POTION) || items.get(i).is(Items.SPLASH_POTION)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!loaded(stack) && potionSlot(player) >= 0) {
            player.startUsingItem(hand);
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 30;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return loaded(stack) ? ItemUseAnimation.NONE : ItemUseAnimation.BOW;
    }

    /** onEaten: load. */
    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (entity instanceof Player player && !loaded(stack)) {
            int slot = potionSlot(player);
            if (slot >= 0) {
                ItemStack potion = player.getInventory().getNonEquipmentItems().get(slot);
                stack.set(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.ofNonEmpty(List.of(potion.copyWithCount(1))));
                if (!player.getAbilities().instabuild) {
                    potion.shrink(1);
                }
                level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.CROSSBOW_LOADING_END.value(), SoundSource.PLAYERS, 1.0F,
                        (level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.15F + 1.0F);
            }
        }
        return stack;
    }

    /** onEntitySwing: launch the loaded potion. */
    @Override
    public boolean onEntitySwing(ItemStack stack, LivingEntity entity, InteractionHand hand) {
        ChargedProjectiles loaded = stack.get(DataComponents.CHARGED_PROJECTILES);
        if (loaded == null || loaded.isEmpty()) {
            return false;
        }
        Level level = entity.level();
        level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 0.5F,
                0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F));
        if (level instanceof ServerLevel server) {
            ItemStack potion = loaded.itemCopies().findFirst().orElse(ItemStack.EMPTY).transmuteCopy(Items.SPLASH_POTION, 1);
            ThrownSplashPotion thrown = new ThrownSplashPotion(server, entity, potion);
            thrown.shootFromRotation(entity, entity.getXRot(), entity.getYRot(), -10.0F, 1.0F, 1.0F);
            server.addFreshEntity(thrown);
        }
        stack.remove(DataComponents.CHARGED_PROJECTILES);
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        tooltip.accept(Component.literal("Test Item"));
    }
}

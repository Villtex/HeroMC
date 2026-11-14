package shadwdrgn.soulshards;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/** CommonProxy.livingDeath (souls), the soul rune of ClientProxy/PacketHandler and the drops of caged blazes. */
public final class SoulEvents {
    private SoulEvents() {
    }

    @SubscribeEvent
    public static void livingDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (!HomeDimensions.accepts(entity.getType()) || !(event.getSource().getEntity() instanceof ServerPlayer player)
                || entity.getPersistentData().getBooleanOr(SoulCageBlockEntity.CAGED, false)) {
            return;
        }
        int souls = 1 + level(player.level(), SoulShards.SOUL_STEALER, player.getMainHandItem());
        String type = SoulShardItem.id(entity.getType());
        Inventory inventory = player.getInventory();
        // hotbar and the first slot after it (slots 0 to 9), the first blank or matching shard takes the soul
        for (int i = 0; i < 10; i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack.isEmpty()) {
                continue;
            }
            if (stack.is(SoulShards.BLANK_SOUL_SHARD.get())) {
                stack.shrink(1);
                ItemStack shard = new ItemStack(SoulShards.SOUL_SHARD.get());
                shard.set(SoulShards.SOUL.get(), new SoulShards.Soul(type, 0));
                BlockPos at = player.blockPosition();
                player.level().addFreshEntity(new ItemEntity(player.level(), at.getX(), at.getY(), at.getZ(), shard));
                return;
            }
            if (stack.is(SoulShards.SOUL_SHARD.get())) {
                SoulShards.Soul soul = SoulShardItem.soul(stack);
                if (soul.type().isEmpty() || soul.type().equals(type)) {
                    int charge = soul.charge() >= SoulShards.MAX_SOULS ? SoulShards.MAX_SOULS : soul.charge() + souls;
                    stack.set(SoulShards.SOUL.get(), new SoulShards.Soul(type, charge));
                    return;
                }
            }
        }
    }

    private static int level(Level level, net.minecraft.resources.ResourceKey<Enchantment> key, ItemStack stack) {
        return level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).get(key)
                .map(holder -> EnchantmentHelper.getItemEnchantmentLevel(holder, stack)).orElse(0);
    }

    /** Right-click on glowstone with a diamond, in the middle of the soul rune: end stone cross, one blank shard. */
    @SubscribeEvent
    public static void soulRune(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        if (event.getHand() != InteractionHand.MAIN_HAND || !player.getMainHandItem().is(Items.DIAMOND)
                || !level.getBlockState(pos).is(Blocks.GLOWSTONE) || !isSoulRune(level, pos)) {
            return;
        }
        if (!level.isClientSide()) {
            BlockState endStone = Blocks.END_STONE.defaultBlockState();
            for (BlockPos p : new BlockPos[] {pos, pos.east(), pos.west(), pos.south(), pos.north()}) {
                level.setBlockAndUpdate(p, endStone);
            }
            player.getMainHandItem().shrink(1);
            level.addFreshEntity(new ItemEntity(level, pos.getX(), pos.getY() + 1, pos.getZ(), new ItemStack(SoulShards.BLANK_SOUL_SHARD.get())));
        }
        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);
    }

    private static boolean isSoulRune(Level level, BlockPos pos) {
        int[][] endStone = {{2, 0}, {-2, 0}, {0, 2}, {0, -2}, {1, 1}, {1, -1}, {-1, 1}, {-1, -1}};
        for (int[] o : endStone) {
            if (!level.getBlockState(pos.offset(o[0], 0, o[1])).is(Blocks.END_STONE)) {
                return false;
            }
        }
        int[][] netherrack = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        for (int[] o : netherrack) {
            if (!level.getBlockState(pos.offset(o[0], 0, o[1])).is(Blocks.NETHERRACK)) {
                return false;
            }
        }
        return true;
    }

    /** EntitySpawnedBlaze: 0 to 1 + looting blaze rods, whoever killed it. */
    @SubscribeEvent
    public static void drops(LivingDropsEvent event) {
        LivingEntity entity = event.getEntity();
        if (!(entity instanceof Blaze) || !entity.getPersistentData().getBooleanOr(SoulCageBlockEntity.CAGED, false)) {
            return;
        }
        event.getDrops().removeIf(item -> item.getItem().is(Items.BLAZE_ROD));
        int looting = event.getSource().getEntity() instanceof LivingEntity killer
                ? level(entity.level(), Enchantments.LOOTING, killer.getMainHandItem()) : 0;
        int rods = entity.getRandom().nextInt(2 + looting);
        for (int i = 0; i < rods; i++) {
            event.getDrops().add(new ItemEntity(entity.level(), entity.getX(), entity.getY(), entity.getZ(), new ItemStack(Items.BLAZE_ROD)));
        }
    }

}

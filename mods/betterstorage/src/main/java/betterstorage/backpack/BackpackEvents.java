package betterstorage.backpack;

import betterstorage.BS;
import betterstorage.BSUtil;
import betterstorage.block.BackpackBlock;
import betterstorage.entity.Frienderman;
import betterstorage.item.BackpackItem;
import betterstorage.menu.BSMenu;
import hexxit.worldgen152.OldGameIds;
import hexxit.worldgen152.core.populate.OldLoot;
import hexxit.worldgen152.core.populate.OldStack;
import java.util.List;
import java.util.Random;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gamerules.GameRules;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * The backpack part of CommonProxy and ItemBackpack: mobs spawning with backpacks (and endermen as Friendermen),
 * the lid and its sounds, putting a worn backpack down (sneak + right click with an empty hand), opening someone
 * else's from behind, dropping the contents on death, the ender backpack that never stays an item, damageArmor and
 * the chest slot that keeps a full or open backpack on.
 */
public final class BackpackEvents {
    /** randomBackpackItems: item id, damage, min, max, weight (1.5.2 ids, through OldGameIds). */
    private static final int[][] RANDOM_ITEMS = {
            {280, 0, 8, 20, 100}, {5, 0, 2, 10, 100}, {17, 0, 1, 8, 40}, {4, 0, 6, 16, 80}, {270, 50, 1, 1, 35}, {270, 20, 1, 1, 10},
            {274, 120, 1, 1, 10}, {274, 80, 1, 1, 5}, {257, 220, 1, 1, 2}, {268, 40, 1, 1, 30}, {272, 60, 1, 1, 5}, {261, 200, 1, 1, 10},
            {261, 50, 1, 1, 3}, {346, 20, 1, 1, 4}, {345, 0, 1, 1, 6}, {347, 0, 1, 1, 5}, {50, 0, 6, 24, 30}, {262, 0, 2, 12, 10},
            {367, 0, 3, 6, 15}, {352, 0, 2, 5, 20}, {287, 0, 3, 10, 15}, {260, 0, 2, 5, 15}, {297, 0, 2, 4, 10}, {296, 0, 3, 6, 10},
            {391, 0, 1, 2, 8}, {392, 0, 1, 2, 5}, {349, 0, 1, 4, 5}, {350, 0, 1, 2, 4}, {263, 0, 3, 9, 20}, {263, 0, 20, 32, 5},
            {15, 0, 2, 5, 15}, {15, 0, 10, 20, 2}, {14, 0, 2, 7, 8}, {264, 0, 1, 2, 1}, {388, 0, 1, 1, 1}};
    private static final OldLoot.Entry[] RANDOM_BACKPACK_ITEMS = new OldLoot.Entry[RANDOM_ITEMS.length];
    /** ItemBackpack.immuneToDamageType ("catcus" was misspelled there, so cactus does wear it down). */
    private static final List<ResourceKey<DamageType>> IMMUNE = List.of(DamageTypes.IN_WALL, DamageTypes.DROWN, DamageTypes.STARVE,
            DamageTypes.FALL, DamageTypes.FELL_OUT_OF_WORLD, DamageTypes.GENERIC, DamageTypes.WITHER, DamageTypes.FALLING_ANVIL,
            DamageTypes.FALLING_BLOCK, DamageTypes.THROWN, DamageTypes.MAGIC, DamageTypes.INDIRECT_MAGIC, DamageTypes.THORNS);
    private static final Random RANDOM = new Random();

    static {
        for (int i = 0; i < RANDOM_ITEMS.length; i++) {
            int[] r = RANDOM_ITEMS[i];
            RANDOM_BACKPACK_ITEMS[i] = new OldLoot.Entry(new OldStack(r[0], r[1], 1), r[2], r[3], r[4]);
        }
    }

    private BackpackEvents() {
    }

    public static void register(IEventBus modBus, IEventBus bus) {
        bus.addListener(BackpackEvents::onSpecialSpawn);
        bus.addListener(BackpackEvents::onLivingUpdate);
        bus.addListener(BackpackEvents::onLivingDeath);
        bus.addListener(BackpackEvents::onPlayerInteract);
        bus.addListener(BackpackEvents::onRightClickItem);
        bus.addListener(BackpackEvents::onEntityInteract);
        bus.addListener(BackpackEvents::onEntityJoin);
        bus.addListener(BackpackEvents::onDamage);
        bus.addListener(BackpackEvents::onPlayerTick);
    }

    /** onSpecialSpawn: natural spawns only. Zombies 1/800, zombie pigmen 1/1000, skeletons 1/1200; 1/80 endermen. */
    private static void onSpecialSpawn(FinalizeSpawnEvent event) {
        EntitySpawnReason reason = event.getSpawnType();
        if (reason != EntitySpawnReason.NATURAL && reason != EntitySpawnReason.CHUNK_GENERATION) {
            return;
        }
        Mob entity = event.getEntity();
        EntityType<?> type = entity.getType();
        double probability = 0.0;
        // the original compared the exact classes: EntityZombie also covered zombie villagers, EntitySkeleton wither skeletons
        if (type == EntityTypes.ZOMBIE || type == EntityTypes.ZOMBIE_VILLAGER) {
            probability = 0.00125;
        } else if (type == EntityTypes.ZOMBIFIED_PIGLIN) {
            probability = 0.001;
        } else if (type == EntityTypes.SKELETON || type == EntityTypes.WITHER_SKELETON) {
            probability = 8.333333333333334E-4;
        } else if (type == EntityTypes.ENDERMAN && BSUtil.getBoolean(0.0125) && event.getLevel().getLevel().dimension() != Level.END) {
            ServerLevel level = event.getLevel().getLevel();
            Frienderman frienderman = BS.FRIENDERMAN.get().create(level, EntitySpawnReason.NATURAL);
            if (frienderman != null) {
                frienderman.snapTo(entity.getX(), entity.getY(), entity.getZ(), entity.getYRot(), 0.0F);
                Backpacks.data(frienderman).spawnsWithBackpack = true;
                level.addFreshEntity(frienderman);
            }
            event.setSpawnCancelled(true);
            return;
        }
        if (BSUtil.RANDOM.nextDouble() < probability) {
            Backpacks.data(entity).spawnsWithBackpack = true;
        }
    }

    /** onLivingUpdate (server): lid and sounds, spawning with a backpack, contents of a backpack that is gone. */
    private static void onLivingUpdate(EntityTickEvent.Pre event) {
        if (!(event.getEntity() instanceof LivingEntity entity) || !(entity.level() instanceof ServerLevel level)) {
            return;
        }
        ItemStack backpack = Backpacks.getBackpack(entity);
        if (!backpack.isEmpty()) {
            BackpackData data = Backpacks.data(entity);
            data.prevLidAngle = data.lidAngle;
            if (Backpacks.isOpen(entity)) {
                data.lidAngle = Math.min(1.0F, data.lidAngle + 0.2F);
            } else {
                data.lidAngle = Math.max(0.0F, data.lidAngle - 0.2F);
            }
            if (data.lidAngle > 0.0F && data.prevLidAngle <= 0.0F) {
                level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.SNOW_STEP, SoundSource.NEUTRAL, 1.0F, 0.5F);
            }
            if (data.lidAngle < 0.2F && data.prevLidAngle >= 0.2F) {
                level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.SNOW_STEP, SoundSource.NEUTRAL, 0.8F, 0.3F);
            }
            return;
        }
        if (!entity.hasData(BS.BACKPACK_DATA.get())) {
            return;
        }
        BackpackData data = Backpacks.data(entity);
        if (data.spawnsWithBackpack) {
            List<ItemStack> contents = null;
            if (entity instanceof Frienderman) {
                backpack = new ItemStack(BS.ENDER_BACKPACK.get());
                if (entity instanceof Mob mob) {
                    mob.setDropChance(EquipmentSlot.CHEST, 0.0F);
                }
            } else {
                backpack = new ItemStack(BS.BACKPACK.get());
                backpack.setDamageValue(BSUtil.getInt(120, 240));
                if (BSUtil.getBoolean(0.15)) {
                    int r = BSUtil.getInt(32, 224);
                    int g = BSUtil.getInt(32, 224);
                    int b = BSUtil.getInt(32, 224);
                    backpack.set(DataComponents.DYED_COLOR, new DyedItemColor(r << 16 | g << 8 | b));
                }
                contents = randomContents(level, ((BackpackItem) backpack.getItem()).getColumns() * ((BackpackItem) backpack.getItem()).getRows());
                if (entity instanceof Mob mob) {
                    mob.setDropChance(EquipmentSlot.CHEST, 1.0F);
                }
            }
            ItemStack armor = entity.getItemBySlot(EquipmentSlot.CHEST);
            if (armor.isEnchanted()) {
                backpack.set(DataComponents.ENCHANTMENTS, armor.get(DataComponents.ENCHANTMENTS));
            }
            Backpacks.setBackpack(entity, backpack, contents);
            Backpacks.data(entity).spawnsWithBackpack = false;
        } else if (data.contents != null) {
            for (ItemStack stack : data.contents) {
                BSUtil.dropFromEntity(entity, stack, 1.5F);
            }
            data.contents = null;
            Backpacks.updateHasItems(entity);
        }
    }

    /** 20 draws from randomBackpackItems, one time in ten 5 more from the dungeon chest loot. */
    private static List<ItemStack> randomContents(ServerLevel level, int size) {
        OldStack[] slots = new OldStack[size];
        OldLoot.generate(RANDOM, RANDOM_BACKPACK_ITEMS, slots, 20);
        if (RANDOM.nextDouble() < 0.1) {
            OldLoot.generate(RANDOM, OldLoot.DUNGEON_CHEST.items(RANDOM), slots, 5);
        }
        NonNullList<ItemStack> contents = NonNullList.withSize(size, ItemStack.EMPTY);
        for (int i = 0; i < size; i++) {
            if (slots[i] != null) {
                contents.set(i, OldGameIds.stack(slots[i], level.registryAccess()));
            }
        }
        return contents;
    }

    /** onLivingDeath: the contents fall out, unless a player keeps the inventory (then they stay, copyOnDeath). */
    private static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (!(entity.level() instanceof ServerLevel level) || Backpacks.getBackpack(entity).isEmpty() || !entity.hasData(BS.BACKPACK_DATA.get())) {
            return;
        }
        BackpackData data = Backpacks.data(entity);
        if (data.contents == null) {
            return;
        }
        if (entity instanceof Player && level.getGameRules().get(GameRules.KEEP_INVENTORY)) {
            return;
        }
        for (ItemStack stack : data.contents) {
            BSUtil.dropFromEntity(entity, stack, 4.0F);
        }
        data.contents = null;
    }

    /** onPlayerInteract: sneak + right click on a block with an empty hand sets the worn backpack down. */
    private static void onPlayerInteract(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        if (event.getHand() != InteractionHand.MAIN_HAND || !player.getMainHandItem().isEmpty() || !player.isShiftKeyDown()) {
            return;
        }
        ItemStack backpack = Backpacks.getBackpack(player);
        if (backpack.isEmpty() || Backpacks.isOpen(player)) {
            return;
        }
        if (BackpackItem.place(backpack, player, player.level(), event.getPos(), event.getFace() == null ? net.minecraft.core.Direction.UP : event.getFace())) {
            player.swing(InteractionHand.MAIN_HAND);
            event.setCancellationResult(InteractionResult.SUCCESS);
            event.setCanceled(true);
        }
    }

    /** ItemArmor did not swap: a chest piece right-clicked in the air does not take the backpack off. */
    private static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        ItemStack held = event.getItemStack();
        var equippable = held.get(DataComponents.EQUIPPABLE);
        if (equippable != null && equippable.slot() == EquipmentSlot.CHEST && !Backpacks.getBackpack(event.getEntity()).isEmpty()) {
            event.setCancellationResult(InteractionResult.FAIL);
            event.setCanceled(true);
        }
    }

    /** onEntityInteract: someone else's backpack, from behind. */
    private static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !(event.getTarget() instanceof LivingEntity target)
                || event.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }
        ItemStack backpack = Backpacks.getBackpack(target);
        if (backpack.isEmpty()) {
            return;
        }
        BackpackItem type = (BackpackItem) backpack.getItem();
        Container items = Backpacks.getBackpackItems(target, player);
        if (items == null) {
            return;
        }
        EquippedBackpackInventory inventory = new EquippedBackpackInventory(target, items);
        if (inventory.stillValid(player)) {
            Component name = backpack.get(DataComponents.CUSTOM_NAME);
            BSMenu.open(player, name != null ? name : Component.translatable(type.getName()), type.getColumns(), type.getRows(), inventory);
            player.swing(InteractionHand.MAIN_HAND, true);
            event.setCancellationResult(InteractionResult.SUCCESS);
            event.setCanceled(true);
        }
    }

    /** onEntityJoinWorldEvent: an ender backpack item teleports away as a block. */
    private static void onEntityJoin(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof ItemEntity item && item.getItem().is(BS.ENDER_BACKPACK.get()) && event.getLevel() instanceof ServerLevel level
                && !event.loadedFromDisk()) {
            event.setCanceled(true);
            ItemStack stack = item.getItem().copy();
            for (int i = 0; i < 64 && !BackpackBlock.teleportRandomly(level, item.getX(), item.getY(), item.getZ(), i > 48, stack); i++) {
            }
        }
    }

    /** ISpecialArmor.damageArmor: max(1, damage / 4) durability, but not for some causes or when protected. */
    private static void onDamage(LivingIncomingDamageEvent event) {
        LivingEntity entity = event.getEntity();
        ItemStack stack = Backpacks.getBackpack(entity);
        DamageSource source = event.getSource();
        if (stack.isEmpty() || !(entity.level() instanceof ServerLevel) || source.is(DamageTypeTags.BYPASSES_ARMOR)) {
            return;
        }
        if (!takesDamage(entity, stack, source)) {
            return;
        }
        int damage = Math.max(1, (int) (event.getAmount() / 4.0F));
        stack.hurtAndBreak(damage, entity, EquipmentSlot.CHEST);
        if (stack.isEmpty() && entity.hasData(BS.BACKPACK_DATA.get())) {
            BackpackData data = Backpacks.data(entity);
            if (data.contents != null) {
                for (ItemStack s : data.contents) {
                    BSUtil.dropFromEntity(entity, s, 2.0F);
                }
                data.contents = null;
            }
        }
    }

    private static boolean takesDamage(LivingEntity entity, ItemStack stack, DamageSource source) {
        for (ResourceKey<DamageType> immune : IMMUNE) {
            if (source.is(immune)) {
                return false;
            }
        }
        return !protection(entity, stack, Enchantments.PROTECTION, 0.3, 0.35, 0.4, 0.45)
                && (!source.is(DamageTypeTags.IS_PROJECTILE) || !protection(entity, stack, Enchantments.PROJECTILE_PROTECTION, 0.4, 0.5, 0.6, 0.7))
                && (!source.is(DamageTypeTags.IS_FIRE) || !protection(entity, stack, Enchantments.FIRE_PROTECTION, 0.55, 0.65, 0.75, 0.85))
                && (!source.is(DamageTypeTags.IS_EXPLOSION) || !protection(entity, stack, Enchantments.BLAST_PROTECTION, 0.65, 0.75, 0.85, 0.95));
    }

    private static boolean protection(LivingEntity entity, ItemStack stack, ResourceKey<Enchantment> key, double... chance) {
        Holder<Enchantment> enchantment = entity.level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).get(key).orElse(null);
        if (enchantment == null) {
            return false;
        }
        int level = Math.min(EnchantmentHelper.getItemEnchantmentLevel(enchantment, stack) - 1, chance.length - 1);
        return level >= 0 && BSUtil.getBoolean(chance[level]);
    }

    /** onArmorTickUpdate: the chest slot of the player's inventory becomes a {@link BackpackArmorSlot}. */
    private static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (Backpacks.getBackpack(player).isEmpty()) {
            return;
        }
        int index = InventoryMenu.ARMOR_SLOT_START + 1;
        if (!(player.inventoryMenu.slots.get(index) instanceof BackpackArmorSlot)) {
            BackpackArmorSlot slot = new BackpackArmorSlot(player);
            slot.index = index;
            player.inventoryMenu.slots.set(index, slot);
        }
    }

    static boolean isEnderman(LivingEntity e) {
        return e instanceof EnderMan;
    }
}

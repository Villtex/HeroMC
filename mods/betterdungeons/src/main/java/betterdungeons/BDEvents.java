package betterdungeons;

import betterdungeons.item.BDItemUtil;
import betterdungeons.item.BagItem;
import betterdungeons.projectile.BDMagicArrow;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.TriState;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingGetProjectileEvent;
import net.neoforged.neoforge.event.entity.player.ArrowLooseEvent;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * EventHandlerBD (bows shoot from the quiver and magic arrows, picked-up arrows go into the quiver), the armour ticks
 * of ItemArmorBase (turtle, cloud boots, scouter) and the cleric's capsule station trades (VillageHandler).
 */
public final class BDEvents {
    private static final Map<UUID, Boolean> LAST_ON_GROUND_CLIENT = new HashMap<>();
    private static final Map<UUID, Boolean> LAST_ON_GROUND_SERVER = new HashMap<>();
    private static final Map<UUID, ServerBossEvent> SCOUTER_BARS = new HashMap<>();
    private static final Map<UUID, Integer> SCOUTER_TIME = new HashMap<>();

    private BDEvents() {
    }

    // ---------------------------------------------------------------- bows

    static ItemStack quiverWithArrows(Player player) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(BDItems.QUIVER.get()) && BagItem.haveArrows(stack)) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    static ItemStack magicArrow(Player player) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (BDItems.isMagicArrow(stack)) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    /** ArrowNockEvent: a bow can be drawn with arrows only in a quiver, or with magic arrows. */
    @SubscribeEvent
    public static void onGetProjectile(LivingGetProjectileEvent event) {
        if (!(event.getEntity() instanceof Player player) || !(event.getProjectileWeaponItemStack().getItem() instanceof BowItem)) {
            return;
        }
        if (!event.getProjectileItemStack().isEmpty()) {
            return;
        }
        ItemStack quiver = quiverWithArrows(player);
        if (!quiver.isEmpty()) {
            event.setProjectileItemStack(new ItemStack(Items.ARROW));
            return;
        }
        ItemStack magic = magicArrow(player);
        if (!magic.isEmpty()) {
            event.setProjectileItemStack(magic.copyWithCount(1));
        }
    }

    /**
     * ArrowLooseEvent (cancelled, so the bow takes no damage, as in 1.5.2): an arrow from a quiver (first) or a magic arrow from the inventory (before plain arrows) with
     * the 1.5.2 speeds: plain (c^2 + 2c) / 3, magic (c^2 + 2c) / 4, each up to 1.5, crit from 1.0.
     */
    @SubscribeEvent
    public static void onArrowLoose(ArrowLooseEvent event) {
        Player player = event.getEntity();
        Level level = event.getLevel();
        ItemStack bow = event.getBow();
        ItemStack quiver = quiverWithArrows(player);
        float c = event.getCharge() / 20.0F;
        if (!quiver.isEmpty()) {
            event.setCanceled(true);
            if (level.isClientSide()) {
                return;
            }
            ItemStack arrow = BagItem.decreaseArrow(quiver);
            level.playSound(null, player, SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.0F, 0.8F + c * 0.5F);
            if (arrow.is(Items.ARROW)) {
                shootNormalArrow(bow, level, player, c);
            } else {
                shootMagicArrow(bow, level, player, c, BDItems.arrowType(arrow));
            }
            return;
        }
        ItemStack magic = magicArrow(player);
        if (!magic.isEmpty()) {
            event.setCanceled(true);
            if (level.isClientSide()) {
                return;
            }
            shootMagicArrow(bow, level, player, c, BDItems.arrowType(magic));
            magic.shrink(1);
            level.playSound(null, player, SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.0F, 0.8F + c * 0.5F);
        }
    }

    private static int enchantment(Level level, ItemStack bow, net.minecraft.resources.ResourceKey<net.minecraft.world.item.enchantment.Enchantment> key) {
        return EnchantmentHelper.getItemEnchantmentLevel(level.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT)
                .getOrThrow(key), bow);
    }

    /** EventHandlerBD.shootMagicArrow. */
    static void shootMagicArrow(ItemStack bow, Level level, Player player, float c, int type) {
        float speed = (c * c + c * 2.0F) / 4.0F;
        if (speed < 0.1) {
            return;
        }
        speed = Math.min(speed, 1.5F);
        BDMagicArrow arrow = BDMagicArrow.shoot(player, speed * 2.0F, type);
        if (speed >= 1.0F) {
            arrow.setCritArrow(true);
        }
        int power = enchantment(level, bow, Enchantments.POWER);
        if (power > 0) {
            arrow.setBaseDamage(arrow.getDamage() + power * 0.5 + 0.5);
        }
        int punch = enchantment(level, bow, Enchantments.PUNCH);
        if (punch > 0) {
            arrow.setKnockbackStrength(punch);
        }
        if (enchantment(level, bow, Enchantments.FLAME) > 0) {
            arrow.igniteForSeconds(100);
        }
        level.addFreshEntity(arrow);
    }

    /** EventHandlerBD.shootNormalArrow: a vanilla arrow at (c^2 + 2c) / 3 * 2 * 1.5. */
    static void shootNormalArrow(ItemStack bow, Level level, Player player, float c) {
        float speed = (c * c + c * 2.0F) / 3.0F;
        if (speed < 0.1) {
            return;
        }
        speed = Math.min(speed, 1.5F);
        Arrow arrow = new Arrow(level, player, new ItemStack(Items.ARROW), bow);
        arrow.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, speed * 2.0F * 1.5F, 1.0F);
        if (speed >= 1.0F) {
            arrow.setCritArrow(true);
        }
        arrow.pickup = Arrow.Pickup.ALLOWED;
        if (enchantment(level, bow, Enchantments.FLAME) > 0) {
            arrow.igniteForSeconds(100);
        }
        level.addFreshEntity(arrow);
    }

    /** EntityItemPickupEvent: arrows and magic arrows go into a quiver if one takes them. */
    @SubscribeEvent
    public static void onPickup(ItemEntityPickupEvent.Pre event) {
        ItemStack picked = event.getItemEntity().getItem();
        if (!BagItem.isArrow(picked) || event.getItemEntity().hasPickUpDelay()) {
            return;
        }
        Player player = event.getPlayer();
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack quiver = player.getInventory().getItem(i);
            if (quiver.is(BDItems.QUIVER.get()) && BagItem.addArrow(quiver, picked)) {
                player.take(event.getItemEntity(), picked.getCount());
                event.getItemEntity().discard();
                event.setCanPickup(TriState.FALSE);
                return;
            }
        }
    }

    // ---------------------------------------------------------------- armour (ItemArmorBase.onArmorTickUpdate)

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        Level level = player.level();
        for (EquipmentSlot slot : new EquipmentSlot[] {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            ItemStack armor = player.getItemBySlot(slot);
            int type = armor.is(BDItems.TURTLE_HELMET.get()) ? 0 : armor.is(BDItems.TURTLE_PLATE.get()) ? 1 : armor.is(BDItems.TURTLE_PANTS.get()) ? 2
                    : armor.is(BDItems.TURTLE_BOOTS.get()) ? 3 : -1;
            if (type >= 0 && player.tickCount % 400 == type * 100) {
                // ItemArmorTurtle: one half heart every 20 s per piece, staggered by the piece
                if (!level.isClientSide()) {
                    player.heal(1);
                }
                BDFx.particle(level, ParticleTypes.HEART, player.getX() + BDItemUtil.RAND.nextDouble() - 0.5, player.getY() + 1.0 + BDItemUtil.RAND.nextDouble(),
                        player.getZ() + BDItemUtil.RAND.nextDouble() - 0.5, 0.0, 1.0, 0.0);
            }
        }
        if (player.getItemBySlot(EquipmentSlot.FEET).is(BDItems.CLOUD_BOOTS.get())) {
            cloudBoots(player, level);
        }
        if (player instanceof ServerPlayer server) {
            scouter(server, player.getItemBySlot(EquipmentSlot.HEAD).is(BDItems.SCOUTER.get()));
        }
    }

    /**
     * ItemArmorBootsCloud: a jump from the ground goes up with 0.9 (not while sneaking), falls end after 3 blocks,
     * clouds while sprinting. Runs on both sides like 1.5.2; the client's motion counts for the player.
     * (jumpMovementFactor 0.045, more air control, has no counterpart.)
     */
    private static void cloudBoots(Player player, Level level) {
        Map<UUID, Boolean> map = level.isClientSide() ? LAST_ON_GROUND_CLIENT : LAST_ON_GROUND_SERVER;
        boolean onGround = map.getOrDefault(player.getUUID(), player.onGround());
        if (BDFx.isJumping(player) && onGround && !player.isShiftKeyDown()) {
            var m = player.getDeltaMovement();
            player.setDeltaMovement(m.x, 0.9, m.z);
            player.fallDistance = -2.0;
            for (int i = 0; i < 3; i++) {
                level.addParticle(ParticleTypes.CLOUD, player.getX(), player.getY() - 0.4, player.getZ(), (BDItemUtil.RAND.nextFloat() - 0.5F) / 2.0F, 0.5,
                        (BDItemUtil.RAND.nextFloat() - 0.5F) / 2.0F);
            }
        }
        map.put(player.getUUID(), player.onGround());
        if (player.fallDistance >= 3.0) {
            player.resetFallDistance();
            for (int i = 0; i < 3; i++) {
                level.addParticle(ParticleTypes.CLOUD, player.getX(), player.getY() - 0.4, player.getZ(), (BDItemUtil.RAND.nextFloat() - 0.5F) / 2.0F, -0.5,
                        (BDItemUtil.RAND.nextFloat() - 0.5F) / 2.0F);
            }
        }
        if (player.isSprinting()) {
            level.addParticle(ParticleTypes.CLOUD, player.getX(), player.getY() + 0.1, player.getZ(), (BDItemUtil.RAND.nextFloat() - 0.5F) / 2.0F, 0.1,
                    (BDItemUtil.RAND.nextFloat() - 0.5F) / 2.0F);
        }
    }

    /**
     * ItemArmorHelmetScouter: the living entity looked at within 40 blocks is shown with "name health/max" in the boss
     * bar (PacketEntityHealth to BossStatus, which faded after 100 frames; here 100 ticks).
     */
    private static void scouter(ServerPlayer player, boolean wearing) {
        UUID id = player.getUUID();
        ServerBossEvent bar = SCOUTER_BARS.get(id);
        Entity target = wearing ? BDItemUtil.getTarget(player, 40.0) : null;
        if (target instanceof LivingEntity living) {
            if (bar == null) {
                bar = new ServerBossEvent(UUID.randomUUID(), Component.empty(), BossEvent.BossBarColor.PINK, BossEvent.BossBarOverlay.PROGRESS);
                SCOUTER_BARS.put(id, bar);
            }
            bar.setName(Component.literal(living.getName().getString() + " " + (int) Math.ceil(living.getHealth()) + "/" + (int) living.getMaxHealth()));
            bar.setProgress(Math.max(0.0F, Math.min(1.0F, living.getHealth() / living.getMaxHealth())));
            bar.addPlayer(player);
            SCOUTER_TIME.put(id, 100);
        } else if (bar != null) {
            int left = SCOUTER_TIME.getOrDefault(id, 0) - 1;
            SCOUTER_TIME.put(id, left);
            if (left <= 0 || player.isRemoved()) {
                bar.removeAllPlayers();
                SCOUTER_BARS.remove(id);
            }
        }
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        ServerBossEvent bar = SCOUTER_BARS.remove(event.getEntity().getUUID());
        if (bar != null) {
            bar.removeAllPlayers();
        }
        LAST_ON_GROUND_SERVER.remove(event.getEntity().getUUID());
    }

    // ---------------------------------------------------------------- VillageHandler

    /**
     * VillageHandler (enableCapsulesInSurvival): the priest (cleric) sells the editable station for 64 emeralds, the big
     * one for 32, the medium for 16, the small for 8. 1.5.2 added them whenever the trades were made; here they are
     * added to every cleric that lacks them (checked every 200 ticks). Uses as a 1.5.2 MerchantRecipe: 7.
     */
    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof Villager villager) || villager.level().isClientSide() || villager.tickCount % 200 != 0) {
            return;
        }
        if (!villager.getVillagerData().profession().is(VillagerProfession.CLERIC)) {
            return;
        }
        MerchantOffers offers = villager.getOffers();
        int[][] trades = {{64, 0}, {32, 4}, {16, 3}, {8, 2}};
        for (int[] trade : trades) {
            ItemStack result = new ItemStack(BDItems.STATION_ITEMS.get(trade[1]).get());
            boolean present = offers.stream().anyMatch(o -> o.getResult().is(result.getItem()));
            if (!present) {
                offers.add(new MerchantOffer(new ItemCost(Items.EMERALD, trade[0]), result, 7, 1, 0.05F));
            }
        }
    }
}

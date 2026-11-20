package legendgear;

import legendgear.block.MysticShrubBlock;
import legendgear.entity.BombEntity;
import legendgear.entity.FallingStarEntity;
import legendgear.entity.QuakeEntity;
import legendgear.entity.WhirlwindEntity;
import legendgear.item.AmmoBagItem;
import legendgear.item.AmuletItem;
import legendgear.item.MagicPowderItem;
import legendgear.item.MedallionItem;
import legendgear.item.TitanBandItem;
import legendgear.recipe.MedallionSwordRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Enderman;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.monster.skeleton.WitherSkeleton;
import net.minecraft.world.entity.monster.zombie.ZombifiedPiglin;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.entity.projectile.hurtingprojectile.SmallFireball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.minecraft.util.TriState;
import net.neoforged.neoforge.event.entity.living.EndermanAngerEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingGetProjectileEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.ArrowLooseEvent;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * ForgeEventHooksHandler, AugmentedSwordHandler, StarfallHandler, JumpNoticeHandler and the event methods of the items
 * (headband, wind boots, titan band, magic powder, ender medallion, soul tether, phoenix feather, emerald sounds).
 * Player data of 1.5.2's getEntityData is the persistent data under the same names.
 */
public final class LGEvents {
    public static final ResourceKey<Enchantment> FOCUS = ResourceKey.create(Registries.ENCHANTMENT, LegendGear.id("focus"));
    public static final ResourceKey<Enchantment> SOUL_TETHER = ResourceKey.create(Registries.ENCHANTMENT, LegendGear.id("soul_tether"));
    private static final net.minecraft.resources.Identifier WIND_BOOTS_SPEED = LegendGear.id("wind_boots");
    private static final int SWORD_CHARGE_TIME = 30;
    private static final int SWORD_DISCHARGE_TIME = 15;
    private static final float SPARK_DISTANCE = 0.75F;

    private LGEvents() {
    }

    static int enchantment(Level level, ResourceKey<Enchantment> key, ItemStack stack) {
        Holder<Enchantment> holder = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).get(key).orElse(null);
        return holder == null ? 0 : EnchantmentHelper.getItemEnchantmentLevel(holder, stack);
    }

    // ---------------------------------------------------------------- pickups

    /** handleCollect: arrows go into a quiver, bombs into a bag, hearts heal at once. */
    @SubscribeEvent
    public static void onPickup(ItemEntityPickupEvent.Pre event) {
        ItemEntity entity = event.getItemEntity();
        ItemStack item = entity.getItem();
        Player player = event.getPlayer();
        if (entity.hasPickUpDelay()) {
            return;
        }
        if (item.is(Items.ARROW) || item.is(LGItems.BOMB.get())) {
            boolean bombs = item.is(LGItems.BOMB.get());
            int total = item.getCount();
            for (ItemStack bag : player.getInventory().getNonEquipmentItems()) {
                if (total <= 0) {
                    break;
                }
                if (bag.getItem() instanceof AmmoBagItem b && b.bombs() == bombs && AmmoBagItem.ammo(bag) < b.capacity()) {
                    int take = Math.min(total, b.capacity() - AmmoBagItem.ammo(bag));
                    total -= take;
                    AmmoBagItem.setAmmo(bag, AmmoBagItem.ammo(bag) + take);
                }
            }
            if (total != item.getCount()) {
                if (total <= 0) {
                    player.take(entity, item.getCount());
                    entity.discard();
                    event.setCanPickup(TriState.FALSE);
                } else {
                    item.setCount(total);
                }
            }
        } else if (item.is(LGItems.HEART.get())) {
            if (!player.level().isClientSide()) {
                player.heal(item.getCount() * 2);
                LGFx.sound(entity, LGSounds.HEART.get(), 0.5F, 1.0F);
                player.take(entity, 1);
                entity.discard();
            }
            event.setCanPickup(TriState.FALSE);
        }
    }

    /** EmeraldSoundHandler: shards, pieces and emeralds ring when picked up. */
    @SubscribeEvent
    public static void onPickedUp(ItemEntityPickupEvent.Post event) {
        ItemStack item = event.getOriginalStack();
        ItemEntity entity = event.getItemEntity();
        if (item.is(LGItems.EMERALD_SHARD.get())) {
            LGFx.sound(event.getPlayer(), LGSounds.MONEYSMALL.get(), 0.5F, 1.0F);
        } else if (item.is(LGItems.EMERALD_PIECE.get())) {
            LGFx.sound(event.getPlayer(), LGSounds.MONEYMID.get(), 0.5F, 1.0F);
        } else if (item.is(Items.EMERALD)) {
            LGFx.sound(event.getPlayer(), LGSounds.MONEYBIG.get(), 0.5F, 1.0F);
        }
        entity.getClass();
    }

    // ---------------------------------------------------------------- bow and quiver

    private static ItemStack nonEmpty(Player player, boolean bombs) {
        for (ItemStack bag : player.getInventory().getNonEquipmentItems()) {
            if (bag.getItem() instanceof AmmoBagItem b && b.bombs() == bombs && AmmoBagItem.ammo(bag) > 0) {
                return bag;
            }
        }
        return ItemStack.EMPTY;
    }

    /** handleDrawBow (ArrowNockEvent): a bow can be drawn with arrows only in a quiver. */
    @SubscribeEvent
    public static void onGetProjectile(LivingGetProjectileEvent event) {
        if (event.getEntity() instanceof Player player && !player.getAbilities().instabuild && event.getProjectileItemStack().isEmpty()
                && event.getProjectileWeaponItemStack().is(Items.BOW) && !nonEmpty(player, false).isEmpty()) {
            event.setProjectileItemStack(new ItemStack(Items.ARROW));
        }
    }

    /**
     * handleFireBow: without infinity a quiver's arrow comes first (1.5.2 speed and enchantments, no bow damage from the
     * bow itself but one point here); sneaking ties a bomb from a bomb bag to it (fuse 80, the arrow 40% slower).
     */
    @SubscribeEvent
    public static void onArrowLoose(ArrowLooseEvent event) {
        Player player = event.getEntity();
        ItemStack bow = event.getBow();
        Level level = event.getLevel();
        if (player.getAbilities().instabuild || enchantment(level, Enchantments.INFINITY, bow) > 0) {
            return;
        }
        ItemStack quiver = nonEmpty(player, false);
        if (quiver.isEmpty()) {
            return;
        }
        event.setCanceled(true);
        float f = event.getCharge() / 20.0F;
        f = (f * f + f * 2.0F) / 3.0F;
        if (f < 0.1) {
            return;
        }
        f = Math.min(f, 1.0F);
        if (level.isClientSide()) {
            return;
        }
        Arrow arrow = new Arrow(level, player, new ItemStack(Items.ARROW), bow);
        arrow.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, f * 2.0F * 1.5F, 1.0F);
        if (f == 1.0F) {
            arrow.setCritArrow(true);
        }
        arrow.pickup = AbstractArrow.Pickup.ALLOWED;
        if (enchantment(level, Enchantments.FLAME, bow) > 0) {
            arrow.igniteForSeconds(100);
        }
        LGFx.damage(bow, 1, player);
        LGFx.sound(player, SoundEvents.ARROW_SHOOT, 1.0F, 1.0F / (float) (Math.random() * 0.4F + 1.2F) + f * 0.5F);
        AmmoBagItem.setAmmo(quiver, AmmoBagItem.ammo(quiver) - 1);
        level.addFreshEntity(arrow);
        if (LGConfig.BOMB_ARROWS && player.isShiftKeyDown()) {
            ItemStack bombs = nonEmpty(player, true);
            if (!bombs.isEmpty()) {
                AmmoBagItem.setAmmo(bombs, AmmoBagItem.ammo(bombs) - 1);
                BombEntity bomb = BombEntity.thrown(level, player, 80);
                level.addFreshEntity(bomb);
                bomb.startRiding(arrow, true, true);
                arrow.setDeltaMovement(arrow.getDeltaMovement().scale(0.6));
                arrow.needsSync = true;
            }
        }
    }

    // ---------------------------------------------------------------- damage

    private static ItemStack findDamaged(Player p, net.minecraft.world.item.Item type) {
        for (ItemStack stack : p.getInventory().getNonEquipmentItems()) {
            if (stack.is(type) && stack.getDamageValue() > 0) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    /**
     * handleHurt: falls charge the earth medallion (or, holding up the geo amulet, become a quake of radius 3 to 15),
     * arrows the wind medallion, fire the fire medallion (or, holding up the pyro amulet, heal). Plus the headband of
     * valor (ItemHeadband.applyDamageBoost) and the titan band dropping what it carries.
     */
    @SubscribeEvent
    public static void onHurt(LivingIncomingDamageEvent event) {
        LivingEntity hurt = event.getEntity();
        DamageSource source = event.getSource();
        if (!(hurt.level() instanceof ServerLevel level)) {
            return;
        }
        int amount = (int) event.getAmount();
        if (hurt instanceof Player ep) {
            ItemStack using = ep.isUsingItem() ? ep.getUseItem() : ItemStack.EMPTY;
            if (source.is(DamageTypes.FALL)) {
                if (using.getItem() instanceof AmuletItem a && a.kind() == AmuletItem.Kind.GEO) {
                    LGFx.damage(using, amount, ep);
                    float scale = Math.min(amount / 20.0F, 1.0F);
                    QuakeEntity quake = new QuakeEntity(level, ep.getX(), ep.getY(), ep.getZ(), ep, true, 3.0 + 12.0 * scale);
                    quake.damagePerHit = amount;
                    level.addFreshEntity(quake);
                    LGFx.sound(ep, SoundEvents.GENERIC_EXPLODE.value(), 0.4F + 3.0F * scale, 1.2F - scale * 0.8F);
                    event.setCanceled(true);
                    return;
                }
                ItemStack stack = findDamaged(ep, LGItems.EARTH_MEDALLION.get());
                if (!stack.isEmpty()) {
                    MedallionItem.charge(ep, stack, amount);
                }
            }
            if (source.is(DamageTypeTags.IS_PROJECTILE) && source.getDirectEntity() instanceof AbstractArrow) {
                ItemStack stack = findDamaged(ep, LGItems.WIND_MEDALLION.get());
                if (!stack.isEmpty()) {
                    MedallionItem.charge(ep, stack, amount);
                }
            }
            if (source.is(DamageTypeTags.IS_FIRE)) {
                if (using.getItem() instanceof AmuletItem a && a.kind() == AmuletItem.Kind.PYRO) {
                    LGFx.damage(using, amount, ep);
                    ep.heal(amount);
                    LGFx.sound(ep, LGSounds.HEART.get(), 0.2F, 1.0F);
                    ep.setInvulnerableTime(20);
                    event.setCanceled(true);
                    return;
                }
                ItemStack stack = findDamaged(ep, LGItems.FIRE_MEDALLION.get());
                if (!stack.isEmpty()) {
                    MedallionItem.charge(ep, stack, amount);
                }
            }
            if (!ep.getPassengers().isEmpty()) {
                TitanBandItem.drop(ep);
            }
        }
        if (hurt.getVehicle() instanceof Player carrier && carrier.getPersistentData().getBooleanOr("TitanLift", false)) {
            TitanBandItem.drop(carrier);
        }
        if (source.is(DamageTypes.PLAYER_ATTACK) && source.getEntity() instanceof Player attacker) {
            ItemStack head = attacker.getItemBySlot(EquipmentSlot.HEAD);
            if (head.is(LGItems.HEADBAND_OF_VALOR.get()) && attacker.getArmorValue() == 0) {
                CompoundTag custom = head.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA,
                        net.minecraft.world.item.component.CustomData.EMPTY).copyTag();
                int bonus = custom.contains("valorBonus") ? amount + custom.getIntOr("valorBonus", 0) : amount + 3 + amount / 4;
                LGFx.sound(hurt, LGSounds.ESCALATE.get(), 0.5F, 0.8F + hurt.getRandom().nextFloat() * 0.4F);
                event.setAmount(bonus);
                LGFx.damage(head, 1, attacker);
            }
        }
    }

    /** EnderMedallion.enderGaze: an enderman's stare (it starts to hunt the player) charges the ender medallion by 5. */
    @SubscribeEvent
    public static void onEndermanAnger(EndermanAngerEvent event) {
        Player ep = event.getPlayer();
        if (!ep.level().isClientSide() && event.getEntity().getTarget() != ep) {
            ItemStack stack = findDamaged(ep, LGItems.ENDER_MEDALLION.get());
            if (!stack.isEmpty()) {
                MedallionItem.charge(ep, stack, 5);
            }
        }
    }

    /** phoenixEffect: a phoenix feather is used up instead of dying. */
    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof Player ep)) {
            return;
        }
        var inv = ep.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.is(LGItems.PHOENIX_FEATHER.get())) {
                if (!ep.level().isClientSide()) {
                    stack.shrink(1);
                    ep.setHealth(1.0F);
                    ep.setInvulnerableTime(65);
                    ep.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 28, 3));
                    ep.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 65, 4));
                    ep.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 65, 1));
                    ep.removeEffect(MobEffects.POISON);
                    ep.removeEffect(MobEffects.WITHER);
                    ep.igniteForSeconds(3);
                    LGFx.sound(ep, SoundEvents.FIREWORK_ROCKET_LAUNCH, 5.0F, 1.0F);
                    LGFx.sound(ep, LGSounds.SPEEDBOOST.get(), 0.4F, 0.3F);
                }
                event.setCanceled(true);
                return;
            }
        }
    }

    // ---------------------------------------------------------------- shrubs, loot

    private static final net.minecraft.world.item.Item[] RECORDS = {Items.MUSIC_DISC_13, Items.MUSIC_DISC_CAT, Items.MUSIC_DISC_BLOCKS,
            Items.MUSIC_DISC_CHIRP, Items.MUSIC_DISC_FAR, Items.MUSIC_DISC_MALL, Items.MUSIC_DISC_MELLOHI, Items.MUSIC_DISC_STAL,
            Items.MUSIC_DISC_STRAD, Items.MUSIC_DISC_WARD, Items.MUSIC_DISC_11, Items.MUSIC_DISC_WAIT};

    /** shrubHandler: hitting an energized shrub in a storm can call lightning and drop a fabulous prize from 30 up. */
    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        Level world = event.getLevel();
        BlockPos pos = event.getPos();
        if (event.getAction() != PlayerInteractEvent.LeftClickBlock.Action.START || !(world instanceof ServerLevel level)
                || !LGConfig.SHRUB_SUPER_PRIZES || level.getRandom().nextDouble() >= LGConfig.SHRUB_JACKPOT_CHANCE) {
            return;
        }
        BlockState state = level.getBlockState(pos);
        if (!state.is(LGBlocks.MYSTIC_SHRUB.get()) || state.getValue(MysticShrubBlock.STAGE) != 1 || !level.isRainingAt(pos)) {
            return;
        }
        MysticShrubBlock.lightning(level, pos);
        var r = level.getRandom();
        ItemStack prize = switch (r.nextInt(13)) {
            case 0 -> new ItemStack(Items.EMERALD, 4 + r.nextInt(4));
            case 1 -> new ItemStack(Items.ENDER_PEARL, 3 + r.nextInt(3));
            case 2 -> new ItemStack(LGItems.PHOENIX_FEATHER.get());
            case 3 -> new ItemStack(Items.GOLDEN_APPLE);
            case 4 -> new ItemStack(Items.FIRE_CHARGE, 8 + r.nextInt(8));
            case 5 -> new ItemStack(Items.EXPERIENCE_BOTTLE, 15 + r.nextInt(11));
            case 6 -> new ItemStack(Items.CLOCK);
            case 7 -> new ItemStack(Items.CAKE);
            case 8 -> new ItemStack(RECORDS[r.nextInt(RECORDS.length)]);
            case 9 -> new ItemStack(Blocks.TNT, 8);
            case 10 -> new ItemStack(Items.DIAMOND);
            case 11 -> new ItemStack(LGItems.MYSTIC_SEED.get());
            default -> ItemStack.EMPTY;
        };
        if (!prize.isEmpty()) {
            level.addFreshEntity(new ItemEntity(level, pos.getX(), pos.getY() + 30, pos.getZ(), prize));
        } else {
            // the anvil of case 12: World.getActualHeight() - 3 of 1.5.2 (256 - 3)
            level.setBlock(new BlockPos(pos.getX(), Math.min(253, level.getMaxY() - 2), pos.getZ()), Blocks.ANVIL.defaultBlockState(), 2);
        }
    }

    /**
     * getLootFrom: endermen, blazes, ghasts, zombie pigmen, wither skeletons and charged creepers drop hearts (1 or 3) or
     * emerald shards/pieces (times 1 + rand(looting + 1)) in 9 of 10 cases; other mobs a heart (2 in 5 or 15) or shards.
     */
    private static List<ItemStack> lootFrom(LivingEntity el, int looting) {
        List<ItemStack> items = new ArrayList<>();
        var rand = el.getRandom();
        int bonus = (int) (Math.random() * (looting + 1)) + 1;
        boolean special = el instanceof Enderman || el instanceof Blaze || el instanceof Ghast || el instanceof ZombifiedPiglin
                || el instanceof WitherSkeleton || el instanceof Creeper c && c.isPowered();
        if (special) {
            switch (rand.nextInt(10)) {
                case 0, 1 -> {
                    for (int i = 0; i < 3; i++) {
                        items.add(new ItemStack(LGItems.HEART.get()));
                    }
                }
                case 2, 3, 4 -> items.add(new ItemStack(LGItems.HEART.get()));
                case 5, 6, 7 -> items.add(new ItemStack(LGItems.EMERALD_SHARD.get(), bonus));
                case 8 -> items.add(new ItemStack(LGItems.EMERALD_SHARD.get(), 3 * bonus));
                default -> items.add(new ItemStack(LGItems.EMERALD_PIECE.get(), bonus));
            }
        } else {
            // experienceValue >= 5: the monsters of 1.5.2
            int roll = rand.nextInt(el instanceof Enemy ? 5 : 15);
            if (roll <= 1) {
                items.add(new ItemStack(LGItems.HEART.get()));
            } else if (roll <= 3) {
                items.add(new ItemStack(LGItems.EMERALD_SHARD.get(), bonus));
            }
        }
        return items;
    }

    /** bonusLoot; creepers killed by an explosion drop 3 bombs. Plus EnchantmentSoulTether.onPlayerDeathDrops. */
    @SubscribeEvent
    public static void onDrops(LivingDropsEvent event) {
        LivingEntity ent = event.getEntity();
        if (!(ent.level() instanceof ServerLevel level)) {
            return;
        }
        if (ent instanceof Player player) {
            soulTether(player, event);
            return;
        }
        if (event.isRecentlyHit()) {
            int looting = 0;
            if (event.getSource().getEntity() instanceof LivingEntity killer) {
                looting = enchantment(level, Enchantments.LOOTING, killer.getMainHandItem());
            }
            for (ItemStack i : lootFrom(ent, looting)) {
                event.getDrops().add(new ItemEntity(level, ent.getX(), ent.getY(), ent.getZ(), i));
            }
        }
        if (ent instanceof Creeper && event.getSource().is(DamageTypeTags.IS_EXPLOSION)) {
            event.getDrops().add(new ItemEntity(level, ent.getX(), ent.getY(), ent.getZ(), new ItemStack(LGItems.BOMB.get(), 3)));
        }
    }

    private static void soulTether(Player player, LivingDropsEvent event) {
        ListTag items = new ListTag();
        Iterator<ItemEntity> it = event.getDrops().iterator();
        var ops = player.registryAccess().createSerializationContext(net.minecraft.nbt.NbtOps.INSTANCE);
        while (it.hasNext()) {
            ItemEntity ei = it.next();
            if (enchantment(player.level(), SOUL_TETHER, ei.getItem()) > 0) {
                it.remove();
                ItemStack.CODEC.encodeStart(ops, ei.getItem()).result().ifPresent(items::add);
            }
        }
        if (!items.isEmpty()) {
            CompoundTag persist = player.getPersistentData().getCompoundOrEmpty(Player.PERSISTED_NBT_TAG);
            persist.put("soulboundItems", items);
            player.getPersistentData().put(Player.PERSISTED_NBT_TAG, persist);
        }
    }

    /** onPlayerRespawn: the soul-tethered items come back. */
    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        Player player = event.getEntity();
        CompoundTag persist = player.getPersistentData().getCompoundOrEmpty(Player.PERSISTED_NBT_TAG);
        if (player.level().isClientSide() || !persist.contains("soulboundItems")) {
            return;
        }
        var ops = player.registryAccess().createSerializationContext(net.minecraft.nbt.NbtOps.INSTANCE);
        for (Tag tag : persist.getListOrEmpty("soulboundItems")) {
            ItemStack.CODEC.parse(ops, tag).result().ifPresent(stack -> {
                if (!player.getInventory().add(stack)) {
                    player.drop(stack, false, net.minecraft.util.Prediction.SERVER_ONLY);
                }
            });
        }
        persist.remove("soulboundItems");
        player.getPersistentData().put(Player.PERSISTED_NBT_TAG, persist);
    }

    /** EntityInteractEvent: magic powder on mobs, the titan band lifting them. */
    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        Player player = event.getEntity();
        ItemStack item = player.getItemInHand(event.getHand());
        if (event.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }
        if (item.is(LGItems.MAGIC_POWDER.get())) {
            if (!player.level().isClientSide() && MagicPowderItem.sprinkle(player, item, event.getTarget())) {
                event.setCancellationResult(InteractionResult.SUCCESS);
                event.setCanceled(true);
            }
        } else if (item.is(LGItems.TITAN_BAND.get())) {
            if (TitanBandItem.lift(player, item, event.getTarget())) {
                event.setCancellationResult(InteractionResult.SUCCESS);
                event.setCanceled(true);
            }
        }
    }

    // ---------------------------------------------------------------- player ticks

    /** JumpNoticeHandler: the tick the jump key went down (the server knows it from the client's input). */
    public static boolean justJumped(Player player) {
        return player.getPersistentData().getBooleanOr("justJumped", false);
    }

    private static boolean jumping(Player player) {
        if (player instanceof ServerPlayer sp) {
            return sp.getLastClientInput().jump();
        }
        return false;
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        CompoundTag data = player.getPersistentData();
        if (!player.level().isClientSide()) {
            boolean jump = jumping(player);
            data.putBoolean("justJumped", jump && !data.getBooleanOr("wasJumping", false));
            data.putBoolean("wasJumping", jump);
        }
        swordTick(player, data);
        starfall(player, data);
        windBoots(player, data);
        // TitanBand.HandleDrop
        if (!player.level().isClientSide() && !player.getPassengers().isEmpty() && data.getBooleanOr("TitanLift", false)
                && !player.getMainHandItem().is(LGItems.TITAN_BAND.get())) {
            TitanBandItem.drop(player);
        }
        if (player.getPassengers().isEmpty()) {
            data.putBoolean("TitanLift", false);
        }
        TitanBandItem.glide(player);
    }

    /** StarfallHandler: under the open sky in the dark (light below 6) a falling star comes every 8400 / 1 ticks on average. */
    private static void starfall(Player player, CompoundTag data) {
        if (!(player.level() instanceof ServerLevel level) || !LGConfig.ENABLE_STARFALL || level.dimension() == Level.NETHER) {
            return;
        }
        BlockPos pos = BlockPos.containing(player.getX(), player.getY() + 2.1, player.getZ());
        if (level.canSeeSky(pos) && level.getMaxLocalRawBrightness(pos) < 6) {
            int timer = data.getIntOr("starTimer", 0) + (int) (Math.random() * 3.0);
            if (timer >= LGConfig.STARFALL_RARITY) {
                level.addFreshEntity(new FallingStarEntity(player));
                timer = 0;
            }
            data.putInt("starTimer", timer);
        }
    }

    /**
     * ItemWindBoots.onArmorTickUpdate: sprinting on land doubles the speed (here a speed modifier) and runs over water;
     * every tick of it wears the boots by one, with dust and a dash sound on the ground.
     */
    private static void windBoots(Player player, CompoundTag data) {
        ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
        AttributeInstance speed = player.getAttribute(Attributes.MOVEMENT_SPEED);
        boolean dashing = boots.is(LGItems.WIND_BOOTS.get()) && player.isSprinting() && !player.isInWater();
        if (speed != null) {
            if (dashing && !speed.hasModifier(WIND_BOOTS_SPEED)) {
                speed.addTransientModifier(new AttributeModifier(WIND_BOOTS_SPEED, 1.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
            } else if (!dashing && speed.hasModifier(WIND_BOOTS_SPEED)) {
                speed.removeModifier(WIND_BOOTS_SPEED);
            }
        }
        if (!dashing) {
            data.putInt("dashTicks", 0);
            return;
        }
        int dash = data.getIntOr("dashTicks", 0);
        BlockPos below = BlockPos.containing(player.getX(), player.getY() - 0.0625, player.getZ());
        Vec3 m = player.getDeltaMovement();
        if (!player.onGround() && !player.level().getFluidState(below).isEmpty() && m.y <= 0.0 && m.y >= -0.2) {
            player.setDeltaMovement(m.x, 0.0, m.z);
            player.setOnGround(true);
            player.setPos(player.getX(), below.getY() + 1.03, player.getZ());
            player.resetFallDistance();
        }
        LGFx.damage(boots, 1, player);
        if (player.onGround()) {
            if (player.level().isClientSide()) {
                player.level().addParticle(ParticleTypes.POOF, player.getX(), player.getY(), player.getZ(), 0.0, 0.0, 0.0);
            } else if (dash % 3 == 0) {
                LGFx.sound(player, LGSounds.DASH.get(), 0.2F, 0.9F + player.getRandom().nextFloat() * 0.2F);
            }
        }
        data.putInt("dashTicks", dash + 1);
    }

    private static Vec3 sparkPoint(Player player) {
        float yaw = player.getYRot() / 180.0F * (float) Math.PI;
        float pitch = player.getXRot() / 180.0F * (float) Math.PI;
        return new Vec3(player.getX() - Mth.sin(yaw) * SPARK_DISTANCE * Mth.cos(pitch),
                player.getEyeY() - Mth.sin(pitch) * SPARK_DISTANCE - 0.125,
                player.getZ() + Mth.cos(yaw) * SPARK_DISTANCE * Mth.cos(pitch));
    }

    /**
     * AugmentedSwordHandler.swordTick: blocking with an augmented sword charges it (1 + 0.5 per focus level a tick, full
     * at 30); a charged sword stays ready for 15 ticks and fires its augment with the next swing. The ender augment's
     * recall: blocking within 50 ticks after the jump takes the player back.
     */
    private static void swordTick(Player player, CompoundTag data) {
        Level level = player.level();
        ItemStack stack = player.getMainHandItem();
        int recall = data.getIntOr("recallTime", 0);
        if (recall > 0) {
            if (player.isBlocking()) {
                data.putInt("recallTime", 0);
                if (level instanceof ServerLevel) {
                    bamf(player, data.getDoubleOr("recallX", player.getX()), data.getDoubleOr("recallY", player.getY()),
                            data.getDoubleOr("recallZ", player.getZ()));
                }
            } else {
                data.putInt("recallTime", recall - 1);
            }
        }
        String augment = MedallionSwordRecipe.augment(stack);
        if (player.isBlocking() && augment != null) {
            float increment = 1.0F + enchantment(level, FOCUS, stack) * 0.5F;
            float charge = data.getFloatOr("swordCharge", 0.0F) + increment;
            data.putFloat("swordCharge", charge);
            if (charge < SWORD_CHARGE_TIME && level.isClientSide() && charge > 4.0F) {
                Vec3 p = sparkPoint(player);
                float yaw = player.getYRot() / 180.0F * (float) Math.PI;
                for (int i = 0; i < increment; i++) {
                    double offx = Mth.cos(yaw) * SPARK_DISTANCE;
                    double offz = Mth.sin(yaw) * SPARK_DISTANCE;
                    double r = (SWORD_CHARGE_TIME - charge - i * increment / 2.0F) / SWORD_CHARGE_TIME;
                    double theta = r * Math.PI * 3.0;
                    double h = Math.cos(theta) * r;
                    double offy = Math.sin(theta) * r;
                    offx *= h;
                    offz *= h;
                    LGParticles.sparkle(level, p.x + offx * 0.6, p.y + offy * 0.6, p.z + offz * 0.6, 0, 0, 0, 0.3F);
                    LGParticles.sparkle(level, p.x - offx * 0.6, p.y - offy * 0.6, p.z - offz * 0.6, 0, 0, 0, 0.3F);
                }
            }
            if (charge >= SWORD_CHARGE_TIME && charge - increment < SWORD_CHARGE_TIME) {
                LGFx.sound(player, LGSounds.PARTIALCHARGE.get(), 0.15F, 2.0F);
            }
            if (charge >= SWORD_CHARGE_TIME) {
                data.putInt("medallionEnergy", SWORD_DISCHARGE_TIME);
            }
        } else {
            data.putFloat("swordCharge", 0.0F);
        }
        int energy = augment == null ? 0 : data.getIntOr("medallionEnergy", 0);
        boolean swinging = player.isSwinging();
        boolean swingStart = swinging && !data.getBooleanOr("lgWasSwinging", false);
        data.putBoolean("lgWasSwinging", swinging);
        if (energy > 0) {
            energy--;
            if (level.isClientSide()) {
                Vec3 p = sparkPoint(player);
                var r = player.getRandom();
                LGParticles.sparkle(level, p.x + r.nextGaussian() * 0.05, p.y + r.nextGaussian() * 0.05, p.z + r.nextGaussian() * 0.05, 0, 0, 0, 0.6F);
            }
            if (swingStart && data.getFloatOr("swordCharge", 0.0F) == 0.0F) {
                doSpecialAbility(player, stack, augment);
                energy = 0;
            }
        }
        data.putInt("medallionEnergy", energy);
    }

    /** bamf: the ender augment's jump (sound, portal particles; the recall timer stays on the player's data). */
    static void bamf(Player player, double x, double y, double z) {
        player.resetFallDistance();
        if (player.level() instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.PORTAL, player.getX(), player.getY() + 1.0, player.getZ(), 16, 1.0, 1.0, 1.0, 1.0);
        }
        player.teleportTo(x, y, z);
        LGFx.sound(player, SoundEvents.ENDERMAN_TELEPORT, 1.0F, 1.0F);
    }

    private static @Nullable LivingEntity beamScan(Player player, double range) {
        AABB box = new AABB(player.getX(), player.getY() + 1.6, player.getZ(), player.getX(), player.getY() + 1.6, player.getZ()).inflate(1.0);
        Vec3 step = player.getLookAngle();
        for (int i = 0; i < range; i++) {
            for (LivingEntity hit : player.level().getEntitiesOfClass(LivingEntity.class, box)) {
                if (hit != player) {
                    return hit;
                }
            }
            box = box.move(step);
            if (player.level() instanceof ServerLevel server) {
                Vec3 c = box.getCenter();
                server.sendParticles(ParticleTypes.POOF, c.x, c.y, c.z, 1, 0, 0, 0, 0);
            }
        }
        return null;
    }

    /** doSpecialAbility: the augment's attack, paid for with 1/(4 * enchantability) of the sword's durability. */
    private static void doSpecialAbility(Player player, ItemStack sword, String augment) {
        if (!(player.level() instanceof ServerLevel level)) {
            return;
        }
        Vec3 at = player.getLookAngle();
        boolean success = false;
        if (augment.equals(LGItems.FIRE_MEDALLION.getId().toString())) {
            SmallFireball fireball = new SmallFireball(level, player, at.scale(0.01));
            fireball.setPos(player.getX() + at.x, player.getEyeY() + at.y, player.getZ() + at.z);
            level.addFreshEntity(fireball);
            LGFx.sound(player, SoundEvents.GHAST_SHOOT, 1.0F, 1.0F);
            level.sendParticles(ParticleTypes.FLAME, player.getX() + at.x, player.getEyeY() + at.y, player.getZ() + at.z, 10, 0.04, 0.04, 0.04, 0.04);
            success = true;
        } else if (augment.equals(LGItems.EARTH_MEDALLION.getId().toString())) {
            QuakeEntity quake = new QuakeEntity(level, player.getX(), player.getY(), player.getZ(), player, true, 6.0);
            quake.damagePerHit = 5;
            level.addFreshEntity(quake);
            LGFx.sound(quake, SoundEvents.GENERIC_EXPLODE.value(), 1.0F, 1.2F);
            success = true;
        } else if (augment.equals(LGItems.WIND_MEDALLION.getId().toString())) {
            LGFx.sound(player, LGSounds.WHIRLWIND.get(), 1.0F, 0.8F + (float) Math.random() * 0.4F);
            level.addFreshEntity(new WhirlwindEntity(level, player));
            success = true;
        } else if (augment.equals(LGItems.ENDER_MEDALLION.getId().toString())) {
            LivingEntity el = beamScan(player, 64.0);
            if (el != null) {
                double angle = Math.atan2(el.getZ() - player.getZ(), el.getX() - player.getX());
                CompoundTag data = player.getPersistentData();
                data.putDouble("recallX", player.getX());
                data.putDouble("recallY", player.getY());
                data.putDouble("recallZ", player.getZ());
                data.putInt("recallTime", 50);
                bamf(player, el.getX() - Math.cos(angle) * 3.0, el.getY(), el.getZ() - Math.sin(angle) * 3.0);
                success = true;
            } else {
                LGFx.sound(player, SoundEvents.ENDERMAN_TELEPORT, 0.2F, 0.7F);
            }
        }
        if (success) {
            int enchantability = Math.max(1, sword.getOrDefault(net.minecraft.core.component.DataComponents.ENCHANTABLE,
                    new net.minecraft.world.item.enchantment.Enchantable(1)).value());
            int uses = enchantability * 4;
            int health = sword.getMaxDamage();
            int damage = health / uses;
            CompoundTag custom = sword.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA,
                    net.minecraft.world.item.component.CustomData.EMPTY).copyTag();
            double overflow = custom.getDoubleOr("damageOverflow", 0.0) + health * 1.0 / uses - damage;
            if (overflow > 1.0) {
                damage++;
                overflow--;
            }
            custom.putDouble("damageOverflow", overflow);
            sword.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.of(custom));
            LGFx.damage(sword, damage, player);
        }
    }
}

package tconstruct.player;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gamerules.GameRules;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import tconstruct.TConstruct;
import tconstruct.TItems;
import tconstruct.TPlayer;
import tconstruct.tools.InfiTool;
import tconstruct.tools.ToolType;

import java.util.Collection;

/**
 * TPlayerHandler, TCraftingHandler and the rest of TEventHandler (onHurt, onLivingDrop, onLivingSpawn) with the pack
 * configuration: levels and hunger kept on death, the beginner book on the first login. "Passive Health Regen" and
 * the alpha options are the vanilla behaviour (see TODO.md for the TFoodStats detail after a respawn).
 */
public final class PlayerEvents {
    private static final net.minecraft.resources.Identifier HEARTS = TConstruct.id("heart_canisters");

    private PlayerEvents() {
    }

    public static void register() {
        NeoForge.EVENT_BUS.addListener(PlayerEvent.PlayerLoggedInEvent.class, e -> login((ServerPlayer) e.getEntity()));
        NeoForge.EVENT_BUS.addListener(PlayerEvent.PlayerRespawnEvent.class, e -> respawn((ServerPlayer) e.getEntity()));
        NeoForge.EVENT_BUS.addListener(PlayerEvent.PlayerChangedDimensionEvent.class, e -> TPlayer.sendState((ServerPlayer) e.getEntity()));
        NeoForge.EVENT_BUS.addListener(PlayerEvent.Clone.class, PlayerEvents::clone);
        NeoForge.EVENT_BUS.addListener(PlayerEvent.ItemCraftedEvent.class, PlayerEvents::crafted);
        NeoForge.EVENT_BUS.addListener(LivingFallEvent.class, e -> {
            if (e.getEntity() instanceof Player) {
                e.setDistance(e.getDistance() - 1);
            }
        });
        NeoForge.EVENT_BUS.addListener(LivingIncomingDamageEvent.class, PlayerEvents::hurt);
        NeoForge.EVENT_BUS.addListener(LivingDropsEvent.class, PlayerEvents::drops);
        NeoForge.EVENT_BUS.addListener(FinalizeSpawnEvent.class, PlayerEvents::spawn);
    }

    // ------------------------------------------------------------------ TPlayerHandler

    private static void login(ServerPlayer player) {
        PlayerData data = TPlayer.data(player);
        if (!data.beginnerManual) {
            data.beginnerManual = true;
            give(player, new ItemStack(TPlayer.MANUALS.get(0).get()));
        }
        recalculateHealth(player);
        TPlayer.sendState(player);
    }

    private static void give(Player player, ItemStack stack) {
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false, net.minecraft.util.Prediction.SERVER_ONLY);
        }
    }

    /** ArmorExtended.recalculateHealth: slot 6 with red heart canisters gives +2 health each. */
    public static void recalculateHealth(ServerPlayer player) {
        AttributeInstance health = player.getAttribute(Attributes.MAX_HEALTH);
        if (health == null) {
            return;
        }
        ItemStack stack = TPlayer.data(player).armor.getItem(PlayerData.HEART_SLOT);
        int bonus = stack.is(TPlayer.RED_HEART_CANISTER.get()) ? stack.getCount() * 2 : 0;
        AttributeModifier current = health.getModifier(HEARTS);
        if (current != null && current.amount() == bonus) {
            return;
        }
        health.removeModifier(HEARTS);
        if (bonus > 0) {
            health.addPermanentModifier(new AttributeModifier(HEARTS, bonus, AttributeModifier.Operation.ADD_VALUE));
        }
        if (player.getHealth() > player.getMaxHealth()) {
            player.setHealth(player.getMaxHealth());
        }
    }

    private static void respawn(ServerPlayer player) {
        recalculateHealth(player);
        player.setHealth(player.getMaxHealth());
        TPlayer.sendState(player);
    }

    /** onPlayerRespawn: keepLevels (half the level of the death), keepHunger (at least 6). */
    private static void clone(PlayerEvent.Clone event) {
        if (!event.isWasDeath()) {
            return;
        }
        Player player = event.getEntity();
        PlayerData data = TPlayer.data(player);
        if (data.level >= 0) {
            player.experienceLevel = data.level;
            player.experienceProgress = 0.0F;
        }
        if (data.hunger >= 0) {
            player.getFoodData().setFoodLevel(data.hunger);
            player.getFoodData().setSaturation(5.0F);
        }
        data.level = -1;
        data.hunger = -1;
    }

    /** TCraftingHandler: the materials manual for the first table, the smeltery manual for the first smeltery block or tank. */
    private static void crafted(PlayerEvent.ItemCraftedEvent event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) {
            return;
        }
        String name = BuiltInRegistries.ITEM.getKey(event.getCrafting().getItem()).toString();
        PlayerData data = TPlayer.data(player);
        if (TPlayer.TABLE_ITEMS.contains(name) && !data.materialManual) {
            data.materialManual = true;
            spawnAtPlayer(player, new ItemStack(TPlayer.MANUALS.get(1).get()));
        }
        if (TPlayer.SMELTERY_ITEMS.contains(name) && !data.smelteryManual) {
            data.smelteryManual = true;
            spawnAtPlayer(player, new ItemStack(TPlayer.MANUALS.get(2).get()));
        }
    }

    /** AbilityHelper.spawnItemAtPlayer: an item at the player that can be picked up at once. */
    private static void spawnAtPlayer(Player player, ItemStack stack) {
        ItemEntity item = new ItemEntity(player.level(), player.getX() + 0.5, player.getY() + 0.5, player.getZ() + 0.5, stack);
        player.level().addFreshEntity(item);
        item.playerTouch(player);
    }

    // ------------------------------------------------------------------ TEventHandler.onHurt

    /** A player hit while holding a cutlass in use (blocking) gets Speed II for 3 seconds. */
    private static void hurt(LivingIncomingDamageEvent event) {
        if (event.getEntity() instanceof Player player && player.isUsingItem() && player.getMainHandItem().is(TItems.tool(ToolType.CUTLASS))) {
            player.addEffect(new MobEffectInstance(MobEffects.SPEED, 60, 1));
        }
    }

    // ------------------------------------------------------------------ TEventHandler.onLivingDrop

    private static void add(Collection<ItemEntity> drops, LivingEntity entity, ItemStack stack) {
        ItemEntity item = new ItemEntity(entity.level(), entity.getX(), entity.getY(), entity.getZ(), stack);
        item.setPickUpDelay(10);
        drops.add(item);
    }

    private static int looting(LivingEntity entity, DamageSource source) {
        if (source.getEntity() instanceof LivingEntity attacker) {
            return EnchantmentHelper.getEnchantmentLevel(entity.level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
                    .getOrThrow(Enchantments.LOOTING), attacker);
        }
        return 0;
    }

    /** Beheading of the weapon of a player's direct hit (cleaver +2), 0 for anything else. */
    private static int beheading(DamageSource source) {
        if (source.is(DamageTypes.PLAYER_ATTACK) && source.getEntity() instanceof Player player) {
            ItemStack stack = player.getMainHandItem();
            InfiTool tool = InfiTool.of(stack);
            if (tool != null) {
                int beheading = tool.getInt("Beheading");
                if (stack.is(TItems.tool(ToolType.CLEAVER))) {
                    beheading += 2;
                }
                return beheading;
            }
        }
        return 0;
    }

    private static void drops(LivingDropsEvent event) {
        LivingEntity entity = event.getEntity();
        if (!(entity.level() instanceof ServerLevel level)) {
            return;
        }
        RandomSource random = entity.getRandom();
        Collection<ItemEntity> drops = event.getDrops();
        DamageSource source = event.getSource();
        EntityType<?> type = entity.getType();
        if (random.nextInt(500) == 0 && entity instanceof Enemy && level.dimension() == Level.OVERWORLD) {
            add(drops, entity, new ItemStack(tconstruct.TWorld.MINIATURE_RED_HEART.get()));
        }
        if (type == EntityTypes.WITHER && random.nextInt(5) == 0) {
            add(drops, entity, new ItemStack(tconstruct.TWorld.MINIATURE_RED_HEART.get()));
        }
        int looting = looting(entity, source);
        if (!entity.isBaby() && (type == EntityTypes.COW || type == EntityTypes.CHICKEN)) {
            int amount = random.nextInt(3) + random.nextInt(1 + looting) + random.nextInt(3) + random.nextInt(1 + looting) + 1;
            for (int i = 0; i < amount; i++) {
                add(drops, entity, new ItemStack(type == EntityTypes.COW ? Items.LEATHER : Items.FEATHER));
            }
        }
        if (event.isRecentlyHit()) {
            int beheading = beheading(source);
            if (type == EntityTypes.SKELETON || type == EntityTypes.WITHER_SKELETON) {
                if (beheading > 0 && random.nextInt(100) < beheading * 10) {
                    add(drops, entity, new ItemStack(type == EntityTypes.SKELETON ? Items.SKELETON_SKULL : Items.WITHER_SKELETON_SKULL));
                }
                if (type == EntityTypes.WITHER_SKELETON && random.nextInt(Math.max(1, 5 - looting)) == 0) {
                    add(drops, entity, new ItemStack(TItems.NECROTIC_BONE.get()));
                }
            }
            if (type == EntityTypes.ZOMBIE && beheading > 0 && random.nextInt(100) < beheading * 10) {
                add(drops, entity, new ItemStack(Items.ZOMBIE_HEAD));
            }
            if (type == EntityTypes.CREEPER && beheading > 0 && random.nextInt(100) < beheading * 5) {
                add(drops, entity, new ItemStack(Items.CREEPER_HEAD));
            }
        }
        if (type == EntityTypes.GHAST) {
            add(drops, entity, new ItemStack(Items.GHAST_TEAR));
        }
        if (entity instanceof ServerPlayer player) {
            // "Players drop heads on death" is off: only beheading takes a head
            int beheading = beheading(source);
            if (beheading > 0 && random.nextInt(100) < beheading * 50) {
                ItemStack head = new ItemStack(Items.PLAYER_HEAD);
                head.set(net.minecraft.core.component.DataComponents.PROFILE,
                        net.minecraft.world.item.component.ResolvableProfile.createResolved(player.getGameProfile()));
                add(drops, entity, head);
            }
            PlayerData data = TPlayer.data(player);
            // playerDrops: the level and hunger to restore
            data.level = player.experienceLevel / 2;
            data.hunger = Math.max(6, player.getFoodData().getFoodLevel());
            if (!level.getGameRules().get(GameRules.KEEP_INVENTORY)) {
                for (int i = 0; i < 4; i++) {
                    dropFromPlayer(player, data.armor.removeItemNoUpdate(i));
                }
                for (int i = 0; i < data.knapsack.getContainerSize(); i++) {
                    dropFromPlayer(player, data.knapsack.removeItemNoUpdate(i));
                }
                data.armor.setChanged();
            }
        }
    }

    /** dropPlayerItemWithRandomChoice(stack, true): scattered around the player. */
    private static void dropFromPlayer(ServerPlayer player, ItemStack stack) {
        if (!stack.isEmpty()) {
            player.drop(stack, true, net.minecraft.util.Prediction.SERVER_ONLY);
        }
    }

    // ------------------------------------------------------------------ TEventHandler.onLivingSpawn

    /** 1 in 100 spiders spawns with a creeper on its back. */
    private static void spawn(FinalizeSpawnEvent event) {
        Mob mob = event.getEntity();
        if (mob.getType() != EntityTypes.SPIDER || mob.getRandom().nextInt(100) != 0) {
            return;
        }
        ServerLevel level = event.getLevel().getLevel();
        Mob creeper = EntityTypes.CREEPER.create(level, EntitySpawnReason.JOCKEY);
        if (creeper != null) {
            creeper.snapTo(mob.getX(), mob.getY() + 1.0, mob.getZ(), mob.getYRot(), 0.0F);
            creeper.finalizeSpawn(event.getLevel(), event.getDifficulty(), EntitySpawnReason.JOCKEY, null);
            creeper.startRiding(mob);
        }
    }
}

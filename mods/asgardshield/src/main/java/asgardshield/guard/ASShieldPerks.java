package asgardshield.guard;

import asgardshield.item.GuardSound;
import asgardshield.item.ItemAsgardShield;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.monster.Enderman;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.monster.cubemob.Slime;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.entity.projectile.hurtingprojectile.LargeFireball;
import net.minecraft.world.entity.projectile.hurtingprojectile.SmallFireball;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.phys.Vec3;
import net.minecraft.resources.ResourceKey;

import java.util.ArrayList;
import java.util.List;

/**
 * ASShieldPerks: what a guarded hit costs the shield, and the perk and weakness of each shield. Returns
 * {damage to the shield, 1 if the hit is not guarded}. Damage is whole numbers as in 1.5.2 (the incoming float damage
 * is rounded down before).
 */
public final class ASShieldPerks {
    private ASShieldPerks() {
    }

    public static int[] doASShieldCalc(ItemStack stack, ItemAsgardShield shield, ServerLevel level, Player player, DamageSource source,
            int damageOutput, GuardState state) {
        int damageOut = damageOutput / 2;
        int playerDamageTaken = (int) (damageOut * shield.damageMultiplier() * -1.0F);
        int shieldDamageTaken = damageOut + playerDamageTaken * -1;
        return getASShieldPerk(stack, shield, level, player, source, shieldDamageTaken, state);
    }

    static void playSound(Player player, SoundEvent sound, float volume, float pitch) {
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), sound, SoundSource.PLAYERS, volume, pitch);
    }

    static void playSound(Player player, Holder<SoundEvent> sound, float volume, float pitch) {
        playSound(player, sound.value(), volume, pitch);
    }

    /** damageType "fireball" of 1.5.2. */
    private static boolean fireball(DamageSource source) {
        return source.is(DamageTypes.FIREBALL) || source.is(DamageTypes.UNATTRIBUTED_FIREBALL);
    }

    /** damageType "mob" or "player": a melee hit. */
    static boolean melee(DamageSource source) {
        return source.is(DamageTypes.MOB_ATTACK) || source.is(DamageTypes.MOB_ATTACK_NO_AGGRO) || source.is(DamageTypes.PLAYER_ATTACK);
    }

    public static int[] getASShieldPerk(ItemStack stack, ItemAsgardShield shield, ServerLevel level, Player player, DamageSource source,
            int shieldDamage, GuardState state) {
        int[] shieldData = {shieldDamage, 0};
        Entity attacker = source.getEntity();
        boolean takesExDamage = true;
        RandomSource rand = player.getRandom();
        boolean gilded = shield.gilded();
        int perkChance = rand.nextInt(100) + 1;
        shieldData[0] = applyEnchants(stack, level, player, source, shieldData[0]);
        switch (shield.perk()) {
            case 1 -> {
                // Arrow Catch (only the shooter counts: getEntity() of an arrow is the skeleton) / Fire Damage
                if ((perkChance >= 50 || gilded) && (attacker instanceof AbstractArrow || attacker instanceof AbstractSkeleton)) {
                    player.getInventory().add(new ItemStack(Items.ARROW, 1));
                    // "random.drr"
                    playSound(player, SoundEvents.ITEM_PICKUP, 1.0F, 1.0F / (rand.nextFloat() * 0.4F + 1.2F) + 0.3F);
                }
                if (source.is(DamageTypeTags.IS_FIRE)) {
                    shieldData[0] += 15;
                }
            }
            case 2 -> {
                // Fire Resistant / Explosive Damage
                if (source.is(DamageTypeTags.IS_FIRE)) {
                    shieldData[0] /= gilded ? 2 : 1;
                }
                if (source.is(DamageTypeTags.IS_EXPLOSION)) {
                    shieldData[0] *= 3;
                }
                takesExDamage = false;
            }
            case 3 -> {
                // Explosive Resistant / Water Damage
                if (source.is(DamageTypeTags.IS_EXPLOSION)) {
                    shieldData[0] /= gilded ? 2 : 1;
                }
                if (player.isInWaterOrRain()) {
                    shieldData[0] += 15;
                }
                takesExDamage = false;
            }
            case 4 -> {
                // Projectile Reflect / Reflected Damage
                if (attacker == null) {
                    shieldData[0] *= 2;
                    return shieldData;
                }
                if (perkChance >= 70 || perkChance >= 40 && gilded) {
                    reflect(level, player, source, attacker, shieldData[0], rand);
                    shieldData[0] += 2;
                }
            }
            case 5 -> {
                // Fire Catch / Fatigue
                if (perkChance >= 50 || gilded) {
                    if (fireball(source) && attacker instanceof Blaze) {
                        // 1.5.2 gave a fire block as item, which no longer exists: only the sound and the flames remain
                        fireCatch(level, player, rand);
                    }
                    if (fireball(source) && attacker instanceof Ghast) {
                        player.getInventory().add(new ItemStack(Items.FIRE_CHARGE, 1));
                        fireCatch(level, player, rand);
                    }
                }
                player.causeFoodExhaustion(0.2F);
            }
            case 6 -> {
                // Teleport Displace / Ender Damage (the attacker check of 1.5.2 is always true)
                if (attacker != null && (perkChance >= 80 || perkChance >= 60 && gilded)) {
                    Effects.teleportRandomly(attacker, player);
                }
                if (attacker instanceof Enderman || attacker instanceof EnderDragon) {
                    shieldData[1] = 1;
                }
            }
            case 7 -> {
                // Fear / Brittle Bones
                if (perkChance >= 85 || perkChance >= 60 && gilded) {
                    confuse(level, attacker, player);
                }
                if (perkChance >= 90) {
                    shieldData[0] *= gilded ? 2 : 3;
                }
            }
            case 8 -> {
                // Foulness / Foulness
                if (perkChance >= 80 && attacker instanceof LivingEntity living) {
                    living.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 200, 0));
                }
                if (perkChance >= 95) {
                    player.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 200, 0));
                }
            }
            case 9 -> {
                // Soul Matrix / Soul Eater: a soul of a Soulkeeper or Soul Vessel buys an aura that takes the next hits
                if (state.livingmetalAura <= 0 && perkChance >= 90
                        && consumeEssence(player, "soulkeeper", "soul_vessel", "essence_keeper", "essence_vessel")) {
                    state.livingmetalAura = gilded ? 30 : 20;
                    playSound(player, SoundEvents.PLAYER_BREATH, 1.0F, 1.0F);
                }
            }
            case 10 -> {
                // Blood Matrix / Blood Drinker
                if (state.biomassAura <= 0 && perkChance >= 90
                        && consumeEssence(player, "bloodkeeper", "blood_vessel", "essence_keeper", "essence_vessel")) {
                    state.biomassAura = gilded ? 30 : 20;
                    playSound(player, SoundEvents.PLAYER_BREATH, 1.0F, 1.0F);
                }
            }
            default -> {
            }
        }
        shieldData[0] *= addedExplosionDamage(source, takesExDamage);
        harkenAugments(stack, level, player, shieldData, rand);
        if (shieldData[1] == 0) {
            playGuardSound(player, shield.guardSound());
        }
        return shieldData;
    }

    private static void fireCatch(ServerLevel level, Player player, RandomSource rand) {
        playSound(player, SoundEvents.FLINTANDSTEEL_USE, 1.0F, 1.0F / (rand.nextFloat() * 0.4F + 1.2F) + 0.3F);
        level.levelEvent(LevelEvent.PARTICLES_MOBBLOCK_SPAWN, BlockPos.containing(Math.round(player.getX() - 0.5), Math.round(player.getY() + 0.5),
                Math.round(player.getZ())), 0);
    }

    /** Perk 4: fireballs of blazes and ghasts and arrows fly back. */
    private static void reflect(ServerLevel level, Player player, DamageSource source, Entity attacker, int shieldDamage, RandomSource rand) {
        if (fireball(source) && attacker instanceof Blaze) {
            double dx = attacker.getX() - player.getX();
            double dy = attacker.getBoundingBox().minY + attacker.getBbHeight() / 2.0F - (player.getY() + player.getBbHeight() / 2.0F);
            double dz = attacker.getZ() - player.getZ();
            SmallFireball fireball = new SmallFireball(level, player, new Vec3(dx, dy, dz));
            fireball.setPos(fireball.getX(), player.getY() + player.getBbHeight() / 2.0F + 0.5, fireball.getZ());
            level.addFreshEntity(fireball);
        }
        if (fireball(source) && attacker instanceof Ghast) {
            double dx = attacker.getX() - player.getX();
            double dy = attacker.getBoundingBox().minY + attacker.getBbHeight() / 2.0F - (player.getY() + player.getBbHeight() / 2.0F);
            double dz = attacker.getZ() - player.getZ();
            LargeFireball fireball = new LargeFireball(level, player, new Vec3(dx, dy, dz), 1);
            Vec3 look = player.getViewVector(1.0F);
            fireball.setPos(player.getX() + look.x * 4.0, player.getY() + player.getBbHeight() / 2.0F + 0.5, player.getZ() + look.z * 4.0);
            level.addFreshEntity(fireball);
        }
        if (source.is(DamageTypes.ARROW)) {
            float power = (3.0F * 3.0F + 3.0F * 2.0F) / 3.0F;
            // new EntityArrow(world, player, speed): the speed is a third of the shield damage
            Arrow arrow = new Arrow(level, player, new ItemStack(Items.ARROW), null);
            arrow.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, shieldDamage / 3.0F * 1.5F, 1.0F);
            arrow.pickup = AbstractArrow.Pickup.DISALLOWED;
            playSound(player, SoundEvents.ARROW_SHOOT, 1.0F, 1.0F / (rand.nextFloat() * 0.4F + 1.2F) + power * 0.5F);
            level.addFreshEntity(arrow);
        }
    }

    /** Perk 7: the attacker turns on another creature nearby (if there are at least two). */
    private static void confuse(ServerLevel level, Entity entity, Player player) {
        if (entity instanceof Slime || !(entity instanceof PathfinderMob attacker)) {
            return;
        }
        List<PathfinderMob> creatures = new ArrayList<>();
        for (Entity other : level.getEntities(player, player.getBoundingBox().inflate(10.0))) {
            if (other instanceof PathfinderMob creature) {
                creatures.add(creature);
            }
        }
        if (creatures.size() >= 2) {
            for (int i = 0; i < creatures.size(); i++) {
                attacker.setTarget(creatures.get(i != 0 ? i - 1 : creatures.size() - 1));
                playSound(player, SoundEvents.GHAST_SCREAM, 0.1F, 0.2F);
            }
        }
    }

    /**
     * Perks 9 and 10: one essence of the first Soulkeeper/Soul Vessel (Bloodkeeper/Blood Vessel) of the main inventory;
     * an emptied one becomes an Essence Keeper/Vessel. The fill level is read from the data component
     * harkenscythe_core:essence (an Integer) if the stack has it, else from the durability as in 1.5.2
     * (souls left = max damage - damage).
     */
    static boolean consumeEssence(Player player, String keeper, String vessel, String emptyKeeper, String emptyVessel) {
        Item keeperItem = HarkenScythe.item(keeper);
        Item vesselItem = HarkenScythe.item(vessel);
        if (keeperItem == null && vesselItem == null) {
            return false;
        }
        var items = player.getInventory().getNonEquipmentItems();
        for (int slot = 0; slot < items.size(); slot++) {
            ItemStack stack = items.get(slot);
            if (stack.isEmpty()) {
                continue;
            }
            boolean isKeeper = keeperItem != null && stack.is(keeperItem);
            if (!isKeeper && !(vesselItem != null && stack.is(vesselItem))) {
                continue;
            }
            Item empty = HarkenScythe.item(isKeeper ? emptyKeeper : emptyVessel);
            DataComponentType<Integer> essence = HarkenScythe.essenceComponent();
            boolean emptied;
            if (essence != null && stack.get(essence) instanceof Integer amount) {
                emptied = amount - 1 <= 0;
                if (!emptied) {
                    stack.set(essence, amount - 1);
                }
            } else if (stack.isDamageableItem()) {
                emptied = stack.getDamageValue() + 1 >= stack.getMaxDamage();
                if (!emptied) {
                    stack.setDamageValue(stack.getDamageValue() + 1);
                }
            } else {
                emptied = true;
            }
            if (emptied) {
                player.getInventory().setItem(slot, empty == null ? ItemStack.EMPTY : new ItemStack(empty));
            }
            return true;
        }
        return false;
    }

    static int addedExplosionDamage(DamageSource source, boolean takesExDamage) {
        return takesExDamage && source.is(DamageTypeTags.IS_EXPLOSION) ? 2 : 1;
    }

    /** The Vitality and Sanguinary augments of Harken Scythe on a guarding item. */
    static void harkenAugments(ItemStack stack, ServerLevel level, Player player, int[] data, RandomSource rand) {
        int vitality = HarkenScythe.level(level, stack, HarkenScythe.VITALITY);
        if (vitality > 0 && data[0] != 0 && data[1] == 0) {
            FoodData food = player.getFoodData();
            if (data[0] <= food.getFoodLevel()) {
                int chance = rand.nextInt(100) + 1;
                if (vitality * 5 >= chance) {
                    if (food.getSaturationLevel() > 0.0F) {
                        food.addExhaustion((data[0] + 1) * food.getSaturationLevel());
                    } else {
                        food.addExhaustion(data[0] + 1);
                        data[0] = 0;
                    }
                }
            }
        }
        int sanguinary = HarkenScythe.level(level, stack, HarkenScythe.SANGUINARY);
        if (sanguinary > 0 && data[1] == 0) {
            int chance = rand.nextInt(100) + 1;
            if (sanguinary * 10 >= chance && player.getHealth() < player.getMaxHealth()) {
                player.heal(1.0F);
            }
        }
    }

    private static int level(ServerLevel level, ItemStack stack, ResourceKey<Enchantment> key) {
        return level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).get(key)
                .map(holder -> EnchantmentHelper.getItemEnchantmentLevel(holder, stack)).orElse(0);
    }

    /** applyShieldEnchants / applySwordEnchants: armour enchantments on the guarding item protect it. */
    static int applyEnchants(ItemStack stack, ServerLevel level, Player player, DamageSource source, int damage) {
        int result = damage;
        boolean projectile = source.is(DamageTypeTags.IS_PROJECTILE);
        boolean fire = source.is(DamageTypeTags.IS_FIRE);
        boolean explosion = source.is(DamageTypeTags.IS_EXPLOSION);
        int protection = level(level, stack, Enchantments.PROTECTION);
        if (protection > 0 && (projectile || fire || explosion)) {
            result = (int) (result - result * (protection * 10.0F * 0.01F));
        }
        int projectileProtection = level(level, stack, Enchantments.PROJECTILE_PROTECTION);
        if (projectileProtection > 0 && projectile) {
            result = (int) (result - result * (projectileProtection * 15 * 0.01F));
        }
        int fireProtection = level(level, stack, Enchantments.FIRE_PROTECTION);
        if (fireProtection > 0 && fire) {
            result = (int) (result - result * (fireProtection * 15 * 0.01F));
        }
        int blastProtection = level(level, stack, Enchantments.BLAST_PROTECTION);
        if (blastProtection > 0 && explosion) {
            result = (int) (result - result * (blastProtection * 15 * 0.01F));
        }
        Entity attacker = source.getEntity();
        int thorns = level(level, stack, Enchantments.THORNS);
        if (thorns > 0 && melee(source) && attacker != null) {
            attacker.hurtServer(level, player.damageSources().thorns(player), thorns);
            level.playSound(null, attacker.getX(), attacker.getY(), attacker.getZ(), SoundEvents.THORNS_HIT, attacker.getSoundSource(), 0.5F, 1.0F);
            result += thorns;
        }
        int knockback = level(level, stack, Enchantments.KNOCKBACK);
        if (knockback > 0 && melee(source) && attacker != null) {
            // EnchantmentHelper.getKnockbackModifier: the knockback level of the held (guarding) item
            int j = knockback + 1;
            float yaw = player.getYRot() * Mth.DEG_TO_RAD;
            attacker.push(-Mth.sin(yaw) * j * 0.5F, 0.1, Mth.cos(yaw) * j * 0.5F);
            attacker.needsSync = true;
        }
        int exude = HarkenScythe.level(level, stack, HarkenScythe.EXUDE);
        if (exude > 0 && (source.is(DamageTypes.MAGIC) || source.is(DamageTypes.WITHER))) {
            if (exude * 4 >= player.getRandom().nextInt(100) + 1) {
                result = 0;
            }
        }
        return result;
    }

    static void playGuardSound(Player player, GuardSound sound) {
        playSound(player, sound.sound, sound.volume, sound.pitch);
    }
}

package harkenscythe.event;

import harkenscythe.HSBlocks;
import harkenscythe.HSEntities;
import harkenscythe.HSItems;
import harkenscythe.HarkenScythe;
import harkenscythe.block.BloodSplashBlock;
import harkenscythe.entity.LostSoulEntity;
import harkenscythe.entity.SoulEntity;
import harkenscythe.entity.SpectralPets;
import harkenscythe.item.GlaiveItem;
import harkenscythe.item.ReapingWeaponItem;
import harkenscythe.item.ScytheItem;
import harkenscythe.item.SkullType;
import harkenscythe.item.TalismanItem;
import harkenscythe.item.WeaveArmor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.entity.animal.cow.MushroomCow;
import net.minecraft.world.entity.animal.feline.Cat;
import net.minecraft.world.entity.animal.feline.Ocelot;
import net.minecraft.world.entity.animal.golem.AbstractGolem;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.entity.animal.squid.Squid;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Enderman;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Silverfish;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.monster.cubemob.Slime;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import net.minecraft.world.entity.monster.skeleton.WitherSkeleton;
import net.minecraft.world.entity.monster.spider.CaveSpider;
import net.minecraft.world.entity.monster.spider.Spider;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.monster.zombie.ZombieVillager;
import net.minecraft.world.entity.monster.zombie.ZombifiedPiglin;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

/**
 * EventHookContainerClassHarkenScythe: soul reaping, blood spilling, the augments, the Harvest Moon, the talisman and
 * the soulweave protection. Forge's LivingHurtEvent (before armour) is {@link LivingIncomingDamageEvent}; what 1.5.2
 * computed "after armour" (Vitality, the talisman) uses the damage of {@link LivingDamageEvent.Pre}.
 */
public final class HSEvents {
    /** Sign patterns of setSplashBlockMobCheck, in its order; the last two only replaced air and snow (a bug of 1.5.2). */
    private static final int[][] SPLASH_SIGNS = {{1, 1, 1}, {-1, -1, -1}, {1, -1, -1}, {-1, 1, 1}, {1, 1, -1}, {-1, -1, 1}, {1, -1, 1}, {-1, 1, -1}};

    private HSEvents() {
    }

    public static int level(Level level, ItemStack stack, ResourceKey<Enchantment> key) {
        if (stack.isEmpty()) {
            return 0;
        }
        return level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).get(key)
                .map(holder -> EnchantmentHelper.getItemEnchantmentLevel(holder, stack)).orElse(0);
    }

    /** EnchantmentHSVitality/Exude.getTotalEnchantmentLvl: the levels of all four armour pieces. */
    private static int armorLevel(Player player, ResourceKey<Enchantment> key) {
        int total = 0;
        for (EquipmentSlot slot : new EquipmentSlot[] {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            total += level(player.level(), player.getItemBySlot(slot), key);
        }
        return total;
    }

    /** The Harvest Moon: night of moon phase 4 in the overworld. */
    public static boolean harvestMoon(Level level, BlockPos pos) {
        return level.dimension() == Level.OVERWORLD && !level.isBrightOutside()
                && level.environmentAttributes().getValue(EnvironmentAttributes.MOON_PHASE, pos).index() == 4;
    }

    @SubscribeEvent
    static void livingHurt(LivingIncomingDamageEvent event) {
        LivingEntity victim = event.getEntity();
        Level level = victim.level();
        if (level.isClientSide()) {
            return;
        }
        DamageSource source = event.getSource();
        RandomSource random = level.getRandom();
        if (source.is(DamageTypeTags.IS_PROJECTILE) && source.getEntity() instanceof LivingEntity shooter) {
            int blight = level(level, shooter.getMainHandItem(), HarkenScythe.BLIGHT);
            if (blight > 0) {
                victim.addEffect(new MobEffectInstance(MobEffects.WITHER, 5 * blight * 20, 0));
            }
            int hemorrhage = level(level, shooter.getMainHandItem(), HarkenScythe.HEMORRHAGE);
            if (hemorrhage > 0 && bloodType(victim) != 0) {
                victim.addEffect(new MobEffectInstance(MobEffects.POISON, 5 * hemorrhage * 20, 0));
            }
        }
        if (source.getEntity() instanceof LivingEntity attacker && !attacker.getMainHandItem().isEmpty()) {
            ItemStack weapon = attacker.getMainHandItem();
            if (weapon.getItem() instanceof GlaiveItem glaive && ReapingWeaponItem.isCharged(attacker)) {
                bloodImpale(victim, attacker, glaive.bloodPoolChance());
            }
            int bloodletting = level(level, weapon, HarkenScythe.BLOODLETTING);
            if (bloodletting > 0 && bloodletting * 10 + 1 >= random.nextInt(100) + 1) {
                bloodImpale(victim, attacker, bloodletting - 1);
            }
        }
        if (victim instanceof Player player) {
            float amount = event.getAmount();
            if (harvestMoon(level, player.blockPosition()) && source.getEntity() instanceof Monster) {
                amount *= 3.0F;
            }
            if (WeaveArmor.count(player, false) >= 4 && (player.hasEffect(MobEffects.INSTANT_DAMAGE) || source.is(DamageTypes.INDIRECT_MAGIC))) {
                amount = 0.0F;
            }
            if (source.is(DamageTypes.MAGIC) || source.is(DamageTypes.WITHER)) {
                int exude = armorLevel(player, HarkenScythe.EXUDE);
                if (exude > 0 && exude * 4 >= random.nextInt(100) + 1) {
                    amount = 0.0F;
                }
            }
            if (amount <= 0.0F) {
                event.setCanceled(true);
            } else {
                event.setAmount(amount);
            }
        }
    }

    /** The part of livingHurt that 1.5.2 computed with the damage after armour: Vitality and the talisman. */
    @SubscribeEvent
    static void livingDamage(LivingDamageEvent.Pre event) {
        if (!(event.getEntity() instanceof Player player) || player.level().isClientSide()) {
            return;
        }
        float damage = event.getNewDamage();
        if (damage <= 0.0F) {
            return;
        }
        int vitality = armorLevel(player, HarkenScythe.VITALITY);
        if (vitality > 0 && damage <= player.getFoodData().getFoodLevel() && vitality * 5 >= player.getRandom().nextInt(100) + 1) {
            float saturation = player.getFoodData().getSaturationLevel();
            if (saturation > 0.0F) {
                player.causeFoodExhaustion(damage * saturation);
            } else {
                player.causeFoodExhaustion(damage);
                event.setNewDamage(0.0F);
            }
        }
        if (damage >= player.getHealth() && TalismanItem.activate(player)) {
            event.setNewDamage(0.0F);
        }
    }

    @SubscribeEvent
    static void livingDrops(LivingDropsEvent event) {
        LivingEntity victim = event.getEntity();
        if (!(victim.level() instanceof ServerLevel level) || !(event.getSource().getEntity() instanceof LivingEntity attacker)) {
            return;
        }
        ItemStack weapon = attacker.getMainHandItem();
        if (weapon.isEmpty()) {
            return;
        }
        RandomSource random = level.getRandom();
        int afterlife = level(level, weapon, HarkenScythe.AFTERLIFE);
        if (afterlife > 0 && afterlife * 10 + 1 >= random.nextInt(100) + 1) {
            SpawnEggItem.byId(victim.getType()).ifPresent(egg -> drop(event, new ItemStack(egg)));
        }
        int decapitate = level(level, weapon, HarkenScythe.DECAPITATE);
        if (decapitate > 0 && decapitate * 10 + 1 >= random.nextInt(100) + 1) {
            decapitate(event);
        }
        if (SpectralPets.isHarbinger(attacker)) {
            SpectralPets.hooks.harbingerSoulReap(attacker, victim);
            return;
        }
        if (weapon.getItem() instanceof ScytheItem scythe && ReapingWeaponItem.isCharged(attacker)) {
            soulReaping(level, victim, scythe.isSoulReaper() ? 1 : 0);
            return;
        }
        int soulsteal = level(level, weapon, HarkenScythe.SOULSTEAL);
        if (soulsteal > 0 && soulsteal * 33 + 1 >= random.nextInt(100) + 1) {
            soulReaping(level, victim, 0);
        }
    }

    private static void drop(LivingDropsEvent event, ItemStack stack) {
        LivingEntity e = event.getEntity();
        ItemEntity item = new ItemEntity(e.level(), e.getX(), e.getY(), e.getZ(), stack);
        item.setDefaultPickUpDelay();
        event.getDrops().add(item);
    }

    private static ItemStack skull(SkullType type) {
        return new ItemStack(HSItems.SKULLS.get(type).get());
    }

    /** augmentDecapitate: the head of the victim (vanilla heads where 1.5.2 had them). */
    private static void decapitate(LivingDropsEvent event) {
        LivingEntity e = event.getEntity();
        if (e instanceof AbstractGolem) {
            drop(event, new ItemStack(Blocks.PUMPKIN));
        }
        if (e instanceof AbstractSkeleton) {
            drop(event, new ItemStack(e instanceof WitherSkeleton ? Items.WITHER_SKELETON_SKULL : Items.SKELETON_SKULL));
        }
        if (e instanceof Zombie) {
            if (e instanceof ZombifiedPiglin) {
                drop(event, skull(SkullType.PIGMAN_ZOMBIE));
            } else if (e instanceof ZombieVillager) {
                drop(event, skull(SkullType.VILLAGER_ZOMBIE));
            } else {
                drop(event, new ItemStack(Items.ZOMBIE_HEAD));
            }
        }
        if (e instanceof Creeper) {
            drop(event, new ItemStack(Items.CREEPER_HEAD));
        }
        if (e instanceof Player) {
            drop(event, new ItemStack(Items.PLAYER_HEAD));
        }
        if (e instanceof Spider) {
            drop(event, skull(e instanceof CaveSpider ? SkullType.CAVE_SPIDER : SkullType.SPIDER));
        }
        if (e instanceof Enderman) {
            drop(event, skull(SkullType.ENDERMAN));
        }
        if (e instanceof Villager || e instanceof Witch) {
            drop(event, skull(SkullType.VILLAGER));
        }
        if (e instanceof Cow) {
            drop(event, skull(SkullType.COW));
        }
        if (e instanceof MushroomCow) {
            drop(event, skull(SkullType.MOOSHROOM));
        }
        if (e instanceof Pig) {
            drop(event, skull(SkullType.PIG));
        }
        if (e instanceof Squid) {
            drop(event, skull(SkullType.SQUID));
        }
        if (e instanceof Sheep) {
            drop(event, skull(SkullType.SHEEP));
        }
        if (e instanceof Wolf) {
            drop(event, skull(SkullType.WOLF));
        }
        if (e instanceof Ocelot || e instanceof Cat) {
            drop(event, skull(SkullType.OCELOT));
        }
        if (e instanceof EnderDragon) {
            drop(event, skull(SkullType.ENDER_DRAGON));
        }
        if (e instanceof Bat) {
            drop(event, skull(SkullType.BAT));
        }
        if (e instanceof Chicken) {
            drop(event, skull(SkullType.CHICKEN));
        }
    }

    /**
     * soulReaping: the soul of the victim floats up where it died. Bosses (or 200 health and more) leave a wrathful
     * soul of 40, a Harbinger a culled soul of 10, a player a grieving soul of 1, the rest a soul of 1; times three
     * under the Harvest Moon, plus one for the Soul Reaper. Golems, babies, small slimes, souls and the spectral mobs
     * leave none.
     */
    public static void soulReaping(ServerLevel level, LivingEntity victim, int bonus) {
        int booster = harvestMoon(level, victim.blockPosition()) ? 3 : 1;
        EntityType<SoulEntity> type;
        int worth;
        if (victim instanceof EnderDragon || victim instanceof WitherBoss || victim.getMaxHealth() >= 200.0F) {
            type = HSEntities.SOUL_WRATHFUL.get();
            worth = 40 * (booster + bonus);
        } else {
            if (SpectralPets.isSoulless(victim) || victim instanceof AbstractGolem || victim instanceof LostSoulEntity || victim instanceof SoulEntity
                    || SpectralPets.isSpectralPet(victim) || victim.isBaby()) {
                return;
            }
            if (victim instanceof Slime slime && slime.getSize() < 3) {
                return;
            }
            if (SpectralPets.isHarbinger(victim)) {
                type = HSEntities.SOUL_CULLED.get();
                worth = 10 * (booster + bonus);
            } else if (victim instanceof Player) {
                type = HSEntities.SOUL_GRIEVING.get();
                worth = booster + bonus;
            } else {
                type = HSEntities.SOUL_COMMON.get();
                worth = booster + bonus;
            }
        }
        spawnSoul(level, type, victim, worth);
    }

    /** playAuxSFX(2002) and a new soul at the rounded position of {@code fallen}; also for the Harbinger of part 3. */
    public static SoulEntity spawnSoul(ServerLevel level, EntityType<SoulEntity> type, LivingEntity fallen, int worth) {
        int x = (int) Math.round(fallen.getX() - 0.5);
        int y = (int) Math.round(fallen.getY() + 0.5);
        int z = (int) Math.round(fallen.getZ());
        level.levelEvent(2002, new BlockPos(x, y, z), 0);
        return SoulEntity.spawn(type, level, x, y, z, worth, fallen);
    }

    /** bloodImpale: blood splashes around the victim (not ghasts and withers), with a hit sound at the attacker. */
    public static void bloodImpale(LivingEntity victim, LivingEntity attacker, int bonus) {
        int type = bloodType(victim);
        if (SpectralPets.isSpectralPet(victim) || type == 0) {
            return;
        }
        if (type != 4) {
            splash(victim, type, victim.isBaby(), bonus);
        }
        attacker.level().playSound(null, attacker.getX(), attacker.getY(), attacker.getZ(), SoundEvents.PLAYER_HURT, SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    /** mobBloodCheck: 0 none (blaze, slime, skeleton, golem), 1 red, 2 green (creeper, spider, silverfish), 3 purple (enderman, dragon), 4 none to spill (ghast, wither). */
    public static int bloodType(LivingEntity e) {
        if (e instanceof WitherBoss || e instanceof Ghast) {
            return 4;
        }
        if (e instanceof Enderman || e instanceof EnderDragon) {
            return 3;
        }
        if (e instanceof Creeper || e instanceof Spider || e instanceof Silverfish) {
            return 2;
        }
        if (e instanceof Blaze || e instanceof Slime || e instanceof AbstractSkeleton || e instanceof AbstractGolem) {
            return 0;
        }
        return 1;
    }

    /** setSplashBlockMobCheck with the area 2 x 5 x 2 around the victim, mirrored in all eight directions. */
    private static void splash(LivingEntity target, int type, boolean baby, int bonus) {
        Level level = target.level();
        RandomSource random = level.getRandom();
        int spill = 95 - bonus * 2;
        BlockPos base = BlockPos.containing(target.getX(), target.getY(), target.getZ());
        for (int xch = 0; xch < 2; xch++) {
            for (int ych = 0; ych < 5; ych++) {
                for (int zch = 0; zch < 2; zch++) {
                    int roll = random.nextInt(100) + 1;
                    if (xch == 0 && ych == 0 && zch == 0 && canBlood(level, base, true)) {
                        level.setBlock(base, bloodState(type, baby, bonus, random), Block.UPDATE_CLIENTS);
                    }
                    for (int i = 0; i < SPLASH_SIGNS.length; i++) {
                        if (i > 0) {
                            roll = random.nextInt(100) + 1;
                        }
                        int[] s = SPLASH_SIGNS[i];
                        BlockPos pos = base.offset(s[0] * xch, s[1] * ych, s[2] * zch);
                        if (canBlood(level, pos, i < 6) && roll >= spill) {
                            level.setBlock(pos, bloodState(type, baby, bonus, random), Block.UPDATE_CLIENTS);
                        }
                    }
                }
            }
        }
    }

    /** Air, a snow layer or dried blood (canBloodReplaceBlock) on a block with a solid top. */
    private static boolean canBlood(Level level, BlockPos pos, boolean driedBlood) {
        BlockState state = level.getBlockState(pos);
        boolean free = state.isAir() || state.is(Blocks.SNOW) || driedBlood && BloodSplashBlock.isDried(state);
        BlockPos below = pos.below();
        return free && level.getBlockState(below).isFaceSturdy(level, below, Direction.UP);
    }

    /** bloodRandomizer: babies and 70 % of the rest leave dried blood, the others fresh blood of stage 1-3 (more with a bonus). */
    private static BlockState bloodState(int type, boolean baby, int bonus, RandomSource random) {
        int roll = random.nextInt(100) + 1 - bonus * 5;
        int stage;
        if (baby || roll >= 30) {
            stage = 4;
        } else if (roll >= 20) {
            stage = 3;
        } else if (roll >= 10) {
            stage = 2;
        } else {
            stage = 1;
        }
        return HSBlocks.BLOOD_SPLASH_RESIDUE.get().fromMeta((type - 1) * 4 + stage);
    }

    static int floor(double d) {
        return Mth.floor(d);
    }
}

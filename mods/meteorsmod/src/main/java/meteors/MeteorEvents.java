package meteors;

import meteors.entity.FallingMeteor;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * The game-bus handlers: HandlerMeteorTick (announcing meteors), HandlerPlayerTick (Kreknorite set, Frezarite
 * legs/boots, magnetism), HandlerPlayerBreakSpeed, HandlerAchievement.notifyPickup, CommandKittyAttack, the
 * login sync of ClientHandler and the capture of a meteor's explosion (its destroyed blocks, for the crater).
 */
public final class MeteorEvents {
    private MeteorEvents() {
    }

    static void register() {
        NeoForge.EVENT_BUS.addListener(ServerTickEvent.Post.class, MeteorEvents::serverTick);
        NeoForge.EVENT_BUS.addListener(PlayerTickEvent.Post.class, MeteorEvents::playerTick);
        NeoForge.EVENT_BUS.addListener(PlayerEvent.BreakSpeed.class, MeteorEvents::breakSpeed);
        NeoForge.EVENT_BUS.addListener(LivingIncomingDamageEvent.class, MeteorEvents::fireImmunity);
        NeoForge.EVENT_BUS.addListener(ItemEntityPickupEvent.Post.class, MeteorEvents::pickup);
        NeoForge.EVENT_BUS.addListener(ExplosionEvent.Detonate.class, MeteorEvents::detonate);
        NeoForge.EVENT_BUS.addListener(PlayerEvent.PlayerLoggedInEvent.class, e -> {
            if (e.getEntity() instanceof ServerPlayer player) {
                MeteorNetwork.send(player);
            }
        });
        NeoForge.EVENT_BUS.addListener(RegisterCommandsEvent.class, e -> e.getDispatcher().register(Commands.literal("kittyattack")
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS)).executes(c -> {
                    ServerLevel level = c.getSource().getServer().overworld();
                    MeteorData.get(level.getServer()).kittyAttack(level);
                    return 1;
                })));
    }

    /** Awards one of the mod's advancements (the achievements of HandlerAchievement). */
    public static void grant(ServerPlayer player, String name) {
        AdvancementHolder holder = player.level().getServer().getAdvancements().get(MeteorsMod.id(name));
        if (holder != null) {
            player.getAdvancements().award(holder, "done");
        }
    }

    // ---- HandlerMeteorTick ----

    private static void serverTick(ServerTickEvent.Post event) {
        ServerLevel level = event.getServer().overworld();
        MeteorData data = MeteorData.get(event.getServer());
        RandomSource random = level.getRandom();
        if (data.tickGoal == -1) {
            data.tickGoal = newTickGoal(random);
        }
        data.updateMeteors(level);
        if ((!level.isBrightOutside() || !MeteorsConfig.ONLY_AT_NIGHT.get()) && data.canSpawnNewMeteor()) {
            data.ticks++;
        }
        if (data.ticks >= data.tickGoal) {
            List<ServerPlayer> players = level.players();
            if (data.canSpawnNewMeteor() && !players.isEmpty()) {
                int distance = MeteorsConfig.METEOR_FALL_RADIUS.get();
                int x = random.nextInt(distance);
                int z = random.nextInt(distance);
                if (random.nextBoolean()) {
                    x = -x;
                }
                if (random.nextBoolean()) {
                    z = -z;
                }
                Player player = players.get(random.nextInt(players.size()));
                x = (int) (x + player.getX());
                z = (int) (z + player.getZ());
                if (data.canSpawnNewMeteorAt(x >> 4, z >> 4)) {
                    if (random.nextInt(100) < MeteorsConfig.KITTY_ATTACK_CHANCE.get()) {
                        data.readyNewMeteor(level, x, z, MeteorType.randomSize(random), 2500, MeteorType.KITTY);
                    } else {
                        data.readyNewMeteor(level, x, z, MeteorType.randomSize(random),
                                random.nextInt(MeteorsConfig.randTicksUntilCrash() + 1) + MeteorsConfig.minTicksUntilCrash(), MeteorType.random(random));
                    }
                }
            }
            data.ticks = 0;
            data.tickGoal = newTickGoal(random);
        }
    }

    private static int newTickGoal(RandomSource random) {
        return random.nextInt(MeteorsConfig.randTicksUntilSpawn() + 1) + MeteorsConfig.minTicksUntilSpawn();
    }

    /**
     * ClientProxy.playCrashSound: "meteor.crash" for every player in the overworld, 4 blocks from the player towards
     * the meteor per 128 blocks of distance (in 1.5.2 only the single-player client heard it).
     */
    public static void playCrashSound(ServerLevel level, FallingMeteor meteor) {
        Holder<net.minecraft.sounds.SoundEvent> sound = BuiltInRegistries.SOUND_EVENT.wrapAsHolder(MeteorsMod.METEOR_CRASH.get());
        for (ServerPlayer player : level.players()) {
            double xMod = (meteor.getX() - player.getX()) / 128.0 * 4.0;
            double zMod = (meteor.getZ() - player.getZ()) / 128.0 * 4.0;
            player.connection.send(new ClientboundSoundPacket(sound, SoundSource.AMBIENT, player.getX() + xMod, player.getY() + 1.0,
                    player.getZ() + zMod, 1.0F, 1.0F, level.getRandom().nextLong()));
        }
    }

    // ---- HandlerPlayerTick ----

    private static boolean wearing(Player player, EquipmentSlot slot, Item item) {
        return player.getItemBySlot(slot).is(item);
    }

    public static boolean wearingKreknoriteSet(Player player) {
        return wearing(player, EquipmentSlot.HEAD, MeteorsMod.KREKNORITE_HELMET.get())
                && wearing(player, EquipmentSlot.CHEST, MeteorsMod.KREKNORITE_CHESTPLATE.get())
                && wearing(player, EquipmentSlot.LEGS, MeteorsMod.KREKNORITE_LEGGINGS.get())
                && wearing(player, EquipmentSlot.FEET, MeteorsMod.KREKNORITE_BOOTS.get());
    }

    /** MeteorsMod.isMeteoriteTool. */
    public static boolean isMeteoriteTool(ItemStack stack) {
        return stack.is(MeteorsMod.METEORITE_AXE.get()) || stack.is(MeteorsMod.METEORITE_SHOVEL.get()) || stack.is(MeteorsMod.METEORITE_SWORD.get())
                || stack.is(MeteorsMod.METEORITE_PICKAXE.get()) || stack.is(MeteorsMod.METEORITE_HOE.get());
    }

    /** ArmorEffectController.isWearingMagneticArmor: Magnetization on any armour piece, or any Meteorite armour piece. */
    public static boolean isWearingMagneticArmor(Player player) {
        var magnetization = player.level().registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT)
                .get(MeteorsMod.MAGNETIZATION);
        for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            if (magnetization.isPresent() && EnchantmentHelper.getItemEnchantmentLevel(magnetization.get(), player.getItemBySlot(slot)) > 0) {
                return true;
            }
        }
        return wearing(player, EquipmentSlot.HEAD, MeteorsMod.METEORITE_HELMET.get())
                || wearing(player, EquipmentSlot.CHEST, MeteorsMod.METEORITE_CHESTPLATE.get())
                || wearing(player, EquipmentSlot.LEGS, MeteorsMod.METEORITE_LEGGINGS.get())
                || wearing(player, EquipmentSlot.FEET, MeteorsMod.METEORITE_BOOTS.get());
    }

    private static boolean isMagnetic(Player player) {
        return isMeteoriteTool(player.getMainHandItem()) || isWearingMagneticArmor(player);
    }

    private static void playerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (!(player.level() instanceof ServerLevel level)) {
            return;
        }
        if (wearingKreknoriteSet(player) && player.isOnFire()) {
            // isImmuneToFire: no burning either
            player.clearFire();
        }
        boolean wearingLegs = wearing(player, EquipmentSlot.LEGS, MeteorsMod.FREZARITE_LEGGINGS.get());
        boolean wearingBoots = wearing(player, EquipmentSlot.FEET, MeteorsMod.FREZARITE_BOOTS.get());
        if ((wearingLegs || wearingBoots) && !player.isInWater()) {
            // posY - 2.0 of 1.5.2 was 2 below the eyes (1.62 above the feet)
            int y = Mth.floor(player.getY() + 1.62 - 2.0);
            if (wearingLegs && wearingBoots && player.isSprinting()) {
                int l = Mth.floor(player.getX());
                int k = Mth.floor(player.getZ());
                for (int x = l - 2; x < l + 2; x++) {
                    for (int z = k - 2; z < k + 2; z++) {
                        freeze(level, new BlockPos(x, y, z));
                    }
                }
            } else {
                for (int j = 0; j < 4; j++) {
                    int l = Mth.floor(player.getX() + (j % 2 * 2 - 1) * 0.25F);
                    int k = Mth.floor(player.getZ() + (j / 2 % 2 * 2 - 1) * 0.25F);
                    freeze(level, new BlockPos(l, y, k));
                }
            }
        }
        // updateEntityItem: items within 16 blocks fly to the closest player if that one is magnetic
        for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, player.getBoundingBox().inflate(16.0))) {
            Player closest = level.getNearestPlayer(item, 16.0);
            if (closest == player && isMagnetic(player)) {
                double dx = (player.getX() - item.getX()) / 16.0;
                double dy = (player.getY() + player.getEyeHeight() - item.getY()) / 16.0;
                double dz = (player.getZ() - item.getZ()) / 16.0;
                double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
                double strength = 1.0 - dist;
                if (strength > 0.0) {
                    strength *= strength;
                    item.setDeltaMovement(item.getDeltaMovement().add(dx / dist * strength * 0.1, dy / dist * strength * 0.1, dz / dist * strength * 0.1));
                    item.needsSync = true;
                }
            }
        }
    }

    private static void freeze(ServerLevel level, BlockPos pos) {
        if (level.getBlockState(pos).is(Blocks.WATER)) {
            level.setBlock(pos, Blocks.ICE.defaultBlockState(), 2);
        }
    }

    /** The full Kreknorite set made the wearer isImmuneToFire. */
    private static void fireImmunity(LivingIncomingDamageEvent event) {
        if (event.getEntity() instanceof Player player && event.getSource().is(DamageTypeTags.IS_FIRE) && wearingKreknoriteSet(player)) {
            event.setCanceled(true);
        }
    }

    /** HandlerPlayerBreakSpeed: Frezarite pickaxe, spade and axe dig five times as fast with the eyes under water. */
    private static void breakSpeed(PlayerEvent.BreakSpeed event) {
        ItemStack stack = event.getEntity().getMainHandItem();
        if (event.getEntity().isEyeInFluid(FluidTags.WATER) && (stack.is(MeteorsMod.FREZARITE_PICKAXE.get())
                || stack.is(MeteorsMod.FREZARITE_SHOVEL.get()) || stack.is(MeteorsMod.FREZARITE_AXE.get()))) {
            event.setNewSpeed(event.getOriginalSpeed() * 5.0F);
        }
    }

    /** HandlerAchievement.notifyPickup: materialGather for the three materials, attractedDrop while magnetic. */
    private static void pickup(ItemEntityPickupEvent.Post event) {
        if (!(event.getPlayer() instanceof ServerPlayer player)) {
            return;
        }
        ItemStack stack = event.getOriginalStack();
        if (stack.is(MeteorsMod.METEOR_CHIPS.get()) || stack.is(MeteorsMod.FREZARITE_CRYSTAL.get()) || stack.is(MeteorsMod.KREKNORITE_CHIP.get())) {
            grant(player, "material_gather");
        }
        // the original asked for an item in hand even when the armour was the magnetic part
        if (!player.getMainHandItem().isEmpty() && isMagnetic(player)) {
            grant(player, "attracted_drop");
        }
    }

    // ---- explosion capture ----

    private static @Nullable Vec3 capturing;
    private static final List<BlockPos> CAPTURED = new ArrayList<>();

    /**
     * World.newExplosion(null, x, y, z, power, fiery, true) of EntityMeteor.explode; returns the positions the
     * explosion destroyed (explosion.affectedBlockPositions), which the craters use for snow, creepers and blazes.
     */
    public static List<BlockPos> explode(ServerLevel level, double x, double y, double z, float power, boolean fiery) {
        CAPTURED.clear();
        capturing = new Vec3(x, y, z);
        try {
            level.explode(null, x, y, z, power, fiery, Level.ExplosionInteraction.BLOCK);
        } finally {
            capturing = null;
        }
        return new ArrayList<>(CAPTURED);
    }

    private static void detonate(ExplosionEvent.Detonate event) {
        if (capturing != null && event.getExplosion().center().distanceToSqr(capturing) < 1.0E-6) {
            CAPTURED.addAll(event.getAffectedBlocks());
        }
    }
}

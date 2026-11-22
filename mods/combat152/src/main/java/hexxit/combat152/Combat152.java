package hexxit.combat152;

import net.minecraft.client.AttackIndicatorStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BlocksAttacks;
import net.minecraft.world.item.component.Tool;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;
import net.neoforged.neoforge.event.PlayLevelSoundEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingKnockBackEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.CriticalHitEvent;
import net.neoforged.neoforge.event.entity.player.SweepAttackEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import io.netty.buffer.ByteBuf;

import java.util.List;
import java.util.Optional;

/**
 * Melee combat of Minecraft 1.5.2 ({@code EntityPlayer.attackTargetEntityWithCurrentItem},
 * {@code EntityPlayer.damageEntity}, {@code EntityMob.attackEntityAsMob}, {@code EnchantmentHelper}).
 *
 * <ul>
 *   <li><b>No attack cooldown.</b> Every click dealt full damage. The player's base attack speed is set to the
 *       attribute maximum (1024): the cooldown is 0.02 ticks and the attack strength is always 1. The base attack speed
 *       of weapons and tools ({@link Item#BASE_ATTACK_SPEED_ID}) is removed so tooltips show no "1021.6 Attack Speed".</li>
 *   <li><b>No attack indicator.</b> The HUD reads the strength with partial tick 0, so it would flash for one tick after
 *       a hit; the client keeps the option "Attack Indicator" on Off.</li>
 *   <li><b>No sweep attack</b> and <b>no attack sounds</b> ({@code entity.player.attack.*}), both added in 1.9.</li>
 *   <li><b>Critical hits:</b> falling, not on ground, ladder or vine, not in water, not blind, not riding, target living;
 *       also while sprinting. {@code i += rand.nextInt(i / 2 + 2)} instead of x1.5, before the enchantment bonus.</li>
 *   <li><b>Axes and shovels</b> of Minecraft: axe 3 + material, shovel 1 + material (wood/gold 0, stone/copper 1,
 *       iron 2, diamond 3, netherite 4). Swords, pickaxes and hoes already match.</li>
 *   <li><b>Strength</b> {@code +(3 << amplifier)}, <b>Weakness</b> {@code -(2 << amplifier)} on the attack damage of
 *       every living entity (today +3 and -4 per level): a correcting modifier, see {@link #fixEffects}.</li>
 *   <li><b>Sharpness, Smite, Bane of Arthropods:</b> together {@code floor(2.75 * level)} (Sharpness), {@code
 *       floor(4.5 * level)} against undead / arthropods, the hit gets {@code 1 + rand.nextInt(sum)}; for players and
 *       for mobs. Their 26.3 effects are emptied in {@code data/minecraft/enchantment}; Bane no longer slows (1.9).</li>
 *   <li><b>Attack knockback</b> (sprint + Knockback enchantment, {@code j}): {@code addVelocity(-sin * j * 0.5, 0.1,
 *       cos * j * 0.5)} on top of the hurt knockback, ignoring knockback resistance, instead of today's
 *       {@code knockback()} which halves the target's motion.</li>
 *   <li><b>Blocking with a sword</b> (right click, {@code EnumAction.block}): every sword gets {@code BLOCKS_ATTACKS}
 *       without own reduction (use, 72000 ticks, slow walking and the old sword pose come from vanilla), the damage is
 *       {@code (1 + damage) >> 1} before armor from every side, except damage that bypasses armor. No knockback for the
 *       blocker, no axe disabling, no sounds, no item damage, as in 1.5.2.</li>
 * </ul>
 * Entity reach 3, the 10 ticks miss delay and the hurt invulnerability already work as in 1.5.2.
 *
 * <p><b>World types:</b> in Hexxit Legacy these rules always apply. In Hexxit Integrated (and any other world type)
 * the option {@code integratedCombat152} in {@code config/combat152-startup.toml} switches them; off = the melee
 * combat of 26.3 (cooldown, sweep, vanilla critical hits, Sharpness/Smite/Bane of Arthropods with their vanilla
 * values, no sword blocking). The server tells each client which rules apply ({@link ModePayload}).
 */
@Mod(Combat152.MOD_ID)
public final class Combat152 {
    public static final String MOD_ID = "combat152";
    private static final Identifier EFFECT_FIX = Identifier.fromNamespaceAndPath(MOD_ID, "effects_152");
    /**
     * {@code EnumAction.block} of 1.5.2 for any item (swords here, shields and banners of Better Dungeons): use with
     * the vanilla block pose, no own reduction (recognised by the empty list), the damage is halved in
     * {@link #onIncomingDamage}.
     */
    public static final BlocksAttacks BLOCKING = new BlocksAttacks(0.0F, 0.0F, List.of(),
            new BlocksAttacks.ItemDamageFunction(1.0E9F, 0.0F, 0.0F), Optional.empty(), Optional.empty(), Optional.empty());

    /** The attack running on this thread (server or client): target and 1.5.2 knockback {@code j}. */
    private record PendingAttack(Player player, Entity target, int knockback, float strength) {
    }

    // ---- which rules apply ---------------------------------------------------------------------------------------

    private static final ModConfigSpec CONFIG_SPEC;
    private static final ModConfigSpec.BooleanValue INTEGRATED;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();
        INTEGRATED = b.comment("Melee combat of Minecraft 1.5.2 in worlds that are not Hexxit Legacy (Hexxit Integrated, other world types).",
                        "true = as in the pack (no cooldown, 1.5.2 damage, sword blocking), false = melee combat of 26.3 for all mods.",
                        "Hexxit Legacy always uses the 1.5.2 rules.")
                .define("integratedCombat152", true);
        CONFIG_SPEC = b.build();
    }

    /** Rules of the running server, and of the server this client is connected to. */
    private static volatile boolean serverActive = true;
    private static volatile boolean serverLegacy;
    private static volatile boolean clientActive = true;

    /** Server to client: whether the 1.5.2 rules apply. */
    public record ModePayload(boolean active) implements CustomPacketPayload {
        public static final Type<ModePayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(MOD_ID, "mode"));
        public static final StreamCodec<ByteBuf, ModePayload> CODEC = StreamCodec.composite(
                ByteBufCodecs.BOOL, ModePayload::active, ModePayload::new);

        @Override
        public Type<ModePayload> type() {
            return TYPE;
        }
    }

    /** Whether the 1.5.2 rules apply on this side (server thread or dedicated server: the server's, else the client's). */
    public static boolean active() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null && (FMLEnvironment.getDist() == Dist.DEDICATED_SERVER || server.isSameThread())) {
            return serverActive;
        }
        return clientActive;
    }

    private static boolean active(Entity entity) {
        return entity.level().isClientSide() ? clientActive : serverActive;
    }

    /** Hexxit Legacy: the overworld runs the 1.5.2 generator of worldgen152. */
    private static boolean isLegacy(MinecraftServer server) {
        ChunkGenerator generator = server.overworld().getChunkSource().getGenerator();
        // OldChunkGenerator of worldgen152 (no compile dependency: combat152 also runs without it).
        return generator.getClass().getName().equals("hexxit.worldgen152.OldChunkGenerator");
    }

    /**
     * The switch in "World Options" (single player): saves {@code integratedCombat152} and, outside Hexxit Legacy,
     * applies it at once on the running server and its clients.
     */
    static void setIntegrated(MinecraftServer server, boolean on) {
        INTEGRATED.set(on);
        INTEGRATED.save();
        server.execute(() -> {
            if (!serverLegacy) {
                serverActive = on;
                PacketDistributor.sendToAllPlayers(new ModePayload(on));
                org.slf4j.LoggerFactory.getLogger(MOD_ID).info("Melee combat: {} (switched in World Options)", on ? "1.5.2" : "26.3");
            }
        });
    }

    private static void onServerStarted(ServerStartedEvent event) {
        boolean legacy = isLegacy(event.getServer());
        serverLegacy = legacy;
        serverActive = legacy || INTEGRATED.get();
        org.slf4j.LoggerFactory.getLogger(MOD_ID).info("Melee combat: {} ({})", serverActive ? "1.5.2" : "26.3",
                legacy ? "Hexxit Legacy" : "integratedCombat152 = " + INTEGRATED.get());
    }

    private static final ThreadLocal<PendingAttack> ATTACK = new ThreadLocal<>();

    public Combat152(IEventBus modBus, ModContainer container) {
        container.registerConfig(ModConfig.Type.STARTUP, CONFIG_SPEC);
        modBus.addListener(RegisterPayloadHandlersEvent.class, event -> event.registrar("1").optional()
                .playToClient(ModePayload.TYPE, ModePayload.CODEC, (payload, context) -> clientActive = payload.active()));
        NeoForge.EVENT_BUS.addListener(ServerStartedEvent.class, Combat152::onServerStarted);
        NeoForge.EVENT_BUS.addListener(ServerStoppedEvent.class, e -> {
            serverActive = true;
            serverLegacy = false;
        });
        NeoForge.EVENT_BUS.addListener(PlayerEvent.PlayerLoggedInEvent.class, e -> {
            if (e.getEntity() instanceof ServerPlayer player) {
                PacketDistributor.sendToPlayer(player, new ModePayload(serverActive));
            }
        });
        NeoForge.EVENT_BUS.addListener(PlayerInteractEvent.RightClickItem.class, Combat152::onRightClickItem);
        modBus.addListener(EventPriority.LOWEST, ModifyDefaultComponentsEvent.class, Combat152::onDefaultComponents);
        NeoForge.EVENT_BUS.addListener(ItemAttributeModifierEvent.class, Combat152::onItemAttributes);
        NeoForge.EVENT_BUS.addListener(AttackEntityEvent.class, Combat152::onAttack);
        NeoForge.EVENT_BUS.addListener(CriticalHitEvent.class, Combat152::onCriticalHit);
        NeoForge.EVENT_BUS.addListener(SweepAttackEvent.class, e -> {
            if (active(e.getEntity())) {
                e.setSweeping(false);
            }
        });
        NeoForge.EVENT_BUS.addListener(LivingKnockBackEvent.class, Combat152::onKnockback);
        NeoForge.EVENT_BUS.addListener(LivingIncomingDamageEvent.class, Combat152::onIncomingDamage);
        NeoForge.EVENT_BUS.addListener(EntityTickEvent.Pre.class, e -> {
            if (e.getEntity() instanceof LivingEntity living) {
                fixEffects(living);
                if (living instanceof Player player) {
                    fixAttackSpeed(player);
                }
            }
        });
        NeoForge.EVENT_BUS.addListener(PlayLevelSoundEvent.class, Combat152::onSound);
        if (FMLEnvironment.getDist() == Dist.CLIENT) {
            Client.init();
        }
    }

    private static final Identifier NO_COOLDOWN = Identifier.fromNamespaceAndPath(MOD_ID, "no_cooldown");

    /** No cooldown: attack speed raised to the attribute maximum (1024) while the 1.5.2 rules apply. */
    private static void fixAttackSpeed(Player player) {
        AttributeInstance speed = player.getAttribute(Attributes.ATTACK_SPEED);
        if (speed == null) {
            return;
        }
        boolean on = active(player);
        if (on != speed.hasModifier(NO_COOLDOWN)) {
            if (on) {
                speed.addTransientModifier(new AttributeModifier(NO_COOLDOWN, 1024.0, AttributeModifier.Operation.ADD_VALUE));
            } else {
                speed.removeModifier(NO_COOLDOWN);
            }
        }
    }

    /** Sword blocking only with the 1.5.2 rules. */
    private static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (!active(event.getEntity()) && event.getItemStack().get(DataComponents.BLOCKS_ATTACKS) == BLOCKING) {
            event.setCanceled(true);
            event.setCancellationResult(net.minecraft.world.InteractionResult.PASS);
        }
    }

    // ---- swords block --------------------------------------------------------------------------------------------

    private static void onDefaultComponents(ModifyDefaultComponentsEvent event) {
        event.modifyMatching((item, components) -> isSword(item, components.get(DataComponents.TOOL))
                        && components.get(DataComponents.WEAPON) != null
                        && components.get(DataComponents.BLOCKS_ATTACKS) == null
                        && components.get(DataComponents.CONSUMABLE) == null
                        && components.get(DataComponents.KINETIC_WEAPON) == null,
                (components, context, item) -> components.set(DataComponents.BLOCKS_ATTACKS, BLOCKING));
    }

    /** A sword: mines cobweb (shears do too, but are no weapon) and keeps the default right click. */
    private static boolean isSword(Item item, Tool tool) {
        if (tool == null) {
            return false;
        }
        try {
            if (item.getClass().getMethod("use", Level.class, Player.class, InteractionHand.class).getDeclaringClass() != Item.class) {
                return false;
            }
        } catch (NoSuchMethodException e) {
            return false;
        }
        Holder<net.minecraft.world.level.block.Block> cobweb = Blocks.COBWEB.builtInRegistryHolder();
        for (Tool.Rule rule : tool.rules()) {
            try {
                if (rule.blocks().contains(cobweb)) {
                    return true;
                }
            } catch (RuntimeException ignored) {
                // tag sets are not bound yet
            }
        }
        return false;
    }

    // ---- item attributes -----------------------------------------------------------------------------------------

    private static void onItemAttributes(ItemAttributeModifierEvent event) {
        if (!active()) {
            return;
        }
        event.removeModifier(Attributes.ATTACK_SPEED, Item.BASE_ATTACK_SPEED_ID);
        Identifier id = BuiltInRegistries.ITEM.getKey(event.getItemStack().getItem());
        if (!Identifier.DEFAULT_NAMESPACE.equals(id.getNamespace())) {
            return;
        }
        String path = id.getPath();
        int base;
        if (path.endsWith("_axe")) {
            base = 3;
        } else if (path.endsWith("_shovel")) {
            base = 1;
        } else {
            return;
        }
        int material = switch (path.substring(0, path.lastIndexOf('_'))) {
            case "wooden", "golden" -> 0;
            case "stone", "copper" -> 1;
            case "iron" -> 2;
            case "diamond" -> 3;
            case "netherite" -> 4;
            default -> -1;
        };
        if (material < 0) {
            return;
        }
        // The modifier adds to the fist damage of 1.
        event.replaceModifier(Attributes.ATTACK_DAMAGE,
                new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID, base + material - 1, AttributeModifier.Operation.ADD_VALUE),
                EquipmentSlotGroup.MAINHAND);
    }

    // ---- strength and weakness -----------------------------------------------------------------------------------

    /** Adds (1.5.2 value - 26.3 value) of Strength and Weakness to the attack damage. */
    private static void fixEffects(LivingEntity living) {
        AttributeInstance damage = living.getAttribute(Attributes.ATTACK_DAMAGE);
        if (damage == null) {
            return;
        }
        double fix = 0;
        if (!active(living)) {
            if (damage.getModifier(EFFECT_FIX) != null) {
                damage.removeModifier(EFFECT_FIX);
            }
            return;
        }
        MobEffectInstance strength = living.getEffect(MobEffects.STRENGTH);
        if (strength != null) {
            int amp = strength.getAmplifier();
            fix += (3 << amp) - 3.0 * (amp + 1);
        }
        MobEffectInstance weakness = living.getEffect(MobEffects.WEAKNESS);
        if (weakness != null) {
            int amp = weakness.getAmplifier();
            fix += 4.0 * (amp + 1) - (2 << amp);
        }
        AttributeModifier current = damage.getModifier(EFFECT_FIX);
        if (fix == 0) {
            if (current != null) {
                damage.removeModifier(EFFECT_FIX);
            }
        } else if (current == null || current.amount() != fix) {
            damage.addOrUpdateTransientModifier(new AttributeModifier(EFFECT_FIX, fix, AttributeModifier.Operation.ADD_VALUE));
        }
    }

    // ---- player attack -------------------------------------------------------------------------------------------

    private static void onAttack(AttackEntityEvent event) {
        Player player = event.getEntity();
        int j = level(player, Enchantments.KNOCKBACK, player.getMainHandItem()) + (player.isSprinting() ? 1 : 0);
        ATTACK.set(new PendingAttack(player, event.getTarget(), j, player.getAttackStrengthScale(0.5F)));
    }

    private static void onCriticalHit(CriticalHitEvent event) {
        Player player = event.getEntity();
        if (!active(player)) {
            return;
        }
        fixEffects(player);
        int damage = (int) player.getAttributeValue(Attributes.ATTACK_DAMAGE);
        boolean crit = player.fallDistance > 0.0
                && !player.onGround()
                && !player.onClimbable()
                && !player.isInWater()
                && !player.isMobilityRestricted()
                && !player.isPassenger()
                && event.getTarget() instanceof LivingEntity
                && damage > 0;
        event.setCriticalHit(crit);
        if (crit) {
            int bonus = player.getRandom().nextInt(damage / 2 + 2);
            event.setDamageMultiplier((damage + bonus) / (float) damage);
        }
    }

    /**
     * Replaces the extra knockback of a player's attack ({@code Player.causeExtraKnockback}: strength from sprint and
     * enchantment, direction the player's yaw) by the 1.5.2 push. The hurt knockback (0.4 towards the attacker) stays.
     */
    private static void onKnockback(LivingKnockBackEvent event) {
        PendingAttack attack = ATTACK.get();
        if (attack == null || !active(attack.player()) || attack.target() != event.getEntity() || event.getOriginalStrength() == 0.4F) {
            return;
        }
        float yaw = attack.player().getYRot() * Mth.DEG_TO_RAD;
        double sin = Mth.sin(yaw);
        double cos = Mth.cos(yaw);
        if (Math.abs(event.getOriginalRatioX() - sin) > 1.0E-4 || Math.abs(event.getOriginalRatioZ() + cos) > 1.0E-4) {
            return;
        }
        event.setCanceled(true);
        ATTACK.remove();
        int j = attack.knockback();
        if (j > 0) {
            event.getEntity().push(-sin * j * 0.5F, 0.1, cos * j * 0.5F);
        }
    }

    // ---- damage taken --------------------------------------------------------------------------------------------

    private static void onIncomingDamage(LivingIncomingDamageEvent event) {
        DamageSource source = event.getSource();
        LivingEntity target = event.getEntity();
        // Damage enchantments of a melee hit (player or mob), part of the hit before armor.
        if ((source.is(DamageTypes.PLAYER_ATTACK) || source.is(DamageTypes.MOB_ATTACK) || source.is(DamageTypes.MOB_ATTACK_NO_AGGRO))
                && source.getEntity() instanceof LivingEntity attacker && source.getDirectEntity() == attacker) {
            int bonus = active(attacker) ? enchantmentBonus(attacker, target) : 0;
            if (!active(attacker)) {
                vanillaEnchantments(event, attacker, target);
            } else if (bonus > 0) {
                event.setAmount(event.getAmount() + bonus);
                if (attacker instanceof Player player) {
                    player.magicCrit(target);
                }
            }
        }
        // Blocking (EnumAction.block): (1 + damage) >> 1.
        if (active(target) && target instanceof Player player && event.getAmount() > 0 && !source.is(DamageTypeTags.BYPASSES_ARMOR)) {
            ItemStack blocking = player.getItemBlockingWith();
            if (blocking != null && blocking.get(DataComponents.BLOCKS_ATTACKS) instanceof BlocksAttacks blocks
                    && blocks.damageReductions().isEmpty()) {
                float amount = event.getAmount();
                event.setAmount(Math.min(amount, (float) Math.floor((1.0F + amount) / 2.0F)));
            }
        }
    }

    /**
     * The damage enchantments of 26.3 (their effects are emptied in {@code data/minecraft/enchantment} for the 1.5.2
     * rules): Sharpness {@code 1 + 0.5 * (level - 1)}, Smite and Bane of Arthropods {@code 2.5 * level} against their
     * targets, scaled by the attack strength of a player; Bane slows the target (Slowness IV, 1.5 to
     * {@code 1.5 + 0.5 * (level - 1)} seconds).
     */
    private static void vanillaEnchantments(LivingIncomingDamageEvent event, LivingEntity attacker, LivingEntity target) {
        ItemStack weapon = attacker.getMainHandItem();
        if (weapon.isEmpty()) {
            return;
        }
        float bonus = 0;
        int sharpness = level(attacker, Enchantments.SHARPNESS, weapon);
        if (sharpness > 0) {
            bonus += 1.0F + 0.5F * (sharpness - 1);
        }
        if (target.is(EntityTypeTags.SENSITIVE_TO_SMITE)) {
            bonus += 2.5F * level(attacker, Enchantments.SMITE, weapon);
        }
        int bane = target.is(EntityTypeTags.SENSITIVE_TO_BANE_OF_ARTHROPODS) ? level(attacker, Enchantments.BANE_OF_ARTHROPODS, weapon) : 0;
        bonus += 2.5F * bane;
        if (bonus <= 0) {
            return;
        }
        if (attacker instanceof Player player) {
            PendingAttack attack = ATTACK.get();
            float strength = attack != null && attack.player() == player ? attack.strength() : 1.0F;
            bonus *= strength;
            if (bonus > 0) {
                player.magicCrit(target);
            }
        }
        event.setAmount(event.getAmount() + bonus);
        if (bane > 0) {
            int ticks = 30 + attacker.getRandom().nextInt(10 * (bane - 1) + 1);
            target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, ticks, 3));
        }
    }

    /** {@code EnchantmentHelper.getEnchantmentModifierLiving} of 1.5.2. */
    private static int enchantmentBonus(LivingEntity attacker, LivingEntity target) {
        ItemStack weapon = attacker.getMainHandItem();
        if (weapon.isEmpty()) {
            return 0;
        }
        int sum = Mth.floor(level(attacker, Enchantments.SHARPNESS, weapon) * 2.75F);
        if (target.is(EntityTypeTags.SENSITIVE_TO_SMITE)) {
            sum += Mth.floor(level(attacker, Enchantments.SMITE, weapon) * 4.5F);
        }
        if (target.is(EntityTypeTags.SENSITIVE_TO_BANE_OF_ARTHROPODS)) {
            sum += Mth.floor(level(attacker, Enchantments.BANE_OF_ARTHROPODS, weapon) * 4.5F);
        }
        return sum > 0 ? 1 + attacker.getRandom().nextInt(sum) : 0;
    }

    private static int level(Entity entity, ResourceKey<Enchantment> key, ItemStack stack) {
        if (stack.isEmpty()) {
            return 0;
        }
        return entity.level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).get(key)
                .map(holder -> EnchantmentHelper.getItemEnchantmentLevel(holder, stack))
                .orElse(0);
    }

    private static void onSound(PlayLevelSoundEvent event) {
        if (event.getSound() != null && active()
                && event.getSound().value().location().getPath().startsWith("entity.player.attack.")) {
            event.setCanceled(true);
        }
    }

    private static final class Client {
        /**
         * "World Options" of the pause menu (single player): a switch for the 1.5.2 melee combat, top left (Custom LAN
         * has the top right corner). In Hexxit Legacy it is shown switched on and cannot be changed.
         */
        static void onScreenInit(net.neoforged.neoforge.client.event.ScreenEvent.Init.Post event) {
            if (!(event.getScreen() instanceof net.minecraft.client.gui.screens.WorldOptionsScreen screen)) {
                return;
            }
            var server = Minecraft.getInstance().getSingleplayerServer();
            if (server == null) {
                return;
            }
            boolean legacy = serverLegacy;
            var button = net.minecraft.client.gui.components.CycleButton.onOffBuilder(legacy || INTEGRATED.get())
                    .create(5, 5, 150, 20, net.minecraft.network.chat.Component.translatable("options.combat152.combat"),
                            (b, value) -> setIntegrated(server, value));
            button.setTooltip(net.minecraft.client.gui.components.Tooltip.create(net.minecraft.network.chat.Component.translatable(
                    legacy ? "options.combat152.combat.legacy" : "options.combat152.combat.tooltip")));
            button.active = !legacy;
            event.addListener(button);
        }

        static void init() {
            NeoForge.EVENT_BUS.addListener(net.neoforged.neoforge.client.event.ScreenEvent.Init.Post.class, Client::onScreenInit);
            NeoForge.EVENT_BUS.addListener(net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent.LoggingOut.class,
                    e -> clientActive = true);
            NeoForge.EVENT_BUS.addListener(ClientTickEvent.Post.class, e -> {
                var option = Minecraft.getInstance().options.attackIndicator();
                if (clientActive && option.get() != AttackIndicatorStatus.OFF) {
                    option.set(AttackIndicatorStatus.OFF);
                }
            });
        }
    }
}

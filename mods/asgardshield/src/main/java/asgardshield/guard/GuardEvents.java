package asgardshield.guard;

import asgardshield.AsgardShield;
import asgardshield.item.AbstractGuardItem;
import asgardshield.item.ItemAsgardShield;
import asgardshield.item.ItemZedSword;
import asgardshield.network.GuardStatePayload;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SpellParticleOption;
import net.minecraft.network.protocol.game.ClientboundSetHeldSlotPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityEvent;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.SwingAnimation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

/**
 * The guarding mechanics: EventHookContainerClassAsgardShield (LivingAttackEvent, LivingHurtEvent),
 * mod_AsgardShield.calcDamage, and the game tick of ClientTickHandlerAsgardShield (Guard Gauge, guard break,
 * Vanguard, switching between sword and shield), which runs on the server here.
 */
public final class GuardEvents {
    /** Guard Gauge limit (ItemAsgardShield.getGuardGuageMax). */
    public static final int GAUGE_MAX = 200;

    private GuardEvents() {
    }

    /** isBlocking() with an AsgardShield item: guarding with the item in the main hand. */
    public static boolean isGuarding(Player player) {
        return player.isUsingItem() && player.getUsedItemHand() == InteractionHand.MAIN_HAND
                && player.getUseItem().getItem() instanceof AbstractGuardItem;
    }

    /** Swords that the guard switch treats as swords: "ItemSword" without the shields, giant swords and the toggle list. */
    private static boolean isPlainSword(ItemStack stack) {
        return stack.is(ItemTags.SWORDS) && !(stack.getItem() instanceof AbstractGuardItem) && !stack.is(AsgardShield.HARKENSCYTHE_WEAPONS);
    }

    private static void select(Player player, int slot) {
        player.getInventory().setSelectedSlot(slot);
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.connection.send(new ClientboundSetHeldSlotPacket(slot));
        }
    }

    /** ItemAsgardShield.onPlayerStoppedUsing: back to the first sword of the hotbar. */
    public static void switchToSword(Player player) {
        for (int slot = 0; slot < 9; slot++) {
            if (isPlainSword(player.getInventory().getItem(slot))) {
                select(player, slot);
                return;
            }
        }
    }

    // ------------------------------------------------------------------ damage

    @SubscribeEvent
    public static void incomingDamage(LivingIncomingDamageEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !(player.level() instanceof ServerLevel level)) {
            return;
        }
        GuardState state = player.getData(AsgardShield.GUARD);
        if (isGuarding(player)) {
            if (state.broken) {
                return;
            }
            int guarded = calcDamage(level, event.getSource(), (int) event.getAmount(), player, state);
            if (guarded == 1 || guarded == 2) {
                event.setCanceled(true);
                Entity direct = event.getSource().getDirectEntity();
                if (event.getSource().is(DamageTypeTags.IS_PROJECTILE) && direct != null) {
                    direct.discard();
                }
            }
            return;
        }
        // LivingHurtEvent: holding a shield or giant sword without guarding lowers the damage (rounded down, as in 1.5.2)
        if (player.getMainHandItem().getItem() instanceof AbstractGuardItem item) {
            event.setAmount(Mth.floor(event.getAmount() * item.damageMultiplier()));
        }
    }

    /** mod_AsgardShield.calcDamage: 1 guarded by a shield, 2 by a giant sword, 0 not guarded. */
    static int calcDamage(ServerLevel level, DamageSource source, int damage, ServerPlayer player, GuardState state) {
        if (source.is(DamageTypeTags.BYPASSES_ARMOR)) {
            return 0;
        }
        int i = damage;
        // the auras of the Livingmetal and Biomass shields take the damage the shield would take
        if (state.livingmetalAura > 0) {
            i /= 2;
            int secondHalf = i + i;
            if (state.livingmetalAura < secondHalf) {
                i = secondHalf - state.livingmetalAura;
                state.livingmetalAura = 0;
            }
            if (state.livingmetalAura >= secondHalf) {
                state.livingmetalAura -= secondHalf;
                i = 0;
            }
        }
        if (state.biomassAura > 0) {
            i /= 2;
            int secondHalf = i + i;
            if (state.biomassAura < secondHalf) {
                i = secondHalf - state.biomassAura;
                state.biomassAura = 0;
            }
            if (state.biomassAura >= secondHalf) {
                state.biomassAura -= secondHalf;
                i = 0;
            }
        }
        ItemStack held = player.getMainHandItem();
        if (held.getItem() instanceof ItemAsgardShield shield) {
            int[] result = ASShieldPerks.doASShieldCalc(held, shield, level, player, source, i, state);
            if (result[1] != 0) {
                return 0;
            }
            held.hurtAndBreak(Math.max(0, result[0]), player, EquipmentSlot.MAINHAND);
            return 1;
        }
        if (held.getItem() instanceof ItemZedSword sword) {
            int[] result = ASGiantSwordPerks.doASSwordCalc(held, sword, level, player, source, i);
            held.hurtAndBreak(Math.max(0, result[0]), player, EquipmentSlot.MAINHAND);
            return 2;
        }
        return 0;
    }

    // ------------------------------------------------------------------ right-click with a sword

    /** Guarding with a sword (right-click) switches to the shield of the hotbar (the last one, as in 1.5.2). */
    @SubscribeEvent
    public static void rightClickItem(PlayerInteractEvent.RightClickItem event) {
        Player player = event.getEntity();
        if (event.getLevel().isClientSide() || event.getHand() != InteractionHand.MAIN_HAND || !isPlainSword(event.getItemStack())
                || player.getData(AsgardShield.GUARD).broken) {
            return;
        }
        int shield = -1;
        for (int slot = 0; slot < 9; slot++) {
            if (player.getInventory().getItem(slot).getItem() instanceof ItemAsgardShield) {
                shield = slot;
            }
        }
        if (shield >= 0) {
            select(player, shield);
        }
    }

    // ------------------------------------------------------------------ tick

    /** playerHasShieldItem: a shield in the hotbar or the first slot of the main inventory (slots 0-9). */
    private static boolean hasShield(Player player) {
        for (int slot = 0; slot <= 9; slot++) {
            if (player.getInventory().getItem(slot).getItem() instanceof ItemAsgardShield) {
                return true;
            }
        }
        return false;
    }

    @SubscribeEvent
    public static void playerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !(player.level() instanceof ServerLevel level)) {
            return;
        }
        GuardState state = player.getData(AsgardShield.GUARD);
        boolean creative = player.isCreative() || player.isSpectator();
        ItemStack held = player.getMainHandItem();
        if (!held.isEmpty()) {
            boolean guarding = isGuarding(player);
            if (guarding && held.getItem() instanceof ItemAsgardShield && state.vanguardReady && player.isShiftKeyDown()) {
                vanguard(level, player, state);
            }
            if (guarding && !creative) {
                if (state.gauge < GAUGE_MAX) {
                    state.gildedGauge = held.getItem() instanceof ItemAsgardShield shield && shield.gilded();
                    state.swordGauge = held.getItem() instanceof ItemZedSword;
                    boolean wardActivated = false;
                    int ward = HarkenScythe.level(level, held, HarkenScythe.WARD);
                    if (ward > 0) {
                        state.wardGauge = true;
                        if (ward * 10 + 1 >= player.getRandom().nextInt(100) + 1) {
                            wardActivated = true;
                            state.gauge += 1;
                        }
                    } else {
                        state.wardGauge = false;
                    }
                    if (!wardActivated) {
                        state.gauge += state.gildedGauge ? 2 : 3;
                    }
                }
                if (state.gauge >= GAUGE_MAX && !state.broken) {
                    // guard break
                    state.broken = true;
                    state.breakGauge = true;
                    Item item = player.getUseItem().getItem();
                    player.stopUsingItem();
                    if (item instanceof AbstractGuardItem guardItem) {
                        guardItem.onStoppedGuarding(player);
                    }
                }
            }
            if (player.isDeadOrDying()) {
                state.broken = false;
            }
        }
        if (state.gauge != 0 && !creative) {
            state.gauge -= state.broken ? 2 : 1;
            if (state.gauge <= 60 && state.broken) {
                state.broken = false;
                state.breakGauge = false;
            }
        }
        if (hasShield(player) && !creative) {
            if (state.vanguardCounter < 100 && state.vanguardCount < 7) {
                state.vanguardCounter++;
            }
            if (state.vanguardCounter >= 100 && state.vanguardCount < 7) {
                state.vanguardCount++;
                state.vanguardCounter = 0;
            }
            if (state.vanguardCount >= 7) {
                state.vanguardReady = true;
            }
        }
        GuardStatePayload payload = state.changed();
        if (payload != null) {
            PacketDistributor.sendToPlayer(player, payload);
        }
    }

    /**
     * Vanguard: with a full Vanguard Count, sneaking while guarding with a shield strikes every living thing in
     * sight within 6 blocks (slowness, nausea, weakness III for 10 s and a hit) and halves the Guard Gauge.
     */
    private static void vanguard(ServerLevel level, ServerPlayer player, GuardState state) {
        level.broadcastEntityEvent(player, EntityEvent.POOF);
        RandomSource random = player.getRandom();
        float pitch = -player.getXRot() * Mth.DEG_TO_RAD;
        float yaw = -player.getYRot() * Mth.DEG_TO_RAD;
        for (int i = 0; i < 5; i++) {
            Vec3 velocity = new Vec3((random.nextFloat() - 0.5) * 0.1, Math.random() * 0.1 + 0.1, 0.0).xRot(pitch).yRot(yaw);
            Vec3 pos = new Vec3((random.nextFloat() - 0.5) * 0.3, -random.nextFloat() * 0.6 - 0.3, 0.6).xRot(pitch).yRot(yaw)
                    .add(player.getX(), player.getEyeY(), player.getZ());
            level.sendParticles(SpellParticleOption.create(ParticleTypes.INSTANT_EFFECT, -1, 1.0F), pos.x, pos.y, pos.z, 0, velocity.x, velocity.y + 0.05, velocity.z, 1.0);
        }
        ASShieldPerks.playSound(player, SoundEvents.ITEM_BREAK, 1.0F, 1.0F);
        List<Entity> entities = level.getEntities(player, player.getBoundingBox().inflate(6.0));
        float damage = (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE);
        for (Entity entity : entities) {
            if (entity.isAlive() && entity instanceof LivingEntity living && player.hasLineOfSight(entity) && player.distanceToSqr(entity) < 36.0) {
                living.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 200, 0), player);
                living.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 200, 0), player);
                living.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 200, 2), player);
                // playerController.attackEntity: a hit with the held shield
                living.hurtServer(level, player.damageSources().playerAttack(player), damage);
                player.swing(InteractionHand.MAIN_HAND, SwingAnimation.DEFAULT, true);
            }
        }
        state.gauge /= 2;
        state.vanguardCount = 0;
        state.vanguardReady = false;
    }
}

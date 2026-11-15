package sct.hexxitgear.core;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * PlayerTickHandler and PlayerTracker: every 21 player ticks the worn set is looked up again, in between
 * the buffs of the last found set are applied each tick. Like the original this runs on both sides; potion
 * effects are given by the server, the thief movement is worked out where the player is simulated.
 */
public final class SetBonuses {
    /** The player's set (playerMap of ArmorSet) and the tick counter, one map per side. */
    private static final class State {
        ArmorSet set;
        int tickCounter;
    }

    private static final Map<UUID, State> SERVER = new ConcurrentHashMap<>();
    private static final Map<UUID, State> CLIENT = new ConcurrentHashMap<>();

    private SetBonuses() {
    }

    private static Map<UUID, State> states(Player player) {
        return player.level().isClientSide() ? CLIENT : SERVER;
    }

    /** The set a player was last found wearing (on this side), or null. */
    public static ArmorSet getPlayerArmorSet(Player player) {
        State state = states(player).get(player.getUUID());
        return state == null ? null : state.set;
    }

    /** PlayerTickHandler.tickStart (TickType.PLAYER, before the player moves). */
    @SubscribeEvent
    public static void tickStart(PlayerTickEvent.Pre event) {
        Player player = event.getEntity();
        State state = states(player).computeIfAbsent(player.getUUID(), id -> new State());
        if (state.tickCounter > 20) {
            state.set = ArmorSet.match(player);
            state.tickCounter = 0;
        }
        state.tickCounter++;
        if (state.set != null) {
            state.set.applyBuffs(player);
        }
        if (state.set != ArmorSet.THIEF) {
            ArmorSet.clearThiefSpeed(player);
        }
    }

    /**
     * The air half of the thief buff: jumpMovementFactor 0.1 instead of 0.02 (0.026 sprinting). Today the
     * air speed is not an attribute, so the difference is added the way travel() adds its own: moveRelative
     * with the same input, for the player this game instance moves.
     */
    @SubscribeEvent
    public static void tickEnd(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (!player.level().isClientSide() || !player.isLocalPlayer() || getPlayerArmorSet(player) != ArmorSet.THIEF) {
            return;
        }
        if (player.onGround() || player.getAbilities().flying || player.isInWater() || player.isInLava() || player.isFallFlying()
                || player.isPassenger() || player.isSpectator()) {
            return;
        }
        float vanilla = player.isSprinting() ? 0.025999999F : 0.02F;
        player.moveRelative(ArmorSet.THIEF_AIR_SPEED - vanilla, new Vec3(player.xxa, player.yya, player.zza));
    }

    /** PlayerTracker.onPlayerLogin: look the set up right away. */
    @SubscribeEvent
    public static void login(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        states(player).computeIfAbsent(player.getUUID(), id -> new State()).set = ArmorSet.match(player);
    }

    /** PlayerTracker.onPlayerLogout: ArmorSet.removePlayerArmorSet. */
    @SubscribeEvent
    public static void logout(PlayerEvent.PlayerLoggedOutEvent event) {
        states(event.getEntity()).remove(event.getEntity().getUUID());
    }

    /** The client forgets everything when it leaves a world. */
    public static void clearClient() {
        CLIENT.clear();
    }
}

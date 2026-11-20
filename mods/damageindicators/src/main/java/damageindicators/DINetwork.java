package damageindicators;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * DIEntityWatcher as one payload: 26.3 only tells a client about its own effects, so the portrait of a mob would
 * never show any. Like the original the server therefore sends, every other tick, the effects of the living entities
 * around each player (the original's packet range of 30 blocks), and only when they changed.
 */
public final class DINetwork {
    /** Blocks around the player that are watched (DIConfig.packetrange, "Range" of the pack config). */
    private static final int RANGE = 30;
    private static final int RESEND_TICKS = 100;

    /** Server: what each player was last told, to send nothing while nothing changes. */
    private static final Map<Integer, Sync> LAST = new HashMap<>();
    private static int tickCounter;

    private DINetwork() {
    }

    public record Effect(Holder<MobEffect> effect, int duration) {
        public static final StreamCodec<RegistryFriendlyByteBuf, Effect> CODEC = StreamCodec.composite(
                ByteBufCodecs.holderRegistry(Registries.MOB_EFFECT), Effect::effect,
                ByteBufCodecs.VAR_INT, Effect::duration, Effect::new);
    }

    public record Entry(int entityId, List<Effect> effects) {
        public static final StreamCodec<RegistryFriendlyByteBuf, Entry> CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, Entry::entityId,
                Effect.CODEC.apply(ByteBufCodecs.list()), Entry::effects, Entry::new);
    }

    public record Sync(List<Entry> entries) implements CustomPacketPayload {
        public static final Type<Sync> TYPE = new Type<>(net.minecraft.resources.Identifier
                .fromNamespaceAndPath(DamageIndicatorsMod.MOD_ID, "effects"));
        public static final StreamCodec<RegistryFriendlyByteBuf, Sync> CODEC = StreamCodec.composite(
                Entry.CODEC.apply(ByteBufCodecs.list()), Sync::entries, Sync::new);

        @Override
        public Type<Sync> type() {
            return TYPE;
        }
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToClient(Sync.TYPE, Sync.CODEC, DINetwork::handle);
    }

    private static void handle(Sync payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> damageindicators.client.DIClient.acceptEffects(payload));
    }

    /** Called from the server tick: every other tick, as the original's nextTickSpacing of 2. */
    public static void serverTick(net.minecraft.server.MinecraftServer server) {
        if (++tickCounter % 2 != 0) {
            return;
        }
        boolean resend = tickCounter % RESEND_TICKS == 0;
        for (ServerLevel level : server.getAllLevels()) {
            for (ServerPlayer player : level.players()) {
                List<Entry> entries = new ArrayList<>();
                AABB box = player.getBoundingBox().inflate(RANGE);
                for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, box, x -> x != player && !x.getActiveEffects().isEmpty())) {
                    List<Effect> effects = new ArrayList<>();
                    for (MobEffectInstance instance : e.getActiveEffects()) {
                        if (instance.isVisible() || instance.showIcon()) {
                            effects.add(new Effect(instance.getEffect(), instance.isInfiniteDuration() ? -1 : instance.getDuration()));
                        }
                    }
                    if (!effects.isEmpty()) {
                        entries.add(new Entry(e.getId(), effects));
                    }
                }
                Sync sync = new Sync(entries);
                Sync last = LAST.get(player.getId());
                // the durations tick down every time: compare only which effects are on which entity
                if (resend || last == null || !sameEffects(last, sync)) {
                    LAST.put(player.getId(), sync);
                    PacketDistributor.sendToPlayer(player, sync);
                }
            }
        }
        if (resend) {
            LAST.keySet().removeIf(id -> server.getPlayerList().getPlayers().stream().noneMatch(p -> p.getId() == id));
        }
    }

    private static boolean sameEffects(Sync a, Sync b) {
        if (a.entries().size() != b.entries().size()) {
            return false;
        }
        for (int i = 0; i < a.entries().size(); i++) {
            Entry x = a.entries().get(i);
            Entry y = b.entries().get(i);
            if (x.entityId() != y.entityId() || x.effects().size() != y.effects().size()) {
                return false;
            }
            for (int j = 0; j < x.effects().size(); j++) {
                if (!Objects.equals(x.effects().get(j).effect(), y.effects().get(j).effect())) {
                    return false;
                }
            }
        }
        return true;
    }
}

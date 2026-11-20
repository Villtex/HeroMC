package legendgear;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * The four particles of CommonProxy (EntityMagicRuneFX, EntityMagicScrambleFX, EntityFireSwirlFX, EntitySparkleFX), each
 * with the scale the original passed. {@link #rune} and friends work like proxy.addRuneParticle on the client and send the
 * particle to the players nearby on the server (1.5.2 called most of them on the client only).
 */
public final class LGParticles {
    static final DeferredRegister<ParticleType<?>> PARTICLES = DeferredRegister.create(Registries.PARTICLE_TYPE, LegendGear.MOD_ID);

    public enum Kind { RUNE, SCRAMBLE, FLARE, SPARKLE }

    /** A particle of one of the four kinds with its scale. */
    public record Options(Kind kind, float scale) implements ParticleOptions {
        @Override
        public ParticleType<?> getType() {
            return switch (this.kind) {
                case RUNE -> RUNE.get();
                case SCRAMBLE -> SCRAMBLE.get();
                case FLARE -> FLARE.get();
                case SPARKLE -> SPARKLE.get();
            };
        }
    }

    private static DeferredHolder<ParticleType<?>, ParticleType<Options>> type(String name, Kind kind) {
        return PARTICLES.register(name, () -> new ParticleType<Options>(false) {
            private final MapCodec<Options> codec = com.mojang.serialization.Codec.FLOAT.fieldOf("scale").xmap(s -> new Options(kind, s), Options::scale);
            private final StreamCodec<RegistryFriendlyByteBuf, Options> streamCodec = ByteBufCodecs.FLOAT.<RegistryFriendlyByteBuf>cast()
                    .map(s -> new Options(kind, s), Options::scale);

            @Override
            public MapCodec<Options> codec() {
                return this.codec;
            }

            @Override
            public StreamCodec<? super RegistryFriendlyByteBuf, Options> streamCodec() {
                return this.streamCodec;
            }
        });
    }

    public static final DeferredHolder<ParticleType<?>, ParticleType<Options>> RUNE = type("rune", Kind.RUNE);
    public static final DeferredHolder<ParticleType<?>, ParticleType<Options>> SCRAMBLE = type("scramble", Kind.SCRAMBLE);
    public static final DeferredHolder<ParticleType<?>, ParticleType<Options>> FLARE = type("flare", Kind.FLARE);
    public static final DeferredHolder<ParticleType<?>, ParticleType<Options>> SPARKLE = type("sparkle", Kind.SPARKLE);

    private LGParticles() {
    }

    static void register(IEventBus modBus) {
        PARTICLES.register(modBus);
    }

    public static void rune(Level level, double x, double y, double z, double vx, double vy, double vz, float scale) {
        LGFx.particle(level, new Options(Kind.RUNE, scale), x, y, z, vx, vy, vz);
    }

    public static void scramble(Level level, double x, double y, double z, double vx, double vy, double vz, float scale) {
        LGFx.particle(level, new Options(Kind.SCRAMBLE, scale), x, y, z, vx, vy, vz);
    }

    public static void flare(Level level, double x, double y, double z, double vx, double vy, double vz, float scale) {
        LGFx.particle(level, new Options(Kind.FLARE, scale), x, y, z, vx, vy, vz);
    }

    public static void sparkle(Level level, double x, double y, double z, double vx, double vy, double vz, float scale) {
        LGFx.particle(level, new Options(Kind.SPARKLE, scale), x, y, z, vx, vy, vz);
    }

    /** Client-only particles of 1.5.2 (inside {@code if (world.isRemote)}): nothing on the server. */
    public static boolean client(Level level) {
        return level.isClientSide() && !(level instanceof ServerLevel);
    }
}

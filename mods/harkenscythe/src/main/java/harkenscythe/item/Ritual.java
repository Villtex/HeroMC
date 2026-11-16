package harkenscythe.item;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/** The particle rings of ItemHSAmulet and ItemHSBook (only shown on the client, as in 1.5.2). */
final class Ritual {
    private Ritual() {
    }

    static void particle(Level level, ParticleOptions particle, Player player, double dx, double dy, double dz) {
        if (level.isClientSide()) {
            level.addParticle(particle, player.getX() + dx, player.getY() + dy, player.getZ() + dz, 0.0, 0.0, 0.0);
        }
    }

    /** amuletEffectText: four enchanting glyphs around the player. */
    static void text(Level level, Player player) {
        particle(level, ParticleTypes.ENCHANT, player, 0.0, 0.0, 0.4);
        particle(level, ParticleTypes.ENCHANT, player, 0.4, 0.0, 0.0);
        particle(level, ParticleTypes.ENCHANT, player, 0.0, 0.0, -0.4);
        particle(level, ParticleTypes.ENCHANT, player, -0.4, 0.0, 0.0);
    }

    /** amuletEffectText2. */
    static void text2(Level level, Player player) {
        particle(level, ParticleTypes.ENCHANT, player, 0.2, 0.2, 0.2);
        particle(level, ParticleTypes.ENCHANT, player, 0.2, 0.2, -0.2);
        particle(level, ParticleTypes.ENCHANT, player, -0.2, 0.2, 0.2);
        particle(level, ParticleTypes.ENCHANT, player, -0.2, 0.2, -0.2);
    }
}

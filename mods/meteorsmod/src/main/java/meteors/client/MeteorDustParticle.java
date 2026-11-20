package meteors.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;

/**
 * EntityMeteordustFX / EntityFrezaDustFX (a reddust particle in fixed colours: 0.09 grey, 0.2/1.0/0.8 icy) and
 * EntityMeteorShieldParticleFX (a coloured spark per meteor type; the cells of the mod's particles.png are the generic
 * puffs here, tinted).
 */
public class MeteorDustParticle extends SingleQuadParticle {
    private final SpriteSet sprites;
    private final float baseSize;

    MeteorDustParticle(ClientLevel level, double x, double y, double z, double xd, double yd, double zd, SpriteSet sprites, float r, float g, float b) {
        super(level, x, y, z, 0, 0, 0, sprites.first());
        this.sprites = sprites;
        this.xd = this.xd * 0.1F + xd;
        this.yd = this.yd * 0.1F + yd;
        this.zd = this.zd * 0.1F + zd;
        this.friction = 0.96F;
        float shade = (float) Math.random() * 0.4F + 0.6F;
        this.setColor(r * shade, g * shade, b * shade);
        this.quadSize *= 0.75F;
        this.baseSize = this.quadSize;
        this.lifetime = (int) (8.0 / (Math.random() * 0.8 + 0.2));
        this.setSpriteFromAge(sprites);
    }

    @Override
    protected Layer getLayer() {
        return Layer.OPAQUE;
    }

    @Override
    public float getQuadSize(float partial) {
        float f = Math.min(1.0F, (this.age + partial) / this.lifetime * 32.0F);
        return this.baseSize * f;
    }

    @Override
    public void tick() {
        super.tick();
        this.setSpriteFromAge(this.sprites);
    }

    public record Provider(SpriteSet sprites, float r, float g, float b) implements ParticleProvider<SimpleParticleType> {
        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xd, double yd, double zd,
                RandomSource random) {
            return new MeteorDustParticle(level, x, y, z, xd, yd, zd, this.sprites, this.r, this.g, this.b);
        }
    }
}

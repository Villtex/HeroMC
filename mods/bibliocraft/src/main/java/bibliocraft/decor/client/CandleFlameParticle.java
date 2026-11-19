package bibliocraft.decor.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.RandomSource;

/**
 * EntityCandleFX: the flame texture (index 48), a tenth of the random motion plus the given one, no collision,
 * friction 0.96, lifetime 4 + 8 / (0.2..1.0), size shrinking as 0.3 - 0.5 t^2 of the start size, block light rising
 * towards 15 with its age. The jitter of the position it worked out was thrown away, so it starts exactly there.
 */
public class CandleFlameParticle extends SingleQuadParticle {
    private final float flameScale;

    protected CandleFlameParticle(ClientLevel level, double x, double y, double z, double xd, double yd, double zd, TextureAtlasSprite sprite) {
        super(level, x, y, z, xd, yd, zd, sprite);
        this.friction = 0.96F;
        this.xd = this.xd * 0.01F + xd;
        this.yd = this.yd * 0.01F + yd;
        this.zd = this.zd * 0.01F + zd;
        this.flameScale = this.quadSize;
        this.rCol = this.gCol = this.bCol = 1.0F;
        this.lifetime = (int) (8.0 / (this.random.nextFloat() * 0.8 + 0.2)) + 4;
        this.hasPhysics = false;
    }

    @Override
    protected SingleQuadParticle.Layer getLayer() {
        return SingleQuadParticle.Layer.OPAQUE;
    }

    @Override
    public void move(double xa, double ya, double za) {
        this.setBoundingBox(this.getBoundingBox().move(xa, ya, za));
        this.setLocationFromBoundingbox();
    }

    @Override
    public float getQuadSize(float a) {
        float t = (this.age + a) / this.lifetime;
        return Math.max(0.0F, this.flameScale * (0.3F - t * t * 0.5F));
    }

    @Override
    public int getLightCoords(float a) {
        return LightCoordsUtil.addSmoothBlockEmission(super.getLightCoords(a), (this.age + a) / this.lifetime);
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType options, ClientLevel level, double x, double y, double z, double xd, double yd, double zd,
                RandomSource random) {
            return new CandleFlameParticle(level, x, y, z, xd, yd, zd, this.sprites.get(random));
        }
    }
}

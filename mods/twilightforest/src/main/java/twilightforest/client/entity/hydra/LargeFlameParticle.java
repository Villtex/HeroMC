package twilightforest.client.entity.hydra;

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
 * EntityTFLargeFlameFX ("largeflame"), the hydra's flame breath: the flame texture (index 48) at five times the
 * normal size, shrinking to half towards its end, a hundredth of the random motion plus the given one, rising 0.004
 * per tick, friction 0.96, colliding with blocks, lifetime 4 + 8 / (0.2..1.0), getting brighter with its age.
 */
public class LargeFlameParticle extends SingleQuadParticle {
    private final float flameScale;

    protected LargeFlameParticle(ClientLevel level, double x, double y, double z, double xd, double yd, double zd, TextureAtlasSprite sprite) {
        super(level, x, y, z, xd, yd, zd, sprite);
        this.friction = 0.96F;
        this.gravity = -0.1F;
        this.xd = this.xd * 0.01F + xd;
        this.yd = this.yd * 0.01F + yd;
        this.zd = this.zd * 0.01F + zd;
        this.quadSize *= 5.0F;
        this.flameScale = this.quadSize;
        this.rCol = this.gCol = this.bCol = 1.0F;
        this.lifetime = (int) (8.0 / (Math.random() * 0.8 + 0.2)) + 4;
    }

    @Override
    protected SingleQuadParticle.Layer getLayer() {
        return SingleQuadParticle.Layer.OPAQUE;
    }

    @Override
    public float getQuadSize(float a) {
        float t = (this.age + a) / this.lifetime;
        return this.flameScale * (1.0F - t * t * 0.5F);
    }

    @Override
    public int getLightCoords(float a) {
        return LightCoordsUtil.addSmoothBlockEmission(super.getLightCoords(a), Math.max(0.0F, Math.min(1.0F, (this.age + a) / this.lifetime)));
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xd, double yd, double zd,
                RandomSource random) {
            return new LargeFlameParticle(level, x, y, z, xd, yd, zd, this.sprites.get(random));
        }
    }
}

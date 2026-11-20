package twilightforest.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;

/**
 * EntityTFTinyFirefly / RenderTFTinyFirefly: a glow of 10 x 10 pixels of firefly-tiny.png (u 20..30, v 0..10),
 * scaled 0.2..0.8, half a block above its position; it brightens and fades again over 10..30 ticks.
 */
public class FireflyParticle extends SingleQuadParticle {
    private final int halfLife;

    protected FireflyParticle(ClientLevel level, double x, double y, double z, TextureAtlasSprite sprite) {
        super(level, x, y + 0.5, z, sprite);
        this.lifetime = 10 + this.random.nextInt(21);
        this.halfLife = this.lifetime / 2;
        float glowSize = 0.2F + this.random.nextFloat() * 0.6F;
        this.quadSize = 5.0F / 16.0F * glowSize;
        this.hasPhysics = false;
        this.gravity = 0.0F;
        this.xd = 0.0;
        this.yd = 0.0;
        this.zd = 0.0;
        this.rCol = this.gCol = this.bCol = 1.0F;
        this.alpha = 0.0F;
    }

    @Override
    public void tick() {
        super.tick();
        int left = this.lifetime - this.age;
        this.alpha = left <= this.halfLife ? (float) left / this.halfLife : 1.0F - ((float) left - this.halfLife) / this.halfLife;
        this.alpha = Math.max(0.0F, Math.min(1.0F, this.alpha));
    }

    @Override
    protected SingleQuadParticle.Layer getLayer() {
        return SingleQuadParticle.Layer.TRANSLUCENT;
    }

    @Override
    protected int getLightCoords(float partialTicks) {
        return 0xF000F0;
    }

    @Override
    protected float getU0() {
        return this.sprite.getU(20.0F / 64.0F);
    }

    @Override
    protected float getU1() {
        return this.sprite.getU(30.0F / 64.0F);
    }

    @Override
    protected float getV0() {
        return this.sprite.getV(0.0F);
    }

    @Override
    protected float getV1() {
        return this.sprite.getV(10.0F / 32.0F);
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType options, ClientLevel level, double x, double y, double z, double xd, double yd, double zd,
                RandomSource random) {
            return new FireflyParticle(level, x, y, z, this.sprites.get(random));
        }
    }
}

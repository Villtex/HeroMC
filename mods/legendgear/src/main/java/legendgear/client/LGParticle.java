package legendgear.client;

import legendgear.LGParticles;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.util.RandomSource;

/**
 * EntityMagicRuneFX (enchanting glyphs, colour cycling, 30 ticks), EntityMagicScrambleFX (glyphs changing every tick,
 * 15 ticks), EntityFireSwirlFX (smoke frames 7..0 in fire colours, 7 ticks) and EntitySparkleFX (a shrinking yellow
 * spark, 6 ticks); all at full brightness, without collision.
 */
public class LGParticle extends SingleQuadParticle {
    private final LGParticles.Kind kind;
    private final SpriteSet sprites;
    private final float baseScale;
    private double hue;

    LGParticle(ClientLevel level, double x, double y, double z, double vx, double vy, double vz, LGParticles.Options options, SpriteSet sprites) {
        super(level, x, y, z, vx, vy, vz, sprites.first());
        this.xd = vx;
        this.yd = vy;
        this.zd = vz;
        this.setPos(x, y, z);
        this.kind = options.kind();
        this.sprites = sprites;
        this.baseScale = options.scale();
        this.hasPhysics = false;
        this.friction = 1.0F;
        this.gravity = 0.0F;
        this.hue = Math.random() * Math.PI * 2.0;
        this.lifetime = switch (this.kind) {
            case RUNE -> 30;
            case SCRAMBLE -> 15;
            case FLARE -> 7;
            case SPARKLE -> 6;
        };
        this.quadSize = 0.1F * (this.kind == LGParticles.Kind.RUNE || this.kind == LGParticles.Kind.SCRAMBLE ? this.baseScale : 1.5F * this.baseScale);
        if (this.kind != LGParticles.Kind.FLARE) {
            this.setSprite(sprites.get(this.random));
        }
        this.update();
    }

    @Override
    protected Layer getLayer() {
        return Layer.TRANSLUCENT;
    }

    @Override
    public int getLightCoords(float partialTick) {
        return 0xF000F0;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.age++ >= this.lifetime) {
            this.remove();
            return;
        }
        this.setPos(this.x + this.xd, this.y + this.yd, this.z + this.zd);
        this.update();
    }

    private void update() {
        float fresh = 1.0F - this.age * 1.0F / this.lifetime;
        switch (this.kind) {
            case RUNE, SCRAMBLE -> {
                this.hue += 0.3;
                float r = (float) Math.sin(this.hue) / 2.0F + 0.5F;
                float g = (float) Math.sin(this.hue + Math.PI * 2.0 / 3.0) / 2.0F + 0.5F;
                float b = (float) Math.sin(this.hue - Math.PI * 2.0 / 3.0) / 2.0F + 0.5F;
                this.setAlpha(Math.max(0.0F, fresh));
                if (this.kind == LGParticles.Kind.RUNE) {
                    this.setColor(fresh + (1 - fresh) * r, fresh + (1 - fresh) * g, fresh + (1 - fresh) * b);
                } else {
                    this.setSprite(this.sprites.get(this.random));
                    this.setColor(Math.max(fresh * 2.0F - 1.0F, 0.0F), Math.min(fresh * 2.0F, 1.0F), 1.0F);
                }
            }
            case FLARE -> {
                this.setSpriteFromAge(this.sprites);
                this.setColor(Math.min(fresh * 3.0F, 1.0F), Math.max(Math.min(fresh * 3.0F - 1.0F, 1.0F), 0.0F),
                        Math.max(Math.min(fresh * 3.0F - 2.0F, 1.0F), 0.0F));
            }
            case SPARKLE -> {
                this.setColor(1.0F, 1.0F, fresh);
                this.quadSize = 0.15F * fresh * this.baseScale;
            }
        }
    }

    public static class Provider implements ParticleProvider<LGParticles.Options> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(LGParticles.Options options, ClientLevel level, double x, double y, double z, double vx, double vy, double vz,
                RandomSource random) {
            return new LGParticle(level, x, y, z, vx, vy, vz, options, this.sprites);
        }
    }
}

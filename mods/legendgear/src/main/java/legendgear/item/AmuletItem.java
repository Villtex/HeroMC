package legendgear.item;

import legendgear.LGFx;
import legendgear.LGParticles;
import legendgear.LGSounds;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * AeroAmulet, GeoAmulet, PyroAmulet: 50 uses, always glowing, held up like a shield (EnumAction.block halves damage,
 * see combat152). The geo amulet turns a fall into a quake and the pyro amulet heals fire damage
 * (ForgeEventHooksHandler); the aero amulet throws back every arrow that flies within 2 blocks, at 1.5 times its
 * damage and critical, for one use per point of damage.
 */
public class AmuletItem extends Item {
    public enum Kind { AERO, GEO, PYRO }

    private final Kind kind;

    public AmuletItem(Properties properties, Kind kind) {
        super(properties);
        this.kind = kind;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.BLOCK;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        player.startUsingItem(hand);
        return InteractionResult.CONSUME;
    }

    @Override
    public void onUseTick(Level level, LivingEntity player, ItemStack stack, int count) {
        if (this.kind != Kind.AERO) {
            return;
        }
        if (LGParticles.client(level)) {
            for (int i = 0; i < 3; i++) {
                double azimuth = count * 0.3 + i * Math.PI * 2.0 / 3.0;
                double r = 2.0;
                double elevation = Math.sin(count * 0.13 + i * Math.PI * 2.0 / 3.0);
                double y = Math.sin(elevation) * r + player.getY();
                double out = Math.cos(elevation);
                double x = Math.sin(azimuth) * out * r + player.getX();
                double z = Math.cos(azimuth) * out * r + player.getZ();
                LGParticles.rune(level, x, y, z, level.getRandom().nextGaussian() * 0.002, level.getRandom().nextGaussian() * 0.002,
                        level.getRandom().nextGaussian() * 0.002, 1.0F);
            }
        }
        for (Entity e : level.getEntities(player, player.getBoundingBox().inflate(2.0))) {
            if (e instanceof AbstractArrow arrow && arrow.getOwner() != player) {
                Vec3 moved = new Vec3(arrow.getX() - arrow.xOld, arrow.getY() - arrow.yOld, arrow.getZ() - arrow.zOld);
                if (moved.length() > 0.1) {
                    arrow.setDeltaMovement(arrow.getDeltaMovement().scale(-1.0));
                    arrow.needsSync = true;
                    arrow.setOwner(player);
                    double power = ArrowDamage.get(arrow);
                    LGFx.damage(stack, (int) power, player);
                    arrow.setBaseDamage(power * 1.5);
                    arrow.setCritArrow(true);
                    LGFx.sound(arrow, LGSounds.REPEL.get(), 1.0F, 0.8F + level.getRandom().nextFloat() * 0.4F);
                    if (LGParticles.client(level)) {
                        Vec3 m = arrow.getDeltaMovement();
                        LGParticles.scramble(level, arrow.getX(), arrow.getY(), arrow.getZ(), m.x * 0.2, m.y * 0.2, m.z * 0.2, 6.0F);
                    }
                }
            }
        }
    }

    public Kind kind() {
        return this.kind;
    }
}

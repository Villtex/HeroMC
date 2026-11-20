package legendgear.item;

import legendgear.LGFx;
import legendgear.LGParticles;
import legendgear.LGSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

/**
 * MagicMirror: remembers the last spot under the open sky where its holder stood ("lastSkyX/Y/Z"; the item damage 1 of
 * 1.5.2, "outdoors", is the boolean "outdoors" here). Held up indoors for 70 ticks in a world where one can respawn, it
 * carries the holder back to that spot (moved up while it is inside a block). For 60 ticks after a warp it stays lit.
 */
public class MagicMirrorItem extends Item {
    public static final int USE_TIME = 70;

    public MagicMirrorItem(Properties properties) {
        super(properties);
    }

    private static CompoundTag data(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data == null ? new CompoundTag() : data.copyTag();
    }

    public static boolean outdoors(ItemStack stack) {
        return data(stack).getBooleanOr("outdoors", false);
    }

    private static boolean isOutdoors(Entity player, Level level) {
        return level.canSeeSky(BlockPos.containing(player.getX(), player.getY() + 2.1, player.getZ()));
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return USE_TIME;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.BLOCK;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!outdoors(stack) && data(stack).contains("lastSkyX") && level.dimension() == Level.OVERWORLD) {
            player.startUsingItem(hand);
            return InteractionResult.CONSUME;
        }
        LGFx.sound(player, SoundEvents.FIRE_EXTINGUISH, 0.5F, 0.5F);
        return InteractionResult.FAIL;
    }

    @Override
    public boolean releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        return false;
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity owner, @Nullable EquipmentSlot slot) {
        if (!(owner instanceof Player player)) {
            return;
        }
        CompoundTag nbt = data(stack);
        boolean lit;
        if (isOutdoors(player, level)) {
            nbt.putDouble("lastSkyX", player.getX());
            nbt.putDouble("lastSkyY", player.getY());
            nbt.putDouble("lastSkyZ", player.getZ());
            lit = true;
        } else {
            lit = level.getGameTime() < nbt.getLongOr("lastwarp", 0L) + 60L && nbt.contains("lastSkyX");
        }
        nbt.putBoolean("outdoors", lit);
        if (!nbt.equals(data(stack))) {
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(nbt));
            // the lit icon of 1.5.2 (item damage 1) for the item model
            stack.set(DataComponents.CUSTOM_MODEL_DATA, new net.minecraft.world.item.component.CustomModelData(java.util.List.of(),
                    java.util.List.of(lit), java.util.List.of(), java.util.List.of()));
        }
    }

    @Override
    public void onUseTick(Level level, LivingEntity player, ItemStack stack, int time) {
        int dur = USE_TIME - time;
        float progress = dur * 1.0F / USE_TIME;
        if (LGParticles.client(level)) {
            double theta = Math.PI * 6 * progress;
            double r = 2.0F * (1.0F - progress);
            for (int i = 0; i < 3; i++) {
                LGParticles.rune(level, player.getX() + Math.cos(theta) * r, player.getY(), player.getZ() + Math.sin(theta) * r, 0.0, 0.1, 0.0, 1.5F);
                theta += Math.PI * 2.0 / 3.0;
            }
        }
        if (dur % 8 == 0) {
            LGFx.sound(player, LGSounds.SINE.get(), 0.3F, 0.2F * (2 + dur / 8));
        }
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity player) {
        if (!isOutdoors(player, level) && level.dimension() == Level.OVERWORLD && level instanceof ServerLevel server) {
            CompoundTag nbt = data(stack);
            double tx = nbt.getDoubleOr("lastSkyX", player.getX());
            double ty = nbt.getDoubleOr("lastSkyY", player.getY());
            double tz = nbt.getDoubleOr("lastSkyZ", player.getZ());
            server.sendParticles(ParticleTypes.EXPLOSION_EMITTER, player.getX(), player.getY(), player.getZ(), 1, 0, 0, 0, 0);
            player.resetFallDistance();
            player.teleportTo(tx, ty, tz);
            while (player.isInWall()) {
                player.teleportTo(player.getX(), player.getY() + 2.0, player.getZ());
            }
            LGFx.sound(player, SoundEvents.PORTAL_TRAVEL, 1.0F, 1.0F);
            server.sendParticles(ParticleTypes.EXPLOSION_EMITTER, tx, ty, tz, 1, 0, 0, 0, 0);
            nbt.putLong("lastwarp", level.getGameTime());
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(nbt));
        }
        return stack;
    }
}

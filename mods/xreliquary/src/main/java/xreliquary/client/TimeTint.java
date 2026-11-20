package xreliquary.client;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;
import xreliquary.item.PotionKind;

/**
 * getColorFromItemStack with TimeKeeperHandler (a client tick counter running 10..88): the shimmering serums and the
 * red pulse of the cloak of distortion. Item model tint {@code {"type":"xreliquary:time","kind":"<potion id>|cloak"}}.
 */
public record TimeTint(String kind) implements ItemTintSource {
    public static final MapCodec<TimeTint> MAP_CODEC = Codec.STRING.fieldOf("kind").xmap(TimeTint::new, TimeTint::kind);
    private static int time;

    /** TimeKeeperHandler.tickEnd. */
    public static void tick() {
        time = time > 88 ? 10 : time + 1;
    }

    public static int time() {
        return time;
    }

    @Override
    public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity owner) {
        if (this.kind.equals("cloak")) {
            int i = time % 87;
            if (i > 43) {
                i = 87 - i;
            }
            return ARGB.opaque((int) (i * 255.0F / 43.0F) << 16);
        }
        for (PotionKind k : PotionKind.values()) {
            if (k.id.equals(this.kind)) {
                return ARGB.opaque(k.animatedColor(time));
            }
        }
        return -1;
    }

    @Override
    public MapCodec<TimeTint> type() {
        return MAP_CODEC;
    }
}

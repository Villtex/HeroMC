package asgardshield.client;

import asgardshield.ASConfig;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import org.jspecify.annotations.Nullable;

/**
 * ItemAsgardShield.getColorFromItemStack, render pass 0: the dyed colour, else the colour of the colour API config
 * for the shield material. Used as tint {@code {"type": "asgardshield_core:shield_color", "shield": "wood"}}.
 */
public record ShieldColorTint(String shield) implements ItemTintSource {
    public static final MapCodec<ShieldColorTint> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Codec.STRING.fieldOf("shield").forGetter(ShieldColorTint::shield)).apply(i, ShieldColorTint::new));

    @Override
    public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity) {
        DyedItemColor dyed = stack.get(DataComponents.DYED_COLOR);
        return ARGB.opaque(dyed != null ? dyed.rgb() : ASConfig.color(this.shield));
    }

    @Override
    public MapCodec<ShieldColorTint> type() {
        return MAP_CODEC;
    }
}

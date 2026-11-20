package tconstruct.client;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.select.SelectItemModelProperty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;
import tconstruct.tools.InfiTool;
import tconstruct.tools.ToolCoreItem;
import tconstruct.tools.ToolType;

/**
 * The render passes of ToolCore.getIcon as a select property: which texture a layer of a tool shows.
 * {@code part} = handle / head / accessory / extra (the material id of the part, "RenderHandle" ... of 1.5.2; the
 * bowstring and the fletching use their own ids), effect1..effect6 (the effect id of "Effect1" ...) or arrow (the arrow
 * on a drawn bow). The broken part (the head, the bowstring of a bow) gets "_broken"; a shortbow in use gets its draw
 * state "_1".."_3" (BowBase.getIcon: from two thirds and from DrawSpeed x FlightSpeed - 2 ticks). Empty = no layer.
 */
public record ToolPartProperty(String part) implements SelectItemModelProperty<String> {
    public static final SelectItemModelProperty.Type<ToolPartProperty, String> TYPE = SelectItemModelProperty.Type.create(
            RecordCodecBuilder.mapCodec(i -> i.group(Codec.STRING.fieldOf("part").forGetter(ToolPartProperty::part)).apply(i, ToolPartProperty::new)),
            Codec.STRING);

    @Override
    public String get(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed, ItemDisplayContext context) {
        if (!(stack.getItem() instanceof ToolCoreItem tool)) {
            return "";
        }
        InfiTool t = InfiTool.of(stack);
        if (t == null) {
            return "";
        }
        boolean bow = tool.type == ToolType.SHORTBOW;
        int state = bow ? drawState(stack, t, entity) : 0;
        String suffix = state > 0 ? "_" + state : "";
        return switch (this.part) {
            case "handle" -> t.handle + (bow && t.broken && state == 0 ? "_broken" : suffix);
            case "head" -> t.head + (!bow && t.broken ? "_broken" : suffix);
            case "accessory" -> t.accessory < 0 ? "" : t.accessory + suffix;
            case "extra" -> t.extra < 0 ? "" : t.extra + suffix;
            case "arrow" -> state > 0 ? Integer.toString(state) : "";
            default -> {
                if (this.part.startsWith("effect")) {
                    int index = Integer.parseInt(this.part.substring(6)) - 1;
                    yield index < t.effects.size() ? t.effects.get(index) + suffix : "";
                }
                yield "";
            }
        };
    }

    /** BowBase.getIcon(stack, pass, player, usingItem, useRemaining): 0 = not drawn. */
    private static int drawState(ItemStack stack, InfiTool t, @Nullable LivingEntity entity) {
        if (entity == null || !entity.isUsingItem() || entity.getUseItem() != stack) {
            return 0;
        }
        int useTime = stack.getUseDuration(entity) - entity.getUseItemRemainingTicks();
        int drawTime = (int) (t.getInt("DrawSpeed") * t.getFloat("FlightSpeed"));
        if (useTime >= drawTime - 2) {
            return 3;
        }
        return useTime >= drawTime * 2 / 3 ? 2 : 1;
    }

    @Override
    public Codec<String> valueCodec() {
        return Codec.STRING;
    }

    @Override
    public SelectItemModelProperty.Type<ToolPartProperty, String> type() {
        return TYPE;
    }
}

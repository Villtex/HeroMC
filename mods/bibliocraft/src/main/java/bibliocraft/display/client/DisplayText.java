package bibliocraft.display.client;

import bibliocraft.common.BCConfig;
import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

/** Text and item helpers of the weapon case, label and armor stand renderers. */
final class DisplayText {
    /** Config.color2: the colour of the enchantment lines (16711935, not in the config file). */
    static final int ENCHANT_COLOR = 0xFFFF00FF;

    private DisplayText() {
    }

    /** fontRender.drawString(text, -width / 2, y, color, Config.textshadow) at the current pose. */
    static void centered(PoseStack poseStack, SubmitNodeCollector collector, Font font, String text, int y, int color, int light) {
        FormattedCharSequence seq = Component.literal(text).getVisualOrderText();
        int adjust = font.width(seq) / 2;
        collector.submitText(poseStack, -adjust, y, seq, BCConfig.textShadow(), Font.DisplayMode.POLYGON_OFFSET, light, color, 0, 0);
    }

    /** The translated names (with level) of the first four enchantments of the stack ("ench" tag), as func_92056_g read them. */
    static List<String> enchantments(ItemStack stack) {
        List<String> names = new ArrayList<>();
        ItemEnchantments ench = stack.get(DataComponents.ENCHANTMENTS);
        if (ench != null) {
            for (Object2IntMap.Entry<Holder<Enchantment>> e : ench.entrySet()) {
                if (names.size() == 4) {
                    break;
                }
                names.add(Enchantment.getFullname(e.getKey(), e.getIntValue()).getString());
            }
        }
        return names;
    }

    /**
     * RenderItem.doRenderItem of an EntityItem without bobbing or spin, drawn with the GROUND transform of 26.3 (an item
     * 0.5 wide from y -0.125, as 1.5.2). 1.5.2 drew a block item centred on the origin; the GROUND transform of block
     * models lifts it by 3/16, which is taken off again.
     */
    static void entityItem(ItemStackRenderState item, boolean block, PoseStack poseStack, SubmitNodeCollector collector, int light) {
        poseStack.pushPose();
        if (block) {
            poseStack.translate(0.0F, -0.1875F, 0.0F);
        }
        item.submit(poseStack, collector, light, OverlayTexture.NO_OVERLAY, 0);
        poseStack.popPose();
    }
}

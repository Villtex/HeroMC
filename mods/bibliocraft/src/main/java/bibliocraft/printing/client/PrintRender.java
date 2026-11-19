package bibliocraft.printing.client;

import bibliocraft.BiblioCraft;
import bibliocraft.common.BCConfig;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;

/** Drawing helpers of the printing renderers. */
final class PrintRender {
    /** Config.color2: the magenta of the enchantment texts (not in the config file). */
    static final int COLOR2 = 0xFFFF00FF;

    private PrintRender() {
    }

    static Identifier tex(String name) {
        return BiblioCraft.id("textures/model/" + name + ".png");
    }

    /** The named parts of a baked layer. */
    static final class Parts {
        private final ModelPart root;
        private final Map<String, ModelPart> cache = new HashMap<>();

        Parts(ModelPart root) {
            this.root = root;
        }

        ModelPart get(String name) {
            return this.cache.computeIfAbsent(name, this.root::getChild);
        }

        /** ModelRenderer.render of the named parts with the texture. */
        void submit(PoseStack pose, SubmitNodeCollector collector, Identifier texture, int light, String... names) {
            for (String n : names) {
                collector.submitModelPart(this.get(n), pose, RenderTypes.entityCutout(texture), light, OverlayTexture.NO_OVERLAY, null);
            }
        }

        /** The same with the enchantment glint over it (the glint pass the renderers drew for enchanted things). */
        void submitGlint(PoseStack pose, SubmitNodeCollector collector, Identifier texture, int light, String... names) {
            for (String n : names) {
                collector.submitModelPart(this.get(n), pose, RenderTypes.entitySolidGlint(texture), light, OverlayTexture.NO_OVERLAY, null);
            }
        }
    }

    /**
     * rotateAngleX of a part set by the renderer: turned about the part's pivot in the pose, so that the shared baked
     * part is not changed (submits are drawn later).
     */
    static void rotateX(PoseStack pose, ModelPart part, float radians) {
        pose.translate(part.x / 16.0F, part.y / 16.0F, part.z / 16.0F);
        pose.rotateDegrees(Axis.XP, (float) Math.toDegrees(radians));
        pose.translate(-part.x / 16.0F, -part.y / 16.0F, -part.z / 16.0F);
    }

    /** fontRenderer.drawString(text, x, y, color, Config.textshadow) at the current pose. */
    static void text(PoseStack pose, SubmitNodeCollector collector, Font font, String text, float x, float y, int color, int light) {
        FormattedCharSequence seq = Component.literal(text).getVisualOrderText();
        collector.submitText(pose, x, y, seq, BCConfig.textShadow(), Font.DisplayMode.POLYGON_OFFSET, light, color, 0, 0);
    }

    /** drawString(text, -width/2, y, ...). */
    static void centered(PoseStack pose, SubmitNodeCollector collector, Font font, String text, float y, int color, int light) {
        text(pose, collector, font, text, -(font.width(text) / 2), y, color, light);
    }

    /** angle 0..3 -> glRotatef 270, 180, 90, 0 about y. */
    static float degrees(int angle) {
        return switch (angle) {
            case 0 -> 270.0F;
            case 1 -> 180.0F;
            case 2 -> 90.0F;
            default -> 0.0F;
        };
    }
}

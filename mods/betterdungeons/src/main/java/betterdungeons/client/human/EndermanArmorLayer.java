package betterdungeons.client.human;

import betterdungeons.BetterDungeons;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import org.jspecify.annotations.Nullable;

/**
 * RenderHumanEnderman.shouldRenderPass: vanilla armour of the armored enderman is drawn on its own long-limbed model with
 * /bdimg/enderman_&lt;material&gt;.png (leather dyed). Armour of other mods is not drawn (1.5.2 used their render index).
 */
public class EndermanArmorLayer extends RenderLayer<HumanRenderState, HumanModel> {
    private final ArmorModelSet<HumanModel> models;

    public EndermanArmorLayer(RenderLayerParent<HumanRenderState, HumanModel> parent, ArmorModelSet<HumanModel> models) {
        super(parent);
        this.models = models;
    }

    static @Nullable String material(ItemStack stack) {
        String path = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
        if (!BuiltInRegistries.ITEM.getKey(stack.getItem()).getNamespace().equals("minecraft")) {
            return null;
        }
        if (path.startsWith("leather_")) {
            return "cloth";
        } else if (path.startsWith("chainmail_")) {
            return "chain";
        } else if (path.startsWith("iron_")) {
            return "iron";
        } else if (path.startsWith("golden_")) {
            return "gold";
        } else if (path.startsWith("diamond_")) {
            return "diamond";
        }
        return null;
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int light, HumanRenderState state, float yRot, float xRot) {
        this.piece(poseStack, collector, light, state, state.chestEquipment, EquipmentSlot.CHEST);
        this.piece(poseStack, collector, light, state, state.legsEquipment, EquipmentSlot.LEGS);
        this.piece(poseStack, collector, light, state, state.feetEquipment, EquipmentSlot.FEET);
        this.piece(poseStack, collector, light, state, state.headEquipment, EquipmentSlot.HEAD);
    }

    private void piece(PoseStack poseStack, SubmitNodeCollector collector, int light, HumanRenderState state, ItemStack stack, EquipmentSlot slot) {
        if (stack.isEmpty()) {
            return;
        }
        String material = material(stack);
        if (material == null) {
            return;
        }
        Identifier texture = Identifier.fromNamespaceAndPath(BetterDungeons.MOD_ID, "textures/entity/enderman_" + material + ".png");
        int color = material.equals("cloth") ? ARGB.opaque(DyedItemColor.getOrDefault(stack, 0xA06540)) : -1;
        HumanModel model = this.models.get(slot);
        var type = RenderTypes.armorCutoutNoCull(texture);
        collector.submitModel(model, state, poseStack, type, light, OverlayTexture.NO_OVERLAY, color, null, state.outlineColor);
    }
}

package projectzulu.client.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.ArmedEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.Equippable;
import projectzulu.entity.HauntedArmorEntity;
import projectzulu.entity.ZuluMonster;

/**
 * RenderEquipment: the haunted armour drawn with the armour textures it wears (chestplate layer 1, leggings layer 2;
 * iron when the slot is empty or has no equipment asset) and its held item at the swordhand anchor.
 */
public class HauntedArmorRenderer extends MobRenderer<HauntedArmorEntity, HauntedArmorModel.State, HauntedArmorModel> {
    private static final Identifier IRON_CHEST = Identifier.withDefaultNamespace("textures/entity/equipment/humanoid/iron.png");
    private static final Identifier IRON_LEGS = Identifier.withDefaultNamespace("textures/entity/equipment/humanoid_leggings/iron.png");

    public HauntedArmorRenderer(EntityRendererProvider.Context context) {
        super(context, new HauntedArmorModel(context.bakeLayer(MonsterRenderers.HAUNTED_ARMOR_LAYER), false), 0.5F);
        HauntedArmorModel legs = new HauntedArmorModel(context.bakeLayer(MonsterRenderers.HAUNTED_ARMOR_LAYER), true);
        this.addLayer(new RenderLayer<>(this) {
            @Override
            public void submit(PoseStack poseStack, SubmitNodeCollector collector, int light, HauntedArmorModel.State state, float yRot, float xRot) {
                if (!state.isInvisible) {
                    collector.order(1).submitModel(legs, state, poseStack, RenderTypes.entityCutout(state.legsTexture), light,
                            LivingEntityRenderer.getOverlayCoords(state, 0.0F), state.outlineColor);
                }
            }
        });
        this.addLayer(new ItemInHandLayer<>(this));
    }

    @Override
    public HauntedArmorModel.State createRenderState() {
        return new HauntedArmorModel.State();
    }

    @Override
    public void extractRenderState(HauntedArmorEntity entity, HauntedArmorModel.State state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        ArmedEntityRenderState.extractArmedEntityRenderState(entity, state, this.itemModelResolver, partialTick);
        state.idle = entity.getState() == ZuluMonster.State.IDLE;
        state.attacking = entity.getState() == ZuluMonster.State.ATTACKING;
        state.wakeUpTimer = entity.getWakeUpTimer();
        state.animTime = entity.getAnimTime();
        state.chestTexture = armorTexture(entity.getItemBySlot(EquipmentSlot.CHEST), "humanoid", IRON_CHEST);
        state.legsTexture = armorTexture(entity.getItemBySlot(EquipmentSlot.LEGS), "humanoid_leggings", IRON_LEGS);
    }

    /** ForgeHooksClient.getArmorTexture of the worn piece: the texture of its equipment asset for that layer. */
    private static Identifier armorTexture(ItemStack stack, String layer, Identifier fallback) {
        Equippable equippable = stack.get(DataComponents.EQUIPPABLE);
        if (equippable == null || equippable.assetId().isEmpty()) {
            return fallback;
        }
        return equippable.assetId().get().identifier().withPath(p -> "textures/entity/equipment/" + layer + "/" + p + ".png");
    }

    @Override
    public Identifier getTextureLocation(HauntedArmorModel.State state) {
        return state.chestTexture;
    }
}

package xreliquary.item;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import xreliquary.XRData;
import xreliquary.XRFx;

import java.util.function.Consumer;

/**
 * ItemDistortionCloak: right click toggles invisibility ("isActive"), paid with one ender pearl per 2400 ticks
 * ("chargeTime"); sneaking it teleports onto the block looked at up to 128 blocks away for one pearl (cooldown 10).
 */
public class DistortionCloakItem extends XRItem {
    public DistortionCloakItem(Properties properties) {
        super(properties, 2);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltip, flag);
        int charge = XRData.getShort(stack, "chargeTime");
        tooltip.accept(Component.translatable("item.xreliquary.distortion_cloak.tooltip.remaining", charge / 1200, charge / 20 % 60));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return XRData.getBool(stack, "isActive");
    }

    private static boolean pearl(Player player) {
        return XRFx.creative(player) || XRFx.consumeMain(player, Items.ENDER_PEARL);
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity owner, @Nullable EquipmentSlot slot) {
        if (!(owner instanceof Player player)) {
            return;
        }
        if (XRData.getBool(stack, "isActive")) {
            if (XRData.getShort(stack, "chargeTime") <= 0) {
                if (pearl(player)) {
                    XRData.setShort(stack, "chargeTime", 2400);
                } else {
                    XRData.setBool(stack, "isActive", false);
                }
            }
            if (XRData.getShort(stack, "chargeTime") > 0) {
                // PotionEffect(invisibility, 5): one tick more so it does not flicker between the item ticks
                player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 6, 0, false, true));
                XRData.setShort(stack, "chargeTime", XRData.getShort(stack, "chargeTime") - 1);
            }
        }
        int cooldown = XRData.getShort(stack, "cooldown");
        if (cooldown > 0) {
            XRData.setShort(stack, "cooldown", cooldown - 1);
        }
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (player.isShiftKeyDown()) {
            if (XRData.getShort(stack, "cooldown") > 0) {
                return InteractionResult.PASS;
            }
            Vec3 eye = player.getEyePosition();
            Vec3 end = eye.add(player.getViewVector(1.0F).scale(128.0));
            BlockHitResult hit = level.clip(new ClipContext(eye, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.SOURCE_ONLY, player));
            if (hit.getType() == HitResult.Type.BLOCK && pearl(player)) {
                double x = hit.getBlockPos().getX();
                double y = hit.getBlockPos().getY() + 1.0;
                double z = hit.getBlockPos().getZ();
                for (int i = 0; i < 32; i++) {
                    XRFx.particle(level, ParticleTypes.PORTAL, x, y + level.getRandom().nextDouble() * 2.0, z, level.getRandom().nextGaussian(), 0.0,
                            level.getRandom().nextGaussian());
                }
                player.teleportTo(x, y, z);
                player.resetFallDistance();
                XRData.setShort(stack, "cooldown", 10);
            }
        } else {
            XRData.setBool(stack, "isActive", !XRData.getBool(stack, "isActive"));
        }
        return InteractionResult.SUCCESS;
    }
}

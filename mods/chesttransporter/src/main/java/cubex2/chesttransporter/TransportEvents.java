package cubex2.chesttransporter;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.entity.vehicle.minecart.Minecart;
import net.minecraft.world.entity.vehicle.minecart.MinecartChest;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/** The right clicks of ItemChestTransporter (they come before the chest opens) and the burden of a carried chest. */
public final class TransportEvents {
    /** Jump boost -2 of the original lowered the jump by 0.1; effect levels below 0 no longer exist. */
    private static final Identifier HEAVY = Identifier.fromNamespaceAndPath(ChestTransporter.MOD_ID, "carrying_chest");

    private TransportEvents() {
    }

    private static boolean holdsTransporter(Player player, InteractionHand hand) {
        return hand == InteractionHand.MAIN_HAND && player.getMainHandItem().is(ChestTransporter.CHEST_TRANSPORTER.get());
    }

    @SubscribeEvent
    public static void rightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        if (!holdsTransporter(player, event.getHand())) {
            return;
        }
        ItemStack stack = player.getMainHandItem();
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        BlockState state = level.getBlockState(pos);
        if (!ChestTransporterItem.isFull(stack) && state.is(Blocks.CHEST)) {
            if (!level.isClientSide() && level.getBlockEntity(pos) instanceof Container chest) {
                ChestTransporterItem.takeFrom(chest, stack);
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                SoundType sound = state.getSoundType(level, pos, player);
                level.playSound(null, pos, sound.getStepSound(), SoundSource.BLOCKS, (sound.getVolume() + 1.0F) / 2.0F, sound.getPitch() * 0.5F);
            }
            cancel(event);
        } else if (ChestTransporterItem.isFull(stack)) {
            if (!level.isClientSide()) {
                place(event, player, stack);
            }
            cancel(event);
        }
    }

    private static void cancel(PlayerInteractEvent.RightClickBlock event) {
        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);
    }

    /** Places the chest like a chest item would; the transporter breaks. */
    private static void place(PlayerInteractEvent.RightClickBlock event, Player player, ItemStack stack) {
        ItemStack chestItem = new ItemStack(Items.CHEST);
        BlockPlaceContext context = new BlockPlaceContext(event.getLevel(), player, InteractionHand.MAIN_HAND, chestItem, event.getHitVec());
        if (!((BlockItem) Items.CHEST).place(context).consumesAction()) {
            return;
        }
        if (event.getLevel().getBlockEntity(context.getClickedPos()) instanceof Container chest) {
            ChestTransporterItem.putInto(stack, chest);
        }
        breakTransporter(player, stack);
    }

    private static void breakTransporter(Player player, ItemStack stack) {
        player.onEquippedItemBroken(stack.copy(), EquipmentSlot.MAINHAND);
        stack.shrink(1);
    }

    @SubscribeEvent
    public static void rightClickEntity(PlayerInteractEvent.EntityInteract event) {
        Player player = event.getEntity();
        if (!holdsTransporter(player, event.getHand()) || !(event.getTarget() instanceof AbstractMinecart minecart)) {
            return;
        }
        ItemStack stack = player.getMainHandItem();
        Level level = event.getLevel();
        boolean full = ChestTransporterItem.isFull(stack);
        if (!full && minecart instanceof MinecartChest chestCart) {
            if (!level.isClientSide()) {
                ChestTransporterItem.takeFrom(chestCart, stack);
                replace(minecart, EntityTypes.MINECART);
                cartSound(minecart, 0.5F);
            }
        } else if (full && minecart instanceof Minecart && !minecart.isVehicle()) {
            if (!level.isClientSide()) {
                MinecartChest chestCart = replace(minecart, EntityTypes.CHEST_MINECART);
                if (chestCart != null) {
                    ChestTransporterItem.putInto(stack, chestCart);
                }
                cartSound(minecart, 0.8F);
                breakTransporter(player, stack);
            }
        } else {
            return;
        }
        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);
    }

    private static <T extends AbstractMinecart> T replace(AbstractMinecart old, EntityType<T> type) {
        T cart = type.create(old.level(), EntitySpawnReason.EVENT);
        if (cart != null) {
            cart.snapTo(old.getX(), old.getY(), old.getZ(), old.getYRot(), old.getXRot());
            old.discard();
            old.level().addFreshEntity(cart);
        }
        return cart;
    }

    private static void cartSound(Entity cart, float pitch) {
        SoundType sound = SoundType.WOOD;
        cart.level().playSound(null, cart.getX() + 0.5, cart.getY() + 0.5, cart.getZ() + 0.5, sound.getStepSound(), SoundSource.BLOCKS,
                (sound.getVolume() + 1.0F) / 2.0F, sound.getPitch() * pitch);
    }

    /** onUpdate of the original: a loaded transporter anywhere in the inventory weighs the player down. */
    @SubscribeEvent
    public static void tick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) {
            return;
        }
        boolean carrying = !player.getAbilities().instabuild && player.getInventory().contains(
                s -> s.is(ChestTransporter.CHEST_TRANSPORTER.get()) && ChestTransporterItem.isFull(s));
        AttributeInstance jump = player.getAttribute(Attributes.JUMP_STRENGTH);
        if (carrying) {
            refresh(player, MobEffects.SLOWNESS, 2);
            refresh(player, MobEffects.MINING_FATIGUE, 3);
            refresh(player, MobEffects.HUNGER, 0);
            if (jump != null && !jump.hasModifier(HEAVY)) {
                jump.addTransientModifier(new AttributeModifier(HEAVY, -0.1, AttributeModifier.Operation.ADD_VALUE));
            }
        } else if (jump != null && jump.hasModifier(HEAVY)) {
            jump.removeModifier(HEAVY);
        }
    }

    private static void refresh(Player player, net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect> effect, int amplifier) {
        MobEffectInstance current = player.getEffect(effect);
        if (current == null || current.getDuration() < 20) {
            player.addEffect(new MobEffectInstance(effect, 60, amplifier));
        }
    }
}

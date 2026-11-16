package harkenscythe.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;

import java.util.function.Consumer;

/**
 * ItemHSAmulet: "Teleport back to resting place". With 5 souls and 10 blood (Soul Attuned: 10 souls, Blood Attuned:
 * 20 blood) in keepers, holding right-click builds up the spell in ten readings of 20 ticks (the damage bar, with
 * water, fire, spell and splash particles, then glyphs and portal particles). Then it takes the player to the bed:
 * the Overworld Amulet within the overworld (the bed must still be there), the Nether Amulet from the Nether, the
 * Ethereal Amulet from any dimension but the End, or within the overworld. Otherwise it fizzles.
 */
public class AmuletItem extends Item {
    public enum Kind { OVERWORLD, NETHER, ETHEREAL }

    private final Kind kind;

    public AmuletItem(Kind kind, Properties properties) {
        super(properties);
        this.kind = kind;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
        builder.accept(Component.translatable("tooltip.harkenscythe_core.amulet").withStyle(ChatFormatting.GRAY));
        String key = TalismanItem.attunement(stack);
        builder.accept(Component.translatable("tooltip.harkenscythe_core.amulet.cost" + (key == null ? "" : "." + key)).withStyle(ChatFormatting.GRAY));
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity user) {
        return 20;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.NONE;
    }

    private static boolean canPay(Level level, ItemStack stack, Player player) {
        int[] cost = TalismanItem.cost(level, stack, 5, 10);
        return Essence.has(player, cost[0], false) && Essence.has(player, cost[1], true);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!canPay(level, stack, player)) {
            return InteractionResult.PASS;
        }
        player.startUsingItem(hand);
        int i = stack.getDamageValue();
        if (i >= 2 && i < 10) {
            Ritual.particle(level, ParticleTypes.UNDERWATER, player, 0.9, -1.6, 0.9);
            Ritual.particle(level, ParticleTypes.UNDERWATER, player, 0.8, -1.6, 0.7);
            Ritual.particle(level, ParticleTypes.UNDERWATER, player, 0.7, -1.6, 0.8);
            level.playSound(player, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_BREATH, SoundSource.PLAYERS, 1.0F, 1.0F);
        }
        if (i >= 4 && i < 10) {
            Ritual.particle(level, ParticleTypes.FLAME, player, -0.9, -1.6, -0.9);
            Ritual.particle(level, ParticleTypes.FLAME, player, -0.7, -1.6, -0.8);
            Ritual.particle(level, ParticleTypes.FLAME, player, -0.8, -1.6, -0.7);
        }
        if (i >= 6 && i < 10) {
            Ritual.particle(level, ParticleTypes.WITCH, player, -0.9, -1.6, 0.9);
            Ritual.particle(level, ParticleTypes.WITCH, player, -0.7, -1.6, 0.8);
            Ritual.particle(level, ParticleTypes.WITCH, player, -0.8, -1.6, 0.7);
        }
        if (i >= 8 && i < 10) {
            Ritual.particle(level, ParticleTypes.SPLASH, player, 0.9, -1.6, -0.9);
            Ritual.particle(level, ParticleTypes.SPLASH, player, 0.8, -1.6, -0.7);
            Ritual.particle(level, ParticleTypes.SPLASH, player, 0.7, -1.6, -0.8);
        }
        if (i == 10 || i == 14 || i == 18 || i == 20) {
            Ritual.text(level, player);
        }
        if (i == 12 || i == 16 || i == 18 || i == 20) {
            Ritual.text2(level, player);
        }
        if (i >= 10) {
            elements(level, player);
        }
        if (i >= 16) {
            portal(level, player);
        }
        if (!level.isClientSide()) {
            stack.setDamageValue(Math.min(i + 2, stack.getMaxDamage() - 1));
            if (i >= 20) {
                this.finish(stack, (ServerLevel) level, player);
                return InteractionResult.CONSUME;
            }
            if (i + 2 >= 10 && i + 2 < 12) {
                level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PORTAL_AMBIENT, SoundSource.PLAYERS, 1.0F, 1.0F);
            }
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public boolean releaseUsing(ItemStack stack, Level level, LivingEntity entity, int remainingTime) {
        if (entity instanceof Player player && level instanceof ServerLevel server) {
            this.finish(stack, server, player);
        }
        return true;
    }

    /** onPlayerStoppedUsing: teleports when the spell is complete (19 or more), else fizzles. */
    private void finish(ItemStack stack, ServerLevel level, Player player) {
        int progress = stack.getDamageValue();
        stack.setDamageValue(0);
        if (progress >= 19 && player instanceof ServerPlayer serverPlayer && canPay(level, stack, player)) {
            ServerPlayer.RespawnConfig respawn = serverPlayer.getRespawnConfig();
            if (respawn != null && respawn.respawnData().dimension() == Level.OVERWORLD) {
                BlockPos bed = respawn.respawnData().pos();
                ServerLevel overworld = level.getServer().overworld();
                boolean inOverworld = level.dimension() == Level.OVERWORLD;
                boolean travel = !inOverworld && level.dimension() != Level.END && this.kind == Kind.ETHEREAL
                        || level.dimension() == Level.NETHER && this.kind == Kind.NETHER;
                boolean home = !travel && level.getBlockState(bed).is(BlockTags.BEDS) && this.kind != Kind.NETHER;
                if (travel || home) {
                    int[] cost = TalismanItem.cost(level, stack, 5, 10);
                    Essence.consume(player, cost[0], false);
                    Essence.consume(player, cost[1], true);
                    Vec3 target = new Vec3(bed.getX() + 0.5, bed.getY() + 2, bed.getZ() + 0.5);
                    serverPlayer.teleport(new TeleportTransition(travel ? overworld : level, target, Vec3.ZERO, player.getYRot(), player.getXRot(),
                            TeleportTransition.DO_NOTHING));
                    player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PORTAL_TRAVEL, SoundSource.PLAYERS, 0.2F, 1.0F);
                    return;
                }
            }
        }
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    private static void elements(Level level, Player player) {
        double[][] spots = {{0.9, 0.9}, {0.8, 0.7}, {0.7, 0.8}};
        for (double[] s : spots) {
            Ritual.particle(level, ParticleTypes.UNDERWATER, player, s[0], -1.6, s[1]);
            Ritual.particle(level, ParticleTypes.SPLASH, player, s[0], -1.6, -s[1]);
            Ritual.particle(level, ParticleTypes.WITCH, player, -s[1], -1.6, s[0]);
            Ritual.particle(level, ParticleTypes.FLAME, player, -s[1], -1.6, -s[0]);
        }
    }

    private static void portal(Level level, Player player) {
        for (double dy : new double[] {-0.3, -0.9}) {
            Ritual.particle(level, ParticleTypes.PORTAL, player, 0.3, dy, 0.3);
            Ritual.particle(level, ParticleTypes.PORTAL, player, 0.3, dy, -0.3);
            Ritual.particle(level, ParticleTypes.PORTAL, player, -0.3, dy, 0.3);
            Ritual.particle(level, ParticleTypes.PORTAL, player, -0.3, dy, -0.3);
        }
    }
}

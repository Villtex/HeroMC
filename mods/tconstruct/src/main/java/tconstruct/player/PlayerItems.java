package tconstruct.player;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import tconstruct.TPlayer;
import tconstruct.entity.BlueSlime;
import tconstruct.entity.TEntities;

import java.text.DecimalFormat;
import java.util.function.Consumer;

/** The items of part 25e: heart canister, manual, jeweled apple, golden head, essence crystal and the slime eggs (TitleIcon). */
public final class PlayerItems {
    private PlayerItems() {
    }

    private static void tips(Consumer<Component> out, String key, int lines, ChatFormatting... style) {
        for (int i = 1; i <= lines; i++) {
            out.accept(Component.translatable(key + "." + i).withStyle(style));
        }
    }

    /** A plain item with the CraftingItem tooltip lines of 1.5.2 ({@code <key>.1..n}). */
    public static class TipItem extends Item {
        private final String key;
        private final int lines;
        private final ChatFormatting[] style;

        public TipItem(String key, int lines, Properties properties, ChatFormatting... style) {
            super(properties);
            this.key = key;
            this.lines = lines;
            this.style = style;
        }

        @Override
        public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> out, TooltipFlag flag) {
            tips(out, this.key, this.lines, this.style);
        }
    }

    /** HeartCanister 2: right-click puts one into slot 6 of the extended armor (up to 10), +2 health each. */
    public static class HeartCanister extends TipItem {
        public HeartCanister(Properties properties) {
            super("item.tconstruct.red_heart_canister.tip", 2, properties);
        }

        @Override
        public InteractionResult use(Level level, Player player, InteractionHand hand) {
            if (player instanceof ServerPlayer serverPlayer) {
                ItemStack stack = player.getItemInHand(hand);
                if (!stack.is(this)) {
                    return InteractionResult.PASS;
                }
                PlayerData data = TPlayer.data(serverPlayer);
                ItemStack slot = data.armor.getItem(PlayerData.HEART_SLOT);
                if (slot.isEmpty()) {
                    data.armor.setItem(PlayerData.HEART_SLOT, stack.copyWithCount(1));
                    stack.shrink(1);
                } else if (slot.is(this) && slot.getCount() < slot.getMaxStackSize()) {
                    slot.grow(1);
                    stack.shrink(1);
                    data.armor.setChanged();
                }
                PlayerEvents.recalculateHealth(serverPlayer);
            }
            return InteractionResult.SUCCESS;
        }
    }

    /** Opens the manual screen on the client, set by the client setup. */
    public static Consumer<ItemStack> openManual = stack -> {
    };

    /** Manual 0..2 (firstday, materials, smeltery); the diary (3) had no item. */
    public static class Manual extends TipItem {
        public final int volume;

        public Manual(int volume, Properties properties) {
            super("item.tconstruct.manual.tip." + volume, 1, properties, ChatFormatting.ITALIC);
            this.volume = volume;
        }

        @Override
        public InteractionResult use(Level level, Player player, InteractionHand hand) {
            if (level.isClientSide()) {
                openManual.accept(player.getItemInHand(hand));
            }
            return InteractionResult.SUCCESS;
        }
    }

    /**
     * DiamondApple (Jeweled Apple): 4 hunger, 2.0 saturation, always edible; resistance, haste and strength for 1200
     * ticks more than the effect already had. Like 1.5.2 the duration is not reset between the three: a missing effect
     * takes the duration of the one before.
     */
    public static class JeweledApple extends Item {
        public JeweledApple(Properties properties) {
            super(properties);
        }

        @Override
        public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
            if (!level.isClientSide()) {
                int duration = 0;
                MobEffectInstance effect = entity.getEffect(MobEffects.RESISTANCE);
                if (effect != null) {
                    duration = effect.getDuration();
                }
                entity.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, duration + 1200, 0));
                effect = entity.getEffect(MobEffects.HASTE);
                if (effect != null) {
                    duration = effect.getDuration();
                }
                entity.addEffect(new MobEffectInstance(MobEffects.HASTE, duration + 1200, 0));
                effect = entity.getEffect(MobEffects.STRENGTH);
                if (effect != null) {
                    duration = effect.getDuration();
                }
                entity.addEffect(new MobEffectInstance(MobEffects.STRENGTH, duration + 1200, 0));
            }
            return super.finishUsingItem(stack, level, entity);
        }
    }

    /** EssenceCrystal: eaten in 32 ticks, gives an experience orb of the stored essence (NBT "Essence"). */
    public static class EssenceCrystal extends Item {
        private static final DecimalFormat FORMAT = new DecimalFormat("##.##");
        /** The creative variants (getSubItems). */
        public static final int[] CREATIVE = {17, 85, 170, 255, 385, 590, 825};

        public EssenceCrystal(Properties properties) {
            super(properties);
        }

        @Override
        public int getUseDuration(ItemStack stack, LivingEntity entity) {
            return 32;
        }

        @Override
        public ItemUseAnimation getUseAnimation(ItemStack stack) {
            return ItemUseAnimation.EAT;
        }

        @Override
        public InteractionResult use(Level level, Player player, InteractionHand hand) {
            player.startUsingItem(hand);
            return InteractionResult.CONSUME;
        }

        @Override
        public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
            Integer essence = stack.get(TPlayer.ESSENCE.get());
            if (essence != null && entity instanceof Player player) {
                if (level instanceof ServerLevel server) {
                    server.addFreshEntity(new ExperienceOrb(server, player.getX(), player.getY() + 1.0, player.getZ(), essence));
                }
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
            }
            return stack;
        }

        @Override
        public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> out, TooltipFlag flag) {
            Integer essence = stack.get(TPlayer.ESSENCE.get());
            out.accept(Component.translatable("item.tconstruct.essence_crystal.levels", essence == null ? "0" : levels(essence)));
        }

        /** EssenceExtractorLogic.getEssencelevels. */
        public static String levels(int essence) {
            if (essence < 255) {
                return FORMAT.format(essence / 17.0F);
            }
            int amount;
            int levels;
            int offset;
            int sub;
            if (essence < 825) {
                amount = essence - 255;
                levels = 15;
                offset = 3;
                sub = 17 + offset;
            } else {
                amount = essence - 825;
                levels = 30;
                offset = 7;
                sub = 62 + offset;
            }
            while (amount - sub >= 0) {
                amount -= sub;
                sub += offset;
                levels++;
            }
            return FORMAT.format(levels + (float) amount / sub);
        }
    }

    /** TitleIcon 0 and 1: spawn a blue slime or a King Slime (size 8) with the riders of BlueSlime.initCreature. */
    public static class SlimeEgg extends Item {
        private final boolean king;

        public SlimeEgg(boolean king, Properties properties) {
            super(properties);
            this.king = king;
        }

        @Override
        public InteractionResult useOn(UseOnContext context) {
            if (context.getLevel() instanceof ServerLevel level) {
                BlockPos pos = context.getClickedPos().relative(context.getClickedFace());
                spawn(level, Vec3.atLowerCornerOf(pos), this.king);
                if (context.getPlayer() == null || !context.getPlayer().getAbilities().instabuild) {
                    context.getItemInHand().shrink(1);
                }
            }
            return InteractionResult.SUCCESS;
        }

        /** activateSpawnEgg (TDispenserBehaviorSpawnEgg): at the corner of the block in front. */
        public static void dispense(ServerLevel level, BlockPos pos, Direction facing, boolean king) {
            spawn(level, Vec3.atLowerCornerOf(pos.relative(facing)), king);
        }

        public static @Nullable BlueSlime spawn(ServerLevel level, Vec3 at, boolean king) {
            BlueSlime slime = create(level, at);
            if (slime == null) {
                return null;
            }
            if (king) {
                slime.setSize(8, true);
            }
            level.addFreshEntity(slime);
            initCreature(level, slime);
            return slime;
        }

        private static @Nullable BlueSlime create(ServerLevel level, Vec3 at) {
            BlueSlime slime = TEntities.BLUE_SLIME.get().create(level, EntitySpawnReason.SPAWN_ITEM_USE);
            if (slime != null) {
                slime.snapTo(at.x, at.y, at.z, 0.0F, 0.0F);
                slime.finalizeSpawn(level, level.getCurrentDifficultyAt(BlockPos.containing(at)), EntitySpawnReason.SPAWN_ITEM_USE, null);
            }
            return slime;
        }

        private static void ride(ServerLevel level, Mob rider, Mob mount) {
            rider.snapTo(mount.getX(), mount.getY(), mount.getZ(), mount.getYRot(), 0.0F);
            rider.finalizeSpawn(level, level.getCurrentDifficultyAt(mount.blockPosition()), EntitySpawnReason.JOCKEY, null);
            level.addFreshEntity(rider);
            rider.startRiding(mount);
        }

        /** BlueSlime.initCreature: a skeleton on 1 in 8 of size 2, a creeper on 1 in 12 of size 4, the King's retinue. */
        private static void initCreature(ServerLevel level, BlueSlime slime) {
            int size = slime.getSize();
            if (size == 2 && slime.getRandom().nextInt(8) == 0) {
                Mob skeleton = EntityTypes.SKELETON.create(level, EntitySpawnReason.JOCKEY);
                if (skeleton != null) {
                    ride(level, skeleton, slime);
                }
            }
            if (size == 4 && slime.getRandom().nextInt(12) == 0) {
                Mob creeper = EntityTypes.CREEPER.create(level, EntitySpawnReason.JOCKEY);
                if (creeper != null) {
                    ride(level, creeper, slime);
                }
            }
            if (size == 8) {
                Vec3 at = slime.position();
                for (int i = 0; i < 5; i++) {
                    BlueSlime minion = create(level, at);
                    if (minion != null) {
                        level.addFreshEntity(minion);
                        initCreature(level, minion);
                    }
                }
                BlueSlime mount = create(level, at);
                if (mount != null) {
                    mount.setSize(2, true);
                    level.addFreshEntity(mount);
                    Mob skeleton = EntityTypes.SKELETON.create(level, EntitySpawnReason.JOCKEY);
                    if (skeleton != null) {
                        ride(level, skeleton, mount);
                    }
                }
            }
        }
    }
}

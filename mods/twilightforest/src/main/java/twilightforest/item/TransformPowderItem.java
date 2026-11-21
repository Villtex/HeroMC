package twilightforest.item;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import twilightforest.TFEntities;
import twilightforest.TFItems;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * ItemTFTransformPowder: used on a creature it turns it into its counterpart (and back): minotaur / zombie pigman, deer /
 * cow, bighorn / sheep, boar / pig, raven / bat, hostile wolf / wolf, penguin / chicken, hedge spider / spider, swarm
 * spider / cave spider, wraith / blaze, redcap / villager, skeleton druid / witch. One powder each; a right click into
 * the air only sparkles. Handled before the creature's own interaction (a villager would trade first).
 */
public class TransformPowderItem extends Item {
    private static final Map<EntityType<?>, Supplier<? extends EntityType<?>>> TRANSFORM = new HashMap<>();

    public TransformPowderItem(Properties properties) {
        super(properties);
    }

    private static void twoWay(Supplier<? extends EntityType<?>> a, Supplier<? extends EntityType<?>> b) {
        TRANSFORM.put(a.get(), b);
        TRANSFORM.put(b.get(), a);
    }

    private static Map<EntityType<?>, Supplier<? extends EntityType<?>>> map() {
        if (TRANSFORM.isEmpty()) {
            twoWay(TFEntities.MINOTAUR, () -> net.minecraft.world.entity.EntityTypes.ZOMBIFIED_PIGLIN);
            twoWay(TFEntities.WILD_DEER, () -> net.minecraft.world.entity.EntityTypes.COW);
            twoWay(TFEntities.BIGHORN_SHEEP, () -> net.minecraft.world.entity.EntityTypes.SHEEP);
            twoWay(TFEntities.WILD_BOAR, () -> net.minecraft.world.entity.EntityTypes.PIG);
            twoWay(TFEntities.RAVEN, () -> net.minecraft.world.entity.EntityTypes.BAT);
            twoWay(TFEntities.HOSTILE_WOLF, () -> net.minecraft.world.entity.EntityTypes.WOLF);
            twoWay(TFEntities.PENGUIN, () -> net.minecraft.world.entity.EntityTypes.CHICKEN);
            twoWay(TFEntities.HEDGE_SPIDER, () -> net.minecraft.world.entity.EntityTypes.SPIDER);
            twoWay(TFEntities.SWARM_SPIDER, () -> net.minecraft.world.entity.EntityTypes.CAVE_SPIDER);
            twoWay(TFEntities.WRAITH, () -> net.minecraft.world.entity.EntityTypes.BLAZE);
            twoWay(TFEntities.REDCAP, () -> net.minecraft.world.entity.EntityTypes.VILLAGER);
            twoWay(TFEntities.SKELETON_DRUID, () -> net.minecraft.world.entity.EntityTypes.WITCH);
        }
        return TRANSFORM;
    }

    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        ItemStack stack = event.getItemStack();
        if (!stack.is(TFItems.TRANSFORM_POWDER.get()) || !(event.getTarget() instanceof LivingEntity target)) {
            return;
        }
        Supplier<? extends EntityType<?>> to = map().get(target.getType());
        if (to == null) {
            return;
        }
        Level level = target.level();
        if (level instanceof ServerLevel server) {
            if (!(to.get().create(server, EntitySpawnReason.CONVERSION) instanceof Mob monster)) {
                return;
            }
            monster.snapTo(target.getX(), target.getY(), target.getZ(), target.getYRot(), target.getXRot());
            monster.finalizeSpawn(server, server.getCurrentDifficultyAt(monster.blockPosition()), EntitySpawnReason.CONVERSION, null);
            server.addFreshEntity(monster);
            target.discard();
            server.broadcastEntityEvent(monster, (byte) 20);
            server.playSound(null, target.getX() + 0.5, target.getY() + 0.5, target.getZ() + 0.5, SoundEvents.ZOMBIE_VILLAGER_CURE,
                    target.getSoundSource(), 1.0F + server.getRandom().nextFloat(), server.getRandom().nextFloat() * 0.7F + 0.3F);
        }
        stack.consume(1, event.getEntity());
        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide()) {
            AABB box = TFItemUtil.effectBox(player, 2.0, 1.0);
            for (int i = 0; i < 30; i++) {
                level.addParticle(ParticleTypes.ENCHANTED_HIT, box.minX + level.getRandom().nextFloat() * (box.maxX - box.minX),
                        box.minY + level.getRandom().nextFloat() * (box.maxY - box.minY),
                        box.minZ + level.getRandom().nextFloat() * (box.maxZ - box.minZ), 0.0, 0.0, 0.0);
            }
        }
        return InteractionResult.PASS;
    }
}

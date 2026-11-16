package cofh;

import net.minecraft.core.registries.Registries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;

/**
 * CoFH Core's {@code EntityDropHead} with the pack's {@code config/cofh/Core.cfg}: zombies and creepers drop their
 * head with 5 % when a player killed them.
 *
 * <p>Faithful to the original, also where it did not do what the config says:
 * <ul>
 * <li>The handler only ran when Forge's "special drop value" was at least 5: 1.5.2 rolled
 * {@code nextInt(200) - looting} for the rare drop of a mob killed by a player and left it 0 otherwise. A head
 * therefore needed a player kill without a rare drop (about 97.5 %) and then the 5 % roll.</li>
 * <li>Skeletons drop nothing: {@code CoFHCore.preInit} wrote the "WitherSkeletonsDropHeads" value (false) into the
 * skeleton switch, and wither skeleton heads are off in the pack.</li>
 * <li>Players drop nothing: {@code PlayerDropsEvent} always passed a special drop value of 0.</li>
 * <li>{@code EntityZombie} included zombie pigmen and zombie villagers; here every {@link Zombie} (also husks,
 * drowned and zombified piglins).</li>
 * </ul>
 */
public final class HeadDrops {
    /** Core.cfg feature.heads.chance; ZombieDropChance and CreeperDropChance. */
    static final int ZOMBIE_CHANCE = 5;
    static final int CREEPER_CHANCE = 5;

    private HeadDrops() {
    }

    @SubscribeEvent
    public static void drops(LivingDropsEvent event) {
        LivingEntity entity = event.getEntity();
        if (!(entity instanceof Zombie) && !(entity instanceof Creeper)) {
            return;
        }
        // MobsDropOnPvEOnly: only when recently hit by a player; that is also the only case with a special drop value
        if (!event.isRecentlyHit() || entity.isBaby()) {
            return;
        }
        RandomSource random = entity.getRandom();
        int looting = event.getSource().getEntity() instanceof LivingEntity killer
                ? entity.level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).get(Enchantments.LOOTING)
                        .map(holder -> EnchantmentHelper.getItemEnchantmentLevel(holder, killer.getMainHandItem())).orElse(0)
                : 0;
        int special = random.nextInt(200) - looting;
        if (special < 5) {
            return;
        }
        int percent = random.nextInt(100);
        ItemStack skull = null;
        if (entity instanceof Zombie && percent < ZOMBIE_CHANCE) {
            skull = new ItemStack(Items.ZOMBIE_HEAD);
        } else if (entity instanceof Creeper && percent < CREEPER_CHANCE) {
            skull = new ItemStack(Items.CREEPER_HEAD);
        }
        if (skull != null) {
            ItemEntity item = new ItemEntity(entity.level(), entity.getX(), entity.getY(), entity.getZ(), skull);
            item.setPickUpDelay(10);
            event.getDrops().add(item);
        }
    }
}

package betterdungeons.api;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * API.RegisterDungeonMobs with the families BetterDungeons.load added, by index (the "Entity" number of the spawner
 * and of the dungeon configuration). 1.5.2's addMob(mob, position) ignored the position and appended, and it added
 * the triton and the ogre twice instead of the gremlin and the golem: index 12 is the triton, 14 the ogre.
 *
 * <p>The defaults build the mobs by their 1.5.2 names through {@link BDEntityIds} and call
 * {@link BDDungeonMob#initDungeonMob} for level and kind, as the MobX classes of chocolate.mods.BD.mobs.mobRegister did
 * with their constructors. A family whose types are missing gives null, and the spawner falls back to vanilla mobs.
 * The mob port may replace an entry with {@link #set} (the list is read every time).
 */
public final class RegisterDungeonMobs {
    public static final List<BaseDungeonMonsters> mobList = new ArrayList<>();

    static {
        add(new ByName("pigman", "ArmoredPigZombie", 4, 4, "PigMage", -1, -1));
        add(new BasedOnDistance());
        add(new ByName("zombie", "ArmoredZombie", 4, 3, "Liche", -1, -1));
        add(new ByName("pirate", "Pirate", 5, 1, "PirateBoss", -1, -1));
        add(new ByName("dwarf", "Dwarf", 4, 1, "GolemBoss", -1, -1));
        add(new ByName("goblin", "Goblin", 4, 1, "SpiderBoss", -1, -1));
        add(new ByName("walker", "Walker", 4, 8, "WalkerBoss", -1, -1));
        add(new ByName("specter", "Specter", 4, 7, "SpecterBoss", -1, -1));
        add(new ByName("enderman", "ArmoredEnderman", 4, 1, "ArmoredEnderman", 4, 7));
        add(new GoblinBlue());
        add(new ByName("monkey", "Monkey", 4, 1, "Monking", -1, -1));
        BaseDungeonMonsters triton = new ByName("Triton", "Triton", 4, 1, "Triton", 4, 6);
        add(triton);
        add(triton);
        BaseDungeonMonsters ogre = new ByName("ogre", "OgreArmored", 4, 7, "OgreArmored", 4, 6);
        add(ogre);
        add(ogre);
        add(new ByName("minotaur", "MinotaurArmored", 4, 8, "MinotaurBoss", -1, -1));
        add(new ByName("skeleton", "ArmoredSkeleton", 4, 1, "Necromancer", -1, -1));
    }

    private RegisterDungeonMobs() {
    }

    public static void add(BaseDungeonMonsters mob) {
        mobList.add(mob);
        mob.setID(mobList.indexOf(mob));
    }

    /** Replaces the family at an index (for the mob port). */
    public static void set(int index, BaseDungeonMonsters mob) {
        mobList.set(index, mob);
        mob.setID(index);
    }

    public static @Nullable BaseDungeonMonsters get(int index) {
        return index >= 0 && index < mobList.size() ? mobList.get(index) : null;
    }

    /** A Better Dungeons mob by its 1.5.2 name with BDDungeonMob.initDungeonMob(level, type), null if not ported. */
    public static @Nullable Entity create(ServerLevel level, String oldName, int lvl, int type) {
        EntityType<?> entityType = BDEntityIds.type(oldName);
        if (entityType == null) {
            return null;
        }
        Entity e = entityType.create(level, EntitySpawnReason.SPAWNER);
        if (e instanceof BDDungeonMob mob) {
            mob.initDungeonMob(lvl, type);
        }
        return e;
    }

    /** One family: warrior Mob(world, lvl), ranged Mob(world, lvl, rangedType), special Mob(world, lvl, specialType), boss. */
    static class ByName extends BaseDungeonMonsters {
        final String name;
        final String warrior;
        final int rangedType;
        final int specialType;
        final String boss;
        final int bossLvl;
        final int bossType;

        ByName(String name, String warrior, int rangedType, int specialType, String boss, int bossLvl, int bossType) {
            this.name = name;
            this.warrior = warrior;
            this.rangedType = rangedType;
            this.specialType = specialType;
            this.boss = boss;
            this.bossLvl = bossLvl;
            this.bossType = bossType;
        }

        @Override
        public @Nullable Entity getWarriorMob(ServerLevel level, int lvl, int x, int y, int z) {
            return create(level, this.warrior, lvl, -1);
        }

        @Override
        public @Nullable Entity getRangedMob(ServerLevel level, int lvl, int x, int y, int z) {
            return create(level, this.warrior, lvl, this.rangedType);
        }

        @Override
        public @Nullable Entity getSpecialMob(ServerLevel level, int lvl, int x, int y, int z) {
            return create(level, this.warrior, lvl, this.specialType);
        }

        /** Bosses had a plain constructor; the enderman, triton and ogre families used a strong mob (4, type). */
        @Override
        public @Nullable Entity getBoss(ServerLevel level, int x, int y, int z) {
            return create(level, this.boss, this.bossLvl, this.bossType);
        }

        @Override
        public String getEntityName() {
            return this.name;
        }
    }

    /** MobGoblinBlue: a green or a snow goblin at random; the boss is the goblin boss riding an orc (4, 3). */
    static final class GoblinBlue extends ByName {
        private final Random random = new Random();

        GoblinBlue() {
            super("goblinBlue", "SnowGoblin", 4, 1, "goblinBoss", -1, -1);
        }

        private String pick() {
            return this.random.nextBoolean() ? "Goblin" : "SnowGoblin";
        }

        @Override
        public @Nullable Entity getWarriorMob(ServerLevel level, int lvl, int x, int y, int z) {
            return create(level, this.pick(), lvl, -1);
        }

        @Override
        public @Nullable Entity getRangedMob(ServerLevel level, int lvl, int x, int y, int z) {
            return create(level, this.pick(), lvl, 4);
        }

        @Override
        public @Nullable Entity getSpecialMob(ServerLevel level, int lvl, int x, int y, int z) {
            return create(level, this.pick(), lvl, 1);
        }

        @Override
        public @Nullable Entity getBoss(ServerLevel level, int x, int y, int z) {
            Entity boss = create(level, "goblinBoss", -1, -1);
            Entity orc = create(level, "OrcArmored", 4, 3);
            if (boss != null && orc != null) {
                orc.setPos(x, y + 1, z);
                level.addFreshEntity(orc);
                boss.startRiding(orc, true, true);
            }
            return boss;
        }
    }

    /** MobBasedOnDistance (index 1, the "default"): the family by the distance from the world spawn, in 1000 block rings. */
    static final class BasedOnDistance extends BaseDungeonMonsters {
        private static BaseDungeonMonsters family(ServerLevel level, int x, int z) {
            BlockPos spawn = level.getRespawnData().pos();
            double dist = Math.sqrt((double) (spawn.getX() - x) * (spawn.getX() - x) + (double) (spawn.getZ() - z) * (spawn.getZ() - z));
            // skeleton, goblinBlue, zombie, gremlin, specter, minotaur, golem, walker; the gremlin and golem families were
            // never in the list in 1.5.2 (BetterDungeons.gremlin / .golem were used directly)
            int ring = (int) Math.min(7, Math.floor(dist / 1000.0));
            return switch (ring) {
                case 0 -> mobList.get(16);
                case 1 -> mobList.get(9);
                case 2 -> mobList.get(2);
                case 3 -> new ByName("gremlin", "Gremlin", 4, 1, "GremlinBoss", -1, -1);
                case 4 -> mobList.get(7);
                case 5 -> mobList.get(15);
                case 6 -> new ByName("golem", "GolemSmall", 4, 1, "GolemBoss", -1, -1);
                default -> mobList.get(6);
            };
        }

        @Override
        public @Nullable Entity getWarriorMob(ServerLevel level, int lvl, int x, int y, int z) {
            return family(level, x, z).getWarriorMob(level, lvl, x, y, z);
        }

        @Override
        public @Nullable Entity getRangedMob(ServerLevel level, int lvl, int x, int y, int z) {
            return family(level, x, z).getRangedMob(level, lvl, x, y, z);
        }

        @Override
        public @Nullable Entity getSpecialMob(ServerLevel level, int lvl, int x, int y, int z) {
            return family(level, x, z).getSpecialMob(level, lvl, x, y, z);
        }

        @Override
        public @Nullable Entity getBoss(ServerLevel level, int x, int y, int z) {
            return family(level, x, z).getBoss(level, x, y, z);
        }

        @Override
        public String getEntityName() {
            return "default";
        }

        @Override
        public int getWeight() {
            return 100;
        }
    }
}

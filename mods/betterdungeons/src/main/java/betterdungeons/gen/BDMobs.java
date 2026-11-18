package betterdungeons.gen;

import java.util.List;

/**
 * RegisterDungeonMobs.mobList as BetterDungeons.load filled it. Gremlin and golem were registered with the triton and
 * the ogre by mistake, so those two appear twice.
 * The triton is called "Triton", so "mob = triton" in a dungeon configuration finds nothing and means the mob of distance. Only the mob of distance has a weight (getMobIndex).
 */
public final class BDMobs {
    /** One entry: getEntityName (what DungeonConfig "mob" names), getSpawnerName (null: a random vanilla dungeon mob). */
    public record Mob(String name, String spawner, int weight) {
    }

    public static final List<Mob> LIST = List.of(
            new Mob("pigman", null, 0),
            new Mob("default", null, 100),
            new Mob("zombie", null, 0),
            new Mob("pirate", "BetterDungeons.Pirate", 0),
            new Mob("dwarf", "BetterDungeons.Dwarf", 0),
            new Mob("goblin", "BetterDungeons.Goblin", 0),
            new Mob("walker", null, 0),
            new Mob("specter", null, 0),
            new Mob("enderman", "BetterDungeons.ArmoredEnderman", 0),
            new Mob("goblinBlue", "BetterDungeons.SnowGoblin", 0),
            new Mob("monkey", null, 0),
            new Mob("Triton", "BetterDungeons.triton", 0),
            new Mob("Triton", "BetterDungeons.triton", 0),
            new Mob("ogre", "BetterDungeons.OgreArmored", 0),
            new Mob("ogre", "BetterDungeons.OgreArmored", 0),
            new Mob("minotaur", "BetterDungeons.minotaurArmored", 0),
            new Mob("skeleton", null, 0));
    /** basedOnDistance.getID(). */
    public static final int BASED_ON_DISTANCE = 1;

    private BDMobs() {
    }

    /** DungeonBase.getMonster: the id of the last mob with that name (mob.getID() = first index of the object). */
    public static int byName(String name) {
        int id = 1;
        for (int i = 0; i < LIST.size(); i++) {
            if (LIST.get(i).name.equals(name)) {
                id = LIST.indexOf(LIST.get(i));
            }
        }
        return id;
    }

    /** WorldGeneratorNew.getMobIndex. */
    public static int randomIndex(java.util.Random random) {
        int maxNum = 0;
        for (Mob mob : LIST) {
            maxNum += mob.weight;
        }
        int randomNum = random.nextInt(maxNum);
        int index = 0;
        int weightSum = LIST.get(0).weight;
        while (weightSum <= randomNum) {
            weightSum += LIST.get(++index).weight;
        }
        return index;
    }
}

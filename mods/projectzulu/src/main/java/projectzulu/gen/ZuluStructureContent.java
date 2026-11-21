package projectzulu.gen;

import hexxit.worldgen152.core.populate.OldEntity;
import hexxit.worldgen152.core.populate.OldTile;

/**
 * Entities and tile data Project Zulu's structures leave in the world. The entities are {@link OldEntity.Mob}s under
 * names of their own (the variant the original made with its constructor); {@code ZuluWorld} maps the names to the
 * entity types and sets them up ({@code OldGameIds.registerEntity}), and writes the tombstone text into the block
 * entity ({@code OldModData.registerTile}). No game classes (tools/expect.sh compiles this).
 */
public final class ZuluStructureContent {
    /** EntityMimic(world, x, y, z, true): a hovering mimic (labyrinth and pyramid dead ends). */
    public static final String MIMIC = "ProjectZulu|Core.Mimic#hovering";
    /**
     * EntityHauntedArmor(world, x, y, z, true) + initCreature (a random iron or gold set) + setPersistenceRequired
     * (cemetery). 1.5.2 drew the armour from the entity's own unseeded Random; the game draws it when it creates the
     * entity.
     */
    public static final String HAUNTED_ARMOR = "ProjectZulu|Core.Haunted Armor#hovering";
    /** Spawner mobs of the labyrinth (BlueprintLabyrinthHiddenWall), EntityList names of 1.5.2. */
    public static final String SPAWNER_MINOTAUR = "ProjectZulu|Core.Minotaur";
    public static final String SPAWNER_HAUNTED_ARMOR = "ProjectZulu|Core.Haunted Armor";
    /** Pyramid spawners (the original wrote the wrong name "Entity Mummy"; corrected). */
    public static final String SPAWNER_MUMMY = "ProjectZulu|Core.Mummy";

    private ZuluStructureContent() {
    }

    /** Both constructors set yaw and pitch 1.0 (setLocationAndAngles). */
    static OldEntity.Mob mimic(double x, double y, double z) {
        return new OldEntity.Mob(MIMIC, x, y, z, 1.0F, null);
    }

    static OldEntity.Mob hauntedArmor(double x, double y, double z) {
        return new OldEntity.Mob(HAUNTED_ARMOR, x, y, z, 1.0F, null);
    }

    /**
     * TileEntityTombstone: the four text lines (signText, at most 10 characters each). The cemetery set only the
     * block, so its graves got a fresh tile entity with empty lines.
     */
    public record Tombstone(String[] lines) implements OldTile.Mod {
        public static Tombstone empty() {
            return new Tombstone(new String[] {"", "", "", ""});
        }
    }
}

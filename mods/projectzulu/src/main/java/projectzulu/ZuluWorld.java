package projectzulu;

import hexxit.worldgen152.OldGameIds;
import hexxit.worldgen152.OldModData;
import net.neoforged.bus.api.IEventBus;
import projectzulu.block.gear.TombstoneBlockEntity;
import projectzulu.entity.HauntedArmorEntity;
import projectzulu.entity.MimicEntity;
import projectzulu.gen.ZuluStructureContent;

/**
 * Teil 11, batch World: what the structures of {@code projectzulu.gen.ZuluWorldGen} need in the game (the mobs of
 * their spawners and dead ends, the tombstone text).
 */
public final class ZuluWorld {
    private ZuluWorld() {
    }

    public static void register(IEventBus modBus) {
        registerStructureContent();
    }

    private static void registerStructureContent() {
        String ns = ProjectZulu.MOD_ID + ":";
        // spawners of the labyrinth (EntityList names of 1.5.2: mod id + "." + mob name)
        OldGameIds.registerEntity(ZuluStructureContent.SPAWNER_MINOTAUR, ns + "minotaur");
        OldGameIds.registerEntity(ZuluStructureContent.SPAWNER_HAUNTED_ARMOR, ns + "haunted_armor");
        // the mobs the structures put into the world, with what their constructor and the builder did
        OldGameIds.registerEntity(ZuluStructureContent.MIMIC, ns + "mimic", entity -> {
            if (entity instanceof MimicEntity mimic) {
                mimic.setHovering();
            }
        });
        OldGameIds.registerEntity(ZuluStructureContent.HAUNTED_ARMOR, ns + "haunted_armor", entity -> {
            if (entity instanceof HauntedArmorEntity armor) {
                armor.setHovering();
                armor.setRandomArmor(armor.getRandom());
                armor.setPersistenceRequired();
            }
        });
        OldModData.registerTile(ZuluStructureContent.Tombstone.class, (tombstone, blockEntity, registries) -> {
            if (blockEntity instanceof TombstoneBlockEntity entity) {
                for (int line = 0; line < TombstoneBlockEntity.LINES && line < tombstone.lines().length; line++) {
                    entity.setLine(line, tombstone.lines()[line]);
                }
            }
        });
    }
}

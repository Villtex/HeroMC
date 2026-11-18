package betterdungeons.gen;

import hexxit.worldgen152.core.populate.OldTile;

/**
 * BlockTileEntityMobSpawner of the mod's spawner block (blockID + 1 = 2002 in the pack): the dungeon mob (index into
 * {@link BDMobs#LIST}) and the kind of mob it makes (the metadata of the template's mob block, 15 = boss).
 */
public record BDSpawnerTile(int mob, int metadata) implements OldTile.Mod {
}

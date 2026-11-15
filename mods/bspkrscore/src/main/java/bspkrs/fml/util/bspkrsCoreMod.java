package bspkrs.fml.util;

import net.neoforged.fml.common.Mod;

/**
 * bspkrsCore 2.09 was a library for bspkrs' mods: its own configuration classes, an update checker, HUD helpers and
 * the BlockID / ItemID / Coord value types. Only the part TreeCapitator still needs is ported: {@link bspkrs.util.BlockID}.
 * Configuration is NeoForge's, positions are BlockPos, items are matched by tags; the update checker (allowUpdateCheck
 * was false in the pack anyway) and the HUD helpers (no mod of the pack used them) are gone.
 */
@Mod(bspkrsCoreMod.MOD_ID)
public final class bspkrsCoreMod {
    public static final String MOD_ID = "mod_bspkrscore";
}

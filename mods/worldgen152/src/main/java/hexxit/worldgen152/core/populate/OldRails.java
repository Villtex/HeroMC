package hexxit.worldgen152.core.populate;

import java.util.ArrayList;
import java.util.List;

/**
 * BlockRailBase.onBlockAdded with BlockBaseRailLogic: a rail that is set turns towards the rails next to
 * it and pulls them round. A lone rail ends up pointing north-south, whatever metadata it was set with.
 */
final class OldRails {
    private final OldWorld world;
    private final int x;
    private final int y;
    private final int z;
    private final boolean straightOnly;
    private final List<int[]> ends = new ArrayList<>();

    private OldRails(OldWorld world, int x, int y, int z) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        int id = world.getBlockId(x, y, z);
        int meta = world.getBlockMetadata(x, y, z);
        this.straightOnly = id != 66;
        this.setShape(this.straightOnly ? meta & 7 : meta);
    }

    static boolean isRail(int id) {
        return id == 66 || id == 27 || id == 28 || id == 157;
    }

    private static boolean isRailAt(OldWorld world, int x, int y, int z) {
        return isRail(world.getBlockId(x, y, z));
    }

    /** refreshTrackShape(world, x, y, z, true). */
    static void added(OldWorld world, int x, int y, int z) {
        new OldRails(world, x, y, z).refresh(false, true);
    }

    private void setShape(int shape) {
        this.ends.clear();
        int x = this.x;
        int y = this.y;
        int z = this.z;
        switch (shape) {
            case 0 -> this.ends(x, y, z - 1, x, y, z + 1);
            case 1 -> this.ends(x - 1, y, z, x + 1, y, z);
            case 2 -> this.ends(x - 1, y, z, x + 1, y + 1, z);
            case 3 -> this.ends(x - 1, y + 1, z, x + 1, y, z);
            case 4 -> this.ends(x, y + 1, z - 1, x, y, z + 1);
            case 5 -> this.ends(x, y, z - 1, x, y + 1, z + 1);
            case 6 -> this.ends(x + 1, y, z, x, y, z + 1);
            case 7 -> this.ends(x - 1, y, z, x, y, z + 1);
            case 8 -> this.ends(x - 1, y, z, x, y, z - 1);
            case 9 -> this.ends(x + 1, y, z, x, y, z - 1);
            default -> {
            }
        }
    }

    private void ends(int x1, int y1, int z1, int x2, int y2, int z2) {
        this.ends.add(new int[] {x1, y1, z1});
        this.ends.add(new int[] {x2, y2, z2});
    }

    private void refreshConnectedTracks() {
        for (int i = 0; i < this.ends.size(); i++) {
            OldRails other = this.logicAt(this.ends.get(i));
            if (other != null && other.pointsAt(this)) {
                this.ends.set(i, new int[] {other.x, other.y, other.z});
            } else {
                this.ends.remove(i--);
            }
        }
    }

    private boolean isTrack(int x, int y, int z) {
        return isRailAt(this.world, x, y, z) || isRailAt(this.world, x, y + 1, z) || isRailAt(this.world, x, y - 1, z);
    }

    private OldRails logicAt(int[] pos) {
        if (isRailAt(this.world, pos[0], pos[1], pos[2])) {
            return new OldRails(this.world, pos[0], pos[1], pos[2]);
        }
        if (isRailAt(this.world, pos[0], pos[1] + 1, pos[2])) {
            return new OldRails(this.world, pos[0], pos[1] + 1, pos[2]);
        }
        if (isRailAt(this.world, pos[0], pos[1] - 1, pos[2])) {
            return new OldRails(this.world, pos[0], pos[1] - 1, pos[2]);
        }
        return null;
    }

    private boolean pointsAt(OldRails other) {
        return this.hasEnd(other.x, other.z);
    }

    private boolean hasEnd(int x, int z) {
        for (int[] end : this.ends) {
            if (end[0] == x && end[2] == z) {
                return true;
            }
        }
        return false;
    }

    private boolean canConnectTo(OldRails other) {
        return this.pointsAt(other) || this.ends.size() != 2;
    }

    private void connectToNeighbor(OldRails other) {
        this.ends.add(new int[] {other.x, other.y, other.z});
        boolean north = this.hasEnd(this.x, this.z - 1);
        boolean south = this.hasEnd(this.x, this.z + 1);
        boolean west = this.hasEnd(this.x - 1, this.z);
        boolean east = this.hasEnd(this.x + 1, this.z);
        int shape = -1;
        if (north || south) {
            shape = 0;
        }
        if (west || east) {
            shape = 1;
        }
        if (!this.straightOnly) {
            if (south && east && !north && !west) {
                shape = 6;
            }
            if (south && west && !north && !east) {
                shape = 7;
            }
            if (north && west && !south && !east) {
                shape = 8;
            }
            if (north && east && !south && !west) {
                shape = 9;
            }
        }
        shape = this.slope(shape);
        if (shape < 0) {
            shape = 0;
        }
        this.store(shape);
    }

    private int slope(int shape) {
        if (shape == 0) {
            if (isRailAt(this.world, this.x, this.y + 1, this.z - 1)) {
                shape = 4;
            }
            if (isRailAt(this.world, this.x, this.y + 1, this.z + 1)) {
                shape = 5;
            }
        }
        if (shape == 1) {
            if (isRailAt(this.world, this.x + 1, this.y + 1, this.z)) {
                shape = 2;
            }
            if (isRailAt(this.world, this.x - 1, this.y + 1, this.z)) {
                shape = 3;
            }
        }
        return shape;
    }

    private int withFlags(int shape) {
        return this.straightOnly ? this.world.getBlockMetadata(this.x, this.y, this.z) & 8 | shape : shape;
    }

    private void store(int shape) {
        this.world.setBlockMetadata(this.x, this.y, this.z, this.withFlags(shape));
    }

    private boolean canConnectFrom(int x, int y, int z) {
        OldRails other = this.logicAt(new int[] {x, y, z});
        if (other == null) {
            return false;
        }
        other.refreshConnectedTracks();
        return other.canConnectTo(this);
    }

    /** BlockBaseRailLogic.func_94511_a. */
    private void refresh(boolean powered, boolean force) {
        boolean north = this.canConnectFrom(this.x, this.y, this.z - 1);
        boolean south = this.canConnectFrom(this.x, this.y, this.z + 1);
        boolean west = this.canConnectFrom(this.x - 1, this.y, this.z);
        boolean east = this.canConnectFrom(this.x + 1, this.y, this.z);
        int shape = -1;
        if ((north || south) && !west && !east) {
            shape = 0;
        }
        if ((west || east) && !north && !south) {
            shape = 1;
        }
        if (!this.straightOnly) {
            if (south && east && !north && !west) {
                shape = 6;
            }
            if (south && west && !north && !east) {
                shape = 7;
            }
            if (north && west && !south && !east) {
                shape = 8;
            }
            if (north && east && !south && !west) {
                shape = 9;
            }
        }
        if (shape == -1) {
            if (north || south) {
                shape = 0;
            }
            if (west || east) {
                shape = 1;
            }
            if (!this.straightOnly) {
                if (powered) {
                    if (south && east) {
                        shape = 6;
                    }
                    if (west && south) {
                        shape = 7;
                    }
                    if (east && north) {
                        shape = 9;
                    }
                    if (north && west) {
                        shape = 8;
                    }
                } else {
                    if (north && west) {
                        shape = 8;
                    }
                    if (east && north) {
                        shape = 9;
                    }
                    if (west && south) {
                        shape = 7;
                    }
                    if (south && east) {
                        shape = 6;
                    }
                }
            }
        }
        shape = this.slope(shape);
        if (shape < 0) {
            shape = 0;
        }
        this.setShape(shape);
        int meta = this.withFlags(shape);
        if (force || this.world.getBlockMetadata(this.x, this.y, this.z) != meta) {
            this.world.setBlockMetadata(this.x, this.y, this.z, meta);
            for (int i = 0; i < this.ends.size(); i++) {
                OldRails other = this.logicAt(this.ends.get(i));
                if (other != null) {
                    other.refreshConnectedTracks();
                    if (other.canConnectTo(this)) {
                        other.connectToNeighbor(this);
                    }
                }
            }
        }
    }
}

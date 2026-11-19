package dimdoors.gen;

import java.util.Set;

/** SchematicLoader.transformMetadata of Dimensional Doors 1.4.1, as it was (with its fall-throughs): block metadata turned for the dungeon. */
final class Rotation {
    /** DungeonHelper.registerFlipBlocks: only these are turned. */
    private static final Set<Integer> FLIP = Set.of(23, 109, 69, 77, 93, 94, 131, 50, 75, 76, 71, 64, 33, 29, 34, 149, 150, 63, 68, 144, 65, 106,
            145, 54, 146, 154, 114, 67, 156, 128);
    /** The stairs of 1.5.2 (BlockStairs). */
    private static final Set<Integer> STAIRS = Set.of(53, 67, 108, 109, 114, 128, 134, 135, 136, 156);

    private Rotation() {
    }

    public static int transformMetadata(int metadata, int orientation, int blockID) {
       if (FLIP.contains(blockID)) {
          switch (orientation) {
             case 0:
                if (STAIRS.contains(blockID)) {
                   switch (metadata) {
                      case 0:
                         return 2;
                      case 1:
                         return 3;
                      case 2:
                         return 1;
                      case 3:
                         return 0;
                      case 4:
                         return 6;
                      case 5:
                         return 7;
                      case 6:
                         return 5;
                      case 7:
                         metadata = 4;
                   }
                } else if (blockID == 54
                   || blockID == 146
                   || blockID == 65) {
                   switch (metadata) {
                      case 2:
                         return 5;
                      case 3:
                         return 4;
                      case 4:
                         return 2;
                      case 5:
                         metadata = 3;
                   }
                } else if (blockID == 106) {
                   switch (metadata) {
                      case 1:
                         return 2;
                      case 2:
                         int metadatax = 4;
                         return metadatax;
                      case 3:
                      case 5:
                      case 6:
                      case 7:
                      default:
                         return metadata;
                      case 4:
                         return 8;
                      case 8:
                         metadata = 1;
                   }
                } else if (blockID == 69
                   || blockID == 77
                   || blockID == 143
                   || blockID == 50
                   || blockID == 75
                   || blockID == 76) {
                   switch (metadata) {
                      case 1:
                         return 3;
                      case 2:
                         return 4;
                      case 3:
                         return 2;
                      case 4:
                         int metadatax = 1;
                         return metadatax;
                      case 5:
                      case 6:
                      case 7:
                      case 8:
                      default:
                         return metadata;
                      case 9:
                         return 11;
                      case 10:
                         return 12;
                      case 11:
                         return 10;
                      case 12:
                         metadata = 9;
                   }
                } else if (blockID == 33
                   || blockID == 34
                   || blockID == 29
                   || blockID == 23
                   || blockID == 158) {
                   switch (metadata) {
                      case 2:
                         return 5;
                      case 3:
                         return 4;
                      case 4:
                         return 2;
                      case 5:
                         int metadatax = 3;
                         return metadatax;
                      case 6:
                      case 7:
                      case 8:
                      case 9:
                      default:
                         return metadata;
                      case 10:
                         return 13;
                      case 11:
                         return 12;
                      case 12:
                         return 10;
                      case 13:
                         metadata = 11;
                   }
                } else if ((blockID == 93 || blockID == 94)
                   || (blockID == 64 || blockID == 71)
                   || blockID == 131
                   || (blockID == 149 || blockID == 150)) {
                   switch (metadata) {
                      case 0:
                         return 1;
                      case 1:
                         return 2;
                      case 2:
                         return 3;
                      case 3:
                         return 0;
                      case 4:
                         return 5;
                      case 5:
                         return 6;
                      case 6:
                         return 7;
                      case 7:
                         return 4;
                      case 8:
                         return 9;
                      case 9:
                         return 10;
                      case 10:
                         return 11;
                      case 11:
                         return 8;
                      case 12:
                         return 13;
                      case 13:
                         return 14;
                      case 14:
                         return 15;
                      case 15:
                         metadata = 12;
                   }
                }
                break;
             case 1:
                if (STAIRS.contains(blockID)) {
                   switch (metadata) {
                      case 0:
                         return 1;
                      case 1:
                         return 0;
                      case 2:
                         return 3;
                      case 3:
                         return 2;
                      case 4:
                         return 5;
                      case 5:
                         return 4;
                      case 6:
                         return 7;
                      case 7:
                         metadata = 6;
                   }
                } else if (blockID == 54
                   || blockID == 146
                   || blockID == 65) {
                   switch (metadata) {
                      case 2:
                         return 3;
                      case 3:
                         return 2;
                      case 4:
                         return 5;
                      case 5:
                         metadata = 4;
                   }
                } else if (blockID == 106) {
                   switch (metadata) {
                      case 1:
                         return 4;
                      case 2:
                         int metadatax = 8;
                         return metadatax;
                      case 3:
                      case 5:
                      case 6:
                      case 7:
                      default:
                         return metadata;
                      case 4:
                         return 1;
                      case 8:
                         metadata = 2;
                   }
                } else if (blockID == 69
                   || blockID == 50
                   || blockID == 75
                   || blockID == 76) {
                   switch (metadata) {
                      case 1:
                         return 2;
                      case 2:
                         return 1;
                      case 3:
                         return 4;
                      case 4:
                         int metadatax = 3;
                         return metadatax;
                      case 5:
                      case 6:
                      case 7:
                      case 8:
                      default:
                         return metadata;
                      case 9:
                         return 10;
                      case 10:
                         return 9;
                      case 11:
                         return 12;
                      case 12:
                         metadata = 11;
                   }
                } else if (blockID == 33
                   || blockID == 29
                   || blockID == 23
                   || blockID == 158) {
                   switch (metadata) {
                      case 2:
                         return 3;
                      case 3:
                         return 2;
                      case 4:
                         return 5;
                      case 5:
                         int metadatax = 4;
                         return metadatax;
                      case 6:
                      case 7:
                      case 8:
                      case 9:
                      default:
                         return metadata;
                      case 10:
                         return 11;
                      case 11:
                         return 10;
                      case 12:
                         return 13;
                      case 13:
                         metadata = 12;
                   }
                } else if ((blockID == 93 || blockID == 94)
                   || (blockID == 64 || blockID == 71)
                   || blockID == 131
                   || (blockID == 149 || blockID == 150)) {
                   switch (metadata) {
                      case 0:
                         return 2;
                      case 1:
                         return 3;
                      case 2:
                         return 0;
                      case 3:
                         return 1;
                      case 4:
                         return 6;
                      case 5:
                         return 7;
                      case 6:
                         return 4;
                      case 7:
                         return 5;
                      case 8:
                         return 10;
                      case 9:
                         return 11;
                      case 10:
                         return 8;
                      case 11:
                         return 9;
                      case 12:
                         return 14;
                      case 13:
                         return 15;
                      case 14:
                         return 12;
                      case 15:
                         metadata = 13;
                   }
                }
                break;
             case 2:
                if (STAIRS.contains(blockID)) {
                   switch (metadata) {
                      case 0:
                         metadata = 3;
                         break;
                      case 1:
                         metadata = 2;
                         break;
                      case 2:
                         metadata = 0;
                         break;
                      case 3:
                         metadata = 1;
                         break;
                      case 4:
                         metadata = 7;
                         break;
                      case 5:
                         metadata = 6;
                         break;
                      case 6:
                         metadata = 4;
                         break;
                      case 7:
                         metadata = 5;
                   }
                } else if (blockID == 54
                   || blockID == 146
                   || blockID == 65) {
                   switch (metadata) {
                      case 2:
                         metadata = 4;
                         break;
                      case 3:
                         metadata = 5;
                         break;
                      case 4:
                         metadata = 3;
                         break;
                      case 5:
                         metadata = 2;
                   }
                } else if (blockID == 106) {
                   switch (metadata) {
                      case 1:
                         metadata = 8;
                         break;
                      case 2:
                         metadata = 1;
                      case 3:
                      case 5:
                      case 6:
                      case 7:
                      default:
                         break;
                      case 4:
                         metadata = 2;
                         break;
                      case 8:
                         metadata = 4;
                   }
                } else if (blockID == 69
                   || blockID == 50
                   || blockID == 75
                   || blockID == 76) {
                   switch (metadata) {
                      case 1:
                         metadata = 4;
                         break;
                      case 2:
                         metadata = 3;
                         break;
                      case 3:
                         metadata = 1;
                         break;
                      case 4:
                         metadata = 2;
                      case 5:
                      case 6:
                      case 7:
                      case 8:
                      default:
                         break;
                      case 9:
                         metadata = 12;
                         break;
                      case 10:
                         metadata = 11;
                         break;
                      case 11:
                         metadata = 9;
                         break;
                      case 12:
                         metadata = 10;
                   }
                } else if (blockID == 33
                   || blockID == 29
                   || blockID == 23
                   || blockID == 158) {
                   switch (metadata) {
                      case 2:
                         metadata = 4;
                         break;
                      case 3:
                         metadata = 5;
                         break;
                      case 4:
                         metadata = 3;
                         break;
                      case 5:
                         metadata = 2;
                      case 6:
                      case 7:
                      case 8:
                      case 9:
                      default:
                         break;
                      case 10:
                         metadata = 12;
                         break;
                      case 11:
                         metadata = 13;
                         break;
                      case 12:
                         metadata = 11;
                         break;
                      case 13:
                         metadata = 10;
                   }
                } else if ((blockID == 93 || blockID == 94)
                   || (blockID == 64 || blockID == 71)
                   || blockID == 131
                   || (blockID == 149 || blockID == 150)) {
                   switch (metadata) {
                      case 0:
                         metadata = 3;
                         break;
                      case 1:
                         metadata = 0;
                         break;
                      case 2:
                         metadata = 1;
                         break;
                      case 3:
                         metadata = 2;
                         break;
                      case 4:
                         metadata = 7;
                         break;
                      case 5:
                         metadata = 4;
                         break;
                      case 6:
                         metadata = 5;
                         break;
                      case 7:
                         metadata = 6;
                         break;
                      case 8:
                         metadata = 11;
                         break;
                      case 9:
                         metadata = 8;
                         break;
                      case 10:
                         metadata = 9;
                         break;
                      case 11:
                         metadata = 10;
                         break;
                      case 12:
                         metadata = 15;
                         break;
                      case 13:
                         metadata = 12;
                         break;
                      case 14:
                         metadata = 13;
                         break;
                      case 15:
                         metadata = 14;
                   }
                }
             case 3:
          }
       }

       return metadata;
    }
}

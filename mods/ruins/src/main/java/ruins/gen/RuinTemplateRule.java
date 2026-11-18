package ruins.gen;

import hexxit.worldgen152.core.populate.OldEntity;
import hexxit.worldgen152.core.populate.OldLoot;
import hexxit.worldgen152.core.populate.OldStack;
import hexxit.worldgen152.core.populate.OldTile;
import hexxit.worldgen152.core.populate.OldWorld;

import java.util.Random;

/**
 * RuinTemplateRule of Ruins 10.3: one "rule=condition,chance,block,block..." line of a template. Every block of a
 * layer names a rule; the rule picks one of its blocks (or a special one: spawners, chests, end crystals) at random.
 * Blocks, metadata, rotation and the order of the random draws are those of the original.
 */
class RuinTemplateRule {
    private final int[] blockIDs;
    private final int[] blockMDs;
    private final String[] blockStrings;
    private final int chance;
    private final int condition;
    final RuinTemplate owner;

    RuinTemplateRule(RuinTemplate r, String rule) throws Exception {
        this.owner = r;
        String[] items = rule.split(",");
        int numblocks = items.length - 2;
        if (numblocks < 1) {
            throw new Exception("No blockIDs specified for rule [" + rule + "] in template " + this.owner.getName());
        }
        this.condition = Integer.parseInt(items[0]);
        this.chance = Integer.parseInt(items[1]);
        this.blockIDs = new int[numblocks];
        this.blockMDs = new int[numblocks];
        this.blockStrings = new String[numblocks];
        for (int i = 0; i < numblocks; i++) {
            String[] data = items[i + 2].split("-");
            if (data.length > 1) {
                if (isNumber(data[0])) {
                    this.blockIDs[i] = Integer.parseInt(data[0]);
                    this.blockMDs[i] = Integer.parseInt(data[1]);
                    this.blockStrings[i] = "";
                } else {
                    this.blockIDs[i] = RuinRotation.blockOfName(data[0]);
                    this.blockMDs[i] = Integer.parseInt(data[1]);
                    this.blockStrings[i] = items[i + 2];
                }
            } else if (!isNumber(data[0])) {
                this.blockIDs[i] = RuinRotation.blockOfName(data[0]);
                this.blockMDs[i] = 0;
                this.blockStrings[i] = items[i + 2];
            } else {
                this.blockIDs[i] = Integer.parseInt(items[i + 2]);
                this.blockMDs[i] = 0;
                this.blockStrings[i] = "";
            }
            // an id without a block of 1.5.2 (Block.blocksList null or ForgeFiller): the fallback switch of the
            // original ran on -1 and always ended in "setting to 0"
            if (this.blockIDs[i] > 0 && !RuinRotation.blockExists(this.blockIDs[i])) {
                this.blockIDs[i] = 0;
            }
        }
    }

    private static boolean isNumber(String s) {
        if (s == null || s.isEmpty()) {
            return false;
        }
        try {
            Integer.parseInt(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    void doBlock(OldWorld world, Random random, int x, int y, int z, int rotate) {
        if (random.nextInt(100) < this.chance) {
            switch (this.condition) {
                case 0 -> this.doNormalBlock(world, random, x, y, z, rotate);
                case 1 -> {
                    if (!this.owner.isAir(world.getBlockId(x, y - 1, z))) {
                        this.doNormalBlock(world, random, x, y, z, rotate);
                    }
                }
                case 2 -> {
                    if (!this.owner.isAir(world.getBlockId(x + 1, y, z)) || !this.owner.isAir(world.getBlockId(x, y, z + 1))
                            || !this.owner.isAir(world.getBlockId(x, y, z - 1)) || !this.owner.isAir(world.getBlockId(x - 1, y, z))) {
                        this.doNormalBlock(world, random, x, y, z, rotate);
                    }
                }
                case 3 -> {
                    if (!this.owner.isAir(world.getBlockId(x, y + 1, z))) {
                        this.doNormalBlock(world, random, x, y, z, rotate);
                    }
                }
                default -> {
                }
            }
        }
    }

    boolean runLater() {
        return this.condition == 2;
    }

    boolean runLast() {
        return this.condition == 3;
    }

    boolean canReplace(int blockID, int targetBlock) {
        return !this.owner.preserveBlock(targetBlock) || blockID != 0;
    }

    private void doNormalBlock(OldWorld world, Random random, int x, int y, int z, int rotate) {
        int blocknum = random.nextInt(this.blockIDs.length);
        if (this.blockIDs[blocknum] == -1) {
            this.doSpecialBlock(world, random, x, y, z, this.blockStrings[blocknum]);
        } else if (this.canReplace(this.blockIDs[blocknum], world.getBlockId(x, y, z))) {
            int metadata = rotate != 0 ? rotateMetadata(this.blockIDs[blocknum], this.blockMDs[blocknum], rotate) : this.blockMDs[blocknum];
            world.setBlock(x, y, z, this.blockIDs[blocknum], metadata);
        }
    }

    private void doSpecialBlock(OldWorld world, Random random, int x, int y, int z, String dataString) {
        if (dataString.equals("preserveBlock")) {
            return;
        }
        if (dataString.startsWith("MobSpawner:")) {
            addCustomSpawner(world, x, y, z, dataString.split(":")[1]);
        } else if (dataString.equals("UprightMobSpawn")) {
            addCustomSpawner(world, x, y, z, switch (random.nextInt(3)) {
                case 0 -> "Creeper";
                case 1 -> "Skeleton";
                default -> "Zombie";
            });
        } else if (dataString.equals("EasyMobSpawn")) {
            addCustomSpawner(world, x, y, z, random.nextInt(2) == 0 ? "Skeleton" : "Zombie");
        } else if (dataString.equals("MediumMobSpawn")) {
            addCustomSpawner(world, x, y, z, switch (random.nextInt(4)) {
                case 0 -> "Spider";
                case 1 -> "Skeleton";
                case 2 -> "CaveSpider";
                default -> "Zombie";
            });
        } else if (dataString.equals("HardMobSpawn")) {
            addCustomSpawner(world, x, y, z, switch (random.nextInt(4)) {
                case 0 -> "Creeper";
                case 1 -> "CaveSpider";
                case 2 -> "Skeleton";
                default -> "Blaze";
            });
        } else if (dataString.equals("EasyChest")) {
            addChest(world, random, x, y, z, random.nextInt(3) + 3, 0);
        } else if (dataString.equals("MediumChest")) {
            addChest(world, random, x, y, z, random.nextInt(4) + 3, 20);
        } else if (dataString.equals("HardChest")) {
            addChest(world, random, x, y, z, random.nextInt(5) + 3, 10);
        } else if (dataString.equals("EnderCrystal")) {
            spawnEnderCrystal(world, x, y, z);
        }
        // ChestGenHook: and CommandBlock: are not used by any template of the pack; anything else only logged
    }

    private static void spawnEnderCrystal(OldWorld world, int x, int y, int z) {
        // the yaw came from world.rand, which was not seed-bound: a fixed one here
        world.spawnEntity(new OldEntity.Mob("EnderCrystal", x + 0.5F, y, z + 0.5F, 0.0F, null));
        world.setBlock(x, y, z, 7, 0);
    }

    private static void addCustomSpawner(OldWorld world, int x, int y, int z, String id) {
        world.setBlock(x, y, z, 52, 0);
        world.setTile(x, y, z, new OldTile.Spawner(id));
    }

    /** The chest the world has there (setBlock of a chest on a chest keeps its tile entity), or a new one. */
    private static OldTile.Chest chestAt(OldWorld world, int x, int y, int z) {
        world.setBlock(x, y, z, 54, 0);
        if (world.getTile(x, y, z) instanceof OldTile.Chest chest) {
            return chest;
        }
        OldTile.Chest chest = new OldTile.Chest();
        world.setTile(x, y, z, chest);
        return chest;
    }

    /** addEasyChest (lootChance 0), addMediumChest (1 in 20 loot), addHardChest (1 in 10 loot). */
    private static void addChest(OldWorld world, Random random, int x, int y, int z, int items, int lootChance) {
        OldTile.Chest chest = chestAt(world, x, y, z);
        OldStack[] slots = chest.slots();
        for (int i = 0; i < items; i++) {
            OldStack stack;
            if (lootChance == 0) {
                stack = getNormalStack(random);
            } else if (random.nextInt(lootChance) < lootChance - 1) {
                stack = getNormalStack(random);
            } else {
                stack = getLootStack(random);
            }
            if (stack != null) {
                slots[random.nextInt(slots.length)] = stack;
            }
        }
    }

    private static OldStack stack(int id, int count) {
        return new OldStack(id, 0, count);
    }

    private static OldStack getNormalStack(Random random) {
        return switch (random.nextInt(25)) {
            case 0, 1 -> null;
            case 2, 3 -> stack(297, 1); // bread
            case 4, 5 -> stack(296, random.nextInt(8) + 8); // wheat
            case 6 -> stack(292, 1); // iron hoe
            case 7 -> stack(256, 1); // iron shovel
            case 8, 9 -> stack(287, random.nextInt(3) + 1); // string
            case 10, 11, 12 -> stack(295, random.nextInt(8) + 8); // seeds
            case 13, 14, 15 -> stack(281, random.nextInt(2) + 1); // bowl
            case 16 -> stack(325, 1); // bucket
            case 17 -> stack(260, 1); // apple
            case 18, 19 -> stack(352, random.nextInt(4) + 1); // bone
            case 20, 21 -> stack(344, random.nextInt(2) + 1); // egg
            case 22 -> stack(263, random.nextInt(5) + 3); // coal
            case 23 -> stack(265, random.nextInt(5) + 3); // iron ingot
            default -> getLootStack(random);
        };
    }

    private static OldStack getLootStack(Random random) {
        return switch (random.nextInt(25)) {
            case 0, 1, 2, 3 -> null;
            case 4, 5 -> stack(301, 1); // leather boots
            case 6, 7 -> stack(300, 1); // leather leggings
            case 8, 9 -> stack(259, 1); // flint and steel
            case 10, 11 -> stack(258, 1); // iron axe
            case 12 -> stack(267, 1); // iron sword
            case 13 -> stack(257, 1); // iron pickaxe
            case 14, 15 -> stack(306, 1); // iron helmet
            case 16 -> stack(303, 1); // chain chestplate
            case 17, 18 -> stack(340, random.nextInt(3) + 1); // book
            case 19 -> stack(345, 1); // compass
            case 20 -> stack(347, 1); // clock
            case 21 -> stack(331, random.nextInt(12) + 12); // redstone
            case 22 -> stack(322, 1); // golden apple
            case 23 -> stack(282, random.nextInt(2) + 1); // mushroom stew
            default -> oneDungeonItem(random);
        };
    }

    /** ChestGenHooks.getOneItem("dungeonChest"): one weighted entry, one stack of it (single items above the stack limit). */
    private static OldStack oneDungeonItem(Random random) {
        OldLoot.Entry[] items = OldLoot.DUNGEON_CHEST.items(random);
        int total = 0;
        for (OldLoot.Entry item : items) {
            total += item.weight();
        }
        int pick = random.nextInt(total);
        OldLoot.Entry chosen = null;
        for (OldLoot.Entry item : items) {
            pick -= item.weight();
            if (pick < 0) {
                chosen = item;
                break;
            }
        }
        int count = chosen.min() + random.nextInt(chosen.max() - chosen.min() + 1);
        return chosen.stack().withCount(count > OldLoot.stackLimit(chosen.stack().itemId()) ? 1 : count);
    }

    private static int rotateMetadata(int blockID, int metadata, int dir) {
       int tempdata = 0;
       switch (blockID) {
          case 23:
          case 61:
          case 62:
          case 65:
          case 68:
             switch (dir) {
                case 1:
                   if (metadata == 2) {
                      return 5;
                   }

                   if (metadata == 3) {
                      return 4;
                   }

                   if (metadata == 4) {
                      return 2;
                   }

                   if (metadata == 5) {
                      return 3;
                   }
                case 2:
                   if (metadata == 2) {
                      return 3;
                   }

                   if (metadata == 3) {
                      return 2;
                   }

                   if (metadata == 4) {
                      return 5;
                   }

                   if (metadata == 5) {
                      return 4;
                   }
                case 3:
                   if (metadata == 2) {
                      return 4;
                   }

                   if (metadata == 3) {
                      return 5;
                   }

                   if (metadata == 4) {
                      return 3;
                   }

                   if (metadata == 5) {
                      return 2;
                   }

                   return metadata + tempdata;
                default:
                   return metadata + tempdata;
             }
          case 24:
          case 25:
          case 29:
          case 30:
          case 31:
          case 32:
          case 33:
          case 34:
          case 35:
          case 36:
          case 37:
          case 38:
          case 39:
          case 40:
          case 41:
          case 42:
          case 43:
          case 44:
          case 45:
          case 46:
          case 47:
          case 48:
          case 49:
          case 51:
          case 52:
          case 53:
          case 54:
          case 55:
          case 56:
          case 57:
          case 58:
          case 59:
          case 60:
          case 67:
          case 70:
          case 72:
          case 73:
          case 74:
          case 78:
          case 79:
          case 80:
          case 81:
          case 82:
          case 83:
          case 84:
          case 85:
          case 87:
          case 88:
          case 89:
          case 90:
          case 92:
          case 95:
          case 97:
          case 98:
          case 99:
          case 100:
          case 101:
          case 102:
          case 103:
          case 104:
          case 105:
          default:
             return RuinRotation.getMapping(blockID, metadata, dir);
          case 26:
             if (metadata - 8 >= 0) {
                tempdata += 8;
                metadata -= 8;
             }

             switch (dir) {
                case 1:
                   if (metadata == 0) {
                      return 1 + tempdata;
                   }

                   if (metadata == 1) {
                      return 2 + tempdata;
                   }

                   if (metadata == 2) {
                      return 3 + tempdata;
                   }

                   if (metadata == 3) {
                      return 0 + tempdata;
                   }
                case 2:
                   if (metadata == 0) {
                      return 2 + tempdata;
                   }

                   if (metadata == 1) {
                      return 3 + tempdata;
                   }

                   if (metadata == 2) {
                      return 0 + tempdata;
                   }

                   if (metadata == 3) {
                      return 1 + tempdata;
                   }
                case 3:
                   if (metadata == 0) {
                      return 3 + tempdata;
                   }

                   if (metadata == 1) {
                      return 0 + tempdata;
                   }

                   if (metadata == 2) {
                      return 1 + tempdata;
                   }

                   if (metadata == 3) {
                      return 2 + tempdata;
                   }

                   return metadata + tempdata;
                default:
                   return metadata + tempdata;
             }
          case 27:
          case 28:
          case 66:
             switch (dir) {
                case 1:
                   if (metadata == 0) {
                      return 1;
                   }

                   if (metadata == 1) {
                      return 0;
                   }

                   if (metadata == 2) {
                      return 5;
                   }

                   if (metadata == 3) {
                      return 4;
                   }

                   if (metadata == 4) {
                      return 2;
                   }

                   if (metadata == 5) {
                      return 3;
                   }

                   if (metadata == 6) {
                      return 7;
                   }

                   if (metadata == 7) {
                      return 8;
                   }

                   if (metadata == 8) {
                      return 9;
                   }

                   if (metadata == 9) {
                      return 6;
                   }
                case 2:
                   if (metadata == 0) {
                      return 0;
                   }

                   if (metadata == 1) {
                      return 1;
                   }

                   if (metadata == 2) {
                      return 3;
                   }

                   if (metadata == 3) {
                      return 2;
                   }

                   if (metadata == 4) {
                      return 5;
                   }

                   if (metadata == 5) {
                      return 4;
                   }

                   if (metadata == 6) {
                      return 8;
                   }

                   if (metadata == 7) {
                      return 9;
                   }

                   if (metadata == 8) {
                      return 6;
                   }

                   if (metadata == 9) {
                      return 7;
                   }
                case 3:
                   if (metadata == 0) {
                      return 1;
                   }

                   if (metadata == 1) {
                      return 0;
                   }

                   if (metadata == 2) {
                      return 4;
                   }

                   if (metadata == 3) {
                      return 5;
                   }

                   if (metadata == 4) {
                      return 3;
                   }

                   if (metadata == 5) {
                      return 2;
                   }

                   if (metadata == 6) {
                      return 9;
                   }

                   if (metadata == 7) {
                      return 6;
                   }

                   if (metadata == 8) {
                      return 7;
                   }

                   if (metadata == 9) {
                      return 8;
                   }

                   return metadata + tempdata;
                default:
                   return metadata + tempdata;
             }
          case 50:
          case 69:
          case 75:
          case 76:
          case 77:
             int tempdatax = 0;
             if (blockID != 69 && blockID != 77) {
                if (metadata == 5) {
                   return metadata;
                }
             } else {
                if (metadata - 8 > 0) {
                   tempdatax += 8;
                   metadata -= 8;
                }

                if (blockID == 69 && (metadata == 5 || metadata == 6)) {
                   return metadata + tempdatax;
                }
             }

             switch (dir) {
                case 1:
                   if (metadata == 1) {
                      return 3 + tempdatax;
                   }

                   if (metadata == 2) {
                      return 4 + tempdatax;
                   }

                   if (metadata == 3) {
                      return 2 + tempdatax;
                   }

                   if (metadata == 4) {
                      return 1 + tempdatax;
                   }
                case 2:
                   if (metadata == 1) {
                      return 2 + tempdatax;
                   }

                   if (metadata == 2) {
                      return 1 + tempdatax;
                   }

                   if (metadata == 3) {
                      return 4 + tempdatax;
                   }

                   if (metadata == 4) {
                      return 3 + tempdatax;
                   }
                case 3:
                   if (metadata == 1) {
                      return 4 + tempdatax;
                   }

                   if (metadata == 2) {
                      return 3 + tempdatax;
                   }

                   if (metadata == 3) {
                      return 1 + tempdatax;
                   }

                   if (metadata == 4) {
                      return 2 + tempdatax;
                   }

                   return metadata + tempdatax;
                default:
                   return metadata + tempdatax;
             }
          case 63:
             switch (dir) {
                case 1:
                   if (metadata == 0) {
                      return 4;
                   }

                   if (metadata == 1) {
                      return 5;
                   }

                   if (metadata == 2) {
                      return 6;
                   }

                   if (metadata == 3) {
                      return 7;
                   }

                   if (metadata == 4) {
                      return 8;
                   }

                   if (metadata == 5) {
                      return 9;
                   }

                   if (metadata == 6) {
                      return 10;
                   }

                   if (metadata == 7) {
                      return 11;
                   }

                   if (metadata == 8) {
                      return 12;
                   }

                   if (metadata == 9) {
                      return 13;
                   }

                   if (metadata == 10) {
                      return 14;
                   }

                   if (metadata == 11) {
                      return 15;
                   }

                   if (metadata == 12) {
                      return 0;
                   }

                   if (metadata == 13) {
                      return 1;
                   }

                   if (metadata == 14) {
                      return 2;
                   }

                   if (metadata == 15) {
                      return 3;
                   }
                case 2:
                   if (metadata == 0) {
                      return 8;
                   }

                   if (metadata == 1) {
                      return 9;
                   }

                   if (metadata == 2) {
                      return 10;
                   }

                   if (metadata == 3) {
                      return 11;
                   }

                   if (metadata == 4) {
                      return 12;
                   }

                   if (metadata == 5) {
                      return 13;
                   }

                   if (metadata == 6) {
                      return 14;
                   }

                   if (metadata == 7) {
                      return 15;
                   }

                   if (metadata == 8) {
                      return 0;
                   }

                   if (metadata == 9) {
                      return 1;
                   }

                   if (metadata == 10) {
                      return 2;
                   }

                   if (metadata == 11) {
                      return 3;
                   }

                   if (metadata == 12) {
                      return 4;
                   }

                   if (metadata == 13) {
                      return 5;
                   }

                   if (metadata == 14) {
                      return 6;
                   }

                   if (metadata == 15) {
                      return 7;
                   }
                case 3:
                   if (metadata == 0) {
                      return 12;
                   }

                   if (metadata == 1) {
                      return 13;
                   }

                   if (metadata == 2) {
                      return 14;
                   }

                   if (metadata == 3) {
                      return 15;
                   }

                   if (metadata == 4) {
                      return 0;
                   }

                   if (metadata == 5) {
                      return 1;
                   }

                   if (metadata == 6) {
                      return 2;
                   }

                   if (metadata == 7) {
                      return 3;
                   }

                   if (metadata == 8) {
                      return 4;
                   }

                   if (metadata == 9) {
                      return 5;
                   }

                   if (metadata == 10) {
                      return 6;
                   }

                   if (metadata == 11) {
                      return 7;
                   }

                   if (metadata == 12) {
                      return 8;
                   }

                   if (metadata == 13) {
                      return 9;
                   }

                   if (metadata == 14) {
                      return 10;
                   }

                   if (metadata == 15) {
                      return 11;
                   }

                   return RuinRotation.getMapping(blockID, metadata, dir);
                default:
                   return RuinRotation.getMapping(blockID, metadata, dir);
             }
          case 64:
          case 71:
          case 96:
             if (metadata - 8 >= 0) {
                tempdata += 8;
                metadata -= 8;
             }

             if (metadata - 4 >= 0) {
                tempdata += 4;
                metadata -= 4;
             }

             switch (dir) {
                case 1:
                   if (metadata == 0) {
                      return 1 + tempdata;
                   }

                   if (metadata == 1) {
                      return 2 + tempdata;
                   }

                   if (metadata == 2) {
                      return 3 + tempdata;
                   }

                   if (metadata == 3) {
                      return 0 + tempdata;
                   }
                case 2:
                   if (metadata == 0) {
                      return 2 + tempdata;
                   }

                   if (metadata == 1) {
                      return 3 + tempdata;
                   }

                   if (metadata == 2) {
                      return 0 + tempdata;
                   }

                   if (metadata == 3) {
                      return 1 + tempdata;
                   }
                case 3:
                   if (metadata == 0) {
                      return 3 + tempdata;
                   }

                   if (metadata == 1) {
                      return 0 + tempdata;
                   }

                   if (metadata == 2) {
                      return 1 + tempdata;
                   }

                   if (metadata == 3) {
                      return 2 + tempdata;
                   }

                   return metadata + tempdata;
                default:
                   return metadata + tempdata;
             }
          case 86:
          case 91:
          case 93:
          case 94:
             if ((blockID == 93 || blockID == 94) && metadata - 4 >= 0) {
                if (metadata - 8 >= 0) {
                   if (metadata - 12 >= 0) {
                      tempdata += 12;
                      metadata -= 12;
                   } else {
                      tempdata += 8;
                      metadata -= 8;
                   }
                } else {
                   tempdata += 4;
                   metadata -= 4;
                }
             }

             switch (dir) {
                case 1:
                   if (metadata == 0) {
                      return 1 + tempdata;
                   }

                   if (metadata == 1) {
                      return 2 + tempdata;
                   }

                   if (metadata == 2) {
                      return 3 + tempdata;
                   }

                   if (metadata == 3) {
                      return 0 + tempdata;
                   }
                case 2:
                   if (metadata == 0) {
                      return 2 + tempdata;
                   }

                   if (metadata == 1) {
                      return 3 + tempdata;
                   }

                   if (metadata == 2) {
                      return 0 + tempdata;
                   }

                   if (metadata == 3) {
                      return 1 + tempdata;
                   }
                case 3:
                   if (metadata == 0) {
                      return 3 + tempdata;
                   }

                   if (metadata == 1) {
                      return 0 + tempdata;
                   }

                   if (metadata == 2) {
                      return 1 + tempdata;
                   }

                   if (metadata == 3) {
                      return 2 + tempdata;
                   }

                   return metadata + tempdata;
                default:
                   return metadata + tempdata;
             }
          case 106:
             switch (dir) {
                case 1:
                   if (metadata == 8) {
                      return 1;
                   }

                   if (metadata == 1) {
                      return 2;
                   }

                   if (metadata == 2) {
                      return 4;
                   }

                   if (metadata == 4) {
                      return 8;
                   }
                case 2:
                   if (metadata == 8) {
                      return 2;
                   }

                   if (metadata == 1) {
                      return 4;
                   }

                   if (metadata == 2) {
                      return 8;
                   }

                   if (metadata == 4) {
                      return 1;
                   }
                case 3:
                   if (metadata == 8) {
                      return 4;
                   }

                   if (metadata == 1) {
                      return 8;
                   }

                   if (metadata == 2) {
                      return 1;
                   }

                   if (metadata == 4) {
                      return 2;
                   }
             }
       }

       return metadata + tempdata;
    }
}

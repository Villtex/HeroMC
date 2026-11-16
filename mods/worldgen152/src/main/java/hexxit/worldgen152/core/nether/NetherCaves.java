package hexxit.worldgen152.core.nether;

import hexxit.worldgen152.core.OldMath;

import java.util.Random;

/** MapGenCavesHell: the wide tunnels of the Nether. They cut netherrack only and stop at lava. */
final class NetherCaves {
    private static final int RANGE = 8;
    private static final int LAVA_MOVING = 10;
    private static final int LAVA_STILL = 11;
    private static final int NETHERRACK = 87;
    private static final int DIRT = 3;
    private static final int GRASS = 2;

    private final long seed;

    NetherCaves(long seed) {
        this.seed = seed;
    }

    /** MapGenBase.generate. */
    void carve(int chunkX, int chunkZ, byte[] blocks) {
        Random rand = new Random(this.seed);
        long xMul = rand.nextLong();
        long zMul = rand.nextLong();
        for (int x = chunkX - RANGE; x <= chunkX + RANGE; x++) {
            for (int z = chunkZ - RANGE; z <= chunkZ + RANGE; z++) {
                long a = x * xMul;
                long b = z * zMul;
                rand.setSeed(a ^ b ^ this.seed);
                this.recursiveGenerate(rand, x, z, chunkX, chunkZ, blocks);
            }
        }
    }

    private void recursiveGenerate(Random rand, int originX, int originZ, int chunkX, int chunkZ, byte[] blocks) {
        int count = rand.nextInt(rand.nextInt(rand.nextInt(10) + 1) + 1);
        if (rand.nextInt(5) != 0) {
            count = 0;
        }

        for (int i = 0; i < count; i++) {
            double x = originX * 16 + rand.nextInt(16);
            double y = rand.nextInt(128);
            double z = originZ * 16 + rand.nextInt(16);
            int tunnels = 1;
            if (rand.nextInt(4) == 0) {
                // generateLargeCaveNode
                this.generateCaveNode(rand.nextLong(), chunkX, chunkZ, blocks, x, y, z, 1.0F + rand.nextFloat() * 6.0F, 0.0F, 0.0F, -1, -1, 0.5);
                tunnels += rand.nextInt(4);
            }

            for (int j = 0; j < tunnels; j++) {
                float yaw = rand.nextFloat() * (float) Math.PI * 2.0F;
                float pitch = (rand.nextFloat() - 0.5F) * 2.0F / 8.0F;
                float width = rand.nextFloat() * 2.0F + rand.nextFloat();
                this.generateCaveNode(rand.nextLong(), chunkX, chunkZ, blocks, x, y, z, width * 2.0F, yaw, pitch, 0, 0, 0.5);
            }
        }
    }

    private void generateCaveNode(long seed, int chunkX, int chunkZ, byte[] blocks, double x, double y, double z, float width, float yaw,
                                  float pitch, int step, int steps, double heightScale) {
        double centerX = chunkX * 16 + 8;
        double centerZ = chunkZ * 16 + 8;
        float yawChange = 0.0F;
        float pitchChange = 0.0F;
        Random random = new Random(seed);
        if (steps <= 0) {
            int max = RANGE * 16 - 16;
            steps = max - random.nextInt(max / 4);
        }

        boolean room = false;
        if (step == -1) {
            step = steps / 2;
            room = true;
        }

        int fork = random.nextInt(steps / 2) + steps / 4;
        boolean steep = random.nextInt(6) == 0;

        while (step < steps) {
            double radius = 1.5 + OldMath.sin(step * (float) Math.PI / steps) * width * 1.0F;
            double radiusY = radius * heightScale;
            float cosPitch = OldMath.cos(pitch);
            float sinPitch = OldMath.sin(pitch);
            x += OldMath.cos(yaw) * cosPitch;
            y += sinPitch;
            z += OldMath.sin(yaw) * cosPitch;
            if (steep) {
                pitch *= 0.92F;
            } else {
                pitch *= 0.7F;
            }

            pitch += pitchChange * 0.1F;
            yaw += yawChange * 0.1F;
            pitchChange *= 0.9F;
            yawChange *= 0.75F;
            pitchChange += (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 2.0F;
            yawChange += (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 4.0F;
            if (!room && step == fork && width > 1.0F) {
                this.generateCaveNode(random.nextLong(), chunkX, chunkZ, blocks, x, y, z, random.nextFloat() * 0.5F + 0.5F,
                        yaw - (float) (Math.PI / 2), pitch / 3.0F, step, steps, 1.0);
                this.generateCaveNode(random.nextLong(), chunkX, chunkZ, blocks, x, y, z, random.nextFloat() * 0.5F + 0.5F,
                        yaw + (float) (Math.PI / 2), pitch / 3.0F, step, steps, 1.0);
                return;
            }

            if (room || random.nextInt(4) != 0) {
                double dx = x - centerX;
                double dz = z - centerZ;
                double left = steps - step;
                double reach = width + 2.0F + 16.0F;
                if (dx * dx + dz * dz - left * left > reach * reach) {
                    return;
                }

                if (!(x < centerX - 16.0 - radius * 2.0)
                        && !(z < centerZ - 16.0 - radius * 2.0)
                        && !(x > centerX + 16.0 + radius * 2.0)
                        && !(z > centerZ + 16.0 + radius * 2.0)) {
                    int minX = OldMath.floor(x - radius) - chunkX * 16 - 1;
                    int maxX = OldMath.floor(x + radius) - chunkX * 16 + 1;
                    int minY = OldMath.floor(y - radiusY) - 1;
                    int maxY = OldMath.floor(y + radiusY) + 1;
                    int minZ = OldMath.floor(z - radius) - chunkZ * 16 - 1;
                    int maxZ = OldMath.floor(z + radius) - chunkZ * 16 + 1;
                    if (minX < 0) {
                        minX = 0;
                    }

                    if (maxX > 16) {
                        maxX = 16;
                    }

                    if (minY < 1) {
                        minY = 1;
                    }

                    if (maxY > 120) {
                        maxY = 120;
                    }

                    if (minZ < 0) {
                        minZ = 0;
                    }

                    if (maxZ > 16) {
                        maxZ = 16;
                    }

                    boolean lava = false;

                    for (int bx = minX; !lava && bx < maxX; bx++) {
                        for (int bz = minZ; !lava && bz < maxZ; bz++) {
                            for (int by = maxY + 1; !lava && by >= minY - 1; by--) {
                                int index = (bx * 16 + bz) * 128 + by;
                                if (by >= 0 && by < 128) {
                                    if (blocks[index] == LAVA_MOVING || blocks[index] == LAVA_STILL) {
                                        lava = true;
                                    }

                                    if (by != minY - 1 && bx != minX && bx != maxX - 1 && bz != minZ && bz != maxZ - 1) {
                                        by = minY;
                                    }
                                }
                            }
                        }
                    }

                    if (!lava) {
                        for (int bx = minX; bx < maxX; bx++) {
                            double nx = (bx + chunkX * 16 + 0.5 - x) / radius;

                            for (int bz = minZ; bz < maxZ; bz++) {
                                double nz = (bz + chunkZ * 16 + 0.5 - z) / radius;
                                int index = (bx * 16 + bz) * 128 + maxY;

                                for (int by = maxY - 1; by >= minY; by--) {
                                    double ny = (by + 0.5 - y) / radiusY;
                                    if (ny > -0.7 && nx * nx + ny * ny + nz * nz < 1.0) {
                                        byte block = blocks[index];
                                        if (block == NETHERRACK || block == DIRT || block == GRASS) {
                                            blocks[index] = 0;
                                        }
                                    }

                                    index--;
                                }
                            }
                        }

                        if (room) {
                            break;
                        }
                    }
                }
            }

            step++;
        }
    }
}

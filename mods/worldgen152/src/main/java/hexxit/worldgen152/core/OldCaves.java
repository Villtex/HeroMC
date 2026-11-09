package hexxit.worldgen152.core;

import java.util.Random;

/** Cave systems of 1.5.2 (MapGenCaves). */
final class OldCaves extends OldCarver {
    OldCaves(long seed) {
        super(seed);
    }

    @Override
    protected void start(Random rand, int originX, int originZ, int chunkX, int chunkZ, byte[] blocks, OldBiome[] biomes) {
        int count = rand.nextInt(rand.nextInt(rand.nextInt(40) + 1) + 1);
        if (rand.nextInt(15) != 0) {
            count = 0;
        }

        for (int i = 0; i < count; i++) {
            double x = originX * 16 + rand.nextInt(16);
            double y = rand.nextInt(rand.nextInt(120) + 8);
            double z = originZ * 16 + rand.nextInt(16);
            int tunnels = 1;
            if (rand.nextInt(4) == 0) {
                this.room(rand, rand.nextLong(), chunkX, chunkZ, blocks, biomes, x, y, z);
                tunnels += rand.nextInt(4);
            }

            for (int j = 0; j < tunnels; j++) {
                float yaw = rand.nextFloat() * (float) Math.PI * 2.0F;
                float pitch = (rand.nextFloat() - 0.5F) * 2.0F / 8.0F;
                float width = rand.nextFloat() * 2.0F + rand.nextFloat();
                if (rand.nextInt(10) == 0) {
                    width *= rand.nextFloat() * rand.nextFloat() * 3.0F + 1.0F;
                }

                this.tunnel(rand.nextLong(), chunkX, chunkZ, blocks, biomes, x, y, z, width, yaw, pitch, 0, 0, 1.0);
            }
        }
    }

    private void room(Random rand, long seed, int chunkX, int chunkZ, byte[] blocks, OldBiome[] biomes, double x, double y, double z) {
        this.tunnel(seed, chunkX, chunkZ, blocks, biomes, x, y, z, 1.0F + rand.nextFloat() * 6.0F, 0.0F, 0.0F, -1, -1, 0.5);
    }

    private void tunnel(long seed, int chunkX, int chunkZ, byte[] blocks, OldBiome[] biomes, double x, double y, double z,
                        float width, float yaw, float pitch, int step, int length, double heightScale) {
        double centerX = chunkX * 16 + 8;
        double centerZ = chunkZ * 16 + 8;
        float yawDrift = 0.0F;
        float pitchDrift = 0.0F;
        Random random = new Random(seed);
        if (length <= 0) {
            int max = RANGE * 16 - 16;
            length = max - random.nextInt(max / 4);
        }

        boolean isRoom = false;
        if (step == -1) {
            step = length / 2;
            isRoom = true;
        }

        int branchAt = random.nextInt(length / 2) + length / 4;
        boolean steep = random.nextInt(6) == 0;

        while (step < length) {
            double radius = 1.5 + OldMath.sin(step * (float) Math.PI / length) * width * 1.0F;
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

            pitch += pitchDrift * 0.1F;
            yaw += yawDrift * 0.1F;
            pitchDrift *= 0.9F;
            yawDrift *= 0.75F;
            pitchDrift += (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 2.0F;
            yawDrift += (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 4.0F;
            if (!isRoom && step == branchAt && width > 1.0F && length > 0) {
                this.tunnel(random.nextLong(), chunkX, chunkZ, blocks, biomes, x, y, z, random.nextFloat() * 0.5F + 0.5F,
                        yaw - (float) (Math.PI / 2), pitch / 3.0F, step, length, 1.0);
                this.tunnel(random.nextLong(), chunkX, chunkZ, blocks, biomes, x, y, z, random.nextFloat() * 0.5F + 0.5F,
                        yaw + (float) (Math.PI / 2), pitch / 3.0F, step, length, 1.0);
                return;
            }

            if (isRoom || random.nextInt(4) != 0) {
                double dx = x - centerX;
                double dz = z - centerZ;
                double remaining = length - step;
                double reach = width + 2.0F + 16.0F;
                if (dx * dx + dz * dz - remaining * remaining > reach * reach) {
                    return;
                }

                if (x >= centerX - 16.0 - radius * 2.0 && z >= centerZ - 16.0 - radius * 2.0
                        && x <= centerX + 16.0 + radius * 2.0 && z <= centerZ + 16.0 + radius * 2.0) {
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

                    boolean hitsWater = false;

                    for (int bx = minX; !hitsWater && bx < maxX; bx++) {
                        for (int bz = minZ; !hitsWater && bz < maxZ; bz++) {
                            for (int by = maxY + 1; !hitsWater && by >= minY - 1; by--) {
                                int index = (bx * 16 + bz) * 128 + by;
                                if (by >= 0 && by < 128) {
                                    if (isWater(blocks[index])) {
                                        hitsWater = true;
                                    }

                                    if (by != minY - 1 && bx != minX && bx != maxX - 1 && bz != minZ && bz != maxZ - 1) {
                                        by = minY;
                                    }
                                }
                            }
                        }
                    }

                    if (!hitsWater) {
                        for (int bx = minX; bx < maxX; bx++) {
                            double nx = (bx + chunkX * 16 + 0.5 - x) / radius;

                            for (int bz = minZ; bz < maxZ; bz++) {
                                double nz = (bz + chunkZ * 16 + 0.5 - z) / radius;
                                int index = (bx * 16 + bz) * 128 + maxY;
                                boolean foundTop = false;
                                if (nx * nx + nz * nz < 1.0) {
                                    for (int by = maxY - 1; by >= minY; by--) {
                                        double ny = (by + 0.5 - y) / radiusY;
                                        if (ny > -0.7 && nx * nx + ny * ny + nz * nz < 1.0) {
                                            if (isTopBlock(blocks, index, bx, bz, biomes)) {
                                                foundTop = true;
                                            }

                                            dig(blocks, index, bx, by, bz, biomes, foundTop);
                                        }

                                        index--;
                                    }
                                }
                            }
                        }

                        if (isRoom) {
                            break;
                        }
                    }
                }
            }

            step++;
        }
    }
}

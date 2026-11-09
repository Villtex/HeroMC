package hexxit.worldgen152.core;

import java.util.Random;

/** Ravines of 1.5.2 (MapGenRavine). */
final class OldRavines extends OldCarver {
    OldRavines(long seed) {
        super(seed);
    }

    @Override
    protected void start(Random rand, int originX, int originZ, int chunkX, int chunkZ, byte[] blocks, OldBiome[] biomes) {
        if (rand.nextInt(50) == 0) {
            double x = originX * 16 + rand.nextInt(16);
            double y = rand.nextInt(rand.nextInt(40) + 8) + 20;
            double z = originZ * 16 + rand.nextInt(16);
            byte count = 1;

            for (int i = 0; i < count; i++) {
                float yaw = rand.nextFloat() * (float) Math.PI * 2.0F;
                float pitch = (rand.nextFloat() - 0.5F) * 2.0F / 8.0F;
                float width = (rand.nextFloat() * 2.0F + rand.nextFloat()) * 2.0F;
                this.ravine(rand.nextLong(), chunkX, chunkZ, blocks, biomes, x, y, z, width, yaw, pitch, 0, 0, 3.0);
            }
        }
    }

    private void ravine(long seed, int chunkX, int chunkZ, byte[] blocks, OldBiome[] biomes, double x, double y, double z,
                        float width, float yaw, float pitch, int step, int length, double heightScale) {
        Random random = new Random(seed);
        double centerX = chunkX * 16 + 8;
        double centerZ = chunkZ * 16 + 8;
        float yawDrift = 0.0F;
        float pitchDrift = 0.0F;
        if (length <= 0) {
            int max = RANGE * 16 - 16;
            length = max - random.nextInt(max / 4);
        }

        boolean single = false;
        if (step == -1) {
            step = length / 2;
            single = true;
        }

        float[] widthAtY = new float[1024];
        float f5 = 1.0F;

        for (int i = 0; i < 128; i++) {
            if (i == 0 || random.nextInt(3) == 0) {
                f5 = 1.0F + random.nextFloat() * random.nextFloat() * 1.0F;
            }

            widthAtY[i] = f5 * f5;
        }

        for (; step < length; step++) {
            double radius = 1.5 + OldMath.sin(step * (float) Math.PI / length) * width * 1.0F;
            double radiusY = radius * heightScale;
            radius *= random.nextFloat() * 0.25 + 0.75;
            radiusY *= random.nextFloat() * 0.25 + 0.75;
            float cosPitch = OldMath.cos(pitch);
            float sinPitch = OldMath.sin(pitch);
            x += OldMath.cos(yaw) * cosPitch;
            y += sinPitch;
            z += OldMath.sin(yaw) * cosPitch;
            pitch *= 0.7F;
            pitch += pitchDrift * 0.05F;
            yaw += yawDrift * 0.05F;
            pitchDrift *= 0.8F;
            yawDrift *= 0.5F;
            pitchDrift += (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 2.0F;
            yawDrift += (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 4.0F;
            if (single || random.nextInt(4) != 0) {
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
                                        if ((nx * nx + nz * nz) * widthAtY[by] + ny * ny / 6.0 < 1.0) {
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

                        if (single) {
                            break;
                        }
                    }
                }
            }
        }
    }
}

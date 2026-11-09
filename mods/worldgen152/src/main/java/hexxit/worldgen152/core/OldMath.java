package hexxit.worldgen152.core;

/** The lookup-table trigonometry and floor helpers of Minecraft 1.5.2. Terrain is only seed-exact with these. */
public final class OldMath {
    private static final float[] SIN_TABLE = new float[65536];

    static {
        for (int i = 0; i < 65536; i++) {
            SIN_TABLE[i] = (float) Math.sin(i * Math.PI * 2.0 / 65536.0);
        }
    }

    private OldMath() {
    }

    public static float sin(float value) {
        return SIN_TABLE[(int) (value * 10430.378F) & 65535];
    }

    public static float cos(float value) {
        return SIN_TABLE[(int) (value * 10430.378F + 16384.0F) & 65535];
    }

    public static float sqrt(float value) {
        return (float) Math.sqrt(value);
    }

    public static int floor(double value) {
        int i = (int) value;
        return value < i ? i - 1 : i;
    }

    public static long floorLong(double value) {
        long l = (long) value;
        return value < l ? l - 1L : l;
    }
}

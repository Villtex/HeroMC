package meteors;

import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.function.Supplier;

/**
 * EnumMeteor: the crater material, the rare material (1 of 45 blocks), whether the explosion sets fires, the colour
 * of the shield particles and the texture of the falling meteor and of the shield's beam.
 */
public enum MeteorType {
    METEORITE("falling_meteor", () -> MeteorsMod.FALLEN_METEOR.get(), () -> MeteorsMod.RARE_FALLEN_METEOR.get(), true, "met"),
    FREZARITE("freza_meteor", () -> MeteorsMod.FREZARITE.get(), () -> Blocks.ICE, false, "freza"),
    KREKNORITE("krekno_meteor", () -> MeteorsMod.KREKNORITE.get(), () -> Blocks.LAVA, true, "krekno"),
    UNKNOWN("unknown_meteor", () -> Blocks.AIR, () -> Blocks.AIR, true, "unk"),
    KITTY("kitty", () -> Blocks.AIR, () -> Blocks.AIR, false, "kitty");

    public final String texture;
    private final Supplier<Block> material;
    private final Supplier<Block> rareMaterial;
    public final boolean fieryExplosion;
    public final String beam;

    MeteorType(String texture, Supplier<Block> material, Supplier<Block> rareMaterial, boolean fiery, String beam) {
        this.texture = texture;
        this.material = material;
        this.rareMaterial = rareMaterial;
        this.fieryExplosion = fiery;
        this.beam = beam;
    }

    public Block material() {
        return this.material.get();
    }

    public Block rareMaterial() {
        return this.rareMaterial.get();
    }

    public int id() {
        return this.ordinal();
    }

    /** getTypeFromID: unknown ids are METEORITE. */
    public static MeteorType byId(int id) {
        return id >= 0 && id < values().length ? values()[id] : METEORITE;
    }

    /** HandlerMeteor.defaultType: the first enabled type, METEORITE when none is. */
    public static MeteorType defaultType() {
        if (MeteorsConfig.METEORITE_ENABLED.get()) {
            return METEORITE;
        } else if (MeteorsConfig.FREZARITE_ENABLED.get()) {
            return FREZARITE;
        } else if (MeteorsConfig.KREKNORITE_ENABLED.get()) {
            return KREKNORITE;
        } else if (MeteorsConfig.UNKNOWN_ENABLED.get()) {
            return UNKNOWN;
        }
        return METEORITE;
    }

    /** HandlerMeteor.getMeteorType: of 63, 3 unknown, 8 kreknorite, 17 frezarite, the rest the default type. */
    public static MeteorType random(RandomSource random) {
        int r = random.nextInt(63);
        if (r >= 60 && MeteorsConfig.UNKNOWN_ENABLED.get()) {
            return UNKNOWN;
        } else if (r >= 52 && MeteorsConfig.KREKNORITE_ENABLED.get()) {
            return KREKNORITE;
        }
        return r >= 35 && MeteorsConfig.FREZARITE_ENABLED.get() ? FREZARITE : defaultType();
    }

    /** HandlerMeteor.getMeteorSize: with sizes 1..3, 16 of 26 are 1, 9 are 2 and 1 is 3. */
    public static int randomSize(RandomSource random) {
        int r = random.nextInt(26);
        int maxSize = MeteorsConfig.maxSize();
        int minSize = MeteorsConfig.minSize();
        if (maxSize == 3 && minSize == 1) {
            return r == 25 ? 3 : r > 15 ? 2 : 1;
        } else if (maxSize == 3 && minSize == 2) {
            return r > 15 ? 3 : 2;
        } else if (minSize != 1 || maxSize != 2) {
            return minSize;
        }
        return r > 15 ? 2 : 1;
    }
}

package xreliquary.item;

/** The bullet and magazine damage values 0..9 of 1.5.2 with their overlay colours (getColor). */
public enum ShotType {
    EMPTY("empty", 0xFFFFFF),
    NEUTRAL("neutral", 0x888888),
    EXORCISM("exorcism", 0xDDDD00),
    BLAZE("blaze", 0xDD2222),
    ENDER("ender", 0x9900EE),
    CONCUSSIVE("concussive", 0x00BB00),
    BUSTER("buster", 0x444444),
    SEEKER("seeker", 0x00BB99),
    SAND("sand", 0xDDDD77),
    STORM("storm", 0x0000DD);

    public final String id;
    public final int color;

    ShotType(String id, int color) {
        this.id = id;
        this.color = color;
    }

    public static ShotType of(int meta) {
        return meta >= 0 && meta < values().length ? values()[meta] : EMPTY;
    }
}

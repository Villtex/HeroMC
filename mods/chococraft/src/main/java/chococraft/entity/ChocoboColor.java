package chococraft.entity;

/**
 * EntityAnimalChocobo.chocoboColor plus the values the nine EntityChocobo&lt;Colour&gt; classes set in their
 * constructors and in setStepHeight / setLandMovementFactor / getMaxHealth (Constants.CHOCOBO_*). The speed factors
 * multiply the rider's motion (EntityChocoboRideable.moveEntity), the fly factor is the acceleration while flying.
 */
public enum ChocoboColor {
    //        health chicobo  climb  water  jump   fly    fireImm landSpd waterSpd airSpd flyMove stepMounted file
    YELLOW(30, 10, false, false, false, false, false, 20.0, 10.0, 0.0, 0.0F, 1.0F, "chocobo"),
    GREEN(30, 10, false, false, false, false, false, 27.0, 10.0, 0.0, 0.0F, 2.0F, "greenchocobo"),
    BLUE(30, 10, false, true, false, false, false, 27.0, 55.0, 0.0, 0.0F, 1.0F, "bluechocobo"),
    WHITE(40, 15, true, true, true, false, false, 35.0, 45.0, 0.0, 0.0F, 2.0F, "whitechocobo"),
    BLACK(40, 15, true, true, true, false, false, 40.0, 20.0, 0.0, 0.0F, 2.0F, "blackchocobo"),
    GOLD(50, 20, true, true, false, true, false, 50.0, 20.0, 55.0, 0.25F, 2.0F, "goldchocobo"),
    PINK(50, 20, true, true, false, true, false, 55.0, 25.0, 60.0, 0.25F, 2.0F, "pinkchocobo"),
    RED(50, 20, true, true, false, true, false, 55.0, 25.0, 60.0, 0.25F, 2.0F, "redchocobo"),
    PURPLE(50, 20, true, false, false, true, true, 40.0, 10.0, 55.0, 0.25F, 1.0F, "purplechocobo");

    /** getMaxHealth of the adult. */
    public final int health;
    /** EntityChicobo.getColorMaxHealth. */
    public final int chicoboHealth;
    public final boolean canClimb;
    public final boolean canCrossWater;
    /** CHOCOBO_*_CANJUMPHIGH: only while mounted (setJumpHigh). */
    public final boolean canJumpHigh;
    public final boolean canFly;
    public final boolean fireImmune;
    public final double landSpeedFactor;
    public final double waterSpeedFactor;
    public final double airSpeedFactor;
    public final float flyMovementFactor;
    /** setStepHeight(true); 0.5 when not mounted. */
    public final float mountedStepHeight;
    /** Texture file name (without .png) of the colour. */
    public final String texture;

    ChocoboColor(int health, int chicoboHealth, boolean canClimb, boolean canCrossWater, boolean canJumpHigh, boolean canFly,
            boolean fireImmune, double landSpeedFactor, double waterSpeedFactor, double airSpeedFactor, float flyMovementFactor,
            float mountedStepHeight, String texture) {
        this.health = health;
        this.chicoboHealth = chicoboHealth;
        this.canClimb = canClimb;
        this.canCrossWater = canCrossWater;
        this.canJumpHigh = canJumpHigh;
        this.canFly = canFly;
        this.fireImmune = fireImmune;
        this.landSpeedFactor = landSpeedFactor;
        this.waterSpeedFactor = waterSpeedFactor;
        this.airSpeedFactor = airSpeedFactor;
        this.flyMovementFactor = flyMovementFactor;
        this.mountedStepHeight = mountedStepHeight;
        this.texture = texture;
    }

    /** Gold, pink, red and purple override fall() with nothing: no fall damage. */
    public boolean ignoresFalls() {
        return this.canFly;
    }

    public String id() {
        return "chocobo_" + this.name().toLowerCase(java.util.Locale.ROOT);
    }
}

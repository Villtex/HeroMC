package asgardshield.guard;

import asgardshield.network.GuardStatePayload;

/**
 * The guard state of one player. In 1.5.2 the Guard Gauge ("AsgardShieldGG") and the Vanguard Count lived in the
 * client tick handler, and only the broken guard ("AsgardShieldGB") went to the server (ASPacket). Here the server
 * runs the same rules and sends the state to the client (HUD, and so the client does not raise a broken guard).
 * Not saved, as the entity data of 1.5.2 was reset when the player was constructed.
 */
public final class GuardState {
    /** AsgardShieldGG: 0-200, the guard breaks at 200. */
    public int gauge;
    /** AsgardShieldGB: the guard is broken until the gauge is down to 60. */
    public boolean broken;
    public int vanguardCount;
    public int vanguardCounter;
    public boolean vanguardReady;
    // HUD flags of the last guard: gilded shield, giant sword, broken, ward augment
    public boolean gildedGauge;
    public boolean swordGauge;
    public boolean breakGauge;
    public boolean wardGauge;
    /** mod_AsgardShield.livingmetalAura / biomassAura (static in 1.5.2, per player here). */
    public int livingmetalAura;
    public int biomassAura;

    private GuardStatePayload lastSent;

    public GuardStatePayload payload() {
        byte flags = (byte) ((this.gildedGauge ? 1 : 0) | (this.swordGauge ? 2 : 0) | (this.breakGauge ? 4 : 0) | (this.wardGauge ? 8 : 0)
                | (this.vanguardReady ? 16 : 0));
        return new GuardStatePayload(this.gauge, this.broken, this.vanguardCount, flags);
    }

    /** The payload to send if anything the client needs has changed since the last one, else null. */
    GuardStatePayload changed() {
        GuardStatePayload now = payload();
        if (now.equals(this.lastSent)) {
            return null;
        }
        this.lastSent = now;
        return now;
    }

    /** Client side: takes over the state the server sent. */
    public void apply(GuardStatePayload p) {
        this.gauge = p.gauge();
        this.broken = p.broken();
        this.vanguardCount = p.vanguardCount();
        this.gildedGauge = (p.flags() & 1) != 0;
        this.swordGauge = (p.flags() & 2) != 0;
        this.breakGauge = (p.flags() & 4) != 0;
        this.wardGauge = (p.flags() & 8) != 0;
        this.vanguardReady = (p.flags() & 16) != 0;
    }
}

package atomicstryker.infernalmobs.common;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

/**
 * One modifier of a rare mob. The modifiers of a mob form a linked list ({@link #nextMod}); every hook is passed down
 * the chain, so the last modifier of the list (the first one created, the last name of the saved string) runs the
 * base logic. Ported 1:1 from MobModifier of Infernal Mobs 1.3.1, including its quirks: only the tail of the chain
 * remembers the attack target, so {@link #getMobTarget()} is null for every other modifier of the chain.
 *
 * <p>Health: the original pushed the health past the maximum by reflection. Here the maximum itself is raised with a
 * {@code max_health} modifier ({@link InfernalMobsCore#applyHealth}), so {@link #getActualMaxHealth} is the normal maximum.
 */
public abstract class MobModifier {
    protected MobModifier nextMod = null;
    protected String modName;
    private String[] bufferedNames;
    private LivingEntity attackTarget;
    private int bufferedSize = 0;
    private String bufferedEntityName;

    public String getModName() {
        return this.modName;
    }

    public String getLinkedModName() {
        return this.modName + " " + (this.nextMod != null ? this.nextMod.getLinkedModName() : "");
    }

    /** The modifier names in up to three lines of five (the original HUD lines, each starting with a space). */
    public String[] getDisplayNames() {
        if (this.bufferedNames == null) {
            String[] allMods = this.getLinkedModName().split(" ");
            int index = 0;
            int j = 0;
            this.bufferedNames = new String[3];
            this.bufferedNames[index] = "";
            for (String m : allMods) {
                this.bufferedNames[index] = this.bufferedNames[index] + " " + m;
                if (++j % 5 == 0 && index + 1 < this.bufferedNames.length) {
                    this.bufferedNames[++index] = "";
                }
            }
        }
        return this.bufferedNames;
    }

    public boolean containsModifierClass(Class<?> checkfor) {
        if (checkfor.equals(this.getClass())) {
            return true;
        }
        return this.nextMod != null && this.nextMod.containsModifierClass(checkfor);
    }

    public void onSpawningComplete(LivingEntity entity) {
        InfernalMobsCore.storeModNames(entity, this.getLinkedModName());
    }

    public boolean onDeath() {
        return this.nextMod != null && this.nextMod.onDeath();
    }

    public void onDropItems(LivingEntity moddedMob, DamageSource killSource, boolean recentlyHit) {
        if (recentlyHit) {
            InfernalMobsCore.dropLootForEnt(moddedMob, this);
        }
    }

    public void onSetAttackTarget(LivingEntity target) {
        if (this.nextMod != null) {
            this.nextMod.onSetAttackTarget(target);
        } else if (target != null) {
            this.attackTarget = target;
        }
    }

    /** The modded mob hurts {@code entity}; returns the new damage. */
    public float onAttack(LivingEntity entity, DamageSource source, float damage) {
        return this.nextMod != null ? this.nextMod.onAttack(entity, source, damage) : damage;
    }

    /** The modded mob is hurt; returns the new damage. */
    public float onHurt(LivingEntity mob, DamageSource source, float damage) {
        if (this.nextMod != null) {
            damage = this.nextMod.onHurt(mob, source, damage);
        }
        return damage;
    }

    public boolean onFall(double distance) {
        return this.nextMod != null && this.nextMod.onFall(distance);
    }

    public void onJump(LivingEntity entityLiving) {
        if (this.nextMod != null) {
            this.nextMod.onJump(entityLiving);
        }
    }

    public boolean onUpdate(LivingEntity mob) {
        if (this.nextMod != null) {
            return this.nextMod.onUpdate(mob);
        }
        if (this.attackTarget == null) {
            this.attackTarget = closestVulnerablePlayer(mob, 12.0);
        }
        if (this.attackTarget != null && (!this.attackTarget.isAlive() || this.attackTarget.distanceTo(mob) > 15.0F)) {
            this.attackTarget = null;
        }
        return false;
    }

    /** World.getClosestVulnerablePlayerToEntity: the nearest player in range that is not in creative or spectator mode. */
    public static Player closestVulnerablePlayer(LivingEntity mob, double range) {
        return mob.level().getNearestPlayer(mob.getX(), mob.getY(), mob.getZ(), range, EntitySelector.NO_CREATIVE_OR_SPECTATOR);
    }

    public float getActualMaxHealth(LivingEntity mob) {
        return mob.getMaxHealth();
    }

    /** setEntityHealthPastMax: the maximum is already raised, so this sets the health up to it. */
    public static void setHealth(LivingEntity mob, float amount) {
        mob.setHealth(Math.min(amount, mob.getMaxHealth()));
    }

    protected LivingEntity getMobTarget() {
        return this.attackTarget;
    }

    public Class<?>[] getBlackListMobClasses() {
        return null;
    }

    public Class<?>[] getModsNotToMixWith() {
        return null;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof MobModifier m && m.modName.equals(this.modName);
    }

    @Override
    public int hashCode() {
        return this.modName.hashCode();
    }

    public int getModSize() {
        if (this.bufferedSize == 0) {
            this.bufferedSize = 1;
            for (MobModifier nextmod = this.nextMod; nextmod != null; nextmod = nextmod.nextMod) {
                this.bufferedSize++;
            }
        }
        return this.bufferedSize;
    }

    protected String[] getModNamePrefix() {
        return null;
    }

    protected String[] getModNameSuffix() {
        return null;
    }

    /**
     * "§bRare undying Zombie of Venom": prefix by size, a random prefix of a random modifier, the entity name, the
     * suffix of the modifier after it. The original split the entity string at {@code "."} as a regex, which never
     * shortened it; the name is the translated entity name today.
     */
    public String getEntityDisplayName(LivingEntity target) {
        if (this.bufferedEntityName == null) {
            String buffer = target.getType().getDescription().getString();
            int size = this.getModSize();
            int randomMod = target.getRandom().nextInt(this.getModSize());
            MobModifier mod = this;
            while (randomMod > 0) {
                mod = mod.nextMod;
                randomMod--;
            }
            String modprefix = "";
            if (mod.getModNamePrefix() != null) {
                modprefix = mod.getModNamePrefix()[target.getRandom().nextInt(mod.getModNamePrefix().length)];
            }
            String prefix = size <= 5 ? "§bRare" : (size <= 10 ? "§6Ultra" : "§4Infernal");
            if (buffer.startsWith("Entity")) {
                buffer = buffer.replaceFirst("Entity", prefix + modprefix);
            } else {
                buffer = prefix + modprefix + buffer;
            }
            if (size > 1) {
                mod = mod.nextMod != null ? mod.nextMod : this;
                if (mod.getModNameSuffix() != null) {
                    String pick = mod.getModNameSuffix()[target.getRandom().nextInt(mod.getModNameSuffix().length)];
                    buffer = buffer + pick;
                }
            }
            this.bufferedEntityName = buffer;
        }
        return this.bufferedEntityName;
    }

    // ---- helpers shared by the modifiers -------------------------------------------------------------------------

    protected static ServerLevel serverLevel(LivingEntity mob) {
        return (ServerLevel) mob.level();
    }

    /** Entity.attackEntityFrom on any entity (server side). */
    protected static void hurt(net.minecraft.world.entity.Entity target, DamageSource source, float amount) {
        if (target.level() instanceof ServerLevel level) {
            target.hurtServer(level, source, amount);
        }
    }

    /** World.playSoundAtEntity. */
    protected static void playSound(LivingEntity at, net.minecraft.core.Holder<net.minecraft.sounds.SoundEvent> sound, float volume, float pitch) {
        at.level().playSound(null, at.getX(), at.getY(), at.getZ(), sound, at.getSoundSource(), volume, pitch);
    }

    protected static void playSound(LivingEntity at, net.minecraft.sounds.SoundEvent sound, float volume, float pitch) {
        at.level().playSound(null, at.getX(), at.getY(), at.getZ(), sound, at.getSoundSource(), volume, pitch);
    }

    /** The pitch {@code (rand - rand) * 0.2 + 1} of many original sounds. */
    protected static float randomPitch(LivingEntity mob) {
        return (mob.getRandom().nextFloat() - mob.getRandom().nextFloat()) * 0.2F + 1.0F;
    }
}

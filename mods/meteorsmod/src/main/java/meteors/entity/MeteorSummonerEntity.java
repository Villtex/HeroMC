package meteors.entity;

import meteors.MeteorData;
import meteors.MeteorEvents;
import meteors.MeteorType;
import meteors.MeteorsConfig;
import meteors.MeteorsMod;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.HitResult;

/**
 * EntitySummoner: the thrown summoner. Where it hits, a meteor of its type (and a random size) falls; it is summoned,
 * so shields do not stop it, but unless "Allow Summoned Meteor Grief" it does not fall on land another player shields.
 * In another dimension, or on such land, the thrower gets the summoner back (not in creative mode).
 */
public class MeteorSummonerEntity extends ThrowableItemProjectile {
    private int meteorType;
    private boolean isRandom;

    public MeteorSummonerEntity(EntityType<? extends MeteorSummonerEntity> type, Level level) {
        super(type, level);
    }

    public MeteorSummonerEntity(Level level, LivingEntity owner, ItemStack stack, int meteorType, boolean isRandom) {
        super(MeteorsMod.METEOR_SUMMONER_ENTITY.get(), owner, level, stack);
        this.meteorType = meteorType;
        this.isRandom = isRandom;
    }

    @Override
    protected Item getDefaultItem() {
        return MeteorsMod.SUMMONERS.getFirst().get();
    }

    private ItemStack refund() {
        return new ItemStack(MeteorsMod.SUMMONERS.get(this.isRandom ? 0 : this.meteorType + 1).get());
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        Level level = this.level();
        for (int i = 0; i < 8; i++) {
            level.addParticle(ParticleTypes.ITEM_SNOWBALL, this.getX(), this.getY(), this.getZ(), 0.0, 0.0, 0.0);
        }
        if (!(level instanceof ServerLevel server)) {
            return;
        }
        Player player = this.getOwner() instanceof Player p ? p : null;
        if (level.dimension() != Level.OVERWORLD) {
            if (player != null) {
                player.sendSystemMessage(Component.translatable("MeteorSummoner.wrongDimension"));
                if (!player.getAbilities().instabuild) {
                    player.getInventory().add(this.refund());
                }
            }
            this.discard();
            return;
        }
        boolean canHit = true;
        if (!MeteorsConfig.ALLOW_SUMMONED_METEOR_GRIEF.get() && player != null) {
            int cx = (int) this.getX() >> 4;
            int cz = (int) this.getZ() >> 4;
            for (String owner : MeteorData.get(server.getServer()).owners(cx, cz)) {
                if (!player.getGameProfile().name().equalsIgnoreCase(owner)) {
                    canHit = false;
                    player.sendSystemMessage(Component.translatable("MeteorSummoner.landProtected"));
                    if (!player.getAbilities().instabuild) {
                        player.getInventory().add(this.refund());
                    }
                    break;
                }
            }
        }
        if (canHit) {
            if (player instanceof ServerPlayer serverPlayer) {
                player.sendSystemMessage(Component.translatable("MeteorSummoner.incomingMeteor").withStyle(ChatFormatting.DARK_PURPLE));
                MeteorEvents.grant(serverPlayer, "summon_meteor");
            }
            FallingMeteor meteor = new FallingMeteor(level, MeteorType.randomSize(server.getRandom()), this.getX(), this.getZ(),
                    MeteorType.byId(this.meteorType), true);
            MeteorData.keepLoaded(server, (int) this.getX(), (int) this.getZ());
            level.addFreshEntity(meteor);
        }
        this.discard();
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt("metType", this.meteorType);
        output.putBoolean("isRandom", this.isRandom);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.meteorType = input.getIntOr("metType", 0);
        this.isRandom = input.getBooleanOr("isRandom", false);
    }
}

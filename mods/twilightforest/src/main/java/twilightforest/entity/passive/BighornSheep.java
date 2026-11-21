package twilightforest.entity.passive;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jspecify.annotations.Nullable;
import twilightforest.TFEntities;

/**
 * EntityTFBighorn: a sheep (shearing, eating grass) with its own model; the fleece is brown (12) half of the time, else
 * one of the colours 0..14. A lamb takes the colour of a random parent. Drops like the 1.5.2 sheep: one wool of its
 * colour unless shorn (loot table: the wool pool of the vanilla sheep).
 */
public class BighornSheep extends Sheep {
    public BighornSheep(EntityType<? extends Sheep> type, Level level) {
        super(type, level);
    }

    public static DyeColor randomFleeceColor(RandomSource random) {
        return DyeColor.byId(random.nextInt(2) == 0 ? 12 : random.nextInt(15));
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason,
            @Nullable SpawnGroupData groupData) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, reason, groupData);
        this.setColor(randomFleeceColor(level.getRandom()));
        return data;
    }

    @Override
    public @Nullable Sheep getBreedOffspring(ServerLevel level, AgeableMob partner) {
        BighornSheep baby = TFEntities.BIGHORN_SHEEP.get().create(level, EntitySpawnReason.BREEDING);
        if (baby != null && partner instanceof Sheep other) {
            baby.setColor(this.random.nextBoolean() ? this.getColor() : other.getColor());
        }
        return baby;
    }
}

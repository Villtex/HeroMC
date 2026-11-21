package twilightforest.entity.boss;

import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jspecify.annotations.Nullable;
import twilightforest.TFItems;
import twilightforest.entity.Minotaur;

/**
 * EntityTFMinoshroom: the minotaur of the labyrinth's boss room. 120 health, 1.49 × 2.9, 100 xp, never despawns; holds
 * the minotaur axe and always drops it; drops 2..5 (+ 0..looting) meef stroganoff.
 */
public class Minoshroom extends Minotaur {
    public Minoshroom(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.xpReward = 100;
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(TFItems.MINOTAUR_AXE.get()));
        this.setDropChance(EquipmentSlot.MAINHAND, 1.0F);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Minotaur.createAttributes().add(Attributes.MAX_HEALTH, 120.0);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason,
            @Nullable SpawnGroupData groupData) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, reason, groupData);
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(TFItems.MINOTAUR_AXE.get()));
        this.setDropChance(EquipmentSlot.MAINHAND, 1.0F);
        return data;
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }
}

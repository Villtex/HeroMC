package myrathi.obsidiplates;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.PressurePlateBlock;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.phys.AABB;

/**
 * The pressure plate of ObsidiPlates: on (signal 15) while a player (obsidian) or a mob that is not a player
 * (mossy cobblestone) stands on it. Checked again every 20 ticks like the stone plate.
 */
public class PlayerMobPlateBlock extends PressurePlateBlock {
    /** 1.5.2 detection box: 1/8 in from the sides, 1/4 high. */
    private static final AABB DETECTION = new AABB(0.125, 0.0, 0.125, 0.875, 0.25, 0.875);
    private final boolean players;

    public PlayerMobPlateBlock(BlockSetType type, Properties properties, boolean players) {
        super(type, properties);
        this.players = players;
    }

    public boolean playersOnly() {
        return this.players;
    }

    @Override
    protected int getSignalStrength(Level level, BlockPos pos) {
        Class<? extends Entity> type = this.players ? Player.class : LivingEntity.class;
        return level.getEntitiesOfClass(type, DETECTION.move(pos), EntitySelector.NO_SPECTATORS
                .and(e -> !e.isIgnoringBlockTriggers() && (e instanceof Player) == this.players)).isEmpty() ? 0 : 15;
    }
}

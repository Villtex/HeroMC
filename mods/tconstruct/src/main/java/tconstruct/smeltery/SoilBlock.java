package tconstruct.smeltery;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * SoilBlock (craftedSoil 0..5): slimy mud, grout, blue slimy mud, graveyard soil, consecrated soil, blue slime dirt.
 * Undead walking on graveyard soil heal half a heart, on consecrated soil they take magic damage and burn for a second.
 */
public class SoilBlock extends Block {
    public final int meta;

    public SoilBlock(int meta, Properties properties) {
        super(properties);
        this.meta = meta;
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        if (level instanceof ServerLevel server && entity instanceof LivingEntity living && living.is(EntityTypeTags.UNDEAD)) {
            if (this.meta == 3) {
                living.heal(1.0F);
            } else if (this.meta == 4) {
                living.hurtServer(server, level.damageSources().magic(), 1.0F);
                living.igniteForSeconds(1.0F);
            }
        }
        super.stepOn(level, pos, state, entity);
    }
}

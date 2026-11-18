package betterdungeons.block;

import betterdungeons.BetterDungeons;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/** BlockTileEntityMobSpawner: the dungeon mob (index of RegisterDungeonMobs) and the metadata of the template's mob block. */
public class BDSpawnerBlockEntity extends BlockEntity {
    private int mob = 1;
    private int metadata;

    public BDSpawnerBlockEntity(BlockPos pos, BlockState state) {
        super(BetterDungeons.SPAWNER_ENTITY.get(), pos, state);
    }

    public void set(int mob, int metadata) {
        this.mob = mob;
        this.metadata = metadata;
        this.setChanged();
    }

    public int mob() {
        return this.mob;
    }

    public int metadata() {
        return this.metadata;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("mob", this.mob);
        output.putInt("metadata", this.metadata);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.mob = input.getIntOr("mob", 1);
        this.metadata = input.getIntOr("metadata", 0);
    }
}

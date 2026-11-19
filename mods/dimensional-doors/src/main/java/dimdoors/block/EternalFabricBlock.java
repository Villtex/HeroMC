package dimdoors.block;

import dimdoors.DimDoors;
import dimdoors.world.DDData.LinkData;
import dimdoors.world.DDWorld;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Random;
import java.util.Set;

/**
 * BlockDimWallPerm, Eternal Fabric: the unbreakable shell of the pockets and the floor of Limbo. A player walking on
 * it in Limbo wakes up in the overworld, within the "Limbo Return Range" of a random link, on a little patch of
 * Unraveled Fabric.
 */
public class EternalFabricBlock extends Block {
    public EternalFabricBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        super.stepOn(level, pos, state, entity);
        if (level.isClientSide() || !DDWorld.isLimbo(level) || !(entity instanceof ServerPlayer player)) {
            return;
        }
        Random rand = new Random();
        LinkData link = DDWorld.randomLink(false);
        int destX = link == null ? 0 : DDWorld.virtualX(link.destDim, link.destX);
        int destZ = link == null ? 0 : DDWorld.virtualZ(link.destDim, link.destZ);
        int range = DimDoors.LIMBO_RETURN_RANGE;
        int x = destX + rand.nextInt(range) - range / 2;
        int z = destZ + rand.nextInt(range) - range / 2;
        x += x >> 4;
        z += z >> 4;
        ServerLevel overworld = DDWorld.server().overworld();
        int y = DDWorld.firstUncovered(overworld, x, 63, z);
        player.teleportTo(overworld, x, y, z, Set.of(), player.getYRot(), player.getXRot(), true);
        overworld.setBlock(new BlockPos(x, y, z), Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        BlockState fabric = DimDoors.UNRAVELED_FABRIC.get().defaultBlockState();
        for (int xc = -3; xc < 4; xc++) {
            for (int zc = -3; zc < 4; zc++) {
                BlockPos at = new BlockPos(x + xc, y - 2, z + zc);
                if (Math.abs(xc) + Math.abs(zc) < rand.nextInt(3) + 2) {
                    overworld.setBlock(at, fabric, Block.UPDATE_ALL);
                } else if (Math.abs(xc) + Math.abs(zc) < rand.nextInt(3) + 3) {
                    overworld.setBlock(at, fabric, Block.UPDATE_CLIENTS);
                }
            }
        }
        player.teleportTo(overworld, x, y, z, Set.of(), player.getYRot(), player.getXRot(), true);
        player.resetFallDistance();
    }
}

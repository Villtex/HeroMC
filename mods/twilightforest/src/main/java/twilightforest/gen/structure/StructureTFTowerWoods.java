package twilightforest.gen.structure;

import hexxit.worldgen152.core.populate.OldWorld;
import hexxit.worldgen152.core.populate.OldTile;
import hexxit.worldgen152.core.structure.*;
import twilightforest.gen.*;

import java.util.Random;

public class StructureTFTowerWoods extends StructurePieceBlockSelector {
   public void selectBlocks(Random par1Random, int x, int y, int z, boolean isWall) {
      if (!isWall) {
         this.selectedBlockId = 0;
         this.selectedBlockMetaData = 0;
      } else {
         this.selectedBlockId = 1372;
         float randFloat = par1Random.nextFloat();
         if (randFloat < 0.1F) {
            this.selectedBlockMetaData = 2;
         } else if (randFloat < 0.2F) {
            this.selectedBlockMetaData = 3;
         } else if (randFloat < 0.225F) {
            this.selectedBlockMetaData = 4;
         } else {
            this.selectedBlockMetaData = 0;
         }
      }
   }
}

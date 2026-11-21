package twilightforest.gen.structure;

import hexxit.worldgen152.core.populate.OldWorld;
import hexxit.worldgen152.core.populate.OldTile;
import hexxit.worldgen152.core.structure.*;
import twilightforest.gen.*;

import java.util.Random;

public class StructureTFMazeStones extends StructurePieceBlockSelector {
   public void selectBlocks(Random par1Random, int par2, int par3, int par4, boolean wall) {
      if (!wall) {
         this.selectedBlockId = 0;
         this.selectedBlockMetaData = 0;
      } else {
         this.selectedBlockId = 1362;
         float rf = par1Random.nextFloat();
         if (rf < 0.2F) {
            this.selectedBlockMetaData = 5;
         } else if (rf < 0.5F) {
            this.selectedBlockMetaData = 4;
         } else {
            this.selectedBlockMetaData = 1;
         }
      }
   }
}

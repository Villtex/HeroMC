package twilightforest.gen.structure;

import hexxit.worldgen152.core.populate.OldWorld;
import hexxit.worldgen152.core.populate.OldTile;
import hexxit.worldgen152.core.structure.*;
import twilightforest.gen.*;

import java.util.Random;

public class StructureTFStrongholdStones extends StructurePieceBlockSelector {
   public void selectBlocks(Random par1Random, int par2, int par3, int par4, boolean par5) {
      if (!par5) {
         this.selectedBlockId = 0;
         this.selectedBlockMetaData = 0;
      } else {
         this.selectedBlockId = 98;
         float var6 = par1Random.nextFloat();
         if (var6 < 0.2F) {
            this.selectedBlockMetaData = 2;
         } else if (var6 < 0.5F) {
            this.selectedBlockMetaData = 1;
         } else if (var6 < 0.55F) {
            this.selectedBlockId = 97;
            this.selectedBlockMetaData = 2;
         } else {
            this.selectedBlockMetaData = 0;
         }
      }
   }
}

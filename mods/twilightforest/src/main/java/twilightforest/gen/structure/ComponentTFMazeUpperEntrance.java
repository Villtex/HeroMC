package twilightforest.gen.structure;

import hexxit.worldgen152.core.populate.OldWorld;
import hexxit.worldgen152.core.populate.OldTile;
import hexxit.worldgen152.core.structure.*;
import twilightforest.gen.*;

import java.util.List;
import java.util.Random;

public class ComponentTFMazeUpperEntrance extends StructureTFComponent {
   private int averageGroundLevel = -1;

   public ComponentTFMazeUpperEntrance(int i, Random rand, int x, int y, int z) {
      super(i);
      this.coordBaseMode = rand.nextInt(4);
      this.boundingBox = new StructureBoundingBox(x, y, z, x + 15, y + 4, z + 15);
   }

   public void buildComponent(StructureComponent structurecomponent, List list, Random random) {
   }

   public boolean addComponentParts(OldWorld world, Random rand, StructureBoundingBox sbb) {
      this.randomlyFillWithBlocks(world, sbb, rand, 0.7F, 0, 5, 0, 15, 5, 15, 1362, 0, true);
      this.fillWithMetadataBlocks(world, sbb, 0, 0, 0, 15, 0, 15, 1362, 6, 0, 0, false);
      this.fillWithMetadataBlocks(world, sbb, 0, 1, 0, 15, 1, 15, 1362, 3, 0, 0, true);
      this.fillWithMetadataBlocks(world, sbb, 0, 2, 0, 15, 3, 15, 1362, 1, 0, 0, true);
      this.fillWithMetadataBlocks(world, sbb, 0, 4, 0, 15, 4, 15, 1362, 3, 0, 0, true);
      this.randomlyFillWithBlocks(world, sbb, rand, 0.2F, 0, 0, 0, 15, 5, 15, 13, 0, true);
      this.fillWithBlocks(world, sbb, 6, 1, 0, 9, 4, 0, 85, 0, false);
      this.fillWithAir(world, sbb, 7, 1, 0, 8, 3, 0);
      this.fillWithBlocks(world, sbb, 6, 1, 15, 9, 4, 15, 85, 0, false);
      this.fillWithAir(world, sbb, 7, 1, 15, 8, 3, 15);
      this.fillWithBlocks(world, sbb, 0, 1, 6, 0, 4, 9, 85, 0, false);
      this.fillWithAir(world, sbb, 0, 1, 7, 0, 3, 8);
      this.fillWithBlocks(world, sbb, 15, 1, 6, 15, 4, 9, 85, 0, false);
      this.fillWithAir(world, sbb, 15, 1, 7, 15, 3, 8);
      this.fillWithAir(world, sbb, 1, 1, 1, 14, 4, 14);
      this.fillWithMetadataBlocks(world, sbb, 5, 1, 5, 10, 1, 10, 1362, 3, 0, 0, false);
      this.fillWithMetadataBlocks(world, sbb, 5, 4, 5, 10, 4, 10, 1362, 3, 0, 0, false);
      this.randomlyFillWithBlocks(world, sbb, rand, 0.7F, 5, 2, 5, 10, 3, 10, 101, 0, false);
      this.fillWithAir(world, sbb, 6, 0, 6, 9, 4, 9);
      return true;
   }

   protected int getAverageGroundLevel(OldWorld par1World, StructureBoundingBox par2StructureBoundingBox) {
      int var3 = 0;
      int var4 = 0;

      for (int var5 = this.boundingBox.minZ; var5 <= this.boundingBox.maxZ; var5++) {
         for (int var6 = this.boundingBox.minX; var6 <= this.boundingBox.maxX; var6++) {
            if (par2StructureBoundingBox.isVecInside(var6, 64, var5)) {
               var3 += Math.max(par1World.getTopSolidOrLiquidBlock(var6, var5), 33 /* WorldProviderTwilightForest.getAverageGroundLevel */);
               var4++;
            }
         }
      }

      return var4 == 0 ? -1 : var3 / var4;
   }
}

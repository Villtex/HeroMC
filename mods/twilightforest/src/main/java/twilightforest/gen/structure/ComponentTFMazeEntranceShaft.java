package twilightforest.gen.structure;

import hexxit.worldgen152.core.populate.OldWorld;
import hexxit.worldgen152.core.populate.OldTile;
import hexxit.worldgen152.core.structure.*;
import twilightforest.gen.*;

import java.util.List;
import java.util.Random;

public class ComponentTFMazeEntranceShaft extends StructureTFComponent {
   private int averageGroundLevel = -1;

   public ComponentTFMazeEntranceShaft(int i, Random rand, int x, int y, int z) {
      super(i);
      this.coordBaseMode = rand.nextInt(4);
      this.boundingBox = new StructureBoundingBox(x, y, z, x + 6 - 1, y + 14, z + 6 - 1);
   }

   public void buildComponent(StructureComponent structurecomponent, List list, Random random) {
   }

   public boolean addComponentParts(OldWorld world, Random rand, StructureBoundingBox sbb) {
      if (this.averageGroundLevel < 0) {
         this.averageGroundLevel = this.getAverageGroundLevel(world, sbb);
         if (this.averageGroundLevel < 0) {
            return true;
         }

         this.boundingBox.offset(0, this.averageGroundLevel - this.boundingBox.maxY + 12 - 1, 0);
      }

      this.fillWithMetadataBlocks(world, sbb, 0, -10, 0, 5, 30, 5, 1362, 1, 0, 0, true);
      this.fillWithAir(world, sbb, 1, -10, 1, 4, 30, 4);
      System.out.println("Drawing entrance");
      int var8 = this.getXWithOffset(0, 0);
      int var9 = this.getYWithOffset(0);
      int var10 = this.getZWithOffset(0, 0);
      System.out.println("Drawing entrance at " + var8 + ", " + var9 + ", " + var10);
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

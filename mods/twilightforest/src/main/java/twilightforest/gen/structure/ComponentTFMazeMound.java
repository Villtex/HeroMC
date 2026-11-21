package twilightforest.gen.structure;

import hexxit.worldgen152.core.populate.OldWorld;
import hexxit.worldgen152.core.populate.OldTile;
import hexxit.worldgen152.core.structure.*;
import twilightforest.gen.*;

import java.util.List;
import java.util.Random;

public class ComponentTFMazeMound extends StructureTFComponent {
   public static final int DIAMETER = 35;
   private int averageGroundLevel = -1;
   private ComponentTFMazeUpperEntrance mazeAbove;

   public ComponentTFMazeMound(int i, Random rand, int x, int y, int z) {
      super(i);
      this.coordBaseMode = rand.nextInt(4);
      this.boundingBox = new StructureBoundingBox(x, y, z, x + 35, y + 8, z + 35);
   }

   public void buildComponent(StructureComponent structurecomponent, List list, Random random) {
      super.buildComponent(structurecomponent, list, random);
      this.mazeAbove = new ComponentTFMazeUpperEntrance(
         3, random, this.boundingBox.minX + 10, this.boundingBox.minY + 1, this.boundingBox.minZ + 10
      );
      list.add(this.mazeAbove);
      this.mazeAbove.buildComponent(this, list, random);
   }

   public boolean addComponentParts(OldWorld world, Random rand, StructureBoundingBox sbb) {
      if (this.averageGroundLevel < 0) {
         this.averageGroundLevel = this.getAverageGroundLevel(world, sbb);
         if (this.averageGroundLevel < 0) {
            return true;
         }

         int offset = this.averageGroundLevel - this.boundingBox.maxY + 8 - 1;
         this.boundingBox.offset(0, offset, 0);
         this.mazeAbove.getBoundingBox().offset(0, offset, 0);
      }

      for (int x = 0; x < 35; x++) {
         for (int z = 0; z < 35; z++) {
            int cx = x - 17;
            int cz = z - 17;
            int dist = (int)Math.sqrt(cx * cx + cz * cz);
            int hheight = (int)(Math.cos(dist / 35.0 * Math.PI) * 11.0);
            if ((cx > 2 || cx < -1 || cz > 2 || cz < -1) && ((cx > 2 || cx < -1) && (cz > 2 || cz < -1) || hheight > 6)) {
               this.placeBlockAtCurrentPosition(world, 2, 0, x, hheight, z, sbb);
               if (cx <= 2 && cx >= -1 || cz <= 2 && cz >= -1) {
                  if (hheight > 6) {
                     this.fillWithBlocks(world, sbb, x, 6, z, x, hheight - 1, z, 3, 0, false);
                  }
               } else {
                  this.fillCurrentPositionBlocksDownwards(world, 3, 0, x, hheight - 1, z, sbb);
               }
            }
         }
      }

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

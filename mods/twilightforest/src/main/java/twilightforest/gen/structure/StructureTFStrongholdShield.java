package twilightforest.gen.structure;

import hexxit.worldgen152.core.populate.OldWorld;
import hexxit.worldgen152.core.populate.OldTile;
import hexxit.worldgen152.core.structure.*;
import twilightforest.gen.*;

import java.util.Random;

public class StructureTFStrongholdShield extends StructureTFStrongholdComponent {
   public StructureTFStrongholdShield(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
      super(0, 0, minX, minY, minZ);
      this.boundingBox = new StructureBoundingBox(minX, minY, minZ, maxX, maxY, maxZ);
      this.spawnListIndex = -1;
   }

   @Override
   public StructureBoundingBox generateBoundingBox(int facing, int x, int y, int z) {
      return null;
   }

   public boolean addComponentParts(OldWorld world, Random random, StructureBoundingBox sbb) {
      int shieldBlock = 20;
      int shieldMeta = 0;
      this.fillWithBlocks(world, sbb, 0, 0, 0, this.boundingBox.getXSize(), 0, this.boundingBox.getZSize(), shieldBlock, shieldBlock, false);
      this.fillWithBlocks(
         world,
         sbb,
         0,
         this.boundingBox.getYSize(),
         0,
         this.boundingBox.getXSize(),
         this.boundingBox.getYSize(),
         this.boundingBox.getZSize(),
         shieldBlock,
         shieldBlock,
         false
      );
      this.fillWithBlocks(
         world,
         sbb,
         this.boundingBox.getXSize(),
         0,
         0,
         this.boundingBox.getXSize(),
         this.boundingBox.getYSize(),
         this.boundingBox.getZSize(),
         shieldBlock,
         shieldBlock,
         false
      );
      this.fillWithBlocks(world, sbb, 0, 0, 0, 0, this.boundingBox.getYSize(), this.boundingBox.getZSize(), shieldBlock, shieldBlock, false);
      this.fillWithBlocks(
         world,
         sbb,
         0,
         0,
         this.boundingBox.getZSize(),
         this.boundingBox.getXSize(),
         this.boundingBox.getYSize(),
         this.boundingBox.getZSize(),
         shieldBlock,
         shieldBlock,
         false
      );
      this.fillWithBlocks(world, sbb, 0, 0, 0, this.boundingBox.getXSize(), this.boundingBox.getYSize(), 0, shieldBlock, shieldBlock, false);
      return true;
   }
}

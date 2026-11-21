package twilightforest.gen.structure;

import hexxit.worldgen152.core.populate.OldWorld;
import hexxit.worldgen152.core.populate.OldTile;
import hexxit.worldgen152.core.structure.*;
import twilightforest.gen.*;

import java.util.List;
import java.util.Random;

public abstract class ComponentTFTowerRoof extends StructureTFComponent {
   protected int size;
   protected int height;

   public ComponentTFTowerRoof(int i, ComponentTFTowerWing wing) {
      super(i);
      this.spawnListIndex = -1;
   }

   protected void makeAttachedOverhangBB(ComponentTFTowerWing wing) {
      switch (this.getCoordBaseMode()) {
         case 0:
            this.boundingBox = new StructureBoundingBox(
               wing.getBoundingBox().minX,
               wing.getBoundingBox().maxY,
               wing.getBoundingBox().minZ - 1,
               wing.getBoundingBox().maxX + 1,
               wing.getBoundingBox().maxY + this.height - 1,
               wing.getBoundingBox().maxZ + 1
            );
            break;
         case 1:
            this.boundingBox = new StructureBoundingBox(
               wing.getBoundingBox().minX - 1,
               wing.getBoundingBox().maxY,
               wing.getBoundingBox().minZ,
               wing.getBoundingBox().maxX + 1,
               wing.getBoundingBox().maxY + this.height - 1,
               wing.getBoundingBox().maxZ + 1
            );
            break;
         case 2:
            this.boundingBox = new StructureBoundingBox(
               wing.getBoundingBox().minX - 1,
               wing.getBoundingBox().maxY,
               wing.getBoundingBox().minZ - 1,
               wing.getBoundingBox().maxX,
               wing.getBoundingBox().maxY + this.height - 1,
               wing.getBoundingBox().maxZ + 1
            );
            break;
         case 3:
            this.boundingBox = new StructureBoundingBox(
               wing.getBoundingBox().minX - 1,
               wing.getBoundingBox().maxY,
               wing.getBoundingBox().minZ - 1,
               wing.getBoundingBox().maxX + 1,
               wing.getBoundingBox().maxY + this.height - 1,
               wing.getBoundingBox().maxZ
            );
      }
   }

   protected void makeCapBB(ComponentTFTowerWing wing) {
      this.boundingBox = new StructureBoundingBox(
         wing.getBoundingBox().minX,
         wing.getBoundingBox().maxY,
         wing.getBoundingBox().minZ,
         wing.getBoundingBox().maxX,
         wing.getBoundingBox().maxY + this.height,
         wing.getBoundingBox().maxZ
      );
   }

   protected void makeOverhangBB(ComponentTFTowerWing wing) {
      this.boundingBox = new StructureBoundingBox(
         wing.getBoundingBox().minX - 1,
         wing.getBoundingBox().maxY,
         wing.getBoundingBox().minZ - 1,
         wing.getBoundingBox().maxX + 1,
         wing.getBoundingBox().maxY + this.height - 1,
         wing.getBoundingBox().maxZ + 1
      );
   }

   public boolean addComponentParts(OldWorld world, Random random, StructureBoundingBox structureboundingbox) {
      return false;
   }

   public boolean fits(ComponentTFTowerWing parent, List list, Random rand) {
      return findIntersecting(list, this.boundingBox) == parent;
   }
}

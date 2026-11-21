package twilightforest.gen.structure;

import hexxit.worldgen152.core.populate.OldWorld;
import hexxit.worldgen152.core.populate.OldTile;
import hexxit.worldgen152.core.structure.*;
import twilightforest.gen.*;

import java.util.List;
import java.util.Random;

public class ComponentTFDarkTowerRoof extends ComponentTFTowerRoof {
   public ComponentTFDarkTowerRoof(int i, ComponentTFTowerWing wing) {
      super(i, wing);
      this.setCoordBaseMode(wing.getCoordBaseMode());
      this.size = wing.size;
      this.height = 12;
      this.makeCapBB(wing);
      this.spawnListIndex = 1;
   }

   public void buildComponent(StructureComponent parent, List list, Random rand) {
      if (parent != null && parent instanceof StructureTFComponent) {
         this.deco = ((StructureTFComponent)parent).deco;
      }
   }

   @Override
   public boolean addComponentParts(OldWorld world, Random rand, StructureBoundingBox sbb) {
      for (int x = 0; x <= this.size - 1; x++) {
         for (int z = 0; z <= this.size - 1; z++) {
            if (x == 0 || x == this.size - 1 || z == 0 || z == this.size - 1) {
               this.placeBlockAtCurrentPosition(world, this.deco.fenceID, this.deco.fenceMeta, x, 1, z, sbb);
            }
         }
      }

      this.placeBlockAtCurrentPosition(world, this.deco.accentID, this.deco.accentMeta, 0, 1, 0, sbb);
      this.placeBlockAtCurrentPosition(world, this.deco.accentID, this.deco.accentMeta, this.size - 1, 1, 0, sbb);
      this.placeBlockAtCurrentPosition(world, this.deco.accentID, this.deco.accentMeta, 0, 1, this.size - 1, sbb);
      this.placeBlockAtCurrentPosition(world, this.deco.accentID, this.deco.accentMeta, this.size - 1, 1, this.size - 1, sbb);
      return true;
   }
}

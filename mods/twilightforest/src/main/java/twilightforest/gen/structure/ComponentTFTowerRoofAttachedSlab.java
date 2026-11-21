package twilightforest.gen.structure;

import hexxit.worldgen152.core.populate.OldWorld;
import hexxit.worldgen152.core.populate.OldTile;
import hexxit.worldgen152.core.structure.*;
import twilightforest.gen.*;

import java.util.Random;

public class ComponentTFTowerRoofAttachedSlab extends ComponentTFTowerRoofSlab {
   public ComponentTFTowerRoofAttachedSlab(int i, ComponentTFTowerWing wing) {
      super(i, wing);
   }

   @Override
   public boolean addComponentParts(OldWorld world, Random rand, StructureBoundingBox sbb) {
      int slabMeta = 2;
      return this.makeConnectedCap(world, slabMeta, sbb);
   }
}

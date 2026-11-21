package twilightforest.gen.structure;

import hexxit.worldgen152.core.populate.OldWorld;
import hexxit.worldgen152.core.populate.OldTile;
import hexxit.worldgen152.core.structure.*;
import twilightforest.gen.*;

import java.util.List;
import java.util.Random;

public class ComponentTFDarkTowerBalcony extends ComponentTFTowerWing {
   protected ComponentTFDarkTowerBalcony(int i, int x, int y, int z, int direction) {
      super(i, x, y, z, 5, 5, direction);
   }

   @Override
   public void buildComponent(StructureComponent parent, List list, Random rand) {
      if (parent != null && parent instanceof StructureTFComponent) {
         this.deco = ((StructureTFComponent)parent).deco;
      }
   }

   @Override
   public boolean addComponentParts(OldWorld world, Random rand, StructureBoundingBox sbb) {
      this.fillWithMetadataBlocks(world, sbb, 0, 0, 0, 2, 0, 4, this.deco.accentID, this.deco.accentMeta, 0, 0, false);
      this.fillWithMetadataBlocks(world, sbb, 0, 0, 1, 1, 0, 3, this.deco.blockID, this.deco.blockMeta, 0, 0, false);
      this.fillWithMetadataBlocks(world, sbb, 0, 1, 0, 2, 1, 4, this.deco.fenceID, this.deco.fenceMeta, 0, 0, false);
      this.placeBlockAtCurrentPosition(world, this.deco.accentID, this.deco.accentMeta, 2, 1, 0, sbb);
      this.placeBlockAtCurrentPosition(world, this.deco.accentID, this.deco.accentMeta, 2, 1, 4, sbb);
      this.fillWithAir(world, sbb, 0, 1, 1, 1, 1, 3);
      return true;
   }
}

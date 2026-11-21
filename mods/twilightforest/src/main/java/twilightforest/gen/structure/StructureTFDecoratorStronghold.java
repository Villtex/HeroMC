package twilightforest.gen.structure;

import hexxit.worldgen152.core.populate.OldWorld;
import hexxit.worldgen152.core.populate.OldTile;
import hexxit.worldgen152.core.structure.*;
import twilightforest.gen.*;


public class StructureTFDecoratorStronghold extends StructureTFDecorator {
   public StructureTFDecoratorStronghold() {
      this.blockID = 98;
      this.blockMeta = 0;
      this.accentID = 98;
      this.accentMeta = 3;
      this.fenceID = 139;
      this.stairID = 109;
      this.pillarID = 98;
      this.pillarMeta = 1;
      this.platformID = 44;
      this.platformMeta = 13;
      this.randomBlocks = StructureTFComponent.getStrongholdStones();
   }
}

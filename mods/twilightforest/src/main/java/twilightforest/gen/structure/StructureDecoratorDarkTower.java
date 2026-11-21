package twilightforest.gen.structure;

import hexxit.worldgen152.core.populate.OldWorld;
import hexxit.worldgen152.core.populate.OldTile;
import hexxit.worldgen152.core.structure.*;
import twilightforest.gen.*;


public class StructureDecoratorDarkTower extends StructureTFDecorator {
   public StructureDecoratorDarkTower() {
      this.blockID = 1372;
      this.blockMeta = 0;
      this.accentID = 1372;
      this.accentMeta = 1;
      this.fenceID = 85;
      this.stairID = 134;
      this.pillarID = 1372;
      this.pillarMeta = 1;
      this.platformID = 1372;
      this.platformMeta = 1;
      this.randomBlocks = StructureTFComponent.getTowerWoods();
   }
}

package twilightforest.gen.structure;

import hexxit.worldgen152.core.populate.OldWorld;
import hexxit.worldgen152.core.populate.OldTile;
import hexxit.worldgen152.core.structure.*;
import twilightforest.gen.*;

public class ComponentTFTowerRoofPointyOverhang extends ComponentTFTowerRoofPointy {
   public ComponentTFTowerRoofPointyOverhang(int i, ComponentTFTowerWing wing) {
      super(i, wing);
      this.setCoordBaseMode(wing.getCoordBaseMode());
      this.size = wing.size + 2;
      this.height = this.size;
      this.makeOverhangBB(wing);
   }
}

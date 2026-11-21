package twilightforest.gen.structure;

import hexxit.worldgen152.core.populate.OldWorld;
import hexxit.worldgen152.core.populate.OldTile;
import hexxit.worldgen152.core.structure.*;
import twilightforest.gen.*;

import java.util.List;
import java.util.Random;

public class ComponentTFDarkTowerMainBridge extends ComponentTFDarkTowerBridge {
   protected ComponentTFDarkTowerMainBridge(int i, int x, int y, int z, int pSize, int pHeight, int direction) {
      super(i, x, y, z, 15, pHeight, direction);
   }

   @Override
   public boolean makeTowerWing(List list, Random rand, int index, int x, int y, int z, int wingSize, int wingHeight, int rotation) {
      int direction = (this.getCoordBaseMode() + rotation) % 4;
      int[] dx = this.offsetTowerCoords(x, y, z, 19, direction);
      ComponentTFDarkTowerMain wing = new ComponentTFDarkTowerMain(null, rand, index, dx[0], dx[1], dx[2], direction);
      list.add(wing);
      wing.buildComponent(this, list, rand);
      this.addOpening(x, y, z, rotation);
      return true;
   }
}

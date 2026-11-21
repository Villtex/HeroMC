package twilightforest.gen.structure;

import hexxit.worldgen152.core.populate.OldWorld;
import hexxit.worldgen152.core.populate.OldTile;
import hexxit.worldgen152.core.structure.*;
import twilightforest.gen.*;

import java.util.List;
import java.util.Random;

public class ComponentTFMazeRuins extends StructureTFComponent {
   public ComponentTFMazeRuins(OldWorld world, Random rand, int i, int x, int y, int z) {
      super(i);
      this.setCoordBaseMode(0);
      this.boundingBox = StructureTFComponent.getComponentToAddBoundingBox(x, y, z, 0, 0, 0, 0, 0, 0, 0);
   }

   public void buildComponent(StructureComponent structurecomponent, List list, Random random) {
      super.buildComponent(structurecomponent, list, random);
      ComponentTFMinotaurMaze maze = new ComponentTFMinotaurMaze(
         1, this.boundingBox.minX, this.boundingBox.minY - 14, this.boundingBox.minZ, 1
      );
      list.add(maze);
      maze.buildComponent(this, list, random);
      ComponentTFMazeEntranceShaft mazeEnter = new ComponentTFMazeEntranceShaft(
         2, random, this.boundingBox.minX + 1, this.boundingBox.minY, this.boundingBox.minZ + 1
      );
      list.add(mazeEnter);
      mazeEnter.buildComponent(this, list, random);
      ComponentTFMazeMound mazeAbove = new ComponentTFMazeMound(
         2, random, this.boundingBox.minX - 14, this.boundingBox.minY, this.boundingBox.minZ - 14
      );
      list.add(mazeAbove);
      mazeAbove.buildComponent(this, list, random);
   }

   public boolean addComponentParts(OldWorld world, Random rand, StructureBoundingBox sbb) {
      return true;
   }
}

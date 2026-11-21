package twilightforest.gen.structure;

import hexxit.worldgen152.core.populate.OldWorld;
import hexxit.worldgen152.core.populate.OldTile;
import hexxit.worldgen152.core.structure.*;
import twilightforest.gen.*;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TFStrongholdPieces {
   private static final TFStrongholdPieceWeight[] pieceWeightArray = new TFStrongholdPieceWeight[]{
      new TFStrongholdPieceWeight(ComponentTFStrongholdSmallHallway.class, 40, 0),
      new TFStrongholdPieceWeight(ComponentTFStrongholdLeftTurn.class, 20, 0),
      new TFStrongholdPieceWeight(ComponentTFStrongholdCrossing.class, 10, 4),
      new TFStrongholdPieceWeight(ComponentTFStrongholdRightTurn.class, 20, 0),
      new TFStrongholdPieceWeight(ComponentTFStrongholdDeadEnd.class, 5, 0),
      new TFStrongholdPieceWeight(ComponentTFStrongholdBalconyRoom.class, 10, 3, 2),
      new TFStrongholdPieceWeight(ComponentTFStrongholdTrainingRoom.class, 10, 2),
      new TFStrongholdPieceWeight(ComponentTFStrongholdSmallStairs.class, 10, 0),
      new TFStrongholdPieceWeight(ComponentTFStrongholdTreasureCorridor.class, 5, 0),
      new TFStrongholdPieceWeight(ComponentTFStrongholdAtrium.class, 5, 2, 3),
      new TFStrongholdPieceWeight(ComponentTFStrongholdFoundry.class, 5, 1, 4),
      new TFStrongholdPieceWeight(ComponentTFStrongholdTreasureRoom.class, 5, 1, 4),
      new TFStrongholdPieceWeight(ComponentTFStrongholdBossRoom.class, 10, 1, 4)
   };
   private List pieceList;
   // static in the original (shared by all strongholds and threads, so the first piece of a stronghold depended on
   // the one built before it); here they belong to one stronghold, as do copies of the weights
   int totalWeight = 0;
   private Class lastPieceMade;

   public void prepareStructurePieces() {
      this.pieceList = new ArrayList();

      for (TFStrongholdPieceWeight piece : pieceWeightArray) {
         TFStrongholdPieceWeight copy = new TFStrongholdPieceWeight(piece.pieceClass, piece.pieceWeight, piece.instancesLimit, piece.minimumDepth);
         copy.instancesSpawned = 0;
         this.pieceList.add(copy);
      }
   }

   public void markBossRoomUsed() {
      this.pieceList.remove(this.pieceList.size() - 1);
   }

   private boolean hasMoreLimitedPieces() {
      boolean flag = false;
      totalWeight = 0;

      for (Object o : this.pieceList) {
         TFStrongholdPieceWeight piece = (TFStrongholdPieceWeight) o;
         totalWeight = totalWeight + piece.pieceWeight;
         if (piece.instancesLimit > 0 && piece.instancesSpawned < piece.instancesLimit) {
            flag = true;
         }
      }

      return flag;
   }

   public StructureTFStrongholdComponent getNextComponent(StructureComponent parent, List list, Random random, int index, int facing, int x, int y, int z) {
      if (!this.hasMoreLimitedPieces()) {
         return null;
      }

      for (int i = 0; i < 5; i++) {
         int counter = random.nextInt(totalWeight);

         for (Object o : this.pieceList) {
         TFStrongholdPieceWeight piece = (TFStrongholdPieceWeight) o;
            counter -= piece.pieceWeight;
            if (counter < 0) {
               if (!piece.isDeepEnough(index) || piece.pieceClass == lastPieceMade) {
                  break;
               }

               StructureTFStrongholdComponent component = instantiateComponent(piece.pieceClass, index, facing, x, y, z);
               if (StructureComponent.findIntersecting(list, component.getBoundingBox()) == null) {
                  piece.instancesSpawned++;
                  if (!piece.canSpawnMoreStructures()) {
                     this.pieceList.remove(piece);
                  }

                  lastPieceMade = piece.pieceClass;
                  return component;
               }
            }
         }
      }

      StructureTFStrongholdComponent deadEnd = new ComponentTFStrongholdDeadEnd(index, facing, x, y, z);
      return StructureComponent.findIntersecting(list, deadEnd.getBoundingBox()) == null ? deadEnd : null;
   }

   private static StructureTFStrongholdComponent instantiateComponent(Class pieceClass, int index, int facing, int x, int y, int z) {
      try {
         return (StructureTFStrongholdComponent)pieceClass.getConstructor(int.class, int.class, int.class, int.class, int.class)
            .newInstance(index, facing, x, y, z);
      } catch (InstantiationException e) {
         e.printStackTrace();
      } catch (IllegalAccessException e) {
         e.printStackTrace();
      } catch (IllegalArgumentException e) {
         e.printStackTrace();
      } catch (InvocationTargetException e) {
         e.printStackTrace();
      } catch (NoSuchMethodException e) {
         e.printStackTrace();
      } catch (SecurityException e) {
         e.printStackTrace();
      }

      return null;
   }
}

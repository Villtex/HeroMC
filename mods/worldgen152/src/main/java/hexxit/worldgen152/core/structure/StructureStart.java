package hexxit.worldgen152.core.structure;

import hexxit.worldgen152.core.OldBiome;
import hexxit.worldgen152.core.OldBiomes;
import hexxit.worldgen152.core.populate.OldBlockInfo;
import hexxit.worldgen152.core.populate.OldEntity;
import hexxit.worldgen152.core.populate.OldLoot;
import hexxit.worldgen152.core.populate.OldTile;
import hexxit.worldgen152.core.populate.OldWorld;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;

public abstract class StructureStart {
    protected LinkedList<StructureComponent> components = new LinkedList<>();
    protected StructureBoundingBox boundingBox;

    protected StructureStart() {
    }

    public StructureBoundingBox getBoundingBox() {
        return this.boundingBox;
    }

    public LinkedList<StructureComponent> getComponents() {
        return this.components;
    }

    public void generateStructure(OldWorld var1, Random var2, StructureBoundingBox var3) {
        Iterator<StructureComponent> var4 = this.components.iterator();

        while (var4.hasNext()) {
            StructureComponent var5 = (StructureComponent)var4.next();
            if (var5.getBoundingBox().intersectsWith(var3) && !var5.addComponentParts(var1, var2, var3)) {
                var4.remove();
            }
        }
    }

    protected void updateBoundingBox() {
        this.boundingBox = StructureBoundingBox.getNewBoundingBox();

        for (StructureComponent var2 : this.components) {
            this.boundingBox.expandTo(var2.getBoundingBox());
        }
    }

    protected void markAvailableHeight(OldStructures var1, Random var2, int var3) {
        int var4 = 63 - var3;
        int var5 = this.boundingBox.getYSize() + 1;
        if (var5 < var4) {
            var5 += var2.nextInt(var4 - var5);
        }

        int var6 = var5 - this.boundingBox.maxY;
        this.boundingBox.offset(0, var6, 0);

        for (StructureComponent var8 : this.components) {
            var8.getBoundingBox().offset(0, var6, 0);
        }
    }

    protected void setRandomHeight(OldStructures var1, Random var2, int var3, int var4) {
        int var5 = var4 - var3 + 1 - this.boundingBox.getYSize();
        int var6 = 1;
        if (var5 > 1) {
            var6 = var3 + var2.nextInt(var5);
        } else {
            var6 = var3;
        }

        int var7 = var6 - this.boundingBox.minY;
        this.boundingBox.offset(0, var7, 0);

        for (StructureComponent var9 : this.components) {
            var9.getBoundingBox().offset(0, var7, 0);
        }
    }

    public boolean isSizeableStructure() {
        return true;
    }
}

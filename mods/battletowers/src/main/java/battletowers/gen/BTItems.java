package battletowers.gen;

import hexxit.worldgen152.core.populate.OldStack;

import java.util.Random;
import java.util.Set;

/**
 * TowerStageItemManager of Battle Towers 1.3.4: the chest items of one floor, "id-damage-chance-min-max;...", tried
 * one after the other. A copy is made for every floor of a tower, so each floor starts at the first entry again.
 */
final class BTItems {
    /**
     * Ids of the pack's list that had no item in Hexxit 1.0.11 (2587 is a typo, 5076 the Project Zulu gold scale
     * chest as written in its config, whose item is 5076 + 256): they took no random number for the stack size.
     */
    private static final Set<Integer> MISSING = Set.of(2587, 5076);

    private final int[] itemID;
    private final int[] itemDamage;
    private final int[] chanceToSpawn;
    private final int[] minAmount;
    private final int[] maxAmount;
    private int curIndex;

    BTItems(String configString) {
        String[] elements = configString.split(";");
        this.itemID = new int[elements.length];
        this.itemDamage = new int[elements.length];
        this.chanceToSpawn = new int[elements.length];
        this.minAmount = new int[elements.length];
        this.maxAmount = new int[elements.length];
        for (int i = 0; i < elements.length; i++) {
            String[] settings = elements[i].trim().split("-");
            if (settings.length >= 5) {
                this.itemID[i] = Integer.parseInt(settings[0]);
                this.itemDamage[i] = Integer.parseInt(settings[1]);
                this.chanceToSpawn[i] = Integer.parseInt(settings[2]);
                this.minAmount[i] = Integer.parseInt(settings[3]);
                this.maxAmount[i] = Integer.parseInt(settings[4]);
            }
        }
    }

    BTItems(BTItems toCopy) {
        this.itemID = toCopy.itemID.clone();
        this.itemDamage = toCopy.itemDamage.clone();
        this.chanceToSpawn = toCopy.chanceToSpawn.clone();
        this.minAmount = toCopy.minAmount.clone();
        this.maxAmount = toCopy.maxAmount.clone();
    }

    /** Whether 1.5.2 had an item (or block) with this id in the pack. */
    private static boolean exists(int id) {
        return id > 0 && !MISSING.contains(id);
    }

    OldStack getStageItem(Random rand) {
        OldStack result = null;
        if (this.curIndex < this.itemID.length && rand.nextInt(100) < this.chanceToSpawn[this.curIndex]) {
            int id = this.itemID[this.curIndex];
            if (exists(id)) {
                result = new OldStack(id, this.itemDamage[this.curIndex], this.minAmount[this.curIndex] + rand.nextInt(this.maxAmount[this.curIndex]));
            }
        }
        this.curIndex++;
        return result;
    }
}

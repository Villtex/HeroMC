package tconstruct.player;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * TPlayerStats of 1.5.2 as a player attachment: the extended armor inventory (ArmorExtended, 7 slots, NBT
 * "TConstruct.Inventory"), the knapsack (KnapsackInventory, 27 slots, "TConstruct.Knapsack"), the manual flags of the
 * "TConstruct" compound and the level and hunger kept over death. Copied on death; what is dropped (armor slots 0-3,
 * the whole knapsack) is taken out before.
 */
public final class PlayerData {
    public static final int ARMOR_SLOTS = 7;
    public static final int KNAPSACK_SLOTS = 27;
    /** ArmorExtended slot 2 holds the knapsack, slot 6 the heart canisters. */
    public static final int KNAPSACK_SLOT = 2;
    public static final int HEART_SLOT = 6;

    public final Inv armor = new Inv(ARMOR_SLOTS);
    public final Inv knapsack = new Inv(KNAPSACK_SLOTS);
    public boolean beginnerManual;
    public boolean materialManual;
    public boolean smelteryManual;
    /** Set on death (playerDrops): half the level, the hunger at least 6. -1 = nothing to restore. */
    public int level = -1;
    public int hunger = -1;
    /** Called after a change of the armor inventory (recalculateHealth), set by {@link PlayerEvents}. */
    public Runnable onArmorChanged = () -> {
    };

    private record Saved(List<ItemStack> armor, List<ItemStack> knapsack, boolean beginner, boolean material, boolean smeltery) {
    }

    public static final MapCodec<PlayerData> CODEC = RecordCodecBuilder.<Saved>mapCodec(i -> i.group(
                    ItemStack.OPTIONAL_CODEC.listOf().optionalFieldOf("Inventory", List.of()).forGetter(Saved::armor),
                    ItemStack.OPTIONAL_CODEC.listOf().optionalFieldOf("Knapsack", List.of()).forGetter(Saved::knapsack),
                    Codec.BOOL.optionalFieldOf("beginnerManual", false).forGetter(Saved::beginner),
                    Codec.BOOL.optionalFieldOf("materialManual", false).forGetter(Saved::material),
                    Codec.BOOL.optionalFieldOf("smelteryManual", false).forGetter(Saved::smeltery))
            .apply(i, Saved::new)).xmap(PlayerData::fromSaved, PlayerData::toSaved);

    private static PlayerData fromSaved(Saved saved) {
        PlayerData data = new PlayerData();
        data.armor.load(saved.armor());
        data.knapsack.load(saved.knapsack());
        data.beginnerManual = saved.beginner();
        data.materialManual = saved.material();
        data.smelteryManual = saved.smeltery();
        return data;
    }

    private Saved toSaved() {
        return new Saved(this.armor.list(), this.knapsack.list(), this.beginnerManual, this.materialManual, this.smelteryManual);
    }

    /** Death keeps the attachment (copyOnDeath); the level and hunger of the death go along. */
    public PlayerData copy() {
        PlayerData data = fromSaved(this.toSaved());
        data.level = this.level;
        data.hunger = this.hunger;
        return data;
    }

    /** A container that tells its owner about changes (ArmorExtended.onInventoryChanged). */
    public final class Inv extends SimpleContainer {
        Inv(int size) {
            super(size);
        }

        void load(List<ItemStack> items) {
            for (int i = 0; i < items.size() && i < this.getContainerSize(); i++) {
                this.getItems().set(i, items.get(i));
            }
        }

        List<ItemStack> list() {
            return new ArrayList<>(this.getItems());
        }

        @Override
        public void setChanged() {
            super.setChanged();
            if (this == PlayerData.this.armor) {
                PlayerData.this.onArmorChanged.run();
            }
        }
    }
}

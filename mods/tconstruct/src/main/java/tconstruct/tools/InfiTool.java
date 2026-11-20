package tconstruct.tools;

import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;
import tconstruct.TItems;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A mutable copy of a tool's {@link ToolData}, with the names of the {@code InfiTool} tags of 1.5.2, so the code of
 * the tool builder and the modifiers reads like the original. {@link #of(ItemStack)} reads it, {@link #save(ItemStack)}
 * writes it back.
 */
public final class InfiTool {
    public int head = -1;
    public int handle = -1;
    public int accessory = -1;
    public int extra = -1;
    public int attack;
    public int miningSpeed;
    public int harvestLevel;
    public int miningSpeedHandle = -1;
    public int harvestLevelHandle = -1;
    public int miningSpeed2 = -1;
    public int harvestLevel2 = -1;
    public int miningSpeedExtra = -1;
    public int harvestLevelExtra = -1;
    public int unbreaking;
    public float shoddy;
    public int modifiers;
    public int damage;
    public int totalDurability;
    public int baseDurability;
    public int bonusDurability;
    public float modDurability;
    public boolean broken;
    public int repairCount;
    /** Effect1..Effect6. */
    public final List<Integer> effects = new ArrayList<>();
    /** Tooltip1.. / ModifierTip1.. (index 0 = number 1). */
    public final List<String[]> tips = new ArrayList<>();
    private final Map<String, int[]> ints = new LinkedHashMap<>();
    private final Map<String, Float> floats = new LinkedHashMap<>();

    public InfiTool() {
    }

    public static @Nullable InfiTool of(ItemStack stack) {
        ToolData data = stack.get(TItems.TOOL.get());
        return data == null ? null : from(data);
    }

    public static InfiTool from(ToolData data) {
        InfiTool t = new InfiTool();
        t.head = data.head();
        t.handle = data.handle();
        t.accessory = data.accessory();
        t.extra = data.extra();
        ToolData.Stats s = data.stats();
        t.attack = s.attack();
        t.miningSpeed = s.miningSpeed();
        t.harvestLevel = s.harvestLevel();
        t.miningSpeedHandle = s.miningSpeedHandle();
        t.harvestLevelHandle = s.harvestLevelHandle();
        t.miningSpeed2 = s.miningSpeed2();
        t.harvestLevel2 = s.harvestLevel2();
        t.miningSpeedExtra = s.miningSpeedExtra();
        t.harvestLevelExtra = s.harvestLevelExtra();
        t.unbreaking = s.unbreaking();
        t.shoddy = s.shoddy();
        t.modifiers = s.modifiers();
        ToolData.Durability d = data.durability();
        t.damage = d.damage();
        t.totalDurability = d.total();
        t.baseDurability = d.base();
        t.bonusDurability = d.bonus();
        t.modDurability = d.modifier();
        t.broken = d.broken();
        t.repairCount = d.repairCount();
        ToolData.Mods m = data.mods();
        t.effects.addAll(m.effects());
        for (ToolData.Tip tip : m.tips()) {
            t.tips.add(new String[]{tip.tooltip(), tip.modifierTip()});
        }
        m.ints().forEach((k, v) -> t.ints.put(k, v.stream().mapToInt(Integer::intValue).toArray()));
        t.floats.putAll(m.floats());
        return t;
    }

    public ToolData toData() {
        List<ToolData.Tip> tipList = new ArrayList<>();
        for (String[] tip : this.tips) {
            tipList.add(new ToolData.Tip(tip[0], tip[1]));
        }
        Map<String, List<Integer>> intMap = new LinkedHashMap<>();
        this.ints.forEach((k, v) -> {
            List<Integer> list = new ArrayList<>(v.length);
            for (int i : v) {
                list.add(i);
            }
            intMap.put(k, List.copyOf(list));
        });
        return new ToolData(this.head, this.handle, this.accessory, this.extra,
                new ToolData.Stats(this.attack, this.miningSpeed, this.harvestLevel, this.miningSpeedHandle, this.harvestLevelHandle,
                        this.miningSpeed2, this.harvestLevel2, this.miningSpeedExtra, this.harvestLevelExtra, this.unbreaking, this.shoddy,
                        this.modifiers),
                new ToolData.Durability(this.damage, this.totalDurability, this.baseDurability, this.bonusDurability, this.modDurability,
                        this.broken, this.repairCount),
                new ToolData.Mods(List.copyOf(this.effects), List.copyOf(tipList), Map.copyOf(intMap), Map.copyOf(this.floats)));
    }

    public void save(ItemStack stack) {
        stack.set(TItems.TOOL.get(), this.toData());
    }

    // ------------------------------------------------------------------ the named tags of 1.5.2

    /** hasKey for the modifier tags (ints, booleans, int arrays and floats). */
    public boolean has(String key) {
        return this.ints.containsKey(key) || this.floats.containsKey(key);
    }

    public int getInt(String key) {
        int[] v = this.ints.get(key);
        return v == null || v.length == 0 ? 0 : v[0];
    }

    public void setInt(String key, int value) {
        this.ints.put(key, new int[]{value});
    }

    public boolean getBool(String key) {
        return this.getInt(key) != 0;
    }

    public void setBool(String key, boolean value) {
        this.setInt(key, value ? 1 : 0);
    }

    /** getIntArray: a copy (an empty array if missing, like NBT). */
    public int[] getArray(String key) {
        int[] v = this.ints.get(key);
        return v == null ? new int[0] : v.clone();
    }

    public void setArray(String key, int[] value) {
        this.ints.put(key, value.clone());
    }

    public float getFloat(String key) {
        Float v = this.floats.get(key);
        return v == null ? 0.0F : v;
    }

    public void setFloat(String key, float value) {
        this.floats.put(key, value);
    }

    public void remove(String key) {
        this.ints.remove(key);
        this.floats.remove(key);
    }

    /** The ModifierTip of tooltip number {@code number} (1-based, as in "ModifierTip3"), or "". */
    public String modifierTip(int number) {
        return number >= 1 && number <= this.tips.size() ? this.tips.get(number - 1)[1] : "";
    }

    public void setModifierTip(int number, String tip) {
        if (number >= 1 && number <= this.tips.size()) {
            this.tips.get(number - 1)[1] = tip;
        }
    }

    // ------------------------------------------------------------------ helpers

    public @Nullable TMaterial headMaterial() {
        return TMaterial.byId(this.head);
    }

    /** MiningSpeed of all heads together and the number of heads (HarvestTool.getStrVsBlock). */
    public float summedSpeed(int[] heads) {
        float speed = this.miningSpeed;
        int count = 1;
        if (this.miningSpeed2 >= 0) {
            speed += this.miningSpeed2;
            count++;
        }
        if (this.miningSpeedHandle >= 0) {
            speed += this.miningSpeedHandle;
            count++;
        }
        if (this.miningSpeedExtra >= 0) {
            speed += this.miningSpeedExtra;
            count++;
        }
        heads[0] = count;
        return speed;
    }
}

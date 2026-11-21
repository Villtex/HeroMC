package twilightforest.entity;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import twilightforest.TwilightForest;
import twilightforest.entity.ai.RedcapGoals;

/**
 * EntityTFRedcapSapper: a redcap with 30 health, 2 more armour, three TNT to plant behind a target that does not look
 * at it, and ironwood pickaxe and boots (Teil 24f; iron ones until those exist).
 */
public class RedcapSapper extends Redcap {
    public RedcapSapper(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.setTntLeft(3);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Redcap.createAttributes().add(Attributes.MAX_HEALTH, 30.0).add(Attributes.ARMOR, 2.0);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(4, new RedcapGoals.PlantTnt(this));
    }

    private static Item ironwood(String name, Item fallback) {
        return BuiltInRegistries.ITEM.getOptional(TwilightForest.id(name)).orElse(fallback);
    }

    @Override
    protected void equip() {
        super.equip();
        this.setItemSlot(EquipmentSlot.FEET, new ItemStack(ironwood("ironwood_boots", Items.IRON_BOOTS)));
    }

    @Override
    public ItemStack pick() {
        return new ItemStack(ironwood("ironwood_pickaxe", Items.IRON_PICKAXE));
    }
}

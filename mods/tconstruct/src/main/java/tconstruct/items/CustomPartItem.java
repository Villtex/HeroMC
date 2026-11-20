package tconstruct.items;

import net.minecraft.world.item.Item;
import tconstruct.tools.PartType;

/**
 * {@code Bowstring} and {@code Fletching}: parts made of their own materials (BowstringMaterial / FletchingMaterial of
 * TContent.registerMaterials, see {@link CustomMaterials}).
 */
public class CustomPartItem extends Item implements IToolPart {
    public final PartType type;
    public final int id;

    public CustomPartItem(PartType type, int id, Properties properties) {
        super(properties);
        this.type = type;
        this.id = id;
    }

    @Override
    public PartType partType() {
        return this.type;
    }

    @Override
    public int materialId() {
        return this.id;
    }
}

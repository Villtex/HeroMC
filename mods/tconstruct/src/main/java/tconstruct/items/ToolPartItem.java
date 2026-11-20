package tconstruct.items;

import net.minecraft.world.item.Item;
import tconstruct.tools.PartType;
import tconstruct.tools.TMaterial;

/** {@code ToolPart} / {@code ToolShard}: one part of one material (the item damage of 1.5.2 is the material). */
public class ToolPartItem extends Item implements IToolPart {
    public final PartType type;
    public final TMaterial material;

    public ToolPartItem(PartType type, TMaterial material, Properties properties) {
        super(properties);
        this.type = type;
        this.material = material;
    }

    @Override
    public PartType partType() {
        return this.type;
    }

    @Override
    public int materialId() {
        return this.material.id;
    }
}

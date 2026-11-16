package natura.entity;

import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.boat.Boat;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.fluids.FluidType;

import java.util.function.Supplier;

/** BloodBoat: fire-proof and floats on lava as on water. */
public class BloodwoodBoat extends Boat {
    public BloodwoodBoat(EntityType<? extends Boat> type, Level level, Supplier<Item> item) {
        super(type, level, item);
    }

    @Override
    public boolean canBoatInFluid(FluidState state) {
        return state.is(FluidTags.LAVA) || super.canBoatInFluid(state);
    }

    @Override
    public boolean canBoatInFluid(FluidType type) {
        return type == NeoForgeMod.LAVA_TYPE.value() || super.canBoatInFluid(type);
    }
}

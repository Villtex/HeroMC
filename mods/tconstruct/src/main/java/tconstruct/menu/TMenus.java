package tconstruct.menu;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import tconstruct.TConstruct;

/** The menus of the tables (the crafting station uses the vanilla crafting menu type). */
public final class TMenus {
    private static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, TConstruct.MOD_ID);

    public static final DeferredHolder<MenuType<?>, MenuType<ToolStationMenu>> TOOL_STATION = MENUS.register("tool_station",
            () -> IMenuTypeExtension.create((id, inv, buf) -> {
                buf.readBlockPos();
                boolean forge = buf.readBoolean();
                return new ToolStationMenu(id, inv, new SimpleContainer(forge ? 5 : 4), forge);
            }));
    public static final DeferredHolder<MenuType<?>, MenuType<PartBuilderMenu>> PART_BUILDER = MENUS.register("part_builder",
            () -> IMenuTypeExtension.create((id, inv, buf) -> {
                buf.readBlockPos();
                boolean chest = buf.readBoolean();
                return new PartBuilderMenu(id, inv, new SimpleContainer(10), chest ? new SimpleContainer(30) : null);
            }));
    public static final DeferredHolder<MenuType<?>, MenuType<PatternChestMenu>> PATTERN_CHEST = MENUS.register("pattern_chest",
            () -> IMenuTypeExtension.create((id, inv, buf) -> new PatternChestMenu(id, inv, new SimpleContainer(30))));
    public static final DeferredHolder<MenuType<?>, MenuType<StencilTableMenu>> STENCIL_TABLE = MENUS.register("stencil_table",
            () -> IMenuTypeExtension.create((id, inv, buf) -> new StencilTableMenu(id, inv, new SimpleContainer(2))));

    private TMenus() {
    }

    public static void register(IEventBus modBus) {
        MENUS.register(modBus);
    }
}

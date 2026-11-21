package twilightforest;

import hexxit.worldgen152.OldBlockStates;
import hexxit.worldgen152.OldGameIds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import twilightforest.block.UncraftingMenu;

/** BlockTFUncraftingTable (1374): Material.wood, hardness 2.5, wood sound; a right click opens the uncrafting menu. */
public final class TFUncrafting {
    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(TwilightForest.MOD_ID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(TwilightForest.MOD_ID);
    private static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, TwilightForest.MOD_ID);
    public static final int ID_UNCRAFTING_TABLE = 1374;

    public static final DeferredBlock<Block> UNCRAFTING_TABLE = BLOCKS.registerBlock("uncrafting_table", p -> new Block(p) {
        @Override
        protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
            if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.openMenu(new SimpleMenuProvider((id, inv, p2) -> new UncraftingMenu(id, inv, UncraftingMenu.access(level, pos)),
                        Component.translatable("container.twilightforest.uncrafting_table")));
            }
            return InteractionResult.SUCCESS;
        }
    }, p -> p.mapColor(MapColor.WOOD).strength(2.5F).sound(SoundType.WOOD).ignitedByLava());
    public static final DeferredItem<BlockItem> UNCRAFTING_TABLE_ITEM = ITEMS.registerSimpleBlockItem(UNCRAFTING_TABLE);
    public static final DeferredHolder<MenuType<?>, MenuType<UncraftingMenu>> MENU = MENUS.register("uncrafting",
            () -> IMenuTypeExtension.create(UncraftingMenu::fromNetwork));

    private TFUncrafting() {
    }

    static void register(IEventBus modBus) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        MENUS.register(modBus);
    }

    static void registerOld() {
        OldBlockStates.register(ID_UNCRAFTING_TABLE, () -> UNCRAFTING_TABLE.get().defaultBlockState());
        OldGameIds.registerItem(ID_UNCRAFTING_TABLE, UNCRAFTING_TABLE_ITEM.getId().toString());
    }
}

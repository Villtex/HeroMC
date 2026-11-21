package tconstruct.client.deco;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import tconstruct.TDeco;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * Client side of part 25f: the landmine and drawbridge GUIs, the camouflage models and their tint, the book of the essence
 * extractor and the item tooltips (MultiBrickItem, BarricadeItem, ItemBlockLandmine).
 */
public final class TDecoClient {
    private TDecoClient() {
    }

    public static void register(IEventBus modBus) {
        modBus.addListener(RegisterMenuScreensEvent.class, event -> {
            event.register(TDeco.LANDMINE_MENU.get(), LandmineScreen::new);
            event.register(TDeco.DRAWBRIDGE_MENU.get(), DrawbridgeScreen::new);
        });
        modBus.addListener(EntityRenderersEvent.RegisterRenderers.class,
                event -> event.registerBlockEntityRenderer(TDeco.EXTRACTOR_ENTITY.get(), ExtractorRenderer::new));
        modBus.addListener(ModelEvent.ModifyBakingResult.class, event -> {
            Map<BlockState, BlockStateModel> models = event.getBakingResult().blockStateModels();
            wrap(models, List.of(TDeco.DRAWBRIDGE.get()), CamoModel.Kind.FULL);
            wrap(models, TDeco.LANDMINE_BLOCKS.stream().map(DeferredBlock::get).map(b -> (Block) b).toList(), CamoModel.Kind.BOX);
        });
        modBus.addListener(RegisterColorHandlersEvent.BlockTintSources.class, event -> {
            List<BlockTintSource> sources = new ArrayList<>();
            for (int i = 0; i < 4; i++) {
                sources.add(new CamoTint(i));
            }
            List<Block> blocks = new ArrayList<>();
            blocks.add(TDeco.DRAWBRIDGE.get());
            TDeco.LANDMINE_BLOCKS.forEach(b -> blocks.add(b.get()));
            event.register(sources, blocks.toArray(new Block[0]));
        });
        NeoForge.EVENT_BUS.addListener(ItemTooltipEvent.class, TDecoClient::tooltip);
    }

    private static void wrap(Map<BlockState, BlockStateModel> models, List<Block> blocks, CamoModel.Kind kind) {
        for (Block block : blocks) {
            for (BlockState state : block.getStateDefinition().getPossibleStates()) {
                BlockStateModel own = models.get(state);
                if (own != null) {
                    models.put(state, new CamoModel(own, kind));
                }
            }
        }
    }

    private static Map<Item, List<String>> tips;

    private static Map<Item, List<String>> tips() {
        if (tips == null) {
            tips = new IdentityHashMap<>();
            for (List<DeferredBlock<tconstruct.deco.MultiBrickBlock>> list : List.of(TDeco.MULTI_BRICKS, TDeco.FANCY_BRICKS)) {
                tips.put(list.get(5).get().asItem(), List.of("tooltip.tconstruct.brick.somewhat_expensive"));
                tips.put(list.get(6).get().asItem(), List.of("tooltip.tconstruct.brick.somewhat_expensive"));
                tips.put(list.get(7).get().asItem(), List.of("tooltip.tconstruct.brick.quite_expensive"));
                tips.put(list.get(8).get().asItem(), List.of("tooltip.tconstruct.brick.redstone"));
                tips.put(list.get(10).get().asItem(), List.of("tooltip.tconstruct.brick.bouncy"));
                tips.put(list.get(11).get().asItem(), List.of("tooltip.tconstruct.brick.bouncy"));
            }
            TDeco.BARRICADES.forEach(b -> tips.put(b.get().asItem(), List.of("tooltip.tconstruct.barricade")));
            for (int i = 0; i < TDeco.LANDMINES.length; i++) {
                tips.put(TDeco.LANDMINE_BLOCKS.get(i).get().asItem(), List.of("tooltip.tconstruct." + TDeco.LANDMINES[i]));
            }
        }
        return tips;
    }

    private static void tooltip(ItemTooltipEvent event) {
        List<String> keys = tips().get(event.getItemStack().getItem());
        if (keys != null) {
            for (String key : keys) {
                event.getToolTip().add(Component.translatable(key).withStyle(ChatFormatting.GRAY));
            }
        }
    }

    /** The tint of the camouflage block. */
    record CamoTint(int index) implements BlockTintSource {
        @Override
        public int color(BlockState state) {
            return -1;
        }

        @Override
        public int colorInWorld(BlockState state, BlockAndTintGetter level, BlockPos pos) {
            BlockState camo = CamoModel.camo(level, pos, state);
            if (camo == null) {
                return -1;
            }
            BlockTintSource source = Minecraft.getInstance().getBlockColors().getTintSource(camo, this.index);
            return source == null ? -1 : source.colorInWorld(camo, level, pos);
        }
    }
}

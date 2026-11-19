package qcraft.client;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.item.properties.select.SelectItemModelProperty;
import net.minecraft.client.resources.model.ModelDebugName;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GrassColor;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterSelectItemModelPropertyEvent;
import net.neoforged.neoforge.client.model.standalone.SimpleUnbakedStandaloneModel;
import net.neoforged.neoforge.client.model.standalone.StandaloneModelKey;
import net.neoforged.neoforge.common.NeoForge;
import org.jspecify.annotations.Nullable;
import qcraft.QuantumCraft;
import qcraft.block.Impostors;
import qcraft.block.QBlock;
import qcraft.block.QBlockEntity;
import qcraft.item.QBlockItem;

import java.util.List;
import java.util.Map;

/** QuantumCraftProxyClient: qBlock model and tint, the qBlock item cycling its sides, the quantum computer GUI. */
public final class QCraftClient {
    static final StandaloneModelKey<BlockStateModelPart> SWIRL = new StandaloneModelKey<>(() -> "quantumcraft:qblock_swirl");
    static final StandaloneModelKey<BlockStateModelPart> FUZZ = new StandaloneModelKey<>(() -> "quantumcraft:qblock_fuzz");
    /** m_tickCount: client ticks since start. */
    static long tickCount;

    private QCraftClient() {
    }

    public static void init(IEventBus modBus) {
        QBlockEntity.localPlayerWearingGoggles = QCraftClient::isLocalPlayerWearingGoggles;
        modBus.addListener((RegisterMenuScreensEvent e) -> e.register(QuantumCraft.QUANTUM_COMPUTER_MENU.get(), QuantumComputerScreen::new));
        modBus.addListener((RegisterColorHandlersEvent.BlockTintSources e) -> e.register(List.of(new QBlockTint()), QuantumCraft.QBLOCK.get()));
        modBus.addListener((ModelEvent.RegisterStandalone e) -> {
            e.register(SWIRL, SimpleUnbakedStandaloneModel.simpleModelWrapper(QuantumCraft.id("block/qblock_swirl")));
            e.register(FUZZ, SimpleUnbakedStandaloneModel.simpleModelWrapper(QuantumCraft.id("block/qblock_fuzz")));
        });
        modBus.addListener(QCraftClient::modifyModels);
        modBus.addListener((RegisterSelectItemModelPropertyEvent e) -> e.register(QuantumCraft.id("qblock_side"), QBlockSide.TYPE));
        NeoForge.EVENT_BUS.addListener((ClientTickEvent.Pre e) -> tickCount++);
    }

    static boolean isLocalPlayerWearingGoggles() {
        Player player = Minecraft.getInstance().player;
        return player != null && player.getItemBySlot(EquipmentSlot.HEAD).is(QuantumCraft.QUANTUM_GOGGLES.get());
    }

    private static void modifyModels(ModelEvent.ModifyBakingResult event) {
        Map<BlockState, BlockStateModel> models = event.getBakingResult().blockStateModels();
        BlockStateModelPart swirl = event.getBakingResult().standaloneModels().get(SWIRL);
        BlockStateModelPart fuzz = event.getBakingResult().standaloneModels().get(FUZZ);
        if (swirl == null || fuzz == null) {
            return;
        }
        for (BlockState state : QuantumCraft.QBLOCK.get().getStateDefinition().getPossibleStates()) {
            BlockState impostor = QBlock.impostorState(state);
            models.put(state, new QBlockModel(impostor.isAir() ? null : models.get(impostor), impostor, swirl, fuzz));
        }
    }

    /** colorMultiplier: the grass colour while the qBlock is grass, white otherwise. */
    static final class QBlockTint implements BlockTintSource {
        @Override
        public int color(BlockState state) {
            return state.getValue(QBlock.TYPE) == Impostors.GRASS ? GrassColor.getDefaultColor() : -1;
        }

        @Override
        public int colorInWorld(BlockState state, BlockAndTintGetter level, BlockPos pos) {
            return state.getValue(QBlock.TYPE) == Impostors.GRASS ? BiomeColors.getAverageGrassColor(level, pos) : -1;
        }

        @Override
        public java.util.Set<net.minecraft.world.level.block.state.properties.Property<?>> relevantProperties() {
            return java.util.Set.of(QBlock.TYPE);
        }
    }

    /**
     * renderInventoryQBlock / cycleType: the item shows its six side types one after the other, 20 ticks each, the first
     * 6 of them as fuzz. Values: "fuzz" or the type number ("0" draws nothing).
     */
    public record QBlockSide() implements SelectItemModelProperty<String> {
        public static final SelectItemModelProperty.Type<QBlockSide, String> TYPE = SelectItemModelProperty.Type.create(
                MapCodec.unit(new QBlockSide()), Codec.STRING);

        @Override
        public String get(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed, ItemDisplayContext context) {
            int[] types = QBlockItem.getTypes(stack);
            int cycle = (int) (tickCount % 120L);
            if (cycle % 20 > 5) {
                return Integer.toString(types[cycle / 20]);
            }
            return "fuzz";
        }

        @Override
        public Codec<String> valueCodec() {
            return Codec.STRING;
        }

        @Override
        public SelectItemModelProperty.Type<QBlockSide, String> type() {
            return TYPE;
        }
    }
}

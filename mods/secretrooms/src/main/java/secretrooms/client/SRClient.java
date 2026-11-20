package secretrooms.client;

import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.registries.DeferredBlock;
import secretrooms.SRBlocks;
import secretrooms.SRNetwork;
import secretrooms.SecretRooms;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * ProxyClient: the camo models, the tint of the copied block, the one-way facing key (backslash, SecretKey) and
 * the /srm-show toggle (SecretRooms.displayCamo, re-rendering 20 blocks around the player).
 */
public final class SRClient {
    /** SecretRooms.displayCamo. */
    public static boolean displayCamo = true;
    private static boolean faceTowards = true;
    static final KeyMapping KEY = new KeyMapping("key.secretroomsmod.one_way_face", 92,
            KeyMapping.Category.MISC);

    private SRClient() {
    }

    private static List<Block> blocks(List<? extends DeferredBlock<? extends Block>> list) {
        List<Block> out = new ArrayList<>();
        list.forEach(b -> out.add(b.get()));
        return out;
    }

    public static void register(IEventBus modBus) {
        modBus.addListener(ModelEvent.ModifyBakingResult.class, e -> {
            Map<BlockState, BlockStateModel> models = e.getBakingResult().blockStateModels();
            wrap(models, blocks(SRBlocks.fullCamo()), CamoModel.Kind.FULL);
            wrap(models, List.of(SRBlocks.ONE_WAY_GLASS.get()), CamoModel.Kind.ONE_WAY);
            wrap(models, List.of(SRBlocks.SECRET_WOODEN_DOOR.get(), SRBlocks.SECRET_IRON_DOOR.get(), SRBlocks.SECRET_TRAPDOOR.get()), CamoModel.Kind.BOX);
        });
        modBus.addListener(RegisterColorHandlersEvent.BlockTintSources.class, e -> {
            List<BlockTintSource> sources = new ArrayList<>();
            for (int i = 0; i < 4; i++) {
                sources.add(new CamoTint(i));
            }
            List<Block> all = blocks(SRBlocks.fullCamo());
            all.add(SRBlocks.ONE_WAY_GLASS.get());
            all.add(SRBlocks.SECRET_WOODEN_DOOR.get());
            all.add(SRBlocks.SECRET_IRON_DOOR.get());
            all.add(SRBlocks.SECRET_TRAPDOOR.get());
            e.register(sources, all.toArray(Block[]::new));
        });
        modBus.addListener(RegisterKeyMappingsEvent.class, e -> e.register(KEY));
        NeoForge.EVENT_BUS.addListener(ClientTickEvent.Post.class, e -> keys());
        SRNetwork.clientToggle = SRClient::toggleShow;
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

    private static void keys() {
        Minecraft mc = Minecraft.getInstance();
        while (KEY.consumeClick()) {
            if (mc.player != null && mc.gui.screen() == null) {
                faceTowards = !faceTowards;
                ClientPacketDistributor.sendToServer(new SRNetwork.Key());
                mc.player.sendSystemMessage(Component.translatable(faceTowards ? "message.secretroomsmod.towards" : "message.secretroomsmod.away")
                        .withStyle(ChatFormatting.YELLOW));
            }
        }
    }

    private static void toggleShow() {
        Minecraft mc = Minecraft.getInstance();
        displayCamo = !displayCamo;
        if (mc.player == null || mc.level == null) {
            return;
        }
        mc.player.sendSystemMessage(Component.translatable(displayCamo ? "message.secretroomsmod.secret" : "message.secretroomsmod.obvious")
                .withStyle(ChatFormatting.YELLOW));
        BlockPos p = mc.player.blockPosition();
        int r = 20;
        mc.level.setSectionRangeDirty(SectionPos.blockToSectionCoord(p.getX() - r), SectionPos.blockToSectionCoord(p.getY() - r),
                SectionPos.blockToSectionCoord(p.getZ() - r), SectionPos.blockToSectionCoord(p.getX() + r), SectionPos.blockToSectionCoord(p.getY() + r),
                SectionPos.blockToSectionCoord(p.getZ() + r));
    }

    /** colorMultiplier of the camo blocks: the tint of the copied block. */
    record CamoTint(int index) implements BlockTintSource {
        @Override
        public int color(BlockState state) {
            return -1;
        }

        @Override
        public int colorInWorld(BlockState state, net.minecraft.client.renderer.block.BlockAndTintGetter level, BlockPos pos) {
            BlockState camo = displayCamo ? CamoModel.camo(level, pos, state) : null;
            if (camo == null) {
                return -1;
            }
            BlockTintSource source = Minecraft.getInstance().getBlockColors().getTintSource(camo, this.index);
            return source == null ? -1 : source.colorInWorld(camo, level, pos);
        }
    }
}

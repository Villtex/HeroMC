package artifice;

import artifice.block.ScaffoldingBlock;
import artifice.item.DispenseBox;
import artifice.item.DispenseSledge;
import artifice.item.SickleItem;
import artifice.item.SledgeItem;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.event.entity.player.BonemealEvent;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.List;

/** Game events: sledge and sickle breaking, climbing scaffolding, flowers from bone meal, dispenser behaviours. */
public final class ArtificeEvents {
    private ArtificeEvents() {
    }

    static void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            ArtificeItems.SLEDGES.forEach(sledge -> DispenserBlock.registerBehavior(sledge.get(), new DispenseSledge()));
            DispenserBlock.registerBehavior(ArtificeItems.BOX.get(), new DispenseBox());
        });
    }

    /** ItemSledge/ItemSickle.onBlockStartBreak: they replace the normal break of the blocks they handle. */
    @SubscribeEvent
    public static void onBreak(BreakBlockEvent event) {
        if (event.isCanceled() || !(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        Player player = event.getPlayer();
        ItemStack stack = player.getMainHandItem();
        BlockPos pos = event.getPos();
        BlockState state = event.getState();
        boolean handled = false;
        if (stack.getItem() instanceof SledgeItem sledge && !player.getAbilities().instabuild) {
            handled = sledge.smash(stack, level, pos, state, player);
        } else if (stack.getItem() instanceof SickleItem sickle) {
            handled = sickle.harvest(stack, level, pos, state, player);
        }
        if (handled) {
            event.setNotifyClient(false);
            event.setCanceled(true);
        }
    }

    /**
     * ScaffoldTicker (client, every player tick): with the block at the feet in front of the player (one look vector
     * ahead) a scaffolding, walking forward lifts the player (motionY 0.1), sneaking holds him, else he sinks slowly
     * (-0.15). Both sides also clear the fall distance there (onEntityCollidedWithBlock), so no fall damage.
     */
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Pre event) {
        Player player = event.getEntity();
        Vec3 look = player.getLookAngle();
        BlockPos front = BlockPos.containing(player.getX() + look.x, player.getBoundingBox().minY, player.getZ() + look.z);
        if (!(player.level().getBlockState(front).getBlock() instanceof ScaffoldingBlock)) {
            return;
        }
        player.resetFallDistance();
        if (player.isLocalPlayer()) {
            Vec3 motion = player.getDeltaMovement();
            double y = player.zza > 0.0F ? 0.1 : player.isShiftKeyDown() ? 0.0 : -0.15;
            player.setDeltaMovement(motion.x, y, motion.z);
        }
    }

    /**
     * MinecraftForge.addGrassPlant(blockFlora, 0-3, 10): bone meal on grass also plants Artifice flowers. 1.5.2 drew
     * a flower in one of ten spots of its 128 tries, from dandelion 20, rose 10 and the four flowers 10 each. The
     * grass bone meal of today stays as it is; this adds the Artifice share of those flowers in its own 128 tries.
     */
    @SubscribeEvent
    public static void onBonemeal(BonemealEvent event) {
        Level level = event.getLevel();
        if (level.isClientSide() || event.isCanceled() || !event.getState().is(Blocks.GRASS_BLOCK) || !event.isValidBonemealTarget()) {
            return;
        }
        List<Block> flora = List.of(ArtificeBlocks.BLUEBELL.get(), ArtificeBlocks.ORCHID.get(), ArtificeBlocks.IRIS.get(), ArtificeBlocks.LOTUS.get());
        RandomSource random = level.getRandom();
        BlockPos start = event.getPos().above();
        tries:
        for (int i = 0; i < 128; i++) {
            BlockPos p = start;
            for (int j = 0; j < i / 16; j++) {
                p = p.offset(random.nextInt(3) - 1, (random.nextInt(3) - 1) * random.nextInt(3) / 2, random.nextInt(3) - 1);
                if (!level.getBlockState(p.below()).is(Blocks.GRASS_BLOCK) || level.getBlockState(p).isCollisionShapeFullBlock(level, p)) {
                    continue tries;
                }
            }
            if (!level.getBlockState(p).isAir() || random.nextInt(10) != 0) {
                continue;
            }
            // WeightedRandom over dandelion 20, rose 10 (left to vanilla) and the four flowers 10 each
            int pick = random.nextInt(70) - 30;
            if (pick < 0) {
                continue;
            }
            BlockState state = flora.get(Mth.clamp(pick / 10, 0, 3)).defaultBlockState();
            if (state.canSurvive(level, p)) {
                level.setBlock(p, state, Block.UPDATE_ALL);
            }
        }
    }
}

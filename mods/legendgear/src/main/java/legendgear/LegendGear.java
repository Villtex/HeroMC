package legendgear;

import hexxit.worldgen152.OldBlockStates;
import hexxit.worldgen152.OldGameIds;
import legendgear.block.BombFlowerBlock;
import legendgear.block.ChainedBlock;
import legendgear.block.MysticShrubBlock;
import legendgear.entity.BombEntity;
import legendgear.gen.LGWorldGen;
import legendgear.recipe.LGRecipes;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;

/**
 * LegendGear (NMcCoy, 1.5.2) for NeoForge: medallions, amulets, the magic mirror, boomerang, hookshot, bombs, quivers,
 * mystic shrubs, falling stars, pedestals and starbeam torches, with the values of the pack's LegendGear.cfg
 * ({@link LGConfig}). World generation (shrub clusters, bomb flowers) in {@link LGWorldGen}, events in {@link LGEvents}.
 * The Thaumcraft aspects of postInit are left out (no Thaumcraft in the pack).
 */
@Mod(LegendGear.MOD_ID)
public final class LegendGear {
    public static final String MOD_ID = "legendgear";
    /** DamageSourceFirestorm("inFire"): bypasses armour, fire, magic. */
    public static final ResourceKey<DamageType> FIRESTORM = ResourceKey.create(Registries.DAMAGE_TYPE, id("firestorm"));

    public LegendGear(IEventBus modBus) {
        LGWorldGen.register();
        LGBlocks.register(modBus);
        LGItems.register(modBus);
        LGEntities.register(modBus);
        LGSounds.register(modBus);
        LGParticles.register(modBus);
        LGRecipes.register(modBus);
        NeoForge.EVENT_BUS.register(LGEvents.class);
        registerOldIds();
        modBus.addListener(FMLCommonSetupEvent.class, event -> event.enqueueWork(LegendGear::dispenser));
        if (FMLEnvironment.getDist() == Dist.CLIENT) {
            legendgear.client.LGClient.register(modBus);
        }
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    private static void registerOldIds() {
        OldBlockStates.registerMeta(LGBlocks.MYSTIC_SHRUB_ID, meta -> LGBlocks.MYSTIC_SHRUB.get().defaultBlockState()
                .setValue(MysticShrubBlock.STAGE, Math.min(meta, 2)));
        OldBlockStates.registerMeta(LGBlocks.BOMB_FLOWER_ID, meta -> LGBlocks.BOMB_FLOWER.get().defaultBlockState()
                .setValue(BombFlowerBlock.CUT, meta == 1));
        OldBlockStates.registerMeta(LGBlocks.CHAINED_ID, meta -> LGBlocks.CHAINED.get().defaultBlockState().setValue(ChainedBlock.KIND, Math.min(meta, 4)));
        OldBlockStates.register(LGBlocks.CALTROPS_ID, () -> LGBlocks.CALTROPS.get().defaultBlockState());
        OldBlockStates.register(LGBlocks.CLAY_JAR_ID, () -> LGBlocks.CLAY_JAR.get().defaultBlockState());
        OldBlockStates.register(LGBlocks.SUGAR_CUBE_ID, () -> LGBlocks.SUGAR_CUBE.get().defaultBlockState());
        OldBlockStates.register(LGBlocks.SKYBEAM_ID, () -> LGBlocks.SKYBEAM.get().defaultBlockState());
        OldBlockStates.register(LGBlocks.STARSTONE_ID, () -> LGBlocks.STARSTONE.get().defaultBlockState());
        OldBlockStates.register(LGBlocks.SWORD_PEDESTAL_ID, () -> LGBlocks.SWORD_PEDESTAL.get().defaultBlockState());
        OldBlockStates.registerMeta(LGBlocks.GRIND_NODE_ID, LegendGear::torch);
        for (LGItems.OldId old : LGItems.OLD_IDS) {
            String item = old.item().getId().toString();
            if (old.damage() < 0) {
                OldGameIds.registerItem(old.id(), item);
            } else {
                OldGameIds.registerItem(old.id(), old.damage(), item);
            }
        }
    }

    /** BlockTorch metadata: 1 east-facing wall (on the west block) .. 4, 5 standing. */
    private static BlockState torch(int meta) {
        Direction facing = switch (meta) {
            case 1 -> Direction.EAST;
            case 2 -> Direction.WEST;
            case 3 -> Direction.SOUTH;
            case 4 -> Direction.NORTH;
            default -> null;
        };
        return facing == null ? LGBlocks.STARBEAM_TORCH.get().defaultBlockState()
                : LGBlocks.STARBEAM_WALL_TORCH.get().defaultBlockState().setValue(net.minecraft.world.level.block.WallTorchBlock.FACING, facing);
    }

    /** BlockDispenser.dispenseBehaviorRegistry.putObject(bombItem, bombItem): a bomb shot out with a fuse of 80. */
    private static void dispenser() {
        DispenserBlock.registerBehavior(LGItems.BOMB.get(), new DefaultDispenseItemBehavior() {
            @Override
            protected ItemStack execute(BlockSource source, ItemStack stack) {
                Direction facing = source.state().getValue(DispenserBlock.FACING);
                var pos = DispenserBlock.getDispensePosition(source);
                BombEntity bomb = BombEntity.at(source.level(), pos.x(), pos.y(), pos.z(), 80);
                bomb.heading(facing.getStepX(), 0.1F, facing.getStepZ(), 0.2F, 0.2F);
                source.level().addFreshEntity(bomb);
                stack.shrink(1);
                return stack;
            }
        });
    }
}

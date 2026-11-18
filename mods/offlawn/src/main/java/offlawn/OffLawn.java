package offlawn;

import hexxit.worldgen152.OldBlockStates;
import hexxit.worldgen152.OldGameIds;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import offlawn.block.BeanstalkBlock;
import offlawn.block.LawnBlock;
import offlawn.block.SunflowerBlock;
import offlawn.gen.OffLawnGen;

/**
 * offLawn 1.5.1: lawn (grass that neither spreads nor dies, and stays under trees), sunflowers with edible seeds
 * that grow wild in plains and forests, and beanstalks to climb.
 * <p>
 * The original needed "fake" copies of flowers, tall grass, mushrooms, sugar cane and saplings, because the
 * plants of 1.5.2 only grew on fixed block ids. Today plants ask block tags, so the lawn is simply in the tags
 * of the soils (data/minecraft/tags/block) and the plants are the normal ones, as the fakes dropped and picked.
 */
@Mod(OffLawn.MOD_ID)
public final class OffLawn {
    public static final String MOD_ID = "offlawn";
    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MOD_ID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MOD_ID);

    /** Hardness 0, Material.plants; the original set no step sound (stone) and no creative tab. */
    public static final DeferredBlock<SunflowerBlock> SUNFLOWER = BLOCKS.registerBlock("sunflower", SunflowerBlock::new,
            p -> p.mapColor(MapColor.PLANT).noCollision().instabreak().sound(SoundType.STONE).pushReaction(PushReaction.POPPED));
    /** Hardness 0.4, Material.plants, a ladder. */
    public static final DeferredBlock<BeanstalkBlock> BEANSTALK = BLOCKS.registerBlock("beanstalk", BeanstalkBlock::new,
            p -> p.mapColor(MapColor.PLANT).strength(0.4F).sound(SoundType.STONE).noOcclusion().pushReaction(PushReaction.POPPED));
    /** Hardness 0.6, Material.grass, grass step sound, shovel. */
    public static final DeferredBlock<LawnBlock> LAWN = BLOCKS.registerBlock("lawn", LawnBlock::new,
            p -> p.mapColor(MapColor.GRASS).strength(0.6F).sound(SoundType.GRASS));

    public static final DeferredItem<BlockItem> BEANSTALK_ITEM = ITEMS.registerSimpleBlockItem(BEANSTALK);
    public static final DeferredItem<BlockItem> LAWN_ITEM = ITEMS.registerSimpleBlockItem(LAWN);
    /** ItemFood(2, 0.1F): two hunger points; right-click on the top of a block plants a sunflower. */
    public static final DeferredItem<SunflowerSeedItem> SUNFLOWER_SEED = ITEMS.registerItem("sunflower_seed", SunflowerSeedItem::new,
            p -> p.food(new FoodProperties.Builder().nutrition(2).saturationModifier(0.1F).build()));

    public OffLawn(IEventBus modBus) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        modBus.addListener(OffLawn::tab);
        NeoForge.EVENT_BUS.addListener(OffLawn::breakSpeed);
        OffLawnGen.register();
        // items of the pack ids, for chests that other mods fill (Better Dungeons): beanstalk, lawn, sunflower seed
        OldGameIds.registerItem(OffLawnGen.BEANSTALK, MOD_ID + ":beanstalk");
        OldGameIds.registerItem(OffLawnGen.LAWN, MOD_ID + ":lawn");
        OldGameIds.registerItem(5976 + 256, MOD_ID + ":sunflower_seed");   // I:sunflowerSeed=5976 of OffLawn.cfg
        OldBlockStates.registerMeta(OffLawnGen.SUNFLOWER, meta -> SUNFLOWER.get().defaultBlockState()
                .setValue(SunflowerBlock.HALF, meta == 1 ? DoubleBlockHalf.UPPER : DoubleBlockHalf.LOWER));
        if (FMLEnvironment.getDist() == Dist.CLIENT) {
            offlawn.client.OffLawnClient.register(modBus);
        }
    }

    private static void tab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.NATURAL_BLOCKS) {
            event.accept(BEANSTALK_ITEM.get());
            event.accept(LAWN_ITEM.get());
            event.accept(SUNFLOWER_SEED.get());
        }
    }

    /** BeanstalkShears: shears cut beanstalks ten times as fast. */
    private static void breakSpeed(PlayerEvent.BreakSpeed event) {
        if (event.getState().is(BEANSTALK.get()) && event.getEntity().getMainHandItem().is(Items.SHEARS)) {
            event.setNewSpeed(event.getOriginalSpeed() * 10.0F);
        }
    }
}

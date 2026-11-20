package tconstruct;

import hexxit.worldgen152.OldGameIds;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import tconstruct.network.TNetwork;
import tconstruct.tools.TEvents;

/**
 * Tinkers' Construct 1.4.3 (mDiyo) for NeoForge, built in parts (docs/PLAN.md, Teil 25a-25f). Part 25a: the tool core
 * ({@link TItems}: materials, patterns, parts and tools; {@link TBlocks}: tool station, part builder, pattern chest,
 * stencil table, tool forge, crafting station and slabs). The pack configuration (TinkersWorkshop.txt) is built in.
 */
@Mod(TConstruct.MOD_ID)
public final class TConstruct {
    public static final String MOD_ID = "tconstruct";

    public TConstruct(IEventBus modBus) {
        TItems.register(modBus);
        TBlocks.register(modBus);
        TSmeltery.register(modBus);
        TSounds.register(modBus);
        tconstruct.menu.TMenus.register(modBus);
        TNetwork.register(modBus);
        tconstruct.entity.TEntities.register(modBus);
        tconstruct.recipe.TRecipes.register(modBus);
        TEvents.register();
        registerOldIds();
        if (FMLEnvironment.getDist() == Dist.CLIENT) {
            tconstruct.client.TClient.register(modBus);
        }
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    /** The ids of 1.5.2 (TinkersWorkshop.txt, game id = config id + 256) for chests and templates of the world generation. */
    private static void registerOldIds() {
        for (TItems.OldId old : TItems.OLD_IDS) {
            OldGameIds.registerItem(old.id() + 256, old.damage(), BuiltIn.name(old.item()));
        }
        TBlocks.registerOldIds();
        TSmeltery.registerOldIds();
    }

    /** Registry names without touching the registry (the items are not registered yet in the constructor). */
    static final class BuiltIn {
        static String name(java.util.function.Supplier<? extends net.minecraft.world.item.Item> item) {
            if (item instanceof net.neoforged.neoforge.registries.DeferredHolder<?, ?> holder) {
                return holder.getId().toString();
            }
            return net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(item.get()).toString();
        }
    }
}

package xreliquary;

import hexxit.worldgen152.OldBlockStates;
import hexxit.worldgen152.OldGameIds;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import xreliquary.block.AltarBlock;
import xreliquary.recipe.XRRecipes;

/**
 * Xeno's Reliquary 1.0.6d (1.5.2) for NeoForge: the handgun and its shots, condensed potions, alkahestry, the relics
 * and the Altar of Light, with the ids of the pack's xreliquary.cfg. disableCoinAudio is false in the pack.
 */
@Mod(Reliquary.MOD_ID)
public final class Reliquary {
    public static final String MOD_ID = "xreliquary";

    public Reliquary(IEventBus modBus) {
        XRBlocks.register(modBus);
        XRItems.register(modBus);
        XREntities.register(modBus);
        XRSounds.register(modBus);
        XRRecipes.register(modBus);
        NeoForge.EVENT_BUS.register(XREvents.class);
        registerOldIds();
        if (FMLEnvironment.getDist() == Dist.CLIENT) {
            xreliquary.client.XRClient.register(modBus);
        }
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    private static void registerOldIds() {
        OldBlockStates.register(XRBlocks.ALTAR_ACTIVE_ID, () -> XRBlocks.ALTAR.get().defaultBlockState().setValue(AltarBlock.ACTIVE, true));
        OldBlockStates.register(XRBlocks.ALTAR_IDLE_ID, () -> XRBlocks.ALTAR.get().defaultBlockState());
        OldBlockStates.register(XRBlocks.LILYPAD_ID, () -> XRBlocks.FERTILE_LILYPAD.get().defaultBlockState());
        OldBlockStates.register(XRBlocks.WRAITH_NODE_ID, () -> XRBlocks.WRAITH_NODE.get().defaultBlockState());
        for (XRItems.OldId old : XRItems.OLD_IDS) {
            String item = old.item().getId().toString();
            if (old.damage() < 0) {
                OldGameIds.registerItem(old.id(), item);
            } else {
                OldGameIds.registerItem(old.id(), old.damage(), item);
            }
        }
        OldGameIds.registerItem(XRBlocks.ALTAR_IDLE_ID, XRItems.ALTAR.getId().toString());
        OldGameIds.registerItem(XRBlocks.LILYPAD_ID, XRItems.FERTILE_LILYPAD.getId().toString());
        OldGameIds.registerItem(XRBlocks.WRAITH_NODE_ID, XRItems.WRAITH_NODE.getId().toString());
    }
}

package betterstorage;

import betterstorage.backpack.BackpackEvents;
import betterstorage.network.BSNetwork;
import hexxit.worldgen152.OldGameIds;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;

/**
 * BetterStorage 0.6.2.23 (FML id "BetterStorage") for NeoForge. Settings are those of the pack's BetterStorage.cfg
 * ({@link BSConfig}); the block and item ids of that file are registered for old loot and templates
 * ({@link #registerOldIds}). Everything that is registered lives in {@link BS}.
 */
@Mod(BetterStorage.MOD_ID)
public final class BetterStorage {
    public static final String MOD_ID = "betterstorage";

    public BetterStorage(IEventBus modBus, ModContainer container) {
        container.registerConfig(ModConfig.Type.SYNCED, BSConfig.SPEC, "BetterStorage.toml");
        BS.register(modBus);
        BSNetwork.register(modBus);
        BackpackEvents.register(modBus, NeoForge.EVENT_BUS);
        registerOldIds();
        if (FMLEnvironment.getDist() == Dist.CLIENT) {
            betterstorage.client.BSClient.init(modBus);
        }
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    /**
     * The ids of the pack's BetterStorage.cfg: blocks 1051..1056 (the backpacks are items with their block's id, the
     * reinforced chest has the material id as metadata), items 5426..5428 (ItemBetterStorage subtracted the 256 again)
     * and the enchantments from enchantmentBaseId 180.
     */
    private static void registerOldIds() {
        OldGameIds.registerItem(1051, id("armor_stand").toString());
        OldGameIds.registerItem(1052, id("backpack").toString());
        for (ChestMaterial material : ChestMaterial.values()) {
            OldGameIds.registerItem(1053, material.id, id(material.blockName()).toString());
        }
        OldGameIds.registerItem(1053, id(ChestMaterial.IRON.blockName()).toString());
        OldGameIds.registerItem(1054, id("crate").toString());
        OldGameIds.registerItem(1055, id("ender_backpack").toString());
        OldGameIds.registerItem(1056, id("locker").toString());
        OldGameIds.registerItem(5426, id("key").toString());
        OldGameIds.registerItem(5427, id("keyring").toString());
        OldGameIds.registerItem(5428, id("lock").toString());
        String[] enchantments = {"unlocking", "lockpicking", "morphing", null, "persistance", null, "security", "shock", "trigger"};
        for (int i = 0; i < enchantments.length; i++) {
            if (enchantments[i] != null) {
                OldGameIds.registerEnchantment(180 + i, ResourceKey.create(Registries.ENCHANTMENT, id(enchantments[i])));
            }
        }
        OldGameIds.registerEntity("BetterStorage.Frienderman", id("frienderman").toString());
    }
}

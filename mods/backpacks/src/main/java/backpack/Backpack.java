package backpack;

import backpack.inventory.BackpackMenu;
import backpack.inventory.WorkbenchBackpackMenu;
import backpack.network.BackpackNetwork;
import backpack.recipe.ConfigCondition;
import backpack.recipe.EnhanceBackpackRecipe;
import backpack.recipe.RecolorBackpackRecipe;
import com.mojang.serialization.MapCodec;
import hexxit.worldgen152.OldGameIds;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.minecraft.world.item.equipment.Equippable;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.InterModComms;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.InterModEnqueueEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

/**
 * Backpack 1.12.13 by Eydamos (FML id "Backpack") for NeoForge. Settings: {@link BackpackConfig} (Backpack.cfg).
 *
 * <p>Items (old id + damage): {@code backpack_<colour>} (5401:0-15), {@code backpack} (5401:16), the big ones
 * {@code backpack_<colour>_big} / {@code backpack_big} (5401:32-48), {@code backpack_ender} (5401:31999),
 * {@code backpack_workbench} (5404:18) and {@code backpack_workbench_big} (5404:50), {@code leather_bound} (5402),
 * {@code leather_tanned} (5403).
 */
@Mod(Backpack.MOD_ID)
public final class Backpack {
    public static final String MOD_ID = "backpack";

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MOD_ID);
    private static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, MOD_ID);
    private static final DeferredRegister.DataComponents COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, MOD_ID);
    private static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, MOD_ID);
    private static final DeferredRegister<MapCodec<? extends ICondition>> CONDITIONS = DeferredRegister.create(NeoForgeRegistries.Keys.CONDITION_CODECS,
            MOD_ID);

    /** The model texture of every backpack (model/backpack.png), drawn by the armour layer with the backpack model. */
    public static final ResourceKey<EquipmentAsset> ASSET = ResourceKey.create(EquipmentAssets.ROOT_ID, id("backpack"));

    /** The crafting grid of the workbench backpack ("Crafting" NBT of InventoryWorkbenchBackpack). */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ItemContainerContents>> CRAFTING = COMPONENTS.registerComponentType(
            "crafting", b -> b.persistent(ItemContainerContents.CODEC).networkSynchronized(ItemContainerContents.STREAM_CODEC));

    public static final DeferredHolder<MenuType<?>, MenuType<BackpackMenu>> BACKPACK_MENU = MENUS.register("backpack",
            () -> IMenuTypeExtension.create((id, inv, buf) -> new BackpackMenu(id, inv, buf.readVarInt())));
    public static final DeferredHolder<MenuType<?>, MenuType<WorkbenchBackpackMenu>> WORKBENCH_BACKPACK_MENU = MENUS.register("workbench_backpack",
            () -> IMenuTypeExtension.create((id, inv, buf) -> new WorkbenchBackpackMenu(id, inv, buf.readVarInt())));

    static {
        SERIALIZERS.register("recolor_backpack", () -> RecolorBackpackRecipe.SERIALIZER);
        SERIALIZERS.register("enhance_backpack", () -> EnhanceBackpackRecipe.SERIALIZER);
        CONDITIONS.register("config", () -> ConfigCondition.CODEC);
    }

    /** All backpacks by name, in the order of the creative tab (getSubItems). */
    private static final Map<String, DeferredItem<BackpackItem>> BACKPACKS = new LinkedHashMap<>();

    static {
        for (boolean big : new boolean[]{false, true}) {
            for (int c = 0; c <= 16; c++) {
                backpackItem(BackpackItem.Kind.NORMAL, c, big);
            }
        }
        backpackItem(BackpackItem.Kind.ENDER, -1, false);
        backpackItem(BackpackItem.Kind.WORKBENCH, -1, false);
        backpackItem(BackpackItem.Kind.WORKBENCH, -1, true);
    }

    public static final DeferredItem<Item> BOUND_LEATHER = ITEMS.registerSimpleItem("leather_bound");
    public static final DeferredItem<Item> TANNED_LEATHER = ITEMS.registerSimpleItem("leather_tanned");

    public Backpack(IEventBus modBus, ModContainer container) {
        container.registerConfig(ModConfig.Type.STARTUP, BackpackConfig.SPEC, "Backpack.toml");
        ITEMS.register(modBus);
        MENUS.register(modBus);
        COMPONENTS.register(modBus);
        SERIALIZERS.register(modBus);
        CONDITIONS.register(modBus);
        modBus.addListener(BackpackNetwork::register);
        modBus.addListener(Backpack::creativeTabs);
        modBus.addListener(InterModEnqueueEvent.class, e -> {
            InterModComms.sendTo("inventorytweaks", "chest", () -> "backpack.client.BackpackScreen;9");
        });
        NeoForge.EVENT_BUS.addListener(Backpack::limitBackpacks);
        registerOldIds();
        if (FMLEnvironment.getDist() == Dist.CLIENT) {
            backpack.client.BackpackClient.init(modBus);
        }
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    private static void backpackItem(BackpackItem.Kind kind, int color, boolean big) {
        String name = BackpackItem.name(kind, color, big);
        BACKPACKS.put(name, ITEMS.registerItem(name, p -> new BackpackItem(p, kind, color, big), p -> p.stacksTo(1)
                .component(DataComponents.EQUIPPABLE, Equippable.builder(EquipmentSlot.CHEST).setEquipSound(SoundEvents.ARMOR_EQUIP_LEATHER)
                        .setAsset(ASSET).setSwappable(false).setDamageOnHurt(false).build())
                // the original showed nothing of the contents
                .component(DataComponents.TOOLTIP_DISPLAY, new TooltipDisplay(false, new LinkedHashSet<>(List.of(DataComponents.CONTAINER))))));
    }

    /** The backpack item of a variant. */
    public static BackpackItem backpack(BackpackItem.Kind kind, int color, boolean big) {
        return BACKPACKS.get(BackpackItem.name(kind, color, big)).get();
    }

    public static List<BackpackItem> backpacks() {
        List<BackpackItem> list = new ArrayList<>();
        BACKPACKS.values().forEach(b -> list.add(b.get()));
        return list;
    }

    /** Backpacks in tabMisc (here: tools and utilities), leather in tabMaterials (ingredients). */
    private static void creativeTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            BACKPACKS.values().forEach(b -> event.accept(b.get()));
        } else if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(BOUND_LEATHER.get());
            event.accept(TANNED_LEATHER.get());
        }
    }

    /** ServerTickHandlerBackpack (only with maxBackpackAmount > 0): extra backpacks in the main inventory are dropped. */
    private static void limitBackpacks(PlayerTickEvent.Post event) {
        int max = BackpackConfig.maxBackpackAmount();
        if (max <= 0 || !(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        var inventory = player.getInventory();
        int counter = 0;
        for (int i = 0; i < 36; i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack.getItem() instanceof BackpackItem && ++counter > max) {
                player.drop(stack.copy(), false, net.minecraft.util.Prediction.SERVER_ONLY);
                inventory.setItem(i, ItemStack.EMPTY);
            }
        }
        counter -= max;
        if (counter > 0) {
            player.sendSystemMessage(Component.translatable("message.backpack.limit", max));
            player.sendSystemMessage(Component.translatable("message.backpack.removed", counter));
        }
    }

    /** The item ids of the pack's Backpack.cfg (shifted by 256) with their damage values, for old loot and templates. */
    private static void registerOldIds() {
        for (int c = 0; c <= 16; c++) {
            OldGameIds.registerItem(5401 + 256, c, id(BackpackItem.name(BackpackItem.Kind.NORMAL, c, false)).toString());
            OldGameIds.registerItem(5401 + 256, c + 32, id(BackpackItem.name(BackpackItem.Kind.NORMAL, c, true)).toString());
        }
        OldGameIds.registerItem(5401 + 256, 31999, id("backpack_ender").toString());
        OldGameIds.registerItem(5404 + 256, 18, id("backpack_workbench").toString());
        OldGameIds.registerItem(5404 + 256, 50, id("backpack_workbench_big").toString());
        OldGameIds.registerItem(5402 + 256, id("leather_bound").toString());
        OldGameIds.registerItem(5403 + 256, id("leather_tanned").toString());
    }
}

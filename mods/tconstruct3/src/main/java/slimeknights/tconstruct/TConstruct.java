package slimeknights.tconstruct;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import slimeknights.mantle.util.TranslationHelper;
import java.util.Locale;
import java.util.Random;
import net.neoforged.bus.api.IEventBus;
import net.minecraft.world.entity.projectile.Projectile;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import slimeknights.tconstruct.common.TinkerModule;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.common.config.Config;
import slimeknights.tconstruct.common.network.TinkerNetwork;
import slimeknights.tconstruct.common.recipe.RecipeCacheInvalidator;
import slimeknights.tconstruct.library.client.materials.MaterialTooltipCache;
import slimeknights.tconstruct.library.json.predicate.tool.ToolStackItemPredicate;
import slimeknights.tconstruct.library.materials.MaterialRegistry;
import slimeknights.tconstruct.library.modifiers.ModifierManager;
import slimeknights.tconstruct.library.tools.capability.BlockItemProviderCapability;
import slimeknights.tconstruct.library.tools.capability.EntityModifierCapability;
import slimeknights.tconstruct.library.tools.capability.PersistentDataCapability;
import slimeknights.tconstruct.library.tools.capability.TinkerDataCapability;
import slimeknights.tconstruct.library.tools.definition.ToolDefinitionLoader;
import slimeknights.tconstruct.library.tools.helper.ModifierLootingHandler;
import slimeknights.tconstruct.library.tools.layout.StationSlotLayoutLoader;
import slimeknights.tconstruct.library.tools.nbt.ToolDataComponents;
import slimeknights.tconstruct.library.utils.BlockSideHitListener;
import slimeknights.tconstruct.library.utils.SlimeBounceHandler;
import slimeknights.tconstruct.shared.TinkerAttributes;
import slimeknights.tconstruct.shared.TinkerCommons;
import slimeknights.tconstruct.tools.TinkerToolParts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tinkers' Construct 3.12.1 for the world type Hexxit Integrated (Teil 27). Registered under the mod ID
 * {@code tconstruct3}: Hexxit Legacy keeps Tinkers' Construct 1.4.3 under {@code tconstruct}, both are installed,
 * the world type decides which one is active (data with the load condition {@code worldgen152:world_type},
 * code through {@code hexxit.worldgen152.HexxitWorldType}).
 *
 * <p>Teil 27a: the mod, its assets (from the release tag, namespace renamed at build time) and the converted data
 * (tools/convert_data.py, switched on per folder in data-enabled.txt once the code that reads it exists).
 * Teil 27d: the tool library (materials, modifier core, tool data as components, definitions, layouts) and the tool parts.
 */
@Mod(TConstruct.MOD_ID)
public final class TConstruct {
    public static final String MOD_ID = "tconstruct3";
    public static final Logger LOG = LoggerFactory.getLogger("TConstruct3");
    public static final Random RANDOM = new Random();

    public TConstruct(IEventBus modBus, ModContainer container) {
        Config.init(container);
        MaterialRegistry.init();

        // modules (Teil 27d: only the tool parts; 1.20.1 registered all Tinker* modules here)
        new TinkerCommons(modBus);
        new TinkerToolParts();
        new slimeknights.tconstruct.tools.TinkerModifiers(modBus);
        // Teil 27f: recipe serializers of the tables and the smeltery (placeholders until 27g/27i)
        slimeknights.tconstruct.tables.TinkerTables.init();
        slimeknights.tconstruct.smeltery.TinkerSmeltery.init();
        slimeknights.tconstruct.library.recipe.ingredient.TinkerIngredients.init(modBus);
        // Teil 27e: modifier library (1.20.1 TinkerModifiers constructor and TinkerTools#commonSetup)
        slimeknights.tconstruct.library.modifiers.util.DynamicModifier.init();
        slimeknights.tconstruct.library.modifiers.fluid.FluidEffectManager.INSTANCE.init();
        slimeknights.tconstruct.library.tools.capability.ToolCapabilityProvider.register(slimeknights.tconstruct.library.tools.capability.fluid.ToolFluidCapability.Provider::new);
        slimeknights.tconstruct.library.tools.capability.ToolCapabilityProvider.register(slimeknights.tconstruct.library.tools.capability.inventory.ToolInventoryCapability.Provider::new);
        slimeknights.tconstruct.library.tools.capability.ToolCapabilityProvider.register((stack, tool) -> new slimeknights.tconstruct.library.tools.capability.ToolEnergyCapability.Provider(tool));
        slimeknights.tconstruct.library.tools.capability.ToolCapabilityProvider.registerCapability(net.neoforged.neoforge.capabilities.Capabilities.Fluid.ITEM);
        slimeknights.tconstruct.library.tools.capability.ToolCapabilityProvider.registerCapability(net.neoforged.neoforge.capabilities.Capabilities.Item.ITEM);
        slimeknights.tconstruct.library.tools.capability.ToolCapabilityProvider.registerCapability(net.neoforged.neoforge.capabilities.Capabilities.Energy.ITEM);
        modBus.register(new TinkerAttributes(modBus));
        modBus.addListener(net.neoforged.neoforge.registries.RegisterEvent.class, slimeknights.tconstruct.tools.TinkerTools::registerSerializers);
        modBus.addListener(net.neoforged.neoforge.registries.RegisterEvent.class, TinkerCommons::registerSerializers);
        slimeknights.tconstruct.library.materials.RandomMaterial.init();
        modBus.addListener(FMLCommonSetupEvent.class, event -> {
            slimeknights.tconstruct.library.modifiers.ModifierHooks.init();
            slimeknights.tconstruct.library.tools.definition.module.ToolHooks.init();
        });

        // init deferred registers
        TinkerModule.initRegisters(modBus);
        TinkerNetwork.setup();
        TinkerTags.init();

        // tool library (1.20.1 in TinkerModifiers, TinkerTools and TinkerCommons, which come in 27e-27h)
        ToolDataComponents.register(modBus);
        TinkerDataCapability.register(modBus);
        PersistentDataCapability.register();
        EntityModifierCapability.registerEntityPredicate(entity -> entity instanceof Projectile);
        BlockItemProviderCapability.register(modBus);
        ToolStackItemPredicate.register(modBus);
        ModifierManager.INSTANCE.init(modBus);
        NeoForge.EVENT_BUS.addListener(AddServerReloadListenersEvent.class, RecipeCacheInvalidator::onReloadListenerReload);
        SlimeBounceHandler.init();
        BlockSideHitListener.init();
        ModifierLootingHandler.init();
        modBus.addListener(FMLCommonSetupEvent.class, event -> {
            ToolDefinitionLoader.init();
            StationSlotLayoutLoader.init();
        });
        if (FMLEnvironment.getDist() == Dist.CLIENT) {
            modBus.addListener(AddClientReloadListenersEvent.class, MaterialTooltipCache::init);
            slimeknights.tconstruct.library.client.LibraryClientEvents.init(modBus);
        }
    }

    /** Gets a data key for the capability, mainly used for modifier markers */
    public static <T> TinkerDataCapability.TinkerDataKey<T> createKey(String name) {
        return TinkerDataCapability.TinkerDataKey.of(getResource(name));
    }

    /** Gets a data key for the capability that can compute a missing value */
    public static <T> TinkerDataCapability.ComputableDataKey<T> createKey(String name, java.util.function.Supplier<T> constructor) {
        return TinkerDataCapability.ComputableDataKey.of(getResource(name), constructor);
    }

    /** An identifier in the namespace of the mod. */
    public static Identifier getResource(String name) {
        return Identifier.fromNamespaceAndPath(MOD_ID, name);
    }

    /** Returns the given resource prefixed with the Tinkers namespace */
    public static String resourceString(String res) {
        return MOD_ID + ':' + res;
    }

    /** Prefixes the given unlocalized name with the Tinkers prefix */
    public static String prefix(String name) {
        return MOD_ID + "." + name.toLowerCase(Locale.US);
    }

    /** Makes a Tinker's description ID */
    public static String makeDescriptionId(String type, String name) {
        return type + "." + MOD_ID + "." + name;
    }

    /** Makes a translation key for the given name, such as {@code gui.tconstruct3.name} */
    public static String makeTranslationKey(String base, String name) {
        return net.minecraft.util.Util.makeDescriptionId(base, getResource(name));
    }

    /** Makes a translation text component for the given name */
    public static MutableComponent makeTranslation(String base, String name) {
        return Component.translatable(makeTranslationKey(base, name));
    }

    /** Makes a translation text component; arguments other than text, numbers and booleans become strings (26.3 rejects them) */
    public static MutableComponent makeTranslation(String base, String name, Object... arguments) {
        Object[] args = new Object[arguments.length];
        for (int i = 0; i < arguments.length; i++) {
            args[i] = TranslationHelper.arg(arguments[i]);
        }
        return Component.translatable(makeTranslationKey(base, name), args);
    }

    /** Called in the constructor of internal classes that addons should not extend */
    public static void sealTinkersClass(Object self, String base, String solution) {
        String name = self.getClass().getName();
        if (!name.startsWith("slimeknights.tconstruct.")) {
            throw new IllegalStateException(base + " being extended from invalid package " + name + ". " + solution);
        }
    }
}

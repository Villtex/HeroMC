package asgardshield;

import asgardshield.guard.GuardEvents;
import asgardshield.guard.GuardState;
import asgardshield.item.ASItems;
import asgardshield.network.GuardStatePayload;
import asgardshield.recipe.GildShieldRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/**
 * AsgardShield 2.0.4 (Hexxit 1.0.11) for NeoForge, with its colour API (ASAddon_ColorAPI): shields and giant swords
 * of wood, stone, iron, diamond (gold for the sword) and their guarding mechanics. The three addons of the pack are
 * the mod asaddons (mods/asgardshield-addons); they build their items with {@link ASItems}.
 */
@Mod(AsgardShield.MOD_ID)
public final class AsgardShield {
    public static final String MOD_ID = "asgardshield_core";

    private static final DeferredRegister<AttachmentType<?>> ATTACHMENTS = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, MOD_ID);
    private static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, MOD_ID);

    /** The guard state of a player (not saved). */
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<GuardState>> GUARD = ATTACHMENTS.register("guard",
            () -> AttachmentType.builder(GuardState::new).build());
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<GildShieldRecipe>> GILD_SHIELD = SERIALIZERS.register("gild_shield",
            () -> GildShieldRecipe.SERIALIZER);

    /** ASAddonConfig.HarkenScytheMod: weapons that do not count as swords for the switch between sword and shield. */
    public static final TagKey<Item> HARKENSCYTHE_WEAPONS = ASItems.itemTag(MOD_ID, "harkenscythe_weapons");
    /** Every shield and giant sword (core and addons). */
    public static final TagKey<Item> SHIELDS = ASItems.itemTag(MOD_ID, "shields");
    public static final TagKey<Item> GIANT_SWORDS = ASItems.itemTag(MOD_ID, "giant_swords");

    public AsgardShield(IEventBus modBus, ModContainer container) {
        ASItems.ITEMS.register(modBus);
        ATTACHMENTS.register(modBus);
        SERIALIZERS.register(modBus);
        container.registerConfig(ModConfig.Type.CLIENT, ASConfig.SPEC);
        modBus.addListener(RegisterPayloadHandlersEvent.class, event -> event.registrar("1").playToClient(GuardStatePayload.TYPE,
                GuardStatePayload.CODEC, (payload, context) -> context.player().getData(GUARD).apply(payload)));
        modBus.addListener(BuildCreativeModeTabContentsEvent.class, event -> {
            if (event.getTabKey() == CreativeModeTabs.COMBAT) {
                ASItems.TAB.forEach(item -> event.accept(item.get()));
            }
        });
        NeoForge.EVENT_BUS.register(GuardEvents.class);
        if (FMLEnvironment.getDist() == Dist.CLIENT) {
            asgardshield.client.ASClient.register(modBus);
        }
    }
}

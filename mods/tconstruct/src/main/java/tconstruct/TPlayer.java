package tconstruct;

import hexxit.worldgen152.OldGameIds;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import net.minecraft.world.level.block.DispenserBlock;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import tconstruct.player.ArmorExtendedMenu;
import tconstruct.player.KnapsackMenu;
import tconstruct.player.PlayerData;
import tconstruct.player.PlayerEvents;
import tconstruct.player.PlayerItems;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Part 25e, the player of Tinkers' Construct: the extended armor inventory and the knapsack (util/player, items/armor),
 * heart canisters, the manuals, jeweled apple, golden head, essence crystal, the slime spawn eggs (TitleIcon), the
 * packets that open the inventories (TPacketHandler 3), the player rules of the pack and the mob drops
 * ({@link PlayerEvents}).
 */
public final class TPlayer {
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENTS = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES,
            TConstruct.MOD_ID);
    private static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, TConstruct.MOD_ID);
    private static final DeferredRegister.DataComponents COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE,
            TConstruct.MOD_ID);

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<PlayerData>> DATA = ATTACHMENTS.register("player",
            () -> AttachmentType.builder(PlayerData::new).serialize(PlayerData.CODEC).copyOnDeath()
                    .copyHandler((data, holder, provider) -> data.copy()).build());

    /** The NBT "Essence" of the essence crystal. */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> ESSENCE = COMPONENTS.registerComponentType("essence",
            b -> b.persistent(com.mojang.serialization.Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT));

    public static final DeferredHolder<MenuType<?>, MenuType<ArmorExtendedMenu>> ARMOR_MENU = MENUS.register("armor_extended",
            () -> IMenuTypeExtension.create((id, inv, buf) -> new ArmorExtendedMenu(id, inv)));
    public static final DeferredHolder<MenuType<?>, MenuType<KnapsackMenu>> KNAPSACK_MENU = MENUS.register("knapsack",
            () -> IMenuTypeExtension.create((id, inv, buf) -> new KnapsackMenu(id, inv)));

    // ------------------------------------------------------------------ items

    /** HeartCanister 0 (crafting item); 1 is TWorld.MINIATURE_RED_HEART; 2 the canister that gives health. */
    public static final DeferredItem<Item> EMPTY_CANISTER = TItems.ITEMS.registerItem("empty_canister",
            p -> new PlayerItems.TipItem("item.tconstruct.empty_canister.tip", 1, p), p -> p.stacksTo(10));
    public static final DeferredItem<PlayerItems.HeartCanister> RED_HEART_CANISTER = TItems.ITEMS.registerItem("red_heart_canister",
            PlayerItems.HeartCanister::new, p -> p.stacksTo(10));
    /** Knapsack 0: opens the knapsack tab when it lies in slot 2 of the extended armor. */
    public static final DeferredItem<Item> KNAPSACK = TItems.ITEMS.registerItem("knapsack",
            p -> new PlayerItems.TipItem("item.tconstruct.knapsack.tip", 1, p), p -> p.stacksTo(10));
    /** Manual 0 (Materials and You: Volume 1), 1 (Volume 2), 2 (Mighty Smelting). */
    public static final String[] MANUAL_NAMES = {"beginner_manual", "materials_manual", "smeltery_manual"};
    public static final List<DeferredItem<PlayerItems.Manual>> MANUALS = new ArrayList<>();

    static {
        for (int i = 0; i < MANUAL_NAMES.length; i++) {
            int volume = i;
            MANUALS.add(TItems.ITEMS.registerItem(MANUAL_NAMES[i], p -> new PlayerItems.Manual(volume, p)));
        }
    }

    public static final DeferredItem<PlayerItems.JeweledApple> JEWELED_APPLE = TItems.ITEMS.registerItem("jeweled_apple",
            PlayerItems.JeweledApple::new, p -> p.food(new FoodProperties.Builder().nutrition(4).saturationModifier(2.0F).alwaysEdible().build()));
    /** GoldenHead(4, 1.2F): always edible, regeneration for 10 seconds; damage 1 (glowing) had no recipe and is left out. */
    public static final DeferredItem<Item> GOLDEN_HEAD = TItems.ITEMS.registerItem("golden_head",
            p -> new PlayerItems.TipItem("item.tconstruct.golden_head.tip", 2, p, net.minecraft.ChatFormatting.DARK_PURPLE,
                    net.minecraft.ChatFormatting.ITALIC),
            p -> p.rarity(Rarity.RARE).food(new FoodProperties.Builder().nutrition(4).saturationModifier(1.2F).alwaysEdible().build(),
                    Consumables.defaultFood().onConsume(new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 0)))
                            .build()));
    public static final DeferredItem<PlayerItems.EssenceCrystal> ESSENCE_CRYSTAL = TItems.ITEMS.registerItem("essence_crystal",
            PlayerItems.EssenceCrystal::new);
    /** TitleIcon 0 (EdibleSlime) and 1 (KingSlime). */
    public static final DeferredItem<PlayerItems.SlimeEgg> BLUE_SLIME_SPAWN_EGG = TItems.ITEMS.registerItem("blue_slime_spawn_egg",
            p -> new PlayerItems.SlimeEgg(false, p));
    public static final DeferredItem<PlayerItems.SlimeEgg> KING_SLIME_SPAWN_EGG = TItems.ITEMS.registerItem("king_slime_spawn_egg",
            p -> new PlayerItems.SlimeEgg(true, p));

    /** The tables of toolStationWood (TCraftingHandler: materials manual) and the smeltery blocks and tanks (smeltery manual). */
    public static final Set<String> TABLE_ITEMS = new HashSet<>();
    public static final Set<String> SMELTERY_ITEMS = new HashSet<>();

    // ------------------------------------------------------------------ packets

    /** TPacketHandler 3: open 0 = the vanilla inventory (client only), 1 = the extended armor, 2 = the knapsack. */
    public record OpenPayload(int which) implements CustomPacketPayload {
        public static final Type<OpenPayload> TYPE = new Type<>(TConstruct.id("open_inventory"));
        public static final StreamCodec<ByteBuf, OpenPayload> CODEC = ByteBufCodecs.VAR_INT.map(OpenPayload::new, OpenPayload::which);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    /** To the client: whether slot 2 of the extended armor holds the knapsack (the client copy of ArmorExtended in 1.5.2). */
    public record StatePayload(boolean knapsack) implements CustomPacketPayload {
        public static final Type<StatePayload> TYPE = new Type<>(TConstruct.id("player_state"));
        public static final StreamCodec<ByteBuf, StatePayload> CODEC = ByteBufCodecs.BOOL.map(StatePayload::new, StatePayload::knapsack);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    /** The client's knapsack state, set by the client handler. */
    public static boolean clientKnapsack;

    private TPlayer() {
    }

    public static PlayerData data(Player player) {
        PlayerData data = player.getData(DATA.get());
        if (player instanceof ServerPlayer serverPlayer) {
            data.onArmorChanged = () -> {
                PlayerEvents.recalculateHealth(serverPlayer);
                sendState(serverPlayer);
            };
        }
        return data;
    }

    public static void sendState(ServerPlayer player) {
        boolean knapsack = player.getData(DATA.get()).armor.getItem(PlayerData.KNAPSACK_SLOT).is(KNAPSACK.get());
        PacketDistributor.sendToPlayer(player, new StatePayload(knapsack));
    }

    private static void open(OpenPayload payload, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player)) {
            return;
        }
        PlayerData data = data(player);
        if (payload.which() == 1) {
            player.openMenu(new SimpleMenuProvider((id, inv, p) -> new ArmorExtendedMenu(id, inv, data.armor), Component.empty()));
        } else if (payload.which() == 2 && data.armor.getItem(PlayerData.KNAPSACK_SLOT).is(KNAPSACK.get())) {
            player.openMenu(new SimpleMenuProvider((id, inv, p) -> new KnapsackMenu(id, inv, data.knapsack),
                    Component.translatable("inventory.knapsack")));
        } else if (payload.which() == 0) {
            player.doCloseContainer();
        }
    }

    public static void register(IEventBus modBus) {
        ATTACHMENTS.register(modBus);
        MENUS.register(modBus);
        COMPONENTS.register(modBus);
        modBus.addListener(RegisterPayloadHandlersEvent.class, event -> event.registrar("1")
                .playToServer(OpenPayload.TYPE, OpenPayload.CODEC, TPlayer::open)
                .playToClient(StatePayload.TYPE, StatePayload.CODEC, (payload, context) -> clientKnapsack = payload.knapsack()));
        modBus.addListener(ModifyDefaultComponentsEvent.class, TPlayer::stackSizes);
        modBus.addListener(BuildCreativeModeTabContentsEvent.class, TPlayer::tabs);
        modBus.addListener(FMLCommonSetupEvent.class, event -> event.enqueueWork(TPlayer::setup));
        PlayerEvents.register();
    }

    /** TContent.registerItems: the stack sizes TConstruct changed on vanilla items. */
    private static void stackSizes(ModifyDefaultComponentsEvent event) {
        for (Item item : List.of(Items.OAK_DOOR, Items.IRON_DOOR, Items.OAK_BOAT, Items.CAKE)) {
            event.modify(item, (b, c, i) -> b.set(DataComponents.MAX_STACK_SIZE, 16));
        }
        event.modify(Items.SNOWBALL, (b, c, i) -> b.set(DataComponents.MAX_STACK_SIZE, 64));
        for (Item item : List.of(Items.MINECART, Items.CHEST_MINECART, Items.FURNACE_MINECART)) {
            event.modify(item, (b, c, i) -> b.set(DataComponents.MAX_STACK_SIZE, 3));
        }
    }

    private static void tabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == TItems.MATERIAL_TAB.getKey()) {
            event.accept(EMPTY_CANISTER.get());
            event.accept(tconstruct.TWorld.MINIATURE_RED_HEART.get());
            event.accept(RED_HEART_CANISTER.get());
            event.accept(KNAPSACK.get());
            MANUALS.forEach(m -> event.accept(m.get()));
        } else if (event.getTabKey() == CreativeModeTabs.FOOD_AND_DRINKS) {
            event.accept(JEWELED_APPLE.get());
            event.accept(GOLDEN_HEAD.get());
        } else if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            for (int essence : PlayerItems.EssenceCrystal.CREATIVE) {
                ItemStack crystal = new ItemStack(ESSENCE_CRYSTAL.get());
                crystal.set(ESSENCE.get(), essence);
                event.accept(crystal);
            }
        } else if (event.getTabKey() == CreativeModeTabs.SPAWN_EGGS) {
            event.accept(BLUE_SLIME_SPAWN_EGG.get());
            event.accept(KING_SLIME_SPAWN_EGG.get());
        }
    }

    private static void setup() {
        TABLE_ITEMS.add(TBlocks.TOOL_STATION.getId().toString());
        TABLE_ITEMS.add(TBlocks.PATTERN_CHEST.getId().toString());
        TBlocks.PART_BUILDERS.forEach(b -> TABLE_ITEMS.add(b.getId().toString()));
        TBlocks.STENCIL_TABLES.forEach(b -> TABLE_ITEMS.add(b.getId().toString()));
        SMELTERY_ITEMS.add(TSmeltery.CONTROLLER.getId().toString());
        SMELTERY_ITEMS.add(TSmeltery.DRAIN.getId().toString());
        TSmeltery.SEARED.forEach(b -> SMELTERY_ITEMS.add(b.getId().toString()));
        TSmeltery.TANKS.forEach(b -> SMELTERY_ITEMS.add(b.getId().toString()));
        for (DeferredItem<PlayerItems.SlimeEgg> egg : List.of(BLUE_SLIME_SPAWN_EGG, KING_SLIME_SPAWN_EGG)) {
            boolean king = egg == KING_SLIME_SPAWN_EGG;
            DispenserBlock.registerBehavior(egg.get(), new DefaultDispenseItemBehavior() {
                @Override
                protected ItemStack execute(BlockSource source, ItemStack stack) {
                    Direction facing = source.state().getValue(DispenserBlock.FACING);
                    BlockPos pos = source.pos();
                    PlayerItems.SlimeEgg.dispense(source.level(), pos, facing, king);
                    stack.shrink(1);
                    return stack;
                }
            });
        }
    }

    static void registerOldIds() {
        String ns = TConstruct.MOD_ID + ":";
        OldGameIds.registerItem(6027 + 256, 0, ns + "empty_canister");
        OldGameIds.registerItem(6027 + 256, 2, ns + "red_heart_canister");
        OldGameIds.registerItem(6074 + 256, 0, ns + "knapsack");
        for (int i = 0; i < MANUAL_NAMES.length; i++) {
            OldGameIds.registerItem(6039 + 256, i, ns + MANUAL_NAMES[i]);
        }
        OldGameIds.registerItem(6034 + 256, 0, ns + "jeweled_apple");
        OldGameIds.registerItem(6075 + 256, 0, ns + "golden_head");
        OldGameIds.registerItem(6094 + 256, 0, ns + "essence_crystal");
        OldGameIds.registerItem(6040 + 256, 0, ns + "blue_slime_spawn_egg");
        OldGameIds.registerItem(6040 + 256, 1, ns + "king_slime_spawn_egg");
    }
}

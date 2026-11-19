package betterstorage;

import betterstorage.backpack.BackpackData;
import betterstorage.backpack.BackpackState;
import betterstorage.block.ArmorStandBlock;
import betterstorage.block.ArmorStandBlockEntity;
import betterstorage.block.BackpackBlock;
import betterstorage.block.BackpackBlockEntity;
import betterstorage.block.LockerBlock;
import betterstorage.block.LockerBlockEntity;
import betterstorage.block.ReinforcedChestBlock;
import betterstorage.block.ReinforcedChestBlockEntity;
import betterstorage.crate.CrateBlock;
import betterstorage.crate.CrateBlockEntity;
import betterstorage.entity.Frienderman;
import betterstorage.item.BackpackItem;
import betterstorage.item.KeyItem;
import betterstorage.item.KeyringItem;
import betterstorage.item.LockItem;
import betterstorage.menu.BSMenu;
import betterstorage.menu.CrateMenu;
import betterstorage.menu.KeyringMenu;
import betterstorage.recipe.KeyRecipe;
import betterstorage.recipe.LockRecipe;
import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/** Everything BetterStorage registers (Registry.doYourThing, CommonProxy.registerTileEntites/registerEntities). */
public final class BS {
    private static final String MOD_ID = BetterStorage.MOD_ID;
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MOD_ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MOD_ID);
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, MOD_ID);
    public static final DeferredRegister.Entities ENTITIES = DeferredRegister.createEntities(MOD_ID);
    public static final DeferredRegister.DataComponents COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, MOD_ID);
    public static final DeferredRegister<RecipeSerializer<?>> RECIPES = DeferredRegister.create(Registries.RECIPE_SERIALIZER, MOD_ID);
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, MOD_ID);
    private static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MOD_ID);

    // ---- data components (the NBT and damage values of the original) ----

    /** Key and lock: the damage value of the original, the id that has to match (1..31999, 0 = not set yet). */
    public static final DeferredHolder<net.minecraft.core.component.DataComponentType<?>, net.minecraft.core.component.DataComponentType<Integer>> KEY_ID =
            COMPONENTS.registerComponentType("key_id", b -> b.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT));
    /** Key: display.ironPlated. */
    public static final DeferredHolder<net.minecraft.core.component.DataComponentType<?>, net.minecraft.core.component.DataComponentType<Unit>> IRON_PLATED =
            COMPONENTS.registerComponentType("iron_plated", b -> b.persistent(Unit.CODEC).networkSynchronized(net.minecraft.network.codec.StreamCodec.unit(Unit.INSTANCE)));
    /** Keyring: the damage value of the original, the number of keys on it (picks the icon). */
    public static final DeferredHolder<net.minecraft.core.component.DataComponentType<?>, net.minecraft.core.component.DataComponentType<Integer>> KEYRING_KEYS =
            COMPONENTS.registerComponentType("keyring_keys", b -> b.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT));

    // ---- blocks ----

    public static final DeferredBlock<CrateBlock> CRATE = BLOCKS.registerBlock("crate", CrateBlock::new,
            p -> p.mapColor(MapColor.WOOD).instrument(NoteBlockInstrument.BASS).strength(2.0F).sound(SoundType.WOOD).ignitedByLava());
    public static final Map<ChestMaterial, DeferredBlock<ReinforcedChestBlock>> CHESTS = new EnumMap<>(ChestMaterial.class);
    static {
        for (ChestMaterial material : ChestMaterial.values()) {
            // hardness 8, resistance 20 (getExplosionResistance gave 20 * 3 / 5 = 12)
            CHESTS.put(material, BLOCKS.registerBlock(material.blockName(), p -> new ReinforcedChestBlock(material, p),
                    p -> p.mapColor(MapColor.WOOD).instrument(NoteBlockInstrument.BASS).strength(8.0F, 12.0F).sound(SoundType.WOOD)
                            .noOcclusion().ignitedByLava()));
        }
    }
    public static final DeferredBlock<LockerBlock> LOCKER = BLOCKS.registerBlock("locker", LockerBlock::new,
            p -> p.mapColor(MapColor.WOOD).instrument(NoteBlockInstrument.BASS).strength(2.5F).sound(SoundType.WOOD).noOcclusion().ignitedByLava());
    /** Material.rock: needs a pickaxe for the drop. */
    public static final DeferredBlock<ArmorStandBlock> ARMOR_STAND = BLOCKS.registerBlock("armor_stand", ArmorStandBlock::new,
            p -> p.mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(2.5F)
                    .sound(SoundType.STONE).noOcclusion());
    public static final DeferredBlock<BackpackBlock> BACKPACK_BLOCK = BLOCKS.registerBlock("backpack", p -> new BackpackBlock(false, p),
            p -> p.mapColor(MapColor.COLOR_BROWN).strength(0.7F).sound(SoundType.WOOL).noOcclusion().ignitedByLava());
    public static final DeferredBlock<BackpackBlock> ENDER_BACKPACK_BLOCK = BLOCKS.registerBlock("ender_backpack", p -> new BackpackBlock(true, p),
            p -> p.mapColor(MapColor.COLOR_BROWN).strength(1.5F).sound(SoundType.WOOL).noOcclusion());

    // ---- block entities ----

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CrateBlockEntity>> CRATE_ENTITY = BLOCK_ENTITIES.register("crate",
            () -> new BlockEntityType<>(CrateBlockEntity::new, CRATE.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ReinforcedChestBlockEntity>> CHEST_ENTITY = BLOCK_ENTITIES.register(
            "reinforced_chest", () -> new BlockEntityType<>(ReinforcedChestBlockEntity::new,
                    CHESTS.values().stream().map(DeferredBlock::get).toArray(ReinforcedChestBlock[]::new)));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<LockerBlockEntity>> LOCKER_ENTITY = BLOCK_ENTITIES.register("locker",
            () -> new BlockEntityType<>(LockerBlockEntity::new, LOCKER.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ArmorStandBlockEntity>> ARMOR_STAND_ENTITY = BLOCK_ENTITIES.register(
            "armor_stand", () -> new BlockEntityType<>(ArmorStandBlockEntity::new, ARMOR_STAND.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BackpackBlockEntity>> BACKPACK_ENTITY = BLOCK_ENTITIES.register("backpack",
            () -> new BlockEntityType<>(BackpackBlockEntity::new, BACKPACK_BLOCK.get(), ENDER_BACKPACK_BLOCK.get()));

    // ---- items (in the order of their ids: the creative tab lists them so) ----

    public static final DeferredItem<BlockItem> ARMOR_STAND_ITEM = ITEMS.registerSimpleBlockItem(ARMOR_STAND, p -> p.stacksTo(1));
    /** ItemArmor CLOTH: dyeable, layer 1 tinted (leather brown when undyed), repaired with leather. */
    public static final ResourceKey<EquipmentAsset> BACKPACK_ASSET = ResourceKey.create(EquipmentAssets.ROOT_ID, BetterStorage.id("backpack"));
    public static final ResourceKey<EquipmentAsset> ENDER_BACKPACK_ASSET = ResourceKey.create(EquipmentAssets.ROOT_ID, BetterStorage.id("ender_backpack"));
    public static final TagKey<Item> REPAIRS_BACKPACK = TagKey.create(Registries.ITEM, BetterStorage.id("repairs_backpack"));
    public static final TagKey<Item> REPAIRS_ENDER_BACKPACK = TagKey.create(Registries.ITEM, BetterStorage.id("repairs_ender_backpack"));
    public static final DeferredItem<BackpackItem> BACKPACK = ITEMS.registerItem("backpack", p -> new BackpackItem(false, p),
            p -> backpack(p, BACKPACK_ASSET, 240, REPAIRS_BACKPACK));
    public static final List<DeferredItem<BlockItem>> CHEST_ITEMS = new ArrayList<>();
    static {
        for (ChestMaterial material : ChestMaterial.values()) {
            CHEST_ITEMS.add(ITEMS.registerSimpleBlockItem(CHESTS.get(material)));
        }
    }
    public static final DeferredItem<BlockItem> CRATE_ITEM = ITEMS.registerSimpleBlockItem(CRATE);
    public static final DeferredItem<BackpackItem> ENDER_BACKPACK = ITEMS.registerItem("ender_backpack", p -> new BackpackItem(true, p),
            p -> backpack(p, ENDER_BACKPACK_ASSET, 380, REPAIRS_ENDER_BACKPACK));
    public static final DeferredItem<BlockItem> LOCKER_ITEM = ITEMS.registerSimpleBlockItem(LOCKER);
    /** ItemBetterStorage: stack size 1; keys and locks have enchantability 20. */
    public static final DeferredItem<KeyItem> KEY = ITEMS.registerItem("key", KeyItem::new, p -> p.stacksTo(1).enchantable(20));
    public static final DeferredItem<KeyringItem> KEYRING = ITEMS.registerItem("keyring", KeyringItem::new, p -> p.stacksTo(1));
    public static final DeferredItem<LockItem> LOCK = ITEMS.registerItem("lock", LockItem::new, p -> p.stacksTo(1).enchantable(20));

    /**
     * ItemBackpack: an armor piece for the chest (2 armor points: ISpecialArmor absorbed 8 %, the same as two points
     * now), enchantability 15. It can not be put on by hand (isValidArmor false): no swapping, no dispensers, and
     * {@link BackpackItem#canEquip} keeps it out of the armor slot. The durability goes down in
     * {@link betterstorage.backpack.BackpackEvents} (damageArmor), not with every hit.
     */
    private static Item.Properties backpack(Item.Properties p, ResourceKey<EquipmentAsset> asset, int durability, TagKey<Item> repair) {
        Equippable equippable = Equippable.builder(EquipmentSlot.CHEST).setEquipSound(SoundEvents.ARMOR_EQUIP_LEATHER).setAsset(asset)
                .setSwappable(false).setDispensable(false).setDamageOnHurt(false).setEquipOnInteract(false).build();
        ItemAttributeModifiers attributes = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR, new AttributeModifier(BetterStorage.id("armor.backpack"), 2.0, AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.CHEST)
                .build();
        return p.stacksTo(1).durability(durability).component(net.minecraft.core.component.DataComponents.EQUIPPABLE, equippable)
                .attributes(attributes).enchantable(15).repairable(repair);
    }

    // ---- menus ----

    public static final DeferredHolder<MenuType<?>, MenuType<BSMenu>> MENU = MENUS.register("container",
            () -> IMenuTypeExtension.create(BSMenu::client));
    public static final DeferredHolder<MenuType<?>, MenuType<CrateMenu>> CRATE_MENU = MENUS.register("crate",
            () -> IMenuTypeExtension.create(CrateMenu::client));
    public static final DeferredHolder<MenuType<?>, MenuType<KeyringMenu>> KEYRING_MENU = MENUS.register("keyring",
            () -> IMenuTypeExtension.create(KeyringMenu::client));

    // ---- entities ----

    /** registerModEntity(EntityFrienderman, "Frienderman", 1, 64, 4, true): an enderman (0.6 x 2.9). */
    public static final DeferredHolder<EntityType<?>, EntityType<Frienderman>> FRIENDERMAN = ENTITIES.registerEntityType("frienderman",
            Frienderman::new, MobCategory.MONSTER, b -> b.sized(0.6F, 2.9F).eyeHeight(2.55F).passengerAttachments(2.80625F)
                    .clientTrackingRange(4).updateInterval(4));

    // ---- recipes ----

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<KeyRecipe>> KEY_RECIPE = RECIPES.register("key",
            () -> KeyRecipe.SERIALIZER);
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<LockRecipe>> LOCK_RECIPE = RECIPES.register("lock",
            () -> LockRecipe.SERIALIZER);

    // ---- backpack data of entities (IExtendedEntityProperties "BetterStorage.Backpack" and the data watcher) ----

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<BackpackData>> BACKPACK_DATA = ATTACHMENTS.register("backpack",
            () -> AttachmentType.builder(BackpackData::new).serialize(BackpackData.CODEC, BackpackData::shouldSave).build());
    /** Open (lid) and has-items flags, sent to every client that sees the entity. */
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<BackpackState>> BACKPACK_STATE = ATTACHMENTS.register("backpack_state",
            () -> AttachmentType.builder(() -> BackpackState.CLOSED).sync(BackpackState.STREAM_CODEC).build());

    // ---- creative tab ----

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TAB = TABS.register("betterstorage", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.betterstorage"))
            .icon(() -> new ItemStack(CRATE_ITEM.get()))
            .displayItems((params, output) -> {
                ITEMS.getEntries().forEach(item -> output.accept(item.get()));
                // displayAllReleventItems: an enchanted book for every key and lock enchantment at its highest level
                BSEnchantments.books(params.holders(), output::accept);
            })
            .build());

    private BS() {
    }

    static void register(IEventBus modBus) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        BLOCK_ENTITIES.register(modBus);
        MENUS.register(modBus);
        ENTITIES.register(modBus);
        COMPONENTS.register(modBus);
        RECIPES.register(modBus);
        ATTACHMENTS.register(modBus);
        TABS.register(modBus);
    }

    public static Holder<net.minecraft.world.item.Item> holder(DeferredItem<?> item) {
        return item;
    }
}

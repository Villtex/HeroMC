package shadwdrgn.soulshards;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Soul Shards 1.0.36: killing mobs with a blank soul shard in the hotbar binds the shard to that kind of mob and
 * counts the kills; from 64 kills on (tier 1) the shard makes a soul cage spawn that mob. Blank shards come from
 * the soul rune (glowstone in a ring of netherrack and end stone, right-clicked with a diamond). Also the Soul
 * Stealer enchantment (more souls per kill) and the shadow blade and pickaxe, which the original registered
 * without a recipe.
 */
@Mod(SoulShards.MOD_ID)
public final class SoulShards {
    public static final String MOD_ID = "soulshards";
    /** between_Tiers of the pack config: souls a spawner adds to a shard. */
    public static final int SPAWNER_SOULS = 200;
    public static final int MAX_SOULS = 1024;

    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MOD_ID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MOD_ID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MOD_ID);
    private static final DeferredRegister.DataComponents COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, MOD_ID);

    /** Kind of mob ("" while unbound) and number of souls of a shard (NBT "mobtype" and "charge" of 1.5.2). */
    public record Soul(String type, int charge) {
        public static final Soul EMPTY = new Soul("", 0);
        public static final Codec<Soul> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.STRING.optionalFieldOf("type", "").forGetter(Soul::type),
                Codec.INT.optionalFieldOf("charge", 0).forGetter(Soul::charge)).apply(i, Soul::new));
        public static final StreamCodec<io.netty.buffer.ByteBuf, Soul> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.STRING_UTF8, Soul::type, ByteBufCodecs.VAR_INT, Soul::charge, Soul::new);

        /** tier = log2(charge) - 5, at least 0: 64, 128, 256, 512 and 1024 souls are tiers 1 to 5. */
        public int tier() {
            return this.charge <= 0 ? 0 : Math.max(0, (int) (Math.log(this.charge) / Math.log(2.0)) - 5);
        }
    }

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Soul>> SOUL = COMPONENTS.registerComponentType("soul",
            b -> b.persistent(Soul.CODEC).networkSynchronized(Soul.STREAM_CODEC));

    /** Hardness 5, stone step sound at pitch 1.5. */
    public static final DeferredBlock<SoulCageBlock> SOUL_CAGE = BLOCKS.registerBlock("soul_cage", SoulCageBlock::new,
            p -> p.mapColor(MapColor.STONE).strength(5.0F).requiresCorrectToolForDrops().noOcclusion()
                    .sound(new SoundType(1.0F, 1.5F, SoundEvents.STONE_BREAK, SoundEvents.STONE_STEP, SoundEvents.STONE_PLACE,
                            SoundEvents.STONE_HIT, SoundEvents.STONE_FALL)));
    public static final DeferredItem<BlockItem> SOUL_CAGE_ITEM = ITEMS.registerSimpleBlockItem(SOUL_CAGE);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SoulCageBlockEntity>> SOUL_CAGE_ENTITY = BLOCK_ENTITIES.register(
            "soul_cage", () -> new BlockEntityType<>(SoulCageBlockEntity::new, SOUL_CAGE.get()));

    public static final DeferredItem<Item> BLANK_SOUL_SHARD = ITEMS.registerSimpleItem("blank_soul_shard");
    public static final DeferredItem<SoulShardItem> SOUL_SHARD = ITEMS.registerItem("soul_shard", SoulShardItem::new,
            p -> p.stacksTo(1).component(SOUL.get(), Soul.EMPTY));
    /** Diamond ("EMERALD") material. */
    public static final DeferredItem<Item> SHADOW_BLADE = ITEMS.registerItem("shadow_blade", Item::new,
            p -> p.sword(ToolMaterial.DIAMOND, 3.0F, -2.4F));
    /** Only a shadow element repaired it, and that item was never created. */
    public static final DeferredItem<ShadowPickaxeItem> SHADOW_PICKAXE = ITEMS.registerItem("shadow_pickaxe", ShadowPickaxeItem::new,
            p -> p.pickaxe(ToolMaterial.DIAMOND, 1.0F, -2.8F)
                    .repairable(TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID, "repairs_shadow_pickaxe"))));

    public static final ResourceKey<Enchantment> SOUL_STEALER = ResourceKey.create(Registries.ENCHANTMENT,
            Identifier.fromNamespaceAndPath(MOD_ID, "soul_stealer"));

    public SoulShards(IEventBus modBus) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        BLOCK_ENTITIES.register(modBus);
        COMPONENTS.register(modBus);
        modBus.addListener(SoulShards::tab);
        NeoForge.EVENT_BUS.register(SoulEvents.class);
    }

    private static void tab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(BLANK_SOUL_SHARD);
            // getSubItems: an unbound shard, then one with 64, 128, 256, 512 and 1024 souls
            event.accept(SOUL_SHARD);
            for (int i = 1; i <= 5; i++) {
                ItemStack stack = new ItemStack(SOUL_SHARD.get());
                stack.set(SOUL.get(), new Soul("", 1 << (i + 5)));
                event.accept(stack);
            }
        } else if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            event.accept(SOUL_CAGE_ITEM);
        } else if (event.getTabKey() == CreativeModeTabs.COMBAT) {
            event.accept(SHADOW_BLADE);
        } else if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(SHADOW_PICKAXE);
        }
    }
}

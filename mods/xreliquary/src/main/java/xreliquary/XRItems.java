package xreliquary;

import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.item.component.Weapon;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import net.minecraft.world.item.consume_effects.RemoveStatusEffectsConsumeEffect;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import xreliquary.entity.ThrownEntity;
import xreliquary.item.AmmoItem;
import xreliquary.item.CondensedPotionItem;
import xreliquary.item.DestructionCatalystItem;
import xreliquary.item.DistortionCloakItem;
import xreliquary.item.EmperorChaliceItem;
import xreliquary.item.EmptyVoidTearItem;
import xreliquary.item.FertileLilypadItem;
import xreliquary.item.FortuneCoinItem;
import xreliquary.item.HandgunItem;
import xreliquary.item.IceRodItem;
import xreliquary.item.MagicbaneItem;
import xreliquary.item.MercyCrossItem;
import xreliquary.item.MidasTouchstoneItem;
import xreliquary.item.PotionKind;
import xreliquary.item.SalamanderEyeItem;
import xreliquary.item.ShotType;
import xreliquary.item.SojournerStaffItem;
import xreliquary.item.ThrowerItem;
import xreliquary.item.TomeItem;
import xreliquary.item.VoidSatchelItem;
import xreliquary.item.VoidTearItem;
import xreliquary.item.WitherlessRoseItem;
import xreliquary.item.WraithEyeItem;
import xreliquary.item.XRItem;

import java.util.ArrayList;
import java.util.List;

/**
 * XRItems.init with the ids of the pack's xreliquary.cfg (+ 256). Bullets, magazines, gun parts and condensed potions
 * had damage values; each is an item of its own here. "Tear" (letheTear, 6170) was read but never made into an item.
 */
public final class XRItems {
    static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Reliquary.MOD_ID);
    static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Reliquary.MOD_ID);

    /** The old item id (config + 256), its damage value (-1: any) and the item. */
    public record OldId(int id, int damage, DeferredItem<? extends Item> item) {
    }

    public static final List<OldId> OLD_IDS = new ArrayList<>();

    private static <I extends Item> DeferredItem<I> old(int configId, int damage, DeferredItem<I> item) {
        OLD_IDS.add(new OldId(configId + 256, damage, item));
        return item;
    }

    private static DeferredItem<XRItem> simple(int id, String name, int lines, Rarity rarity, boolean foil, int stack) {
        return old(id, -1, ITEMS.registerItem(name, p -> new XRItem(p, lines), p -> {
            p = p.stacksTo(stack).rarity(rarity);
            return foil ? p.component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true) : p;
        }));
    }

    /** ItemSword(GOLD) with its own uses: damage 4, 1.5 on every block, blocks like every sword of 1.5.2. */
    private static Item.Properties goldSword(Item.Properties p, int uses) {
        return p.sword(ToolMaterial.GOLD, 3.0F - ToolMaterial.GOLD.attackDamageBonus(), -2.4F).durability(uses)
                .component(DataComponents.TOOL, new Tool(List.of(), 1.5F, 2, false))
                .component(DataComponents.WEAPON, new Weapon(1))
                .component(DataComponents.BLOCKS_ATTACKS, hexxit.combat152.Combat152.BLOCKING)
                .rarity(Rarity.EPIC);
    }

    public static final DeferredItem<HandgunItem> HANDGUN = old(6163, -1, ITEMS.registerItem("handgun", HandgunItem::new,
            p -> p.durability(267).setNoCombineRepair()));
    public static final List<DeferredItem<AmmoItem>> BULLETS = new ArrayList<>();
    public static final List<DeferredItem<AmmoItem>> MAGAZINES = new ArrayList<>();

    static {
        for (ShotType t : ShotType.values()) {
            BULLETS.add(old(6153, t.ordinal(), ITEMS.registerItem(t.id + "_shot", p -> new AmmoItem(p, t, false))));
        }
        for (ShotType t : ShotType.values()) {
            MAGAZINES.add(old(6165, t.ordinal(), ITEMS.registerItem(t.id + "_magazine", p -> new AmmoItem(p, t, true))));
        }
    }

    public static final DeferredItem<MercyCrossItem> MERCY_CROSS = old(6159, -1, ITEMS.registerItem("mercy_cross", MercyCrossItem::new,
            p -> goldSword(p, 64)));
    public static final DeferredItem<ThrowerItem> HOLY_HAND_GRENADE = old(6161, -1, ITEMS.registerItem("holy_hand_grenade",
            p -> new ThrowerItem(p, ThrownEntity.Kind.GRENADE, true), p -> p.rarity(Rarity.RARE)));
    public static final DeferredItem<FortuneCoinItem> FORTUNE_COIN = old(6157, -1, ITEMS.registerItem("fortune_coin", FortuneCoinItem::new,
            p -> p.stacksTo(1).rarity(Rarity.EPIC)));
    public static final DeferredItem<ThrowerItem> GLOWING_WATER = old(6175, -1, ITEMS.registerItem("glowing_water",
            p -> new ThrowerItem(p, ThrownEntity.Kind.GLOWING_WATER, false)));
    public static final List<DeferredItem<XRItem>> GUN_PARTS = List.of(
            old(6162, 0, ITEMS.registerItem("grip_assembly", p -> new XRItem(p, 1), p -> p.stacksTo(1))),
            old(6162, 1, ITEMS.registerItem("barrel_assembly", p -> new XRItem(p, 1), p -> p.stacksTo(1))),
            old(6162, 2, ITEMS.registerItem("hammer_assembly", p -> new XRItem(p, 1), p -> p.stacksTo(1))));
    /** ItemGlowingBread: ItemFood(20, 1.0), eaten in 16 ticks. */
    public static final DeferredItem<XRItem> GLOWING_BREAD = old(6152, -1, ITEMS.registerItem("glowing_bread", p -> new XRItem(p, 1),
            p -> p.rarity(Rarity.RARE).component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true)
                    .food(new FoodProperties.Builder().nutrition(20).saturationModifier(1.0F).build(), Consumables.defaultFood().consumeSeconds(0.8F).build())));
    public static final DeferredItem<SojournerStaffItem> SOJOURNER_STAFF = old(6172, -1, ITEMS.registerItem("sojourner_staff", SojournerStaffItem::new,
            p -> p.durability(513).setNoCombineRepair().rarity(Rarity.EPIC)));
    public static final DeferredItem<TomeItem> ALKAHESTRY_TOME = old(6171, -1, ITEMS.registerItem("alkahestry_tome", TomeItem::new,
            p -> p.stacksTo(1).rarity(Rarity.EPIC)));
    public static final DeferredItem<MidasTouchstoneItem> MIDAS_TOUCHSTONE = old(6173, -1, ITEMS.registerItem("midas_touchstone",
            MidasTouchstoneItem::new, p -> p.durability(257).setNoCombineRepair().rarity(Rarity.EPIC)));
    public static final DeferredItem<XRItem> ALKAHEST = simple(6151, "alkahest", 2, Rarity.EPIC, true, 1);
    public static final DeferredItem<EmperorChaliceItem> EMPEROR_CHALICE = old(6155, -1, ITEMS.registerItem("emperor_chalice", EmperorChaliceItem::new,
            p -> p.stacksTo(1).rarity(Rarity.EPIC)));
    public static final DeferredItem<SalamanderEyeItem> SALAMANDER_EYE = old(6168, -1, ITEMS.registerItem("salamander_eye",
            p -> new SalamanderEyeItem(p, 3), p -> p.stacksTo(1).rarity(Rarity.EPIC)));
    public static final DeferredItem<IceRodItem> ICE_ROD = old(6164, -1, ITEMS.registerItem("ice_rod", IceRodItem::new,
            p -> p.durability(257).setNoCombineRepair().rarity(Rarity.EPIC)));
    public static final List<DeferredItem<CondensedPotionItem>> POTIONS = new ArrayList<>();

    static {
        for (PotionKind kind : PotionKind.values()) {
            POTIONS.add(old(6158, kind.ordinal(), ITEMS.registerItem("potion_" + kind.id, p -> new CondensedPotionItem(p, kind), p -> {
                p = p.stacksTo(16).rarity(kind.hasEffect() ? Rarity.EPIC : Rarity.COMMON);
                if (kind.isPotion()) {
                    Consumable.Builder drink = Consumables.defaultDrink().consumeSeconds(0.8F).onConsume(new ApplyStatusEffectsConsumeEffect(kind.effects()));
                    if (kind == PotionKind.PANACEA) {
                        drink.onConsume(new RemoveStatusEffectsConsumeEffect(HolderSet.direct(MobEffects.WITHER, MobEffects.HUNGER, MobEffects.POISON,
                                MobEffects.NAUSEA, MobEffects.MINING_FATIGUE, MobEffects.SLOWNESS, MobEffects.BLINDNESS, MobEffects.WEAKNESS)));
                    }
                    p = p.component(DataComponents.CONSUMABLE, drink.build()).usingConvertsTo(POTIONS.get(PotionKind.EMPTY_VIAL.ordinal()).get());
                }
                return p;
            })));
        }
    }

    public static final DeferredItem<MagicbaneItem> MAGICBANE = old(6166, -1, ITEMS.registerItem("magicbane", MagicbaneItem::new, p -> goldSword(p, 16)));
    public static final DeferredItem<WitherlessRoseItem> WITHERLESS_ROSE = old(6167, -1, ITEMS.registerItem("witherless_rose", WitherlessRoseItem::new,
            p -> p.stacksTo(1).rarity(Rarity.EPIC)));
    public static final DeferredItem<DistortionCloakItem> DISTORTION_CLOAK = old(6156, -1, ITEMS.registerItem("distortion_cloak",
            DistortionCloakItem::new, p -> p.stacksTo(1).rarity(Rarity.EPIC)));
    public static final DeferredItem<EmptyVoidTearItem> EMPTY_VOID_TEAR = old(6160, -1, ITEMS.registerItem("empty_void_tear", EmptyVoidTearItem::new));
    public static final DeferredItem<VoidTearItem> VOID_TEAR = old(6174, -1, ITEMS.registerItem("void_tear", VoidTearItem::new,
            p -> p.stacksTo(1).rarity(Rarity.EPIC).component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true)));
    public static final DeferredItem<WraithEyeItem> WRAITH_EYE = old(6176, -1, ITEMS.registerItem("wraith_eye", WraithEyeItem::new,
            p -> p.stacksTo(1).rarity(Rarity.EPIC)));
    public static final DeferredItem<VoidSatchelItem> VOID_SATCHEL = old(6169, -1, ITEMS.registerItem("void_satchel", VoidSatchelItem::new,
            p -> p.stacksTo(1).rarity(Rarity.EPIC)));
    public static final DeferredItem<DestructionCatalystItem> DESTRUCTION_CATALYST = old(6154, -1, ITEMS.registerItem("destruction_catalyst",
            DestructionCatalystItem::new, p -> p.stacksTo(1)));

    public static final DeferredItem<net.minecraft.world.item.BlockItem> ALTAR = ITEMS.registerSimpleBlockItem(XRBlocks.ALTAR);
    public static final DeferredItem<FertileLilypadItem> FERTILE_LILYPAD = ITEMS.registerItem("fertile_lilypad",
            p -> new FertileLilypadItem(XRBlocks.FERTILE_LILYPAD.get(), p), p -> p.useBlockDescriptionPrefix().rarity(Rarity.EPIC));
    public static final DeferredItem<net.minecraft.world.item.BlockItem> WRAITH_NODE = ITEMS.registerSimpleBlockItem(XRBlocks.WRAITH_NODE);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TAB = TABS.register("xreliquary", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.xreliquary"))
            .icon(() -> new ItemStack(MERCY_CROSS.get()))
            .displayItems((params, output) -> ITEMS.getEntries().forEach(item -> output.accept(item.get())))
            .build());

    public static Item potion(PotionKind kind) {
        return POTIONS.get(kind.ordinal()).get();
    }

    private XRItems() {
    }

    static void register(IEventBus modBus) {
        ITEMS.register(modBus);
        TABS.register(modBus);
    }
}

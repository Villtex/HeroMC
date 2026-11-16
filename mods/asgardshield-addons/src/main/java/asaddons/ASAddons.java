package asaddons;

import asgardshield.item.ASItems;
import asgardshield.item.GuardSound;
import asgardshield.item.ItemAsgardShield;
import asgardshield.item.ItemZedSword;
import asgardshield.item.Special;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.List;

/**
 * The three AsgardShield addons 2.0.0 of Hexxit 1.0.11 in one mod: ASAddon_NetherEnder (nether quartz and ender),
 * ASAddon_Hallowed (skull and patchwork) and ASAddon_HarkenScythe (livingmetal and biomass). Each adds a normal and
 * a gilded shield of two materials and two giant swords. The Harken Scythe equipment needs the items of Harken Scythe
 * for its recipes and repairs; without Harken Scythe its items stay out of the creative tab and cannot be crafted.
 * The textures ship in the core archive.
 */
@Mod(ASAddons.MOD_ID)
public final class ASAddons {
    public static final String MOD_ID = "asaddons";
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MOD_ID);
    private static final List<DeferredItem<? extends Item>> TAB = new ArrayList<>();
    private static final List<DeferredItem<? extends Item>> HARKEN_TAB = new ArrayList<>();

    /** Tool materials of EnumHelper.addToolMaterial: uses, damage bonus, enchantability. */
    private record Material(String name, int uses, int damage, int enchantability) {
    }

    private static final Material NETHERQUARTZ = new Material("nether", 350, 2, 16);
    private static final Material ENDER = new Material("ender", 900, 3, 15);
    private static final Material SKULL = new Material("skull", 160, 1, 11);
    private static final Material PATCHWORK = new Material("patchwork", 30, 0, 5);
    private static final Material LIVINGMETAL = new Material("livingmetal", 250, 2, 20);
    private static final Material BIOMASS = new Material("biomass", 150, 2, 20);

    private static TagKey<Item> repair(String kind, Material material) {
        return ASItems.itemTag(MOD_ID, "repairs/" + kind + "/" + material.name);
    }

    /**
     * A shield and its gilded version (ItemAsgardShield with specialNumber): durability uses x {@code durabilityFactor}
     * (patchwork x2, ender / 2), damage 1 (1 + bonus / 2 for skull and patchwork, bonus / 2 for the others: always 1),
     * enchantability / 3; gilded +128, +1, +2.
     */
    private static DeferredItem<ItemAsgardShield> shields(List<DeferredItem<? extends Item>> tab, String name, Material material, int perk,
            GuardSound sound, float multiplier, Special special, int durability) {
        DeferredItem<ItemAsgardShield> gilded = ITEMS.registerItem("gilded_" + name, p -> new ItemAsgardShield(p, perk, true, sound,
                multiplier - 0.05F, -0.2F, special, material.name, null), p -> ASItems.shield(p, durability + 128, 2,
                material.enchantability / 3 + 2, repair("shield", material)));
        DeferredItem<ItemAsgardShield> shield = ITEMS.registerItem(name, p -> new ItemAsgardShield(p, perk, false, sound, multiplier, -0.1F,
                special, material.name, gilded), p -> ASItems.shield(p, durability, 1, material.enchantability / 3, repair("shield", material)));
        tab.add(shield);
        return gilded;
    }

    /** ItemZedSword: 1.5 x the uses, damage 6 + bonus. */
    private static DeferredItem<ItemZedSword> sword(String name, Material material, GuardSound sound, float multiplier, Special special) {
        return ITEMS.registerItem(name, p -> new ItemZedSword(p, sound, multiplier, special), p -> ASItems.giantSword(p,
                material.uses + material.uses / 2, 6 + material.damage, material.enchantability, repair("giant_sword", material), false));
    }

    static {
        // ASAddon_NetherEnder
        DeferredItem<ItemAsgardShield> gildedNether = shields(TAB, "nether_quartz_shield", NETHERQUARTZ, 5, GuardSound.CRYSTAL, 0.85F,
                Special.NETHER, 350);
        DeferredItem<ItemAsgardShield> gildedEnder = shields(TAB, "ender_shield", ENDER, 6, GuardSound.STONE, 0.8F, Special.ENDER, 450);
        TAB.add(gildedNether);
        TAB.add(gildedEnder);
        TAB.add(sword("nether_quartz_giant_sword", NETHERQUARTZ, GuardSound.CRYSTAL, 0.85F, Special.NETHER));
        TAB.add(sword("ender_giant_sword", ENDER, GuardSound.STONE, 0.8F, Special.ENDER));
        // ASAddon_Hallowed
        DeferredItem<ItemAsgardShield> gildedSkull = shields(TAB, "skull_shield", SKULL, 7, GuardSound.BONE, 0.95F, Special.SKULL, 160);
        DeferredItem<ItemAsgardShield> gildedPatchwork = shields(TAB, "patchwork_shield", PATCHWORK, 8, GuardSound.FLESH, 0.95F,
                Special.PATCHWORK, 60);
        TAB.add(gildedSkull);
        TAB.add(gildedPatchwork);
        TAB.add(sword("skull_giant_sword", SKULL, GuardSound.BONE, 0.9F, Special.SKULL));
        TAB.add(sword("patchwork_giant_sword", PATCHWORK, GuardSound.FLESH, 0.95F, Special.PATCHWORK));
        // ASAddon_HarkenScythe
        DeferredItem<ItemAsgardShield> gildedLivingmetal = shields(HARKEN_TAB, "livingmetal_shield", LIVINGMETAL, 9, GuardSound.METAL, 0.85F,
                Special.LIVINGMETAL, 250);
        DeferredItem<ItemAsgardShield> gildedBiomass = shields(HARKEN_TAB, "biomass_shield", BIOMASS, 10, GuardSound.FLESH, 0.85F,
                Special.BIOMASS, 150);
        HARKEN_TAB.add(gildedLivingmetal);
        HARKEN_TAB.add(gildedBiomass);
        HARKEN_TAB.add(sword("livingmetal_giant_sword", LIVINGMETAL, GuardSound.METAL, 0.85F, Special.LIVINGMETAL));
        // the biomass sword had the type "livingmetal" in 1.5.2 (no effect)
        HARKEN_TAB.add(sword("biomass_giant_sword", BIOMASS, GuardSound.FLESH, 0.85F, Special.LIVINGMETAL));
    }

    public ASAddons(IEventBus modBus) {
        ITEMS.register(modBus);
        modBus.addListener(BuildCreativeModeTabContentsEvent.class, event -> {
            if (event.getTabKey() == CreativeModeTabs.COMBAT) {
                TAB.forEach(item -> event.accept(item.get()));
                if (ModList.get().isLoaded("harkenscythe_core")) {
                    HARKEN_TAB.forEach(item -> event.accept(item.get()));
                }
            }
        });
    }
}

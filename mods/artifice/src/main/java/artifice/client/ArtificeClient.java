package artifice.client;

import artifice.Artifice;
import artifice.ArtificeBlocks;
import artifice.item.BoxItem;
import artifice.item.Upgrade;
import artifice.item.UpgradeItem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.color.block.BlockTintSources;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import java.util.List;

/** Client side: the lily pad colour under the lotus lily and the tooltips of the original (ArtificeTooltips). */
public final class ArtificeClient {
    /** BlockLilyPad.colorMultiplier of 1.5.2 (renderBlockLilyPad draws the vanilla pad in its colour). */
    private static final int LILY_PAD_COLOR = 0xFF208030;

    private ArtificeClient() {
    }

    private static void colors(RegisterColorHandlersEvent.BlockTintSources event) {
        event.register(List.of(BlockTintSources.constant(LILY_PAD_COLOR)), ArtificeBlocks.LOTUS_LILY.get());
    }

    /** RomanUtil for the levels of the upgrades. */
    private static String roman(int n) {
        return switch (n) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            default -> Integer.toString(n);
        };
    }

    private static Component enchantmentLine(ResourceKey<Enchantment> key, int level) {
        Identifier id = key.identifier();
        return Component.translatable("enchantment." + id.getNamespace() + "." + id.getPath()).append(" (" + roman(level) + ")")
                .withStyle(ChatFormatting.YELLOW);
    }

    /**
     * Lines {@code tooltip.artifice.<item>.0, .1 ...} (grey), then what the item adds (box contents, upgrade
     * enchantments), then the flavor lines {@code flavor.artifice.<item>.0 ...} (dark grey italic).
     */
    private static void tooltips(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        Identifier id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (!Artifice.MOD_ID.equals(id.getNamespace())) {
            return;
        }
        List<Component> lines = event.getToolTip();
        Language language = Language.getInstance();
        String tip = "tooltip." + Artifice.MOD_ID + "." + id.getPath() + ".";
        for (int i = 0; language.has(tip + i); i++) {
            lines.add(Component.translatable(tip + i).withStyle(ChatFormatting.GRAY));
        }
        if (stack.getItem() instanceof UpgradeItem upgrade) {
            Upgrade u = upgrade.upgrade();
            lines.add(enchantmentLine(u.enchantment, u.maxLevel));
            if (u == Upgrade.SHARPENING_KIT) {
                lines.add(enchantmentLine(Enchantments.EFFICIENCY, u.maxLevel));
            }
        }
        ItemStackTemplate contents = BoxItem.contents(stack);
        if (contents != null) {
            ItemStack thing = contents.create();
            lines.add(Component.translatable("tooltip.artifice.box.contains",
                    thing.getHoverName().copy().withStyle(ChatFormatting.AQUA)).withStyle(ChatFormatting.GRAY));
            lines.add(Component.translatable("tooltip.artifice.box.amount",
                    Component.literal(Integer.toString(contents.count())).withStyle(ChatFormatting.AQUA)).withStyle(ChatFormatting.GRAY));
            ItemEnchantments enchantments = thing.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
            if (enchantments.isEmpty()) {
                enchantments = thing.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);
            }
            for (var e : enchantments.entrySet()) {
                lines.add(Enchantment.getFullname(e.getKey(), e.getIntValue()).copy().withStyle(ChatFormatting.YELLOW));
            }
        }
        String flavor = "flavor." + Artifice.MOD_ID + "." + id.getPath() + ".";
        for (int i = 0; language.has(flavor + i); i++) {
            lines.add(Component.translatable(flavor + i).withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        }
    }

    public static void register(IEventBus modBus) {
        modBus.addListener(ArtificeClient::colors);
        NeoForge.EVENT_BUS.addListener(ArtificeClient::tooltips);
    }
}

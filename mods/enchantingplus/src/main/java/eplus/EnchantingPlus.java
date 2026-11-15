package eplus;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import eplus.network.EplusNetwork;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.slf4j.Logger;

/**
 * Enchanting Plus 1.15.4: the enchanting table (the vanilla block, as in the original, which replaced it under the
 * same id) opens a GUI with every enchantment the item can take; each has a slider for its level, the levels are
 * bought with experience levels (formulas of ContainerEnchantTable), bookshelves limit the price, enchantments added
 * on the table can be taken back, enchanted items can be repaired. A "Vanilla" / "Mod" button switches between this
 * GUI and the vanilla one; the table glows (light 15) and gives off portal particles.
 */
@Mod(EnchantingPlus.MOD_ID)
public final class EnchantingPlus {
    public static final String MOD_ID = "eplus";
    public static final Logger LOGGER = LogUtils.getLogger();

    private static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, MOD_ID);
    private static final DeferredRegister.DataComponents COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, MOD_ID);
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENTS = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, MOD_ID);

    public static final DeferredHolder<MenuType<?>, MenuType<EnchantTableMenu>> MENU = MENUS.register("enchant_table",
            () -> IMenuTypeExtension.create(EnchantTableMenu::fromNetwork));

    /** One entry of the "restrictions" NBT list: enchantment, level, name of the player who enchanted. */
    public record Restriction(Holder<Enchantment> enchantment, int level, String player) {
        public static final Codec<Restriction> CODEC = RecordCodecBuilder.create(i -> i.group(
                Enchantment.CODEC.fieldOf("id").forGetter(Restriction::enchantment),
                Codec.INT.fieldOf("lvl").forGetter(Restriction::level),
                Codec.STRING.optionalFieldOf("player", "").forGetter(Restriction::player)).apply(i, Restriction::new));
        public static final StreamCodec<RegistryFriendlyByteBuf, Restriction> STREAM_CODEC = StreamCodec.composite(
                Enchantment.STREAM_CODEC, Restriction::enchantment, ByteBufCodecs.VAR_INT, Restriction::level,
                ByteBufCodecs.STRING_UTF8, Restriction::player, Restriction::new);
    }

    /** Levels an enchantment passed on the table; the GUI only lets you go back down to those. */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<List<Restriction>>> RESTRICTIONS = COMPONENTS.registerComponentType(
            "restrictions", b -> b.persistent(Restriction.CODEC.listOf()).networkSynchronized(Restriction.STREAM_CODEC.apply(ByteBufCodecs.list())));

    /** Player data "eplus:useMod": the client's useMod setting, sent on login (ConfigPacket of 1.5.2). */
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Boolean>> USE_MOD = ATTACHMENTS.register("use_mod",
            () -> AttachmentType.builder(() -> Boolean.TRUE).serialize(Codec.BOOL.fieldOf("value")).copyOnDeath().build());

    public EnchantingPlus(IEventBus modBus, ModContainer container) {
        MENUS.register(modBus);
        COMPONENTS.register(modBus);
        ATTACHMENTS.register(modBus);
        container.registerConfig(ModConfig.Type.SYNCED, EplusConfig.SERVER_SPEC, "eplus-server.toml");
        container.registerConfig(ModConfig.Type.CLIENT, EplusConfig.CLIENT_SPEC, "eplus-client.toml");
        modBus.addListener(EplusNetwork::register);
        NeoForge.EVENT_BUS.register(TableEvents.class);
        if (FMLEnvironment.getDist() == Dist.CLIENT) {
            eplus.client.EplusClient.register(modBus);
        }
    }

    private static final Map<BlockState, Integer> VANILLA_LIGHT = new HashMap<>();

    /**
     * hasLight: setLightValue(1.0F) on the table, light 15 (vanilla 26.3: 7). The light level is fixed per block
     * state when the block is created, so it is set on the states of the vanilla block.
     */
    public static synchronized void applyLight(boolean hasLight) {
        try {
            Field field = BlockBehaviour.BlockStateBase.class.getDeclaredField("lightEmission");
            field.setAccessible(true);
            for (BlockState state : Blocks.ENCHANTING_TABLE.getStateDefinition().getPossibleStates()) {
                int vanilla = VANILLA_LIGHT.computeIfAbsent(state, BlockState::getLightEmission);
                field.setInt(state, hasLight ? 15 : vanilla);
            }
        } catch (ReflectiveOperationException | RuntimeException e) {
            LOGGER.warn("Could not change the light level of the enchanting table: {}", e.toString());
        }
    }
}

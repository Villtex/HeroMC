package bspkrs.treecapitator;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;
import net.neoforged.neoforge.registries.NewDatapackRegistryEvent;

/**
 * TreeCapitator 1.5.2.r14: breaking a log of a known tree with an axe also breaks the logs above it and the leaves of
 * the crown. The ASM hook into ItemInWorldManager.removeBlock is the block break event here (fired before the block
 * is removed, like the hook), the break speed handler is PlayerHandler.getPlayerBreakSpeed.
 *
 * <p>Trees: data registry treecapitator:tree (synced to clients, which need it for the break speed). Axes and shears:
 * item tags #treecapitator:axes and #treecapitator:shears. Settings: config treecapitator-server.toml.
 */
@Mod(TreeCapitatorMod.MOD_ID)
public final class TreeCapitatorMod {
    public static final String MOD_ID = "treecapitator";
    public static final ResourceKey<Registry<TreeDefinition.Data>> TREE_REGISTRY = ResourceKey.createRegistryKey(id("tree"));
    public static final TagKey<Item> AXES = TagKey.create(Registries.ITEM, id("axes"));
    public static final TagKey<Item> SHEARS = TagKey.create(Registries.ITEM, id("shears"));

    /** PlayerHandler.playerSneakingMap: sneak state at the last left click, for treeHeightDecidesBreakSpeed. */
    private final Map<UUID, Boolean> playerSneakingMap = new HashMap<>();

    public TreeCapitatorMod(IEventBus modBus, ModContainer container) {
        container.registerConfig(ModConfig.Type.SYNCED, TCSettings.SPEC, "treecapitator-server.toml");
        modBus.addListener(NewDatapackRegistryEvent.class,
                e -> e.worldRegistry(TREE_REGISTRY, TreeDefinition.Data.CODEC, TreeDefinition.Data.CODEC));
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, BreakBlockEvent.class, this::onBlockBreak);
        NeoForge.EVENT_BUS.addListener(PlayerEvent.BreakSpeed.class, this::onBreakSpeed);
        NeoForge.EVENT_BUS.addListener(PlayerInteractEvent.LeftClickBlock.class, this::onBlockClicked);
    }

    static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    /** TreeCapitatorMod.onBlockHarvested, called by the ASM hook before the log is removed. */
    private void onBlockBreak(BreakBlockEvent event) {
        if (event.isCanceled() || !(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        Player player = event.getPlayer();
        BlockState state = event.getState();
        TreeRegistry registry = TreeRegistry.of(level.registryAccess());
        if (!registry.isRegistered(state)) {
            return;
        }
        TCLog.debug("Block %s is a log.", state);
        if (!TreeCapitator.isBreakingEnabled(player)) {
            TCLog.debug("Chopping disabled due to player state or gamemode.");
            return;
        }
        if (TreeCapitator.isBreakingPossible(player, true)) {
            TreeDefinition treeDef = registry.get(state);
            if (treeDef != null) {
                new TreeCapitator(player, treeDef, registry.masterDefinition()).onBlockHarvested(level, event.getPos(), state);
            }
        }
    }

    private void onBlockClicked(PlayerInteractEvent.LeftClickBlock event) {
        if (event.getAction() != PlayerInteractEvent.LeftClickBlock.Action.START) {
            return;
        }
        Player player = event.getEntity();
        BlockState state = player.level().getBlockState(event.getPos());
        if (state.isAir()) {
            return;
        }
        if (TCSettings.ALLOW_DEBUG_OUTPUT.get() && player.isShiftKeyDown() && !player.level().isClientSide()) {
            player.sendSystemMessage(Component.literal("Block: " + state));
        }
        playerSneakingMap.put(player.getUUID(), player.isShiftKeyDown());
    }

    /** PlayerHandler.getPlayerBreakSpeed: logs break slower with an axe (the whole tree comes down). */
    private void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        Player player = event.getEntity();
        BlockState state = event.getState();
        TreeRegistry registry = TreeRegistry.of(player.level().registryAccess());
        if (!registry.isRegistered(state) || !TreeCapitator.isAxeItemEquipped(player)) {
            return;
        }
        TreeDefinition treeDef = registry.get(state);
        if (treeDef == null) {
            return;
        }
        if (TCSettings.TREE_HEIGHT_DECIDES_BREAK_SPEED.get()) {
            BlockPos pos = event.getPosition().orElse(null);
            if (pos == null) {
                return;
            }
            Boolean sneaking = playerSneakingMap.get(player.getUUID());
            if (sneaking != null && sneaking != player.isShiftKeyDown()) {
                event.setNewSpeed(1.0E-4F);
            } else if (TreeCapitator.isBreakingEnabled(player)) {
                int height = TreeCapitator.getTreeHeight(treeDef, player.level(), pos, player);
                if (height > 1) {
                    event.setNewSpeed(event.getOriginalSpeed() / (height * 2));
                }
            }
        } else {
            event.setNewSpeed(event.getOriginalSpeed() * treeDef.breakSpeedModifier());
        }
    }
}

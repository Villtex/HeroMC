package ichun.backtools.client;

import ichun.backtools.BackTools;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShearsItem;
import net.neoforged.bus.api.IEventBus;
import net.minecraft.client.entity.ClientAvatarEntity;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.renderstate.AvatarRenderStateModifier;
import net.neoforged.neoforge.client.renderstate.RegisterRenderStateModifiersEvent;
import net.neoforged.neoforge.common.NeoForge;

import java.util.HashMap;
import java.util.Map;

/**
 * mod_BackTools and BT_EntityBackItem: every client tick each player's back item is worked out the way the
 * original's helper entity did; the render state carries it to {@link BackItemLayer}.
 */
public final class BackToolsClient {
    static final ContextKey<BackItem> BACK_ITEM = new ContextKey<>(Identifier.fromNamespaceAndPath(BackTools.MOD_ID, "back_item"));
    private static final Map<Integer, Back> BACKS = new HashMap<>();
    private static ClientLevel level;

    /** What a render state needs: the item model, its turn (quarter turns) and whether it is flipped. */
    record BackItem(ItemStackRenderState item, int orientation, boolean flipped, boolean chestArmor) {
    }

    /** The fields of BT_EntityBackItem. */
    private static final class Back {
        ItemStack itemToRender = ItemStack.EMPTY;
        ItemStack heldItem = ItemStack.EMPTY;
        int heldOrientation;
        int renderOrientation;
        boolean heldFlipped;
        boolean renderFlipped;
    }

    /** Orientation and flip of an item that goes on the back, or null (addClass of mod_BackTools.load). */
    private static int[] support(ItemStack stack) {
        for (String entry : BackTools.ITEM_SUPPORT.get()) {
            String[] parts = entry.split(":");
            if (parts.length >= 2 && Identifier.tryParse(parts[0] + ":" + parts[1]) instanceof Identifier id
                    && BuiltInRegistries.ITEM.getValue(id) == stack.getItem()) {
                int orientation = parts.length > 2 ? parse(parts[2]) : 0;
                return new int[] {orientation < 0 || orientation > 3 ? 0 : orientation, parts.length > 3 ? parse(parts[3]) : 0};
            }
        }
        // tags of today (Teil 26e): tools and weapons of other mods hang on the back as well
        if (stack.is(ItemTags.SWORDS) || stack.getItem() instanceof ShearsItem || stack.is(net.neoforged.neoforge.common.Tags.Items.TOOLS_SHEAR)
                || stack.is(net.neoforged.neoforge.common.Tags.Items.MELEE_WEAPON_TOOLS)) {
            return new int[] {2, 0};
        }
        if (stack.getItem() instanceof BowItem || stack.is(net.neoforged.neoforge.common.Tags.Items.TOOLS_BOW)) {
            return new int[] {1, 0};
        }
        if (stack.is(ItemTags.HOES) || stack.is(ItemTags.PICKAXES) || stack.is(ItemTags.AXES) || stack.is(ItemTags.SHOVELS)
                || stack.is(net.neoforged.neoforge.common.Tags.Items.MINING_TOOL_TOOLS)) {
            return new int[] {0, 0};
        }
        return null;
    }

    private static int parse(String s) {
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public static void register(IEventBus modBus) {
        NeoForge.EVENT_BUS.addListener(ClientTickEvent.Post.class, event -> tick());
        modBus.addListener(RegisterRenderStateModifiersEvent.class, event -> event.registerAvatarEntityModifier(new AvatarRenderStateModifier() {
            @Override
            public <T extends Avatar & ClientAvatarEntity> void accept(T avatar, AvatarRenderState state) {
                extract(avatar, state);
            }
        }));
        modBus.addListener(EntityRenderersEvent.AddLayers.class, event -> {
            for (var skin : event.getSkins()) {
                AvatarRenderer<?> renderer = event.getPlayerRenderer(skin);
                if (renderer != null) {
                    renderer.addLayer(new BackItemLayer(renderer));
                }
            }
        });
    }

    private static void tick() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != level) {
            level = mc.level;
            BACKS.clear();
        }
        if (mc.level == null) {
            return;
        }
        BACKS.keySet().removeIf(id -> !(mc.level.getEntity(id) instanceof Player p) || !p.isAlive());
        for (Player player : mc.level.players()) {
            if (player.isAlive()) {
                update(player, BACKS.computeIfAbsent(player.getId(), id -> new Back()));
            }
        }
    }

    /** BT_EntityBackItem.onUpdate. */
    private static void update(Player player, Back back) {
        ItemStack held = player.getMainHandItem();
        if (!held.isEmpty()) {
            if (!back.itemToRender.isEmpty() && ItemStack.matches(back.itemToRender, held)) {
                back.itemToRender = ItemStack.EMPTY;
                if (!back.heldItem.isEmpty() && !ItemStack.matches(back.heldItem, held)) {
                    back.itemToRender = back.heldItem;
                    back.renderOrientation = back.heldOrientation;
                    back.renderFlipped = back.heldFlipped;
                }
            } else if (!back.heldItem.isEmpty() && !ItemStack.matches(back.heldItem, held)) {
                back.itemToRender = back.heldItem;
                back.renderOrientation = back.heldOrientation % 4;
                back.renderFlipped = back.heldFlipped;
            }
            int[] support = support(held);
            if (support != null) {
                back.heldItem = held.copy();
                back.heldOrientation = support[0];
                back.heldFlipped = support[1] == 1;
            }
        } else {
            back.itemToRender = back.heldItem;
            back.renderOrientation = back.heldOrientation;
            back.renderFlipped = back.heldFlipped;
        }
        // the item was just dropped: it is no longer on the back
        if (!back.itemToRender.isEmpty()) {
            for (ItemEntity item : player.level().getEntitiesOfClass(ItemEntity.class,
                    player.getBoundingBox().expandTowards(player.getDeltaMovement()).inflate(0.5))) {
                if (item.getAge() <= 1 && ItemStack.matches(item.getItem(), back.itemToRender)) {
                    back.heldItem = ItemStack.EMPTY;
                    back.itemToRender = ItemStack.EMPTY;
                    break;
                }
            }
        }
    }

    private static void extract(Avatar avatar, AvatarRenderState state) {
        Back back = BACKS.get(avatar.getId());
        if (back == null || back.itemToRender.isEmpty() || avatar.isSleeping() || state.isInvisible
                || state.showCape && state.skin.cape() != null) {
            state.setRenderData(BACK_ITEM, null);
            return;
        }
        ItemStackRenderState item = new ItemStackRenderState();
        Minecraft.getInstance().getItemModelResolver().updateForLiving(item, back.itemToRender, ItemDisplayContext.NONE, avatar);
        state.setRenderData(BACK_ITEM, new BackItem(item, back.renderOrientation, back.renderFlipped,
                !avatar.getItemBySlot(EquipmentSlot.CHEST).isEmpty()));
    }
}

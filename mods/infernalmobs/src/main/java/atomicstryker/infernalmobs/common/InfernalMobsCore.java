package atomicstryker.infernalmobs.common;

import atomicstryker.infernalmobs.common.mods.*;
import com.mojang.logging.LogUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantable;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.core.component.DataComponents;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.function.BiFunction;
import java.util.stream.Stream;

/**
 * Infernal Mobs 1.3.1 (FML id "InfernalMobs"): one in {@code eliteRarity} monsters becomes a rare mob with 2-4
 * modifiers, one in {@code ultraRarity} of those an Ultra (+3-4), one in {@code infernoRarity} of those an Infernal
 * (+3-4 more). Rare mobs have their health times the number of modifiers, drop 25 XP and one enchanted item of the
 * loot list per five modifiers.
 *
 * <p>The modifier names are saved where the original kept them, in the entity's persistent data under
 * "InfernalMobsMod" ({@code NeoForgeData} in the NBT), and reach the client through a synced data attachment instead
 * of the old packets 1 and 4.
 */
@Mod(InfernalMobsCore.MOD_ID)
public final class InfernalMobsCore {
    public static final String MOD_ID = "infernalmobs";
    public static final String NBT_TAG = "InfernalMobsMod";
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final DeferredRegister<AttachmentType<?>> ATTACHMENTS = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, MOD_ID);
    /** The modifier string of a rare mob ("" for none), synced to every client that tracks the mob; not saved. */
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<String>> MOD_NAMES = ATTACHMENTS.register("mods",
            () -> AttachmentType.builder(() -> "").sync(ByteBufCodecs.STRING_UTF8).build());

    private static final Identifier HEALTH_MODIFIER = Identifier.fromNamespaceAndPath(MOD_ID, "health");

    /** The modifiers in the order of loadMods (the order matters for createMobModifiers and stringToMobModifiers). */
    private record ModEntry(String className, BiFunction<LivingEntity, MobModifier, MobModifier> factory) {
    }

    private static final List<ModEntry> ALL_MODS = List.of(
            new ModEntry("MM_1UP", MM_1UP::new),
            new ModEntry("MM_Alchemist", MM_Alchemist::new),
            new ModEntry("MM_Berserk", MM_Berserk::new),
            new ModEntry("MM_Blastoff", MM_Blastoff::new),
            new ModEntry("MM_Bulwark", MM_Bulwark::new),
            new ModEntry("MM_Cloaking", MM_Cloaking::new),
            new ModEntry("MM_Darkness", MM_Darkness::new),
            new ModEntry("MM_Ender", MM_Ender::new),
            new ModEntry("MM_Exhaust", MM_Exhaust::new),
            new ModEntry("MM_Fiery", MM_Fiery::new),
            new ModEntry("MM_Ghastly", MM_Ghastly::new),
            new ModEntry("MM_Gravity", MM_Gravity::new),
            new ModEntry("MM_Lifesteal", MM_Lifesteal::new),
            new ModEntry("MM_Ninja", MM_Ninja::new),
            new ModEntry("MM_Poisonous", MM_Poisonous::new),
            new ModEntry("MM_Quicksand", MM_Quicksand::new),
            new ModEntry("MM_Regen", MM_Regen::new),
            new ModEntry("MM_Rust", MM_Rust::new),
            new ModEntry("MM_Sapper", MM_Sapper::new),
            new ModEntry("MM_Sprint", MM_Sprint::new),
            new ModEntry("MM_Sticky", MM_Sticky::new),
            new ModEntry("MM_Storm", MM_Storm::new),
            new ModEntry("MM_Vengeance", MM_Vengeance::new),
            new ModEntry("MM_Weakness", MM_Weakness::new),
            new ModEntry("MM_Webber", MM_Webber::new),
            new ModEntry("MM_Wither", MM_Wither::new));

    public static final List<String> MODIFIER_NAMES = ALL_MODS.stream().map(ModEntry::className).toList();

    /** proxy.getRareMobs() of the server; the client keeps its own map. */
    private static final Map<LivingEntity, MobModifier> RARE_MOBS = new WeakHashMap<>();

    /** Parsed droppedItemIDs: item, meta, stack size, randomizer. */
    private record Drop(Item item, int meta, int size, int random) {
    }

    private static List<Drop> dropList;
    private static final Set<String> FAILED_ITEMS = new HashSet<>();

    public InfernalMobsCore(IEventBus modBus, ModContainer container) {
        ATTACHMENTS.register(modBus);
        container.registerConfig(ModConfig.Type.SYNCED, IMConfig.SPEC, "InfernalMobs.toml");
        container.registerConfig(ModConfig.Type.CLIENT, IMConfig.CLIENT_SPEC, "InfernalMobs-client.toml");
        modBus.addListener(ModConfigEvent.Loading.class, e -> dropList = null);
        modBus.addListener(ModConfigEvent.Reloading.class, e -> dropList = null);
        EntityEventHandler.register();
        if (FMLEnvironment.getDist() == Dist.CLIENT) {
            atomicstryker.infernalmobs.client.InfernalMobsClient.register(modBus);
        }
        LOGGER.info("InfernalMobsCore load() completed! Modifiers ready: {}", ALL_MODS.size());
    }

    private static List<ModEntry> enabledMods() {
        return ALL_MODS.stream().filter(e -> IMConfig.modifierEnabled(e.className())).toList();
    }

    // ---- spawning ------------------------------------------------------------------------------------------------

    /** EntityJoinWorldEvent on the server: restore the saved modifiers or roll new ones. */
    static void onEntityJoined(LivingEntity entity) {
        String savedMods = entity.getPersistentData().getStringOr(NBT_TAG, "");
        if (!savedMods.isEmpty()) {
            addEntityModifiersByString(entity, savedMods);
        } else {
            processEntitySpawn(entity);
        }
    }

    static void processEntitySpawn(LivingEntity entity) {
        if (!entity.level().isClientSide() && !getIsRareEntity(entity) && entity instanceof Enemy && checkEntityClassAllowed(entity)
                && (checkEntityClassForced(entity) || entity.level().getRandom().nextInt(IMConfig.eliteRarity()) == 0)) {
            MobModifier mod = createMobModifiers(entity);
            if (mod != null) {
                RARE_MOBS.put(entity, mod);
                mod.onSpawningComplete(entity);
                applyHealth(entity, mod, true);
            }
        }
    }

    private static String typeId(LivingEntity entity) {
        return BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString();
    }

    private static boolean checkEntityClassAllowed(LivingEntity entity) {
        return !IMConfig.forbiddenEntities().contains(typeId(entity));
    }

    private static boolean checkEntityClassForced(LivingEntity entity) {
        return IMConfig.alwaysInfernal().contains(typeId(entity));
    }

    /** createMobModifiers of the original, including its checks (see the comments). */
    private static MobModifier createMobModifiers(LivingEntity entity) {
        RandomSource rand = entity.level().getRandom();
        int number = 2 + rand.nextInt(3);
        List<ModEntry> possibleMods = new ArrayList<>(enabledMods());
        if (rand.nextInt(IMConfig.ultraRarity()) == 0) {
            number += 3 + rand.nextInt(2);
            if (rand.nextInt(IMConfig.infernoRarity()) == 0) {
                number += 3 + rand.nextInt(2);
            }
        }
        MobModifier lastMod = null;
        while (number > 0 && !possibleMods.isEmpty()) {
            int index = rand.nextInt(possibleMods.size());
            MobModifier nextMod = possibleMods.get(index).factory().apply(entity, lastMod);
            boolean allowed = true;
            if (nextMod.getBlackListMobClasses() != null) {
                for (Class<?> cl : nextMod.getBlackListMobClasses()) {
                    // original: the entity class must be a superclass of the banned class (exactly Creeper, Spider)
                    if (entity.getClass().isAssignableFrom(cl)) {
                        allowed = false;
                        break;
                    }
                }
            }
            if (lastMod != null && lastMod.getModsNotToMixWith() != null) {
                // original: checks the bans of the previous modifier against its own chain, not the new modifier
                for (Class<?> cl : lastMod.getModsNotToMixWith()) {
                    if (lastMod.containsModifierClass(cl)) {
                        allowed = false;
                        break;
                    }
                }
            }
            possibleMods.remove(index);
            if (allowed) {
                lastMod = nextMod;
                number--;
            }
        }
        return lastMod;
    }

    static void addEntityModifiersByString(LivingEntity entity, String savedMods) {
        MobModifier mod = stringToMobModifiers(entity, savedMods);
        if (mod != null) {
            RARE_MOBS.put(entity, mod);
            mod.onSpawningComplete(entity);
            applyHealth(entity, mod, false);
        }
    }

    /** Builds the chain from the saved names (last name first). Unknown or disabled names are skipped. */
    public static MobModifier stringToMobModifiers(LivingEntity entity, String buffer) {
        MobModifier lastMod = null;
        String[] tokens = buffer.split("\\s");
        List<ModEntry> mods = enabledMods();
        for (int j = tokens.length - 1; j >= 0; j--) {
            String modName = tokens[j];
            for (ModEntry entry : mods) {
                MobModifier nextMod = entry.factory().apply(entity, lastMod);
                if (nextMod.modName.equals(modName)) {
                    lastMod = nextMod;
                    break;
                }
            }
        }
        return lastMod;
    }

    /** onSpawningComplete: saves the names and shows them to the clients. */
    static void storeModNames(LivingEntity entity, String names) {
        if (!entity.level().isClientSide()) {
            entity.getPersistentData().putString(NBT_TAG, names);
            entity.setData(MOD_NAMES, names);
        }
    }

    /**
     * getActualMaxHealth / setEntityHealthPastMax: max health = rint(max health * modifiers * mobHealthFactor) as a
     * {@code max_health} modifier (the attribute stops at 1024). A new rare mob (rolled now, or summoned with the
     * names but without the modifier yet) gets the full new health; a mob loaded from disk keeps its saved health.
     */
    static void applyHealth(LivingEntity entity, MobModifier mod, boolean fresh) {
        AttributeInstance attribute = entity.getAttribute(Attributes.MAX_HEALTH);
        if (attribute == null) {
            return;
        }
        boolean had = attribute.getModifier(HEALTH_MODIFIER) != null;
        attribute.removeModifier(HEALTH_MODIFIER);
        double base = attribute.getValue();
        double actualMax = Math.rint(base * mod.getModSize() * IMConfig.mobHealthFactor());
        attribute.addPermanentModifier(new AttributeModifier(HEALTH_MODIFIER, actualMax - base, AttributeModifier.Operation.ADD_VALUE));
        if (fresh || !had) {
            entity.setHealth(entity.getMaxHealth());
        }
    }

    public static MobModifier getMobModifiers(LivingEntity target) {
        return RARE_MOBS.get(target);
    }

    public static boolean getIsRareEntity(LivingEntity ent) {
        return RARE_MOBS.containsKey(ent);
    }

    public static void removeEntFromElites(LivingEntity entity) {
        RARE_MOBS.remove(entity);
    }

    // ---- loot ----------------------------------------------------------------------------------------------------

    private static List<Drop> drops() {
        if (dropList == null) {
            List<Drop> list = new ArrayList<>();
            for (String s : IMConfig.droppedItems()) {
                String[] meta = s.trim().split("-");
                Optional<Holder.Reference<Item>> item = Identifier.tryParse(meta[0]) == null ? Optional.empty()
                        : BuiltInRegistries.ITEM.get(Identifier.tryParse(meta[0]));
                if (item.isEmpty() || item.get().value() == Items.AIR) {
                    if (FAILED_ITEMS.add(s)) {
                        LOGGER.info("Infernal Mobs Item config failed to find any match for [{}], skipped", s);
                    }
                    continue;
                }
                try {
                    list.add(new Drop(item.get().value(), meta.length > 1 ? Integer.parseInt(meta[1]) : 0,
                            meta.length > 2 ? Integer.parseInt(meta[2]) : 1, meta.length > 3 ? Integer.parseInt(meta[3]) : 0));
                } catch (NumberFormatException e) {
                    if (FAILED_ITEMS.add(s)) {
                        LOGGER.warn("Infernal Mobs Item config entry [{}] is malformed, skipped", s);
                    }
                }
            }
            dropList = list;
        }
        return dropList;
    }

    static void dropLootForEnt(LivingEntity mob, MobModifier mods) {
        if (!(mob.level() instanceof ServerLevel level)) {
            return;
        }
        // 25 XP, split into orbs like EntityXPOrb.getXPSplit
        ExperienceOrb.award(level, mob.position(), 25);
        dropRandomEnchantedItems(level, mob, mods);
    }

    private static void dropRandomEnchantedItems(ServerLevel level, LivingEntity mob, MobModifier mods) {
        List<Drop> list = drops();
        if (list.isEmpty()) {
            return;
        }
        int modStr = mods.getModSize();
        while (modStr > 0) {
            ItemStack itemStack = getRandomItem(mob, list);
            if (itemStack.is(Items.ENCHANTED_BOOK)) {
                itemStack = randomEnchantedBook(level, mob.getRandom());
            } else {
                int usedStr = modStr - 5 > 0 ? 5 : modStr;
                Enchantable enchantable = itemStack.get(DataComponents.ENCHANTABLE);
                enchantRandomly(level, level.getRandom(), itemStack, enchantable == null ? 0 : enchantable.value(), usedStr);
            }
            level.addFreshEntity(new ItemEntity(level, mob.getX(), mob.getY(), mob.getZ(), itemStack));
            modStr -= 5;
        }
    }

    private static Stream<Holder<Enchantment>> tableEnchantments(ServerLevel level) {
        Optional<HolderSet.Named<Enchantment>> tag = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
                .get(EnchantmentTags.IN_ENCHANTING_TABLE);
        return tag.map(HolderSet::stream).orElseGet(Stream::empty);
    }

    /**
     * EnchantmentHelper.buildEnchantmentList with the item's enchantability as level (as the original passed it),
     * then at most (modStr + 1) / 2 of the enchantments.
     */
    private static void enchantRandomly(ServerLevel level, RandomSource rand, ItemStack itemStack, int itemEnchantability, int modStr) {
        int remainStr = (modStr + 1) / 2;
        if (itemEnchantability <= 0) {
            return;
        }
        List<EnchantmentInstance> enchantments = EnchantmentHelper.selectEnchantment(rand, itemStack, itemEnchantability, tableEnchantments(level));
        for (EnchantmentInstance data : enchantments) {
            if (remainStr <= 0) {
                break;
            }
            remainStr--;
            itemStack.enchant(data.enchantment(), data.level());
        }
    }

    /** ItemEnchantedBook.func_92109_a: one random enchantment at a random level between its minimum and maximum. */
    private static ItemStack randomEnchantedBook(ServerLevel level, RandomSource rand) {
        List<Holder<Enchantment>> all = tableEnchantments(level).toList();
        ItemStack book = new ItemStack(Items.ENCHANTED_BOOK);
        if (!all.isEmpty()) {
            Holder<Enchantment> e = all.get(rand.nextInt(all.size()));
            book.enchant(e, Mth.nextInt(rand, e.value().getMinLevel(), e.value().getMaxLevel()));
        }
        return book;
    }

    private static ItemStack getRandomItem(LivingEntity mob, List<Drop> list) {
        Drop drop = list.get(mob.level().getRandom().nextInt(list.size()));
        int stackSize = drop.size();
        if (drop.random() != 0) {
            stackSize += mob.getRandom().nextInt(drop.random()) + 1;
            stackSize -= mob.getRandom().nextInt(drop.random()) + 1;
            if (stackSize < 1) {
                stackSize = 1;
            }
        }
        ItemStack stack = new ItemStack(drop.item(), stackSize);
        if (drop.meta() > 0 && stack.isDamageableItem()) {
            stack.setDamageValue(Math.min(drop.meta(), stack.getMaxDamage() - 1));
        }
        return stack;
    }
}

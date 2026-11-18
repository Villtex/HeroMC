package betterdungeons.mob;

import betterdungeons.BetterDungeons;
import betterdungeons.api.BDEntityIds;
import com.mojang.serialization.MapCodec;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.UnaryOperator;

/**
 * Strand b1 of the mob port: the humans of BetterDungeons.registerEntities (EntityHuman and its subclasses, the human
 * bosses, the mages and the mercenary) under the ids of {@link BDEntityIds}, their attributes, the loot condition of
 * their levels and the AI mode packet of the controller screen. registerModEntity(..., 80 | 100, 3, true):
 * tracking 5 or 6 chunks, update interval 3.
 */
public final class BDHumans {
    static final DeferredRegister.Entities ENTITIES = DeferredRegister.createEntities(BetterDungeons.MOD_ID);
    static final DeferredRegister<MapCodec<? extends LootItemCondition>> CONDITIONS = DeferredRegister.create(Registries.LOOT_CONDITION_TYPE,
            BetterDungeons.MOD_ID);

    public static final List<DeferredHolder<EntityType<?>, ? extends EntityType<? extends HumanEntity>>> ALL = new ArrayList<>();

    public static final DeferredHolder<EntityType<?>, EntityType<HumanMobs.ArmoredSkeleton>> ARMORED_SKELETON = mob("ArmoredSkeleton",
            HumanMobs.ArmoredSkeleton::new, 0.6F, 1.8F, 80, b -> b);
    public static final DeferredHolder<EntityType<?>, EntityType<HumanMobs.ArmoredZombie>> ARMORED_ZOMBIE = mob("ArmoredZombie",
            HumanMobs.ArmoredZombie::new, 0.6F, 1.8F, 80, b -> b);
    /** setSize(0.7, 1.2) came before the setSize(0.6, 1.8) of EntityHuman.initCreature: the dwarf kept the human box. */
    public static final DeferredHolder<EntityType<?>, EntityType<HumanMobs.Dwarf>> DWARF = mob("Dwarf", HumanMobs.Dwarf::new, 0.6F, 1.8F, 80,
            EntityType.Builder::fireImmune);
    public static final DeferredHolder<EntityType<?>, EntityType<HumanMobs.Pirate>> PIRATE = mob("Pirate", HumanMobs.Pirate::new, 0.6F, 1.8F, 80,
            b -> b);
    public static final DeferredHolder<EntityType<?>, EntityType<HumanMobs.ArmoredPigZombie>> ARMORED_PIG_ZOMBIE = mob("ArmoredPigZombie",
            HumanMobs.ArmoredPigZombie::new, 0.6F, 1.8F, 80, EntityType.Builder::fireImmune);
    public static final DeferredHolder<EntityType<?>, EntityType<HumanMobs.Walker>> WALKER = mob("Walker", HumanMobs.Walker::new, 0.6F, 1.8F, 80,
            b -> b);
    public static final DeferredHolder<EntityType<?>, EntityType<HumanMobs.Specter>> SPECTER = mob("Specter", HumanMobs.Specter::new, 0.6F, 1.8F,
            80, b -> b);
    public static final DeferredHolder<EntityType<?>, EntityType<HumanMobs.Goblin>> GOBLIN = mob("Goblin", HumanMobs.Goblin::new, 0.7F, 0.98F, 80,
            b -> b);
    public static final DeferredHolder<EntityType<?>, EntityType<HumanMobs.SnowGoblin>> SNOW_GOBLIN = mob("SnowGoblin", HumanMobs.SnowGoblin::new,
            0.7F, 0.98F, 80, b -> b);
    public static final DeferredHolder<EntityType<?>, EntityType<HumanMobs.Triton>> TRITON = mob("Triton", HumanMobs.Triton::new, 0.6F, 1.8F, 80,
            b -> b);
    public static final DeferredHolder<EntityType<?>, EntityType<HumanMobs.Monkey>> MONKEY = mob("Monkey", HumanMobs.Monkey::new, 0.6F, 1.8F, 80,
            b -> b);
    public static final DeferredHolder<EntityType<?>, EntityType<HumanMobs.Gremlin>> GREMLIN = mob("Gremlin", HumanMobs.Gremlin::new, 0.7F, 0.98F,
            80, b -> b);
    public static final DeferredHolder<EntityType<?>, EntityType<HumanMobs.GolemSmall>> GOLEM_SMALL = mob("GolemSmall", HumanMobs.GolemSmall::new,
            0.6F, 1.8F, 80, b -> b);
    public static final DeferredHolder<EntityType<?>, EntityType<HumanMobs.Ogre>> OGRE_ARMORED = mob("OgreArmored", HumanMobs.Ogre::new, 1.0F, 2.5F,
            80, b -> b);
    public static final DeferredHolder<EntityType<?>, EntityType<HumanMobs.Minotaur>> MINOTAUR_ARMORED = mob("MinotaurArmored",
            HumanMobs.Minotaur::new, 0.6F, 1.8F, 80, b -> b);
    public static final DeferredHolder<EntityType<?>, EntityType<HumanMobs.Orc>> ORC_ARMORED = mob("OrcArmored", HumanMobs.Orc::new, 0.9F, 1.9F, 80,
            b -> b);
    public static final DeferredHolder<EntityType<?>, EntityType<HumanMobs.ArmoredEnderman>> ARMORED_ENDERMAN = mob("ArmoredEnderman",
            HumanMobs.ArmoredEnderman::new, 0.6F, 1.8F, 100, b -> b);
    public static final DeferredHolder<EntityType<?>, EntityType<HumanBosses.PirateBoss>> PIRATE_BOSS = mob("PirateBoss", HumanBosses.PirateBoss::new,
            0.6F, 1.8F, 100, b -> b);
    public static final DeferredHolder<EntityType<?>, EntityType<HumanBosses.WalkerBoss>> WALKER_BOSS = mob("WalkerBoss", HumanBosses.WalkerBoss::new,
            0.6F, 1.8F, 100, b -> b);
    public static final DeferredHolder<EntityType<?>, EntityType<MageEntity.Necromancer>> NECROMANCER = mob("Necromancer",
            MageEntity.Necromancer::new, 0.6F, 1.8F, 100, b -> b);
    public static final DeferredHolder<EntityType<?>, EntityType<MageEntity.Liche>> LICHE = mob("Liche", MageEntity.Liche::new, 0.6F, 1.8F, 100,
            EntityType.Builder::fireImmune);
    public static final DeferredHolder<EntityType<?>, EntityType<MageEntity.PigMage>> PIG_MAGE = mob("PigMage", MageEntity.PigMage::new, 0.6F, 1.8F,
            100, EntityType.Builder::fireImmune);
    public static final DeferredHolder<EntityType<?>, EntityType<HumanBosses.SpecterBoss>> SPECTER_BOSS = mob("SpecterBoss",
            HumanBosses.SpecterBoss::new, 0.6F, 1.8F, 100, b -> b);
    public static final DeferredHolder<EntityType<?>, EntityType<HumanBosses.GremlinBoss>> GREMLIN_BOSS = mob("GremlinBoss",
            HumanBosses.GremlinBoss::new, 0.7F, 0.98F, 100, b -> b);
    public static final DeferredHolder<EntityType<?>, EntityType<HumanBosses.MinotaurBoss>> MINOTAUR_BOSS = mob("MinotaurBoss",
            HumanBosses.MinotaurBoss::new, 0.6F, 1.8F, 100, b -> b);
    public static final DeferredHolder<EntityType<?>, EntityType<HumanBosses.GoblinBoss>> GOBLIN_BOSS = mob("goblinBoss", HumanBosses.GoblinBoss::new,
            0.7F, 0.98F, 100, b -> b);
    public static final DeferredHolder<EntityType<?>, EntityType<KnightEntity>> FRIENDLY_HUMAN = ENTITIES.registerEntityType(
            BDEntityIds.NAMES.get("FriendlyHuman"), KnightEntity::new, MobCategory.CREATURE,
            b -> b.sized(0.6F, 1.8F).eyeHeight(1.62F).clientTrackingRange(5).updateInterval(3));

    static {
        ALL.add(FRIENDLY_HUMAN);
    }

    public static final DeferredHolder<MapCodec<? extends LootItemCondition>, MapCodec<MobLevelCondition>> MOB_LEVEL = CONDITIONS.register("mob_level",
            () -> MobLevelCondition.MAP_CODEC);

    /**
     * The controller screen (sneak + use with an assigned controller) lives on the client; the client half sets this.
     * Arguments: the player and the controller stack.
     */
    public static BiConsumer<Player, ItemStack> openControlScreen = (player, stack) -> {
    };

    private BDHumans() {
    }

    private static <E extends HumanEntity> DeferredHolder<EntityType<?>, EntityType<E>> mob(String oldName, EntityType.EntityFactory<E> factory,
            float width, float height, int trackingRange, UnaryOperator<EntityType.Builder<E>> extra) {
        String path = BDEntityIds.NAMES.get(oldName);
        DeferredHolder<EntityType<?>, EntityType<E>> holder = ENTITIES.registerEntityType(path, factory, MobCategory.MONSTER,
                b -> extra.apply(b.sized(width, height).eyeHeight(height * 0.85F).clientTrackingRange(trackingRange / 16).updateInterval(3)));
        ALL.add(holder);
        return holder;
    }

    private static void attributes(EntityAttributeCreationEvent event) {
        for (DeferredHolder<EntityType<?>, ? extends EntityType<? extends HumanEntity>> type : ALL) {
            event.put(type.get(), HumanEntity.createAttributes().build());
        }
    }

    /** PacketEntityAIMode: the controller screen sets the AI mode of a mob. */
    public record AIModePayload(int entityId, int mode) implements CustomPacketPayload {
        public static final Type<AIModePayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(BetterDungeons.MOD_ID, "ai_mode"));
        public static final StreamCodec<ByteBuf, AIModePayload> CODEC = StreamCodec.composite(ByteBufCodecs.VAR_INT, AIModePayload::entityId,
                ByteBufCodecs.VAR_INT, AIModePayload::mode, AIModePayload::new);

        @Override
        public Type<AIModePayload> type() {
            return TYPE;
        }
    }

    /** The original trusted the client; here only a player within 64 blocks may order, and only the five modes exist. */
    private static void aiMode(AIModePayload payload, IPayloadContext context) {
        if (context.player() instanceof ServerPlayer player && payload.mode() >= 0 && payload.mode() <= 4) {
            Entity e = player.level().getEntity(payload.entityId());
            if (e instanceof HumanEntity human && human.distanceToSqr(player) < 64.0 * 64.0) {
                human.AIMode = payload.mode();
            }
        }
    }

    private static void payloads(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToServer(AIModePayload.TYPE, AIModePayload.CODEC, BDHumans::aiMode);
    }

    public static void register(IEventBus modBus) {
        ENTITIES.register(modBus);
        CONDITIONS.register(modBus);
        modBus.addListener(BDHumans::attributes);
        modBus.addListener(BDHumans::payloads);
    }
}

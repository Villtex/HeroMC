package slimeknights.mantle.data.predicate.entity;

import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nullable;

/**
 * Port: Minecraft 1.20.1's {@code net.minecraft.world.entity.MobType}, removed in 1.20.5 in favour of entity type
 * tags. Kept for {@link MobTypePredicate} (JSON {@code "mobs": "undead"} etc.); a mob's type is read from the vanilla tags.
 */
public enum MobType {
  UNDEFINED(null),
  UNDEAD(EntityTypeTags.UNDEAD),
  ARTHROPOD(EntityTypeTags.ARTHROPOD),
  ILLAGER(EntityTypeTags.ILLAGER),
  WATER(EntityTypeTags.AQUATIC);

  @Nullable
  private final TagKey<EntityType<?>> tag;

  MobType(@Nullable TagKey<EntityType<?>> tag) {
    this.tag = tag;
  }

  /** Mob type of the entity as 1.20.1 would have reported it */
  public static MobType of(LivingEntity entity) {
    for (MobType type : values()) {
      if (type.tag != null && entity.typeHolder().is(type.tag)) {
        return type;
      }
    }
    return UNDEFINED;
  }
}
